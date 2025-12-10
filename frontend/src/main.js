/*
TODO: 易瑜需要实现的全局配置和状态管理
=========================================

状态管理 Stores (需要创建):
TODO: stores/auth.js - 用户认证状态管理
  - 用户登录状态
  - JWT token管理
  - 用户信息存储
  - 登录登出逻辑

TODO: stores/medicine.js - 药品管理状态
  - 药品列表数据
  - 药品搜索筛选
  - 药品缓存管理

TODO: stores/inventory.js - 库存管理状态
  - 库存数据管理
  - 入库出库操作
  - 库存预警逻辑

TODO: stores/prescription.js - 处方管理状态 ⭐⭐⭐
  - 处方列表数据
  - 处方录入表单状态
  - 审核结果管理
  - 处方缓存和同步

TODO: stores/user.js - 用户管理状态
  - 用户列表管理
  - 角色权限管理
  - 用户操作状态

全局配置 (需要完善):
TODO: utils/request.js - HTTP请求封装
  - Axios实例配置
  - 请求拦截器 (JWT认证)
  - 响应拦截器 (错误处理)
  - 请求取消功能

TODO: utils/auth.js - 认证工具函数
  - token存储获取
  - 权限检查函数
  - 路由守卫逻辑

TODO: utils/permission.js - 权限控制
  - 角色权限验证
  - 页面权限检查
  - 按钮级权限控制

TODO: utils/format.js - 数据格式化工具
  - 日期时间格式化
  - 数字金额格式化
  - 药品单位转换

TODO: utils/validation.js - 表单验证规则
  - 通用验证规则
  - 业务特定验证
  - 自定义验证函数

路由配置 (需要完善):
TODO: router/guards.js - 路由守卫
  - 登录状态检查
  - 权限路由控制
  - 页面标题设置

TODO: router/routes.js - 路由定义
  - 所有页面路由配置
  - 嵌套路由设置
  - 路由元信息 (权限、标题等)

样式和主题 (需要实现):
TODO: styles/variables.scss - 样式变量
TODO: styles/mixins.scss - 样式混入
TODO: styles/theme.scss - 主题配置

作者: 易瑜 (前端负责人)
*/

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

import App from './App.vue'
import router from './router'

const app = createApp(App)

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(createPinia())
app.use(router)
app.use(ElementPlus)

app.mount('#app')
