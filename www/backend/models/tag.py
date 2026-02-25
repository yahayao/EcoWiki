"""
标签数据模型
"""
from sqlalchemy import Column, Integer, String, DateTime
from datetime import datetime
from database import Base


class Tag(Base):
    """标签实体，映射 `tags` 表"""
    __tablename__ = "tags"

    tag_id     = Column(Integer, primary_key=True, autoincrement=True)
    tag_name   = Column("tagName", String(50), unique=True, nullable=False)
    color      = Column(String(20))
    created_at = Column(DateTime, default=datetime.now)
