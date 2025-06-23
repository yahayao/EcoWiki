// 表单验证工具函数

// 验证邮箱格式
export const validateEmail = (email: string): boolean => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailRegex.test(email)
}

// 验证用户名格式（3-20位，只能包含字母、数字、下划线）
export const validateUsername = (username: string): boolean => {
  const usernameRegex = /^[a-zA-Z0-9_]{3,20}$/
  return usernameRegex.test(username)
}

// 验证密码强度（至少8位，包含字母和数字）
export const validatePassword = (password: string): boolean => {
  const passwordRegex = /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d@$!%*#?&]{8,}$/
  return passwordRegex.test(password)
}

// 获取密码强度描述
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

// 表单错误类型
export interface FormErrors {
  username?: string
  email?: string
  password?: string
  confirmPassword?: string
}

// 验证注册表单
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

// 防抖函数
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
