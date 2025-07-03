/**
 * 用户服务模块
 * 
 * 提供与用户相关的业务逻辑和API调用服务。
 * 这是一个服务层模块，位于API层之上，处理复杂的业务逻辑和数据处理。
 * 
 * 主要功能：
 * - 系统统计信息获取
 * - 用户数据的业务逻辑处理
 * - API响应数据的格式化和验证
 * - 错误处理和异常管理
 * 
 * 架构设计：
 * - 服务层：处理业务逻辑，位于组件和API之间
 * - 数据验证：确保API返回数据的有效性
 * - 错误处理：统一的错误处理机制
 * - 可扩展性：易于添加新的用户相关服务
 * 
 * 使用场景：
 * - 管理后台统计面板
 * - 用户信息展示
 * - 数据报表生成
 * - 业务逻辑复用
 * 
 * @author EcoWiki Team
 * @version 1.0.0
 * @since 2024-01-01
 * 
 * @example
 * ```typescript
 * import { userService } from '@/services/userService'
 * 
 * // 获取系统统计
 * try {
 *   const stats = await userService.getStats()
 *   console.log('用户统计:', stats)
 * } catch (error) {
 *   console.error('获取统计失败:', error)
 * }
 * ```
 */

import { api } from '../api'

/**
 * 用户服务对象
 * 
 * 包含所有用户相关的业务逻辑方法。
 * 采用对象字面量方式组织，便于扩展和维护。
 */
export const userService = {
  /**
   * 获取系统统计信息
   * 
   * 从后端API获取系统的统计数据，包括用户数量、文章数量等信息。
   * 主要用于管理后台的统计面板显示。
   * 
   * 功能特点：
   * - 统一的数据格式处理
   * - 完善的错误处理机制
   * - 状态码验证
   * - 异常信息提取
   * 
   * @returns {Promise<any>} 系统统计数据对象
   * @throws {Error} 当API调用失败或返回错误状态时抛出异常
   * 
   * @example
   * ```typescript
   * try {
   *   const stats = await userService.getStats()
   *   console.log('总用户数:', stats.data.userCount)
   *   console.log('总文章数:', stats.data.articleCount)
   * } catch (error) {
   *   console.error('获取统计失败:', error.message)
   * }
   * ```
   */
  async getStats() {
    const response = await api.get('/admin/stats')
    
    // 验证响应状态码
    if (response.data.code !== 200) {
      throw new Error(response.data.message || '获取系统统计失败')
    }
    
    return response.data
  },
}


 
