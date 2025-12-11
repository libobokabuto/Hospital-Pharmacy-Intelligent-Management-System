<template>
  <div class="login-container">
    <div class="login-form">
      <div class="login-header">
        <h2>医院药房智能管理系统</h2>
        <p>请登录您的账户</p>
      </div>

      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        label-width="0px"
        class="login-form-content"
        @submit.prevent="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            size="large"
            :prefix-icon="User"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            :prefix-icon="Lock"
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="login-button"
            :loading="loading"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-footer">
        <p>© 2025 医院药房智能管理系统</p>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { User, Lock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

export default {
  name: 'Login',
  components: {
    User,
    Lock
  },
  setup() {
    const router = useRouter()
    const userStore = useUserStore()

    const loginFormRef = ref()
    const loading = ref(false)

    const loginForm = reactive({
      username: '',
      password: ''
    })

    const loginRules = {
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' }
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' }
      ]
    }

    const handleLogin = async (e) => {
      // 阻止表单默认提交行为
      if (e && e.preventDefault) {
        e.preventDefault()
      }

      if (!loginFormRef.value) return

      try {
        // 验证表单
        await loginFormRef.value.validate()

        loading.value = true

        // 调用登录接口
        const result = await userStore.login(loginForm.username, loginForm.password)

        if (result.success) {
          ElMessage.success('登录成功')

          // 根据角色跳转到对应页面
          const role = userStore.user.role
          switch (role) {
            case 'admin':
              router.push('/admin')
              break
            case 'doctor':
              router.push('/doctor')
              break
            case 'nurse':
              router.push('/nurse')
              break
            case 'pharmacist':
              router.push('/pharmacist')
              break
            default:
              ElMessage.warning('未知角色，请联系管理员')
              router.push('/login')
          }
        } else {
          // 登录失败，显示错误信息
          ElMessage.error(result.message || '登录失败，请检查用户名和密码')
        }
      } catch (error) {
        // 表单验证失败或其他错误
        // Element Plus 验证失败时，error 格式为 { fieldName: [errorMessages] }
        // 这种情况下不需要额外处理，Element Plus 已经显示了错误信息
        if (error && typeof error === 'object' && !error.message) {
          // 这是表单验证错误，Element Plus 会自动显示，不需要额外处理
          return
        }
        // 其他类型的错误才显示
        console.error('Login error:', error)
        if (error.message) {
          ElMessage.error(error.message)
        }
      } finally {
        loading.value = false
      }
    }

    return {
      loginFormRef,
      loginForm,
      loginRules,
      loading,
      handleLogin,
      User,
      Lock
    }
  }
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-form {
  width: 400px;
  padding: 40px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h2 {
  color: #333;
  margin-bottom: 10px;
  font-size: 24px;
  font-weight: 600;
}

.login-header p {
  color: #666;
  margin: 0;
}

.login-form-content {
  margin-bottom: 20px;
}

.login-button {
  width: 100%;
}

.login-footer {
  text-align: center;
  color: #999;
  font-size: 14px;
}
</style>
