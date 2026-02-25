"""
消息数据模型
"""
from datetime import datetime
from sqlalchemy import Column, Integer, String, Text, DateTime, Enum as SAEnum
from database import Base
import enum


class MessageStatus(str, enum.Enum):
    UNREAD  = "UNREAD"
    READ    = "READ"
    DELETED = "DELETED"
    ARCHIVED = "ARCHIVED"


class MessageType(str, enum.Enum):
    USER_PRIVATE   = "USER_PRIVATE"
    SYSTEM_NOTICE  = "SYSTEM_NOTICE"
    ARTICLE_NOTICE = "ARTICLE_NOTICE"
    COMMENT_NOTICE = "COMMENT_NOTICE"
    WARNING        = "WARNING"


class Message(Base):
    """消息实体，映射 `messages` 表"""
    __tablename__ = "messages"

    message_id         = Column(Integer, primary_key=True, autoincrement=True)
    recipient_user_id  = Column(Integer, nullable=False)
    sender_user_id     = Column(Integer, nullable=False)
    content            = Column(Text, nullable=False)
    subject            = Column(String(255))
    send_time          = Column(DateTime, default=datetime.now)
    status             = Column(SAEnum(MessageStatus), default=MessageStatus.UNREAD)
    message_type       = Column(SAEnum(MessageType),   default=MessageType.USER_PRIVATE)
    priority           = Column(Integer)
    expire_time        = Column(DateTime)
    read_time          = Column(DateTime)
    metadata_          = Column("metadata", Text)   # JSON 字符串
    created_at         = Column(DateTime, default=datetime.now)
    updated_at         = Column(DateTime, default=datetime.now, onupdate=datetime.now)
