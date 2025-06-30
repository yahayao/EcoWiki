/**
 * 表单验证工具模块
 * 
 * 该模块提供了完整的前端表单验证功能，包括字段验证、错误处理和工具函数。
 * 支持常见的验证场景如邮箱、用户名、密码验证，以及表单整体验证。
 * 
 * 功能特性：
 * - 邮箱格式验证
 * - 用户名格式验证  
 * - 密码强度验证和评估
 * - 表单整体验证
 * - 防抖函数工具
 * - TypeScript 类型支持
 * 
 * 验证规则：
 * - 邮箱：标准邮箱格式验证
 * - 用户名：3-20位字母数字下划线
 * - 密码：至少8位包含字母和数字
 * - 确认密码：与原密码一致性验证
 * 
 * 设计原则：
 * - 纯函数设计，无副作用
 * - 统一的错误信息格式
 * - 灵活的组合验证方式
 * - 国际化友好的错误消息
 * 
 * 使用场景：
 * - 注册表单验证
 * - 登录表单验证
 * - 个人信息修改验证
 * - 实时输入验证
 * 
 * @author EcoWiki Team
 * @version 1.0.0
 * @since 2024-01-01
 * 
 * @example
 * ```typescript
 * import { validateEmail, validateRegisterForm } from '@/utils/validation'
 * 
 * // 单个字段验证
 * if (!validateEmail(email)) {
 *   console.log('邮箱格式不正确')
 * }
 * 
 * // 表单整体验证
 * const errors = validateRegisterForm(formData)
 * if (Object.keys(errors).length > 0) {
 *   console.log('表单验证失败', errors)
 * }
 * ```
 */

/**
 * 验证邮箱格式
 * 
 * 使用正则表达式验证邮箱地址的有效性。
 * 支持标准的邮箱格式：username@domain.extension
 * 
 * @param {string} email - 待验证的邮箱地址
 * @returns {boolean} 邮箱格式是否有效
 * 
 * @example
 * ```typescript
 * validateEmail('user@example.com')     // true
 * validateEmail('invalid-email')        // false
 * validateEmail('user@domain')          // false
 * ```
 */
export const validateEmail = (email: string): boolean => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailRegex.test(email)
}

/**
 * 验证用户名格式
 * 
 * 验证用户名是否符合平台规范：
 * - 长度：3-20位字符
 * - 字符集：仅允许字母、数字、下划线
 * - 无特殊字符和空格
 * 
 * @param {string} username - 待验证的用户名
 * @returns {boolean} 用户名格式是否有效
 * 
 * @example
 * ```typescript
 * validateUsername('user123')           // true
 * validateUsername('test_user')         // true  
 * validateUsername('ab')                // false (太短)
 * validateUsername('user@name')         // false (包含特殊字符)
 * ```
 */
export const validateUsername = (username: string): boolean => {
  const usernameRegex = /^[a-zA-Z0-9_]{3,20}$/
  return usernameRegex.test(username)
}

/**
 * 验证密码强度
 * 
 * 验证密码是否满足安全要求：
 * - 最少8位字符
 * - 至少包含一个字母
 * - 至少包含一个数字
 * - 支持特殊字符
 * 
 * @param {string} password - 待验证的密码
 * @returns {boolean} 密码强度是否符合要求
 * 
 * @example
 * ```typescript
 * validatePassword('password123')       // true
 * validatePassword('Pass123!')          // true
 * validatePassword('123456')            // false (无字母)
 * validatePassword('password')          // false (无数字)
 * validatePassword('Pass1')             // false (太短)
 * ```
 */
export const validatePassword = (password: string): boolean => {
  const passwordRegex = /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d@$!%*#?&]{8,}$/
  return passwordRegex.test(password)
}

/**
 * 获取密码强度描述
 * 
 * 根据密码的复杂度返回强度级别描述。
 * 帮助用户了解密码安全性并进行改进。
 * 
 * @param {string} password - 待评估的密码
 * @returns {string} 密码强度描述（'弱'、'中'、'强'）
 * 
 * @example
 * ```typescript
 * getPasswordStrength('123')            // '弱'
 * getPasswordStrength('password123')    // '中'  
 * getPasswordStrength('Pass123!')       // '强'
 * ```
 */
export const getPasswordStrength = (password: string): string => {
  if (password.length < 6) {
    return '弱'
  } else if (password.length < 8 || !/(?=.*[A-Za-z])(?=.*\d)/.test(password)) {
    return '中'
  } else if (/(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&])/.test(password)) {
    return '强'
  } else {
    return '中'
  }
}

/**
 * 表单错误信息接口
 * 定义了表单验证错误的数据结构
 * 
 * @interface FormErrors
 */
export interface FormErrors {
  /** 用户名错误信息 */
  username?: string
  /** 邮箱错误信息 */
  email?: string
  /** 密码错误信息 */
  password?: string
  /** 确认密码错误信息 */
  confirmPassword?: string
}

/**
 * 验证注册表单
 * 
 * 对整个注册表单进行综合验证，返回所有字段的错误信息。
 * 支持批量验证，一次性返回所有验证结果。
 * 
 * @param {Object} data - 注册表单数据
 * @param {string} data.username - 用户名
 * @param {string} data.email - 邮箱地址
 * @param {string} data.password - 密码
 * @param {string} data.confirmPassword - 确认密码
 * @returns {FormErrors} 包含所有验证错误的对象
 * 
 * @example
 * ```typescript
 * const formData = {
 *   username: 'test',
 *   email: 'invalid-email',
 *   password: '123',
 *   confirmPassword: '456'
 * }
 * 
 * const errors = validateRegisterForm(formData)
 * // 返回：
 * // {
 * //   username: '用户名只能包含字母、数字、下划线，长度3-20位',
 * //   email: '邮箱格式不正确',
 * //   password: '密码至少8位，且包含字母和数字',
 * //   confirmPassword: '两次输入的密码不一致'
 * // }
 * ```
 */
export const validateRegisterForm = (data: {
  username: string
  email: string
  password: string
  confirmPassword: string
}): FormErrors => {
  const errors: FormErrors = {}

  // 验证用户名
  if (!data.username) {
    errors.username = '用户名不能为空'
  } else if (!validateUsername(data.username)) {
    errors.username = '用户名只能包含字母、数字、下划线，长度3-20位'
  }

  // 验证邮箱
  if (!data.email) {
    errors.email = '邮箱不能为空'
  } else if (!validateEmail(data.email)) {
    errors.email = '邮箱格式不正确'
  }

  // 验证密码
  if (!data.password) {
    errors.password = '密码不能为空'
  } else if (!validatePassword(data.password)) {
    errors.password = '密码至少8位，且包含字母和数字'
  }

  // 验证确认密码
  if (!data.confirmPassword) {
    errors.confirmPassword = '请确认密码'
  } else if (data.password !== data.confirmPassword) {
    errors.confirmPassword = '两次输入的密码不一致'
  }

  return errors
}

/**
 * 防抖函数工具
 * 
 * 创建一个防抖版本的函数，在指定延迟后执行。
 * 如果在延迟期间再次调用，则重新计时。
 * 常用于优化搜索输入、API调用等场景。
 * 
 * @template T - 函数类型
 * @param {T} func - 需要防抖的函数
 * @param {number} wait - 延迟执行时间（毫秒）
 * @returns {Function} 防抖后的函数
 * 
 * @example
 * ```typescript
 * // 搜索输入防抖
 * const debouncedSearch = debounce((query: string) => {
 *   console.log('搜索:', query)
 * }, 300)
 * 
 * // 用户快速输入时，只有停止输入300ms后才会执行搜索
 * debouncedSearch('a')
 * debouncedSearch('ab')  
 * debouncedSearch('abc') // 只有这次会执行
 * 
 * // API调用防抖
 * const debouncedApiCall = debounce(async (data: any) => {
 *   await api.post('/endpoint', data)
 * }, 500)
 * ```
 */
export const debounce = <T extends (...args: any[]) => any>(
  func: T,
  wait: number
): ((...args: Parameters<T>) => void) => {
  let timeout: number | null = null
  
  return (...args: Parameters<T>) => {
    if (timeout) {
      clearTimeout(timeout)
    }
    timeout = setTimeout(() => func(...args), wait)
  }
}
