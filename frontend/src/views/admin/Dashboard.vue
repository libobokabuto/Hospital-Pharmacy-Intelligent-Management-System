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

        <!-- 数据统计图表 -->
        <el-row :gutter="20" class="charts-row">
          <el-col :xs="24" :sm="12" :md="12">
            <el-card shadow="never">
              <template #header>
                <div class="card-header">
                  <span>处方状态分布</span>
                </div>
              </template>
              <div ref="prescriptionStatusChart" style="width: 100%; height: 300px;"></div>
            </el-card>
          </el-col>
          <el-col :xs="24" :sm="12" :md="12">
            <el-card shadow="never">
              <template #header>
                <div class="card-header">
                  <span>库存统计</span>
                </div>
              </template>
              <div ref="stockChart" style="width: 100%; height: 300px;"></div>
            </el-card>
          </el-col>
        </el-row>

        <el-row :gutter="20" class="charts-row" style="margin-top: 20px">
          <el-col :xs="24" :sm="24" :md="24">
            <el-card shadow="never">
              <template #header>
                <div class="card-header">
                  <span>审核通过率趋势</span>
                </div>
              </template>
              <div ref="auditTrendChart" style="width: 100%; height: 300px;"></div>
            </el-card>
          </el-col>
        </el-row>

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
import { ref, onMounted, nextTick, markRaw } from 'vue'
import { useRouter } from 'vue-router'
import { medicineAPI, stockAPI, prescriptionAPI, auditAPI } from '@/api'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
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
  { title: '药品总数', value: 0, icon: markRaw(FirstAidKit), color: '#409EFF' },
  { title: '库存预警', value: 0, icon: markRaw(Warning), color: '#E6A23C' },
  { title: '待审核处方', value: 0, icon: markRaw(Document), color: '#67C23A' },
  { title: '系统用户', value: 0, icon: markRaw(UserFilled), color: '#F56C6C' },
])

const prescriptionStatusChart = ref(null)
const stockChart = ref(null)
const auditTrendChart = ref(null)
let prescriptionStatusChartInstance = null
let stockChartInstance = null
let auditTrendChartInstance = null

const quickActions = [
  { title: '新增药品', icon: markRaw(Plus), path: '/admin/medicines?action=create' },
  { title: '药品入库', icon: markRaw(Box), path: '/admin/inventory?action=stockIn' },
  { title: '处方查询', icon: markRaw(Search), path: '/admin/prescriptions' },
  { title: '用户管理', icon: markRaw(UserFilled), path: '/admin/users' },
]

const lowStockMedicines = ref([])

const initCharts = async () => {
  await nextTick()
  
  // 初始化处方状态分布饼图
  if (prescriptionStatusChart.value && !prescriptionStatusChartInstance) {
    prescriptionStatusChartInstance = echarts.init(prescriptionStatusChart.value)
  }
  
  // 初始化库存统计柱状图
  if (stockChart.value && !stockChartInstance) {
    stockChartInstance = echarts.init(stockChart.value)
  }
  
  // 初始化审核趋势折线图
  if (auditTrendChart.value && !auditTrendChartInstance) {
    auditTrendChartInstance = echarts.init(auditTrendChart.value)
  }
  
  // 加载处方数据并更新图表
  await loadPrescriptionChartData()
  await loadStockChartData()
  await loadAuditTrendChartData()
}

const loadPrescriptionChartData = async () => {
  try {
    const response = await prescriptionAPI.getPrescriptions({ page: 0, size: 1000 })
    if (response.success && prescriptionStatusChartInstance) {
      const prescriptions = response.data.content || []
      const statusMap = {}
      prescriptions.forEach(p => {
        statusMap[p.status] = (statusMap[p.status] || 0) + 1
      })
      
      const option = {
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b}: {c} ({d}%)'
        },
        legend: {
          orient: 'vertical',
          left: 'left'
        },
        series: [
          {
            name: '处方状态',
            type: 'pie',
            radius: '50%',
            data: Object.keys(statusMap).map(status => ({
              value: statusMap[status],
              name: status
            })),
            emphasis: {
              itemStyle: {
                shadowBlur: 10,
                shadowOffsetX: 0,
                shadowColor: 'rgba(0, 0, 0, 0.5)'
              }
            }
          }
        ]
      }
      prescriptionStatusChartInstance.setOption(option)
    }
  } catch (error) {
    console.error('Load prescription chart data error:', error)
  }
}

const loadStockChartData = async () => {
  try {
    const response = await stockAPI.getStockStatistics()
    if (response.success && stockChartInstance) {
      const data = response.data || {}
      const option = {
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          }
        },
        xAxis: {
          type: 'category',
          data: ['药品总数', '库存预警', '缺货数量']
        },
        yAxis: {
          type: 'value'
        },
        series: [
          {
            name: '数量',
            type: 'bar',
            data: [
              data.totalMedicines || 0,
              data.lowStockCount || 0,
              data.outOfStockCount || 0
            ],
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: '#83bff6' },
                { offset: 0.5, color: '#188df0' },
                { offset: 1, color: '#188df0' }
              ])
            }
          }
        ]
      }
      stockChartInstance.setOption(option)
    }
  } catch (error) {
    console.error('Load stock chart data error:', error)
  }
}

const loadAuditTrendChartData = async () => {
  try {
    const response = await auditAPI.getAuditStatistics()
    if (response.success && auditTrendChartInstance) {
      // 模拟最近7天的数据（实际应该从后端获取）
      const dates = []
      const passRates = []
      for (let i = 6; i >= 0; i--) {
        const date = new Date()
        date.setDate(date.getDate() - i)
        dates.push(`${date.getMonth() + 1}/${date.getDate()}`)
        // 模拟通过率数据
        passRates.push(Math.floor(Math.random() * 30) + 70)
      }
      
      const option = {
        tooltip: {
          trigger: 'axis'
        },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: dates
        },
        yAxis: {
          type: 'value',
          min: 0,
          max: 100,
          axisLabel: {
            formatter: '{value}%'
          }
        },
        series: [
          {
            name: '审核通过率',
            type: 'line',
            data: passRates,
            smooth: true,
            areaStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: 'rgba(24, 144, 255, 0.3)' },
                { offset: 1, color: 'rgba(24, 144, 255, 0.1)' }
              ])
            },
            itemStyle: {
              color: '#1890ff'
            }
          }
        ]
      }
      auditTrendChartInstance.setOption(option)
    } else if (auditTrendChartInstance) {
      // 如果没有数据，显示空图表提示
      const option = {
        title: {
          text: '暂无数据',
          left: 'center',
          top: 'center',
          textStyle: {
            color: '#999'
          }
        }
      }
      auditTrendChartInstance.setOption(option)
    }
  } catch (error) {
    console.error('Load audit trend chart data error:', error)
    if (error.response) {
      console.error('Error response:', error.response.data)
      ElMessage.error('加载审核趋势图表数据失败: ' + (error.response.data?.message || error.message))
    } else {
      ElMessage.error('加载审核趋势图表数据失败: ' + error.message)
    }
  }
}

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

onMounted(async () => {
  await loadStatistics()
  await initCharts()
  
  // 响应式调整图表大小
  window.addEventListener('resize', () => {
    prescriptionStatusChartInstance?.resize()
    stockChartInstance?.resize()
    auditTrendChartInstance?.resize()
  })
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

.charts-row {
  margin-bottom: 20px;
}
</style>
