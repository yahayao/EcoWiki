// 权限分组类型定义
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

// 权限类型定义
export interface Permission {
  permissionId?: number;
  permissionName: string;
  description?: string;
  permissionKey: string;
  groupId?: number;
  groupName?: string;
  isSystem?: boolean;
  sortOrder: number;
}

// 角色权限分配接口
export interface RolePermissionAssignment {
  roleId: number;
  permissionGroups: {
    groupId: number;
    groupKey: string;
    groupName: string;
    permissions: {
      permissionId: number;
      permissionKey: string;
      permissionName: string;
      assigned: boolean;
    }[];
  }[];
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
export interface PermissionGroupForm {
  groupKey: string;
  groupName: string;
  groupDescription: string;
  icon: string;
  sortOrder: number;
  isActive: boolean;
}

export interface PermissionForm {
  permissionName: string;
  description: string;
  permissionKey: string;
  groupId: number;
  sortOrder: number;
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
