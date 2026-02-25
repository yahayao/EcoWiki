"""
通用 API 响应模型
"""
from typing import Generic, Optional, TypeVar
from pydantic import BaseModel

T = TypeVar("T")


class ApiResponse(BaseModel, Generic[T]):
    success: bool = True
    message: str = "操作成功"
    data: Optional[T] = None

    @classmethod
    def ok(cls, data: T = None, message: str = "操作成功") -> "ApiResponse[T]":
        return cls(success=True, message=message, data=data)

    @classmethod
    def fail(cls, message: str = "操作失败", data: T = None) -> "ApiResponse[T]":
        return cls(success=False, message=message, data=data)


class PageResult(BaseModel, Generic[T]):
    """分页结果"""
    content: list[T]
    total_elements: int
    total_pages: int
    page: int
    size: int
    number_of_elements: int
