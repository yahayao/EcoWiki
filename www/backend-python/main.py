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


# ── 启动时自动建表（仅供开发，生产环境建议用 Alembic 管理迁移） ───────────────
@app.on_event("startup")
def on_startup():
    # 如果表不存在则创建（不会删除已有表）
    Base.metadata.create_all(bind=engine)
    print("✅ EcoWiki Python Backend 启动成功！")
    print(f"   文档地址: {settings.server_base_url}/api/docs")


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
