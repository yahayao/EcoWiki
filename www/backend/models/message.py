"""
消息数据模型
"""
from datetime import datetime
from enum import Enum

from sqlalchemy import (
    BigInteger, Column, DateTime, ForeignKey,
    Integer, String, Text,
)
from sqlalchemy.orm import relationship

from database import Base


class MessageStatus(str, Enum):
    UNREAD = "UNREAD"
    READ = "READ"
    DELETED = "DELETED"


class Message(Base):
    """消息实体，映射 `messages` 表"""
    __tablename__ = "messages"

    message_id        = Column(BigInteger, primary_key=True, autoincrement=True)
    sender_user_id    = Column(BigInteger, ForeignKey("user.user_id"), nullable=True)   # None = 系统消息
    recipient_user_id = Column(BigInteger, ForeignKey("user.user_id"), nullable=False)
    subject           = Column(String(200), nullable=True)
    content           = Column(Text, nullable=False)
    message_type      = Column(String(50), nullable=False, default="USER")   # USER / SYSTEM / REVIEW
    priority          = Column(Integer, nullable=False, default=1)
    status            = Column(String(20), nullable=False, default=MessageStatus.UNREAD)
    send_time         = Column(DateTime, nullable=False, default=datetime.now)
    read_time         = Column(DateTime, nullable=True)

    sender    = relationship("User", foreign_keys=[sender_user_id])
    recipient = relationship("User", foreign_keys=[recipient_user_id])

    # ── 快捷属性，供 schema 序列化时使用 ─────────────────────────────────────
    @property
    def sender_username(self) -> str | None:
        return self.sender.username if self.sender else None

    @property
    def recipient_username(self) -> str | None:
        return self.recipient.username if self.recipient else None
