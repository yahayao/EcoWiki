# 文章管理 API 文档

## 概述

基于提供的文章表格结构，已创建了完整的文章管理API系统，包括：

### 数据库表结构

```sql
CREATE TABLE articles (
    article_id BIGINT AUTO_INCREMENT PRIMARY KEY,  -- 文章ID
    title VARCHAR(255) NOT NULL,                   -- 标题
    author VARCHAR(50) NOT NULL,                   -- 作者
    content TEXT,                                  -- 内容
    publish_date DATETIME NOT NULL,                -- 发布时间
    category VARCHAR(50),                          -- 分类
    views INT DEFAULT 0,                           -- 阅读数
    likes INT DEFAULT 0,                           -- 点赞数
    tags VARCHAR(255),                             -- 标签
    comments INT DEFAULT 0,                        -- 评论数
    update_time DATETIME                           -- 更新时间
);
```

## API 端点列表

### 基础文章操作

| 方法 | 端点 | 描述 | 参数 |
|------|------|------|------|
| POST | `/api/articles` | 创建文章 | 请求体：ArticleCreateRequest |
| GET | `/api/articles` | 获取文章列表（分页） | page, size, sortBy, sortDir |
| GET | `/api/articles/{id}` | 根据ID获取文章 | 路径参数：id |
| PUT | `/api/articles/{id}` | 更新文章 | 路径参数：id，请求体：ArticleUpdateRequest |
| DELETE | `/api/articles/{id}` | 删除文章 | 路径参数：id |

### 文章查询操作

| 方法 | 端点 | 描述 | 参数 |
|------|------|------|------|
| GET | `/api/articles/category/{category}` | 根据分类获取文章 | 路径参数：category，page, size |
| GET | `/api/articles/author/{author}` | 根据作者获取文章 | 路径参数：author |
| GET | `/api/articles/search` | 搜索文章 | keyword, page, size |
| GET | `/api/articles/tag/{tag}` | 根据标签获取文章 | 路径参数：tag，page, size |
| GET | `/api/articles/popular` | 获取热门文章 | limit |
| GET | `/api/articles/latest` | 获取最新文章 | limit |

### 文章互动操作

| 方法 | 端点 | 描述 | 参数 |
|------|------|------|------|
| POST | `/api/articles/{id}/like` | 点赞文章 | 路径参数：id |
| DELETE | `/api/articles/{id}/like` | 取消点赞 | 路径参数：id |
| PUT | `/api/articles/{id}/comments` | 更新评论数 | 路径参数：id，count |

### 统计信息

| 方法 | 端点 | 描述 | 参数 |
|------|------|------|------|
| GET | `/api/articles/statistics` | 获取文章统计信息 | 无 |

## 请求和响应格式

### ArticleCreateRequest（创建文章请求）

```json
{
    "title": "文章标题",
    "author": "作者名称",
    "content": "文章内容",
    "category": "文章分类",
    "tags": "标签1,标签2,标签3"
}
```

### ArticleUpdateRequest（更新文章请求）

```json
{
    "title": "更新的标题",
    "content": "更新的内容",
    "category": "更新的分类",
    "tags": "更新的标签"
}
```

### ArticleDto（文章响应）

```json
{
    "articleId": 1,
    "title": "文章标题",
    "author": "作者名称",
    "content": "文章内容",
    "publishDate": "2024-01-01T10:00:00",
    "category": "文章分类",
    "views": 100,
    "likes": 15,
    "tags": "标签1,标签2",
    "comments": 5,
    "updateTime": "2024-01-01T12:00:00"
}
```

### 统计信息响应

```json
{
    "totalArticles": 100,
    "totalViews": 5000,
    "totalLikes": 500
}
```

## 使用示例

### 1. 创建文章

```bash
curl -X POST http://localhost:8080/api/articles \
  -H "Content-Type: application/json" \
  -d '{
    "title": "生态环境保护",
    "author": "张三",
    "content": "这是关于生态环境保护的文章内容...",
    "category": "环保",
    "tags": "环保,生态,保护"
  }'
```

### 2. 获取文章列表

```bash
curl "http://localhost:8080/api/articles?page=0&size=10&sortBy=publishDate&sortDir=desc"
```

### 3. 搜索文章

```bash
curl "http://localhost:8080/api/articles/search?keyword=环保&page=0&size=10"
```

### 4. 点赞文章

```bash
curl -X POST http://localhost:8080/api/articles/1/like
```

### 5. 获取统计信息

```bash
curl http://localhost:8080/api/articles/statistics
```

## 特性说明

### 1. 自动时间管理
- `publish_date`：文章创建时自动设置
- `update_time`：文章更新时自动更新（使用@UpdateTimestamp）

### 2. 浏览量统计
- 通过GET `/api/articles/{id}`获取文章时会自动增加浏览量

### 3. 分页支持
- 大部分列表查询都支持分页
- 支持自定义排序字段和排序方向

### 4. 全文搜索
- 支持标题和内容的模糊搜索
- 大小写不敏感

### 5. 统计功能
- 提供文章总数、总浏览量、总点赞数等统计信息

### 6. 热门文章算法
- 基于公式：`views + likes * 2`来计算热门程度

## 数据库索引

为了提高查询性能，建议在以下字段上创建索引：
- `title`：支持标题搜索
- `author`：支持作者查询
- `category`：支持分类查询
- `publish_date`：支持时间排序
- `views`：支持浏览量排序
- `likes`：支持点赞数排序
- `update_time`：支持更新时间查询

## 错误处理

所有API都使用统一的错误响应格式：

```json
{
    "success": false,
    "message": "错误信息",
    "data": null
}
```

常见错误状态码：
- 400：请求参数错误
- 404：文章不存在
- 500：服务器内部错误
