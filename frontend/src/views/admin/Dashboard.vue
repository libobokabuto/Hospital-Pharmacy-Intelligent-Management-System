<template>
  <div class="dashboard">
    <div class="dashboard-content">
        <h2 class="page-title">系统概览</h2>

        <!-- 统计卡片 -->
        <el-row :gutter="20" class="stats-row">
          <el-col :xs="24" :sm="12" :md="6" v-for="stat in stats" :key="stat.title">
            <el-card class="stat-card" shadow="hover">
              <div class="stat-content">
                <div class="stat-icon" :style="{ background: stat.color }">
                  <el-icon :size="30"><component :is="stat.icon" /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ stat.value }}</div>
                  <div class="stat-title">{{ stat.title }}</div>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <!-- 快捷操作 -->
        <el-card class="quick-actions-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span>快捷操作</span>
            </div>
          </template>
          <el-row :gutter="20">
            <el-col :xs="12" :sm="8" :md="6" v-for="action in quickActions" :key="action.title">
              <div class="quick-action-item" @click="handleQuickAction(action.path)">
                <el-icon :size="24"><component :is="action.icon" /></el-icon>
                <span>{{ action.title }}</span>
              </div>
            </el-col>
          </el-row>
        </el-card>

        <!-- 低库存预警 -->
        <el-card class="alert-card" shadow="never" v-if="lowStockMedicines.length > 0">
          <template #header>
            <div class="card-header">
              <el-icon><Warning /></el-icon>
              <span>低库存预警</span>
            </div>
          </template>
          <el-table :data="lowStockMedicines" style="width: 100%">
            <el-table-column prop="name" label="药品名称" />
            <el-table-column prop="stockQuantity" label="当前库存" />
            <el-table-column prop="minStock" label="最低库存" />
            <el-table-column label="状态">
              <template #default="{ row }">
                <el-tag type="warning">库存不足</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作">
              <template #default="{ row }">
                <el-button type="primary" size="small" @click="goToStockIn(row.id)">
                  立即入库
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </div>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { medicineAPI, stockAPI, prescriptionAPI, auditAPI } from '@/api'
import { ElMessage } from 'element-plus'
import {
  FirstAidKit,
  Box,
  Document,
  UserFilled,
  Warning,
  Plus,
  Edit,
  Search,
} from '@element-plus/icons-vue'

const router = useRouter()

const stats = ref([
  { title: '药品总数', value: 0, icon: FirstAidKit, color: '#409EFF' },
  { title: '库存预警', value: 0, icon: Warning, color: '#E6A23C' },
  { title: '待审核处方', value: 0, icon: Document, color: '#67C23A' },
  { title: '系统用户', value: 0, icon: UserFilled, color: '#F56C6C' },
])

const quickActions = [
  { title: '新增药品', icon: Plus, path: '/admin/medicines?action=create' },
  { title: '药品入库', icon: Box, path: '/admin/inventory?action=stockIn' },
  { title: '处方查询', icon: Search, path: '/admin/prescriptions' },
  { title: '用户管理', icon: UserFilled, path: '/admin/users' },
]

const lowStockMedicines = ref([])

const loadStatistics = async () => {
  try {
    // 加载药品统计
    const medicinesRes = await medicineAPI.getMedicines({ page: 0, size: 1 })
    if (medicinesRes.success) {
      stats.value[0].value = medicinesRes.data.totalElements || 0
    }

    // 加载低库存药品
    const lowStockRes = await medicineAPI.getLowStockMedicines()
    if (lowStockRes.success) {
      lowStockMedicines.value = lowStockRes.data || []
      stats.value[1].value = lowStockMedicines.value.length
    }

    // 加载待审核处方
    const prescriptionsRes = await prescriptionAPI.getPrescriptions({ status: '未审核', page: 0, size: 1 })
    if (prescriptionsRes.success) {
      stats.value[2].value = prescriptionsRes.data.totalElements || 0
    }

    // 加载用户统计（需要用户API）
    // stats.value[3].value = 0
  } catch (error) {
    console.error('Load statistics error:', error)
  }
}

const handleQuickAction = (path) => {
  router.push(path)
}

const goToStockIn = (medicineId) => {
  router.push(`/admin/inventory?action=stockIn&medicineId=${medicineId}`)
}

onMounted(() => {
  loadStatistics()
})
</script>

<style scoped>
.dashboard {
  height: 100%;
}

.dashboard-content {
  padding: 20px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 20px;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  margin-bottom: 20px;
}

.stat-content {
  display: flex;
  align-items: center;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  margin-right: 15px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 5px;
}

.stat-title {
  font-size: 14px;
  color: #909399;
}

.quick-actions-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  font-weight: 600;
}

.card-header .el-icon {
  margin-right: 8px;
}

.quick-action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  margin-bottom: 10px;
}

.quick-action-item:hover {
  border-color: #409EFF;
  color: #409EFF;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15);
}

.quick-action-item .el-icon {
  margin-bottom: 8px;
}

.alert-card {
  margin-top: 20px;
}
</style>
