-- 标签系统数据迁移脚本
-- 将 articles 表中的 tags 字段数据迁移到独立的 tags 表和 Article_Tags 关联表

-- 开始事务
START TRANSACTION;

-- 1. 首先确保 tags 表和 Article_Tags 表存在（根据图片显示已经存在）
-- 如果需要创建表，可以取消以下注释：

/*
-- 创建 tags 表
CREATE TABLE IF NOT EXISTS tags (
    tag_id INT AUTO_INCREMENT PRIMARY KEY,
    tag_name VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(500),
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 创建 Article_Tags 关联表
CREATE TABLE IF NOT EXISTS Article_Tags (
    article_id INT NOT NULL,
    tag_id INT NOT NULL,
    PRIMARY KEY (article_id, tag_id),
    FOREIGN KEY (article_id) REFERENCES articles(article_id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES tags(tag_id) ON DELETE CASCADE
);
*/

-- 2. 从 articles 表中提取所有唯一的标签
-- 创建临时表存储标签数据
CREATE TEMPORARY TABLE temp_tags AS
SELECT DISTINCT
    TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(all_tags.tag, ',', numbers.n), ',', -1)) as tag_name
FROM (
    SELECT 
        CONCAT(tags, ',') as tag,
        LENGTH(tags) - LENGTH(REPLACE(tags, ',', '')) + 1 as tag_count
    FROM articles 
    WHERE tags IS NOT NULL AND tags != ''
) all_tags
CROSS JOIN (
    SELECT 1 n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL 
    SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL 
    SELECT 9 UNION ALL SELECT 10 UNION ALL SELECT 11 UNION ALL SELECT 12 UNION ALL 
    SELECT 13 UNION ALL SELECT 14 UNION ALL SELECT 15 UNION ALL SELECT 16 UNION ALL 
    SELECT 17 UNION ALL SELECT 18 UNION ALL SELECT 19 UNION ALL SELECT 20
) numbers
WHERE numbers.n <= all_tags.tag_count
AND TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(all_tags.tag, ',', numbers.n), ',', -1)) != '';

-- 3. 插入唯一标签到 tags 表（避免重复）
INSERT IGNORE INTO tags (tag_name, created_time)
SELECT DISTINCT 
    tag_name,
    NOW() as created_time
FROM temp_tags
WHERE tag_name IS NOT NULL AND tag_name != '';

-- 4. 为每篇文章创建标签关联关系
-- 使用存储过程来处理复杂的字符串分割和插入操作
DELIMITER //

CREATE PROCEDURE migrate_article_tags()
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE v_article_id INT;
    DECLARE v_tags TEXT;
    DECLARE v_tag_name VARCHAR(255);
    DECLARE v_tag_id INT;
    DECLARE v_pos INT;
    DECLARE v_next_pos INT;
    DECLARE v_remaining_tags TEXT;
    
    -- 声明游标
    DECLARE article_cursor CURSOR FOR 
        SELECT article_id, tags 
        FROM articles 
        WHERE tags IS NOT NULL AND tags != '';
    
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    
    -- 打开游标
    OPEN article_cursor;
    
    -- 循环处理每篇文章
    article_loop: LOOP
        FETCH article_cursor INTO v_article_id, v_tags;
        
        IF done THEN
            LEAVE article_loop;
        END IF;
        
        -- 处理当前文章的标签字符串
        SET v_remaining_tags = CONCAT(TRIM(v_tags), ',');
        
        -- 分割标签字符串并插入关联关系
        tag_loop: WHILE LENGTH(v_remaining_tags) > 0 DO
            SET v_pos = LOCATE(',', v_remaining_tags);
            
            IF v_pos > 0 THEN
                SET v_tag_name = TRIM(SUBSTRING(v_remaining_tags, 1, v_pos - 1));
                SET v_remaining_tags = SUBSTRING(v_remaining_tags, v_pos + 1);
                
                -- 如果标签名不为空
                IF LENGTH(v_tag_name) > 0 THEN
                    -- 获取标签ID
                    SELECT tag_id INTO v_tag_id 
                    FROM tags 
                    WHERE tag_name = v_tag_name 
                    LIMIT 1;
                    
                    -- 如果找到标签ID，插入关联关系（避免重复）
                    IF v_tag_id IS NOT NULL THEN
                        INSERT IGNORE INTO Article_Tags (article_id, tag_id)
                        VALUES (v_article_id, v_tag_id);
                    END IF;
                    
                    SET v_tag_id = NULL;
                END IF;
            ELSE
                LEAVE tag_loop;
            END IF;
        END WHILE;
        
    END LOOP;
    
    -- 关闭游标
    CLOSE article_cursor;
END //

DELIMITER ;

-- 5. 执行迁移存储过程
CALL migrate_article_tags();

-- 6. 删除存储过程（清理）
DROP PROCEDURE migrate_article_tags;

-- 7. 删除临时表
DROP TEMPORARY TABLE temp_tags;

-- 8. 验证迁移结果
SELECT 
    '标签总数' as 统计项,
    COUNT(*) as 数量
FROM tags
UNION ALL
SELECT 
    '关联关系总数' as 统计项,
    COUNT(*) as 数量
FROM Article_Tags
UNION ALL
SELECT 
    '有标签的文章数' as 统计项,
    COUNT(DISTINCT article_id) as 数量
FROM Article_Tags;

-- 显示一些示例数据
SELECT 
    a.article_id,
    a.title,
    a.tags as 原始标签字符串,
    GROUP_CONCAT(t.tag_name ORDER BY t.tag_name) as 迁移后的标签
FROM articles a
LEFT JOIN Article_Tags at ON a.article_id = at.article_id
LEFT JOIN tags t ON at.tag_id = t.tag_id
WHERE a.tags IS NOT NULL AND a.tags != ''
GROUP BY a.article_id, a.title, a.tags
LIMIT 10;

-- 提交事务
COMMIT;

-- 输出完成信息
SELECT '标签系统数据迁移完成！' as 状态;
