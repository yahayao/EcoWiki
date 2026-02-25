"""
认证相关 Pydantic 模型
"""
from pydantic import BaseModel, EmailStr, field_validator
from typing import Optional


class LoginRequest(BaseModel):
    username: str
    password: str


class RegisterRequest(BaseModel):
    username: str
    password: str
    email: EmailStr
    full_name: Optional[str] = None

    @field_validator("username")
    @classmethod
    def username_length(cls, v: str) -> str:
        if len(v) < 3 or len(v) > 50:
            raise ValueError("用户名长度必须在 3~50 个字符之间")
        return v

    @field_validator("password")
    @classmethod
    def password_length(cls, v: str) -> str:
        if len(v) < 6:
            raise ValueError("密码长度至少 6 个字符")
        return v


class ResetPasswordRequest(BaseModel):
    email: str
    security_question: str
    security_answer: str
    new_password: str


class ChangePasswordRequest(BaseModel):
    old_password: str
    new_password: str


class TokenResponse(BaseModel):
    access_token: str
    token_type: str = "Bearer"
    username: str
    user_id: int
    role_id: Optional[int] = None
    avatar_url: Optional[str] = None
