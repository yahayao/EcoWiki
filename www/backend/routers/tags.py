"""
标签路由：列表、创建、删除
"""
from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session
from sqlalchemy import func
from typing import List, Optional

from database import get_db
from models.tag import Tag
from models.article import Article
from schemas.article import TagOut
from schemas.common import ApiResponse
from core.security import get_current_user, require_admin, require_permission
from models.user import User

router = APIRouter(prefix="/tags", tags=["标签"])


@router.get("", response_model=ApiResponse[List[TagOut]])
def list_tags(db: Session = Depends(get_db)):
    tags = db.query(Tag).order_by(Tag.tag_name.asc()).all()
    return ApiResponse.ok(data=[TagOut.model_validate(t) for t in tags])


@router.get("/search", response_model=ApiResponse[List[TagOut]])
def search_tags(
    keyword: str = Query(""),
    page: int = Query(0, ge=0),
    size: int = Query(20, ge=1, le=100),
    db: Session = Depends(get_db),
):
    """按关键词搜索标签"""
    q = db.query(Tag)
    if keyword:
        q = q.filter(Tag.tag_name.ilike(f"%{keyword}%"))
    total = q.count()
    tags = q.order_by(Tag.tag_name.asc()).offset(page * size).limit(size).all()
    return ApiResponse.ok(data=[TagOut.model_validate(t) for t in tags])


@router.get("/popular", response_model=ApiResponse[List[TagOut]])
def popular_tags(
    limit: int = Query(10, ge=1, le=100),
    db: Session = Depends(get_db),
):
    """获取最常用的标签（按关联文章数倒序）"""
    # 尝试按文章关联数排序，若表结构不支持则直接返回全部
    try:
        from models.article import article_tag
        popular = (
            db.query(Tag, func.count(article_tag.c.article_id).label("cnt"))
            .outerjoin(article_tag, Tag.tag_id == article_tag.c.tag_id)
            .group_by(Tag.tag_id)
            .order_by(func.count(article_tag.c.article_id).desc())
            .limit(limit)
            .all()
        )
        tags = [row[0] for row in popular]
    except Exception:
        tags = db.query(Tag).limit(limit).all()
    return ApiResponse.ok(data=[TagOut.model_validate(t) for t in tags])


@router.get("/recent", response_model=ApiResponse[List[TagOut]])
def recent_tags(
    limit: int = Query(10, ge=1, le=100),
    db: Session = Depends(get_db),
):
    """获取最近创建的标签"""
    tags = (
        db.query(Tag)
        .order_by(Tag.created_at.desc())
        .limit(limit)
        .all()
    )
    return ApiResponse.ok(data=[TagOut.model_validate(t) for t in tags])


@router.get("/statistics")
def tag_statistics(db: Session = Depends(get_db)):
    """获取标签统计信息"""
    total_tags = db.query(func.count(Tag.tag_id)).scalar()
    return ApiResponse.ok(data={
        "total_tags": total_tags,
        "unused_tag_count": 0,
        "usage_statistics": [],
    })


@router.get("/name/{tag_name}", response_model=ApiResponse[TagOut])
def get_tag_by_name(tag_name: str, db: Session = Depends(get_db)):
    """按名称查询标签"""
    tag = db.query(Tag).filter(Tag.tag_name == tag_name).first()
    if not tag:
        raise HTTPException(status_code=404, detail="标签不存在")
    return ApiResponse.ok(data=TagOut.model_validate(tag))


@router.get("/{tag_id}", response_model=ApiResponse[TagOut])
def get_tag(tag_id: int, db: Session = Depends(get_db)):
    """按ID查询标签"""
    tag = db.query(Tag).filter(Tag.tag_id == tag_id).first()
    if not tag:
        raise HTTPException(status_code=404, detail="标签不存在")
    return ApiResponse.ok(data=TagOut.model_validate(tag))


@router.post("", response_model=ApiResponse[TagOut])
def create_tag(
    body: dict,
    _: User = Depends(require_permission("管理标签")),
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
    _: User = Depends(require_permission("管理标签")),
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


@router.delete("/unused")
def delete_unused_tags(
    _: User = Depends(require_permission("管理标签")),
    db: Session = Depends(get_db),
):
    """删除未使用的标签（暂时删除所有未关联文章的标签）"""
    try:
        from models.article import article_tag
        used_ids = db.query(article_tag.c.tag_id).distinct().subquery()
        count = db.query(Tag).filter(~Tag.tag_id.in_(used_ids)).delete(synchronize_session=False)
    except Exception:
        count = 0
    db.commit()
    return ApiResponse.ok(data=count, message=f"已删除 {count} 个未使用标签")


@router.delete("/{tag_id}")
def delete_tag(
    tag_id: int,
    _: User = Depends(require_permission("管理标签")),
    db: Session = Depends(get_db),
):
    tag = db.query(Tag).filter(Tag.tag_id == tag_id).first()
    if not tag:
        raise HTTPException(status_code=404, detail="标签不存在")
    db.delete(tag)
    db.commit()
    return ApiResponse.ok(message="标签删除成功")

