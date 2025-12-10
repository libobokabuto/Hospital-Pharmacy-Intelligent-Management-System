/*
TODO: 易瑜需要实现的完整路由配置
================================

需要创建的页面组件文件:
TODO: views/doctor/Dashboard.vue - 医生主页 ⭐⭐⭐
TODO: views/nurse/Dashboard.vue - 护士主页
TODO: views/pharmacist/Dashboard.vue - 药剂师主页
TODO: views/admin/MedicineManagement.vue - 药品管理页面
TODO: views/admin/InventoryManagement.vue - 库存管理页面
TODO: views/admin/UserManagement.vue - 用户管理页面
TODO: views/pharmacist/PrescriptionManagement.vue - 处方管理页面

处方管理详细路由 (最重要):
TODO: views/pharmacist/prescription/
  - List.vue - 处方列表页面
  - Create.vue - 新建处方页面 ⭐⭐⭐
  - Detail.vue - 处方详情页面
  - Edit.vue - 编辑处方页面

药品管理详细路由:
TODO: views/admin/medicine/
  - List.vue - 药品列表
  - Create.vue - 新增药品
  - Edit.vue - 编辑药品
  - Detail.vue - 药品详情
  - Import.vue - 批量导入

库存管理详细路由:
TODO: views/admin/inventory/
  - Dashboard.vue - 库存概览
  - StockIn.vue - 入库管理
  - StockOut.vue - 出库管理
  - Alerts.vue - 库存预警
  - History.vue - 库存历史

用户管理详细路由:
TODO: views/admin/user/
  - List.vue - 用户列表
  - Create.vue - 创建用户
  - Edit.vue - 编辑用户
  - Roles.vue - 角色管理
  - Permissions.vue - 权限设置

医生工作台路由:
TODO: views/doctor/
  - Dashboard.vue - 工作台概览 ⭐⭐⭐
  - Patients.vue - 患者管理
  - Prescriptions.vue - 我的处方
  - Templates.vue - 处方模板

护士工作台路由:
TODO: views/nurse/
  - Dashboard.vue - 工作台概览
  - Patients.vue - 患者管理
  - Tasks.vue - 待办任务

药剂师工作台路由:
TODO: views/pharmacist/
  - Dashboard.vue - 工作台概览 ⭐⭐⭐
  - Prescriptions.vue - 处方审核 ⭐⭐⭐
  - Inventory.vue - 库存管理
  - Reports.vue - 审核报告

通用页面:
TODO: views/common/
  - NotFound.vue - 404页面
  - Forbidden.vue - 403页面
  - Loading.vue - 加载页面
  - Error.vue - 错误页面

路由守卫增强:
TODO: router/guards.js - 路由守卫逻辑
  - 更完善的权限检查
  - 路由懒加载优化
  - 页面访问日志
  - 异常路由处理

作者: 易瑜 (前端负责人)
*/

import { createRouter, createWebHistory } from 'vue-router'

// 懒加载组件
const Login = () => import('@/views/Login.vue')
const AdminDashboard = () => import('@/views/admin/Dashboard.vue')
const DoctorDashboard = () => import('@/views/doctor/Dashboard.vue')
const NurseDashboard = () => import('@/views/nurse/Dashboard.vue')
const PharmacistDashboard = () => import('@/views/pharmacist/Dashboard.vue')

// 药品管理相关
const MedicineManagement = () => import('@/views/admin/MedicineManagement.vue')
const InventoryManagement = () => import('@/views/admin/InventoryManagement.vue')
const PrescriptionManagement = () => import('@/views/pharmacist/PrescriptionManagement.vue')
const UserManagement = () => import('@/views/admin/UserManagement.vue')

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { title: '登录' }
  },
  // 管理员路由
  {
    path: '/admin',
    name: 'AdminDashboard',
    component: AdminDashboard,
    meta: { title: '管理员主页', requiresAuth: true, role: 'admin' },
    children: [
      {
        path: 'users',
        name: 'UserManagement',
        component: UserManagement,
        meta: { title: '用户管理' }
      },
      {
        path: 'medicines',
        name: 'MedicineManagement',
        component: MedicineManagement,
        meta: { title: '药品管理' }
      },
      {
        path: 'inventory',
        name: 'InventoryManagement',
        component: InventoryManagement,
        meta: { title: '库存管理' }
      }
    ]
  },
  // 医生路由
  {
    path: '/doctor',
    name: 'DoctorDashboard',
    component: DoctorDashboard,
    meta: { title: '医生主页', requiresAuth: true, role: 'doctor' }
  },
  // 护士路由
  {
    path: '/nurse',
    name: 'NurseDashboard',
    component: NurseDashboard,
    meta: { title: '护士主页', requiresAuth: true, role: 'nurse' }
  },
  // 药剂师路由
  {
    path: '/pharmacist',
    name: 'PharmacistDashboard',
    component: PharmacistDashboard,
    meta: { title: '药剂师主页', requiresAuth: true, role: 'pharmacist' },
    children: [
      {
        path: 'prescriptions',
        name: 'PrescriptionManagement',
        component: PrescriptionManagement,
        meta: { title: '处方管理' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - 医院药房智能管理系统`
  }

  // 检查是否需要认证
  if (to.matched.some(record => record.meta.requiresAuth)) {
    const token = localStorage.getItem('token')
    const userRole = localStorage.getItem('userRole')

    if (!token) {
      next('/login')
      return
    }

    // 检查角色权限
    if (to.matched.some(record => record.meta.role && record.meta.role !== userRole)) {
      next('/login')
      return
    }
  }

  next()
})

export default router
