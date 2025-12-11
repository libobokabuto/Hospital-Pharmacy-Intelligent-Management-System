# 医院药房智能管理系统 (HPIMS)

## 项目简介

医院药房管理系统是一个简化的医药库存和处方管理平台，采用前后端分离架构，支持基本的药品管理、库存控制和处方处理功能。

## 技术栈

- **后端**: Spring Boot 2.7 + MySQL + JPA
- **前端**: HTML/CSS/JavaScript (可选Vue.js)
- **数据库**: MySQL 8.0

## 快速开始

### 1. 环境准备
- JDK 21+
- MySQL 8.0
- Maven 3.6+
- IDE (IntelliJ IDEA 或 Eclipse)

### 2. 数据库初始化
```bash
# 登录MySQL
mysql -u root -p

# 执行初始化脚本
source database/init.sql
```

### 3. 后端启动
```bash
cd backend
mvn clean install
mvn spring-boot:run
```

### 4. 前端访问
打开浏览器访问: http://localhost:8080

### 5. 测试接口
访问测试接口: http://localhost:8080/api/test/hello

## 项目结构

```
Hospital-Pharmacy-Intelligent-Management-System/
├── backend/                    # 后端代码
│   ├── src/main/java/com/hpims/
│   │   ├── controller/         # 控制器层
│   │   ├── service/           # 业务逻辑层
│   │   ├── mapper/            # 数据访问层
│   │   ├── entity/            # 实体类
│   │   └── config/            # 配置类
│   ├── src/main/resources/    # 配置文件
│   └── pom.xml               # Maven配置
├── frontend/                  # 前端代码
├── database/                  # 数据库脚本
├── PROJECT_PLAN.md           # 项目计划文档
├── QUICK_START.md            # 快速开始指南
└── README.md                 # 项目说明
```

## 核心功能

1. **用户管理**: 管理员登录认证
2. **药品管理**: 药品信息的增删改查
3. **库存管理**: 药品入库、出库、库存查询
4. **处方管理**: 处方录入、发药管理

## API接口

### 用户管理
- `POST /api/login` - 用户登录

### 药品管理
- `GET /api/medicines` - 获取药品列表
- `POST /api/medicines` - 添加药品
- `PUT /api/medicines/{id}` - 更新药品
- `DELETE /api/medicines/{id}` - 删除药品

### 库存管理
- `POST /api/stock/in` - 药品入库
- `POST /api/stock/out` - 药品出库
- `GET /api/stock/{medicineId}` - 查询库存

### 处方管理
- `GET /api/prescriptions` - 获取处方列表
- `POST /api/prescriptions` - 创建处方
- `PUT /api/prescriptions/{id}/dispense` - 发药

## 开发计划

项目计划为期一周，每天有明确的任务目标：

- **Day 1**: 项目初始化和数据库设计
- **Day 2**: 用户管理和药品管理
- **Day 3**: 库存管理功能
- **Day 4**: 处方管理功能
- **Day 5**: 前端页面开发
- **Day 6**: 系统集成测试
- **Day 7**: 文档编写和完善

## 团队分工

- **成员1**: Java后端开发 (60%)
- **成员2**: Python数据分析和前端辅助 (20%)
- **成员3**: 系统设计和文档编写 (20%)

## 注意事项

1. 确保MySQL服务正在运行
2. 检查数据库连接配置
3. 端口8080未被占用
4. 首次运行会自动创建表结构

## 联系方式

如有问题请及时沟通，确保项目按时完成。