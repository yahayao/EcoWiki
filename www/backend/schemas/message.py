"""
消息 Pydantic 模型
"""
from pydantic import BaseModel
from typing import Optional
from datetime import datetime
from models.message import MessageStatus, MessageType


class MessageOut(BaseModel):
    message_id: int
    recipient_user_id: int
    sender_user_id: int
    content: str
    subject: Optional[str] = None
    send_time: Optional[datetime] = None
    status: MessageStatus = MessageStatus.UNREAD
    message_type: MessageType = MessageType.USER_PRIVATE
    priority: Optional[int] = None
    read_time: Optional[datetime] = None
    created_at: Optional[datetime] = None

    model_config = {"from_attributes": True}


class MessageCreateRequest(BaseModel):
    recipient_user_id: int
    content: str
    subject: Optional[str] = None
    message_type: MessageType = MessageType.USER_PRIVATE
    priority: Optional[int] = None
