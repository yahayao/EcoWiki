"""
文章路由：增删改查、搜索、分类、版本、草稿、审核
"""
from datetime import datetime
from typing import Optional, List

from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy import or_
from sqlalchemy.orm import Session

from database import get_db
from models.article import (
    Article, ArticleDraft, ArticleReview, ArticleVersion,
    UserArticleLike, UserArticleFavorite, article_tags_table,
)
from models.tag import Tag
from models.user import User
from schemas.article import (
    ArticleOut, ArticleListOut, ArticleCreateRequest, ArticleUpdateRequest,
    DraftOut, DraftCreateRequest, DraftUpdateRequest, ReviewRequest, VersionOut,
)
from schemas.common import ApiResponse, PageResult
from core.security import get_current_user, get_current_user_optional, require_admin

router = APIRouter(prefix="/articles", tags=["文章"])


# ─── 辅助函数 ────────────────────────────────────────────────────────────────
def _paginate(query, page: int, size: int, schema):
    total = query.count()
    items = query.offset(page * size).limit(size).all()
    return PageResult(
        content=[schema.model_validate(i) for i in items],
        total_elements=total,
        total_pages=(total + size - 1) // size if size else 0,
        page=page,
        size=size,
        number_of_elements=len(items),
    )


def _get_article_or_404(article_id: int, db: Session) -> Article:
    article = db.query(Article).filter(Article.article_id == article_id).first()
    if not article:
        raise HTTPException(status_code=404, detail="文章不存在")
    return article


# ─── 列表 & 搜索 ──────────────────────────────────────────────────────────────
@router.get("", response_model=ApiResponse[PageResult[ArticleListOut]])
def list_articles(
    page: int = Query(0, ge=0),
    size: int = Query(10, ge=1, le=100),
    category: Optional[str] = None,
    author: Optional[str] = None,
    tags: Optional[str] = None,
    sort: Optional[str] = Query("newest", description="newest/oldest/views/likes"),
    db: Session = Depends(get_db),
):
    q = db.query(Article).filter(Article.status == "published")
    if category:
        q = q.filter(Article.category == category)
    if author:
        q = q.filter(Article.author == author)
    if tags:
        tag_names = [t.strip() for t in tags.split(",") if t.strip()]
        if tag_names:
            q = q.join(article_tags_table, Article.article_id == article_tags_table.c.article_id)
            q = q.join(Tag, article_tags_table.c.tag_id == Tag.tag_id)
            q = q.filter(Tag.tag_name.in_(tag_names)).distinct()

    if sort == "oldest":
        q = q.order_by(Article.publish_date.asc())
    elif sort == "views":
        q = q.order_by(Article.views.desc())
    elif sort == "likes":
        q = q.order_by(Article.likes.desc())
    else:
        q = q.order_by(Article.publish_date.desc())

    return ApiResponse.ok(data=_paginate(q, page, size, ArticleListOut))


@router.get("/search", response_model=ApiResponse[PageResult[ArticleListOut]])
def search_articles(
    keyword: str = Query(..., min_length=1),
    page: int = Query(0, ge=0),
    size: int = Query(10, ge=1, le=100),
    category: Optional[str] = None,
    db: Session = Depends(get_db),
):
    q = db.query(Article).filter(
        Article.status == "published",
        or_(
            Article.title.contains(keyword),
            Article.content.contains(keyword),
            Article.author.contains(keyword),
        ),
    )
    if category:
        q = q.filter(Article.category == category)
    q = q.order_by(Article.publish_date.desc())
    return ApiResponse.ok(data=_paginate(q, page, size, ArticleListOut))


@router.get("/categories")
def get_categories(db: Session = Depends(get_db)):
    rows = (
        db.query(Article.category)
        .filter(Article.status == "published", Article.category.isnot(None))
        .distinct()
        .all()
    )
    return ApiResponse.ok(data=[r[0] for r in rows if r[0]])


@router.get("/statistics")
def article_statistics(db: Session = Depends(get_db)):
    from sqlalchemy import func
    total = db.query(func.count(Article.article_id)).filter(Article.status == "published").scalar()
    views = db.query(func.sum(Article.views)).filter(Article.status == "published").scalar() or 0
    likes = db.query(func.sum(Article.likes)).filter(Article.status == "published").scalar() or 0
    return ApiResponse.ok(data={"total_articles": total, "total_views": views, "total_likes": likes})


# ─── 标题检查 / 解析 ──────────────────────────────────────────────────────────
@router.get("/check-title")
def check_title(title: str, db: Session = Depends(get_db)):
    exists = db.query(Article).filter(Article.title == title).first() is not None
    return ApiResponse.ok(data={"exists": exists})


@router.get("/title/{title}/id")
def get_id_by_title(
    title: str,
    db: Session = Depends(get_db),
    current_user: Optional[User] = Depends(get_current_user_optional),
):
    # 优先查已发布文章
    article = db.query(Article).filter(Article.title == title, Article.status == "published").first()
    if not article:
        # 已登录用户可查找自己的 pending 文章
        if current_user:
            article = db.query(Article).filter(
                Article.title == title,
                Article.status.in_(["pending", "draft"]),
                Article.author_id == current_user.user_id,
            ).first()
            # 管理员可查任意状态
            if not article and current_user.role_id == 1:
                article = db.query(Article).filter(Article.title == title).first()
    if not article:
        raise HTTPException(status_code=404, detail="文章不存在")
    return ApiResponse.ok(data=article.article_id)


@router.post("/parse")
def parse_wiki(body: dict):
    """简单返回文章内容（实际渲染由前端完成）"""
    content = body.get("content", "")
    return ApiResponse.ok(data=content)


# ─── 热门 / 最新（首页专用） ───────────────────────────────────────────────────
@router.get("/popular", response_model=ApiResponse[List[ArticleListOut]])
def popular_articles(
    limit: int = Query(6, ge=1, le=50),
    db: Session = Depends(get_db),
):
    """按浏览量排序返回热门文章"""
    items = (
        db.query(Article)
        .filter(Article.status == "published")
        .order_by(Article.views.desc())
        .limit(limit)
        .all()
    )
    return ApiResponse.ok(data=[ArticleListOut.model_validate(a) for a in items])


@router.get("/latest", response_model=ApiResponse[List[ArticleListOut]])
def latest_articles(
    limit: int = Query(8, ge=1, le=50),
    db: Session = Depends(get_db),
):
    """按发布时间排序返回最新文章"""
    items = (
        db.query(Article)
        .filter(Article.status == "published")
        .order_by(Article.publish_date.desc())
        .limit(limit)
        .all()
    )
    return ApiResponse.ok(data=[ArticleListOut.model_validate(a) for a in items])


# ─── 单篇文章 ─────────────────────────────────────────────────────────────────
@router.get("/{article_id}", response_model=ApiResponse[ArticleOut])
def get_article(
    article_id: int,
    db: Session = Depends(get_db),
    current_user: Optional[User] = Depends(get_current_user_optional),
):
    article = _get_article_or_404(article_id, db)
    # 非管理员只能看已发布文章（或自己的文章）
    if article.status != "published":
        if not current_user:
            raise HTTPException(status_code=403, detail="无权查看此文章")
        if current_user.role_id != 1 and article.author_id != current_user.user_id:
            raise HTTPException(status_code=403, detail="无权查看此文章")

    article.views = (article.views or 0) + 1
    db.commit()
    db.refresh(article)
    return ApiResponse.ok(data=ArticleOut.model_validate(article))


# ─── 创建文章 ─────────────────────────────────────────────────────────────────
@router.post("", response_model=ApiResponse[ArticleOut])
def create_article(
    body: ArticleCreateRequest,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    # 非管理员不能直接发布，强制走审核流程
    if not (current_user.role_id == 1 or current_user.role_id == 4):
        article_status = "pending"
    else:
        article_status = body.status or "published"

    article = Article(
        title=body.title,
        content=body.content,
        category=body.category,
        author=current_user.username,
        author_id=current_user.user_id,
        publish_date=datetime.now(),
        status=article_status,
    )
    # 处理标签
    if body.tag_ids:
        tags = db.query(Tag).filter(Tag.tag_id.in_(body.tag_ids)).all()
        article.tags = tags

    db.add(article)
    db.flush()  # 获取 article_id

    # 非管理员同步创建待审核草稿，纳入审核队列
    if article_status == "pending":
        draft = ArticleDraft(
            article_id=article.article_id,
            title=body.title,
            content=body.content,
            category=body.category,
            author=current_user.username,
            author_id=current_user.user_id,
            status="pending",
            submitted_at=datetime.now(),
        )
        db.add(draft)

    db.commit()
    db.refresh(article)

    # 创建第一个版本记录
    _save_version(article, current_user, db, "初始版本")
    db.commit()

    msg = "文章已提交审核，等待管理员审核" if article_status == "pending" else "文章创建成功"
    return ApiResponse.ok(data=ArticleOut.model_validate(article), message=msg)


# ─── 更新文章 ─────────────────────────────────────────────────────────────────
@router.put("/{article_id}", response_model=ApiResponse[ArticleOut])
def update_article(
    article_id: int,
    body: ArticleUpdateRequest,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    article = _get_article_or_404(article_id, db)
    _check_article_permission(article, current_user)

    # 非管理员不能直接修改文章，提交更新草稿走审核流程
    if not (current_user.role_id == 1 or current_user.role_id == 4):
        draft = ArticleDraft(
            article_id=article_id,
            title=body.title if body.title is not None else article.title,
            content=body.content if body.content is not None else article.content,
            category=body.category if body.category is not None else article.category,
            author=current_user.username,
            author_id=current_user.user_id,
            status="pending",
            submitted_at=datetime.now(),
        )
        db.add(draft)
        db.commit()
        db.refresh(article)
        return ApiResponse.ok(data=ArticleOut.model_validate(article), message="更新已提交审核，等待管理员审核")

    if body.title is not None:
        article.title = body.title
    if body.content is not None:
        article.content = body.content
    if body.category is not None:
        article.category = body.category
    if body.status is not None:
        article.status = body.status
    if body.tag_ids is not None:
        tags = db.query(Tag).filter(Tag.tag_id.in_(body.tag_ids)).all()
        article.tags = tags

    db.commit()
    db.refresh(article)

    # 保存版本
    _save_version(article, current_user, db, "编辑更新")
    db.commit()

    return ApiResponse.ok(data=ArticleOut.model_validate(article), message="文章更新成功")


# ─── 删除文章 ─────────────────────────────────────────────────────────────────
@router.delete("/{article_id}")
def delete_article(
    article_id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    article = _get_article_or_404(article_id, db)
    _check_article_permission(article, current_user)
    db.delete(article)
    db.commit()
    return ApiResponse.ok(message="文章删除成功")


# ─── 点赞 / 取消点赞 ───────────────────────────────────────────────────────────
@router.post("/{article_id}/like")
def toggle_like(
    article_id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    article = _get_article_or_404(article_id, db)
    existing = db.query(UserArticleLike).filter_by(
        user_id=current_user.user_id, article_id=article_id
    ).first()

    if existing:
        db.delete(existing)
        article.likes = max(0, (article.likes or 0) - 1)
        liked = False
    else:
        db.add(UserArticleLike(user_id=current_user.user_id, article_id=article_id))
        article.likes = (article.likes or 0) + 1
        liked = True

    db.commit()
    return ApiResponse.ok(data={"liked": liked, "likes": article.likes})


# ─── 收藏 / 取消收藏 ───────────────────────────────────────────────────────────
@router.post("/{article_id}/favorite")
def toggle_favorite(
    article_id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    _get_article_or_404(article_id, db)
    existing = db.query(UserArticleFavorite).filter_by(
        user_id=current_user.user_id, article_id=article_id
    ).first()

    if existing:
        db.delete(existing)
        favorited = False
    else:
        db.add(UserArticleFavorite(user_id=current_user.user_id, article_id=article_id))
        favorited = True

    db.commit()
    return ApiResponse.ok(data={"favorited": favorited})


# ─── 点赞/收藏状态查询 (需登录) ──────────────────────────────────────────────
@router.get("/{article_id}/like-status")
def like_status(
    article_id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    _get_article_or_404(article_id, db)
    liked = db.query(UserArticleLike).filter_by(
        user_id=current_user.user_id, article_id=article_id
    ).first() is not None
    return ApiResponse.ok(data={"liked": liked})


@router.get("/{article_id}/favorite-status")
def favorite_status(
    article_id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    _get_article_or_404(article_id, db)
    favorited = db.query(UserArticleFavorite).filter_by(
        user_id=current_user.user_id, article_id=article_id
    ).first() is not None
    return ApiResponse.ok(data={"favorited": favorited})


# ─── 发布文章（便捷接口）──────────────────────────────────────────────────────
@router.put("/{article_id}/publish", response_model=ApiResponse[ArticleOut])
def publish_article(
    article_id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    article = _get_article_or_404(article_id, db)
    _check_article_permission(article, current_user)
    article.status = "published"
    db.commit()
    db.refresh(article)
    return ApiResponse.ok(data=ArticleOut.model_validate(article), message="文章发布成功")


# ─── 用户自己的文章列表 ────────────────────────────────────────────────────────
@router.get("/user/{user_id}", response_model=ApiResponse[PageResult[ArticleListOut]])
def user_articles(
    user_id: int,
    page: int = Query(0, ge=0),
    size: int = Query(10, ge=1, le=100),
    db: Session = Depends(get_db),
    current_user: Optional[User] = Depends(get_current_user_optional),
):
    q = db.query(Article).filter(Article.author_id == user_id)
    if not current_user or (current_user.user_id != user_id and current_user.role_id != 1):
        q = q.filter(Article.status == "published")
    q = q.order_by(Article.publish_date.desc())
    return ApiResponse.ok(data=_paginate(q, page, size, ArticleListOut))


# ─── 文章版本历史 ──────────────────────────────────────────────────────────────
@router.get("/{article_id}/versions", response_model=ApiResponse[List[VersionOut]])
def get_versions(
    article_id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    article = _get_article_or_404(article_id, db)
    _check_article_permission(article, current_user)
    versions = (
        db.query(ArticleVersion)
        .filter(ArticleVersion.article_id == article_id)
        .order_by(ArticleVersion.version_number.desc())
        .all()
    )
    return ApiResponse.ok(data=[VersionOut.model_validate(v) for v in versions])


# ─── 版本详情 ─────────────────────────────────────────────────────────────────
@router.get("/{article_id}/versions/latest")
def get_latest_version(
    article_id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    _get_article_or_404(article_id, db)
    version = (
        db.query(ArticleVersion)
        .filter(ArticleVersion.article_id == article_id)
        .order_by(ArticleVersion.version_number.desc())
        .first()
    )
    if not version:
        raise HTTPException(status_code=404, detail="无版本记录")
    return ApiResponse.ok(data=VersionOut.model_validate(version))


@router.get("/{article_id}/versions/stats")
def get_version_stats(
    article_id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    from sqlalchemy import func
    _get_article_or_404(article_id, db)
    total = db.query(func.count(ArticleVersion.version_id)).filter(
        ArticleVersion.article_id == article_id
    ).scalar() or 0
    return ApiResponse.ok(data={
        "total_versions": total,
        "article_id": article_id,
    })


@router.get("/{article_id}/versions/{version_number}")
def get_version_by_number(
    article_id: int,
    version_number: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    _get_article_or_404(article_id, db)
    version = db.query(ArticleVersion).filter(
        ArticleVersion.article_id == article_id,
        ArticleVersion.version_number == version_number,
    ).first()
    if not version:
        raise HTTPException(status_code=404, detail="版本不存在")
    return ApiResponse.ok(data=VersionOut.model_validate(version))


@router.post("/{article_id}/versions")
def create_version_manually(
    article_id: int,
    body: dict,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    article = _get_article_or_404(article_id, db)
    _check_article_permission(article, current_user)
    _save_version(article, current_user, db, body.get("change_summary", "手动创建"))
    db.commit()
    return ApiResponse.ok(message="版本创建成功")


@router.post("/{article_id}/versions/{version_number}/restore")
def restore_version(
    article_id: int,
    version_number: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    article = _get_article_or_404(article_id, db)
    _check_article_permission(article, current_user)
    version = db.query(ArticleVersion).filter(
        ArticleVersion.article_id == article_id,
        ArticleVersion.version_number == version_number,
    ).first()
    if not version:
        raise HTTPException(status_code=404, detail="版本不存在")

    # 保存当前状态为新版本
    _save_version(article, current_user, db, f"恢复至版本 {version_number}")

    # 恢复到目标版本
    if version.title:     article.title    = version.title
    if version.content:   article.content  = version.content
    if version.category:  article.category = version.category
    db.commit()
    db.refresh(article)
    return ApiResponse.ok(data=ArticleOut.model_validate(article), message="版本恢复成功")


# ─── 贡献者 ───────────────────────────────────────────────────────────────────
@router.get("/{article_id}/contributors")
def get_contributors(
    article_id: int,
    db: Session = Depends(get_db),
):
    _get_article_or_404(article_id, db)
    from sqlalchemy import func
    rows = (
        db.query(
            User.username,
            User.full_name,
            User.avatar_url,
            func.count(ArticleVersion.version_id).label("edit_count"),
            func.max(ArticleVersion.created_at).label("latest_edit"),
        )
        .join(ArticleVersion, ArticleVersion.editor_id == User.user_id)
        .filter(ArticleVersion.article_id == article_id)
        .group_by(User.user_id, User.username, User.full_name, User.avatar_url)
        .order_by(func.count(ArticleVersion.version_id).desc())
        .all()
    )
    data = [
        {
            "username":      r.username,
            "display_name":  r.full_name or r.username,
            "avatar_url":    r.avatar_url or "",
            "edit_count":    r.edit_count,
            "latest_edit":   r.latest_edit.isoformat() if r.latest_edit else None,
        }
        for r in rows
    ]
    return ApiResponse.ok(data=data)


# ─── 文章草稿 ─────────────────────────────────────────────────────────────────
@router.post("/{article_id}/drafts", response_model=ApiResponse[DraftOut])
def create_draft(
    article_id: int,
    body: DraftCreateRequest,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    """编辑现有文章：先保存草稿（status=draft），再手动提交审核。
    若该用户已有 pending 状态的草稿则拒绝；若已有 draft 状态的草稿则更新而不新建。"""
    article = _get_article_or_404(article_id, db)

    # 检查该用户对该文章是否已有未完结的草稿（draft / rejected / pending）
    existing = (
        db.query(ArticleDraft)
        .filter(
            ArticleDraft.article_id == article_id,
            ArticleDraft.author_id == current_user.user_id,
            ArticleDraft.status.in_(["draft", "pending", "rejected"]),
        )
        .order_by(ArticleDraft.created_at.desc())
        .first()
    )

    if existing:
        if existing.status == "pending":
            raise HTTPException(status_code=409, detail="该文章已有草稿在审核中，审核完成前不可修改")
        # draft 或 rejected 状态：直接更新内容，重置为 draft 等待重新提交
        existing.content = body.content
        existing.category = body.category
        existing.status = "draft"
        db.commit()
        db.refresh(existing)
        return ApiResponse.ok(data=DraftOut.model_validate(existing), message="草稿已更新")

    # 没有已有草稿，新建
    draft = ArticleDraft(
        article_id=article_id,
        title=article.title,
        content=body.content,
        category=body.category,
        author=current_user.username,
        author_id=current_user.user_id,
        status="draft",
    )
    db.add(draft)
    db.commit()
    db.refresh(draft)
    return ApiResponse.ok(data=DraftOut.model_validate(draft), message="草稿已保存")


@router.post("/drafts", response_model=ApiResponse[DraftOut])
def create_new_draft(
    body: DraftCreateRequest,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    """新文章：先保存草稿（status=draft），再手动提交审核。
    若已有同标题的 pending 草稿则拒绝；若已有 draft/rejected 草稿则更新而不新建。"""
    if not body.title:
        raise HTTPException(status_code=422, detail="新文章草稿必须提供标题")

    # 查找同标题、同作者的未完结草稿
    existing = (
        db.query(ArticleDraft)
        .filter(
            ArticleDraft.title == body.title,
            ArticleDraft.author_id == current_user.user_id,
            ArticleDraft.article_id.is_(None),   # 新文章草稿无 article_id
            ArticleDraft.status.in_(["draft", "pending", "rejected"]),
        )
        .order_by(ArticleDraft.created_at.desc())
        .first()
    )

    if existing:
        if existing.status == "pending":
            raise HTTPException(status_code=409, detail="该标题已有草稿在审核中，审核完成前不可修改")
        existing.content = body.content
        existing.category = body.category
        existing.status = "draft"
        db.commit()
        db.refresh(existing)
        return ApiResponse.ok(data=DraftOut.model_validate(existing), message="草稿已更新")

    draft = ArticleDraft(
        article_id=body.article_id,
        title=body.title,
        content=body.content,
        category=body.category,
        author=current_user.username,
        author_id=current_user.user_id,
        status="draft",
    )
    db.add(draft)
    db.commit()
    db.refresh(draft)
    return ApiResponse.ok(data=DraftOut.model_validate(draft), message="草稿已保存")


@router.get("/drafts/my", response_model=ApiResponse[List[DraftOut]])
def my_drafts(
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    drafts = (
        db.query(ArticleDraft)
        .filter(ArticleDraft.author_id == current_user.user_id)
        .order_by(ArticleDraft.created_at.desc())
        .all()
    )
    return ApiResponse.ok(data=[DraftOut.model_validate(d) for d in drafts])


@router.get("/drafts/pending", response_model=ApiResponse[List[DraftOut]])
def pending_drafts(
    _: User = Depends(require_admin),
    db: Session = Depends(get_db),
):
    """获取审核队列中的草稿（已提交、等待审核）"""
    # 只返回 article_reviews 表中存在对应记录的草稿（status=pending）
    drafts = (
        db.query(ArticleDraft)
        .join(ArticleReview, ArticleReview.draft_id == ArticleDraft.draft_id)
        .filter(ArticleDraft.status == "pending")
        .order_by(ArticleReview.submitted_at.asc())
        .all()
    )
    return ApiResponse.ok(data=[DraftOut.model_validate(d) for d in drafts])


@router.post("/drafts/{draft_id}/submit", response_model=ApiResponse[DraftOut])
def submit_draft_for_review(
    draft_id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    """将草稿提交到审核队列（draft → pending）"""
    draft = db.query(ArticleDraft).filter(ArticleDraft.draft_id == draft_id).first()
    if not draft:
        raise HTTPException(status_code=404, detail="草稿不存在")
    if draft.author_id != current_user.user_id and current_user.role_id != 1:
        raise HTTPException(status_code=403, detail="无权提交此草稿")
    if draft.status == "pending":
        raise HTTPException(status_code=400, detail="草稿已在审核队列中")
    if draft.status == "approved":
        raise HTTPException(status_code=400, detail="草稿已通过审核，无需重复提交")

    # 写入审核队列
    existing_review = db.query(ArticleReview).filter(ArticleReview.draft_id == draft_id).first()
    if not existing_review:
        review_entry = ArticleReview(
            draft_id=draft_id,
            submitter_id=current_user.user_id,
            submitted_at=datetime.now(),
        )
        db.add(review_entry)

    # 更新草稿状态和提交时间
    draft.status = "pending"
    draft.submitted_at = datetime.now()
    draft.reject_reason = None   # 重新提交时清除上次拒绝原因
    db.commit()
    db.refresh(draft)
    return ApiResponse.ok(data=DraftOut.model_validate(draft), message="草稿已提交审核，等待管理员审核")


@router.post("/drafts/{draft_id}/review", response_model=ApiResponse[DraftOut])
def review_draft(
    draft_id: int,
    body: ReviewRequest,
    reviewer: User = Depends(require_admin),
    db: Session = Depends(get_db),
):
    """
    审核草稿：
      1. 校验草稿处于 pending 状态且在审核队列中
      2. 从 article_reviews 表删除该队列记录
      3. 更新 article_drafts 的状态、审核人、审核时间
      4. 若通过：写入/更新 articles 表
    """
    draft = db.query(ArticleDraft).filter(ArticleDraft.draft_id == draft_id).first()
    if not draft:
        raise HTTPException(status_code=404, detail="草稿不存在")
    if draft.status != "pending":
        raise HTTPException(status_code=400, detail=f"草稿当前状态为 '{draft.status}'，未在审核队列中")

    # 确认审核队列记录存在
    review_entry = db.query(ArticleReview).filter(ArticleReview.draft_id == draft_id).first()
    if not review_entry:
        raise HTTPException(status_code=400, detail="该草稿未在审核队列中，请重新提交")

    # ── 第1步：从审核队列删除该记录 ──────────────────────────────
    db.delete(review_entry)
    db.flush()

    # ── 第2步：更新草稿状态、记录审核人和时间（可追溯） ────────────
    draft.reviewer_id = reviewer.user_id
    draft.reviewed_at = datetime.now()

    if body.action == "approve":
        draft.status = "approved"

        # ── 第3步（通过）：写入/更新 articles 表 ──────────────────
        if draft.article_id:
            # 编辑现有文章（标题不可修改）
            article = db.query(Article).filter(Article.article_id == draft.article_id).first()
            if article:
                article.content = draft.content
                article.category = draft.category
                article.status = "published"
                db.flush()
                _save_version(article, reviewer, db, f"审核通过（草稿 #{draft_id}）")
        else:
            # 新文章：创建并回写 article_id 到草稿
            new_article = Article(
                title=draft.title,
                content=draft.content,
                category=draft.category,
                author=draft.author,
                author_id=draft.author_id,
                publish_date=datetime.now(),
                status="published",
            )
            db.add(new_article)
            db.flush()
            draft.article_id = new_article.article_id
            _save_version(new_article, reviewer, db, "初始版本（审核通过）")

    elif body.action == "reject":
        draft.status = "rejected"
        draft.reject_reason = body.comment
        # 若关联文章仍处于 pending 状态，将其更新为 draft
        if draft.article_id:
            article = db.query(Article).filter(Article.article_id == draft.article_id).first()
            if article and article.status == "pending":
                article.status = "draft"
    else:
        raise HTTPException(status_code=400, detail="无效的审核操作，action 必须为 approve 或 reject")

    db.commit()
    db.refresh(draft)

    msg = "审核通过，文章已发布" if body.action == "approve" else "已拒绝，原因已记录"
    return ApiResponse.ok(data=DraftOut.model_validate(draft), message=msg)


@router.get("/drafts/all", response_model=ApiResponse[List[DraftOut]])
def all_drafts(
    status: Optional[str] = None,
    _: User = Depends(require_admin),
    db: Session = Depends(get_db),
):
    """获取所有草稿（管理员），可按状态过滤"""
    q = db.query(ArticleDraft)
    if status:
        q = q.filter(ArticleDraft.status == status)
    drafts = q.order_by(ArticleDraft.created_at.desc()).all()
    return ApiResponse.ok(data=[DraftOut.model_validate(d) for d in drafts])


@router.get("/drafts/{draft_id}", response_model=ApiResponse[DraftOut])
def get_draft(
    draft_id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    """获取草稿详情（本人或管理员可访问）"""
    draft = db.query(ArticleDraft).filter(ArticleDraft.draft_id == draft_id).first()
    if not draft:
        raise HTTPException(status_code=404, detail="草稿不存在")
    if draft.author_id != current_user.user_id and current_user.role_id != 1:
        raise HTTPException(status_code=403, detail="无权访问此草稿")
    return ApiResponse.ok(data=DraftOut.model_validate(draft))


@router.delete("/drafts/{draft_id}", response_model=ApiResponse[str])
def delete_draft(
    draft_id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    """删除草稿（本人或管理员）——已审核（approved/rejected）的草稿仅管理员可删除"""
    draft = db.query(ArticleDraft).filter(ArticleDraft.draft_id == draft_id).first()
    if not draft:
        raise HTTPException(status_code=404, detail="草稿不存在")
    if draft.author_id != current_user.user_id and current_user.role_id != 1:
        raise HTTPException(status_code=403, detail="无权删除此草稿")
    # 已审核的草稿属于审核记录，仅管理员可删除
    if draft.status in ("approved", "rejected") and current_user.role_id != 1:
        raise HTTPException(status_code=403, detail="已审核的草稿不可删除，如需申诉请联系管理员")
    # 先删除关联审核日志，再删除草稿，避免 FK 约束报错
    db.query(ArticleReview).filter(ArticleReview.draft_id == draft_id).delete(synchronize_session=False)
    db.delete(draft)
    db.commit()
    return ApiResponse.ok(data="删除成功", message="草稿已删除")


@router.put("/drafts/{draft_id}", response_model=ApiResponse[DraftOut])
def update_draft(
    draft_id: int,
    body: DraftUpdateRequest,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    """更新草稿内容（仅限 draft / rejected 状态）。更新后需再调用 /submit 提交审核。"""
    draft = db.query(ArticleDraft).filter(ArticleDraft.draft_id == draft_id).first()
    if not draft:
        raise HTTPException(status_code=404, detail="草稿不存在")
    if draft.author_id != current_user.user_id and current_user.role_id != 1:
        raise HTTPException(status_code=403, detail="无权修改此草稿")
    if draft.status == "pending":
        raise HTTPException(status_code=400, detail="草稿正在审核中，审核结束前不允许修改")
    if draft.status == "approved":
        raise HTTPException(status_code=400, detail="草稿已通过审核，无法再次修改")

    if body.content is not None:
        draft.content = body.content
    if body.category is not None:
        draft.category = body.category
    # 修改后状态回到 draft，需用户再次调用 /submit 提交审核
    draft.status = "draft"
    db.commit()
    db.refresh(draft)
    return ApiResponse.ok(data=DraftOut.model_validate(draft), message="草稿已更新，请提交审核")


# ─── 内部工具函数 ──────────────────────────────────────────────────────────────
def _check_article_permission(article: Article, user: User):
    if user.role_id == 1:
        return  # 管理员可操作所有
    if article.author_id != user.user_id:
        raise HTTPException(status_code=403, detail="无权操作此文章")


def _save_version(article: Article, editor: User, db: Session, summary: str = ""):
    last = (
        db.query(ArticleVersion)
        .filter(ArticleVersion.article_id == article.article_id)
        .order_by(ArticleVersion.version_number.desc())
        .first()
    )
    next_num = (last.version_number + 1) if last else 1
    version = ArticleVersion(
        article_id=article.article_id,
        version_number=next_num,
        title=article.title,
        content=article.content,
        category=article.category,
        editor_id=editor.user_id,
        change_summary=summary,
    )
    db.add(version)
    db.flush()
