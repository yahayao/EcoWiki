-- 用户文章收藏表
CREATE TABLE IF NOT EXISTS user_article_favorites (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    article_id BIGINT NOT NULL COMMENT '文章ID',
    favorite_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    UNIQUE KEY uk_user_article_favorite (user_id, article_id) COMMENT '用户文章收藏唯一约束',
    INDEX idx_user_id (user_id) COMMENT '用户ID索引',
    INDEX idx_article_id (article_id) COMMENT '文章ID索引',
    INDEX idx_favorite_time (favorite_time) COMMENT '收藏时间索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户文章收藏表';

-- 用户文章点赞表
CREATE TABLE IF NOT EXISTS user_article_likes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    article_id BIGINT NOT NULL COMMENT '文章ID',
    like_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
    UNIQUE KEY uk_user_article_like (user_id, article_id) COMMENT '用户文章点赞唯一约束',
    INDEX idx_user_id (user_id) COMMENT '用户ID索引',
    INDEX idx_article_id (article_id) COMMENT '文章ID索引',
    INDEX idx_like_time (like_time) COMMENT '点赞时间索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户文章点赞表';

-- 插入示例数据（可选）
-- INSERT INTO user_article_favorites (user_id, article_id) VALUES 
-- (1, 1), (1, 2), (2, 1);

-- INSERT INTO user_article_likes (user_id, article_id) VALUES 
-- (1, 1), (1, 2), (2, 1), (2, 2);
