import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建axios实例
const api = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// 请求拦截器
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
api.interceptors.response.use(
  (response) => {
    const data = response.data
    // 如果响应数据已经是处理过的格式（有success字段），直接返回
    if (data && typeof data.success !== 'undefined') {
      return data
    }
    // 否则包装成标准格式
    return { success: true, data: data }
  },
  (error) => {
    if (error.response) {
      const { status, data } = error.response

      switch (status) {
        case 401:
          localStorage.removeItem('token')
          localStorage.removeItem('userRole')
          window.location.href = '/login'
          break
        case 403:
          ElMessage.error('权限不足')
          break
        case 404:
          ElMessage.error('接口不存在')
          break
        case 500:
          ElMessage.error('服务器错误')
          break
        default:
          ElMessage.error(data?.message || '请求失败')
      }
    } else {
      ElMessage.error('网络错误，请检查网络连接')
    }

    return Promise.reject(error)
  }
)

// ==================== 认证相关API ====================
export const authAPI = {
  // 登录
  login: (data) => api.post('/auth/login', data),
  // 注册
  register: (data) => api.post('/auth/register', data),
  // 获取当前用户信息
  getCurrentUser: () => api.get('/auth/me'),
  // 刷新令牌
  refreshToken: (data) => api.post('/auth/refresh', data),
  // 登出
  logout: () => api.post('/auth/logout'),
}

// ==================== 用户管理API ====================
export const userAPI = {
  // 获取用户列表
  getUsers: (params) => api.get('/users', { params }),
  // 获取用户详情
  getUser: (id) => api.get(`/users/${id}`),
  // 创建用户
  createUser: (data) => api.post('/users', data),
  // 更新用户
  updateUser: (id, data) => api.put(`/users/${id}`, data),
  // 删除用户
  deleteUser: (id) => api.delete(`/users/${id}`),
  // 更新用户角色
  updateUserRole: (id, role) => api.put(`/users/${id}/role`, role),
}

// ==================== 药品管理API ====================
export const medicineAPI = {
  // 获取药品列表（分页、搜索、筛选）
  getMedicines: (params) => api.get('/medicines', { params }),
  // 获取药品详情
  getMedicine: (id) => api.get(`/medicines/${id}`),
  // 创建药品
  createMedicine: (data) => api.post('/medicines', data),
  // 更新药品
  updateMedicine: (id, data) => api.put(`/medicines/${id}`, data),
  // 删除药品
  deleteMedicine: (id) => api.delete(`/medicines/${id}`),
  // 搜索药品
  searchMedicines: (keyword) => api.get('/medicines/search', { params: { keyword } }),
  // 按分类查询
  getMedicinesByCategory: (category) => api.get(`/medicines/category/${category}`),
  // 查询低库存药品
  getLowStockMedicines: () => api.get('/medicines/low-stock'),
}

// ==================== 库存管理API ====================
export const stockAPI = {
  // 药品入库
  stockIn: (data) => api.post('/stock/in', data),
  // 药品出库
  stockOut: (data) => api.post('/stock/out', data),
  // 获取入库记录列表
  getStockInRecords: (params) => api.get('/stock/in', { params }),
  // 获取出库记录列表
  getStockOutRecords: (params) => api.get('/stock/out', { params }),
  // 获取入库记录详情
  getStockInById: (id) => api.get(`/stock/in/${id}`),
  // 获取出库记录详情
  getStockOutById: (id) => api.get(`/stock/out/${id}`),
  // 查询药品库存
  getMedicineStock: (medicineId) => api.get(`/stock/medicine/${medicineId}`),
  // 库存统计
  getStockStatistics: () => api.get('/stock/statistics'),
}

// ==================== 处方管理API ====================
export const prescriptionAPI = {
  // 创建处方
  createPrescription: (data, details) => {
    const requestData = { ...data, details }
    return api.post('/prescriptions', requestData)
  },
  // 获取处方列表（分页、筛选）
  getPrescriptions: (params) => api.get('/prescriptions', { params }),
  // 获取处方详情
  getPrescription: (id) => api.get(`/prescriptions/${id}`),
  // 更新处方
  updatePrescription: (id, data) => api.put(`/prescriptions/${id}`, data),
  // 提交审核
  submitForAudit: (id) => api.post(`/prescriptions/${id}/submit-audit`),
  // 人工审核
  auditPrescription: (id, data) => api.post(`/prescriptions/${id}/audit`, data),
  // 发药
  dispensePrescription: (id) => api.post(`/prescriptions/${id}/dispense`),
  // 取消处方
  cancelPrescription: (id) => api.post(`/prescriptions/${id}/cancel`),
  // 获取处方明细
  getPrescriptionDetails: (id) => api.get(`/prescriptions/${id}/details`),
  // 获取审核历史
  getAuditHistory: (id) => api.get(`/prescriptions/${id}/audit-history`),
}

// ==================== 审核记录API ====================
export const auditAPI = {
  // 获取审核记录列表
  getAuditRecords: (params) => api.get('/audit/records', { params }),
  // 获取审核记录详情
  getAuditRecord: (id) => api.get(`/audit/records/${id}`),
  // 获取处方的审核历史
  getPrescriptionAuditHistory: (prescriptionId) => api.get(`/audit/records/prescription/${prescriptionId}`),
  // 审核统计
  getAuditStatistics: () => api.get('/audit/statistics'),
}

export default api
