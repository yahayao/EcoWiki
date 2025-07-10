# 文章历史版本系统

## 概述

这是一个高效的文章历史版本管理系统，采用混合存储策略，实现了智能的版本存储、压缩和归档功能。

## 核心特性

### 1. 混合存储策略
- **第一个版本**：存储完整内容作为基础版本
- **小更改**：只存储差异数据，大幅节省存储空间
- **大更改**：当差异大小超过原文的70%时，直接存储完整版本
- **版本链优化**：超过10个差异版本时，自动合并并重新计算基础版本

### 2. 冷热数据分离
- **热数据**：频繁访问的版本保持快速访问
- **冷数据**：超过30天未访问的版本进行额外压缩
- **归档策略**：超过90天的版本移动到归档存储

### 3. 智能压缩
- **自动算法选择**：在brotli、GZIP和Deflate之间自动选择最佳压缩算法
- **多层压缩**：归档数据使用更强的压缩算法
- **压缩率监控**：实时监控和统计压缩效果

### 4. 版本去重
- **内容哈希**：使用SHA-256计算内容哈希值
- **重复检测**：创建版本时检测是否有相同内容的版本
- **智能链接**：重复内容自动链接到已存在的版本

## 系统架构

### 核心组件

1. **ArticleVersion** - 版本实体类
2. **ArticleVersionStats** - 版本统计实体类
3. **ArticleVersionService** - 核心业务逻辑
4. **CompressionUtil** - 压缩工具类
5. **DiffUtil** - 差异计算工具类

### 存储类型

```java
public enum StorageType {
    FULL_CONTENT,    // 完整内容
    DIFF_FROM_BASE,  // 基于基础版本的差异
    DIFF_FROM_PREV   // 基于前一版本的差异
}
```

### 数据表结构

#### article_versions
- `version_id` - 版本ID（主键）
- `article_id` - 文章ID
- `version_number` - 版本号
- `storage_type` - 存储类型
- `content` - 内容/差异数据（压缩存储）
- `content_hash` - 内容哈希值
- `compression_algorithm` - 压缩算法
- `is_archived` - 是否已归档

#### article_version_stats
- `article_id` - 文章ID
- `total_versions` - 总版本数
- `base_versions_count` - 基础版本数
- `diff_versions_count` - 差异版本数
- `access_frequency` - 访问频率
- `last_optimized_at` - 最后优化时间

## API接口

### 创建版本
```http
POST /api/articles/{articleId}/versions
Content-Type: application/json

{
    "content": "文章内容",
    "author": "作者名称",
    "changeSummary": "修改说明"
}
```

### 获取版本内容
```http
GET /api/articles/{articleId}/versions/{versionNumber}
```

### 获取最新版本
```http
GET /api/articles/{articleId}/versions/latest
```

### 获取版本历史
```http
GET /api/articles/{articleId}/versions?page=0&size=20
```

### 获取统计信息
```http
GET /api/articles/{articleId}/versions/stats
```

## 性能优化

### 自动优化任务
- **执行时间**：每天凌晨2点
- **优化条件**：
  - 差异版本超过10个
  - 超过30天未优化
  - 手动标记需要优化

### 冷数据归档任务
- **执行时间**：每天凌晨3点
- **归档条件**：
  - 版本创建超过90天
  - 超过30天未访问

### 压缩策略
```java
// 自动选择最佳压缩算法
CompressionResult result = compressionUtil.compressBest(content);

// 压缩率计算
double ratio = (double) compressedSize / originalSize;
```

## 使用示例

### 创建文章版本
```java
@Autowired
private ArticleVersionService versionService;

// 创建新版本
ArticleVersion version = versionService.createVersion(
    articleId, 
    content, 
    author
);
```

### 获取版本内容
```java
// 获取指定版本
String content = versionService.getVersionContent(articleId, versionNumber);

// 获取最新版本
String latestContent = versionService.getLatestVersionContent(articleId);
```

### 版本统计
```java
ArticleVersionStats stats = versionService.getVersionStats(articleId);
System.out.println("总版本数: " + stats.getTotalVersions());
System.out.println("压缩率: " + stats.getCompressionRatio());
```

## 配置参数

系统提供了灵活的配置选项：

```sql
-- 差异阈值（70%）
UPDATE article_version_config 
SET config_value = '0.7' 
WHERE config_key = 'diff_threshold_percent';

-- 优化触发阈值（10个差异版本）
UPDATE article_version_config 
SET config_value = '10' 
WHERE config_key = 'optimization_diff_count';

-- 冷数据天数（30天）
UPDATE article_version_config 
SET config_value = '30' 
WHERE config_key = 'cold_data_days';
```

## 监控和维护

### 数据库视图
- `v_article_version_summary` - 版本统计概览
- `v_storage_efficiency` - 存储效率分析

### 存储过程
- `GetArticlesNeedingOptimization()` - 查找需要优化的文章
- `GetColdVersions()` - 查找冷数据版本

### 性能指标
- 存储空间节省率
- 压缩效率
- 版本重构速度
- 归档数据比例

## 最佳实践

1. **定期监控**：关注存储空间使用和压缩效率
2. **合理配置**：根据业务需求调整阈值参数
3. **版本清理**：定期清理不需要的旧版本
4. **性能测试**：定期测试版本重构性能
5. **备份策略**：确保归档数据的备份策略

## 故障排查

### 常见问题

1. **版本重构失败**
   - 检查基础版本是否存在
   - 验证差异数据完整性
   - 确认压缩算法支持

2. **存储空间增长过快**
   - 检查差异阈值设置
   - 确认压缩功能正常
   - 验证归档任务执行

3. **访问性能下降**
   - 检查版本链长度
   - 确认索引完整性
   - 考虑增加基础版本

## 扩展功能

系统设计具有良好的扩展性，可以轻松添加：

1. **更多压缩算法**：Brotli、LZ4等
2. **分布式存储**：支持对象存储
3. **版本比较**：可视化差异显示
4. **版本合并**：支持版本分支合并
5. **访问控制**：细粒度的版本访问权限

## 总结

这个文章历史版本系统提供了一个完整、高效、可扩展的解决方案，通过智能的存储策略和自动化的维护任务，在保证数据完整性的同时，大幅降低了存储成本和维护复杂度。
