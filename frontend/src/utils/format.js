import { ElMessage } from 'element-plus'

/**
 * 格式化工具函数
 */

// 格式化日期
export const formatDate = (date, format = 'YYYY-MM-DD') => {
  if (!date) return ''

  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  const seconds = String(d.getSeconds()).padStart(2, '0')

  switch (format) {
    case 'YYYY-MM-DD':
      return `${year}-${month}-${day}`
    case 'YYYY-MM-DD HH:mm:ss':
      return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
    case 'YYYY/MM/DD':
      return `${year}/${month}/${day}`
    default:
      return `${year}-${month}-${day}`
  }
}

// 格式化金额
export const formatMoney = (amount) => {
  if (amount === null || amount === undefined) return '0.00'
  return Number(amount).toFixed(2)
}

// 格式化数量
export const formatQuantity = (quantity) => {
  if (quantity === null || quantity === undefined) return '0'
  return Number(quantity).toString()
}

// 格式化状态
export const formatStatus = (status, type = 'prescription') => {
  const statusMap = {
    prescription: {
      'pending': { text: '待审核', type: 'warning' },
      'approved': { text: '审核通过', type: 'success' },
      'rejected': { text: '审核驳回', type: 'danger' },
      'dispensed': { text: '已发药', type: 'success' },
      'returned': { text: '已退药', type: 'info' }
    },
    inventory: {
      'normal': { text: '正常', type: 'success' },
      'expired': { text: '过期', type: 'danger' },
      'depleted': { text: '停用', type: 'info' }
    }
  }

  const statusConfig = statusMap[type]?.[status]
  return statusConfig || { text: status, type: 'info' }
}

// 显示成功消息
export const showSuccess = (message) => {
  ElMessage.success(message)
}

// 显示错误消息
export const showError = (message) => {
  ElMessage.error(message)
}

// 显示警告消息
export const showWarning = (message) => {
  ElMessage.warning(message)
}

// 显示信息消息
export const showInfo = (message) => {
  ElMessage.info(message)
}

// 确认对话框
export const confirmAction = (message, title = '确认操作') => {
  return ElMessageBox.confirm(message, title, {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  })
}
