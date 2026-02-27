"""
消息 Pydantic 序列化 schema
"""
from datetime import datetime
from typing import Optional

from pydantic import BaseModel


class MessageOut(BaseModel):
    message_id:         int
    sender_user_id:     Optional[int]
    recipient_user_id:  int
    subject:            Optional[str]
    content:            str
    message_type:       str
    priority:           int
    status:             str
    send_time:          datetime
    read_time:          Optional[datetime]
    # 从 ORM @property 自动读取（from_attributes=True 支持）
    sender_username:    Optional[str] = None
    recipient_username: Optional[str] = None

    model_config = {"from_attributes": True}


class MessageCreateRequest(BaseModel):
    recipient_user_id: int
    subject:           Optional[str] = None
    content:           str
    message_type:      str = "USER"
    priority:          int = 1
