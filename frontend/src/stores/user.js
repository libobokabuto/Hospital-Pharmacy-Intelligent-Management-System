import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authAPI } from '@/api'
import { ElMessage } from 'element-plus'

export const useUserStore = defineStore('user', () => {
  // 状态
  const user = ref(null)
  const token = ref(localStorage.getItem('token') || '')
  const isLoggedIn = computed(() => !!token.value)

  // 用户信息
  const userInfo = computed(() => user.value)

  // 登录
  const login = async (username, password) => {
    try {
      const response = await authAPI.login({ username, password })
      
      if (response.success && response.data) {
        token.value = response.data.token
        user.value = {
          id: response.data.userId,
          username: response.data.username,
          role: response.data.role,
          realName: response.data.realName,
        }
        localStorage.setItem('token', token.value)
        localStorage.setItem('userRole', response.data.role)
        localStorage.setItem('userId', response.data.userId)
        localStorage.setItem('username', response.data.username)
        return { success: true }
      } else {
        return { success: false, message: response.message || '登录失败' }
      }
    } catch (error) {
      console.error('Login error:', error)
      return { success: false, message: error.message || '网络错误，请重试' }
    }
  }

  // 获取当前用户信息
  const fetchCurrentUser = async () => {
    try {
      const response = await authAPI.getCurrentUser()
      if (response.success && response.data) {
        user.value = response.data
        return { success: true, data: response.data }
      }
      return { success: false }
    } catch (error) {
      console.error('Fetch user error:', error)
      return { success: false }
    }
  }

  // 登出
  const logout = async () => {
    try {
      await authAPI.logout()
    } catch (error) {
      console.error('Logout error:', error)
    } finally {
      token.value = ''
      user.value = null
      localStorage.removeItem('token')
      localStorage.removeItem('userRole')
      localStorage.removeItem('userId')
      localStorage.removeItem('username')
    }
  }

  // 检查权限
  const hasRole = (role) => {
    return user.value && user.value.role === role
  }

  // 检查是否有任意一个角色
  const hasAnyRole = (roles) => {
    return user.value && roles.includes(user.value.role)
  }

  // 初始化用户信息（从localStorage恢复）
  const initUser = () => {
    const storedRole = localStorage.getItem('userRole')
    const storedUsername = localStorage.getItem('username')
    const storedUserId = localStorage.getItem('userId')
    
    if (storedRole && storedUsername) {
      user.value = {
        id: storedUserId,
        username: storedUsername,
        role: storedRole,
      }
    }
  }

  return {
    user,
    token,
    isLoggedIn,
    userInfo,
    login,
    logout,
    fetchCurrentUser,
    hasRole,
    hasAnyRole,
    initUser,
  }
})
