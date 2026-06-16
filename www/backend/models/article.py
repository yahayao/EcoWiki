"""
文章相关数据模型
"""
from datetime import datetime
from sqlalchemy import (
    Column, Integer, BigInteger, String, Text, DateTime,
    ForeignKey, Table
)
from sqlalchemy.orm import relationship
from database import Base


# 文章-标签 多对多关联表
article_tags_table = Table(
    "Article_Tags",
    Base.metadata,
    Column("article_id", BigInteger, ForeignKey("articles.article_id"), primary_key=True),
    Column("tag_id",     Integer,    ForeignKey("tags.tag_id"),         primary_key=True),
)


class Article(Base):
    """文章实体，映射 `articles` 表"""
    __tablename__ = "articles"

    article_id   = Column(BigInteger, primary_key=True, autoincrement=True)
    title        = Column(String(255), nullable=False)
    author       = Column(String(50),  nullable=False)
    content      = Column(Text)
    publish_date = Column("publish_date", DateTime, nullable=False, default=datetime.now)
    category     = Column(String(50))
    views        = Column(Integer, default=0)
    likes        = Column(Integer, default=0)
    comments     = Column(Integer, default=0)
    update_time  = Column("update_time", DateTime, default=datetime.now, onupdate=datetime.now)

    # 状态字段：published / draft / pending / rejected
    status       = Column(String(20), default="published")
    # 关联作者 user_id（可选，用于用户文章列表）
    author_id    = Column("author_id", BigInteger, ForeignKey("user.user_id"))

    tags = relationship("Tag", secondary=article_tags_table, lazy="joined")
    author_user = relationship("User", foreign_keys=[author_id])


class ArticleDraft(Base):
    """文章草稿，映射 `article_drafts` 表"""
    __tablename__ = "article_drafts"

    draft_id      = Column(BigInteger, primary_key=True, autoincrement=True)
    article_id    = Column(BigInteger, ForeignKey("articles.article_id"), nullable=True)
    title         = Column(String(255), nullable=False)
    content       = Column(Text)
    category      = Column(String(50))
    author        = Column(String(50))
    author_id     = Column(BigInteger, ForeignKey("user.user_id"))
    status        = Column(String(20), default="draft")   # draft/pending/approved/rejected
    reject_reason = Column(Text)
    created_at    = Column(DateTime, default=datetime.now)
    updated_at    = Column(DateTime, default=datetime.now, onupdate=datetime.now)
    submitted_at  = Column(DateTime)
    # 审核追溯字段
    reviewer_id   = Column(BigInteger, ForeignKey("user.user_id"), nullable=True)
    reviewed_at   = Column(DateTime, nullable=True)

    article       = relationship("Article", foreign_keys=[article_id])
    author_user   = relationship("User",    foreign_keys=[author_id])
    reviewer_user = relationship("User",    foreign_keys=[reviewer_id])


class ArticleReview(Base):
    """
    审核队列表（article_reviews）。
    - 草稿提交审核时插入一条记录（submitter_id / submitted_at）
    - 管理员审核完成（通过/拒绝）后删除该记录
    - 审核结果保存在 article_drafts 表的 reviewer_id / reviewed_at / status / reject_reason 字段
    """
    __tablename__ = "article_reviews"

    review_id    = Column(BigInteger, primary_key=True, autoincrement=True)
    # 一条草稿最多同时有一条待审记录
    draft_id     = Column(BigInteger, ForeignKey("article_drafts.draft_id"), unique=True, nullable=False)
    submitter_id = Column(BigInteger, ForeignKey("user.user_id"), nullable=False)
    submitted_at = Column(DateTime, default=datetime.now, nullable=False)

    draft        = relationship("ArticleDraft", foreign_keys=[draft_id])
    submitter    = relationship("User",         foreign_keys=[submitter_id])


class ArticleVersion(Base):
    """文章历史版本，映射 `article_versions` 表"""
    __tablename__ = "article_versions"

    version_id   = Column(BigInteger, primary_key=True, autoincrement=True)
    article_id   = Column(BigInteger, ForeignKey("articles.article_id"))
    version_number = Column(Integer, nullable=False)
    title        = Column(String(255))
    content      = Column(Text)
    category     = Column(String(50))
    editor_id    = Column(BigInteger, ForeignKey("user.user_id"))
    created_at   = Column(DateTime, default=datetime.now)
    change_summary = Column(String(500))

    article  = relationship("Article", foreign_keys=[article_id])
    editor   = relationship("User",    foreign_keys=[editor_id])


class UserArticleLike(Base):
    """用户文章点赞，映射 `user_article_likes` 表"""
    __tablename__ = "user_article_likes"

    user_id    = Column(BigInteger, ForeignKey("user.user_id"),      primary_key=True)
    article_id = Column(BigInteger, ForeignKey("articles.article_id"), primary_key=True)
    created_at = Column(DateTime, default=datetime.now)


class UserArticleFavorite(Base):
    """用户文章收藏，映射 `user_article_favorites` 表"""
    __tablename__ = "user_article_favorites"

    user_id    = Column(BigInteger, ForeignKey("user.user_id"),       primary_key=True)
    article_id = Column(BigInteger, ForeignKey("articles.article_id"), primary_key=True)
    created_at = Column(DateTime, default=datetime.now)
