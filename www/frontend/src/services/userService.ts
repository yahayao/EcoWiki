import { api } from '../api'

export const userService = {
  // 获取系统统计
  async getStats() {
    const response = await api.get('/admin/stats')
    if (response.data.code !== 200) {
      throw new Error(response.data.message || '获取系统统计失败')
    }
    return response.data
  },
}


 
