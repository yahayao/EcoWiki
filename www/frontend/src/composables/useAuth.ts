import { useAuthStore } from '../stores/authStore'
import type { UserResponse } from '../api/user'
import { USER_GROUPS } from '../api/user'

export function useAuth() {
  const store = useAuthStore()
  return {
    user: store.user,
    token: store.token,
    isAuthenticated: store.isAuthenticated,
    isLoggedIn: store.isLoggedIn,
    userAvatar: store.userAvatar,
    userDisplayName: store.userDisplayName,
    isAdmin: store.isAdmin,
    isSuperAdmin: store.isSuperAdmin,
    setUser: store.setUser,
    clearUser: store.clearUser,
    hasPermission: store.hasPermission,
    refreshUserInfo: store.refreshUserInfo,
    USER_GROUPS
  }
}

export type { UserResponse }
export { USER_GROUPS }
