"""
用户路由：个人信息、收藏列表、点赞列表
"""
from typing import List

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
