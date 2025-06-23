// 表单验证工具函数

// 表单错误类型
export interface FormErrors {
  [key: string]: string
}

// 验证邮箱格式
export const isValidEmail = (email: string): boolean => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailRegex.test(email)
}

// 验证用户名格式
export const isValidUsername = (username: string): boolean => {
  const usernameRegex = /^[a-zA-Z0-9_]{3,20}$/
  return usernameRegex.test(username)
}

// 验证密码强度
export const isStrongPassword = (password: string): boolean => {
  if (password.length < 8) return false
  const hasLetter = /[a-zA-Z]/.test(password)
  const hasNumber = /\d/.test(password)
  return hasLetter && hasNumber
}

// 注册表单验证
export const validateRegisterForm = (formData: any): FormErrors => {
  const errors: FormErrors = {}

  if (!formData.username) {
    errors.username = '用户名不能为空'
  } else if (!isValidUsername(formData.username)) {
    errors.username = '用户名应为3-20位字母、数字或下划线'
  }

  if (!formData.email) {
    errors.email = '邮箱不能为空'
  } else if (!isValidEmail(formData.email)) {
    errors.email = '请输入有效的邮箱地址'
  }

  if (!formData.password) {
    errors.password = '密码不能为空'
  } else if (!isStrongPassword(formData.password)) {
    errors.password = '密码至少8位，包含字母和数字'
  }

  if (!formData.confirmPassword) {
    errors.confirmPassword = '请确认密码'
  } else if (formData.password !== formData.confirmPassword) {
    errors.confirmPassword = '密码不一致'
  }

  return errors
}

// 防抖函数
export const debounce = <T extends (...args: any[]) => any>(
  func: T,
  wait: number
): ((...args: Parameters<T>) => void) => {
  let timeout: ReturnType<typeof setTimeout>
  return (...args: Parameters<T>) => {
    clearTimeout(timeout)
    timeout = setTimeout(() => func(...args), wait)
  }
}
