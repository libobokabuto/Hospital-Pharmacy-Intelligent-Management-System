# 前端应用 (Frontend)

## 概述

HPIMS 前端应用基于 Vue 3 开发，采用现代化的前端技术栈，提供友好的用户界面和流畅的交互体验。

## 技术栈

- **框架**: Vue 3.4+
- **构建工具**: Vite 5.0+
- **UI组件库**: Element Plus 2.4+
- **状态管理**: Pinia 2.1+
- **路由管理**: Vue Router 4.2+
- **HTTP客户端**: Axios 1.6+
- **图表库**: ECharts 6.0+
- **图标库**: @element-plus/icons-vue

## 项目结构

```
frontend/
├── src/
│   ├── main.js                    # 应用入口文件
│   ├── App.vue                    # 根组件
│   ├── api/                       # API接口封装
│   │   └── index.js               # API请求配置和接口定义
│   ├── components/                # 公共组件
│   │   ├── Layout.vue             # 布局组件
│   │   └── FileUpload.vue         # 文件上传组件
│   ├── views/                     # 页面组件
│   │   ├── Login.vue              # 登录页面
│   │   ├── admin/                 # 管理员页面
│   │   │   ├── Dashboard.vue      # 系统概览
│   │   │   ├── MedicineManagement.vue # 药品管理
│   │   │   ├── InventoryManagement.vue # 库存管理
│   │   │   ├── UserManagement.vue # 用户管理
│   │   │   ├── AuditManagement.vue # 审核记录
│   │   │   └── SystemSettings.vue # 系统设置
│   │   ├── doctor/                # 医生页面
│   │   │   ├── Dashboard.vue      # 医生工作台
│   │   │   └── DoctorPrescriptionManagement.vue # 我的处方
│   │   ├── nurse/                 # 护士页面
│   │   │   ├── Dashboard.vue      # 护士工作台
│   │   │   └── NursePrescriptionManagement.vue # 处方查询
│   │   └── pharmacist/           # 药师页面
│   │       ├── Dashboard.vue      # 药师工作台
│   │       ├── PrescriptionManagement.vue # 处方管理
│   │       ├── PharmacistInventoryManagement.vue # 库存管理
│   │       └── AuditManagement.vue # 审核记录
│   ├── router/                    # 路由配置
│   │   └── index.js               # 路由定义和守卫
│   ├── stores/                    # 状态管理 (Pinia)
│   │   └── user.js                # 用户状态管理
│   └── utils/                     # 工具函数
│       ├── auth.js                # 认证工具
│       └── format.js              # 格式化工具
├── index.html                     # HTML模板
├── vite.config.js                 # Vite配置文件
├── package.json                   # 项目依赖配置
└── README.md                      # 本文档
```

## 核心功能模块

### 1. 用户认证

- **登录页面**: 用户名密码登录，支持记住密码
- **JWT Token管理**: 自动存储和刷新Token
- **路由守卫**: 基于Token和角色的路由权限控制
- **自动登出**: Token过期自动跳转登录页

**主要页面**:
- `Login.vue` - 登录页面

### 2. 管理员功能

管理员拥有系统所有功能的访问权限。

**主要页面**:
- **系统概览** (`admin/Dashboard.vue`): 系统统计数据、图表展示
- **药品管理** (`admin/MedicineManagement.vue`): 药品CRUD、搜索、分类管理
- **库存管理** (`admin/InventoryManagement.vue`): 入库出库记录、库存查询
- **用户管理** (`admin/UserManagement.vue`): 用户CRUD、角色管理
- **审核记录** (`admin/AuditManagement.vue`): 审核历史查询、统计分析
- **系统设置** (`admin/SystemSettings.vue`): 系统参数配置

### 3. 医生功能

医生主要负责处方创建和管理。

**主要页面**:
- **医生工作台** (`doctor/Dashboard.vue`): 个人工作统计
- **我的处方** (`doctor/DoctorPrescriptionManagement.vue`): 创建处方、查看我的处方、提交审核

### 4. 护士功能

护士主要负责处方查询和信息录入。

**主要页面**:
- **护士工作台** (`nurse/Dashboard.vue`): 工作统计
- **处方查询** (`nurse/NursePrescriptionManagement.vue`): 查询处方、查看详情

### 5. 药师功能

药师负责处方审核、库存管理和发药。

**主要页面**:
- **药师工作台** (`pharmacist/Dashboard.vue`): 工作统计、待审核处方
- **处方管理** (`pharmacist/PrescriptionManagement.vue`): 审核处方、发药、查看处方详情
- **库存管理** (`pharmacist/PharmacistInventoryManagement.vue`): 库存查询、入库出库
- **审核记录** (`pharmacist/AuditManagement.vue`): 审核历史、审核统计

## 路由配置

### 路由结构

```javascript
/                    → 重定向到 /login
/login               → 登录页面
/admin               → 管理员主页
  ├── /              → 系统概览
  ├── /medicines     → 药品管理
  ├── /inventory     → 库存管理
  ├── /users         → 用户管理
  ├── /prescriptions → 处方管理
  ├── /audit         → 审核记录
  └── /settings      → 系统设置
/doctor              → 医生主页
  ├── /              → 医生工作台
  └── /prescriptions → 我的处方
/nurse               → 护士主页
  ├── /              → 护士工作台
  └── /prescriptions → 处方查询
/pharmacist          → 药师主页
  ├── /              → 药师工作台
  ├── /prescriptions → 处方管理
  ├── /inventory     → 库存管理
  └── /audit         → 审核记录
```

### 路由守卫

- **认证检查**: 未登录用户自动跳转到登录页
- **角色权限**: 根据用户角色限制页面访问
- **Token验证**: 自动验证Token有效性

## API集成

### API配置

所有API请求通过 `src/api/index.js` 统一管理：

```javascript
// API基础配置
const api = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
})
```

### 请求拦截器

- 自动添加JWT Token到请求头
- 统一请求格式处理

### 响应拦截器

- 统一响应格式处理
- 自动处理401/403/500等错误
- Token过期自动跳转登录

### API模块

- `authAPI` - 认证相关接口
- `userAPI` - 用户管理接口
- `medicineAPI` - 药品管理接口
- `stockAPI` - 库存管理接口
- `prescriptionAPI` - 处方管理接口
- `auditAPI` - 审核记录接口

## 状态管理

使用 Pinia 进行状态管理：

### 用户状态 (stores/user.js)

- `user` - 当前用户信息
- `token` - JWT Token
- `isLoggedIn` - 登录状态
- `login()` - 登录方法
- `logout()` - 登出方法
- `fetchCurrentUser()` - 获取用户信息
- `hasRole()` - 角色检查

## 启动步骤

### 1. 环境要求

- Node.js 16+
- npm 或 yarn

### 2. 安装依赖

```bash
cd frontend
npm install
```

### 3. 开发模式运行

```bash
npm run dev
```

应用将在 http://localhost:3000 启动（根据vite.config.js配置）

### 4. 生产构建

```bash
npm run build
```

构建产物在 `dist/` 目录。

### 5. 预览生产构建

```bash
npm run preview
```

## 开发配置

### Vite配置 (vite.config.js)

```javascript
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'),  // 路径别名
    },
  },
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',  // 后端API代理
        changeOrigin: true,
      },
      '/audit': {
        target: 'http://localhost:5000',  // Python服务代理
        changeOrigin: true,
      },
    },
  },
})
```

### 环境变量

可在项目根目录创建 `.env` 文件：

```env
VITE_API_BASE_URL=http://localhost:8080/api
VITE_AUDIT_SERVICE_URL=http://localhost:5000
```

## UI组件

### Element Plus

项目使用 Element Plus 作为UI组件库，主要组件：

- **表单组件**: el-form, el-input, el-select, el-date-picker
- **表格组件**: el-table, el-pagination
- **对话框**: el-dialog, el-drawer
- **消息提示**: ElMessage, ElNotification
- **加载**: el-loading, el-skeleton

### 自定义组件

- `Layout.vue` - 主布局组件（侧边栏、顶部导航、内容区）
- `FileUpload.vue` - 文件上传组件

## 样式规范

### CSS组织

- 使用 `<style scoped>` 实现组件样式隔离
- 全局样式在 `App.vue` 中定义
- 使用Element Plus主题变量

### 响应式设计

- 支持桌面端和移动端适配
- 使用Element Plus的响应式栅格系统

## 开发规范

### 代码规范

- 遵循Vue 3 Composition API规范
- 使用 `<script setup>` 语法
- 组件命名使用PascalCase
- 文件命名使用PascalCase（组件）或kebab-case（工具）

### Git提交规范

- `feat`: 新功能
- `fix`: 修复bug
- `docs`: 文档更新
- `style`: 代码格式调整
- `refactor`: 代码重构
- `test`: 测试相关
- `chore`: 构建过程或工具配置

## 常见问题

### 1. 端口被占用

修改 `vite.config.js` 中的 `server.port` 配置。

### 2. API请求失败

检查：
- 后端服务是否运行在 http://localhost:8080
- 代理配置是否正确
- 网络连接是否正常

### 3. Token过期

Token过期会自动跳转到登录页，重新登录即可。

### 4. 路由跳转失败

检查：
- 路由配置是否正确
- 用户角色权限是否匹配
- Token是否有效

## 开发计划

- ✅ 项目初始化和基础配置
- ✅ 登录认证功能
- ✅ 管理员功能页面
- ✅ 医生功能页面
- ✅ 护士功能页面
- ✅ 药师功能页面
- ✅ API集成
- ⏳ 响应式设计优化
- ⏳ 性能优化
- ⏳ 单元测试

## 联系方式

如有问题，请联系前端开发负责人。






