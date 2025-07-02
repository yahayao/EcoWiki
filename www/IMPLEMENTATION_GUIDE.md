# 权限层次结构实施指南

## 当前状态
根据您提供的数据库截图，permission表当前有6条记录：
1. 查看文章
2. 编辑文章  
3. 删除文章
4. 发表评论
5. 查看评论
6. 删除评论

## 实施步骤

### 第一步：数据库结构升级

请在您的数据库管理工具中执行以下SQL语句：

```sql
-- 1. 添加父权限字段
ALTER TABLE permission ADD COLUMN parent_permission_id INT NULL;

-- 2. 添加外键约束
ALTER TABLE permission 
ADD CONSTRAINT fk_permission_parent 
FOREIGN KEY (parent_permission_id) REFERENCES permission(permission_id)
ON DELETE SET NULL;

-- 3. 添加索引
CREATE INDEX idx_permission_parent ON permission(parent_permission_id);
```

### 第二步：创建权限层次结构

```sql
-- 创建父权限分类
INSERT INTO permission (permission_name, description, parent_permission_id, created_at, updated_at) VALUES
('article_management', '文章管理', NULL, NOW(), NOW()),
('comment_management', '评论管理', NULL, NOW(), NOW());

-- 将现有权限分配到合适的父权限下
-- 文章相关权限
UPDATE permission 
SET parent_permission_id = (SELECT permission_id FROM permission WHERE permission_name = 'article_management' LIMIT 1)
WHERE permission_name IN ('查看文章', '编辑文章', '删除文章');

-- 评论相关权限
UPDATE permission 
SET parent_permission_id = (SELECT permission_id FROM permission WHERE permission_name = 'comment_management' LIMIT 1)
WHERE permission_name IN ('发表评论', '查看评论', '删除评论');
```

### 第三步：验证结果

```sql
-- 查询权限层次结构
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
```

预期结果应该类似于：
```
ID | 权限名称      | 描述        | 父权限ID | 父权限名称
7  | article_management | 文章管理   | NULL    | NULL
8  | comment_management | 评论管理   | NULL    | NULL  
1  | 查看文章      | ...        | 7       | article_management
2  | 编辑文章      | ...        | 7       | article_management
3  | 删除文章      | ...        | 7       | article_management
4  | 发表评论      | ...        | 8       | comment_management
5  | 查看评论      | ...        | 8       | comment_management
6  | 删除评论      | ...        | 8       | comment_management
```

### 第四步：启动后端服务

数据库升级完成后，您可以启动后端服务：

```bash
cd "c:\Users\Z9495\Desktop\EcoWiki_project\EcoWiki-3\www\backend"
mvn spring-boot:run
```

### 第五步：测试前端权限管理

启动前端服务：
```bash
cd "c:\Users\Z9495\Desktop\EcoWiki_project\EcoWiki-3\www\frontend"
npm run dev
```

然后访问权限管理页面，您应该能看到：
- 层次化的权限列表显示
- 父权限和子权限的视觉区分
- 展开/收起子权限功能
- 添加子权限的快捷按钮

## 功能特性

### 权限管理界面
- ✅ 树形结构显示权限
- ✅ 父权限选择下拉框
- ✅ 子权限缩进显示
- ✅ 快速添加子权限按钮
- ✅ 展开/收起子权限列表

### 数据安全
- ✅ 外键约束确保数据一致性
- ✅ 防止循环引用
- ✅ 级联删除处理

### 用户体验
- ✅ 直观的层次关系显示
- ✅ 便捷的权限编辑操作
- ✅ 实时的权限树状态管理

## 注意事项

1. **备份数据**：执行数据库修改前请备份现有数据
2. **权限分配**：现有的角色权限分配会保持不变
3. **API兼容**：前端API已经更新支持父权限参数
4. **性能优化**：已添加必要的数据库索引

执行完这些步骤后，您的权限管理系统将支持完整的层次结构功能！
