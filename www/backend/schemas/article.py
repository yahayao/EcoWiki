"""
文章相关 Pydantic 模型
"""
from pydantic import BaseModel
from typing import Optional, List
from datetime import datetime


class TagOut(BaseModel):
    tag_id: int
    tag_name: str
    color: Optional[str] = None

    model_config = {"from_attributes": True}


class ArticleOut(BaseModel):
    article_id: int
    title: str
    author: str
    content: Optional[str] = None
    publish_date: Optional[datetime] = None
    category: Optional[str] = None
    views: int = 0
    likes: int = 0
    comments: int = 0
    update_time: Optional[datetime] = None
    status: Optional[str] = None
    author_id: Optional[int] = None
    tags: List[TagOut] = []

    model_config = {"from_attributes": True}


class ArticleListOut(BaseModel):
    """列表页使用——不返回 content 正文"""
    article_id: int
    title: str
    author: str
    publish_date: Optional[datetime] = None
    category: Optional[str] = None
    views: int = 0
    likes: int = 0
    comments: int = 0
    update_time: Optional[datetime] = None
    status: Optional[str] = None
    tags: List[TagOut] = []

    model_config = {"from_attributes": True}


class ArticleCreateRequest(BaseModel):
    title: str
    content: Optional[str] = None
    category: Optional[str] = None
    tag_ids: Optional[List[int]] = []
    status: Optional[str] = "published"


class ArticleUpdateRequest(BaseModel):
    title: Optional[str] = None
    content: Optional[str] = None
    category: Optional[str] = None
    tag_ids: Optional[List[int]] = None
    status: Optional[str] = None


# ── 草稿 ──
class DraftOut(BaseModel):
    draft_id: int
    article_id: Optional[int] = None
    title: str
    content: Optional[str] = None
    category: Optional[str] = None
    author: Optional[str] = None
    author_id: Optional[int] = None
    status: str
    reject_reason: Optional[str] = None
    created_at: Optional[datetime] = None
    updated_at: Optional[datetime] = None
    submitted_at: Optional[datetime] = None
    # 审核追溯字段
    reviewer_id: Optional[int] = None
    reviewed_at: Optional[datetime] = None

    model_config = {"from_attributes": True}


class DraftCreateRequest(BaseModel):
    title: Optional[str] = None
    content: Optional[str] = None
    category: Optional[str] = None
    article_id: Optional[int] = None


class DraftUpdateRequest(BaseModel):
    """更新草稿内容并重新提交审核（仅限 rejected/draft 状态）"""
    content: Optional[str] = None
    category: Optional[str] = None


class ReviewRequest(BaseModel):
    action: str     # approve / reject
    comment: Optional[str] = None


# ── 版本 ──
class VersionOut(BaseModel):
    version_id: int
    article_id: int
    version_number: int
    title: Optional[str] = None
    content: Optional[str] = None
    category: Optional[str] = None
    editor_id: Optional[int] = None
    change_summary: Optional[str] = None
    created_at: Optional[datetime] = None

    model_config = {"from_attributes": True}
