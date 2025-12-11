<template>
  <Layout>
    <div class="inventory-management">
      <div class="page-header">
        <h2>库存管理</h2>
        <div>
          <el-button type="primary" @click="showStockInDialog = true">
            <el-icon><Plus /></el-icon>
            药品入库
          </el-button>
          <el-button type="warning" @click="showStockOutDialog = true">
            <el-icon><Minus /></el-icon>
            药品出库
          </el-button>
        </div>
      </div>

      <!-- 统计卡片 -->
      <el-row :gutter="20" class="stats-row">
        <el-col :span="6">
          <el-card shadow="hover">
            <div class="stat-item">
              <div class="stat-label">药品总数</div>
              <div class="stat-value">{{ statistics.totalMedicines }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover">
            <div class="stat-item">
              <div class="stat-label">库存预警</div>
              <div class="stat-value warning">{{ statistics.lowStockCount }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover">
            <div class="stat-item">
              <div class="stat-label">缺货数量</div>
              <div class="stat-value danger">{{ statistics.outOfStockCount }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover">
            <div class="stat-item">
              <div class="stat-label">库存总值</div>
              <div class="stat-value">¥{{ formatMoney(statistics.totalStockValue) }}</div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 标签页 -->
      <el-tabs v-model="activeTab" class="tabs-container">
        <el-tab-pane label="入库记录" name="in">
          <el-card shadow="never">
            <el-table
              v-loading="loadingIn"
              :data="stockInList"
              style="width: 100%"
              stripe
            >
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column prop="medicineId" label="药品ID" width="100" />
              <el-table-column prop="batchNumber" label="批号" width="120" />
              <el-table-column prop="quantity" label="数量" width="100" />
              <el-table-column prop="supplier" label="供应商" min-width="150" />
              <el-table-column prop="operator" label="操作人" width="120" />
              <el-table-column prop="inDate" label="入库日期" width="120">
                <template #default="{ row }">
                  {{ formatDate(row.inDate) }}
                </template>
              </el-table-column>
              <el-table-column prop="createTime" label="创建时间" width="180">
                <template #default="{ row }">
                  {{ formatDateTime(row.createTime) }}
                </template>
              </el-table-column>
            </el-table>
            <div class="pagination-container">
              <el-pagination
                v-model:current-page="paginationIn.page"
                v-model:page-size="paginationIn.size"
                :total="paginationIn.total"
                layout="total, prev, pager, next"
                @current-change="loadStockIn"
              />
            </div>
          </el-card>
        </el-tab-pane>

        <el-tab-pane label="出库记录" name="out">
          <el-card shadow="never">
            <el-table
              v-loading="loadingOut"
              :data="stockOutList"
              style="width: 100%"
              stripe
            >
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column prop="medicineId" label="药品ID" width="100" />
              <el-table-column prop="batchNumber" label="批号" width="120" />
              <el-table-column prop="quantity" label="数量" width="100" />
              <el-table-column prop="reason" label="出库原因" min-width="150" />
              <el-table-column prop="operator" label="操作人" width="120" />
              <el-table-column prop="outDate" label="出库日期" width="120">
                <template #default="{ row }">
                  {{ formatDate(row.outDate) }}
                </template>
              </el-table-column>
              <el-table-column prop="createTime" label="创建时间" width="180">
                <template #default="{ row }">
                  {{ formatDateTime(row.createTime) }}
                </template>
              </el-table-column>
            </el-table>
            <div class="pagination-container">
              <el-pagination
                v-model:current-page="paginationOut.page"
                v-model:page-size="paginationOut.size"
                :total="paginationOut.total"
                layout="total, prev, pager, next"
                @current-change="loadStockOut"
              />
            </div>
          </el-card>
        </el-tab-pane>
      </el-tabs>

      <!-- 入库对话框 -->
      <el-dialog v-model="showStockInDialog" title="药品入库" width="500px">
        <el-form ref="stockInFormRef" :model="stockInForm" :rules="stockInRules" label-width="100px">
          <el-form-item label="药品ID" prop="medicineId">
            <el-input-number v-model="stockInForm.medicineId" :min="1" style="width: 100%" />
          </el-form-item>
          <el-form-item label="批号" prop="batchNumber">
            <el-input v-model="stockInForm.batchNumber" placeholder="请输入批号" />
          </el-form-item>
          <el-form-item label="数量" prop="quantity">
            <el-input-number v-model="stockInForm.quantity" :min="1" style="width: 100%" />
          </el-form-item>
          <el-form-item label="供应商" prop="supplier">
            <el-input v-model="stockInForm.supplier" placeholder="请输入供应商" />
          </el-form-item>
          <el-form-item label="入库日期" prop="inDate">
            <el-date-picker
              v-model="stockInForm.inDate"
              type="date"
              placeholder="选择日期"
              style="width: 100%"
              value-format="YYYY-MM-DD"
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="showStockInDialog = false">取消</el-button>
          <el-button type="primary" @click="handleStockIn" :loading="submitting">
            确定
          </el-button>
        </template>
      </el-dialog>

      <!-- 出库对话框 -->
      <el-dialog v-model="showStockOutDialog" title="药品出库" width="500px">
        <el-form ref="stockOutFormRef" :model="stockOutForm" :rules="stockOutRules" label-width="100px">
          <el-form-item label="药品ID" prop="medicineId">
            <el-input-number v-model="stockOutForm.medicineId" :min="1" style="width: 100%" />
          </el-form-item>
          <el-form-item label="批号" prop="batchNumber">
            <el-input v-model="stockOutForm.batchNumber" placeholder="请输入批号" />
          </el-form-item>
          <el-form-item label="数量" prop="quantity">
            <el-input-number v-model="stockOutForm.quantity" :min="1" style="width: 100%" />
          </el-form-item>
          <el-form-item label="出库原因" prop="reason">
            <el-input
              v-model="stockOutForm.reason"
              type="textarea"
              :rows="3"
              placeholder="请输入出库原因"
            />
          </el-form-item>
          <el-form-item label="出库日期" prop="outDate">
            <el-date-picker
              v-model="stockOutForm.outDate"
              type="date"
              placeholder="选择日期"
              style="width: 100%"
              value-format="YYYY-MM-DD"
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="showStockOutDialog = false">取消</el-button>
          <el-button type="primary" @click="handleStockOut" :loading="submitting">
            确定
          </el-button>
        </template>
      </el-dialog>
    </div>
  </Layout>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import Layout from '@/components/Layout.vue'
import { stockAPI } from '@/api'
import { ElMessage } from 'element-plus'
import { Plus, Minus } from '@element-plus/icons-vue'
import { formatMoney, formatDate } from '@/utils/format'

const activeTab = ref('in')
const loadingIn = ref(false)
const loadingOut = ref(false)
const submitting = ref(false)
const showStockInDialog = ref(false)
const showStockOutDialog = ref(false)
const stockInFormRef = ref()
const stockOutFormRef = ref()

const statistics = reactive({
  totalMedicines: 0,
  lowStockCount: 0,
  outOfStockCount: 0,
  totalStockValue: 0,
})

const paginationIn = reactive({
  page: 1,
  size: 10,
  total: 0,
})

const paginationOut = reactive({
  page: 1,
  size: 10,
  total: 0,
})

const stockInList = ref([])
const stockOutList = ref([])

const stockInForm = reactive({
  medicineId: null,
  batchNumber: '',
  quantity: 1,
  supplier: '',
  inDate: new Date().toISOString().split('T')[0],
})

const stockOutForm = reactive({
  medicineId: null,
  batchNumber: '',
  quantity: 1,
  reason: '',
  outDate: new Date().toISOString().split('T')[0],
})

const stockInRules = {
  medicineId: [{ required: true, message: '请输入药品ID', trigger: 'blur' }],
  quantity: [{ required: true, message: '请输入数量', trigger: 'blur' }],
}

const stockOutRules = {
  medicineId: [{ required: true, message: '请输入药品ID', trigger: 'blur' }],
  quantity: [{ required: true, message: '请输入数量', trigger: 'blur' }],
  reason: [{ required: true, message: '请输入出库原因', trigger: 'blur' }],
}

const formatDateTime = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleString('zh-CN')
}

const loadStatistics = async () => {
  try {
    const response = await stockAPI.getStockStatistics()
    if (response.success) {
      Object.assign(statistics, response.data)
    }
  } catch (error) {
    console.error('Load statistics error:', error)
  }
}

const loadStockIn = async () => {
  loadingIn.value = true
  try {
    const response = await stockAPI.getStockInRecords({
      page: paginationIn.page - 1,
      size: paginationIn.size,
    })
    if (response.success) {
      stockInList.value = response.data.content || []
      paginationIn.total = response.data.totalElements || 0
    }
  } catch (error) {
    ElMessage.error('加载入库记录失败')
  } finally {
    loadingIn.value = false
  }
}

const loadStockOut = async () => {
  loadingOut.value = true
  try {
    const response = await stockAPI.getStockOutRecords({
      page: paginationOut.page - 1,
      size: paginationOut.size,
    })
    if (response.success) {
      stockOutList.value = response.data.content || []
      paginationOut.total = response.data.totalElements || 0
    }
  } catch (error) {
    ElMessage.error('加载出库记录失败')
  } finally {
    loadingOut.value = false
  }
}

const handleStockIn = async () => {
  if (!stockInFormRef.value) return

  try {
    await stockInFormRef.value.validate()
    submitting.value = true

    const response = await stockAPI.stockIn(stockInForm)
    if (response.success) {
      ElMessage.success('入库成功')
      showStockInDialog.value = false
      Object.assign(stockInForm, {
        medicineId: null,
        batchNumber: '',
        quantity: 1,
        supplier: '',
        inDate: new Date().toISOString().split('T')[0],
      })
      loadStockIn()
      loadStatistics()
    }
  } catch (error) {
    ElMessage.error('入库失败')
  } finally {
    submitting.value = false
  }
}

const handleStockOut = async () => {
  if (!stockOutFormRef.value) return

  try {
    await stockOutFormRef.value.validate()
    submitting.value = true

    const response = await stockAPI.stockOut(stockOutForm)
    if (response.success) {
      ElMessage.success('出库成功')
      showStockOutDialog.value = false
      Object.assign(stockOutForm, {
        medicineId: null,
        batchNumber: '',
        quantity: 1,
        reason: '',
        outDate: new Date().toISOString().split('T')[0],
      })
      loadStockOut()
      loadStatistics()
    }
  } catch (error) {
    ElMessage.error('出库失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadStatistics()
  loadStockIn()
  loadStockOut()
})
</script>

<style scoped>
.inventory-management {
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

.stat-value.danger {
  color: #F56C6C;
}

.tabs-container {
  margin-top: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>

