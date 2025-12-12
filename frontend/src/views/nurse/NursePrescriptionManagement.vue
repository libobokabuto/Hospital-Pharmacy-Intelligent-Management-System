<template>
  <div class="prescription-management">
    <div class="page-header">
      <h2>处方查询</h2>
    </div>

    <!-- 搜索和筛选 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="处方状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 150px">
            <el-option label="已通过" value="已通过" />
            <el-option label="已发药" value="已发药" />
          </el-select>
        </el-form-item>
        <el-form-item label="患者姓名">
          <el-input
            v-model="searchForm.patientName"
            placeholder="请输入患者姓名"
            clearable
            style="width: 150px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 处方列表 -->
    <el-card shadow="never">
      <el-table
        v-loading="loading"
        :data="prescriptionList"
        style="width: 100%"
        stripe
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="prescriptionNumber" label="处方号" width="150" />
        <el-table-column prop="patientName" label="患者姓名" width="120" />
        <el-table-column prop="patientAge" label="年龄" width="80" />
        <el-table-column prop="patientGender" label="性别" width="80" />
        <el-table-column prop="doctorName" label="医生" width="120" />
        <el-table-column prop="department" label="科室" width="120" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createDate" label="处方日期" width="120">
          <template #default="{ row }">
            {{ formatDate(row.createDate) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleView(row)">
              查看
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="处方详情" width="800px">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center; width: 100%">
          <span>处方详情</span>
          <el-button type="primary" @click="handlePrint" :icon="Printer">打印</el-button>
        </div>
      </template>
      <div v-if="currentPrescription" id="prescription-print-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="处方号">{{ currentPrescription.prescriptionNumber }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusTagType(currentPrescription.status)">
              {{ currentPrescription.status }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="患者姓名">{{ currentPrescription.patientName }}</el-descriptions-item>
          <el-descriptions-item label="年龄">{{ currentPrescription.patientAge }}</el-descriptions-item>
          <el-descriptions-item label="性别">{{ currentPrescription.patientGender }}</el-descriptions-item>
          <el-descriptions-item label="科室">{{ currentPrescription.department }}</el-descriptions-item>
          <el-descriptions-item label="患者症状" :span="2">
            {{ currentPrescription.patientSymptoms || '暂无' }}
          </el-descriptions-item>
          <el-descriptions-item label="诊断" :span="2">
            {{ currentPrescription.diagnosis || '暂无' }}
          </el-descriptions-item>
          <el-descriptions-item label="过敏史" :span="2">
            {{ currentPrescription.allergies || '无' }}
          </el-descriptions-item>
          <el-descriptions-item label="处方日期">{{ formatDate(currentPrescription.createDate) }}</el-descriptions-item>
          <el-descriptions-item label="医生">{{ currentPrescription.doctorName }}</el-descriptions-item>
        </el-descriptions>

        <h3 style="margin-top: 20px; margin-bottom: 10px">处方明细</h3>
        <el-table :data="prescriptionDetails" style="width: 100%">
          <el-table-column prop="medicineId" label="药品ID" width="100" />
          <el-table-column prop="quantity" label="数量" width="100" />
          <el-table-column prop="dosage" label="用法" width="150" />
          <el-table-column prop="frequency" label="频次" width="100" />
          <el-table-column prop="days" label="天数" width="100" />
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { prescriptionAPI } from '@/api'
import { ElMessage } from 'element-plus'
import { Search, Printer } from '@element-plus/icons-vue'
import { formatDate } from '@/utils/format'

const loading = ref(false)
const detailDialogVisible = ref(false)
const currentPrescription = ref(null)
const prescriptionDetails = ref([])

const searchForm = reactive({
  status: '',
  patientName: '',
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0,
})

const prescriptionList = ref([])

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

const loadPrescriptions = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page - 1,
      size: pagination.size,
      status: '已通过', // 护士只能查看已通过的处方
    }
    if (searchForm.status) {
      params.status = searchForm.status
    }
    if (searchForm.patientName) {
      params.patientName = searchForm.patientName
    }

    const response = await prescriptionAPI.getPrescriptions(params)
    if (response.success) {
      prescriptionList.value = response.data.content || []
      pagination.total = response.data.totalElements || 0
    }
  } catch (error) {
    ElMessage.error('加载处方列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadPrescriptions()
}

const handleReset = () => {
  searchForm.status = ''
  searchForm.patientName = ''
  handleSearch()
}

const handleView = async (row) => {
  currentPrescription.value = row
  try {
    const response = await prescriptionAPI.getPrescription(row.id)
    if (response.success) {
      prescriptionDetails.value = response.data.details || []
    }

    detailDialogVisible.value = true
  } catch (error) {
    ElMessage.error('加载处方详情失败')
  }
}

const handlePageChange = (page) => {
  pagination.page = page
  loadPrescriptions()
}

const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  loadPrescriptions()
}

const handlePrint = () => {
  const printContent = document.getElementById('prescription-print-content')
  if (!printContent || !currentPrescription.value) return

  // 创建打印内容的HTML
  const printHTML = `
    <!DOCTYPE html>
    <html>
      <head>
        <meta charset="UTF-8">
        <title>处方详情 - ${currentPrescription.value.prescriptionNumber || ''}</title>
        <style>
          @media print {
            @page {
              margin: 15mm;
              size: A4;
            }
            body {
              margin: 0;
              padding: 0;
            }
          }
          body {
            font-family: "Microsoft YaHei", Arial, sans-serif;
            padding: 20px;
            font-size: 14px;
            color: #333;
          }
          .print-header {
            text-align: center;
            margin-bottom: 30px;
            padding-bottom: 15px;
            border-bottom: 2px solid #333;
          }
          .print-header h1 {
            margin: 0 0 10px 0;
            font-size: 24px;
            font-weight: bold;
          }
          .print-header .hospital-name {
            font-size: 18px;
            margin-bottom: 5px;
          }
          .prescription-info table {
            width: 100%;
            border-collapse: collapse;
            margin-bottom: 20px;
          }
          .prescription-info table td {
            padding: 8px;
            border: 1px solid #ddd;
          }
          .medicine-table {
            width: 100%;
            border-collapse: collapse;
            margin: 20px 0;
          }
          .medicine-table th,
          .medicine-table td {
            border: 1px solid #ddd;
            padding: 8px;
            text-align: left;
          }
          .medicine-table th {
            background-color: #f5f7fa;
            font-weight: bold;
            text-align: center;
          }
          .print-footer {
            margin-top: 40px;
            text-align: right;
            padding-top: 20px;
            border-top: 1px solid #ddd;
          }
        </style>
      </head>
      <body>
        <div class="print-header">
          <div class="hospital-name">医院药房智能管理系统</div>
          <h1>处方详情</h1>
        </div>
        ${printContent.innerHTML}
        <div class="print-footer">
          <div>医生签名：${currentPrescription.value.doctorName || ''}</div>
          <div style="margin-top: 20px;">日期：${formatDate(currentPrescription.value.createDate) || ''}</div>
        </div>
      </body>
    </html>
  `

  const printWindow = window.open('', '_blank')
  printWindow.document.write(printHTML)
  printWindow.document.close()
  
  printWindow.onload = () => {
    setTimeout(() => {
      printWindow.focus()
      printWindow.print()
    }, 250)
  }
}

onMounted(() => {
  loadPrescriptions()
})
</script>

<style scoped>
.prescription-management {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.search-card {
  margin-bottom: 20px;
}

.search-form {
  margin: 0;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>

