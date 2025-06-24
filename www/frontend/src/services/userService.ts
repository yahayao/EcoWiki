import { api } from '../api'

export const userService = {
  // 更新系统设置
  async updateSystemSettings(settings: Record<string, any>) {
    // 假设后端有 /admin/settings 接口
    const response = await api.post('/admin/settings', settings)
    if (response.data.code !== 200) {
      throw new Error(response.data.message || '更新系统设置失败')
    }
    return response.data
  }
}