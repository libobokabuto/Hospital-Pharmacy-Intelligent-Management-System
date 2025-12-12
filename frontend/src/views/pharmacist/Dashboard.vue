<template>
  <div class="pharmacist-dashboard">
      <h2 class="page-title">药师工作台</h2>

      <el-row :gutter="20" class="stats-row">
        <el-col :span="6">
          <el-card shadow="hover">
            <div class="stat-item">
              <div class="stat-label">待审核处方</div>
              <div class="stat-value">{{ stats.pendingCount }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover">
            <div class="stat-item">
              <div class="stat-label">待发药处方</div>
              <div class="stat-value">{{ stats.dispenseCount }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover">
            <div class="stat-item">
              <div class="stat-label">今日审核</div>
              <div class="stat-value">{{ stats.todayAuditCount }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover">
            <div class="stat-item">
              <div class="stat-label">库存预警</div>
              <div class="stat-value warning">{{ stats.lowStockCount }}</div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="20" style="margin-top: 20px">
        <el-col :span="12">
          <el-card shadow="hover">
            <template #header>
              <span>待审核处方</span>
            </template>
            <el-table :data="pendingAudit" style="width: 100%">
              <el-table-column prop="prescriptionNumber" label="处方号" width="150" />
              <el-table-column prop="patientName" label="患者" width="120" />
              <el-table-column prop="createDate" label="日期" width="120">
                <template #default="{ row }">
                  {{ formatDate(row.createDate) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="100">
                <template #default="{ row }">
                  <el-button type="primary" size="small" @click="handleAudit(row)">
                    审核
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>

        <el-col :span="12">
          <el-card shadow="hover">
            <template #header>
              <span>待发药处方</span>
            </template>
            <el-table :data="pendingDispense" style="width: 100%">
              <el-table-column prop="prescriptionNumber" label="处方号" width="150" />
              <el-table-column prop="patientName" label="患者" width="120" />
              <el-table-column prop="createDate" label="日期" width="120">
                <template #default="{ row }">
                  {{ formatDate(row.createDate) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="100">
                <template #default="{ row }">
                  <el-button type="warning" size="small" @click="handleDispense(row)">
                    发药
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>
      </el-row>
    </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { prescriptionAPI, medicineAPI } from '@/api'
import { formatDate } from '@/utils/format'

const router = useRouter()

const stats = reactive({
  pendingCount: 0,
  dispenseCount: 0,
  todayAuditCount: 0,
  lowStockCount: 0,
})

const pendingAudit = ref([])
const pendingDispense = ref([])

const loadStatistics = async () => {
  try {
    const pendingRes = await prescriptionAPI.getPrescriptions({ status: '未审核', page: 0, size: 1 })
    if (pendingRes.success) {
      stats.pendingCount = pendingRes.data.totalElements || 0
    }

    const dispenseRes = await prescriptionAPI.getPrescriptions({ status: '已通过', page: 0, size: 1 })
    if (dispenseRes.success) {
      stats.dispenseCount = dispenseRes.data.totalElements || 0
    }

    const lowStockRes = await medicineAPI.getLowStockMedicines()
    if (lowStockRes.success) {
      stats.lowStockCount = lowStockRes.data?.length || 0
    }
  } catch (error) {
    console.error('Load statistics error:', error)
  }
}

const loadPendingAudit = async () => {
  try {
    const response = await prescriptionAPI.getPrescriptions({ status: '未审核', page: 0, size: 5 })
    if (response.success) {
      pendingAudit.value = response.data.content || []
    }
  } catch (error) {
    console.error('Load pending audit error:', error)
  }
}

const loadPendingDispense = async () => {
  try {
    const response = await prescriptionAPI.getPrescriptions({ status: '已通过', page: 0, size: 5 })
    if (response.success) {
      pendingDispense.value = response.data.content || []
    }
  } catch (error) {
    console.error('Load pending dispense error:', error)
  }
}

const handleAudit = (row) => {
  router.push(`/pharmacist/prescriptions`)
}

const handleDispense = (row) => {
  router.push(`/pharmacist/prescriptions`)
}

onMounted(() => {
  loadStatistics()
  loadPendingAudit()
  loadPendingDispense()
})
</script>

<style scoped>
.pharmacist-dashboard {
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

.stat-item {
  text-align: center;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 10px;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
}

.stat-value.warning {
  color: #E6A23C;
}
</style>

