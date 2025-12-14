<template>
  <div class="user-management">
      <div class="page-header">
        <h2>用户管理</h2>
        <el-button type="primary" @click="handleCreate">
          <el-icon><Plus /></el-icon>
          新增用户
        </el-button>
      </div>

      <!-- 用户列表 -->
      <el-card shadow="never">
        <el-table
          v-loading="loading"
          :data="userList"
          style="width: 100%"
          stripe
        >
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="username" label="用户名" width="150" />
          <el-table-column prop="realName" label="真实姓名" width="120" />
          <el-table-column prop="employeeNumber" label="职工号" width="120" />
          <el-table-column prop="title" label="职称" width="120" />
          <el-table-column prop="role" label="角色" width="120">
            <template #default="{ row }">
              <el-tag :type="getRoleTagType(row.role)">
                {{ getRoleName(row.role) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="department" label="科室" width="150" />
          <el-table-column prop="createTime" label="创建时间" width="180">
            <template #default="{ row }">
              {{ formatDateTime(row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="250" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" size="small" @click="handleEdit(row)">
                编辑
              </el-button>
              <el-button type="warning" size="small" @click="handleChangeRole(row)">
                修改角色
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
        width="500px"
        @close="handleDialogClose"
      >
        <el-form
          ref="formRef"
          :model="formData"
          :rules="formRules"
          label-width="100px"
        >
          <el-form-item label="用户名" prop="username">
            <el-input
              v-model="formData.username"
              placeholder="请输入用户名"
              :disabled="!!formData.id"
            />
          </el-form-item>
          <el-form-item label="密码" prop="password" v-if="!formData.id">
            <el-input
              v-model="formData.password"
              type="password"
              placeholder="请输入密码"
            />
          </el-form-item>
          <el-form-item label="真实姓名" prop="realName">
            <el-input v-model="formData.realName" placeholder="请输入真实姓名" />
          </el-form-item>
          <el-form-item label="角色" prop="role">
            <el-select v-model="formData.role" placeholder="请选择角色" style="width: 100%">
              <el-option label="管理员" value="admin" />
              <el-option label="医生" value="doctor" />
              <el-option label="护士" value="nurse" />
              <el-option label="药师" value="pharmacist" />
            </el-select>
          </el-form-item>
          <el-form-item label="科室" prop="department">
            <el-input v-model="formData.department" placeholder="请输入科室" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            确定
          </el-button>
        </template>
      </el-dialog>

      <!-- 修改角色对话框 -->
      <el-dialog v-model="roleDialogVisible" title="修改角色" width="400px">
        <el-form label-width="100px">
          <el-form-item label="当前角色">
            <el-tag :type="getRoleTagType(currentUser.role)">
              {{ getRoleName(currentUser.role) }}
            </el-tag>
          </el-form-item>
          <el-form-item label="新角色">
            <el-select v-model="newRole" placeholder="请选择新角色" style="width: 100%">
              <el-option label="管理员" value="admin" />
              <el-option label="医生" value="doctor" />
              <el-option label="护士" value="nurse" />
              <el-option label="药师" value="pharmacist" />
            </el-select>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="roleDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleRoleSubmit" :loading="submitting">
            确定
          </el-button>
        </template>
      </el-dialog>
    </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { userAPI } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const roleDialogVisible = ref(false)
const dialogTitle = ref('新增用户')
const formRef = ref()

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0,
})

const userList = ref([])
const currentUser = ref({})
const newRole = ref('')

const formData = reactive({
  id: null,
  username: '',
  password: '',
  realName: '',
  employeeNumber: '',
  title: '',
  role: '',
  department: '',
})

const formRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }],
}

const formatDateTime = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleString('zh-CN')
}

const getRoleName = (role) => {
  const roleMap = {
    admin: '管理员',
    doctor: '医生',
    nurse: '护士',
    pharmacist: '药师',
  }
  return roleMap[role] || role
}

const getRoleTagType = (role) => {
  const typeMap = {
    admin: 'danger',
    doctor: 'success',
    nurse: 'warning',
    pharmacist: 'info',
  }
  return typeMap[role] || ''
}

const loadUsers = async () => {
  loading.value = true
  try {
    const response = await userAPI.getUsers({
      page: pagination.page - 1,
      size: pagination.size,
    })
    if (response.success) {
      userList.value = response.data.content || []
      pagination.total = response.data.totalElements || 0
    }
  } catch (error) {
    ElMessage.error('加载用户列表失败')
  } finally {
    loading.value = false
  }
}

const handleCreate = () => {
  dialogTitle.value = '新增用户'
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑用户'
  Object.assign(formData, {
    id: row.id,
    username: row.username,
    password: '',
    realName: row.realName,
    employeeNumber: row.employeeNumber || '',
    title: row.title || '',
    role: row.role,
    department: row.department,
  })
  dialogVisible.value = true
}

const handleChangeRole = (row) => {
  currentUser.value = row
  newRole.value = row.role
  roleDialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除用户"${row.username}"吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })

    const response = await userAPI.deleteUser(row.id)
    if (response.success) {
      ElMessage.success('删除成功')
      loadUsers()
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
    if (formData.id && !data.password) {
      delete data.password
    }

    let response
    if (formData.id) {
      response = await userAPI.updateUser(formData.id, data)
    } else {
      response = await userAPI.createUser(data)
    }

    if (response.success) {
      ElMessage.success(formData.id ? '更新成功' : '创建成功')
      dialogVisible.value = false
      loadUsers()
    }
  } catch (error) {
    if (error !== false) {
      ElMessage.error('操作失败')
    }
  } finally {
    submitting.value = false
  }
}

const handleRoleSubmit = async () => {
  if (!newRole.value) {
    ElMessage.warning('请选择新角色')
    return
  }

  submitting.value = true
  try {
    const response = await userAPI.updateUserRole(currentUser.value.id, newRole.value)
    if (response.success) {
      ElMessage.success('角色修改成功')
      roleDialogVisible.value = false
      loadUsers()
    }
  } catch (error) {
    ElMessage.error('角色修改失败')
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
    username: '',
    password: '',
    realName: '',
    employeeNumber: '',
    title: '',
    role: '',
    department: '',
  })
  formRef.value?.clearValidate()
}

const handlePageChange = (page) => {
  pagination.page = page
  loadUsers()
}

const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  loadUsers()
}

onMounted(() => {
  loadUsers()
})
</script>

<style scoped>
.user-management {
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

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>

