"""
认证路由：注册、登录、用户信息
"""
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from typing import Optional

from database import get_db
from models.user import User
from schemas.auth import (
    LoginRequest, RegisterRequest, TokenResponse,
    ResetPasswordRequest, ChangePasswordRequest,
)
from schemas.user import UserOut, UpdateProfileRequest
from schemas.common import ApiResponse
from core.security import (
    hash_password, verify_password,
    create_access_token, get_current_user,
)

router = APIRouter(prefix="/auth", tags=["认证"])


# ── 注册 ────────────────────────────────────────────────────────────────────
@router.post("/register")
def register(body: RegisterRequest, db: Session = Depends(get_db)):
    if db.query(User).filter(User.username == body.username).first():
        raise HTTPException(status_code=400, detail="用户名已被占用")
    if db.query(User).filter(User.email == body.email).first():
        raise HTTPException(status_code=400, detail="邮箱已被注册")

    user = User(
        username=body.username,
        password=hash_password(body.password),
        email=body.email,
        full_name=body.full_name,
        active=True,
        role_id=2,   # 默认普通用户
    )
    db.add(user)
    db.commit()
    db.refresh(user)

    token = create_access_token(user.username)
    return ApiResponse.ok(
        data={
            "user_id":    user.user_id,
            "username":   user.username,
            "email":      user.email,
            "role_id":    user.role_id,
            "token":      token,
            "token_type": "Bearer",
        },
        message="注册成功",
    )


# ── 登录 ────────────────────────────────────────────────────────────────────
@router.post("/login")
def login(body: LoginRequest, db: Session = Depends(get_db)):
    user = db.query(User).filter(
        (User.username == body.username) | (User.email == body.username)
    ).first()

    if not user or not verify_password(body.password, user.password):
        raise HTTPException(status_code=401, detail="用户名或密码错误")
    if not user.active:
        raise HTTPException(status_code=403, detail="账户已被禁用")

    from datetime import datetime
    user.last_login = datetime.now()
    db.commit()

    token = create_access_token(user.username)
    return ApiResponse.ok(
        data={
            "user_id":    user.user_id,
            "username":   user.username,
            "email":      user.email,
            "role_id":    user.role_id,
            "avatar_url": user.avatar_url,
            "token":      token,
            "token_type": "Bearer",
        },
        message="登录成功",
    )


# ── 当前用户信息 ─────────────────────────────────────────────────────────────
@router.get("/me", response_model=ApiResponse[UserOut])
def me(current_user: User = Depends(get_current_user)):
    return ApiResponse.ok(data=UserOut.model_validate(current_user))


# ── 检查用户名可用性 ──────────────────────────────────────────────────────────
@router.get("/check-username")
def check_username(username: str, db: Session = Depends(get_db)):
    exists = db.query(User).filter(User.username == username).first()
    return ApiResponse.ok(data={"available": exists is None})


# ── 检查邮箱可用性 ───────────────────────────────────────────────────────────
@router.get("/check-email")
def check_email(email: str, db: Session = Depends(get_db)):
    exists = db.query(User).filter(User.email == email).first()
    return ApiResponse.ok(data={"available": exists is None})


# ── 修改个人资料 ─────────────────────────────────────────────────────────────
@router.put("/profile")
def update_profile(
    body: UpdateProfileRequest,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    if body.full_name is not None:
        current_user.full_name = body.full_name
    if body.email is not None:
        conflict = db.query(User).filter(
            User.email == body.email,
            User.user_id != current_user.user_id,
        ).first()
        if conflict:
            raise HTTPException(status_code=400, detail="邮箱已被使用")
        current_user.email = body.email
    if body.gender is not None:
        current_user.gender = body.gender
    if body.bio is not None:
        current_user.bio = body.bio
    if body.security_question is not None:
        current_user.security_question = body.security_question
    if body.security_answer is not None:
        current_user.security_answer = hash_password(body.security_answer)

    db.commit()
    db.refresh(current_user)
    return ApiResponse.ok(data=UserOut.model_validate(current_user), message="资料更新成功")


# ── 修改密码 ─────────────────────────────────────────────────────────────────
@router.put("/change-password")
def change_password(
    body: ChangePasswordRequest,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    if not verify_password(body.old_password, current_user.password):
        raise HTTPException(status_code=400, detail="旧密码不正确")
    current_user.password = hash_password(body.new_password)
    db.commit()
    return ApiResponse.ok(message="密码修改成功")


@router.post("/change-password")
def change_password_post(
    body: ChangePasswordRequest,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    """修改密码（POST 方式别名）"""
    if not verify_password(body.old_password, current_user.password):
        raise HTTPException(status_code=400, detail="旧密码不正确")
    current_user.password = hash_password(body.new_password)
    db.commit()
    return ApiResponse.ok(message="密码修改成功")


@router.put("/security")
def update_security_settings(
    body: UpdateProfileRequest,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    """更新安全设置（PUT /profile 的别名，专注于安全字段）"""
    if body.security_question is not None:
        current_user.security_question = body.security_question
    if body.security_answer is not None:
        current_user.security_answer = hash_password(body.security_answer)
    db.commit()
    db.refresh(current_user)
    return ApiResponse.ok(data=UserOut.model_validate(current_user), message="安全设置更新成功")


# ── 重置密码（通过密保）──────────────────────────────────────────────────────
@router.post("/reset-password")
def reset_password(body: ResetPasswordRequest, db: Session = Depends(get_db)):
    user = db.query(User).filter(User.email == body.email).first()
    if not user:
        raise HTTPException(status_code=404, detail="邮箱不存在")
    if user.security_question != body.security_question:
        raise HTTPException(status_code=400, detail="安全问题不匹配")
    if not verify_password(body.security_answer, user.security_answer or ""):
        raise HTTPException(status_code=400, detail="安全答案不正确")

    user.password = hash_password(body.new_password)
    db.commit()
    return ApiResponse.ok(message="密码重置成功")
