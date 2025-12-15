# 后端服务 (Backend)

## 概述

HPIMS 后端服务基于 Spring Boot 2.7 开发，提供完整的 RESTful API，负责业务逻辑处理、数据持久化和系统安全认证。

## 技术栈

- **框架**: Spring Boot 2.7.18
- **Java版本**: JDK 21
- **数据库**: MySQL 8.0
- **ORM**: Spring Data JPA
- **安全框架**: Spring Security + JWT
- **构建工具**: Maven 3.6+
- **API文档**: SpringDoc OpenAPI (Swagger)
- **工具库**: Lombok, Jackson

## 项目结构

```
backend/
├── src/main/java/com/hpims/
│   ├── HpimsApplication.java          # 主应用类
│   ├── config/                        # 配置类
│   │   ├── SecurityConfig.java        # Spring Security配置
│   │   ├── JwtConfig.java             # JWT配置
│   │   ├── WebConfig.java             # Web MVC配置
│   │   ├── SwaggerConfig.java         # Swagger API文档配置
│   │   └── RestTemplateConfig.java    # HTTP客户端配置
│   ├── controller/                    # 控制器层 (REST API)
│   │   ├── AuthController.java        # 认证接口
│   │   ├── UserController.java        # 用户管理接口
│   │   ├── MedicineController.java   # 药品管理接口
│   │   ├── StockController.java       # 库存管理接口
│   │   ├── PrescriptionController.java # 处方管理接口
│   │   └── AuditController.java       # 审核记录接口
│   ├── service/                       # 业务逻辑层
│   │   ├── UserService.java           # 用户业务逻辑
│   │   ├── MedicineService.java       # 药品业务逻辑
│   │   ├── StockInService.java        # 入库业务逻辑
│   │   ├── StockOutService.java        # 出库业务逻辑
│   │   ├── PrescriptionService.java   # 处方业务逻辑
│   │   ├── PrescriptionDetailService.java # 处方明细业务逻辑
│   │   ├── AuditRecordService.java    # 审核记录业务逻辑
│   │   └── AuditServiceClient.java    # Python审核服务客户端
│   ├── repository/                    # 数据访问层
│   │   ├── UserRepository.java
│   │   ├── MedicineRepository.java
│   │   ├── StockInRepository.java
│   │   ├── StockOutRepository.java
│   │   ├── PrescriptionRepository.java
│   │   ├── PrescriptionDetailRepository.java
│   │   └── AuditRecordRepository.java
│   ├── model/                         # 实体类 (JPA Entity)
│   │   ├── User.java                  # 用户实体
│   │   ├── Medicine.java              # 药品实体
│   │   ├── StockIn.java                # 入库记录实体
│   │   ├── StockOut.java               # 出库记录实体
│   │   ├── Prescription.java           # 处方实体
│   │   ├── PrescriptionDetail.java     # 处方明细实体
│   │   └── AuditRecord.java            # 审核记录实体
│   ├── dto/                           # 数据传输对象
│   │   ├── request/                   # 请求DTO
│   │   │   ├── LoginRequest.java
│   │   │   ├── RegisterRequest.java
│   │   │   ├── MedicineCreateRequest.java
│   │   │   ├── StockInRequest.java
│   │   │   ├── StockOutRequest.java
│   │   │   ├── PrescriptionCreateRequest.java
│   │   │   └── PrescriptionAuditRequest.java
│   │   ├── response/                  # 响应DTO
│   │   │   ├── LoginResponse.java
│   │   │   ├── UserResponse.java
│   │   │   ├── MedicineResponse.java
│   │   │   └── AuditRecordResponse.java
│   │   ├── ApiResponse.java           # 统一响应格式
│   │   └── PageResponse.java           # 分页响应格式
│   ├── security/                      # 安全认证
│   │   ├── JwtUtil.java               # JWT工具类
│   │   ├── JwtAuthenticationFilter.java # JWT认证过滤器
│   │   └── UserDetailsServiceImpl.java # 用户详情服务
│   ├── exception/                     # 异常处理
│   │   ├── GlobalExceptionHandler.java # 全局异常处理器
│   │   ├── BusinessException.java     # 业务异常
│   │   ├── AuthenticationException.java # 认证异常
│   │   ├── MedicineNotFoundException.java
│   │   ├── PrescriptionNotFoundException.java
│   │   └── StockInsufficientException.java
│   ├── util/                          # 工具类
│   │   ├── ResponseUtil.java         # 响应工具
│   │   ├── DateUtil.java              # 日期工具
│   │   ├── ValidationUtil.java       # 验证工具
│   │   └── PasswordGenerator.java     # 密码生成器
│   └── validation/                    # 自定义验证器
│       ├── PrescriptionNumber.java    # 处方号验证注解
│       └── PrescriptionNumberValidator.java # 处方号验证器
├── src/main/resources/
│   └── application.yml                # 应用配置文件
└── pom.xml                            # Maven依赖配置
```

## 核心功能模块

### 1. 用户认证与授权

- **JWT认证**: 基于Token的无状态认证机制
- **角色权限**: 支持 admin、doctor、nurse、pharmacist 四种角色
- **密码加密**: BCrypt 密码哈希加密
- **接口权限**: 基于角色的接口访问控制

**主要接口**:
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/register` - 用户注册
- `GET /api/auth/me` - 获取当前用户信息
- `POST /api/auth/refresh` - 刷新Token
- `POST /api/auth/logout` - 用户登出

### 2. 用户管理

- 用户CRUD操作
- 角色管理
- 用户信息查询

**主要接口**:
- `GET /api/users` - 获取用户列表
- `POST /api/users` - 创建用户
- `GET /api/users/{id}` - 获取用户详情
- `PUT /api/users/{id}` - 更新用户信息
- `DELETE /api/users/{id}` - 删除用户
- `PUT /api/users/{id}/role` - 更新用户角色

### 3. 药品管理

- 药品信息CRUD
- 药品搜索和分类查询
- 低库存预警
- 药品分类管理

**主要接口**:
- `GET /api/medicines` - 获取药品列表（支持分页、搜索、筛选）
- `GET /api/medicines/{id}` - 获取药品详情
- `POST /api/medicines` - 创建药品
- `PUT /api/medicines/{id}` - 更新药品信息
- `DELETE /api/medicines/{id}` - 删除药品
- `GET /api/medicines/search` - 搜索药品
- `GET /api/medicines/category/{category}` - 按分类查询
- `GET /api/medicines/low-stock` - 查询低库存药品

### 4. 库存管理

- 药品入库管理（批次、供应商）
- 药品出库管理（处方发药、盘亏、过期等）
- 库存查询和统计
- 库存预警

**主要接口**:
- `POST /api/stock/in` - 药品入库
- `POST /api/stock/out` - 药品出库
- `GET /api/stock/in` - 获取入库记录列表
- `GET /api/stock/out` - 获取出库记录列表
- `GET /api/stock/medicine/{medicineId}` - 查询药品库存
- `GET /api/stock/statistics` - 库存统计

### 5. 处方管理

- 处方创建和编辑
- 处方状态流转（未审核 → 审核中 → 已通过/已拒绝 → 已发药）
- 处方明细管理
- 处方查询和筛选
- 与Python审核服务集成

**主要接口**:
- `POST /api/prescriptions` - 创建处方
- `GET /api/prescriptions` - 获取处方列表（支持分页、筛选）
- `GET /api/prescriptions/{id}` - 获取处方详情
- `PUT /api/prescriptions/{id}` - 更新处方信息
- `POST /api/prescriptions/{id}/submit-audit` - 提交审核（调用Python服务）
- `POST /api/prescriptions/{id}/audit` - 人工审核
- `POST /api/prescriptions/{id}/dispense` - 发药
- `POST /api/prescriptions/{id}/cancel` - 取消处方
- `GET /api/prescriptions/{id}/details` - 获取处方明细
- `GET /api/prescriptions/{id}/audit-history` - 获取审核历史

### 6. 审核记录管理

- 审核记录查询
- 审核历史追溯
- 审核统计分析

**主要接口**:
- `GET /api/audit/records` - 获取审核记录列表
- `GET /api/audit/records/{id}` - 获取审核记录详情
- `GET /api/audit/records/prescription/{prescriptionId}` - 获取处方的审核历史
- `GET /api/audit/statistics` - 审核统计

## 配置说明

### application.yml 配置

```yaml
server:
  port: 8080
  servlet:
    context-path: /api

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/hpims?useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver

jwt:
  secret: your-jwt-secret-key
  expiration: 86400000  # 24小时

audit:
  service:
    url: http://localhost:5000  # Python审核服务地址
```

### 数据库配置

- 数据库名: `hpims`
- 字符集: `utf8mb4`
- 时区: `Asia/Shanghai`

## 启动步骤

### 1. 环境要求

- JDK 21+
- Maven 3.6+
- MySQL 8.0

### 2. 配置数据库

确保MySQL服务运行，并执行数据库初始化脚本：

```bash
mysql -u root -p < ../database/init.sql
```

### 3. 修改配置

编辑 `src/main/resources/application.yml`，修改数据库连接信息。

### 4. 编译和运行

```bash
# 清理并编译
mvn clean install

# 运行应用
mvn spring-boot:run
```

### 5. 验证启动

- 访问 Swagger API 文档: http://localhost:8080/api/swagger-ui.html
- 访问健康检查: http://localhost:8080/api/auth/me (需要认证)

## API文档

启动应用后，可通过以下地址访问API文档：

- **Swagger UI**: http://localhost:8080/api/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api/api-docs

## 安全机制

### JWT认证流程

1. 用户登录，后端验证用户名密码
2. 验证成功后生成JWT Token
3. 前端存储Token，后续请求在Header中携带
4. 后端通过JwtAuthenticationFilter验证Token有效性
5. 根据Token中的角色信息进行权限控制

### 角色权限

- **admin**: 系统管理员，拥有所有权限
- **doctor**: 医生，可以创建和查看处方
- **nurse**: 护士，可以查看处方
- **pharmacist**: 药师，可以审核处方、管理库存、发药

## 与Python服务集成

后端通过 `AuditServiceClient` 调用Python审核服务：

```java
// 提交处方审核
prescriptionService.submitForAudit(prescriptionId);
```

Python服务地址配置在 `application.yml` 的 `audit.service.url`。

## 开发规范

### 代码规范

- 遵循阿里巴巴Java开发规范
- 使用Lombok简化代码
- 统一异常处理机制
- 统一API响应格式

### 命名规范

- 类名: 大驼峰 (PascalCase)
- 方法名/变量名: 小驼峰 (camelCase)
- 常量: 全大写下划线分隔 (UPPER_SNAKE_CASE)
- 数据库表名: 小写下划线分隔 (snake_case)

## 常见问题

### 1. 端口被占用

修改 `application.yml` 中的 `server.port` 配置。

### 2. 数据库连接失败

检查：
- MySQL服务是否运行
- 数据库用户名密码是否正确
- 数据库 `hpims` 是否已创建

### 3. JWT Token过期

Token默认24小时过期，可通过 `jwt.expiration` 配置调整。

## 开发计划

- ✅ 基础架构搭建
- ✅ 用户认证与授权
- ✅ 药品管理模块
- ✅ 库存管理模块
- ✅ 处方管理模块
- ✅ 审核服务集成
- ⏳ 单元测试完善
- ⏳ 性能优化

## 联系方式

如有问题，请联系后端开发负责人。

