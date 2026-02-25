"""
用户相关 Pydantic 模型
"""
from pydantic import BaseModel, EmailStr, model_validator
from typing import Optional
from datetime import datetime

_ROLE_NAMES = {1: "admin", 2: "user", 3: "moderator"}


class UserOut(BaseModel):
    user_id: int
    username: str
    email: str
    full_name: Optional[str] = None
    active: bool = True
    gender: Optional[int] = None
    role_id: Optional[int] = None
    user_group: Optional[str] = None   # 前端使用的角色名称
    avatar_url: Optional[str] = None
    bio: Optional[str] = None
    security_question: Optional[str] = None
    created_at: Optional[datetime] = None
    last_login: Optional[datetime] = None

    model_config = {"from_attributes": True}

    @model_validator(mode="after")
    def set_user_group(self) -> "UserOut":
        if self.user_group is None and self.role_id is not None:
            self.user_group = _ROLE_NAMES.get(self.role_id, "user")
        return self


class UpdateProfileRequest(BaseModel):
    full_name: Optional[str] = None
    email: Optional[EmailStr] = None
    gender: Optional[int] = None
    bio: Optional[str] = None
    security_question: Optional[str] = None
    security_answer: Optional[str] = None


class UserWithRoleOut(UserOut):
    role_name: Optional[str] = None
