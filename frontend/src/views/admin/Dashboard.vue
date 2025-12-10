<template>
  <div class="admin-dashboard">
    <el-container style="height: 100vh">
      <!-- 侧边栏 -->
      <el-aside width="200px">
        <el-menu
          :default-active="$route.path"
          class="admin-menu"
          @select="handleMenuSelect"
        >
          <el-menu-item index="/admin">
            <el-icon><House /></el-icon>
            <span>主页</span>
          </el-menu-item>
          <el-menu-item index="/admin/users">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/medicines">
            <el-icon><FirstAidKit /></el-icon>
            <span>药品管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/inventory">
            <el-icon><Box /></el-icon>
            <span>库存管理</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <!-- 主内容区 -->
      <el-container>
        <!-- 头部 -->
        <el-header class="admin-header">
          <div class="header-left">
            <h3>管理员控制台</h3>
          </div>
          <div class="header-right">
            <el-dropdown @command="handleCommand">
              <span class="user-info">
                {{ userStore.userInfo?.realName || userStore.userInfo?.username }}
                <el-icon class="el-icon--right">
                  <ArrowDown />
                </el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人资料</el-dropdown-item>
                  <el-dropdown-item command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>

        <!-- 主要内容 -->
        <el-main class="admin-main">
          <router-view />
          <!-- 默认显示统计面板 -->
          <div v-if="$route.path === '/admin'" class="dashboard-content">
            <el-row :gutter="20" class="stats-row">
              <el-col :span="6">
                <el-card class="stat-card">
                  <div class="stat-content">
                    <div class="stat-icon">
                      <el-icon size="32" color="#409EFF"><User /></el-icon>
                    </div>
                    <div class="stat-info">
                      <div class="stat-number">{{ stats.totalUsers }}</div>
                      <div class="stat-label">总用户数</div>
                    </div>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card class="stat-card">
                  <div class="stat-content">
                    <div class="stat-icon">
                      <el-icon size="32" color="#67C23A"><FirstAidKit /></el-icon>
                    </div>
                    <div class="stat-info">
                      <div class="stat-number">{{ stats.totalMedicines }}</div>
                      <div class="stat-label">药品种类</div>
                    </div>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card class="stat-card">
                  <div class="stat-content">
                    <div class="stat-icon">
                      <el-icon size="32" color="#E6A23C"><Box /></el-icon>
                    </div>
                    <div class="stat-info">
                      <div class="stat-number">{{ stats.totalInventory }}</div>
                      <div class="stat-label">库存总量</div>
                    </div>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card class="stat-card">
                  <div class="stat-content">
                    <div class="stat-icon">
                      <el-icon size="32" color="#F56C6C"><Document /></el-icon>
                    </div>
                    <div class="stat-info">
                      <div class="stat-number">{{ stats.pendingPrescriptions }}</div>
                      <div class="stat-label">待审核处方</div>
                    </div>
                  </div>
                </el-card>
              </el-col>
            </el-row>

            <el-row :gutter="20" class="charts-row">
              <el-col :span="12">
                <el-card title="系统状态">
                  <div class="system-status">
                    <el-progress
                      type="circle"
                      :percentage="85"
                      :width="120"
                      status="success"
                      class="status-item"
                    >
                      <template #default>
                        <span class="percentage-text">服务器</span>
                      </template>
                    </el-progress>
                    <el-progress
                      type="circle"
                      :percentage="92"
                      :width="120"
                      status="success"
                      class="status-item"
                    >
                      <template #default>
                        <span class="percentage-text">数据库</span>
                      </template>
                    </el-progress>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="12">
                <el-card title="近期活动">
                  <el-timeline>
                    <el-timeline-item
                      v-for="activity in recentActivities"
                      :key="activity.id"
                      :timestamp="activity.timestamp"
                    >
                      {{ activity.description }}
                    </el-timeline-item>
                  </el-timeline>
                </el-card>
              </el-col>
            </el-row>
          </div>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import {
  House,
  User,
  FirstAidKit,
  Box,
  Document,
  ArrowDown
} from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'

export default {
  name: 'AdminDashboard',
  components: {
    House,
    User,
    FirstAidKit,
    Box,
    Document,
    ArrowDown
  },
  setup() {
    const router = useRouter()
    const userStore = useUserStore()

    // 统计数据
    const stats = ref({
      totalUsers: 0,
      totalMedicines: 0,
      totalInventory: 0,
      pendingPrescriptions: 0
    })

    // 近期活动
    const recentActivities = ref([
      {
        id: 1,
        timestamp: '2025-12-09 10:30:00',
        description: '管理员李教博登录系统'
      },
      {
        id: 2,
        timestamp: '2025-12-09 09:15:00',
        description: '新增药品信息：阿莫西林'
      },
      {
        id: 3,
        timestamp: '2025-12-09 08:45:00',
        description: '库存预警：布洛芬库存不足'
      }
    ])

    const handleMenuSelect = (index) => {
      router.push(index)
    }

    const handleCommand = async (command) => {
      switch (command) {
        case 'profile':
          // 跳转到个人资料页
          break
        case 'logout':
          try {
            await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
              confirmButtonText: '确定',
              cancelButtonText: '取消',
              type: 'warning',
            })
            userStore.logout()
            router.push('/login')
          } catch {
            // 用户取消操作
          }
          break
      }
    }

    // 加载统计数据
    const loadStats = async () => {
      try {
        // 这里调用API获取统计数据
        // 暂时使用模拟数据
        stats.value = {
          totalUsers: 25,
          totalMedicines: 156,
          totalInventory: 2847,
          pendingPrescriptions: 8
        }
      } catch (error) {
        console.error('Load stats error:', error)
      }
    }

    onMounted(() => {
      loadStats()
    })

    return {
      userStore,
      stats,
      recentActivities,
      handleMenuSelect,
      handleCommand
    }
  }
}
</script>

<style scoped>
.admin-dashboard {
  height: 100vh;
}

.admin-menu {
  height: 100%;
  border-right: none;
}

.admin-header {
  background-color: #fff;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}

.header-left h3 {
  margin: 0;
  color: #333;
}

.user-info {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 5px;
}

.admin-main {
  background-color: #f5f5f5;
  padding: 20px;
}

.dashboard-content {
  max-width: 1200px;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  height: 120px;
}

.stat-content {
  display: flex;
  align-items: center;
  height: 100%;
}

.stat-icon {
  margin-right: 20px;
}

.stat-info {
  flex: 1;
}

.stat-number {
  font-size: 28px;
  font-weight: bold;
  color: #333;
  margin-bottom: 5px;
}

.stat-label {
  color: #666;
  font-size: 14px;
}

.charts-row {
  margin-top: 20px;
}

.system-status {
  display: flex;
  justify-content: space-around;
  align-items: center;
}

.status-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.percentage-text {
  font-size: 12px;
  color: #666;
  margin-top: 5px;
}
</style>
