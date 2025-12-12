<template>
  <div class="nurse-dashboard">
      <h2 class="page-title">护士工作台</h2>

      <el-card shadow="hover">
        <template #header>
          <span>待发药处方</span>
        </template>
        <el-table :data="pendingDispense" style="width: 100%">
          <el-table-column prop="prescriptionNumber" label="处方号" width="150" />
          <el-table-column prop="patientName" label="患者" width="120" />
          <el-table-column prop="doctorName" label="医生" width="120" />
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
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { prescriptionAPI } from '@/api'
import { ElMessage } from 'element-plus'
import { formatDate } from '@/utils/format'

const pendingDispense = ref([])

const loadPendingDispense = async () => {
  try {
    const response = await prescriptionAPI.getPrescriptions({ status: '已通过', page: 0, size: 10 })
    if (response.success) {
      pendingDispense.value = response.data.content || []
    }
  } catch (error) {
    console.error('Load pending dispense error:', error)
  }
}

const handleView = (row) => {
  ElMessage.info('查看处方详情功能开发中')
}

onMounted(() => {
  loadPendingDispense()
})
</script>

<style scoped>
.nurse-dashboard {
  padding: 20px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 20px;
}
</style>

