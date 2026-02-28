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
                print(f"  [DB] 数据库 `{db_name}` 不存在，已自动创建 [OK] ")
            else:
                print(f"  [DB] 数据库 `{db_name}` 已存在 [OK] ")
    except OperationalError as exc:
        print(f"  [DB] [ERROR] 无法连接数据库服务器: {exc}")
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

    # 对现有表做增量字段迁移（只加列，不删列）
    _run_column_migrations(inspector)


def _run_column_migrations(inspector=None) -> None:
    """
    检查并按需给现有表补充缺失字段（增量迁移，幂等）。
    每次启动时自动执行，不影响已有数据。
    """
    if inspector is None:
        inspector = inspect(engine)

    # 格式: (表名, 列名, DDL片段)
    _pending: list[tuple[str, str, str]] = [
        (
            "article_drafts",
            "reviewer_id",
            "ADD COLUMN `reviewer_id` BIGINT NULL COMMENT '审核者用户ID'",
        ),
        (
            "article_drafts",
            "reviewed_at",
            "ADD COLUMN `reviewed_at` DATETIME NULL COMMENT '审核时间'",
        ),
        # article_reviews 重构为审核队列：新增 submitter 相关字段
        (
            "article_reviews",
            "submitter_id",
            "ADD COLUMN `submitter_id` BIGINT NULL COMMENT '提交者用户ID'",
        ),
        (
            "article_reviews",
            "submitted_at",
            "ADD COLUMN `submitted_at` DATETIME NULL COMMENT '提交时间'",
        ),
    ]

    with engine.connect() as conn:
        for table, column, ddl in _pending:
            if table not in inspector.get_table_names():
                continue
            existing_cols = {c["name"] for c in inspector.get_columns(table)}
            if column not in existing_cols:
                conn.execute(text(f"ALTER TABLE `{table}` {ddl}"))
                conn.commit()
                print(f"  [DB] 迁移: `{table}`.`{column}` 列已添加 [OK]")
            else:
                print(f"  [DB] 迁移: `{table}`.`{column}` 已存在，跳过")

        # 清理 article_reviews 中旧的无效记录（有 reviewer_id 但没有 submitter_id 的是旧审核日志格式）
        # 同时删除重复的 draft_id 记录，只保留最近一条，以便后续加唯一索引
        if "article_reviews" in inspector.get_table_names():
            try:
                conn.execute(text(
                    "DELETE r1 FROM article_reviews r1 "
                    "INNER JOIN article_reviews r2 "
                    "WHERE r1.review_id < r2.review_id AND r1.draft_id = r2.draft_id"
                ))
                conn.commit()
            except Exception:
                pass  # 若无重复则忽略

            # 添加 draft_id 唯一索引（保证每条草稿最多一条队列记录）
            indexes = {idx["name"] for idx in inspector.get_indexes("article_reviews")}
            if "uq_article_reviews_draft_id" not in indexes:
                try:
                    conn.execute(text(
                        "ALTER TABLE `article_reviews` "
                        "ADD UNIQUE INDEX `uq_article_reviews_draft_id` (`draft_id`)"
                    ))
                    conn.commit()
                    print("  [DB] 迁移: `article_reviews`.`draft_id` 唯一索引已添加 [OK]")
                except Exception as e:
                    print(f"  [DB] 迁移: 添加唯一索引跳过（{e}）")


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
