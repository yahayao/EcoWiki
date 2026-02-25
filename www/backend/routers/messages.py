"""
消息路由：发送、接收、已读、删除
"""
from datetime import datetime
from typing import List, Optional

from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session

from database import get_db
from models.message import Message, MessageStatus
from models.user import User
from schemas.message import MessageOut, MessageCreateRequest
from schemas.common import ApiResponse, PageResult
from core.security import get_current_user

router = APIRouter(prefix="/messages", tags=["消息"])


@router.get("", response_model=ApiResponse[PageResult[MessageOut]])
def inbox(
    page: int = Query(0, ge=0),
    size: int = Query(20, ge=1, le=100),
    msg_type: Optional[str] = None,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    q = db.query(Message).filter(
        Message.recipient_user_id == current_user.user_id,
        Message.status != MessageStatus.DELETED,
    )
    if msg_type:
        q = q.filter(Message.message_type == msg_type)
    q = q.order_by(Message.send_time.desc())

    total = q.count()
    items = q.offset(page * size).limit(size).all()
    return ApiResponse.ok(data=PageResult(
        content=[MessageOut.model_validate(m) for m in items],
        total_elements=total,
        total_pages=(total + size - 1) // size if size else 0,
        page=page,
        size=size,
        number_of_elements=len(items),
    ))


@router.get("/sent", response_model=ApiResponse[PageResult[MessageOut]])
def sent(
    page: int = Query(0, ge=0),
    size: int = Query(20, ge=1, le=100),
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    q = (
        db.query(Message)
        .filter(Message.sender_user_id == current_user.user_id)
        .order_by(Message.send_time.desc())
    )
    total = q.count()
    items = q.offset(page * size).limit(size).all()
    return ApiResponse.ok(data=PageResult(
        content=[MessageOut.model_validate(m) for m in items],
        total_elements=total,
        total_pages=(total + size - 1) // size if size else 0,
        page=page,
        size=size,
        number_of_elements=len(items),
    ))


@router.get("/unread/count")
def unread_count(
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    from sqlalchemy import func
    count = db.query(func.count(Message.message_id)).filter(
        Message.recipient_user_id == current_user.user_id,
        Message.status == MessageStatus.UNREAD,
    ).scalar()
    return ApiResponse.ok(data={"count": count})


@router.post("", response_model=ApiResponse[MessageOut])
def send_message(
    body: MessageCreateRequest,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    recipient = db.query(User).filter(User.user_id == body.recipient_user_id).first()
    if not recipient:
        raise HTTPException(status_code=404, detail="收件人不存在")

    msg = Message(
        recipient_user_id=body.recipient_user_id,
        sender_user_id=current_user.user_id,
        content=body.content,
        subject=body.subject,
        message_type=body.message_type,
        priority=body.priority,
    )
    db.add(msg)
    db.commit()
    db.refresh(msg)
    return ApiResponse.ok(data=MessageOut.model_validate(msg), message="消息发送成功")


@router.put("/{message_id}/read")
def mark_read(
    message_id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    msg = db.query(Message).filter(
        Message.message_id == message_id,
        Message.recipient_user_id == current_user.user_id,
    ).first()
    if not msg:
        raise HTTPException(status_code=404, detail="消息不存在")
    msg.status = MessageStatus.READ
    msg.read_time = datetime.now()
    db.commit()
    return ApiResponse.ok(message="标记已读成功")


@router.put("/read-all")
def mark_all_read(
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    db.query(Message).filter(
        Message.recipient_user_id == current_user.user_id,
        Message.status == MessageStatus.UNREAD,
    ).update({"status": MessageStatus.READ, "read_time": datetime.now()})
    db.commit()
    return ApiResponse.ok(message="全部已读")


@router.delete("/{message_id}")
def delete_message(
    message_id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    msg = db.query(Message).filter(
        Message.message_id == message_id,
        Message.recipient_user_id == current_user.user_id,
    ).first()
    if not msg:
        raise HTTPException(status_code=404, detail="消息不存在")
    msg.status = MessageStatus.DELETED
    db.commit()
    return ApiResponse.ok(message="消息删除成功")


# ─── 收件箱别名（兼容前端 /received） ─────────────────────────────────────────
@router.get("/received", response_model=ApiResponse[PageResult[MessageOut]])
def received(
    page: int = Query(0, ge=0),
    size: int = Query(20, ge=1, le=100),
    msg_type: Optional[str] = None,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    return inbox(page=page, size=size, msg_type=msg_type, current_user=current_user, db=db)


# ─── 全部消息（收件+发件） ─────────────────────────────────────────────────────
@router.get("/all", response_model=ApiResponse[PageResult[MessageOut]])
def all_messages(
    page: int = Query(0, ge=0),
    size: int = Query(20, ge=1, le=100),
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    from sqlalchemy import or_
    q = (
        db.query(Message)
        .filter(
            or_(
                Message.recipient_user_id == current_user.user_id,
                Message.sender_user_id == current_user.user_id,
            ),
            Message.status != MessageStatus.DELETED,
        )
        .order_by(Message.send_time.desc())
    )
    total = q.count()
    items = q.offset(page * size).limit(size).all()
    return ApiResponse.ok(data=PageResult(
        content=[MessageOut.model_validate(m) for m in items],
        total_elements=total,
        total_pages=(total + size - 1) // size if size else 0,
        page=page,
        size=size,
        number_of_elements=len(items),
    ))


# ─── 与某用户的对话 ───────────────────────────────────────────────────────────
@router.get("/conversation/{user_id}", response_model=ApiResponse[PageResult[MessageOut]])
def conversation(
    user_id: int,
    page: int = Query(0, ge=0),
    size: int = Query(20, ge=1, le=100),
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    from sqlalchemy import or_, and_
    q = (
        db.query(Message)
        .filter(
            or_(
                and_(Message.sender_user_id == current_user.user_id, Message.recipient_user_id == user_id),
                and_(Message.sender_user_id == user_id, Message.recipient_user_id == current_user.user_id),
            ),
            Message.status != MessageStatus.DELETED,
        )
        .order_by(Message.send_time.desc())
    )
    total = q.count()
    items = q.offset(page * size).limit(size).all()
    return ApiResponse.ok(data=PageResult(
        content=[MessageOut.model_validate(m) for m in items],
        total_elements=total,
        total_pages=(total + size - 1) // size if size else 0,
        page=page,
        size=size,
        number_of_elements=len(items),
    ))


# ─── 获取未读消息列表 ──────────────────────────────────────────────────────────
@router.get("/unread", response_model=ApiResponse[List[MessageOut]])
def unread_messages(
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    items = (
        db.query(Message)
        .filter(
            Message.recipient_user_id == current_user.user_id,
            Message.status == MessageStatus.UNREAD,
        )
        .order_by(Message.send_time.desc())
        .all()
    )
    return ApiResponse.ok(data=[MessageOut.model_validate(m) for m in items])


# ─── 群发消息 ─────────────────────────────────────────────────────────────────
@router.post("/broadcast", response_model=ApiResponse[List[MessageOut]])
def broadcast_message(
    body: dict,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    recipient_ids = body.get("recipient_user_ids") or body.get("recipientUserIds") or []
    content = body.get("content", "")
    subject = body.get("subject")
    created = []
    for rid in recipient_ids:
        recipient = db.query(User).filter(User.user_id == rid).first()
        if not recipient:
            continue
        msg = Message(
            recipient_user_id=rid,
            sender_user_id=current_user.user_id,
            content=content,
            subject=subject,
        )
        db.add(msg)
        created.append(msg)
    db.commit()
    for m in created:
        db.refresh(m)
    return ApiResponse.ok(data=[MessageOut.model_validate(m) for m in created], message="群发成功")


# ─── 发送别名（兼容前端 /send） ────────────────────────────────────────────────
@router.post("/send", response_model=ApiResponse[MessageOut])
def send_message_alias(
    body: MessageCreateRequest,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    recipient = db.query(User).filter(User.user_id == body.recipient_user_id).first()
    if not recipient:
        raise HTTPException(status_code=404, detail="收件人不存在")
    msg = Message(
        recipient_user_id=body.recipient_user_id,
        sender_user_id=current_user.user_id,
        content=body.content,
        subject=body.subject,
        message_type=body.message_type,
        priority=body.priority,
    )
    db.add(msg)
    db.commit()
    db.refresh(msg)
    return ApiResponse.ok(data=MessageOut.model_validate(msg), message="消息发送成功")
