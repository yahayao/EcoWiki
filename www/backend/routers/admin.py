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


@router.put("/users/{user_id}/group")
def set_user_group(
    user_id: int,
    body: dict,
    _: User = Depends(require_admin),
    db: Session = Depends(get_db),
):
    """通过 userGroup 字符串设置角色（admin=1, user=2, moderator=3）"""
    user = db.query(User).filter(User.user_id == user_id).first()
    if not user:
        raise HTTPException(status_code=404, detail="用户不存在")
    group_map = {"admin": 1, "superadmin": 1, "user": 2, "moderator": 3}
    user_group = body.get("user_group") or body.get("userGroup")
    user.role_id = group_map.get(user_group, 2)
    db.commit()
    return ApiResponse.ok(message="角色分配成功")


@router.put("/users/{user_id}/status")
def set_user_status(
    user_id: int,
    body: dict,
    _: User = Depends(require_admin),
    db: Session = Depends(get_db),
):
    """显式设置用户激活状态"""
    user = db.query(User).filter(User.user_id == user_id).first()
    if not user:
        raise HTTPException(status_code=404, detail="用户不存在")
    user.active = bool(body.get("active", True))
    db.commit()
    return ApiResponse.ok(data={"active": user.active}, message="状态更新成功")


@router.put("/users/{user_id}/restore")
def restore_user(
    user_id: int,
    _: User = Depends(require_admin),
    db: Session = Depends(get_db),
):
    """恢复被禁用的用户"""
    user = db.query(User).filter(User.user_id == user_id).first()
    if not user:
        raise HTTPException(status_code=404, detail="用户不存在")
    user.active = True
    db.commit()
    return ApiResponse.ok(data={"active": True}, message="用户已恢复")


@router.get("/users/active")
def get_active_users(
    _: User = Depends(require_admin),
    db: Session = Depends(get_db),
):
    """获取所有激活状态的用户列表"""
    users = db.query(User).filter(User.active == True).all()
    return ApiResponse.ok(data=[UserOut.model_validate(u) for u in users])


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


@router.post("/roles")
def create_role(body: dict, _: User = Depends(require_admin), db: Session = Depends(get_db)):
    role_name = body.get("role_name") or body.get("roleName")
    if not role_name:
        raise HTTPException(status_code=400, detail="角色名不能为空")
    if db.query(Role).filter(Role.role_name == role_name).first():
        raise HTTPException(status_code=400, detail="角色名已存在")
    role = Role(role_name=role_name, description=body.get("description"))
    db.add(role)
    db.commit()
    db.refresh(role)
    return ApiResponse.ok(data={"role_id": role.role_id, "role_name": role.role_name, "description": role.description}, message="角色创建成功")


@router.put("/roles/{role_id}")
def update_role(role_id: int, body: dict, _: User = Depends(require_admin), db: Session = Depends(get_db)):
    role = db.query(Role).filter(Role.role_id == role_id).first()
    if not role:
        raise HTTPException(status_code=404, detail="角色不存在")
    if "role_name" in body or "roleName" in body:
        role.role_name = body.get("role_name") or body.get("roleName")
    if "description" in body:
        role.description = body.get("description")
    db.commit()
    db.refresh(role)
    return ApiResponse.ok(data={"role_id": role.role_id, "role_name": role.role_name, "description": role.description}, message="角色更新成功")


@router.delete("/roles/{role_id}")
def delete_role(role_id: int, _: User = Depends(require_admin), db: Session = Depends(get_db)):
    role = db.query(Role).filter(Role.role_id == role_id).first()
    if not role:
        raise HTTPException(status_code=404, detail="角色不存在")
    db.delete(role)
    db.commit()
    return ApiResponse.ok(message="角色删除成功")


@router.get("/roles/{role_id}/permissions")
def get_role_permissions(role_id: int, _: User = Depends(require_admin), db: Session = Depends(get_db)):
    from models.user import Permission
    role = db.query(Role).filter(Role.role_id == role_id).first()
    if not role:
        raise HTTPException(status_code=404, detail="角色不存在")
    perms = [{"permission_id": p.permission_id, "permission_name": p.permission_name, "description": p.description} for p in role.permissions]
    return ApiResponse.ok(data=perms)


@router.put("/roles/{role_id}/permissions")
def update_role_permissions(role_id: int, body: dict, _: User = Depends(require_admin), db: Session = Depends(get_db)):
    from models.user import Permission
    role = db.query(Role).filter(Role.role_id == role_id).first()
    if not role:
        raise HTTPException(status_code=404, detail="角色不存在")
    perm_ids = body.get("permission_ids") or body.get("permissionIds") or []
    perms = db.query(Permission).filter(Permission.permission_id.in_(perm_ids)).all()
    role.permissions = perms
    db.commit()
    return ApiResponse.ok(message="权限更新成功")


# ─── 权限管理 ─────────────────────────────────────────────────────────────────
@router.get("/permissions")
def list_permissions(_: User = Depends(require_admin), db: Session = Depends(get_db)):
    from models.user import Permission
    perms = db.query(Permission).all()
    return ApiResponse.ok(data=[{"permission_id": p.permission_id, "permission_name": p.permission_name, "description": p.description} for p in perms])


@router.post("/permissions")
def create_permission(body: dict, _: User = Depends(require_admin), db: Session = Depends(get_db)):
    from models.user import Permission
    perm_name = body.get("permission_name") or body.get("permissionName")
    if not perm_name:
        raise HTTPException(status_code=400, detail="权限名不能为空")
    if db.query(Permission).filter(Permission.permission_name == perm_name).first():
        raise HTTPException(status_code=400, detail="权限名已存在")
    perm = Permission(permission_name=perm_name, description=body.get("description"))
    db.add(perm)
    db.commit()
    db.refresh(perm)
    return ApiResponse.ok(data={"permission_id": perm.permission_id, "permission_name": perm.permission_name, "description": perm.description}, message="权限创建成功")


@router.put("/permissions/{permission_id}")
def update_permission(permission_id: int, body: dict, _: User = Depends(require_admin), db: Session = Depends(get_db)):
    from models.user import Permission
    perm = db.query(Permission).filter(Permission.permission_id == permission_id).first()
    if not perm:
        raise HTTPException(status_code=404, detail="权限不存在")
    if "permission_name" in body or "permissionName" in body:
        perm.permission_name = body.get("permission_name") or body.get("permissionName")
    if "description" in body:
        perm.description = body.get("description")
    db.commit()
    db.refresh(perm)
    return ApiResponse.ok(data={"permission_id": perm.permission_id, "permission_name": perm.permission_name, "description": perm.description}, message="权限更新成功")


@router.delete("/permissions/{permission_id}")
def delete_permission(permission_id: int, _: User = Depends(require_admin), db: Session = Depends(get_db)):
    from models.user import Permission
    perm = db.query(Permission).filter(Permission.permission_id == permission_id).first()
    if not perm:
        raise HTTPException(status_code=404, detail="权限不存在")
    db.delete(perm)
    db.commit()
    return ApiResponse.ok(message="权限删除成功")
