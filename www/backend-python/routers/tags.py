"""
标签路由：列表、创建、删除
"""
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from typing import List, Optional

from database import get_db
from models.tag import Tag
from schemas.article import TagOut
from schemas.common import ApiResponse
from core.security import get_current_user, require_admin
from models.user import User

router = APIRouter(prefix="/tags", tags=["标签"])


@router.get("", response_model=ApiResponse[List[TagOut]])
def list_tags(db: Session = Depends(get_db)):
    tags = db.query(Tag).order_by(Tag.tag_name.asc()).all()
    return ApiResponse.ok(data=[TagOut.model_validate(t) for t in tags])


@router.post("", response_model=ApiResponse[TagOut])
def create_tag(
    body: dict,
    _: User = Depends(require_admin),
    db: Session = Depends(get_db),
):
    tag_name = body.get("tag_name")
    if not tag_name:
        raise HTTPException(status_code=400, detail="标签名不能为空")
    existing = db.query(Tag).filter(Tag.tag_name == tag_name).first()
    if existing:
        raise HTTPException(status_code=400, detail="标签已存在")
    tag = Tag(tag_name=tag_name, color=body.get("color"))
    db.add(tag)
    db.commit()
    db.refresh(tag)
    return ApiResponse.ok(data=TagOut.model_validate(tag), message="标签创建成功")


@router.put("/{tag_id}", response_model=ApiResponse[TagOut])
def update_tag(
    tag_id: int,
    body: dict,
    _: User = Depends(require_admin),
    db: Session = Depends(get_db),
):
    tag = db.query(Tag).filter(Tag.tag_id == tag_id).first()
    if not tag:
        raise HTTPException(status_code=404, detail="标签不存在")
    if "tag_name" in body:
        tag.tag_name = body["tag_name"]
    if "color" in body:
        tag.color = body["color"]
    db.commit()
    db.refresh(tag)
    return ApiResponse.ok(data=TagOut.model_validate(tag), message="标签更新成功")


@router.delete("/{tag_id}")
def delete_tag(
    tag_id: int,
    _: User = Depends(require_admin),
    db: Session = Depends(get_db),
):
    tag = db.query(Tag).filter(Tag.tag_id == tag_id).first()
    if not tag:
        raise HTTPException(status_code=404, detail="标签不存在")
    db.delete(tag)
    db.commit()
    return ApiResponse.ok(message="标签删除成功")
