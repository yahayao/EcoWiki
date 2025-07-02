# Wiki.js风格权限分组系统实施指南

## 系统概述

本次重构将EcoWiki的权限管理系统从简单的角色-权限模式升级为Wiki.js风格的权限分组-细分权限模式，提供更灵活、更直观的权限管理体验。

## 新系统特性

### 1. 权限分组结构
- **内容管理** (`content`): 页面、文章的创建、编辑、删除等权限
- **用户管理** (`users`): 用户账户、角色、权限的管理
- **系统管理** (`admin`): 系统设置、配置、维护等管理权限
- **导航管理** (`navigation`): 菜单、导航结构的管理
- **资源管理** (`assets`): 文件、图片、媒体资源的管理

### 2. 细分权限模式
每个分组下包含多个细分权限，采用 `动作:资源` 的命名规范：
- `read:pages` - 查看页面
- `write:pages` - 创建页面
- `manage:pages` - 编辑页面
- `delete:pages` - 删除页面
- `read:users` - 查看用户
- `write:users` - 创建用户
- `manage:users` - 编辑用户
- `delete:users` - 删除用户
- 等等...

### 3. UI界面特性
- Wiki.js风格的分组展示
- 支持分组的展开/收起
- 批量权限分配
- 实时权限统计
- 系统内置权限保护

## 数据库变更

### 新增表结构

#### permission_group 表
```sql
CREATE TABLE permission_group (
    group_id INT PRIMARY KEY AUTO_INCREMENT,
    group_key VARCHAR(50) NOT NULL UNIQUE,
    group_name VARCHAR(100) NOT NULL,
    group_description TEXT,
    icon VARCHAR(50),
    sort_order INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

#### permission 表扩展
```sql
ALTER TABLE permission 
ADD COLUMN group_id INT,
ADD COLUMN permission_key VARCHAR(100),
ADD COLUMN is_system BOOLEAN DEFAULT FALSE,
ADD COLUMN sort_order INT DEFAULT 0,
ADD FOREIGN KEY (group_id) REFERENCES permission_group(group_id) ON DELETE SET NULL;
```

## 后端实现

### 1. 实体类
- `PermissionGroup.java` - 权限分组实体
- `Permission.java` - 扩展权限实体，支持分组关联
- `PermissionGroupDto.java` - 权限分组DTO
- `PermissionDto.java` - 更新权限DTO

### 2. 数据访问层
- `PermissionGroupRepository.java` - 权限分组数据访问
- `PermissionRepository.java` - 扩展权限数据访问

### 3. 服务层
- `PermissionGroupService.java` - 权限分组业务逻辑

### 4. 控制器层
- `PermissionGroupController.java` - 权限分组API接口

## 前端实现

### 1. 类型定义
- `/types/permission.ts` - 权限相关类型定义

### 2. API服务
- `/api/user.ts` - 扩展权限分组API方法

### 3. 组件实现
- `PermissionGroupsManagement.vue` - 权限分组管理界面
- `RolePermissionAssignment.vue` - 角色权限分配界面

### 4. 路由配置
- 添加权限分组管理路由

## 实施步骤

### 阶段1: 数据库迁移
1. 执行数据库迁移脚本 `V005__create_permission_groups_system.sql`
2. 验证数据迁移结果
3. 创建默认权限分组和权限

### 阶段2: 后端开发
1. 创建实体类和DTO
2. 实现Repository和Service层
3. 开发Controller API接口
4. 编写单元测试

### 阶段3: 前端开发
1. 定义TypeScript类型
2. 实现API服务方法
3. 开发权限分组管理组件
4. 开发角色权限分配组件
5. 更新路由和导航

### 阶段4: 集成测试
1. 端到端功能测试
2. 权限分配测试
3. 数据一致性验证
4. 性能测试

### 阶段5: 部署上线
1. 生产环境数据备份
2. 执行数据库迁移
3. 部署新版本应用
4. 验证功能正常

## API接口文档

### 权限分组管理

#### 获取所有权限分组
```
GET /api/admin/permission-groups
Response: PermissionGroup[]
```

#### 创建权限分组
```
POST /api/admin/permission-groups
Body: PermissionGroupForm
Response: PermissionGroup
```

#### 更新权限分组
```
PUT /api/admin/permission-groups/{groupId}
Body: PermissionGroupForm
Response: PermissionGroup
```

#### 删除权限分组
```
DELETE /api/admin/permission-groups/{groupId}
```

#### 为分组添加权限
```
POST /api/admin/permission-groups/{groupId}/permissions
Body: PermissionForm
Response: Permission
```

### 角色权限分配

#### 获取角色权限
```
GET /api/admin/roles/{roleId}/permissions
Response: Permission[]
```

#### 更新角色权限
```
PUT /api/admin/roles/{roleId}/permissions
Body: { permissionIds: number[] }
```

## 配置说明

### 默认权限分组配置
系统初始化时会创建以下默认分组：

1. **内容管理** (content)
   - 图标: content-copy
   - 权限: read:pages, write:pages, manage:pages, delete:pages, read:history

2. **用户管理** (users)
   - 图标: account-group
   - 权限: read:users, write:users, manage:users, delete:users, manage:roles, assign:permissions

3. **系统管理** (admin)
   - 图标: cog
   - 权限: manage:system, read:logs, manage:database, manage:themes

4. **导航管理** (navigation)
   - 图标: menu
   - 权限: read:navigation, manage:navigation

5. **资源管理** (assets)
   - 图标: folder-image
   - 权限: read:assets, write:assets, manage:assets

### 权限命名规范
- **读取权限**: `read:资源名`
- **写入权限**: `write:资源名`
- **管理权限**: `manage:资源名`
- **删除权限**: `delete:资源名`

## 迁移注意事项

### 1. 数据完整性
- 迁移过程中保持现有权限数据不丢失
- 自动将现有权限分配到合适的分组
- 保持角色权限关联关系

### 2. 兼容性考虑
- API接口保持向后兼容
- 权限检查逻辑平滑过渡
- 支持新旧权限模式并存

### 3. 性能优化
- 使用JOIN FETCH避免N+1查询问题
- 权限缓存策略
- 批量操作优化

### 4. 安全考虑
- 系统内置权限保护
- 权限分配审计日志
- 最小权限原则

## 维护和扩展

### 添加新权限分组
1. 在数据库中插入分组记录
2. 创建对应的权限记录
3. 更新前端图标映射
4. 测试权限分配功能

### 添加新权限
1. 在对应分组下创建权限记录
2. 更新权限检查逻辑
3. 测试角色分配功能

### 性能监控
- 权限查询响应时间
- 权限分配操作性能
- 数据库查询优化

## 支持和文档

- 技术文档: [PROJECT_STRUCTURE.md](PROJECT_STRUCTURE.md)
- API文档: [API_TEST_GUIDE.md](API_TEST_GUIDE.md)
- 数据库文档: [DATABASE_UPGRADE_GUIDE.md](DATABASE_UPGRADE_GUIDE.md)

## 更新日志

### v2.0.0 (2025-07-02)
- 实现Wiki.js风格权限分组系统
- 支持细分权限管理
- 新增权限分组管理界面
- 优化角色权限分配体验
- 提升权限管理的可视化程度
