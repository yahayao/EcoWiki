"""
管理员路由：用户管理、文章管理、系统统计
"""
from typing import Optional, List

from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session
from sqlalchemy import func

from database import get_db
from models.user import User, Role
from models.article import Article
from models.comment import Comment
from models.message import Message
from schemas.user import UserOut
from schemas.article import ArticleOut, ArticleListOut, ArticleCreateRequest, ArticleUpdateRequest
from schemas.common import ApiResponse, PageResult
from core.security import get_current_user, require_admin, hash_password

router = APIRouter(prefix="/admin", tags=["管理员"])


# ─── 系统统计 ─────────────────────────────────────────────────────────────────
@router.get("/statistics")
def statistics(
    _: User = Depends(require_admin),
    db: Session = Depends(get_db),
):
    user_count    = db.query(func.count(User.user_id)).scalar()
    article_count = db.query(func.count(Article.article_id)).scalar()
    comment_count = db.query(func.count(Comment.comment_id)).filter(Comment.is_deleted == False).scalar()
    message_count = db.query(func.count(Message.message_id)).scalar()
    total_views   = db.query(func.sum(Article.views)).scalar() or 0
    total_likes   = db.query(func.sum(Article.likes)).scalar() or 0

    return ApiResponse.ok(data={
        "user_count":    user_count,
        "article_count": article_count,
        "comment_count": comment_count,
        "message_count": message_count,
        "total_views":   total_views,
        "total_likes":   total_likes,
    })


# ─── 用户管理 ─────────────────────────────────────────────────────────────────
@router.get("/users", response_model=ApiResponse[PageResult[UserOut]])
def list_users(
    page: int = Query(0, ge=0),
    size: int = Query(20, ge=1, le=100),
    keyword: Optional[str] = None,
    _: User = Depends(require_admin),
    db: Session = Depends(get_db),
):
    q = db.query(User)
    if keyword:
        q = q.filter(
            (User.username.contains(keyword)) | (User.email.contains(keyword))
        )
    q = q.order_by(User.user_id.asc())

    total = q.count()
    items = q.offset(page * size).limit(size).all()
    return ApiResponse.ok(data=PageResult(
        content=[UserOut.model_validate(u) for u in items],
        total_elements=total,
        total_pages=(total + size - 1) // size if size else 0,
        page=page,
        size=size,
        number_of_elements=len(items),
    ))


@router.get("/users/{user_id}", response_model=ApiResponse[UserOut])
def get_user(
    user_id: int,
    _: User = Depends(require_admin),
    db: Session = Depends(get_db),
):
    user = db.query(User).filter(User.user_id == user_id).first()
    if not user:
        raise HTTPException(status_code=404, detail="用户不存在")
    return ApiResponse.ok(data=UserOut.model_validate(user))


@router.put("/users/{user_id}")
def update_user(
    user_id: int,
    body: dict,
    _: User = Depends(require_admin),
    db: Session = Depends(get_db),
):
    user = db.query(User).filter(User.user_id == user_id).first()
    if not user:
        raise HTTPException(status_code=404, detail="用户不存在")

    allowed = {"full_name", "email", "active", "role_id", "gender", "bio"}
    for k, v in body.items():
        if k in allowed:
            setattr(user, k, v)

    db.commit()
    db.refresh(user)
    return ApiResponse.ok(data=UserOut.model_validate(user), message="用户信息更新成功")


@router.put("/users/{user_id}/toggle-active")
def toggle_active(
    user_id: int,
    _: User = Depends(require_admin),
    db: Session = Depends(get_db),
):
    user = db.query(User).filter(User.user_id == user_id).first()
    if not user:
        raise HTTPException(status_code=404, detail="用户不存在")
    user.active = not user.active
    db.commit()
    return ApiResponse.ok(data={"active": user.active}, message="状态切换成功")


@router.delete("/users/{user_id}")
def delete_user(
    user_id: int,
    admin: User = Depends(require_admin),
    db: Session = Depends(get_db),
):
    if user_id == admin.user_id:
        raise HTTPException(status_code=400, detail="不能删除自己的账号")
    user = db.query(User).filter(User.user_id == user_id).first()
    if not user:
        raise HTTPException(status_code=404, detail="用户不存在")
    db.delete(user)
    db.commit()
    return ApiResponse.ok(message="用户删除成功")


@router.put("/users/{user_id}/role")
def set_role(
    user_id: int,
    body: dict,
    _: User = Depends(require_admin),
    db: Session = Depends(get_db),
):
    user = db.query(User).filter(User.user_id == user_id).first()
    if not user:
        raise HTTPException(status_code=404, detail="用户不存在")
    user.role_id = body.get("role_id")
    db.commit()
    return ApiResponse.ok(message="角色分配成功")


# ─── 文章管理（管理员视角） ────────────────────────────────────────────────────
@router.get("/articles", response_model=ApiResponse[PageResult[ArticleListOut]])
def admin_list_articles(
    page: int = Query(0, ge=0),
    size: int = Query(20, ge=1, le=100),
    status: Optional[str] = None,
    keyword: Optional[str] = None,
    _: User = Depends(require_admin),
    db: Session = Depends(get_db),
):
    q = db.query(Article)
    if status:
        q = q.filter(Article.status == status)
    if keyword:
        q = q.filter(
            (Article.title.contains(keyword)) | (Article.author.contains(keyword))
        )
    q = q.order_by(Article.publish_date.desc())
    total = q.count()
    items = q.offset(page * size).limit(size).all()
    return ApiResponse.ok(data=PageResult(
        content=[ArticleListOut.model_validate(a) for a in items],
        total_elements=total,
        total_pages=(total + size - 1) // size if size else 0,
        page=page,
        size=size,
        number_of_elements=len(items),
    ))


@router.post("/articles", response_model=ApiResponse[ArticleOut])
def admin_create_article(
    body: ArticleCreateRequest,
    admin: User = Depends(require_admin),
    db: Session = Depends(get_db),
):
    from datetime import datetime
    from models.tag import Tag as TagModel
    article = Article(
        title=body.title,
        content=body.content,
        category=body.category,
        author=admin.username,
        author_id=admin.user_id,
        publish_date=datetime.now(),
        status=body.status or "published",
    )
    if body.tag_ids:
        article.tags = db.query(TagModel).filter(TagModel.tag_id.in_(body.tag_ids)).all()
    db.add(article)
    db.commit()
    db.refresh(article)
    return ApiResponse.ok(data=ArticleOut.model_validate(article), message="文章创建成功")


@router.put("/articles/{article_id}", response_model=ApiResponse[ArticleOut])
def admin_update_article(
    article_id: int,
    body: ArticleUpdateRequest,
    _: User = Depends(require_admin),
    db: Session = Depends(get_db),
):
    article = db.query(Article).filter(Article.article_id == article_id).first()
    if not article:
        raise HTTPException(status_code=404, detail="文章不存在")

    if body.title is not None:   article.title    = body.title
    if body.content is not None: article.content  = body.content
    if body.category is not None: article.category = body.category
    if body.status is not None:  article.status   = body.status
    if body.tag_ids is not None:
        from models.tag import Tag as TagModel
        article.tags = db.query(TagModel).filter(TagModel.tag_id.in_(body.tag_ids)).all()

    db.commit()
    db.refresh(article)
    return ApiResponse.ok(data=ArticleOut.model_validate(article), message="文章更新成功")


@router.delete("/articles/{article_id}")
def admin_delete_article(
    article_id: int,
    _: User = Depends(require_admin),
    db: Session = Depends(get_db),
):
    article = db.query(Article).filter(Article.article_id == article_id).first()
    if not article:
        raise HTTPException(status_code=404, detail="文章不存在")
    db.delete(article)
    db.commit()
    return ApiResponse.ok(message="文章删除成功")


# ─── 角色管理 ─────────────────────────────────────────────────────────────────
@router.get("/roles")
def list_roles(_: User = Depends(require_admin), db: Session = Depends(get_db)):
    roles = db.query(Role).all()
    return ApiResponse.ok(data=[{"role_id": r.role_id, "role_name": r.role_name, "description": r.description} for r in roles])
