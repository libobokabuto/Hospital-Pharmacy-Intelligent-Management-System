/*
TODO: 易瑜需要完善的API接口配置
==============================

需要完善的API模块:
TODO: api/auth.js - 用户认证相关API
  - login() - 用户登录
  - logout() - 用户登出
  - refresh() - 令牌刷新
  - getCurrentUser() - 获取当前用户信息
  - updateProfile() - 更新用户信息
  - changePassword() - 修改密码

TODO: api/medicine.js - 药品管理API ⭐⭐⭐
  - getMedicines() - 获取药品列表 (分页、筛选、搜索)
  - getMedicine(id) - 获取药品详情
  - createMedicine() - 创建药品
  - updateMedicine() - 更新药品
  - deleteMedicine() - 删除药品
  - searchMedicines() - 药品搜索
  - getMedicineCategories() - 获取药品分类
  - uploadMedicineImage() - 上传药品图片

TODO: api/inventory.js - 库存管理API ⭐⭐⭐
  - getInventory() - 获取库存列表
  - getLowStockAlerts() - 获取库存预警
  - stockIn() - 药品入库
  - stockOut() - 药品出库
  - getStockHistory() - 库存操作历史
  - getStockStatistics() - 库存统计数据
  - batchStockIn() - 批量入库

TODO: api/prescription.js - 处方管理API ⭐⭐⭐
  - getPrescriptions() - 获取处方列表 (分页、筛选)
  - getPrescription(id) - 获取处方详情
  - createPrescription() - 创建处方
  - updatePrescription() - 更新处方
  - deletePrescription() - 删除处方
  - auditPrescription() - 审核处方
  - getPrescriptionAuditHistory() - 获取审核历史
  - dispensePrescription() - 发药
  - printPrescription() - 打印处方

TODO: api/audit.js - 审核服务API ⭐⭐⭐
  - auditPrescription() - 单个处方审核
  - batchAuditPrescriptions() - 批量审核
  - getAuditHistory() - 审核历史查询
  - getAuditStatistics() - 审核统计
  - getAuditRules() - 获取审核规则
  - updateAuditRules() - 更新审核规则

TODO: api/user.js - 用户管理API
  - getUsers() - 获取用户列表 (管理员)
  - getUser(id) - 获取用户详情
  - createUser() - 创建用户
  - updateUser() - 更新用户
  - deleteUser() - 删除用户
  - updateUserRole() - 更新用户角色
  - resetUserPassword() - 重置密码

TODO: api/statistics.js - 统计报表API
  - getDashboardStats() - 仪表板统计
  - getPrescriptionStats() - 处方统计
  - getInventoryStats() - 库存统计
  - getAuditStats() - 审核统计
  - exportReport() - 导出报表

API工具函数 (需要完善):
TODO: utils/request.js - 请求工具函数
  - 统一错误处理
  - 请求取消功能
  - 重试机制
  - 缓存机制

TODO: utils/api-helpers.js - API辅助函数
  - 参数序列化
  - 响应数据转换
  - 分页参数处理
  - 文件下载处理

类型定义 (需要创建):
TODO: types/api.ts - API类型定义
  - 请求响应类型
  - 数据模型类型
  - 错误类型定义

测试 (需要实现):
TODO: tests/api/ - API测试文件
  - 单元测试
  - 集成测试
  - Mock数据

作者: 易瑜 (前端负责人)
*/

import axios from 'axios'

// 创建axios实例
const api = axios.create({
  baseURL: '/api',
  timeout: 10000,
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
    return response.data
  },
  (error) => {
    if (error.response) {
      const { status, data } = error.response

      // 处理常见HTTP状态码
      switch (status) {
        case 401:
          // 未授权，清除token并跳转到登录页
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
          ElMessage.error(data.message || '请求失败')
      }
    } else {
      ElMessage.error('网络错误，请检查网络连接')
    }

    return Promise.reject(error)
  }
)

// 用户相关API
export const userAPI = {
  login: (data) => api.post('/auth/login', data),
  logout: () => api.post('/auth/logout'),
  getUserInfo: () => api.get('/auth/user'),
  updatePassword: (data) => api.put('/auth/password', data),
}

// 药品管理API
export const medicineAPI = {
  getMedicines: (params) => api.get('/medicines', { params }),
  getMedicine: (id) => api.get(`/medicines/${id}`),
  createMedicine: (data) => api.post('/medicines', data),
  updateMedicine: (id, data) => api.put(`/medicines/${id}`, data),
  deleteMedicine: (id) => api.delete(`/medicines/${id}`),
}

// 库存管理API
export const inventoryAPI = {
  getInventory: (params) => api.get('/inventory', { params }),
  stockIn: (data) => api.post('/inventory/stock-in', data),
  stockOut: (data) => api.post('/inventory/stock-out', data),
  getStockHistory: (params) => api.get('/inventory/history', { params }),
}

// 处方管理API
export const prescriptionAPI = {
  getPrescriptions: (params) => api.get('/prescriptions', { params }),
  getPrescription: (id) => api.get(`/prescriptions/${id}`),
  createPrescription: (data) => api.post('/prescriptions', data),
  updatePrescription: (id, data) => api.put(`/prescriptions/${id}`, data),
  auditPrescription: (id, data) => api.post(`/prescriptions/${id}/audit`, data),
  dispensePrescription: (id, data) => api.post(`/prescriptions/${id}/dispense`, data),
}

// 审核服务API
export const auditAPI = {
  auditPrescription: (data) => axios.post('/audit/prescription/audit', data, {
    headers: { 'Content-Type': 'application/json' }
  }),
  getAuditRules: () => axios.get('/audit/rules'),
}

export default api
