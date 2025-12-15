# 医院药房智能管理系统 (HPIMS)

## 项目简介

医院药房智能管理系统（Hospital Pharmacy Intelligent Management System, HPIMS）是一个基于前后端分离架构的现代化医药管理平台。系统集成了药品管理、库存控制、处方处理和智能审核等核心功能，通过Python智能审核算法提供处方安全性评估，帮助医院药房提高管理效率和服务质量。

### 主要特性

- 🏥 **多角色权限管理**: 支持管理员、医生、护士、药师四种角色，精细化权限控制
- 💊 **药品全生命周期管理**: 从药品信息录入到库存管理，完整的药品管理体系
- 📋 **处方智能审核**: 基于Python算法的处方自动审核，包括剂量合理性、药品相互作用、患者安全性等多维度检查
- 📊 **数据可视化**: 丰富的统计图表和数据分析功能
- 🔒 **安全可靠**: JWT认证、BCrypt密码加密、完善的异常处理机制

## 技术栈

### 后端服务
- **框架**: Spring Boot 2.7.18
- **语言**: Java 21
- **ORM**: Spring Data JPA
- **安全**: Spring Security + JWT
- **数据库**: MySQL 8.0
- **构建工具**: Maven 3.6+
- **API文档**: SpringDoc OpenAPI (Swagger)

### 前端应用
- **框架**: Vue 3.4+
- **构建工具**: Vite 5.0+
- **UI组件库**: Element Plus 2.4+
- **状态管理**: Pinia 2.1+
- **路由**: Vue Router 4.2+
- **HTTP客户端**: Axios 1.6+
- **图表库**: ECharts 6.0+

### 审核服务
- **框架**: Flask 3.1+
- **语言**: Python 3.8+
- **数据库**: MySQL 8.0 / SQLite
- **RESTful**: Flask-RESTful

## 快速开始

### 环境要求

- **JDK**: 21+
- **Node.js**: 16+
- **Python**: 3.8+
- **MySQL**: 8.0+
- **Maven**: 3.6+

### 1. 克隆项目

```bash
git clone <repository-url>
cd Hospital-Pharmacy-Intelligent-Management-System
```

### 2. 数据库初始化

```bash
# 登录MySQL
mysql -u root -p

# 创建数据库
CREATE DATABASE hpims CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 执行初始化脚本
USE hpims;
SOURCE database/init.sql;
```

### 3. 配置数据库连接

编辑 `backend/src/main/resources/application.yml`，修改数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/hpims?useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
```

### 4. 启动后端服务

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

后端服务将在 http://localhost:8080 启动

### 5. 启动Python审核服务

```bash
cd python-audit-service
pip install -r requirements.txt
python src/app.py
```

Python服务将在 http://localhost:5000 启动

### 6. 启动前端应用

```bash
cd frontend
npm install
npm run dev
```

前端应用将在 http://localhost:3000 启动（根据vite.config.js配置）

### 7. 访问系统

- **前端界面**: http://localhost:3000
- **后端API文档**: http://localhost:8080/api/swagger-ui.html
- **Python服务健康检查**: http://localhost:5000/health

### 默认账号

- **用户名**: admin
- **密码**: 123456

## 项目结构

```
Hospital-Pharmacy-Intelligent-Management-System/
├── backend/                          # Java后端服务
│   ├── src/main/java/com/hpims/
│   │   ├── controller/              # REST API控制器
│   │   ├── service/                  # 业务逻辑层
│   │   ├── repository/              # 数据访问层
│   │   ├── model/                    # 实体类
│   │   ├── dto/                      # 数据传输对象
│   │   ├── security/                 # 安全认证
│   │   ├── exception/                # 异常处理
│   │   └── util/                     # 工具类
│   ├── src/main/resources/
│   │   └── application.yml           # 应用配置
│   ├── pom.xml                       # Maven依赖
│   └── README.md                     # 后端文档
├── frontend/                         # Vue3前端应用
│   ├── src/
│   │   ├── views/                    # 页面组件
│   │   │   ├── admin/                # 管理员页面
│   │   │   ├── doctor/               # 医生页面
│   │   │   ├── nurse/                # 护士页面
│   │   │   └── pharmacist/           # 药师页面
│   │   ├── components/               # 公共组件
│   │   ├── router/                   # 路由配置
│   │   ├── stores/                   # 状态管理
│   │   ├── api/                      # API接口
│   │   └── utils/                    # 工具函数
│   ├── package.json                  # 依赖配置
│   ├── vite.config.js                # Vite配置
│   └── README.md                     # 前端文档
├── python-audit-service/             # Python审核服务
│   ├── src/
│   │   ├── app.py                    # Flask应用入口
│   │   ├── services/                 # 审核算法
│   │   ├── routes/                   # API路由
│   │   ├── dao/                      # 数据访问
│   │   └── models.py                 # 数据模型
│   ├── config/                       # 配置文件
│   ├── requirements.txt              # Python依赖
│   └── README.md                     # 审核服务文档
├── database/                         # 数据库脚本
│   ├── init.sql                      # 数据库初始化脚本
│   ├── update_schema.sql             # 数据库更新脚本
│   └── README.md                     # 数据库文档
├── dep/                              # 部署和配置文档
│   ├── 启动指南.md
│   ├── 环境搭建指南.md
│   └── 数据库配置.md
├── PROJECT_PLAN.md                   # 项目计划文档
├── TASK_LIST.md                     # 任务清单
└── README.md                         # 项目说明（本文档）
```

## 核心功能

### 1. 用户认证与权限管理

- JWT Token认证机制
- 多角色权限控制（admin、doctor、nurse、pharmacist）
- 密码BCrypt加密存储
- 用户信息管理

### 2. 药品管理

- 药品信息CRUD操作
- 药品搜索和分类查询
- 低库存预警
- 药品分类管理
- 药品知识库（适应症、禁忌症、相互作用）

### 3. 库存管理

- 药品入库管理（批次、供应商、有效期）
- 药品出库管理（处方发药、盘亏、过期等）
- 库存查询和统计
- 库存预警功能

### 4. 处方管理

- 处方创建和编辑
- 处方状态流转（未审核 → 审核中 → 已通过/已拒绝 → 已发药）
- 处方明细管理
- 处方查询和筛选
- 处方打印功能

### 5. 智能审核系统 ⭐

- **自动审核**: Python算法自动分析处方安全性
  - 药品兼容性检查
  - 用量合理性验证（单次剂量、日总剂量）
  - 药品相互作用检测
  - 患者安全性评估（疾病禁忌、适应症匹配）
- **人工审核**: 药师人工审核和复核
- **审核历史**: 完整的审核记录和追溯
- **审核统计**: 审核通过率、风险趋势分析

## API接口概览

### 认证接口
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/register` - 用户注册
- `GET /api/auth/me` - 获取当前用户信息
- `POST /api/auth/refresh` - 刷新Token

### 用户管理
- `GET /api/users` - 获取用户列表
- `POST /api/users` - 创建用户
- `PUT /api/users/{id}` - 更新用户
- `DELETE /api/users/{id}` - 删除用户

### 药品管理
- `GET /api/medicines` - 获取药品列表（支持分页、搜索、筛选）
- `POST /api/medicines` - 创建药品
- `PUT /api/medicines/{id}` - 更新药品
- `DELETE /api/medicines/{id}` - 删除药品
- `GET /api/medicines/low-stock` - 查询低库存药品

### 库存管理
- `POST /api/stock/in` - 药品入库
- `POST /api/stock/out` - 药品出库
- `GET /api/stock/in` - 获取入库记录
- `GET /api/stock/out` - 获取出库记录
- `GET /api/stock/medicine/{medicineId}` - 查询药品库存

### 处方管理
- `POST /api/prescriptions` - 创建处方
- `GET /api/prescriptions` - 获取处方列表
- `GET /api/prescriptions/{id}` - 获取处方详情
- `POST /api/prescriptions/{id}/submit-audit` - 提交审核
- `POST /api/prescriptions/{id}/audit` - 人工审核
- `POST /api/prescriptions/{id}/dispense` - 发药

### 审核记录
- `GET /api/audit/records` - 获取审核记录列表
- `GET /api/audit/records/{id}` - 获取审核记录详情
- `GET /api/audit/statistics` - 审核统计

### Python审核服务
- `POST /api/prescription/audit` - 单个处方审核
- `POST /api/prescription/batch-audit` - 批量审核
- `GET /api/audit/history` - 审核历史查询

**完整API文档**: 启动后端服务后访问 http://localhost:8080/api/swagger-ui.html

## 数据库设计

系统包含以下核心数据表：

- **用户表** (`users`): 用户信息和角色
- **药品表** (`medicine`): 药品基本信息
- **入库记录表** (`stock_in`): 药品入库记录
- **出库记录表** (`stock_out`): 药品出库记录
- **处方表** (`prescription`): 处方主表
- **处方明细表** (`prescription_detail`): 处方药品明细
- **审核记录表** (`audit_record`): 审核记录
- **审核问题表** (`audit_issue`): 审核发现的问题
- **药品知识库表**: `medicine_indication`、`medicine_contraindication`、`medicine_interaction` 等

详细数据库设计请参考 `database/init.sql` 和 `database/README.md`

## 开发文档

各模块详细文档：

- [后端服务文档](backend/README.md) - Java后端开发指南
- [前端应用文档](frontend/README.md) - Vue3前端开发指南
- [审核服务文档](python-audit-service/README.md) - Python审核算法说明
- [数据库文档](database/README.md) - 数据库设计和脚本说明
- [项目计划](PROJECT_PLAN.md) - 项目规划和开发计划
- [任务清单](TASK_LIST.md) - 详细开发任务

## 团队分工

- **李教博**: Java后端开发 (70%)
  - Spring Boot后端架构
  - 用户认证与权限管理
  - 药品和库存管理模块
  - 处方管理模块
  - 与Python服务集成

- **田纹搴**: Python审核服务 (20%)
  - 处方智能审核算法
  - 药品相互作用检测
  - 剂量合理性验证
  - 审核服务API开发

- **易禹**: 前端开发和文档 (10%)
  - Vue3前端界面开发
  - 用户交互优化
  - 系统分析和设计文档
  - 项目文档编写

## 开发计划

项目开发周期：2024年12月 - 2025年1月

### 已完成功能 ✅

- 基础架构搭建
- 用户认证与权限管理
- 药品管理模块
- 库存管理模块
- 处方管理模块
- 智能审核服务集成
- 前端界面开发

### 待优化功能 ⏳

- 单元测试完善
- 性能优化
- 审核算法准确性提升
- 响应式设计优化

## 常见问题

### 1. 端口冲突

- 后端默认端口: 8080（可在 `application.yml` 修改）
- 前端默认端口: 3000（可在 `vite.config.js` 修改）
- Python服务默认端口: 5000（可在 `app.py` 修改）

### 2. 数据库连接失败

- 检查MySQL服务是否运行
- 验证数据库用户名密码
- 确认数据库 `hpims` 已创建
- 检查防火墙设置

### 3. 前端无法访问后端API

- 检查后端服务是否启动
- 验证 `vite.config.js` 中的代理配置
- 检查CORS配置

### 4. Python审核服务无法连接数据库

- 检查数据库配置（环境变量或 `config/settings.py`）
- 确认数据库表已创建
- 验证数据库连接权限

## 部署说明

### 生产环境部署

1. **后端部署**
   ```bash
   cd backend
   mvn clean package
   java -jar target/backend-1.0.0.jar
   ```

2. **前端部署**
   ```bash
   cd frontend
   npm run build
   # 将 dist/ 目录部署到Nginx或其他Web服务器
   ```

3. **Python服务部署**
   ```bash
   cd python-audit-service
   # 使用 gunicorn 或 uwsgi 部署
   gunicorn -w 4 -b 0.0.0.0:5000 src.app:app
   ```

详细部署指南请参考 `dep/` 目录下的文档。

## 许可证

本项目为课程设计项目，仅供学习和研究使用。

## 联系方式

如有问题或建议，请通过以下方式联系：

- 项目Issue: [GitHub Issues]
- 项目文档: 查看各模块的 README.md 文件

---

**最后更新**: 2024年12月