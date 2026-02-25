"""数据库连接模块"""
import sys
from sqlalchemy import create_engine, text, inspect
from sqlalchemy.orm import declarative_base, sessionmaker
from sqlalchemy.exc import OperationalError
from config import settings

Base = declarative_base()


def _server_url() -> str:
    """不含数据库名的 MySQL 连接 URL（用于检查/创建数据库）"""
    return (
        f"mysql+pymysql://{settings.db_user}:{settings.db_password}"
        f"@{settings.db_host}:{settings.db_port}"
        f"?charset=utf8mb4"
    )


def ensure_database() -> None:
    """
    检查目标数据库是否存在；不存在则自动创建。
    在应用启动时（创建连接池之前）调用。
    """
    db_name = settings.db_name
    server_engine = create_engine(_server_url(), pool_pre_ping=True)
    try:
        with server_engine.connect() as conn:
            row = conn.execute(
                text(
                    "SELECT SCHEMA_NAME FROM information_schema.SCHEMATA "
                    "WHERE SCHEMA_NAME = :name"
                ),
                {"name": db_name},
            ).fetchone()

            if row is None:
                conn.execute(
                    text(
                        f"CREATE DATABASE `{db_name}` "
                        "CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci"
                    )
                )
                conn.commit()
                print(f"  [DB] 数据库 `{db_name}` 不存在，已自动创建 ✅")
            else:
                print(f"  [DB] 数据库 `{db_name}` 已存在 ✅")
    except OperationalError as exc:
        print(f"  [DB] ❌ 无法连接 MySQL 服务器: {exc}")
        sys.exit(1)
    finally:
        server_engine.dispose()


def init_tables() -> None:
    """
    在目标数据库中创建所有尚不存在的表（已有表不会被删除/修改）。
    """
    inspector = inspect(engine)
    existing = set(inspector.get_table_names())
    all_tables = [t.name for t in Base.metadata.sorted_tables]

    new_tables = [t for t in all_tables if t not in existing]
    skip_tables = [t for t in all_tables if t in existing]

    Base.metadata.create_all(bind=engine, checkfirst=True)

    if new_tables:
        print(f"  [DB] 新建表 ({len(new_tables)}): {', '.join(new_tables)}")
    if skip_tables:
        print(f"  [DB] 已有表，跳过 ({len(skip_tables)}): {', '.join(skip_tables)}")
    if not new_tables and not skip_tables:
        print("  [DB] 无需建表")


# ── 连接池（模块加载时初始化，ensure_database() 在 startup 中先运行） ─────────
engine = create_engine(
    settings.database_url,
    pool_size=10,
    max_overflow=20,
    pool_timeout=30,
    pool_recycle=1800,
    pool_pre_ping=True,
    echo=False,
)

SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)


def get_db():
    """FastAPI 依赖注入：获取数据库会话"""
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()
