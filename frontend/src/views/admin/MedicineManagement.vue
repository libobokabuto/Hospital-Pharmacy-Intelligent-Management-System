<template>
  <Layout>
    <div class="medicine-management">
      <div class="page-header">
        <h2>药品管理</h2>
        <el-button type="primary" @click="handleCreate">
          <el-icon><Plus /></el-icon>
          新增药品
        </el-button>
      </div>

      <!-- 搜索和筛选 -->
      <el-card class="search-card" shadow="never">
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-form-item label="关键词">
            <el-input
              v-model="searchForm.keyword"
              placeholder="请输入药品名称或通用名"
              clearable
              style="width: 200px"
            />
          </el-form-item>
          <el-form-item label="分类">
            <el-input
              v-model="searchForm.category"
              placeholder="请输入分类"
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

      <!-- 药品列表 -->
      <el-card shadow="never">
        <el-table
          v-loading="loading"
          :data="medicineList"
          style="width: 100%"
          stripe
        >
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="name" label="药品名称" min-width="150" />
          <el-table-column prop="genericName" label="通用名" min-width="120" />
          <el-table-column prop="specification" label="规格" width="120" />
          <el-table-column prop="manufacturer" label="生产厂家" min-width="150" />
          <el-table-column prop="price" label="价格" width="100">
            <template #default="{ row }">
              ¥{{ formatMoney(row.price) }}
            </template>
          </el-table-column>
          <el-table-column prop="stockQuantity" label="库存数量" width="100" />
          <el-table-column prop="minStock" label="最低库存" width="100" />
          <el-table-column prop="category" label="分类" width="100" />
          <el-table-column label="库存状态" width="100">
            <template #default="{ row }">
              <el-tag
                v-if="row.minStock && row.stockQuantity <= row.minStock"
                type="warning"
              >
                库存不足
              </el-tag>
              <el-tag v-else type="success">正常</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" size="small" @click="handleEdit(row)">
                编辑
              </el-button>
              <el-button type="danger" size="small" @click="handleDelete(row)">
                删除
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

      <!-- 新增/编辑对话框 -->
      <el-dialog
        v-model="dialogVisible"
        :title="dialogTitle"
        width="600px"
        @close="handleDialogClose"
      >
        <el-form
          ref="formRef"
          :model="formData"
          :rules="formRules"
          label-width="100px"
        >
          <el-form-item label="药品名称" prop="name">
            <el-input v-model="formData.name" placeholder="请输入药品名称" />
          </el-form-item>
          <el-form-item label="通用名" prop="genericName">
            <el-input v-model="formData.genericName" placeholder="请输入通用名" />
          </el-form-item>
          <el-form-item label="规格" prop="specification">
            <el-input v-model="formData.specification" placeholder="请输入规格" />
          </el-form-item>
          <el-form-item label="生产厂家" prop="manufacturer">
            <el-input v-model="formData.manufacturer" placeholder="请输入生产厂家" />
          </el-form-item>
          <el-form-item label="价格" prop="price">
            <el-input-number
              v-model="formData.price"
              :precision="2"
              :min="0"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="库存数量" prop="stockQuantity">
            <el-input-number
              v-model="formData.stockQuantity"
              :min="0"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="最低库存" prop="minStock">
            <el-input-number
              v-model="formData.minStock"
              :min="0"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="分类" prop="category">
            <el-input v-model="formData.category" placeholder="请输入分类" />
          </el-form-item>
          <el-form-item label="批准文号" prop="approvalNumber">
            <el-input v-model="formData.approvalNumber" placeholder="请输入批准文号" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
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
import { medicineAPI } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { formatMoney } from '@/utils/format'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增药品')
const formRef = ref()

const searchForm = reactive({
  keyword: '',
  category: '',
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0,
})

const medicineList = ref([])

const formData = reactive({
  id: null,
  name: '',
  genericName: '',
  specification: '',
  manufacturer: '',
  price: 0,
  stockQuantity: 0,
  minStock: 10,
  category: '',
  approvalNumber: '',
})

const formRules = {
  name: [{ required: true, message: '请输入药品名称', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  stockQuantity: [{ required: true, message: '请输入库存数量', trigger: 'blur' }],
}

const loadMedicines = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page - 1,
      size: pagination.size,
    }
    if (searchForm.keyword) {
      params.keyword = searchForm.keyword
    }
    if (searchForm.category) {
      params.category = searchForm.category
    }

    const response = await medicineAPI.getMedicines(params)
    if (response.success) {
      medicineList.value = response.data.content || []
      pagination.total = response.data.totalElements || 0
    }
  } catch (error) {
    ElMessage.error('加载药品列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadMedicines()
}

const handleReset = () => {
  searchForm.keyword = ''
  searchForm.category = ''
  handleSearch()
}

const handleCreate = () => {
  dialogTitle.value = '新增药品'
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑药品'
  Object.assign(formData, {
    id: row.id,
    name: row.name,
    genericName: row.genericName,
    specification: row.specification,
    manufacturer: row.manufacturer,
    price: row.price,
    stockQuantity: row.stockQuantity,
    minStock: row.minStock,
    category: row.category,
    approvalNumber: row.approvalNumber,
  })
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除药品"${row.name}"吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })

    const response = await medicineAPI.deleteMedicine(row.id)
    if (response.success) {
      ElMessage.success('删除成功')
      loadMedicines()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    submitting.value = true

    const data = { ...formData }
    let response

    if (formData.id) {
      response = await medicineAPI.updateMedicine(formData.id, data)
    } else {
      response = await medicineAPI.createMedicine(data)
    }

    if (response.success) {
      ElMessage.success(formData.id ? '更新成功' : '创建成功')
      dialogVisible.value = false
      loadMedicines()
    }
  } catch (error) {
    if (error !== false) {
      ElMessage.error('操作失败')
    }
  } finally {
    submitting.value = false
  }
}

const handleDialogClose = () => {
  resetForm()
}

const resetForm = () => {
  Object.assign(formData, {
    id: null,
    name: '',
    genericName: '',
    specification: '',
    manufacturer: '',
    price: 0,
    stockQuantity: 0,
    minStock: 10,
    category: '',
    approvalNumber: '',
  })
  formRef.value?.clearValidate()
}

const handlePageChange = (page) => {
  pagination.page = page
  loadMedicines()
}

const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  loadMedicines()
}

onMounted(() => {
  loadMedicines()
})
</script>

<style scoped>
.medicine-management {
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

