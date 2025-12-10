import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

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
      // 这里调用登录API
      const response = await fetch('/api/auth/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ username, password }),
      })

      const data = await response.json()

      if (data.success) {
        token.value = data.data.token
        user.value = data.data.user
        localStorage.setItem('token', token.value)
        localStorage.setItem('userRole', data.data.user.role)
        return { success: true }
      } else {
        return { success: false, message: data.message }
      }
    } catch (error) {
      console.error('Login error:', error)
      return { success: false, message: '网络错误，请重试' }
    }
  }

  // 登出
  const logout = () => {
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userRole')
  }

  // 检查权限
  const hasRole = (role) => {
    return user.value && user.value.role === role
  }

  return {
    user,
    token,
    isLoggedIn,
    userInfo,
    login,
    logout,
    hasRole
  }
})
