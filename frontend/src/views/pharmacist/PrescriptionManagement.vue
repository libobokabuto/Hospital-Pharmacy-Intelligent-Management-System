<template>
  <Layout>
    <div class="prescription-management">
      <div class="page-header">
        <h2>处方管理</h2>
        <el-button type="primary" @click="handleCreate" v-if="isDoctor">
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
          <el-table-column label="操作" width="300" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" size="small" @click="handleView(row)">
                查看
              </el-button>
              <el-button
                v-if="row.status === '未审核' && isPharmacist"
                type="success"
                size="small"
                @click="handleAudit(row)"
              >
                审核
              </el-button>
              <el-button
                v-if="row.status === '已通过' && isPharmacist"
                type="warning"
                size="small"
                @click="handleDispense(row)"
              >
                发药
              </el-button>
              <el-button
                v-if="row.status !== '已发药' && row.status !== '已取消'"
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

      <!-- 处方详情对话框 -->
      <el-dialog v-model="detailDialogVisible" title="处方详情" width="800px">
        <div v-if="currentPrescription">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="处方号">
              {{ currentPrescription.prescriptionNumber }}
            </el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="getStatusTagType(currentPrescription.status)">
                {{ currentPrescription.status }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="患者姓名">
              {{ currentPrescription.patientName }}
            </el-descriptions-item>
            <el-descriptions-item label="年龄">
              {{ currentPrescription.patientAge }}
            </el-descriptions-item>
            <el-descriptions-item label="性别">
              {{ currentPrescription.patientGender }}
            </el-descriptions-item>
            <el-descriptions-item label="医生">
              {{ currentPrescription.doctorName }}
            </el-descriptions-item>
            <el-descriptions-item label="科室">
              {{ currentPrescription.department }}
            </el-descriptions-item>
            <el-descriptions-item label="处方日期">
              {{ formatDate(currentPrescription.createDate) }}
            </el-descriptions-item>
            <el-descriptions-item label="审核结果" :span="2">
              {{ currentPrescription.auditResult || '暂无' }}
            </el-descriptions-item>
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

      <!-- 审核对话框 -->
      <el-dialog v-model="auditDialogVisible" title="处方审核" width="600px">
        <el-form ref="auditFormRef" :model="auditForm" :rules="auditRules" label-width="100px">
          <el-form-item label="审核结果" prop="auditResult">
            <el-select v-model="auditForm.auditResult" placeholder="请选择审核结果" style="width: 100%">
              <el-option label="通过" value="通过" />
              <el-option label="拒绝" value="拒绝" />
            </el-select>
          </el-form-item>
          <el-form-item label="审核得分" prop="auditScore">
            <el-input-number
              v-model="auditForm.auditScore"
              :min="0"
              :max="100"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="发现问题" prop="issuesFound">
            <el-input
              v-model="auditForm.issuesFound"
              type="textarea"
              :rows="3"
              placeholder="请输入发现的问题"
            />
          </el-form-item>
          <el-form-item label="建议" prop="suggestions">
            <el-input
              v-model="auditForm.suggestions"
              type="textarea"
              :rows="3"
              placeholder="请输入建议"
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="auditDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleAuditSubmit" :loading="submitting">
            确定
          </el-button>
        </template>
      </el-dialog>
    </div>
  </Layout>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import Layout from '@/components/Layout.vue'
import { prescriptionAPI } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { formatDate } from '@/utils/format'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const isDoctor = computed(() => userStore.hasRole('doctor') || userStore.hasRole('admin'))
const isPharmacist = computed(() => userStore.hasRole('pharmacist') || userStore.hasRole('admin'))

const loading = ref(false)
const submitting = ref(false)
const detailDialogVisible = ref(false)
const auditDialogVisible = ref(false)
const auditFormRef = ref()

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
const currentPrescription = ref(null)
const prescriptionDetails = ref([])
const auditHistory = ref([])

const auditForm = reactive({
  auditResult: '',
  auditScore: 100,
  issuesFound: '',
  suggestions: '',
})

const auditRules = {
  auditResult: [{ required: true, message: '请选择审核结果', trigger: 'change' }],
}

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

const handleCreate = () => {
  ElMessage.info('处方创建功能开发中，请使用API创建')
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

const handleAudit = (row) => {
  currentPrescription.value = row
  Object.assign(auditForm, {
    auditResult: '',
    auditScore: 100,
    issuesFound: '',
    suggestions: '',
  })
  auditDialogVisible.value = true
}

const handleAuditSubmit = async () => {
  if (!auditFormRef.value) return

  try {
    await auditFormRef.value.validate()
    submitting.value = true

    const response = await prescriptionAPI.auditPrescription(
      currentPrescription.value.id,
      auditForm
    )
    if (response.success) {
      ElMessage.success('审核完成')
      auditDialogVisible.value = false
      loadPrescriptions()
    }
  } catch (error) {
    if (error !== false) {
      ElMessage.error('审核失败')
    }
  } finally {
    submitting.value = false
  }
}

const handleDispense = async (row) => {
  try {
    await ElMessageBox.confirm('确定要发药吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })

    const response = await prescriptionAPI.dispensePrescription(row.id)
    if (response.success) {
      ElMessage.success('发药成功')
      loadPrescriptions()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('发药失败')
    }
  }
}

const handleCancel = async (row) => {
  try {
    await ElMessageBox.confirm('确定要取消该处方吗？', '提示', {
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

