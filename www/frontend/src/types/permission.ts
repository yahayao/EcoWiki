/**
 * 权限管理系统类型定义文件
 * 
 * 这个文件定义了EcoWiki权限管理系统中使用的所有TypeScript类型接口。
 * 类型定义严格对应后端数据库表结构，确保前后端数据一致性。
 * 
 * 主要类型分类：
 * 1. 核心实体类型 - Permission, Role, RolePermission
 * 2. 业务组合类型 - RolePermissionAssignment, PermissionGroup
 * 3. 表单数据类型 - RoleForm, PermissionForm, PermissionGroupForm
 * 4. API响应类型 - 统一的接口响应格式
 * 
 * 设计原则：
 * - 与数据库表结构一致
 * - 支持可选字段和扩展
 * - 类型安全和可维护性
 * 
 * @author EcoWiki开发团队
 * @version 2.0.0 - 完善权限系统类型定义
 * @since 2024-04-01
 * @lastModified 2025-07-03
 */

// === 核心实体类型定义 ===

/**
 * 权限实体类型
 * 对应数据库中的permissions表结构
 * 
 * 表结构映射：
 * - permissionId -> permission_id (主键)
 * - permissionName -> permission_name (权限标识)
 * - description -> description (权限描述)
 * - createdAt -> created_at (创建时间)
 * - updatedAt -> updated_at (更新时间)
 */
export interface Permission {
  /** 权限唯一标识符（主键） */
  permissionId: number;
  /** 权限名称/标识符，如 'system:read', 'user:write' */
  permissionName: string;
  /** 权限功能描述，用于界面显示 */
  description?: string;
  /** 权限创建时间 */
  createdAt?: string;
  /** 权限最后更新时间 */
  updatedAt?: string;
}

/**
 * 角色实体类型
 * 对应数据库中的roles表结构
 * 
 * 表结构映射：
 * - roleId -> role_id (主键)
 * - roleName -> role_name (角色名称)
 * - description -> description (角色描述)
 * - createdAt -> created_at (创建时间)
 * - updatedAt -> updated_at (更新时间)
 */
export interface Role {
  /** 角色唯一标识符（主键） */
  roleId: number;
  /** 角色名称，如 'admin', 'user', 'moderator' */
  roleName: string;
  /** 角色功能描述，用于界面显示 */
  description?: string;
  /** 角色创建时间 */
  createdAt?: string;
  /** 角色最后更新时间 */
  updatedAt?: string;
}

/**
 * 角色权限关联实体类型
 * 对应数据库中的role_permissions表结构（多对多关联表）
 * 
 * 表结构映射：
 * - roleId -> role_id (外键，关联roles表)
 * - permissionId -> permission_id (外键，关联permissions表)
 * - createdAt -> created_at (关联创建时间)
 */
export interface RolePermission {
  /** 角色ID（外键） */
  roleId: number;
  /** 权限ID（外键） */
  permissionId: number;
  /** 关联关系创建时间 */
  createdAt?: string;
}

// === 业务组合类型定义 ===

/**
 * 角色权限分配展示类型
 * 用于前端权限分配页面的数据展示和交互
 * 
 * 这是一个组合类型，将角色信息与其权限列表整合在一起，
 * 方便权限分配界面进行状态管理和用户交互。
 */
export interface RolePermissionAssignment {
  /** 角色ID */
  roleId: number;
  /** 角色名称 */
  roleName: string;
  /** 角色拥有的权限列表，包含分配状态 */
  permissions: {
    /** 权限ID */
    permissionId: number;
    /** 权限名称 */
    permissionName: string;
    /** 权限描述 */
    description?: string;
    /** 是否已分配给当前角色 */
    assigned: boolean;
  }[];
}

/**
 * 权限分组类型定义
 * 用于将权限按功能模块分组显示，提升用户体验
 * 
 * 分组功能说明：
 * - 支持图标和排序，美化界面展示
 * - 可以启用/禁用整个分组
 * - 分组内权限可以批量操作
 */
export interface PermissionGroup {
  /** 分组唯一标识符（可选，用于数据库存储） */
  groupId?: number;
  /** 分组标识键，如 'system', 'user', 'content' */
  groupKey: string;
  /** 分组显示名称，如 '系统管理', '用户管理' */
  groupName: string;
  /** 分组功能描述 */
  groupDescription?: string;
  /** 分组图标（可选） */
  icon?: string;
  /** 分组排序权重 */
  sortOrder: number;
  /** 分组是否启用 */
  isActive: boolean;
  /** 分组包含的权限列表（可选，用于关联查询） */
  permissions?: Permission[];
}

// === 表单数据类型定义 ===

/**
 * 权限表单数据类型
 * 用于创建或编辑权限时的表单数据绑定
 */
export interface PermissionForm {
  /** 权限标识符，如 'system:read' */
  permissionName: string;
  /** 权限描述，如 '系统读取权限' */
  description?: string;
}

/**
 * 角色表单数据类型
 * 用于创建或编辑角色时的表单数据绑定
 */
export interface RoleForm {
  /** 角色名称，如 'editor' */
  roleName: string;
  /** 角色描述，如 '内容编辑者' */
  description?: string;
}

/**
 * 权限分组表单数据类型
 * 用于创建或编辑权限分组时的表单数据绑定
 */
export interface PermissionGroupForm {
  /** 分组标识键，必须唯一 */
  groupKey: string;
  /** 分组显示名称 */
  groupName: string;
  /** 分组功能描述 */
  groupDescription: string;
  /** 分组图标标识 */
  icon: string;
  /** 排序权重，数值越小越靠前 */
  sortOrder: number;
  /** 是否启用此分组 */
  isActive: boolean;
}

// === API响应类型定义 ===

/**
 * 权限分组API响应类型
 */
export interface PermissionGroupResponse {
  code: number;
  message: string;
  data: PermissionGroup[];
}

/**
 * 权限列表API响应类型
 */
export interface PermissionResponse {
  code: number;
  message: string;
  data: Permission[];
}

// === 业务操作类型定义 ===

/**
 * 批量权限操作类型
 * 用于批量分配或撤销角色权限
 */
export interface BatchPermissionOperation {
  /** 操作类型：分配或撤销 */
  action: 'assign' | 'revoke';
  /** 目标角色ID */
  roleId: number;
  /** 权限ID列表 */
  permissionIds: number[];
}

/**
 * 权限树节点类型
 * 用于权限管理界面的树形结构展示
 */
export interface PermissionTreeNode {
  /** 节点唯一标识 */
  id: string;
  /** 节点显示标签 */
  label: string;
  /** 节点类型：分组或权限 */
  type: 'group' | 'permission';
  /** 节点关联的数据对象 */
  data: PermissionGroup | Permission;
  /** 子节点列表（递归结构） */
  children?: PermissionTreeNode[];
  /** 是否禁用此节点 */
  disabled?: boolean;
  /** 是否选中此节点 */
  checked?: boolean;
}
