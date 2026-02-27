"""
EcoWiki Python 后端
FastAPI + SQLAlchemy + MySQL

启动命令：
    uv python -m main --port 8080 --reload
"""
import os
import sys
from pathlib import Path
from typing import Dict, List, Tuple

from fastapi import FastAPI, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
from fastapi.staticfiles import StaticFiles

from config import settings
from database import engine, Base

# ── 导入所有 Model（确保建表时 metadata 已加载） ────────────────────────────────
import models.user      # noqa: F401
import models.tag       # noqa: F401
import models.article   # noqa: F401
import models.comment   # noqa: F401
import models.message   # noqa: F401

# ── 导入路由 ──────────────────────────────────────────────────────────────────
from routers import auth, articles, comments, admin, tags, upload, users, messages

# ── 创建 FastAPI 应用 ─────────────────────────────────────────────────────────
app = FastAPI(
    title="EcoWiki API",
    description="EcoWiki 生态百科后端 API（Python 版）",
    version="1.0.0",
    docs_url="/api/docs",
    redoc_url="/api/redoc",
    openapi_url="/api/openapi.json",
)

# ── CORS 中间件 ───────────────────────────────────────────────────────────────
app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.allowed_origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# ── 静态文件（头像等） ────────────────────────────────────────────────────────
upload_dir = Path(settings.upload_path)
upload_dir.mkdir(parents=True, exist_ok=True)
app.mount("/api/uploads/avatars", StaticFiles(directory=str(upload_dir)), name="avatars")

# ── 注册路由（统一加 /api 前缀） ──────────────────────────────────────────────
PREFIX = "/api"

app.include_router(auth.router,     prefix=PREFIX)
app.include_router(articles.router, prefix=PREFIX)
app.include_router(comments.router, prefix=PREFIX)
app.include_router(admin.router,    prefix=PREFIX)
app.include_router(tags.router,     prefix=PREFIX)
app.include_router(upload.router,   prefix=PREFIX)
app.include_router(users.router,    prefix=PREFIX)
app.include_router(messages.router, prefix=PREFIX)


# ── 全局异常处理 ──────────────────────────────────────────────────────────────
@app.exception_handler(Exception)
async def global_exception_handler(request: Request, exc: Exception):
    return JSONResponse(
        status_code=500,
        content={"success": False, "message": f"服务器内部错误: {str(exc)}", "data": None},
    )


# ── 健康检查 ──────────────────────────────────────────────────────────────────
@app.get("/api/health", tags=["系统"])
def health():
    return {"status": "ok", "service": "EcoWiki Python Backend"}


@app.get("/api", tags=["系统"])
def root():
    return {
        "service": "EcoWiki API",
        "version": "1.0.0",
        "docs":    "/api/docs",
    }


# ── 启动初始化 ────────────────────────────────────────────────────────────────
def _seed_superadmin() -> None:
    """若超管账户不存在则自动创建"""
    from database import SessionLocal
    from models.user import User
    from core.security import hash_password

    db = SessionLocal()
    try:
        existing = db.query(User).filter(
            (User.username == settings.superadmin_username) |
            (User.email    == settings.superadmin_email)
        ).first()
        if existing:
            if existing.role_id != 1:
                existing.role_id = 1
                db.commit()
            print(f"  [Init] 超管账户已存在 -> username: {existing.username} [OK]")
            return

        admin = User(
            username=settings.superadmin_username,
            password=hash_password(settings.superadmin_password),
            email=settings.superadmin_email,
            full_name="超级管理员",
            active=True,
            role_id=1,
        )
        db.add(admin)
        db.commit()
        print(f"  [Init] 超管账户创建成功 [OK]")
        print(f"         username : {settings.superadmin_username}")
        print(f"         password : {settings.superadmin_password}")
        print(f"         email    : {settings.superadmin_email}")
    finally:
        db.close()


def _seed_rbac_data() -> None:
    """初始化角色、权限及角色权限关系（幂等）"""
    from database import SessionLocal
    from models.user import Role, Permission

    default_roles: List[Tuple[int, str, str]] = [
        (1, "admin", "管理员，可管理用户和权限"),
        (2, "user", "普通用户，可浏览文章、发表评论"),
        (3, "moderator", "版主，可管理文章和评论"),
        (4, "superadmin", "超级管理员，拥有全部权限"),
    ]

    default_permissions: List[Tuple[str, str]] = [
        ("查看文章", "允许用户查看文章内容"),
        ("创建文章", "允许用户新建文章及草稿"),
        ("编辑文章", "允许用户编辑文章内容"),
        ("删除文章", "允许用户删除文章"),
        ("发表评论", "允许用户发表评论"),
        ("查看评论", "允许用户查看评论"),
        ("删除评论", "允许用户删除评论"),
        ("查看用户", "允许查看用户列表和信息"),
        ("编辑用户", "允许编辑用户信息"),
        ("删除用户", "允许删除用户账户"),
        ("管理角色", "允许创建、编辑、删除角色"),
        ("分配权限", "允许为角色分配权限"),
        ("系统设置", "允许修改系统设置"),
        ("查看日志", "允许查看系统日志"),
        ("数据备份", "允许执行数据备份操作"),
        ("管理标签", "允许管理文章标签"),
        ("管理分类", "允许管理文章分类"),
        ("审核文章", "允许审核用户提交的文章草稿"),
    ]

    role_permission_map: Dict[str, List[str]] = {
        "user": ["查看文章", "创建文章", "查看评论", "发表评论"],
        "moderator": ["查看文章", "创建文章", "编辑文章", "审核文章", "查看评论", "发表评论", "删除评论", "管理标签"],
        "admin": [
            "查看文章", "创建文章", "编辑文章", "删除文章", "审核文章", "查看评论", "发表评论", "删除评论",
            "查看用户", "编辑用户", "管理角色", "分配权限", "管理标签", "管理分类",
        ],
        "superadmin": [name for name, _ in default_permissions],
    }

    db = SessionLocal()
    try:
        created_roles = 0
        created_permissions = 0
        assigned_permissions = 0

        roles = db.query(Role).all()
        roles_by_id = {r.role_id: r for r in roles}
        roles_by_name = {r.role_name: r for r in roles}

        for role_id, role_name, description in default_roles:
            role = roles_by_id.get(role_id) or roles_by_name.get(role_name)
            if role is None:
                role = Role(role_id=role_id, role_name=role_name, description=description)
                db.add(role)
                db.flush()
                created_roles += 1
            else:
                role.role_name = role_name
                role.description = description
            roles_by_id[role.role_id] = role
            roles_by_name[role.role_name] = role

        permissions = db.query(Permission).all()
        perms_by_name = {p.permission_name: p for p in permissions}
        for perm_name, desc in default_permissions:
            perm = perms_by_name.get(perm_name)
            if perm is None:
                perm = Permission(permission_name=perm_name, description=desc)
                db.add(perm)
                db.flush()
                perms_by_name[perm_name] = perm
                created_permissions += 1
            else:
                perm.description = desc

        for role_name, perm_names in role_permission_map.items():
            role = roles_by_name.get(role_name)
            if role is None:
                continue
            existing_ids = {p.permission_id for p in role.permissions}
            for perm_name in perm_names:
                perm = perms_by_name.get(perm_name)
                if perm and perm.permission_id not in existing_ids:
                    role.permissions.append(perm)
                    existing_ids.add(perm.permission_id)
                    assigned_permissions += 1

        db.commit()
        print(
            f"  [Init] RBAC 初始化完成 [OK] (新增角色 {created_roles}，"
            f"新增权限 {created_permissions}，新增授权 {assigned_permissions})"
        )
    finally:
        db.close()



def _seed_messages() -> None:
    """插入示例消息数据（幂等：messages 表有数据时跳过）"""
    from database import SessionLocal
    from models.message import Message, MessageStatus
    from models.user import User

    db = SessionLocal()
    try:
        if db.query(Message).count() > 0:
            return

        users = db.query(User).filter(User.active == True).limit(5).all()
        if not users:
            return

        u1 = users[0]
        u2 = users[1] if len(users) >= 2 else None
        from datetime import datetime, timedelta
        now = datetime.now()
        msgs: List[Message] = []

        msgs += [
            Message(sender_user_id=None, recipient_user_id=u1.user_id,
                    subject="欢迎来到 EcoWiki",
                    content="欢迎！您的账户已创建成功，快来探索文章并开始贡献吧。",
                    message_type="SYSTEM", priority=1, status=MessageStatus.UNREAD,
                    send_time=now - timedelta(days=3)),
            Message(sender_user_id=None, recipient_user_id=u1.user_id,
                    subject="文章审核已完成",
                    content="您提交的文章已通过审核，现已对外公开。感谢您的贡献！",
                    message_type="REVIEW", priority=2, status=MessageStatus.UNREAD,
                    send_time=now - timedelta(hours=6)),
            Message(sender_user_id=None, recipient_user_id=u1.user_id,
                    subject="安全提醒",
                    content="检测到您的账户在新设备登录，若非本人操作请立即修改密码。",
                    message_type="SYSTEM", priority=3, status=MessageStatus.READ,
                    send_time=now - timedelta(days=1), read_time=now - timedelta(hours=20)),
        ]
        if u2:
            msgs += [
                Message(sender_user_id=u2.user_id, recipient_user_id=u1.user_id,
                        subject="关于您的文章",
                        content=f"你好 {u1.username}，我阅读了您的文章，想请问一下数据来源的参考文献？",
                        message_type="USER", priority=1, status=MessageStatus.UNREAD,
                        send_time=now - timedelta(hours=2)),
                Message(sender_user_id=u1.user_id, recipient_user_id=u2.user_id,
                        subject="Re: 关于您的文章",
                        content=f"你好 {u2.username}，数据来自联合国环境规划署 2023 年全球环境调查报告，可在 unep.org 查阅。",
                        message_type="USER", priority=1, status=MessageStatus.READ,
                        send_time=now - timedelta(hours=1), read_time=now - timedelta(minutes=45)),
                Message(sender_user_id=u2.user_id, recipient_user_id=u1.user_id,
                        subject="合作邀请",
                        content=f"嗨 {u1.username}，想邀请您联合撰写一篇城市生物多样性的文章，有兴趣吗？",
                        message_type="USER", priority=1, status=MessageStatus.UNREAD,
                        send_time=now - timedelta(minutes=30)),
            ]

        for msg in msgs:
            db.add(msg)
        db.commit()
        unread = sum(1 for m in msgs if m.recipient_user_id == u1.user_id and m.status == MessageStatus.UNREAD)
        print(f"  [Init] 示例消息初始化完成 [OK] (共 {len(msgs)} 条，{u1.username} 未读 {unread} 条)")
    finally:
        db.close()


def _sync_user_role_relations() -> None:
    """将 user.role_id 同步到 user_roles 关联表（幂等）"""
    from database import SessionLocal
    from models.user import User, Role

    db = SessionLocal()
    try:
        roles_by_id = {r.role_id: r for r in db.query(Role).all()}
        users = db.query(User).all()
        linked = 0

        for user in users:
            if not user.role_id:
                continue
            role = roles_by_id.get(user.role_id)
            if role is None:
                continue
            if role not in user.roles:
                user.roles.append(role)
                linked += 1

        db.commit()
        print(f"  [Init] 用户角色关系同步完成 [OK] (新增关联 {linked} 条)")
    finally:
        db.close()


@app.on_event("startup")
def on_startup():
    from database import ensure_database, init_tables

    print("-" * 50)
    print("[EcoWiki] Python Backend 正在启动...")
    print(f"   数据库: {settings.db_host}:{settings.db_port}/{settings.db_name}")

    # 1. 确保数据库存在（不存在则自动创建）
    ensure_database()

    # 2. 确保所有表存在（不存在则自动建表）
    init_tables()

    # 3. 初始化角色与权限数据
    _seed_rbac_data()

    # 4. 初始化超级管理员账户
    _seed_superadmin()

    # 5. 同步用户角色关系表
    _sync_user_role_relations()

    # 6. 初始化示例消息数据
    _seed_messages()

    print(f"   API 文档: {settings.server_base_url}/api/docs")
    print("-" * 50)
    print("[EcoWiki] 启动成功！")


# ── 直接运行 ──────────────────────────────────────────────────────────────────
if __name__ == "__main__":
    import uvicorn
    uvicorn.run(
        "main:app",
        host=settings.server_host,
        port=settings.server_port,
        reload=True,
        reload_dirs=["."],
    )
