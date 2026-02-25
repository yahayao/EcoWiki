"""
文件上传路由：头像上传
"""
import os
import uuid
from pathlib import Path

from fastapi import APIRouter, Depends, File, HTTPException, UploadFile
from sqlalchemy.orm import Session

from config import settings
from database import get_db
from models.user import User
from schemas.common import ApiResponse
from core.security import get_current_user

router = APIRouter(prefix="/upload", tags=["上传"])

ALLOWED_EXTS   = {".jpg", ".jpeg", ".png", ".gif", ".webp"}
ALLOWED_MIMES  = {"image/jpeg", "image/png", "image/gif", "image/webp"}


@router.post("/avatar")
async def upload_avatar(
    file: UploadFile = File(...),
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    # 检查文件类型
    if file.content_type not in ALLOWED_MIMES:
        raise HTTPException(status_code=400, detail="仅支持 JPG / PNG / GIF / WebP 格式")

    ext = Path(file.filename or "").suffix.lower()
    if ext not in ALLOWED_EXTS:
        ext = "." + (file.content_type.split("/")[-1] or "jpg")

    # 检查大小
    content = await file.read()
    if len(content) > settings.max_avatar_size:
        raise HTTPException(status_code=400, detail="文件大小超过 5MB 限制")

    # 保存文件
    upload_dir = Path(settings.upload_path)
    upload_dir.mkdir(parents=True, exist_ok=True)

    filename = f"{current_user.user_id}_{uuid.uuid4().hex}{ext}"
    dest = upload_dir / filename
    dest.write_bytes(content)

    # 删除旧头像（可选）
    if current_user.avatar_url:
        old_name = current_user.avatar_url.split("/")[-1]
        old_path = upload_dir / old_name
        if old_path.exists() and old_path != dest:
            try:
                old_path.unlink()
            except Exception:
                pass

    # 更新数据库
    avatar_url = f"{settings.server_base_url}/api/uploads/avatars/{filename}"
    current_user.avatar_url = avatar_url
    db.commit()

    return ApiResponse.ok(data={"avatar_url": avatar_url}, message="头像上传成功")
