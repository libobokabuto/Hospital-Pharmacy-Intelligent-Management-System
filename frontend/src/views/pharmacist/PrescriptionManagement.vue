<template>
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
          <el-table-column label="操作" width="400" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" size="small" @click="handleView(row)">
                查看
              </el-button>
              <el-button
                v-if="(row.status === '未审核' || row.status === '已拒绝') && isPharmacist"
                type="success"
                size="small"
                @click="handleAutoAudit(row)"
                :loading="autoAuditingId === row.id"
              >
                <el-icon><Refresh /></el-icon>
                自动审核
              </el-button>
              <el-button
                v-if="(row.status === '未审核' || row.status === '审核中' || row.status === '已通过') && isPharmacist"
                type="warning"
                size="small"
                @click="handleAudit(row)"
              >
                人工审核
              </el-button>
              <el-button
                v-if="row.status === '已通过' && isPharmacist"
                type="success"
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
        <template #header>
          <div style="display: flex; justify-content: space-between; align-items: center; width: 100%">
            <span>处方详情</span>
            <el-button type="primary" @click="handlePrint" :icon="Printer">打印</el-button>
          </div>
        </template>
        <div v-if="currentPrescription" id="prescription-print-content">
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
            <el-table-column prop="auditType" label="审核类型" width="120">
              <template #default="{ row }">
                <el-tag :type="row.auditType === 'auto' || row.auditType === '自动审核' ? 'info' : 'warning'">
                  {{ row.auditType === 'auto' ? '自动审核' : row.auditType === 'manual' ? '人工审核' : row.auditType }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="auditResult" label="审核结果" width="120">
              <template #default="{ row }">
                <el-tag :type="getAuditResultTagType(row.auditResult)">
                  {{ row.auditResult }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="auditScore" label="得分" width="100">
              <template #default="{ row }">
                <span style="font-weight: bold">{{ row.auditScore || 'N/A' }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="auditor" label="审核人" width="120" />
            <el-table-column prop="auditTime" label="审核时间" width="180">
              <template #default="{ row }">
                {{ formatDateTime(row.auditTime) }}
              </template>
            </el-table-column>
            <el-table-column label="详情" width="100">
              <template #default="{ row }">
                <el-button type="text" size="small" @click="showAuditDetail(row)">
                  查看详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-dialog>

      <!-- 自动审核结果对话框 -->
      <el-dialog v-model="autoAuditDialogVisible" title="自动审核结果" width="700px">
        <div v-if="autoAuditResult">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="审核类型">
              <el-tag type="info">{{ autoAuditResult.auditType || '自动审核' }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="审核结果">
              <el-tag :type="getAuditResultTagType(autoAuditResult.auditResult)">
                {{ autoAuditResult.auditResult }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="审核得分">
              <span style="font-size: 18px; font-weight: bold; color: #409EFF">
                {{ autoAuditResult.auditScore || 'N/A' }}
              </span>
            </el-descriptions-item>
            <el-descriptions-item label="审核时间">
              {{ formatDateTime(autoAuditResult.auditTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="审核人" :span="2">
              {{ autoAuditResult.auditor || '系统' }}
            </el-descriptions-item>
          </el-descriptions>

          <el-divider content-position="left">发现问题</el-divider>
          <div v-if="autoAuditResult.issuesFound" style="margin-bottom: 20px">
            <el-alert
              :title="autoAuditResult.issuesFound"
              :type="getAuditResultAlertType(autoAuditResult.auditResult)"
              :closable="false"
              show-icon
            />
          </div>
          <div v-else style="margin-bottom: 20px">
            <el-alert title="未发现问题" type="success" :closable="false" show-icon />
          </div>

          <el-divider content-position="left">审核建议</el-divider>
          <div v-if="autoAuditResult.suggestions" style="margin-bottom: 20px">
            <el-alert
              :title="autoAuditResult.suggestions"
              type="info"
              :closable="false"
              show-icon
            />
          </div>
          <div v-else>
            <el-alert title="无特殊建议" type="info" :closable="false" />
          </div>
        </div>
        <template #footer>
          <el-button type="primary" @click="autoAuditDialogVisible = false">确定</el-button>
          <el-button
            v-if="autoAuditResult && (autoAuditResult.auditResult === '拒绝' || autoAuditResult.auditResult === '警告')"
            type="warning"
            @click="handleAuditAfterAutoAudit"
          >
            进行人工审核
          </el-button>
        </template>
      </el-dialog>

      <!-- 审核对话框 -->
      <el-dialog v-model="auditDialogVisible" title="人工审核" width="600px">
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

      <!-- 审核详情对话框 -->
      <el-dialog v-model="auditDetailDialogVisible" title="审核详情" width="700px">
        <div v-if="currentAuditRecord">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="审核类型">
              <el-tag :type="currentAuditRecord.auditType === 'auto' || currentAuditRecord.auditType === '自动审核' ? 'info' : 'warning'">
                {{ currentAuditRecord.auditType === 'auto' ? '自动审核' : currentAuditRecord.auditType === 'manual' ? '人工审核' : currentAuditRecord.auditType }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="审核结果">
              <el-tag :type="getAuditResultTagType(currentAuditRecord.auditResult)">
                {{ currentAuditRecord.auditResult }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="审核得分">
              <span style="font-size: 18px; font-weight: bold; color: #409EFF">
                {{ currentAuditRecord.auditScore || 'N/A' }}
              </span>
            </el-descriptions-item>
            <el-descriptions-item label="审核时间">
              {{ formatDateTime(currentAuditRecord.auditTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="审核人" :span="2">
              {{ currentAuditRecord.auditor || '系统' }}
            </el-descriptions-item>
          </el-descriptions>

          <el-divider content-position="left">发现问题</el-divider>
          <div v-if="currentAuditRecord.issuesFound" style="margin-bottom: 20px">
            <el-alert
              :title="currentAuditRecord.issuesFound"
              :type="getAuditResultAlertType(currentAuditRecord.auditResult)"
              :closable="false"
              show-icon
            />
          </div>
          <div v-else style="margin-bottom: 20px">
            <el-alert title="未发现问题" type="success" :closable="false" show-icon />
          </div>

          <el-divider content-position="left">审核建议</el-divider>
          <div v-if="currentAuditRecord.suggestions" style="margin-bottom: 20px">
            <el-alert
              :title="currentAuditRecord.suggestions"
              type="info"
              :closable="false"
              show-icon
            />
          </div>
          <div v-else>
            <el-alert title="无特殊建议" type="info" :closable="false" />
          </div>
        </div>
        <template #footer>
          <el-button type="primary" @click="auditDetailDialogVisible = false">确定</el-button>
        </template>
      </el-dialog>
    </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { prescriptionAPI } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Printer, Refresh } from '@element-plus/icons-vue'
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
const autoAuditingId = ref(null)
const autoAuditResult = ref(null)
const autoAuditDialogVisible = ref(false)

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
const auditDetailDialogVisible = ref(false)
const currentAuditRecord = ref(null)

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

const getAuditResultTagType = (result) => {
  if (!result) return 'info'
  const resultLower = result.toLowerCase()
  if (resultLower.includes('通过') || resultLower === 'pass') return 'success'
  if (resultLower.includes('警告') || resultLower === 'warning') return 'warning'
  if (resultLower.includes('拒绝') || resultLower === 'reject') return 'danger'
  return 'info'
}

const getAuditResultAlertType = (result) => {
  if (!result) return 'info'
  const resultLower = result.toLowerCase()
  if (resultLower.includes('通过') || resultLower === 'pass') return 'success'
  if (resultLower.includes('警告') || resultLower === 'warning') return 'warning'
  if (resultLower.includes('拒绝') || resultLower === 'reject') return 'error'
  return 'info'
}

const handleAuditAfterAutoAudit = () => {
  autoAuditDialogVisible.value = false
  if (currentPrescription.value) {
    handleAudit(currentPrescription.value)
  }
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

const handleAutoAudit = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要对处方"${row.prescriptionNumber}"进行自动审核吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info',
      }
    )

    autoAuditingId.value = row.id
    try {
      const response = await prescriptionAPI.submitForAudit(row.id)
      if (response.success) {
        ElMessage.success('自动审核提交成功，正在审核中...')
        // 等待后刷新列表并加载审核结果
        setTimeout(async () => {
          await loadPrescriptions()
          await loadAutoAuditResult(row.id)
        }, 2000)
      } else {
        ElMessage.error(response.message || '提交自动审核失败')
      }
    } catch (error) {
      console.error('自动审核错误:', error)
      ElMessage.error('提交自动审核失败: ' + (error.message || '未知错误'))
    } finally {
      autoAuditingId.value = null
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('自动审核错误:', error)
    }
  }
}

const loadAutoAuditResult = async (prescriptionId) => {
  try {
    const historyResponse = await prescriptionAPI.getAuditHistory(prescriptionId)
    if (historyResponse.success && historyResponse.data && historyResponse.data.length > 0) {
      // 获取最新的自动审核记录
      const autoAuditRecord = historyResponse.data
        .filter(r => r.auditType === 'auto' || r.auditType === '自动审核')
        .sort((a, b) => {
          const timeA = new Date(a.auditTime || a.createTime || 0)
          const timeB = new Date(b.auditTime || b.createTime || 0)
          return timeB - timeA
        })[0]
      
      if (autoAuditRecord) {
        autoAuditResult.value = autoAuditRecord
        autoAuditDialogVisible.value = true
      }
    }
  } catch (error) {
    console.error('加载审核结果失败:', error)
  }
}

const handleAudit = async (row) => {
  currentPrescription.value = row
  
  // 先加载自动审核结果
  try {
    const historyResponse = await prescriptionAPI.getAuditHistory(row.id)
    if (historyResponse.success && historyResponse.data && historyResponse.data.length > 0) {
      const autoAuditRecord = historyResponse.data
        .filter(r => r.auditType === 'auto' || r.auditType === '自动审核')
        .sort((a, b) => {
          const timeA = new Date(a.auditTime || a.createTime || 0)
          const timeB = new Date(b.auditTime || b.createTime || 0)
          return timeB - timeA
        })[0]
      
      if (autoAuditRecord) {
        // 如果有自动审核结果，预填充到表单
        Object.assign(auditForm, {
          auditResult: autoAuditRecord.auditResult === '通过' ? '通过' : 
                      autoAuditRecord.auditResult === '拒绝' ? '拒绝' : '通过',
          auditScore: autoAuditRecord.auditScore || 100,
          issuesFound: autoAuditRecord.issuesFound || '',
          suggestions: autoAuditRecord.suggestions || '',
        })
      } else {
        Object.assign(auditForm, {
          auditResult: '',
          auditScore: 100,
          issuesFound: '',
          suggestions: '',
        })
      }
    } else {
      Object.assign(auditForm, {
        auditResult: '',
        auditScore: 100,
        issuesFound: '',
        suggestions: '',
      })
    }
  } catch (error) {
    console.error('加载自动审核结果失败:', error)
    Object.assign(auditForm, {
      auditResult: '',
      auditScore: 100,
      issuesFound: '',
      suggestions: '',
    })
  }
  
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

const showAuditDetail = (row) => {
  currentAuditRecord.value = row
  auditDetailDialogVisible.value = true
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
            <div>医生签名：${currentPrescription.value.doctorName || ''}</div>
            <div style="margin-top: 20px;">日期：${formatDate(currentPrescription.value.createDate) || ''}</div>
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

