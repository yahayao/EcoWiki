# 权限层次结构数据库升级指南

## 📋 升级前准备

### 1. 数据库备份
```sql
-- 备份整个permission表
CREATE TABLE permission_backup AS SELECT * FROM permission;

-- 备份role_permission表（如果存在）
CREATE TABLE role_permission_backup AS SELECT * FROM role_permission;
```

### 2. 确认当前权限数据
```sql
-- 查看当前权限列表
SELECT permission_id, permission_name, description FROM permission ORDER BY permission_id;
```

## 🚀 升级方案

我为您准备了两个升级方案：

### 方案一：完整升级（推荐）
文件：`UPGRADE_PERMISSION_HIERARCHY.sql`
- ✅ 添加parent_permission_id字段
- ✅ 创建完整的权限分类体系
- ✅ 为现有权限分配父权限
- ✅ 添加常用的权限项目
- ✅ 数据完整性验证

### 方案二：简化升级（保守）
文件：`SIMPLE_PERMISSION_UPGRADE.sql`
- ✅ 仅添加必要的字段和约束
- ✅ 创建基本的分类
- ✅ 最小化变更

## 📝 执行步骤

### 1. 选择升级方案
根据您的需求选择：
- **初次升级或希望完整功能**：使用 `UPGRADE_PERMISSION_HIERARCHY.sql`
- **谨慎升级或生产环境**：使用 `SIMPLE_PERMISSION_UPGRADE.sql`

### 2. 在MySQL中执行脚本
```bash
# 方式1：通过MySQL命令行
mysql -h sh-cynosdbmysql-grp-2yvr1y3c.sql.tencentcdb.com -P 26809 -u root -p Ecosql < UPGRADE_PERMISSION_HIERARCHY.sql

# 方式2：通过MySQL Workbench或其他数据库管理工具
# 打开SQL文件并执行
```

### 3. 验证升级结果
```sql
-- 检查表结构
DESCRIBE permission;

-- 查看权限层次结构
SELECT 
    p1.permission_id,
    p1.permission_name,
    p1.description,
    p1.parent_permission_id,
    p2.permission_name AS parent_name
FROM permission p1
LEFT JOIN permission p2 ON p1.parent_permission_id = p2.permission_id
ORDER BY p1.parent_permission_id ASC, p1.permission_id ASC;

-- 检查外键约束
SELECT 
    CONSTRAINT_NAME, 
    COLUMN_NAME, 
    REFERENCED_TABLE_NAME, 
    REFERENCED_COLUMN_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE 
WHERE TABLE_NAME = 'permission' 
    AND TABLE_SCHEMA = 'Ecosql'
    AND REFERENCED_TABLE_NAME IS NOT NULL;
```

## 🔧 后端代码同步

升级数据库后，需要确保后端代码支持新的字段：

### 1. 检查Permission实体
```java
// 确保Permission.java包含parent_permission_id字段
@Entity
@Table(name = "permission")
public class Permission {
    // ... 其他字段 ...
    
    @Column(name = "parent_permission_id")
    private Integer parentPermissionId;
    
    @OneToMany(mappedBy = "parentPermissionId", fetch = FetchType.LAZY)
    private List<Permission> subPermissions;
    
    // getter和setter方法
}
```

### 2. 更新PermissionDto
```java
// 确保PermissionDto包含parentPermissionId和subPermissions
public class PermissionDto {
    private Integer parentPermissionId;
    private List<PermissionDto> subPermissions;
    // ... 其他字段和方法 ...
}
```

### 3. 重启Spring Boot应用
```bash
# 在backend目录下
mvn spring-boot:run
```

## 🎯 前端功能验证

升级完成后，前端权限管理页面应该能够：

1. ✅ 显示权限树形结构
2. ✅ 添加新权限时选择父权限
3. ✅ 编辑权限的父子关系
4. ✅ 展开/收起权限子项
5. ✅ 删除权限及其子权限

## 🚨 故障恢复

如果升级过程中出现问题：

```sql
-- 恢复备份数据
DROP TABLE permission;
RENAME TABLE permission_backup TO permission;

-- 如果需要恢复role_permission表
DROP TABLE role_permission;
RENAME TABLE role_permission_backup TO role_permission;
```

## 📞 技术支持

如果在升级过程中遇到问题：
1. 检查MySQL错误日志
2. 确认数据库连接权限
3. 验证SQL语法兼容性
4. 联系开发团队获取支持

---

**重要提示：** 
- 在生产环境执行前，请先在测试环境验证
- 确保有完整的数据备份
- 建议在业务低峰期进行升级
