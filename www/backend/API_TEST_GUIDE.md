# EcoWiki 文章 API 测试指南

## API 概览

所有 API 都位于 `/api` 路径下，文章相关的 API 位于 `/api/articles` 子路径。

## 路径说明

- **应用根路径**：`/api`（由 `server.servlet.context-path=/api` 设置）
- **文章 API 基础路径**：`/api/articles`
- **通用 API 路径**：`/api/info`、`/api/articles/statistics` 等

## 测试命令

### 1. 获取 API 信息
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/info" -Method Get
```

### 2. 获取文章统计信息
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/articles/statistics" -Method Get
```

### 3. 获取所有文章（分页）
```powershell
# 默认分页（第1页，每页10条）
Invoke-RestMethod -Uri "http://localhost:8080/api/articles" -Method Get

# 自定义分页
Invoke-RestMethod -Uri "http://localhost:8080/api/articles?page=0&size=5" -Method Get
```

### 4. 根据ID获取文章（会增加浏览量）
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/articles/1" -Method Get
```

### 5. 创建新文章
```powershell
$body = @{
    title = "新测试文章"
    author = "张三"
    content = "这是一篇新的测试文章内容"
    category = "技术"
    tags = "测试,新功能,API"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/articles" -Method Post -Body $body -ContentType "application/json; charset=UTF-8"
```

### 6. 更新文章
```powershell
$body = @{
    title = "更新后的文章标题"
    author = "李四"
    content = "这是更新后的文章内容"
    category = "技术更新"
    tags = "更新,测试,API修改"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/articles/1" -Method Put -Body $body -ContentType "application/json; charset=UTF-8"
```

### 7. 搜索文章
```powershell
# 搜索英文关键词
Invoke-RestMethod -Uri "http://localhost:8080/api/articles/search?keyword=test" -Method Get

# 搜索中文关键词（推荐使用 Invoke-RestMethod）
Invoke-RestMethod -Uri "http://localhost:8080/api/articles/search?keyword=测试" -Method Get

# 使用 curl 搜索中文需要 URL 编码
$encodedKeyword = [System.Web.HttpUtility]::UrlEncode("测试", [System.Text.Encoding]::UTF8)
curl -X GET "http://localhost:8080/api/articles/search?keyword=$encodedKeyword" -H "Content-Type: application/json"
```

### 8. 按标签获取文章
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/articles/tag/测试" -Method Get
```

### 9. 按分类获取文章
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/articles/category/技术" -Method Get
```

### 10. 删除文章
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/articles/1" -Method Delete
```

## 注意事项

### 中文参数处理
1. **推荐使用 `Invoke-RestMethod`**：PowerShell 的 `Invoke-RestMethod` 能自动处理中文字符编码，无需手动编码。

2. **使用 curl 时需要 URL 编码**：
   ```powershell
   $encodedKeyword = [System.Web.HttpUtility]::UrlEncode("中文关键词", [System.Text.Encoding]::UTF8)
   curl -X GET "http://localhost:8080/api/api/articles/search?keyword=$encodedKeyword"
   ```

### API 路径说明
现在的路径结构更加清晰：
- **应用根路径**: `/api` (由 `server.servlet.context-path=/api` 设置)
- **文章相关 API**: `/api/articles`
- **通用 API**: `/api/info`、`/api/articles/statistics`

这样避免了之前的 `/api/api/articles` 双重路径问题。

### 字段说明
- `updateTime`：当文章内容被修改时自动更新
- `views`：每次通过 ID 获取文章时自动递增
- `publishDate`：创建文章时自动设置为当前时间

## 测试数据示例

当前数据库中的测试文章：
```json
{
  "articleId": 1,
  "title": "测试文章",
  "author": "测试作者",
  "content": "这是一篇测试文章的内容，用来验证API功能是否正常工作。",
  "publishDate": "2025-06-29T16:19:14",
  "category": "测试",
  "views": 2,
  "likes": 0,
  "tags": "测试,API,Spring Boot",
  "comments": 0,
  "updateTime": "2025-06-29T16:20:11"
}
```

## 验证清单

✅ 所有 CRUD 操作正常工作  
✅ 分页功能正常  
✅ 搜索功能支持中文和英文  
✅ 浏览量自动递增  
✅ 更新时间自动维护  
✅ 统计信息正确  
✅ 异常处理完善  
✅ API 响应格式统一  

所有文章相关的 API 功能已全部实现并测试通过！
