"""
用户相关数据模型
"""
from datetime import datetime
from sqlalchemy import (
    Column, Integer, BigInteger, String, Boolean, DateTime,
    SmallInteger, ForeignKey, Table, Text
)
from sqlalchemy.orm import relationship
from database import Base


# 用户-角色 关联表
user_roles_table = Table(
    "user_roles",
    Base.metadata,
    Column("user_id",  BigInteger, ForeignKey("user.user_id"),  primary_key=True),
    Column("role_id",  Integer,    ForeignKey("roles.role_id"), primary_key=True),
)

# 角色-权限 关联表
role_permissions_table = Table(
    "role_permissions",
    Base.metadata,
    Column("role_id",        Integer, ForeignKey("roles.role_id"),            primary_key=True),
    Column("permission_id",  Integer, ForeignKey("permissions.permission_id"), primary_key=True),
)


class User(Base):
    """用户实体，映射 `user` 表"""
    __tablename__ = "user"

    user_id       = Column(BigInteger, primary_key=True, autoincrement=True, name="user_id")
    username      = Column(String(50),  unique=True, nullable=False)
    password      = Column(String(255), nullable=False)
    email         = Column(String(100), unique=True, nullable=False)
    full_name     = Column("fullName", String(100))
    active        = Column(Boolean, nullable=False, default=True)
    gender        = Column(SmallInteger)
    email_verified = Column("emailVerified", Boolean)
    login_token   = Column("loginToken", String(255))
    role_id       = Column("roleId", Integer)
    permissions   = Column(String(255))
    last_login    = Column("lastLogin", DateTime)
    avatar_url    = Column("avatarUrl", String(255))
    security_question = Column("security_question", String(500))
    security_answer   = Column("security_answer",   String(500))
    bio           = Column(Text)
    created_at    = Column(DateTime, default=datetime.now)
    updated_at    = Column(DateTime, default=datetime.now, onupdate=datetime.now)

    # 关联
    roles = relationship("Role", secondary=user_roles_table, back_populates="users")


class Role(Base):
    """角色表"""
    __tablename__ = "roles"

    role_id     = Column(Integer, primary_key=True, autoincrement=True)
    role_name   = Column("roleName", String(50), unique=True, nullable=False)
    description = Column(String(255))

    users       = relationship("User", secondary=user_roles_table, back_populates="roles")
    permissions = relationship("Permission", secondary=role_permissions_table, back_populates="roles")


class Permission(Base):
    """权限表"""
    __tablename__ = "permissions"

    permission_id   = Column(Integer, primary_key=True, autoincrement=True)
    permission_name = Column("permissionName", String(100), unique=True, nullable=False)
    description     = Column(String(255))

    roles = relationship("Role", secondary=role_permissions_table, back_populates="permissions")
