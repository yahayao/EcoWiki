// 权限类型定义（基于实际数据库表结构）
export interface Permission {
  permissionId: number;
  permissionName: string;
  description?: string;
  createdAt?: string;
  updatedAt?: string;
}

// 角色类型定义（基于实际数据库表结构）
export interface Role {
  roleId: number;
  roleName: string;
  description?: string;
  createdAt?: string;
  updatedAt?: string;
}

// 角色权限关联类型定义
export interface RolePermission {
  roleId: number; // 对应role_permissions表中的roleId字段
  permissionId: number;
  createdAt?: string;
}

// 角色权限分配接口（用于前端展示）
export interface RolePermissionAssignment {
  roleId: number;
  roleName: string;
  permissions: {
    permissionId: number;
    permissionName: string;
    description?: string;
    assigned: boolean;
  }[];
}

// 权限分组类型定义（如果将来需要分组功能）
export interface PermissionGroup {
  groupId?: number;
  groupKey: string;
  groupName: string;
  groupDescription?: string;
  icon?: string;
  sortOrder: number;
  isActive: boolean;
  permissions?: Permission[];
}

// API请求响应类型
export interface PermissionGroupResponse {
  code: number;
  message: string;
  data: PermissionGroup[];
}

export interface PermissionResponse {
  code: number;
  message: string;
  data: Permission[];
}

// 权限管理表单类型
export interface PermissionForm {
  permissionName: string;
  description?: string;
}

export interface RoleForm {
  roleName: string;
  description?: string;
}

// 权限分组表单类型（如果将来需要分组功能）
export interface PermissionGroupForm {
  groupKey: string;
  groupName: string;
  groupDescription: string;
  icon: string;
  sortOrder: number;
  isActive: boolean;
}

// 批量操作类型
export interface BatchPermissionOperation {
  action: 'assign' | 'revoke';
  roleId: number;
  permissionIds: number[];
}

// 权限树节点类型（用于UI展示）
export interface PermissionTreeNode {
  id: string;
  label: string;
  type: 'group' | 'permission';
  data: PermissionGroup | Permission;
  children?: PermissionTreeNode[];
  disabled?: boolean;
  checked?: boolean;
}
