-- 手动执行脚本：为现有权限表添加层次结构支持
-- 请在数据库管理工具中执行以下SQL语句

-- 第一步：添加父权限字段
ALTER TABLE permission ADD COLUMN parent_permission_id INT NULL;

-- 第二步：添加外键约束
ALTER TABLE permission 
ADD CONSTRAINT fk_permission_parent 
FOREIGN KEY (parent_permission_id) REFERENCES permission(permission_id)
ON DELETE SET NULL;

-- 第三步：添加索引
CREATE INDEX idx_permission_parent ON permission(parent_permission_id);

-- 第四步：创建父权限分类
INSERT INTO permission (permission_name, description, parent_permission_id, created_at, updated_at) VALUES
('article_management', '文章管理', NULL, NOW(), NOW()),
('comment_management', '评论管理', NULL, NOW(), NOW());

-- 第五步：为现有权限分配父权限（根据实际权限名称调整）
-- 将文章相关权限（ID 1,2,3）归类到文章管理下
UPDATE permission 
SET parent_permission_id = (SELECT permission_id FROM permission WHERE permission_name = 'article_management' LIMIT 1)
WHERE permission_name IN ('查看文章', '编辑文章', '删除文章');

-- 将评论相关权限（ID 4,5,6）归类到评论管理下
UPDATE permission 
SET parent_permission_id = (SELECT permission_id FROM permission WHERE permission_name = 'comment_management' LIMIT 1)
WHERE permission_name IN ('发表评论', '查看评论', '删除评论');

-- 查询验证结果
SELECT 
    p.permission_id,
    p.permission_name,
    p.description,
    p.parent_permission_id,
    parent.permission_name as parent_name,
    p.created_at,
    p.updated_at
FROM permission p
LEFT JOIN permission parent ON p.parent_permission_id = parent.permission_id
ORDER BY p.parent_permission_id, p.permission_id;
