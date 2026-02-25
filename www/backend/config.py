"""
应用配置模块
"""
from pydantic_settings import BaseSettings
from typing import List
import json


class Settings(BaseSettings):
    # 数据库配置
    db_host: str = "localhost"
    db_port: int = 3306
    db_user: str = "root"
    db_password: str = ""
    db_name: str = "EcoWiki"

    # JWT 配置
    jwt_secret_key: str = "ecowiki-python-backend-secret-key-2026"
    jwt_algorithm: str = "HS256"
    jwt_expire_minutes: int = 1440       # 24小时
    jwt_refresh_expire_minutes: int = 10080  # 7天

    # 服务器配置
    server_host: str = "0.0.0.0"
    server_port: int = 8080
    server_base_url: str = "http://localhost:8080"

    # 文件上传配置
    upload_path: str = "../backend/uploads/avatars/"
    max_avatar_size: int = 5242880   # 5MB

    # 超级管理员初始账户（首次启动若不存在则自动创建）
    superadmin_username: str = "admin"
    superadmin_password: str = "admin123"
    superadmin_email: str = "admin@ecowiki.com"

    # CORS 配置
    cors_origins: str = '["http://localhost:5173","http://localhost:3000"]'

    @property
    def database_url(self) -> str:
        return (
            f"mysql+pymysql://{self.db_user}:{self.db_password}"
            f"@{self.db_host}:{self.db_port}/{self.db_name}"
            f"?charset=utf8mb4"
        )

    @property
    def allowed_origins(self) -> List[str]:
        try:
            return json.loads(self.cors_origins)
        except Exception:
            return ["*"]

    class Config:
        env_file = ".env"
        env_file_encoding = "utf-8"
        case_sensitive = False


settings = Settings()
