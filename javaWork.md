分析 Java 模块（backend）的工作项。先查看项目结构和现有代码：


[3 tools called]


查看现有代码和数据库结构：


[6 tools called]


查看数据库脚本和现有控制器：


[4 tools called]


检查配置和现有代码结构：


[3 tools called]


## Java 模块工作清单

### 一、实体类（Model 层）- 6 个实体类

#### 1. Medicine（药品信息）
- 字段：id, name, genericName, specification, manufacturer, price, stockQuantity, minStock, category, approvalNumber, createTime, updateTime
- 注解：JPA 实体注解、表名映射、字段验证

#### 2. StockIn（入库记录）
- 字段：id, medicineId, batchNumber, quantity, supplier, inDate, operator, createTime
- 关系：与 Medicine 多对一

#### 3. StockOut（出库记录）
- 字段：id, medicineId, batchNumber, quantity, outDate, operator, reason, createTime
- 关系：与 Medicine 多对一

#### 4. Prescription（处方主表）
- 字段：id, prescriptionNumber, patientName, patientAge, patientGender, doctorName, department, createDate, status, auditResult, auditTime, createTime, updateTime
- 关系：与 PrescriptionDetail 一对多

#### 5. PrescriptionDetail（处方明细）
- 字段：id, prescriptionId, medicineId, quantity, dosage, frequency, days, createTime
- 关系：与 Prescription 多对一，与 Medicine 多对一

#### 6. AuditRecord（审核记录）
- 字段：id, prescriptionId, auditType, auditResult, auditScore, issuesFound, suggestions, auditor, auditTime, createTime
- 关系：与 Prescription 多对一

---

### 二、数据访问层（Repository）- 6 个 Repository

#### 1. MedicineRepository
- 方法：findByName, findByCategory, findByManufacturer, findByNameContaining

#### 2. StockInRepository
- 方法：findByMedicineId, findByInDateBetween, findByOperator

#### 3. StockOutRepository
- 方法：findByMedicineId, findByOutDateBetween, findByReason

#### 4. PrescriptionRepository
- 方法：findByPrescriptionNumber, findByStatus, findByPatientName, findByDoctorName, findByCreateDateBetween

#### 5. PrescriptionDetailRepository
- 方法：findByPrescriptionId, findByMedicineId

#### 6. AuditRecordRepository
- 方法：findByPrescriptionId, findByAuditResult, findByAuditTimeBetween

---

### 三、业务逻辑层（Service）- 6 个 Service

#### 1. MedicineService
- 方法：
  - `List<Medicine> findAll()` - 获取所有药品
  - `Medicine findById(Long id)` - 根据ID查询
  - `Medicine save(Medicine medicine)` - 保存药品
  - `Medicine update(Long id, Medicine medicine)` - 更新药品
  - `void delete(Long id)` - 删除药品
  - `List<Medicine> search(String keyword)` - 搜索药品
  - `List<Medicine> findByCategory(String category)` - 按分类查询
  - `void updateStock(Long id, Integer quantity)` - 更新库存

#### 2. StockInService
- 方法：
  - `StockIn createStockIn(StockIn stockIn)` - 创建入库记录
  - `List<StockIn> findAll()` - 获取所有入库记录
  - `List<StockIn> findByMedicineId(Long medicineId)` - 按药品查询
  - `List<StockIn> findByDateRange(LocalDate start, LocalDate end)` - 按日期范围查询
  - `void processStockIn(StockIn stockIn)` - 处理入库（更新库存）

#### 3. StockOutService
- 方法：
  - `StockOut createStockOut(StockOut stockOut)` - 创建出库记录
  - `List<StockOut> findAll()` - 获取所有出库记录
  - `List<StockOut> findByMedicineId(Long medicineId)` - 按药品查询
  - `List<StockOut> findByDateRange(LocalDate start, LocalDate end)` - 按日期范围查询
  - `void processStockOut(StockOut stockOut)` - 处理出库（更新库存，检查库存是否充足）

#### 4. PrescriptionService
- 方法：
  - `Prescription createPrescription(Prescription prescription, List<PrescriptionDetail> details)` - 创建处方
  - `Prescription findById(Long id)` - 根据ID查询
  - `Prescription findByPrescriptionNumber(String number)` - 根据处方号查询
  - `List<Prescription> findAll()` - 获取所有处方
  - `List<Prescription> findByStatus(String status)` - 按状态查询
  - `Prescription updateStatus(Long id, String status)` - 更新处方状态
  - `void submitForAudit(Long prescriptionId)` - 提交审核（调用Python服务）
  - `void dispense(Long prescriptionId)` - 发药（更新状态，扣减库存）

#### 5. PrescriptionDetailService
- 方法：
  - `List<PrescriptionDetail> findByPrescriptionId(Long prescriptionId)` - 查询处方明细
  - `PrescriptionDetail save(PrescriptionDetail detail)` - 保存明细

#### 6. AuditRecordService
- 方法：
  - `AuditRecord save(AuditRecord record)` - 保存审核记录
  - `List<AuditRecord> findByPrescriptionId(Long prescriptionId)` - 查询审核历史
  - `AuditRecord findLatestByPrescriptionId(Long prescriptionId)` - 查询最新审核记录

---

### 四、控制器层（Controller）- 6 个 Controller

#### 1. AuthController（认证）
- 接口：
  - `POST /auth/login` - 用户登录（返回JWT）
  - `POST /auth/register` - 用户注册
  - `GET /auth/me` - 获取当前用户信息
  - `POST /auth/logout` - 登出（可选）
  - `POST /auth/refresh` - 刷新JWT令牌

#### 2. UserController（用户管理）
- 接口：
  - `GET /users` - 获取用户列表（分页，管理员）
  - `POST /users` - 创建用户（管理员）
  - `GET /users/{id}` - 获取用户详情
  - `PUT /users/{id}` - 更新用户信息
  - `DELETE /users/{id}` - 删除用户（管理员）
  - `PUT /users/{id}/role` - 修改用户角色（管理员）

#### 3. MedicineController（药品管理）
- 接口：
  - `GET /medicines` - 获取药品列表（分页、搜索、筛选）
  - `GET /medicines/{id}` - 获取药品详情
  - `POST /medicines` - 创建药品（管理员/药师）
  - `PUT /medicines/{id}` - 更新药品信息
  - `DELETE /medicines/{id}` - 删除药品（管理员）
  - `GET /medicines/search` - 搜索药品
  - `GET /medicines/category/{category}` - 按分类查询
  - `GET /medicines/low-stock` - 查询低库存药品

#### 4. StockController（库存管理）
- 接口：
  - `POST /stock/in` - 药品入库
  - `POST /stock/out` - 药品出库
  - `GET /stock/in` - 获取入库记录列表（分页、筛选）
  - `GET /stock/out` - 获取出库记录列表（分页、筛选）
  - `GET /stock/in/{id}` - 获取入库记录详情
  - `GET /stock/out/{id}` - 获取出库记录详情
  - `GET /stock/medicine/{medicineId}` - 查询药品库存
  - `GET /stock/statistics` - 库存统计

#### 5. PrescriptionController（处方管理）
- 接口：
  - `POST /prescriptions` - 创建处方
  - `GET /prescriptions` - 获取处方列表（分页、筛选）
  - `GET /prescriptions/{id}` - 获取处方详情（包含明细）
  - `PUT /prescriptions/{id}` - 更新处方信息
  - `POST /prescriptions/{id}/submit-audit` - 提交审核（调用Python服务）
  - `POST /prescriptions/{id}/audit` - 人工审核
  - `POST /prescriptions/{id}/dispense` - 发药
  - `POST /prescriptions/{id}/cancel` - 取消处方
  - `GET /prescriptions/{id}/details` - 获取处方明细
  - `GET /prescriptions/{id}/audit-history` - 获取审核历史

#### 6. AuditController（审核记录）
- 接口：
  - `GET /audit/records` - 获取审核记录列表（分页、筛选）
  - `GET /audit/records/{id}` - 获取审核记录详情
  - `GET /audit/records/prescription/{prescriptionId}` - 获取处方的审核历史
  - `GET /audit/statistics` - 审核统计

---

### 五、DTO 类（数据传输对象）- 约 15 个 DTO

#### 请求 DTO：
1. `LoginRequest` - 登录请求
2. `RegisterRequest` - 注册请求
3. `MedicineCreateRequest` - 创建药品请求
4. `MedicineUpdateRequest` - 更新药品请求
5. `StockInRequest` - 入库请求
6. `StockOutRequest` - 出库请求
7. `PrescriptionCreateRequest` - 创建处方请求
8. `PrescriptionDetailRequest` - 处方明细请求
9. `PrescriptionAuditRequest` - 处方审核请求

#### 响应 DTO：
10. `LoginResponse` - 登录响应（包含JWT）
11. `UserResponse` - 用户信息响应
12. `MedicineResponse` - 药品信息响应
13. `StockInResponse` - 入库记录响应
14. `StockOutResponse` - 出库记录响应
15. `PrescriptionResponse` - 处方响应
16. `AuditRecordResponse` - 审核记录响应

#### 通用 DTO：
17. `ApiResponse<T>` - 统一API响应格式
18. `PageResponse<T>` - 分页响应

---

### 六、Python 服务集成

#### 1. AuditServiceClient（HTTP 客户端）
- 方法：
  - `AuditResult auditPrescription(Prescription prescription)` - 调用Python审核服务
  - `boolean isServiceAvailable()` - 检查服务是否可用
  - 使用 RestTemplate 或 WebClient 调用 `http://localhost:5000/api/prescription/audit`

#### 2. 审核结果映射
- 将 Python 服务返回的审核结果映射为 Java 对象
- 保存审核记录到数据库

---

### 七、异常处理

#### 1. 自定义异常类
- `BusinessException` - 业务异常基类
- `MedicineNotFoundException` - 药品不存在
- `StockInsufficientException` - 库存不足
- `PrescriptionNotFoundException` - 处方不存在
- `AuthenticationException` - 认证异常

#### 2. GlobalExceptionHandler（全局异常处理）
- 统一异常响应格式
- 处理各种异常类型

---

### 八、工具类和配置

#### 1. 工具类
- `ResponseUtil` - 响应工具类
- `DateUtil` - 日期工具类
- `ValidationUtil` - 验证工具类

#### 2. 配置类
- `RestTemplateConfig` - RestTemplate 配置（用于调用Python服务）
- `SwaggerConfig` - Swagger 配置（已配置，可能需要完善）

---

### 九、数据验证

#### 1. 实体类验证注解
- `@NotNull`, `@NotBlank`, `@Size`, `@Min`, `@Max`, `@Email` 等

#### 2. Controller 参数验证
- `@Valid` 注解
- 自定义验证器（如处方号格式验证）

---

### 十、测试（可选但推荐）

#### 1. 单元测试
- Service 层单元测试
- Repository 层测试

#### 2. 集成测试
- Controller 层集成测试
- API 接口测试

---

## 优先级建议

### 高优先级（核心功能）
1. 实体类（Model）- 6 个
2. Repository 层 - 6 个
3. Service 层 - 6 个
4. Controller 层 - 6 个
5. AuthController（登录认证）
6. Python 服务集成

### 中优先级（完善功能）
7. DTO 类
8. 异常处理
9. 数据验证

### 低优先级（优化）
10. 工具类
11. 单元测试

---

## 工作量估算

- 实体类：6 个 × 30 分钟 = 3 小时
- Repository：6 个 × 15 分钟 = 1.5 小时
- Service：6 个 × 1 小时 = 6 小时
- Controller：6 个 × 1.5 小时 = 9 小时
- DTO：15 个 × 20 分钟 = 5 小时
- Python 集成：2 小时
- 异常处理：1 小时
- 测试和调试：5 小时

总计：约 32.5 小时（约 4-5 个工作日）

---

以上为 Java 模块的工作清单。需要我开始实现哪一部分？