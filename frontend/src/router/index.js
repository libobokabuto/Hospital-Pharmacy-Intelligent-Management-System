import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

// 懒加载组件
const Login = () => import('@/views/Login.vue')
const Layout = () => import('@/components/Layout.vue')

// 管理员页面
const AdminDashboard = () => import('@/views/admin/Dashboard.vue')
const MedicineManagement = () => import('@/views/admin/MedicineManagement.vue')
const InventoryManagement = () => import('@/views/admin/InventoryManagement.vue')
const UserManagement = () => import('@/views/admin/UserManagement.vue')
const PrescriptionManagement = () => import('@/views/pharmacist/PrescriptionManagement.vue')

// 医生页面
const DoctorDashboard = () => import('@/views/doctor/Dashboard.vue')

// 护士页面
const NurseDashboard = () => import('@/views/nurse/Dashboard.vue')

// 药师页面
const PharmacistDashboard = () => import('@/views/pharmacist/Dashboard.vue')

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { title: '登录', requiresAuth: false }
  },
  // 管理员路由
  {
    path: '/admin',
    component: Layout,
    meta: { title: '管理员主页', requiresAuth: true, role: 'admin' },
    children: [
      {
        path: '',
        name: 'AdminDashboard',
        component: AdminDashboard,
        meta: { title: '系统概览' }
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
      },
      {
        path: 'users',
        name: 'UserManagement',
        component: UserManagement,
        meta: { title: '用户管理' }
      },
      {
        path: 'prescriptions',
        name: 'AdminPrescriptionManagement',
        component: PrescriptionManagement,
        meta: { title: '处方管理' }
      },
    ]
  },
  // 医生路由
  {
    path: '/doctor',
    component: Layout,
    meta: { title: '医生主页', requiresAuth: true, role: 'doctor' },
    children: [
      {
        path: '',
        name: 'DoctorDashboard',
        component: DoctorDashboard,
        meta: { title: '医生工作台' }
      },
    ]
  },
  // 护士路由
  {
    path: '/nurse',
    component: Layout,
    meta: { title: '护士主页', requiresAuth: true, role: 'nurse' },
    children: [
      {
        path: '',
        name: 'NurseDashboard',
        component: NurseDashboard,
        meta: { title: '护士工作台' }
      },
    ]
  },
  // 药剂师路由
  {
    path: '/pharmacist',
    component: Layout,
    meta: { title: '药剂师主页', requiresAuth: true, role: 'pharmacist' },
    children: [
      {
        path: '',
        name: 'PharmacistDashboard',
        component: PharmacistDashboard,
        meta: { title: '药师工作台' }
      },
      {
        path: 'prescriptions',
        name: 'PrescriptionManagement',
        component: PrescriptionManagement,
        meta: { title: '处方管理' }
      },
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
    const userStore = useUserStore()
    const token = localStorage.getItem('token')
    const userRole = localStorage.getItem('userRole')

    if (!token) {
      next('/login')
      return
    }

    // 初始化用户信息
    if (!userStore.userInfo) {
      userStore.initUser()
    }

    // 检查角色权限
    if (to.meta.role && to.meta.role !== userRole) {
      ElMessage.error('权限不足')
      next('/login')
      return
    }
  }

  // 如果已登录，访问登录页则跳转到对应角色首页
  if (to.path === '/login') {
    const token = localStorage.getItem('token')
    const userRole = localStorage.getItem('userRole')
    if (token && userRole) {
      const roleRoutes = {
        admin: '/admin',
        doctor: '/doctor',
        nurse: '/nurse',
        pharmacist: '/pharmacist',
      }
      next(roleRoutes[userRole] || '/login')
      return
    }
  }

  next()
})

export default router
