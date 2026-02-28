"""
安全核心模块：JWT 生成与验证、密码哈希
直接使用 bcrypt 库，兼容 Java BCryptPasswordEncoder 格式
"""
from datetime import datetime, timedelta, timezone
from typing import Optional

import bcrypt as _bcrypt
from jose import JWTError, jwt
from fastapi import Depends, HTTPException, status
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from sqlalchemy.orm import Session

from config import settings
from database import get_db

# Bearer token 提取器
bearer_scheme = HTTPBearer(auto_error=False)


# ── 密码工具（直接调用 bcrypt，不经 passlib） ──────────────────────────────
def verify_password(plain: str, hashed: str) -> bool:
    try:
        return _bcrypt.checkpw(plain.encode("utf-8"), hashed.encode("utf-8"))
    except Exception:
        return False


def hash_password(plain: str) -> str:
    return _bcrypt.hashpw(plain.encode("utf-8"), _bcrypt.gensalt()).decode("utf-8")


# ── JWT 工具 ──────────────────────────────────────────────────────────────
def create_access_token(subject: str, expires_delta: Optional[timedelta] = None) -> str:
    expire = datetime.now(timezone.utc) + (
        expires_delta or timedelta(minutes=settings.jwt_expire_minutes)
    )
    payload = {"sub": subject, "exp": expire, "iat": datetime.now(timezone.utc)}
    return jwt.encode(payload, settings.jwt_secret_key, algorithm=settings.jwt_algorithm)


def create_refresh_token(subject: str) -> str:
    return create_access_token(
        subject, timedelta(minutes=settings.jwt_refresh_expire_minutes)
    )


def decode_token(token: str) -> Optional[str]:
    """解码 JWT，返回 username；失败返回 None"""
    try:
        payload = jwt.decode(
            token, settings.jwt_secret_key, algorithms=[settings.jwt_algorithm]
        )
        return payload.get("sub")
    except JWTError:
        return None


# ── 依赖注入：获取当前用户 ──────────────────────────────────────────────────
def get_current_user_optional(
    credentials: Optional[HTTPAuthorizationCredentials] = Depends(bearer_scheme),
    db: Session = Depends(get_db),
):
    """可选认证，未登录时返回 None"""
    from models.user import User  # 避免循环导入

    if credentials is None:
        return None
    username = decode_token(credentials.credentials)
    if username is None:
        return None
    user = db.query(User).filter(User.username == username).first()
    return user


def get_current_user(
    credentials: Optional[HTTPAuthorizationCredentials] = Depends(bearer_scheme),
    db: Session = Depends(get_db),
):
    """必须认证，未登录时抛 401"""
    user = get_current_user_optional(credentials, db)
    if user is None:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="请先登录",
            headers={"WWW-Authenticate": "Bearer"},
        )
    return user


def require_admin(current_user=Depends(get_current_user)):
    """要求管理员权限（roleId == 1）"""
    if not current_user.role_id or current_user.role_id != 1:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN, detail="权限不足，需要管理员权限"
        )
    return current_user


def require_permission(permission_name: str):
    """
    工厂函数：返回一个 FastAPI 依赖，检查当前用户的角色是否拥有指定权限。
    用法：reviewer: User = Depends(require_permission("审核文章"))
    """
    def _dependency(
        current_user=Depends(get_current_user),
        db: Session = Depends(get_db),
    ):
        from models.user import Role  # 避免循环导入

        if not current_user.role_id:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail=f"权限不足，需要「{permission_name}」权限",
            )
        role = db.query(Role).filter(Role.role_id == current_user.role_id).first()
        if role is None or not any(p.permission_name == permission_name for p in role.permissions):
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail=f"权限不足，需要「{permission_name}」权限",
            )
        return current_user

    # 让 FastAPI 能正确生成文档和缓存依赖
    _dependency.__name__ = f"require_permission_{permission_name}"
    return _dependency


def user_has_permission(user, db: Session, permission_name: str) -> bool:
    """检查用户的角色是否拥有指定权限（可在任意 router 中复用）"""
    from models.user import Role  # 避免循环导入
    if not user or not user.role_id:
        return False
    role = db.query(Role).filter(Role.role_id == user.role_id).first()
    if role is None:
        return False
    return any(p.permission_name == permission_name for p in role.permissions)
