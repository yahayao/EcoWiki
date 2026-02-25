"""
用户路由：个人信息、收藏列表、点赞列表
"""
from typing import List, Optional

from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session

from database import get_db
from models.user import User
from models.article import Article, UserArticleLike, UserArticleFavorite
from schemas.user import UserOut
from schemas.article import ArticleListOut
from schemas.common import ApiResponse, PageResult
from core.security import get_current_user

router = APIRouter(prefix="/users", tags=["用户"])


@router.get("/me", response_model=ApiResponse[UserOut])
def me(current_user: User = Depends(get_current_user)):
    return ApiResponse.ok(data=UserOut.model_validate(current_user))


@router.get("/{user_id}", response_model=ApiResponse[UserOut])
def get_user(user_id: int, db: Session = Depends(get_db)):
    user = db.query(User).filter(User.user_id == user_id).first()
    if not user:
        raise HTTPException(status_code=404, detail="用户不存在")
    return ApiResponse.ok(data=UserOut.model_validate(user))


@router.get("/me/favorites", response_model=ApiResponse[PageResult[ArticleListOut]])
def my_favorites(
    page: int = Query(0, ge=0),
    size: int = Query(10, ge=1, le=100),
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    q = (
        db.query(Article)
        .join(UserArticleFavorite, Article.article_id == UserArticleFavorite.article_id)
        .filter(UserArticleFavorite.user_id == current_user.user_id)
        .order_by(UserArticleFavorite.created_at.desc())
    )
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


@router.get("/me/likes", response_model=ApiResponse[PageResult[ArticleListOut]])
def my_likes(
    page: int = Query(0, ge=0),
    size: int = Query(10, ge=1, le=100),
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    q = (
        db.query(Article)
        .join(UserArticleLike, Article.article_id == UserArticleLike.article_id)
        .filter(UserArticleLike.user_id == current_user.user_id)
        .order_by(UserArticleLike.created_at.desc())
    )
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


# ─── 当前用户的文章列表 ───────────────────────────────────────────────────────
@router.get("/me/articles", response_model=ApiResponse[PageResult[ArticleListOut]])
def my_articles(
    page: int = Query(0, ge=0),
    size: int = Query(10, ge=1, le=100),
    status: Optional[str] = None,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    q = db.query(Article).filter(Article.author_id == current_user.user_id)
    if status:
        q = q.filter(Article.status == status)
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


# ─── 当前用户的文章统计 ───────────────────────────────────────────────────────
@router.get("/me/article-stats")
def my_article_stats(
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    from sqlalchemy import func
    total = db.query(func.count(Article.article_id)).filter(Article.author_id == current_user.user_id).scalar() or 0
    published = db.query(func.count(Article.article_id)).filter(
        Article.author_id == current_user.user_id, Article.status == "published"
    ).scalar() or 0
    draft = db.query(func.count(Article.article_id)).filter(
        Article.author_id == current_user.user_id, Article.status == "draft"
    ).scalar() or 0
    favorites = db.query(func.count(UserArticleFavorite.article_id)).filter(
        UserArticleFavorite.user_id == current_user.user_id
    ).scalar() or 0
    total_views = db.query(func.sum(Article.views)).filter(Article.author_id == current_user.user_id).scalar() or 0
    total_likes = db.query(func.sum(Article.likes)).filter(Article.author_id == current_user.user_id).scalar() or 0
    return ApiResponse.ok(data={
        "total_articles":     total,
        "published_articles": published,
        "draft_articles":     draft,
        "favorite_articles":  favorites,
        "total_views":        total_views,
        "total_likes":        total_likes,
    })


# ─── 当前用户的贡献统计 ───────────────────────────────────────────────────────
@router.get("/me/contributions")
def my_contributions(
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    from sqlalchemy import func
    from models.article import ArticleVersion
    from datetime import datetime, date
    created = db.query(func.count(Article.article_id)).filter(Article.author_id == current_user.user_id).scalar() or 0
    edit_count = db.query(func.count(ArticleVersion.version_id)).filter(ArticleVersion.editor_id == current_user.user_id).scalar() or 0
    return ApiResponse.ok(data={
        "created_pages":       created,
        "edit_count":          edit_count,
        "points":              created * 10 + edit_count * 5,
        "created_this_month":  0,
        "edits_this_month":    0,
        "points_this_month":   0,
        "achievements":        [],
        "contribution_calendar": [],
    })
