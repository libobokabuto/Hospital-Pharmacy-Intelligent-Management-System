<template>
  <Layout>
    <div class="doctor-dashboard">
      <h2 class="page-title">医生工作台</h2>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">
                <span>我的处方</span>
                <el-button type="primary" size="small" @click="handleCreatePrescription">
                  新建处方
                </el-button>
              </div>
            </template>
            <el-table :data="myPrescriptions" style="width: 100%">
              <el-table-column prop="prescriptionNumber" label="处方号" width="150" />
              <el-table-column prop="patientName" label="患者" width="120" />
              <el-table-column prop="status" label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="getStatusTagType(row.status)">
                    {{ row.status }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="createDate" label="日期" width="120">
                <template #default="{ row }">
                  {{ formatDate(row.createDate) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="100">
                <template #default="{ row }">
                  <el-button type="primary" size="small" @click="handleView(row)">
                    查看
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>

        <el-col :span="12">
          <el-card shadow="hover">
            <template #header>
              <span>待审核处方</span>
            </template>
            <el-table :data="pendingPrescriptions" style="width: 100%">
              <el-table-column prop="prescriptionNumber" label="处方号" width="150" />
              <el-table-column prop="patientName" label="患者" width="120" />
              <el-table-column prop="createDate" label="日期" width="120">
                <template #default="{ row }">
                  {{ formatDate(row.createDate) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="100">
                <template #default="{ row }">
                  <el-button type="primary" size="small" @click="handleView(row)">
                    查看
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </Layout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import Layout from '@/components/Layout.vue'
import { prescriptionAPI } from '@/api'
import { ElMessage } from 'element-plus'
import { formatDate } from '@/utils/format'

const myPrescriptions = ref([])
const pendingPrescriptions = ref([])

const getStatusTagType = (status) => {
  const typeMap = {
    '未审核': 'info',
    '审核中': 'warning',
    '已通过': 'success',
    '已拒绝': 'danger',
    '已发药': 'success',
    '已取消': 'info',
  }
  return typeMap[status] || ''
}

const loadMyPrescriptions = async () => {
  try {
    const response = await prescriptionAPI.getPrescriptions({ page: 0, size: 5 })
    if (response.success) {
      myPrescriptions.value = response.data.content || []
    }
  } catch (error) {
    console.error('Load prescriptions error:', error)
  }
}

const loadPendingPrescriptions = async () => {
  try {
    const response = await prescriptionAPI.getPrescriptions({ status: '未审核', page: 0, size: 5 })
    if (response.success) {
      pendingPrescriptions.value = response.data.content || []
    }
  } catch (error) {
    console.error('Load pending prescriptions error:', error)
  }
}

const handleCreatePrescription = () => {
  ElMessage.info('处方创建功能开发中')
}

const handleView = (row) => {
  ElMessage.info('查看处方详情功能开发中')
}

onMounted(() => {
  loadMyPrescriptions()
  loadPendingPrescriptions()
})
</script>

<style scoped>
.doctor-dashboard {
  padding: 20px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>

