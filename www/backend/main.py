"""
EcoWiki Python 后端
FastAPI + SQLAlchemy + MySQL

启动命令：
    uvicorn main:app --host 0.0.0.0 --port 8080 --reload
"""
import os
from pathlib import Path

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
from routers import auth, articles, comments, messages, admin, tags, upload, users

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
app.include_router(messages.router, prefix=PREFIX)
app.include_router(admin.router,    prefix=PREFIX)
app.include_router(tags.router,     prefix=PREFIX)
app.include_router(upload.router,   prefix=PREFIX)
app.include_router(users.router,    prefix=PREFIX)


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
            print(f"  [Init] 超管账户已存在 → username: {existing.username} ✅")
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
        print(f"  [Init] 超管账户创建成功 ✅")
        print(f"         username : {settings.superadmin_username}")
        print(f"         password : {settings.superadmin_password}")
        print(f"         email    : {settings.superadmin_email}")
    finally:
        db.close()


@app.on_event("startup")
def on_startup():
    from database import ensure_database, init_tables

    print("─" * 50)
    print("🚀 EcoWiki Python Backend 正在启动...")
    print(f"   数据库: {settings.db_host}:{settings.db_port}/{settings.db_name}")

    # 1. 确保数据库存在（不存在则自动创建）
    ensure_database()

    # 2. 确保所有表存在（不存在则自动建表）
    init_tables()

    # 3. 初始化超级管理员账户
    _seed_superadmin()

    print(f"   API 文档: {settings.server_base_url}/api/docs")
    print("─" * 50)
    print("✅ 启动成功！")


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
