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
    DraftOut, DraftCreateRequest, ReviewRequest, VersionOut,
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
    sort: Optional[str] = Query("newest", description="newest/oldest/views/likes"),
    db: Session = Depends(get_db),
):
    q = db.query(Article).filter(Article.status == "published")
    if category:
        q = q.filter(Article.category == category)

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
    article = Article(
        title=body.title,
        content=body.content,
        category=body.category,
        author=current_user.username,
        author_id=current_user.user_id,
        publish_date=datetime.now(),
        status=body.status or "published",
    )
    # 处理标签
    if body.tag_ids:
        tags = db.query(Tag).filter(Tag.tag_id.in_(body.tag_ids)).all()
        article.tags = tags

    db.add(article)
    db.commit()
    db.refresh(article)

    # 创建第一个版本记录
    _save_version(article, current_user, db, "初始版本")

    return ApiResponse.ok(data=ArticleOut.model_validate(article), message="文章创建成功")


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


# ─── 文章草稿 ─────────────────────────────────────────────────────────────────
@router.post("/{article_id}/drafts", response_model=ApiResponse[DraftOut])
def create_draft(
    article_id: int,
    body: DraftCreateRequest,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    _get_article_or_404(article_id, db)
    draft = ArticleDraft(
        article_id=article_id,
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
    return ApiResponse.ok(data=DraftOut.model_validate(draft), message="草稿创建成功")


@router.post("/drafts", response_model=ApiResponse[DraftOut])
def create_new_draft(
    body: DraftCreateRequest,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    draft = ArticleDraft(
        article_id=body.article_id,
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
    db.refresh(draft)
    return ApiResponse.ok(data=DraftOut.model_validate(draft), message="草稿提交成功，等待审核")


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
    drafts = (
        db.query(ArticleDraft)
        .filter(ArticleDraft.status == "pending")
        .order_by(ArticleDraft.submitted_at.asc())
        .all()
    )
    return ApiResponse.ok(data=[DraftOut.model_validate(d) for d in drafts])


@router.post("/drafts/{draft_id}/review", response_model=ApiResponse[DraftOut])
def review_draft(
    draft_id: int,
    body: ReviewRequest,
    reviewer: User = Depends(require_admin),
    db: Session = Depends(get_db),
):
    draft = db.query(ArticleDraft).filter(ArticleDraft.draft_id == draft_id).first()
    if not draft:
        raise HTTPException(status_code=404, detail="草稿不存在")

    if body.action == "approve":
        draft.status = "approved"
        # 写入正式文章
        if draft.article_id:
            article = db.query(Article).filter(Article.article_id == draft.article_id).first()
            if article:
                article.title = draft.title
                article.content = draft.content
                article.category = draft.category
                article.status = "published"
        else:
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
    elif body.action == "reject":
        draft.status = "rejected"
        draft.reject_reason = body.comment
    else:
        raise HTTPException(status_code=400, detail="无效的审核操作")

    review = ArticleReview(
        draft_id=draft_id,
        reviewer_id=reviewer.user_id,
        action=body.action,
        comment=body.comment,
    )
    db.add(review)
    db.commit()
    db.refresh(draft)
    return ApiResponse.ok(data=DraftOut.model_validate(draft), message="审核完成")


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
