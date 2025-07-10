-- 文章版本系统数据库迁移脚本
-- 创建文章版本表和相关索引

-- 1. 创建文章版本表
CREATE TABLE IF NOT EXISTS article_versions (
    version_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    article_id BIGINT NOT NULL,
    version_number INT NOT NULL,
    author VARCHAR(100),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    storage_type ENUM('FULL_CONTENT', 'DIFF_FROM_BASE', 'DIFF_FROM_PREV') NOT NULL,
    content LONGBLOB,
    content_hash VARCHAR(64),
    original_size BIGINT,
    compressed_size BIGINT,
    base_version_id BIGINT,
    is_compressed BOOLEAN NOT NULL DEFAULT FALSE,
    is_archived BOOLEAN NOT NULL DEFAULT FALSE,
    change_summary VARCHAR(500),
    compression_algorithm VARCHAR(20),
    
    -- 索引
    INDEX idx_article_id (article_id),
    INDEX idx_version_number (version_number),
    INDEX idx_created_at (created_at),
    INDEX idx_storage_type (storage_type),
    INDEX idx_is_archived (is_archived),
    INDEX idx_content_hash (content_hash),
    INDEX idx_base_version_id (base_version_id),
    
    -- 外键约束
    FOREIGN KEY (base_version_id) REFERENCES article_versions(version_id) ON DELETE SET NULL,
    
    -- 唯一约束：每篇文章的版本号唯一
    UNIQUE KEY uk_article_version (article_id, version_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. 创建文章版本统计表
CREATE TABLE IF NOT EXISTS article_version_stats (
    stats_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    article_id BIGINT NOT NULL UNIQUE,
    total_versions INT NOT NULL DEFAULT 0,
    base_versions_count INT NOT NULL DEFAULT 0,
    diff_versions_count INT NOT NULL DEFAULT 0,
    archived_versions_count INT NOT NULL DEFAULT 0,
    total_storage_size BIGINT DEFAULT 0,
    compressed_storage_size BIGINT DEFAULT 0,
    last_optimized_at DATETIME,
    last_accessed_at DATETIME,
    access_frequency INT NOT NULL DEFAULT 0,
    optimization_needed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- 索引
    INDEX idx_article_id (article_id),
    INDEX idx_last_optimized (last_optimized_at),
    INDEX idx_last_accessed (last_accessed_at),
    INDEX idx_optimization_needed (optimization_needed),
    INDEX idx_total_storage_size (total_storage_size)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. 添加一些有用的视图
-- 版本统计视图
CREATE OR REPLACE VIEW v_article_version_summary AS
SELECT 
    av.article_id,
    COUNT(*) as total_versions,
    SUM(CASE WHEN av.storage_type = 'FULL_CONTENT' THEN 1 ELSE 0 END) as base_versions,
    SUM(CASE WHEN av.storage_type != 'FULL_CONTENT' THEN 1 ELSE 0 END) as diff_versions,
    SUM(CASE WHEN av.is_archived = TRUE THEN 1 ELSE 0 END) as archived_versions,
    SUM(av.original_size) as total_original_size,
    SUM(av.compressed_size) as total_compressed_size,
    AVG(av.compressed_size / av.original_size) as avg_compression_ratio,
    MAX(av.created_at) as latest_version_date,
    MAX(av.version_number) as latest_version_number
FROM article_versions av
GROUP BY av.article_id;

-- 存储效率视图
CREATE OR REPLACE VIEW v_storage_efficiency AS
SELECT 
    av.article_id,
    av.storage_type,
    COUNT(*) as version_count,
    SUM(av.original_size) as total_original_size,
    SUM(av.compressed_size) as total_compressed_size,
    AVG(av.compressed_size / av.original_size) as avg_compression_ratio,
    MIN(av.compressed_size / av.original_size) as best_compression_ratio,
    MAX(av.compressed_size / av.original_size) as worst_compression_ratio
FROM article_versions av
WHERE av.original_size > 0 AND av.compressed_size > 0
GROUP BY av.article_id, av.storage_type;

-- 4. 创建优化相关的存储过程
DELIMITER //

-- 查找需要优化的文章
CREATE PROCEDURE GetArticlesNeedingOptimization(
    IN diff_threshold INT,
    IN days_since_optimization INT
)
BEGIN
    -- 设置默认值
    SET diff_threshold = IFNULL(diff_threshold, 10);
    SET days_since_optimization = IFNULL(days_since_optimization, 30);
    
    SELECT DISTINCT
        avs.article_id,
        avs.total_versions,
        avs.diff_versions_count,
        avs.last_optimized_at,
        DATEDIFF(NOW(), COALESCE(avs.last_optimized_at, avs.created_at)) as days_since_optimization
    FROM article_version_stats avs
    WHERE (
        avs.diff_versions_count > diff_threshold
        OR avs.optimization_needed = TRUE
        OR avs.last_optimized_at IS NULL
        OR DATEDIFF(NOW(), avs.last_optimized_at) > days_since_optimization
    )
    ORDER BY avs.diff_versions_count DESC, days_since_optimization DESC;
END //

-- 查找冷数据
CREATE PROCEDURE GetColdVersions(
    IN access_days_threshold INT,
    IN archive_days_threshold INT
)
BEGIN
    -- 设置默认值
    SET access_days_threshold = IFNULL(access_days_threshold, 30);
    SET archive_days_threshold = IFNULL(archive_days_threshold, 90);
    
    SELECT 
        av.version_id,
        av.article_id,
        av.version_number,
        av.created_at,
        av.is_archived,
        COALESCE(avs.last_accessed_at, av.created_at) as last_access,
        DATEDIFF(NOW(), av.created_at) as days_old,
        DATEDIFF(NOW(), COALESCE(avs.last_accessed_at, av.created_at)) as days_since_access
    FROM article_versions av
    LEFT JOIN article_version_stats avs ON av.article_id = avs.article_id
    WHERE av.is_archived = FALSE
        AND DATEDIFF(NOW(), av.created_at) > archive_days_threshold
        AND DATEDIFF(NOW(), COALESCE(avs.last_accessed_at, av.created_at)) > access_days_threshold
    ORDER BY days_since_access DESC, days_old DESC;
END //

DELIMITER ;

-- 5. 初始化配置表（可选）
CREATE TABLE IF NOT EXISTS article_version_config (
    config_key VARCHAR(50) PRIMARY KEY,
    config_value VARCHAR(255) NOT NULL,
    description TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 插入默认配置
INSERT INTO article_version_config (config_key, config_value, description) VALUES
('diff_threshold_percent', '0.7', '差异超过此比例时存储完整版本'),
('optimization_diff_count', '10', '差异版本超过此数量时触发优化'),
('cold_data_days', '30', '超过此天数未访问视为冷数据'),
('archive_days', '90', '超过此天数的版本可以归档'),
('compression_algorithm', 'auto', '默认压缩算法: auto, gzip, deflate'),
('max_versions_per_article', '100', '每篇文章最大版本数'),
('enable_auto_optimization', 'true', '是否启用自动优化'),
('enable_auto_archiving', 'true', '是否启用自动归档')
ON DUPLICATE KEY UPDATE 
    config_value = VALUES(config_value),
    updated_at = CURRENT_TIMESTAMP;

-- 6. 创建触发器来自动更新统计信息
DELIMITER //

CREATE TRIGGER tr_article_versions_after_insert
AFTER INSERT ON article_versions
FOR EACH ROW
BEGIN
    INSERT INTO article_version_stats (article_id, total_versions, base_versions_count, diff_versions_count)
    VALUES (NEW.article_id, 1, 
           CASE WHEN NEW.storage_type = 'FULL_CONTENT' THEN 1 ELSE 0 END,
           CASE WHEN NEW.storage_type != 'FULL_CONTENT' THEN 1 ELSE 0 END)
    ON DUPLICATE KEY UPDATE
        total_versions = total_versions + 1,
        base_versions_count = base_versions_count + CASE WHEN NEW.storage_type = 'FULL_CONTENT' THEN 1 ELSE 0 END,
        diff_versions_count = diff_versions_count + CASE WHEN NEW.storage_type != 'FULL_CONTENT' THEN 1 ELSE 0 END,
        updated_at = CURRENT_TIMESTAMP;
END //

CREATE TRIGGER tr_article_versions_after_delete
AFTER DELETE ON article_versions
FOR EACH ROW
BEGIN
    UPDATE article_version_stats 
    SET total_versions = total_versions - 1,
        base_versions_count = base_versions_count - CASE WHEN OLD.storage_type = 'FULL_CONTENT' THEN 1 ELSE 0 END,
        diff_versions_count = diff_versions_count - CASE WHEN OLD.storage_type != 'FULL_CONTENT' THEN 1 ELSE 0 END,
        updated_at = CURRENT_TIMESTAMP
    WHERE article_id = OLD.article_id;
END //

DELIMITER ;

-- 7. 创建一些有用的索引来优化查询性能
-- 复合索引用于版本链查询
CREATE INDEX idx_article_version_chain ON article_versions(article_id, base_version_id, version_number);

-- 复合索引用于归档查询
CREATE INDEX idx_archive_candidates ON article_versions(is_archived, created_at, article_id);

-- 复合索引用于优化查询
CREATE INDEX idx_optimization_candidates ON article_versions(article_id, storage_type, base_version_id);

-- 8. 添加注释
ALTER TABLE article_versions COMMENT = '文章版本表：存储文章的历史版本，支持完整内容和差异存储';
ALTER TABLE article_version_stats COMMENT = '文章版本统计表：跟踪每篇文章的版本统计信息和优化状态';
ALTER TABLE article_version_config COMMENT = '文章版本系统配置表：存储系统配置参数';

-- 完成
SELECT 'Article version system database migration completed successfully!' as message;
