"""
评论相关数据模型
"""
from datetime import datetime
from sqlalchemy import Column, BigInteger, Integer, String, Text, DateTime, ForeignKey, Boolean
from sqlalchemy.orm import relationship
from database import Base


class Comment(Base):
    """评论实体，映射 `comments` 表"""
    __tablename__ = "comments"

    comment_id  = Column(BigInteger, primary_key=True, autoincrement=True)
    article_id  = Column(BigInteger, ForeignKey("articles.article_id"), nullable=False)
    author      = Column(String(50), nullable=False)
    author_id   = Column(BigInteger, ForeignKey("user.user_id"))
    content     = Column(Text, nullable=False)
    parent_id   = Column(BigInteger, ForeignKey("comments.comment_id"))
    likes       = Column(Integer, default=0)
    dislikes    = Column(Integer, default=0)
    ip_address  = Column(String(50))
    user_agent  = Column(String(500))
    is_deleted  = Column(Boolean, default=False)
    created_at  = Column(DateTime, default=datetime.now)
    updated_at  = Column(DateTime, default=datetime.now, onupdate=datetime.now)

    article     = relationship("Article",  foreign_keys=[article_id])
    author_user = relationship("User",     foreign_keys=[author_id])
    replies     = relationship("Comment",  foreign_keys=[parent_id], back_populates="parent")
    parent      = relationship("Comment",  foreign_keys=[parent_id], back_populates="replies",
                               remote_side=[comment_id])


class CommentLike(Base):
    """评论点赞，映射 `comment_likes` 表"""
    __tablename__ = "comment_likes"

    user_id    = Column(BigInteger, ForeignKey("user.user_id"),         primary_key=True)
    comment_id = Column(BigInteger, ForeignKey("comments.comment_id"),  primary_key=True)
    is_like    = Column(Boolean, default=True)   # True=点赞，False=点踩
    created_at = Column(DateTime, default=datetime.now)
