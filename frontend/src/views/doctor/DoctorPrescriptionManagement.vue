<template>
  <div class="prescription-management">
    <div class="page-header">
      <h2>我的处方</h2>
      <el-button type="primary" @click="handleCreate">
        <el-icon><Plus /></el-icon>
        新建处方
      </el-button>
    </div>

    <!-- 搜索和筛选 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="处方状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 150px">
            <el-option label="未审核" value="未审核" />
            <el-option label="审核中" value="审核中" />
            <el-option label="已通过" value="已通过" />
            <el-option label="已拒绝" value="已拒绝" />
            <el-option label="已发药" value="已发药" />
            <el-option label="已取消" value="已取消" />
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
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleView(row)">
              查看
            </el-button>
            <el-button
              v-if="row.status === '未审核'"
              type="danger"
              size="small"
              @click="handleCancel(row)"
            >
              取消
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

    <!-- 创建处方对话框 -->
    <el-dialog v-model="createDialogVisible" title="新建处方" width="900px" @close="resetPrescriptionForm">
      <el-form ref="createFormRef" :model="prescriptionForm" :rules="prescriptionFormRules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="患者姓名" prop="patientName">
              <el-input v-model="prescriptionForm.patientName" placeholder="请输入患者姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="患者年龄" prop="patientAge">
              <el-input-number v-model="prescriptionForm.patientAge" :min="0" :max="150" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="患者性别" prop="patientGender">
              <el-select v-model="prescriptionForm.patientGender" placeholder="请选择性别" style="width: 100%">
                <el-option label="男" value="男" />
                <el-option label="女" value="女" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="科室" prop="department">
              <el-input v-model="prescriptionForm.department" placeholder="请输入科室" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">处方明细</el-divider>
        <div v-for="(detail, index) in prescriptionForm.details" :key="index" class="medicine-detail-item">
          <el-row :gutter="10" style="margin-bottom: 10px">
            <el-col :span="6">
              <el-select
                v-model="detail.medicineId"
                placeholder="选择药品"
                filterable
                remote
                reserve-keyword
                clearable
                :remote-method="(query) => loadMedicineOptions(query, index)"
                :loading="medicineSearchLoading"
                @focus="() => loadMedicineOptions('', index)"
                @change="(val) => { console.log('药品选择变化:', index, val, detail); detail.medicineId = val }"
                style="width: 100%"
              >
                <el-option
                  v-for="medicine in getMedicineOptionsForDetail(index)"
                  :key="medicine.id"
                  :label="medicine.name"
                  :value="medicine.id"
                >
                  <span>{{ medicine.name }}</span>
                  <span style="color: #8492a6; font-size: 13px; margin-left: 10px">
                    ({{ medicine.specification }})
                  </span>
                </el-option>
              </el-select>
            </el-col>
            <el-col :span="4">
              <el-input-number
                v-model="detail.quantity"
                :min="1"
                placeholder="数量"
                style="width: 100%"
              />
            </el-col>
            <el-col :span="4">
              <el-input v-model="detail.dosage" placeholder="用法" />
            </el-col>
            <el-col :span="4">
              <el-input v-model="detail.frequency" placeholder="频次" />
            </el-col>
            <el-col :span="4">
              <el-input-number v-model="detail.days" :min="1" placeholder="天数" style="width: 100%" />
            </el-col>
            <el-col :span="2">
              <el-button
                type="danger"
                :icon="Delete"
                circle
                @click="removeMedicineDetail(index)"
                :disabled="prescriptionForm.details.length === 1"
              />
            </el-col>
          </el-row>
        </div>
        <el-button type="primary" :icon="Plus" @click="addMedicineDetail" style="margin-top: 10px">
          添加药品
        </el-button>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitPrescription" :loading="submitting">
          保存并提交
        </el-button>
      </template>
    </el-dialog>

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

        <h3 style="margin-top: 20px; margin-bottom: 10px">审核历史</h3>
        <el-table :data="auditHistory" style="width: 100%">
          <el-table-column prop="auditType" label="审核类型" width="120" />
          <el-table-column prop="auditResult" label="审核结果" width="120" />
          <el-table-column prop="auditScore" label="得分" width="100" />
          <el-table-column prop="auditor" label="审核人" width="120" />
          <el-table-column prop="auditTime" label="审核时间" width="180">
            <template #default="{ row }">
              {{ formatDateTime(row.auditTime) }}
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { prescriptionAPI, medicineAPI } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Delete, Printer } from '@element-plus/icons-vue'
import { formatDate } from '@/utils/format'
import { useUserStore } from '@/stores/user'

const loading = ref(false)
const detailDialogVisible = ref(false)
const currentPrescription = ref(null)
const prescriptionDetails = ref([])
const auditHistory = ref([])

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

const formatDateTime = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleString('zh-CN')
}

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
    }
    if (searchForm.status) {
      params.status = searchForm.status
    }
    if (searchForm.patientName) {
      params.patientName = searchForm.patientName
    }

    const response = await prescriptionAPI.getPrescriptions(params)
    if (response.success && response.data) {
      // 确保 data 是对象而不是字符串
      const data = typeof response.data === 'string' ? JSON.parse(response.data) : response.data
      prescriptionList.value = data.content || []
      pagination.total = data.totalElements || 0
    } else {
      console.error('获取处方列表失败:', response.message)
      ElMessage.error('获取处方列表失败: ' + (response.message || '未知错误'))
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

const createDialogVisible = ref(false)
const createFormRef = ref()
const submitting = ref(false)
const medicineOptions = ref([])
const medicineSearchLoading = ref(false)

const prescriptionForm = reactive({
  patientName: '',
  patientAge: null,
  patientGender: '',
  department: '',
  details: [],
})

const prescriptionFormRules = {
  patientName: [{ required: true, message: '请输入患者姓名', trigger: 'blur' }],
  patientAge: [{ required: true, message: '请输入患者年龄', trigger: 'blur' }],
  patientGender: [{ required: true, message: '请选择患者性别', trigger: 'change' }],
  department: [{ required: true, message: '请输入科室', trigger: 'blur' }],
}

const addMedicineDetail = () => {
  prescriptionForm.details.push({
    medicineId: null,
    quantity: 1,
    dosage: '',
    frequency: '',
    days: 1,
  })
}

const removeMedicineDetail = (index) => {
  prescriptionForm.details.splice(index, 1)
}

const medicineOptionsMap = ref(new Map()) // 存储每个明细项的药品选项

const loadMedicineOptions = async (keyword, index) => {
  medicineSearchLoading.value = true
  try {
    const response = await medicineAPI.getMedicines({
      keyword: keyword || '',
      page: 0,
      size: 100,
    })
    if (response.success) {
      const medicines = response.data.content || []
      // 如果当前明细已选择了药品，确保该药品在列表中
      const currentDetail = prescriptionForm.details[index]
      if (currentDetail && currentDetail.medicineId) {
        // 查找已选药品，如果不在列表中则添加
        const selectedMedicine = medicines.find(m => m.id === currentDetail.medicineId)
        if (!selectedMedicine) {
          // 如果已选药品不在搜索结果中，需要单独获取
          // 这里先检查是否有缓存，或者简单地将已选ID添加到列表中
          const existingMedicines = medicineOptionsMap.value.get(index) || []
          const alreadyInList = existingMedicines.find(m => m.id === currentDetail.medicineId)
          if (alreadyInList) {
            medicines.unshift(alreadyInList)
          }
        }
      }
      medicineOptionsMap.value.set(index, medicines)
    }
  } catch (error) {
    console.error('Load medicines error:', error)
  } finally {
    medicineSearchLoading.value = false
  }
}

const getMedicineOptionsForDetail = (index) => {
  let options = medicineOptionsMap.value.get(index) || []
  // 如果当前明细已选择药品，确保该药品在列表中
  const currentDetail = prescriptionForm.details[index]
  if (currentDetail && currentDetail.medicineId) {
    const hasSelected = options.some(m => m.id === currentDetail.medicineId)
    if (!hasSelected) {
      // 如果已选药品不在列表中，尝试从其他明细的选项中找
      for (const [idx, meds] of medicineOptionsMap.value.entries()) {
        const found = meds.find(m => m.id === currentDetail.medicineId)
        if (found) {
          options = [found, ...options]
          break
        }
      }
      // 如果还是找不到，创建一个占位对象（至少让选择器能显示）
      if (!options.some(m => m.id === currentDetail.medicineId)) {
        options = [{ id: currentDetail.medicineId, name: '已选药品', specification: '' }, ...options]
      }
    }
  }
  return options
}

const handleCreate = () => {
  createDialogVisible.value = true
  resetPrescriptionForm()
}

const resetPrescriptionForm = () => {
  prescriptionForm.patientName = ''
  prescriptionForm.patientAge = null
  prescriptionForm.patientGender = ''
  prescriptionForm.department = ''
  prescriptionForm.details = []
  // 默认添加一行药品明细
  addMedicineDetail()
  createFormRef.value?.clearValidate()
}

const handleSubmitPrescription = async () => {
  if (!createFormRef.value) return

  try {
    await createFormRef.value.validate()

    // 验证药品明细
    if (prescriptionForm.details.length === 0) {
      ElMessage.warning('请至少添加一种药品')
      return
    }

    for (let i = 0; i < prescriptionForm.details.length; i++) {
      const detail = prescriptionForm.details[i]
      if (!detail.medicineId) {
        ElMessage.warning(`第${i + 1}行药品明细未选择药品`)
        return
      }
      if (!detail.quantity || detail.quantity <= 0) {
        ElMessage.warning(`第${i + 1}行药品数量必须大于0`)
        return
      }
    }

    submitting.value = true

    // 获取当前用户信息
    const userStore = useUserStore()
    const doctorName = userStore.userInfo?.realName || userStore.userInfo?.username || ''

    // 调试：打印所有明细
    console.log('所有明细:', JSON.parse(JSON.stringify(prescriptionForm.details)))
    
    // 过滤掉无效的明细（确保 medicineId 和 quantity 都存在）
    const validDetails = prescriptionForm.details.filter(detail => {
      const isValid = detail.medicineId != null && detail.quantity != null && detail.quantity > 0
      if (!isValid) {
        console.warn('无效明细:', detail)
      }
      return isValid
    })
    
    console.log('有效明细:', validDetails)
    
    if (validDetails.length === 0) {
      ElMessage.error('请至少添加一个有效的药品明细（请确保已选择药品）')
      submitting.value = false
      return
    }

    // 将 Vue Proxy 对象转换为纯 JSON 对象
    const detailsForSubmit = validDetails.map(detail => {
      const result = {
        medicineId: Number(detail.medicineId), // 确保是数字
        quantity: Number(detail.quantity),
        dosage: detail.dosage || '',
        frequency: detail.frequency || '',
        days: detail.days || 1,
      }
      console.log('转换后的明细项:', result)
      return result
    })

    const prescriptionData = {
      patientName: prescriptionForm.patientName,
      patientAge: prescriptionForm.patientAge,
      patientGender: prescriptionForm.patientGender,
      doctorName: doctorName,
      department: prescriptionForm.department,
      createDate: new Date().toISOString().split('T')[0],
      status: '未审核',
      details: detailsForSubmit,
    }

    console.log('提交处方数据:', JSON.stringify(prescriptionData, null, 2))
    console.log('处方明细数量:', detailsForSubmit.length)
    console.log('处方明细详情:', detailsForSubmit)

    try {
      const response = await prescriptionAPI.createPrescription(prescriptionData)
      if (response.success) {
        ElMessage.success('处方创建成功')
        createDialogVisible.value = false
        // 重置到第一页并刷新列表
        pagination.page = 1
        await loadPrescriptions()
      } else {
        ElMessage.error(response.message || '创建处方失败')
      }
    } catch (error) {
      console.error('创建处方错误:', error)
      if (error.response && error.response.data) {
        ElMessage.error(error.response.data.message || '创建处方失败')
      } else {
        ElMessage.error('创建处方失败: ' + (error.message || '未知错误'))
      }
    } finally {
      submitting.value = false
    }
  } catch (error) {
    console.error('表单验证或其他错误:', error)
    if (error !== false) {
      ElMessage.error('创建处方失败: ' + (error.message || '未知错误'))
    }
    submitting.value = false
  }
}

const handleView = async (row) => {
  currentPrescription.value = row
  try {
    const response = await prescriptionAPI.getPrescription(row.id)
    if (response.success) {
      prescriptionDetails.value = response.data.details || []
    }

    const historyResponse = await prescriptionAPI.getAuditHistory(row.id)
    if (historyResponse.success) {
      auditHistory.value = historyResponse.data || []
    }

    detailDialogVisible.value = true
  } catch (error) {
    ElMessage.error('加载处方详情失败')
  }
}

const handleCancel = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要取消处方"${row.prescriptionNumber}"吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })

    const response = await prescriptionAPI.cancelPrescription(row.id)
    if (response.success) {
      ElMessage.success('取消成功')
      loadPrescriptions()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('取消失败')
    }
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
  if (!printContent) return

  // 创建打印内容的HTML
  const printHTML = `
    <!DOCTYPE html>
    <html>
      <head>
        <meta charset="UTF-8">
        <title>处方详情 - ${currentPrescription.value?.prescriptionNumber || ''}</title>
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
          .prescription-info {
            margin: 20px 0;
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
          .prescription-info table td:first-child {
            background-color: #f5f7fa;
            font-weight: bold;
            width: 120px;
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
          .print-footer .doctor-sign {
            margin-bottom: 50px;
          }
          .no-print {
            display: none !important;
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
          <div class="doctor-sign">
            <div>医生签名：${currentPrescription.value?.doctorName || ''}</div>
            <div style="margin-top: 20px;">日期：${formatDate(currentPrescription.value?.createDate) || ''}</div>
          </div>
        </div>
      </body>
    </html>
  `

  const printWindow = window.open('', '_blank')
  printWindow.document.write(printHTML)
  printWindow.document.close()
  
  // 等待内容加载后打印
  printWindow.onload = () => {
    setTimeout(() => {
      printWindow.focus()
      printWindow.print()
      // 打印后可以选择关闭窗口
      // printWindow.close()
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

.medicine-detail-item {
  margin-bottom: 10px;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
}
</style>

