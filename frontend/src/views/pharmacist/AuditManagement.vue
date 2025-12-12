<template>
  <div class="audit-management">
    <div class="page-header">
      <h2>审核记录</h2>
    </div>

    <!-- 搜索和筛选 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="审核类型">
          <el-select v-model="searchForm.auditType" placeholder="请选择类型" clearable style="width: 150px">
            <el-option label="自动审核" value="自动审核" />
            <el-option label="人工审核" value="人工审核" />
          </el-select>
        </el-form-item>
        <el-form-item label="审核结果">
          <el-select v-model="searchForm.auditResult" placeholder="请选择结果" clearable style="width: 150px">
            <el-option label="通过" value="通过" />
            <el-option label="拒绝" value="拒绝" />
            <el-option label="待审核" value="待审核" />
          </el-select>
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

    <!-- 审核记录列表 -->
    <el-card shadow="never">
      <el-table
        v-loading="loading"
        :data="auditList"
        style="width: 100%"
        stripe
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="prescriptionId" label="处方ID" width="100" />
        <el-table-column prop="auditType" label="审核类型" width="120" />
        <el-table-column prop="auditResult" label="审核结果" width="120">
          <template #default="{ row }">
            <el-tag :type="getResultTagType(row.auditResult)">
              {{ row.auditResult }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="auditScore" label="得分" width="100">
          <template #default="{ row }">
            {{ row.auditScore || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="auditor" label="审核人" width="120" />
        <el-table-column prop="auditTime" label="审核时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.auditTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="issuesFound" label="发现问题" min-width="200" show-overflow-tooltip />
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
    <el-dialog v-model="detailDialogVisible" title="审核记录详情" width="700px">
      <div v-if="currentRecord">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="记录ID">{{ currentRecord.id }}</el-descriptions-item>
          <el-descriptions-item label="处方ID">{{ currentRecord.prescriptionId }}</el-descriptions-item>
          <el-descriptions-item label="审核类型">{{ currentRecord.auditType }}</el-descriptions-item>
          <el-descriptions-item label="审核结果">
            <el-tag :type="getResultTagType(currentRecord.auditResult)">
              {{ currentRecord.auditResult }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="审核得分">{{ currentRecord.auditScore || '-' }}</el-descriptions-item>
          <el-descriptions-item label="审核人">{{ currentRecord.auditor }}</el-descriptions-item>
          <el-descriptions-item label="审核时间">
            {{ formatDateTime(currentRecord.auditTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ formatDateTime(currentRecord.createTime) }}
          </el-descriptions-item>
        </el-descriptions>

        <div style="margin-top: 20px">
          <h4>发现问题</h4>
          <p>{{ currentRecord.issuesFound || '无' }}</p>
        </div>

        <div style="margin-top: 20px">
          <h4>建议</h4>
          <p>{{ currentRecord.suggestions || '无' }}</p>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { auditAPI } from '@/api'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'

const loading = ref(false)
const detailDialogVisible = ref(false)
const currentRecord = ref(null)

const searchForm = reactive({
  auditType: '',
  auditResult: '',
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0,
})

const auditList = ref([])

const formatDateTime = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleString('zh-CN')
}

const getResultTagType = (result) => {
  const typeMap = {
    '通过': 'success',
    '拒绝': 'danger',
    '待审核': 'warning',
  }
  return typeMap[result] || ''
}

const loadAuditRecords = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page - 1,
      size: pagination.size,
    }
    if (searchForm.auditType) {
      params.auditType = searchForm.auditType
    }
    if (searchForm.auditResult) {
      params.auditResult = searchForm.auditResult
    }

    const response = await auditAPI.getAuditRecords(params)
    if (response.success) {
      auditList.value = response.data.content || []
      pagination.total = response.data.totalElements || 0
    }
  } catch (error) {
    ElMessage.error('加载审核记录失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadAuditRecords()
}

const handleReset = () => {
  searchForm.auditType = ''
  searchForm.auditResult = ''
  handleSearch()
}

const handleView = async (row) => {
  try {
    const response = await auditAPI.getAuditRecord(row.id)
    if (response.success) {
      currentRecord.value = response.data
      detailDialogVisible.value = true
    }
  } catch (error) {
    ElMessage.error('加载审核记录详情失败')
  }
}

const handlePageChange = (page) => {
  pagination.page = page
  loadAuditRecords()
}

const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  loadAuditRecords()
}

onMounted(() => {
  loadAuditRecords()
})
</script>

<style scoped>
.audit-management {
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

