# EcoWiki 草稿审核系统实现文档

## 📋 系统概述

EcoWiki 草稿审核系统已完全重构，实现了新的审核流程。主要特点：

- **统一的文章创建流程**：新建文章和编辑文章都遵循相同的审核流程
- **完整的数据关联**：所有草稿都关联到对应的 article 记录
- **历史记录保留**：审核拒绝时保留完整的历史记录

## 🔄 核心流程变更

### 新建文章流程（已更新）

1. **用户提交新文章**

   ```java
   // ArticleDraftService.submitNewArticleDraft()
   // 1. 检查标题是否重复
   // 2. 先创建 Article 记录（草稿状态）
   Article article = new Article();
   article.setTitle(request.getTitle());
   // ... 设置其他字段
   Article savedArticle = articleRepository.save(article);
   
   // 3. 创建对应的 ArticleDraft 记录
   ArticleDraft draft = new ArticleDraft();
   draft.setArticleId(savedArticle.getArticleId()); // 关联到刚创建的文章
   // ... 设置其他字段
   ```

2. **审核通过处理**

   ```java
   // 更新已存在的 Article 记录（不是创建新的）
   updateExistingArticleFromDraft(draft);
   ```

3. **审核拒绝处理**

   ```java
   // 保留 Article 记录和 ArticleDraft 记录作为历史记录
   // 不删除任何数据，确保审核历史的完整性
   ```

### 编辑文章流程（保持不变）

1. 用户编辑现有文章 → 创建草稿（articleId 指向现有文章）
2. 审核通过 → 更新现有文章
3. 审核拒绝 → 保留草稿记录

## 🗄️ 数据库结构

### article_drafts 表结构

```sql
CREATE TABLE `article_drafts` (
  `draft_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '草稿ID',
  `article_id` BIGINT NOT NULL COMMENT '关联的文章ID（新建和编辑都有值）',
  `editor_user_id` BIGINT NOT NULL COMMENT '编辑者用户ID',
  `title` VARCHAR(255) NOT NULL COMMENT '文章标题',
  `content` LONGTEXT COMMENT '文章内容',
  `category` VARCHAR(50) COMMENT '文章分类',
  `tags` TEXT COMMENT '文章标签',
  `review_status` ENUM('PENDING', 'APPROVED', 'REJECTED') NOT NULL DEFAULT 'PENDING',
  `submitted_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `reviewed_at` TIMESTAMP NULL COMMENT '审核时间',
  `reviewer_user_id` BIGINT COMMENT '审核者用户ID',
  `review_notes` TEXT COMMENT '审核备注',
  -- 索引省略
);
```

**重要变更**：

- `article_id` 字段：**新建文章和编辑文章都必须有值**
- 新建文章：先创建 Article 记录，然后关联
- 编辑文章：直接关联到现有 Article

### articles 表

- 包含所有文章记录（包括审核中的草稿对应的文章）
- 审核通过后更新现有记录
- 审核拒绝后保留记录（不删除）

## 🔧 代码实现详解

### 后端关键方法

#### 1. 提交新文章草稿

```java
// ArticleDraftService.java
public ArticleDraft submitNewArticleDraft(ArticleCreateRequest request, Long editorUserId) {
    // 检查标题重复
    if (articleRepository.findByTitle(request.getTitle()).isPresent()) {
        throw new IllegalArgumentException("标题已存在");
    }
    
    // 先创建 Article 记录
    Article draftArticle = new Article();
    draftArticle.setTitle(request.getTitle());
    draftArticle.setAuthor(request.getAuthor());
    draftArticle.setContent(request.getContent());
    // ... 设置其他字段
    Article savedArticle = articleRepository.save(draftArticle);
    
    // 创建对应的草稿记录
    ArticleDraft draft = new ArticleDraft();
    draft.setArticleId(savedArticle.getArticleId()); // 关联文章ID
    draft.setEditorUserId(editorUserId);
    // ... 设置其他字段
    draft.setReviewStatus(ArticleDraft.ReviewStatus.PENDING);
    
    return articleDraftRepository.save(draft);
}
```

#### 2. 审核草稿

```java
// ArticleDraftService.java
public ArticleDraft reviewDraft(Long draftId, ReviewDraftRequest request, Long reviewerUserId) {
    // 获取草稿
    ArticleDraft draft = articleDraftRepository.findById(draftId)
        .orElseThrow(() -> new IllegalArgumentException("草稿不存在"));
    
    // 更新审核状态
    draft.setReviewStatus(request.getApproved() ? 
        ArticleDraft.ReviewStatus.APPROVED : ArticleDraft.ReviewStatus.REJECTED);
    draft.setReviewedAt(LocalDateTime.now());
    draft.setReviewNotes(request.getReviewNotes());
    
    ArticleDraft savedDraft = articleDraftRepository.save(draft);
    
    if (request.getApproved()) {
        // 审核通过：更新对应的 Article 记录
        publishApprovedDraft(savedDraft);
        sendApprovalNotificationToEditor(savedDraft, reviewerUserId);
    } else {
        // 审核拒绝：保留所有记录作为历史
        sendRejectionNotificationToEditor(savedDraft, reviewerUserId);
    }
    
    return savedDraft;
}
```

#### 3. 发布已审核通过的草稿

```java
// ArticleDraftService.java
private void publishApprovedDraft(ArticleDraft draft) {
    // 所有草稿都应该有 articleId，直接更新
    if (draft.getArticleId() != null) {
        updateExistingArticleFromDraft(draft);
    } else {
        // 兼容性处理（不应该发生）
        createNewArticleFromDraft(draft);
    }
}

private void updateExistingArticleFromDraft(ArticleDraft draft) {
    ArticleUpdateRequest updateRequest = new ArticleUpdateRequest();
    updateRequest.setTitle(draft.getTitle());
    updateRequest.setContent(draft.getContent());
    updateRequest.setCategory(draft.getCategory());
    updateRequest.setTags(draft.getTags());
    
    // 更新现有文章
    articleService.updateArticle(draft.getArticleId(), updateRequest, editorName);
}
```

### 前端API接口

#### 草稿API (draft.ts)

```typescript
export const draftApi = {
  // 提交新文章草稿
  async submitNewArticle(request: {
    title: string
    author: string
    content: string
    category: string
    tags?: string
  }): Promise<DraftSubmissionResult> {
    const response = await api.post<ApiResponse<DraftSubmissionResult>>('/api/drafts/submit-new', request)
    return response.data.data
  },
  
  // ... 其他方法
}
```

## 🚀 使用流程

### 用户创建新文章

1. 访问 `/create` 页面
2. 填写文章信息并提交
3. 前端调用 `draftApi.submitNewArticle()`
4. 后端：
   - 创建 Article 记录
   - 创建 ArticleDraft 记录并关联
   - 发送审核通知给管理员

### 管理员审核草稿

1. 访问管理后台草稿审核页面
2. 查看待审核草稿列表
3. 审核操作：
   - **通过**：更新对应的 Article 记录，文章正式发布
   - **拒绝**：保留草稿和文章记录作为历史

## ⚠️ 重要注意事项

### 数据一致性

- **所有草稿都必须有 article_id**
- 新建文章：先创建 Article，再创建 Draft
- 编辑文章：直接关联现有 Article

### 历史记录保留

- 审核拒绝时**不删除**任何记录
- 保留完整的审核历史和数据链路
- 便于后续分析和恢复

### 错误处理

- 标题重复检查
- 草稿状态验证
- 文章关联验证
- 完整的错误消息返回

## 🔄 与版本系统的集成

- 文章审核通过后，会自动触发版本系统创建新版本
- 新建文章：创建第一个版本
- 编辑文章：创建新版本（如果内容有变化）

## 📝 API端点汇总

### 草稿相关

- `POST /api/drafts/submit-new` - 提交新文章草稿
- `POST /api/drafts/submit-edit/{articleId}` - 提交编辑草稿
- `PUT /api/drafts/review/{draftId}` - 审核草稿
- `GET /api/drafts/pending` - 获取待审核草稿
- `GET /api/drafts/by-status/{status}` - 按状态获取草稿

## 🔧 故障排除

### 常见问题

1. **`article_id cannot be null` 错误**
   - 原因：数据库约束不允许 article_id 为空
   - 解决：确保代码中为新文章预先创建了 Article 记录

2. **审核通过后文章未发布**
   - 检查 `updateExistingArticleFromDraft` 方法实现
   - 确认 ArticleService.updateArticle 方法正常工作

3. **草稿列表显示异常**
   - 检查前端 DraftReviewDashboard 组件
   - 确认用户信息缓存正常加载

## 📊 性能考虑

- 草稿列表采用分页加载
- 用户信息采用批量缓存
- 审核状态变更使用缓存更新策略

---

*文档更新时间：2025-08-05*
*对应代码版本：dev/modularization分支*
