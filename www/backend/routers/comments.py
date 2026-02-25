"""
评论路由：获取、创建、删除、点赞
"""
from datetime import datetime
from typing import Optional, List

from fastapi import APIRouter, Depends, HTTPException, Query, Request
from sqlalchemy.orm import Session

from database import get_db
from models.comment import Comment, CommentLike
from models.article import Article
from models.user import User
from schemas.comment import CommentOut, CommentCreateRequest, ReplyCreateRequest
from schemas.common import ApiResponse, PageResult
from core.security import get_current_user, get_current_user_optional

router = APIRouter(prefix="/comments", tags=["评论"])


def _build_comment_out(comment: Comment, current_user: Optional[User], db: Session) -> dict:
    liked = False
    if current_user:
        liked = db.query(CommentLike).filter_by(
            user_id=current_user.user_id, comment_id=comment.comment_id, is_like=True
        ).first() is not None

    replies_data = []
    if not comment.parent_id:   # 只在顶层评论中填充回复
        replies = (
            db.query(Comment)
            .filter(Comment.parent_id == comment.comment_id, Comment.is_deleted == False)
            .order_by(Comment.created_at.asc())
            .all()
        )
        replies_data = [_build_comment_out(r, current_user, db) for r in replies]

    return {
        "comment_id":         comment.comment_id,
        "article_id":         comment.article_id,
        "author":             comment.author,
        "author_id":          comment.author_id,
        "content":            comment.content,
        "parent_id":          comment.parent_id,
        "likes":              comment.likes,
        "dislikes":           comment.dislikes,
        "is_deleted":         comment.is_deleted,
        "created_at":         comment.created_at,
        "replies":            replies_data,
        "liked_by_current":   liked,
    }


@router.get("/article/{article_id}", response_model=ApiResponse[PageResult[CommentOut]])
def get_comments(
    article_id: int,
    page: int = Query(0, ge=0),
    size: int = Query(20, ge=1, le=100),
    sort: str = Query("newest"),
    current_user: Optional[User] = Depends(get_current_user_optional),
    db: Session = Depends(get_db),
):
    q = db.query(Comment).filter(
        Comment.article_id == article_id,
        Comment.parent_id == None,
        Comment.is_deleted == False,
    )
    if sort == "oldest":
        q = q.order_by(Comment.created_at.asc())
    elif sort == "hot":
        q = q.order_by(Comment.likes.desc())
    else:
        q = q.order_by(Comment.created_at.desc())

    total = q.count()
    comments = q.offset(page * size).limit(size).all()
    content = [CommentOut.model_validate(_build_comment_out(c, current_user, db)) for c in comments]

    return ApiResponse.ok(data=PageResult(
        content=content,
        total_elements=total,
        total_pages=(total + size - 1) // size if size else 0,
        page=page,
        size=size,
        number_of_elements=len(content),
    ))


@router.post("", response_model=ApiResponse[CommentOut])
def create_comment(
    body: CommentCreateRequest,
    request: Request,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    article = db.query(Article).filter(Article.article_id == body.article_id).first()
    if not article:
        raise HTTPException(status_code=404, detail="文章不存在")

    comment = Comment(
        article_id=body.article_id,
        author=current_user.username,
        author_id=current_user.user_id,
        content=body.content,
        parent_id=body.parent_id,
        ip_address=request.client.host if request.client else None,
        user_agent=request.headers.get("User-Agent"),
    )
    db.add(comment)

    # 更新文章评论数
    article.comments = (article.comments or 0) + 1
    db.commit()
    db.refresh(comment)

    return ApiResponse.ok(
        data=CommentOut.model_validate(_build_comment_out(comment, current_user, db)),
        message="评论发表成功",
    )


@router.post("/{comment_id}/reply", response_model=ApiResponse[CommentOut])
def reply_comment(
    comment_id: int,
    body: ReplyCreateRequest,
    request: Request,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    parent = db.query(Comment).filter(Comment.comment_id == comment_id).first()
    if not parent:
        raise HTTPException(status_code=404, detail="评论不存在")

    reply = Comment(
        article_id=parent.article_id,
        author=current_user.username,
        author_id=current_user.user_id,
        content=body.content,
        parent_id=comment_id,
        ip_address=request.client.host if request.client else None,
        user_agent=request.headers.get("User-Agent"),
    )
    db.add(reply)
    db.commit()
    db.refresh(reply)

    return ApiResponse.ok(
        data=CommentOut.model_validate(_build_comment_out(reply, current_user, db)),
        message="回复成功",
    )


@router.delete("/{comment_id}")
def delete_comment(
    comment_id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    comment = db.query(Comment).filter(Comment.comment_id == comment_id).first()
    if not comment:
        raise HTTPException(status_code=404, detail="评论不存在")
    if comment.author_id != current_user.user_id and current_user.role_id != 1:
        raise HTTPException(status_code=403, detail="无权删除此评论")

    comment.is_deleted = True
    comment.content = "[该评论已被删除]"

    # 更新文章评论数
    article = db.query(Article).filter(Article.article_id == comment.article_id).first()
    if article:
        article.comments = max(0, (article.comments or 0) - 1)

    db.commit()
    return ApiResponse.ok(message="评论删除成功")


@router.post("/{comment_id}/like")
def like_comment(
    comment_id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    comment = db.query(Comment).filter(Comment.comment_id == comment_id).first()
    if not comment:
        raise HTTPException(status_code=404, detail="评论不存在")

    existing = db.query(CommentLike).filter_by(
        user_id=current_user.user_id, comment_id=comment_id, is_like=True
    ).first()

    if existing:
        db.delete(existing)
        comment.likes = max(0, (comment.likes or 0) - 1)
        liked = False
    else:
        db.add(CommentLike(user_id=current_user.user_id, comment_id=comment_id, is_like=True))
        comment.likes = (comment.likes or 0) + 1
        liked = True

    db.commit()
    return ApiResponse.ok(data={"liked": liked, "likes": comment.likes})


@router.get("/article/{article_id}/stats")
def comment_stats(article_id: int, db: Session = Depends(get_db)):
    from sqlalchemy import func
    total = db.query(func.count(Comment.comment_id)).filter(
        Comment.article_id == article_id, Comment.is_deleted == False
    ).scalar()
    return ApiResponse.ok(data={"total": total})
