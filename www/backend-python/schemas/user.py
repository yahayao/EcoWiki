"""
用户相关 Pydantic 模型
"""
from pydantic import BaseModel, EmailStr
from typing import Optional
from datetime import datetime


class UserOut(BaseModel):
    user_id: int
    username: str
    email: str
    full_name: Optional[str] = None
    active: bool = True
    gender: Optional[int] = None
    role_id: Optional[int] = None
    avatar_url: Optional[str] = None
    bio: Optional[str] = None
    created_at: Optional[datetime] = None
    last_login: Optional[datetime] = None

    model_config = {"from_attributes": True}


class UpdateProfileRequest(BaseModel):
    full_name: Optional[str] = None
    email: Optional[EmailStr] = None
    gender: Optional[int] = None
    bio: Optional[str] = None
    security_question: Optional[str] = None
    security_answer: Optional[str] = None


class UserWithRoleOut(UserOut):
    role_name: Optional[str] = None
