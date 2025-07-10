# 文章版本历史系统完整实现

## 后端实现

### 1. ArticleVersionService.java 新增功能

#### 恢复版本方法
```java
public ArticleVersion restoreToVersion(Long articleId, Integer versionNumber, String author)
```
- 获取指定版本的内容
- 与当前版本比较，如果相同则抛出异常
- 创建新版本，内容为恢复的版本内容
- 设置变更摘要为"恢复到版本 X"

### 2. ArticleVersionController.java 新增功能

#### 恢复版本API端点
```java
@PostMapping("/{versionNumber}/restore")
public ResponseEntity<Map<String, Object>> restoreToVersion(...)
```
- 路径: `/api/articles/{articleId}/versions/{versionNumber}/restore`
- 方法: POST
- 请求体: `RestoreVersionRequest { author }`
- 响应: 恢复成功的版本信息

#### 新增DTO类
```java
public static class RestoreVersionRequest {
    private String author;
}
```

### 3. ArticleService.java 自动版本创建

#### 创建文章时自动创建初始版本
```java
public ArticleDto createArticle(ArticleCreateRequest request)
```
- 保存文章后自动调用 `articleVersionService.createVersion()`
- 创建文章的第一个版本

#### 更新文章时自动创建新版本
```java
public ArticleDto updateArticle(Long articleId, ArticleUpdateRequest request)
```
- 比较新旧内容
- 如果内容有变化，自动创建新版本
- 版本创建失败不影响文章更新

## 前端实现

### 1. API接口更新 (article.ts)

#### 新增恢复版本API
```typescript
async restoreToVersion(articleId: number, versionNumber: number, author: string): Promise<ArticleVersion>
```
- 调用后端恢复版本API
- 返回新创建的版本信息

### 2. ArticleHistory.vue 恢复功能

#### 恢复版本实现
```typescript
const restoreVersion = async (version: ArticleVersion) => {
  // 用户确认
  // 获取文章ID
  // 调用恢复API
  // 重新加载版本列表
}
```

#### 用户体验优化
- 恢复前弹出确认对话框
- 显示操作进度和结果
- 恢复成功后自动刷新版本列表
- 错误处理和用户提示

### 3. 自动版本创建

#### 编辑文章自动创建版本
- 通过现有的 `articleApi.updateArticle()` 调用
- 后端自动检测内容变化并创建版本
- 前端无需额外代码

#### 创建文章自动创建初始版本
- 通过现有的 `articleApi.createArticle()` 调用
- 后端自动创建文章的第一个版本
- 前端无需额外代码

## 使用流程

### 1. 创建/编辑文章
1. 用户创建新文章或编辑现有文章
2. 点击保存
3. 后端自动检测内容变化
4. 如有变化，自动创建新版本

### 2. 查看版本历史
1. 在文章详情页点击"历史"按钮
2. 跳转到版本历史页面
3. 查看所有版本的列表和详细信息

### 3. 恢复版本
1. 在版本历史页面选择要恢复的版本
2. 点击"恢复"按钮
3. 确认操作
4. 系统创建新版本，内容为选定版本的内容
5. 版本列表自动更新

### 4. 版本对比
1. 在版本历史页面点击"版本对比"
2. 选择两个版本
3. 点击"开始对比"
4. 查看并排显示的版本差异

## 技术特点

### 1. 混合存储策略
- 第一个版本：完整内容存储
- 小更改：只存储差异
- 差异超过70%：存储完整版本
- 超过1个月无修改：合并小差异
- 冷热数据分离：旧版本压缩存储

### 2. 自动化管理
- 文章编辑自动创建版本
- 去重检查避免重复版本
- 自动压缩和归档
- 定时优化任务

### 3. 用户友好
- 版本类型标识
- 压缩比显示
- 操作确认机制
- 错误处理和提示

### 4. 性能优化
- 分页加载版本列表
- 按需加载版本内容
- 压缩存储节省空间
- 差异存储减少冗余

## API端点汇总

### 版本历史相关
- `GET /api/articles/{articleId}/versions` - 获取版本列表
- `GET /api/articles/{articleId}/versions/{versionNumber}` - 获取版本内容
- `GET /api/articles/{articleId}/versions/latest` - 获取最新版本
- `GET /api/articles/{articleId}/versions/stats` - 获取版本统计
- `POST /api/articles/{articleId}/versions` - 创建新版本
- `POST /api/articles/{articleId}/versions/{versionNumber}/restore` - 恢复版本

### 文章相关（已有）
- `POST /api/articles` - 创建文章（自动创建初始版本）
- `PUT /api/articles/{id}` - 更新文章（自动创建新版本）

## 数据库表结构

### article_versions
- 存储文章版本数据
- 支持完整内容和差异存储
- 包含压缩和归档标识

### article_version_stats
- 存储版本统计信息
- 优化和访问频率跟踪
- 自动维护统计数据

## 注意事项

1. **版本创建是异步的**：文章保存不会因版本创建失败而失败
2. **去重机制**：相同内容不会创建重复版本
3. **性能考虑**：大量版本时建议定期清理或归档
4. **权限控制**：目前恢复版本使用原作者身份，可根据需要调整
5. **错误处理**：所有操作都有完整的错误处理和用户提示

## 后续优化建议

1. **权限控制**：添加版本操作权限检查
2. **批量操作**：支持批量删除或归档版本
3. **版本标签**：支持给重要版本添加标签
4. **差异高亮**：优化版本对比的可视化效果
5. **性能监控**：添加版本存储性能监控和报警
