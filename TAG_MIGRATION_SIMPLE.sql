-- 简化版标签数据迁移脚本
-- 适用于将 articles 表中的 tags 字段迁移到新的标签系统

-- 开始事务
START TRANSACTION;

-- 步骤1: 清理可能存在的旧数据
DELETE FROM Article_Tags;
DELETE FROM tags;

-- 步骤2: 提取并插入唯一标签
-- 使用简单的方法处理标签字符串
INSERT INTO tags (tag_name, created_time)
SELECT DISTINCT 
    TRIM(tag_part) as tag_name,
    NOW() as created_time
FROM (
    -- 处理单个标签的文章
    SELECT TRIM(tags) as tag_part
    FROM articles 
    WHERE tags IS NOT NULL 
    AND tags != '' 
    AND tags NOT LIKE '%,%'
    
    UNION DISTINCT
    
    -- 处理第一个标签
    SELECT TRIM(SUBSTRING_INDEX(tags, ',', 1)) as tag_part
    FROM articles 
    WHERE tags IS NOT NULL 
    AND tags != '' 
    AND tags LIKE '%,%'
    
    UNION DISTINCT
    
    -- 处理第二个标签
    SELECT TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(tags, ',', 2), ',', -1)) as tag_part
    FROM articles 
    WHERE tags IS NOT NULL 
    AND tags != '' 
    AND tags LIKE '%,%,%'
    OR (tags LIKE '%,%' AND tags NOT LIKE '%,%,%')
    
    UNION DISTINCT
    
    -- 处理第三个标签
    SELECT TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(tags, ',', 3), ',', -1)) as tag_part
    FROM articles 
    WHERE tags IS NOT NULL 
    AND tags != '' 
    AND tags LIKE '%,%,%,%'
    OR (tags LIKE '%,%,%' AND tags NOT LIKE '%,%,%,%')
    
    UNION DISTINCT
    
    -- 处理第四个标签
    SELECT TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(tags, ',', 4), ',', -1)) as tag_part
    FROM articles 
    WHERE tags IS NOT NULL 
    AND tags != '' 
    AND tags LIKE '%,%,%,%,%'
    OR (tags LIKE '%,%,%,%' AND tags NOT LIKE '%,%,%,%,%')
    
    UNION DISTINCT
    
    -- 处理第五个标签
    SELECT TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(tags, ',', 5), ',', -1)) as tag_part
    FROM articles 
    WHERE tags IS NOT NULL 
    AND tags != '' 
    AND (tags LIKE '%,%,%,%,%' AND LENGTH(tags) - LENGTH(REPLACE(tags, ',', '')) >= 4)
) tag_list
WHERE tag_part IS NOT NULL 
AND tag_part != '' 
AND tag_part != ',';

-- 步骤3: 为单个标签的文章创建关联
INSERT INTO Article_Tags (article_id, tag_id)
SELECT DISTINCT a.article_id, t.tag_id
FROM articles a
JOIN tags t ON t.tag_name = TRIM(a.tags)
WHERE a.tags IS NOT NULL 
AND a.tags != '' 
AND a.tags NOT LIKE '%,%';

-- 步骤4: 为多个标签的文章创建关联（第一个标签）
INSERT INTO Article_Tags (article_id, tag_id)
SELECT DISTINCT a.article_id, t.tag_id
FROM articles a
JOIN tags t ON t.tag_name = TRIM(SUBSTRING_INDEX(a.tags, ',', 1))
WHERE a.tags IS NOT NULL 
AND a.tags != '' 
AND a.tags LIKE '%,%'
AND NOT EXISTS (
    SELECT 1 FROM Article_Tags at 
    WHERE at.article_id = a.article_id AND at.tag_id = t.tag_id
);

-- 步骤5: 为多个标签的文章创建关联（第二个标签）
INSERT INTO Article_Tags (article_id, tag_id)
SELECT DISTINCT a.article_id, t.tag_id
FROM articles a
JOIN tags t ON t.tag_name = TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(a.tags, ',', 2), ',', -1))
WHERE a.tags IS NOT NULL 
AND a.tags != '' 
AND (a.tags LIKE '%,%,%' OR (a.tags LIKE '%,%' AND a.tags NOT LIKE '%,%,%'))
AND TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(a.tags, ',', 2), ',', -1)) != TRIM(SUBSTRING_INDEX(a.tags, ',', 1))
AND NOT EXISTS (
    SELECT 1 FROM Article_Tags at 
    WHERE at.article_id = a.article_id AND at.tag_id = t.tag_id
);

-- 步骤6: 为多个标签的文章创建关联（第三个标签）
INSERT INTO Article_Tags (article_id, tag_id)
SELECT DISTINCT a.article_id, t.tag_id
FROM articles a
JOIN tags t ON t.tag_name = TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(a.tags, ',', 3), ',', -1))
WHERE a.tags IS NOT NULL 
AND a.tags != '' 
AND (a.tags LIKE '%,%,%,%' OR (a.tags LIKE '%,%,%' AND a.tags NOT LIKE '%,%,%,%'))
AND TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(a.tags, ',', 3), ',', -1)) != TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(a.tags, ',', 2), ',', -1))
AND NOT EXISTS (
    SELECT 1 FROM Article_Tags at 
    WHERE at.article_id = a.article_id AND at.tag_id = t.tag_id
);

-- 步骤7: 验证迁移结果
SELECT '=== 迁移统计结果 ===' as 信息;

SELECT 
    COUNT(*) as 标签总数
FROM tags;

SELECT 
    COUNT(*) as 关联关系总数
FROM Article_Tags;

SELECT 
    COUNT(DISTINCT article_id) as 有标签的文章数
FROM Article_Tags;

-- 显示前10个标签和使用次数
SELECT 
    t.tag_name as 标签名称,
    COUNT(at.article_id) as 使用次数
FROM tags t
LEFT JOIN Article_Tags at ON t.tag_id = at.tag_id
GROUP BY t.tag_id, t.tag_name
ORDER BY 使用次数 DESC, t.tag_name
LIMIT 10;

-- 显示迁移示例（对比原始数据和迁移结果）
SELECT 
    a.article_id,
    a.title,
    a.tags as 原始标签,
    GROUP_CONCAT(t.tag_name ORDER BY t.tag_name SEPARATOR ', ') as 迁移后标签
FROM articles a
LEFT JOIN Article_Tags at ON a.article_id = at.article_id
LEFT JOIN tags t ON at.tag_id = t.tag_id
WHERE a.tags IS NOT NULL AND a.tags != ''
GROUP BY a.article_id, a.title, a.tags
ORDER BY a.article_id
LIMIT 5;

-- 提交事务
COMMIT;

SELECT '标签系统数据迁移完成！请检查上述统计结果是否正确。' as 完成状态;
