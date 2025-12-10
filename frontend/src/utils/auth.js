/**
 * 权限验证工具函数
 */

// 检查用户是否已登录
export const isAuthenticated = () => {
  return !!localStorage.getItem('token')
}

// 获取用户角色
export const getUserRole = () => {
  return localStorage.getItem('userRole')
}

// 检查用户是否有指定角色
export const hasRole = (role) => {
  const userRole = getUserRole()
  return userRole === role
}

// 检查用户是否有多个角色中的任意一个
export const hasAnyRole = (roles) => {
  const userRole = getUserRole()
  return roles.includes(userRole)
}

// 检查用户是否是管理员
export const isAdmin = () => {
  return hasRole('admin')
}

// 检查用户是否是医生
export const isDoctor = () => {
  return hasRole('doctor')
}

// 检查用户是否是护士
export const isNurse = () => {
  return hasRole('nurse')
}

// 检查用户是否是药剂师
export const isPharmacist = () => {
  return hasRole('pharmacist')
}

// 清除认证信息
export const clearAuth = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('userRole')
}
