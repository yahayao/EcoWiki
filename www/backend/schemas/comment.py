"""
评论相关 Pydantic 模型
"""
from pydantic import BaseModel
from typing import Optional, List
from datetime import datetime


class CommentOut(BaseModel):
    comment_id: int
    article_id: int
    author: str
    author_id: Optional[int] = None
    content: str
    parent_id: Optional[int] = None
    likes: int = 0
    dislikes: int = 0
    is_deleted: bool = False
    created_at: Optional[datetime] = None
    replies: List["CommentOut"] = []
    liked_by_current: bool = False     # 当前用户是否已点赞

    model_config = {"from_attributes": True}


CommentOut.model_rebuild()


class CommentCreateRequest(BaseModel):
    article_id: int
    content: str
    parent_id: Optional[int] = None


class ReplyCreateRequest(BaseModel):
    content: str
