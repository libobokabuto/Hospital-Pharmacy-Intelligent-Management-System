<template>
  <el-container class="layout-container">
    <!-- 顶部导航栏 -->
    <el-header class="layout-header">
      <div class="header-left">
        <h1 class="system-title">医院药房智能管理系统</h1>
      </div>
      <div class="header-right">
        <el-dropdown @command="handleCommand">
          <span class="user-info">
            <el-icon><User /></el-icon>
            <span>{{ userStore.userInfo?.realName || userStore.userInfo?.username || '用户' }}</span>
            <el-icon class="el-icon--right"><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">个人中心</el-dropdown-item>
              <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>

    <el-container>
      <!-- 侧边栏 -->
      <el-aside :width="isCollapse ? '64px' : '200px'" class="layout-aside">
        <el-menu
          :default-active="activeMenu"
          :collapse="isCollapse"
          :collapse-transition="false"
          router
          class="sidebar-menu"
        >
          <template v-for="item in menuItems" :key="item.path">
            <el-menu-item v-if="!item.children" :index="item.path">
              <el-icon><component :is="item.icon" /></el-icon>
              <template #title>{{ item.title }}</template>
            </el-menu-item>
            <el-sub-menu v-else :index="item.path">
              <template #title>
                <el-icon><component :is="item.icon" /></el-icon>
                <span>{{ item.title }}</span>
              </template>
              <el-menu-item
                v-for="child in item.children"
                :key="child.path"
                :index="child.path"
              >
                {{ child.title }}
              </el-menu-item>
            </el-sub-menu>
          </template>
        </el-menu>
      </el-aside>

      <!-- 主内容区 -->
      <el-main class="layout-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  User,
  ArrowDown,
  House,
  FirstAidKit,
  Box,
  Document,
  UserFilled,
  DataAnalysis,
  Setting,
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isCollapse = ref(false)
const activeMenu = computed(() => route.path)

// 根据角色生成菜单
const menuItems = computed(() => {
  const role = userStore.userInfo?.role || localStorage.getItem('userRole')
  
  const baseMenus = {
    admin: [
      {
        path: '/admin',
        title: '仪表板',
        icon: 'House',
      },
      {
        path: '/admin/medicines',
        title: '药品管理',
        icon: 'FirstAidKit',
      },
      {
        path: '/admin/inventory',
        title: '库存管理',
        icon: 'Box',
      },
      {
        path: '/admin/users',
        title: '用户管理',
        icon: 'UserFilled',
      },
      {
        path: '/admin/prescriptions',
        title: '处方管理',
        icon: 'Document',
      },
      {
        path: '/admin/audit',
        title: '审核记录',
        icon: 'DataAnalysis',
      },
      {
        path: '/admin/settings',
        title: '系统设置',
        icon: 'Setting',
      },
    ],
    doctor: [
      {
        path: '/doctor',
        title: '工作台',
        icon: 'House',
      },
      {
        path: '/doctor/prescriptions',
        title: '我的处方',
        icon: 'Document',
      },
    ],
    nurse: [
      {
        path: '/nurse',
        title: '工作台',
        icon: 'House',
      },
      {
        path: '/nurse/prescriptions',
        title: '处方查询',
        icon: 'Document',
      },
    ],
    pharmacist: [
      {
        path: '/pharmacist',
        title: '工作台',
        icon: 'House',
      },
      {
        path: '/pharmacist/prescriptions',
        title: '处方审核',
        icon: 'Document',
      },
      {
        path: '/pharmacist/inventory',
        title: '库存管理',
        icon: 'Box',
      },
      {
        path: '/pharmacist/audit',
        title: '审核记录',
        icon: 'DataAnalysis',
      },
    ],
  }

  return baseMenus[role] || []
})

const handleCommand = async (command) => {
  if (command === 'logout') {
    try {
      await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      })
      await userStore.logout()
      ElMessage.success('退出登录成功')
      router.push('/login')
    } catch (error) {
      // 用户取消
    }
  } else if (command === 'profile') {
    ElMessage.info('个人中心功能开发中')
  }
}

onMounted(() => {
  userStore.initUser()
})
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.layout-header {
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.header-left {
  display: flex;
  align-items: center;
}

.system-title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  color: #606266;
  font-size: 14px;
}

.user-info .el-icon {
  margin: 0 5px;
}

.layout-aside {
  background: #fff;
  border-right: 1px solid #e4e7ed;
}

.sidebar-menu {
  border-right: none;
  height: 100%;
}

.layout-main {
  background: #f5f7fa;
  padding: 20px;
  overflow-y: auto;
}
</style>

