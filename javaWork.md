
## Java 模块工作清单

### 一、实体类（Model 层）- 6 个实体类



### 1. **Medicine.java** - 药品信息实体类
- 字段：id, name, genericName, specification, manufacturer, price, stockQuantity, minStock, category, approvalNumber, createTime, updateTime
- 包含数据验证注解（@NotBlank, @NotNull, @Min, @DecimalMin）
- 自动设置创建时间和更新时间

### 2. **StockIn.java** - 入库记录实体类
- 字段：id, medicineId, batchNumber, quantity, supplier, inDate, operator, createTime
- 与 Medicine 多对一关系（@ManyToOne）
- 包含数据验证注解

### 3. **StockOut.java** - 出库记录实体类
- 字段：id, medicineId, batchNumber, quantity, outDate, operator, reason, createTime
- 与 Medicine 多对一关系（@ManyToOne）
- 包含数据验证注解

### 4. **Prescription.java** - 处方主表实体类
- 字段：id, prescriptionNumber, patientName, patientAge, patientGender, doctorName, department, createDate, status, auditResult, auditTime, createTime, updateTime
- 与 PrescriptionDetail 一对多关系（@OneToMany）
- 包含数据验证注解

### 5. **PrescriptionDetail.java** - 处方明细实体类
- 字段：id, prescriptionId, medicineId, quantity, dosage, frequency, days, createTime
- 与 Prescription 多对一关系（@ManyToOne）
- 与 Medicine 多对一关系（@ManyToOne）
- 包含数据验证注解

### 6. **AuditRecord.java** - 审核记录实体类
- 字段：id, prescriptionId, auditType, auditResult, auditScore, issuesFound, suggestions, auditor, auditTime, createTime
- 与 Prescription 多对一关系（@ManyToOne）
- 包含数据验证注解

## 特性

1. 使用 Lombok：@Data, @NoArgsConstructor, @AllArgsConstructor, @Builder
2. JPA 注解：@Entity, @Table, @Id, @GeneratedValue, @Column
3. 关系映射：正确配置了实体间的关系
4. 数据验证：使用 javax.validation 注解进行字段验证
5. 自动时间戳：使用 @PrePersist 和 @PreUpdate 自动设置时间
6. 字段映射：使用 @Column(name = "xxx") 映射数据库字段名

---

### 二、数据访问层（Repository）- 6 个 Repository

1. MedicineRepository.java - 药品数据访问接口
Optional<Medicine> findByName(String name) - 根据药品名称查找
List<Medicine> findByCategory(String category) - 根据分类查找
List<Medicine> findByManufacturer(String manufacturer) - 根据生产厂家查找
List<Medicine> findByNameContaining(String name) - 根据药品名称模糊查询
2. StockInRepository.java - 入库记录数据访问接口
List<StockIn> findByMedicineId(Long medicineId) - 根据药品ID查找
List<StockIn> findByInDateBetween(LocalDate startDate, LocalDate endDate) - 根据入库日期范围查找
List<StockIn> findByOperator(String operator) - 根据操作员查找
3. StockOutRepository.java - 出库记录数据访问接口
List<StockOut> findByMedicineId(Long medicineId) - 根据药品ID查找
List<StockOut> findByOutDateBetween(LocalDate startDate, LocalDate endDate) - 根据出库日期范围查找
List<StockOut> findByReason(String reason) - 根据出库原因查找
4. PrescriptionRepository.java - 处方数据访问接口
Optional<Prescription> findByPrescriptionNumber(String prescriptionNumber) - 根据处方号查找
List<Prescription> findByStatus(String status) - 根据状态查找
List<Prescription> findByPatientName(String patientName) - 根据患者姓名查找
List<Prescription> findByDoctorName(String doctorName) - 根据医生姓名查找
List<Prescription> findByCreateDateBetween(LocalDate startDate, LocalDate endDate) - 根据创建日期范围查找
5. PrescriptionDetailRepository.java - 处方明细数据访问接口
List<PrescriptionDetail> findByPrescriptionId(Long prescriptionId) - 根据处方ID查找
List<PrescriptionDetail> findByMedicineId(Long medicineId) - 根据药品ID查找
6. AuditRecordRepository.java - 审核记录数据访问接口
List<AuditRecord> findByPrescriptionId(Long prescriptionId) - 根据处方ID查找
List<AuditRecord> findByAuditResult(String auditResult) - 根据审核结果查找
List<AuditRecord> findByAuditTimeBetween(LocalDateTime startTime, LocalDateTime endTime) - 根据审核时间范围查找
特性
继承 JpaRepository：所有接口继承 JpaRepository<Entity, Long>
使用 @Repository：标注为Repository组件
方法命名规范：遵循Spring Data JPA命名约定，自动生成查询
返回类型：单条用 Optional，多条用 List
日期范围查询：使用 Between 进行范围查询
注释：每个方法都有JavaDoc注释

---

### 三、业务逻辑层（Service）- 6 个 Service

1. **MedicineService** - 药品业务逻辑服务
   - `findAll()` - 获取所有药品
   - `findById(Long id)` - 根据ID查询
   - `save(Medicine medicine)` - 保存药品
   - `update(Long id, Medicine medicine)` - 更新药品
   - `delete(Long id)` - 删除药品
   - `search(String keyword)` - 搜索药品（名称模糊查询）
   - `findByCategory(String category)` - 按分类查询
   - `updateStock(Long id, Integer quantity)` - 更新库存（支持增减）

2. **StockInService** - 入库业务逻辑服务
   - `createStockIn(StockIn stockIn)` - 创建入库记录（自动更新库存）
   - `findAll()` - 获取所有入库记录
   - `findByMedicineId(Long medicineId)` - 按药品查询
   - `findByDateRange(LocalDate start, LocalDate end)` - 按日期范围查询
   - `processStockIn(StockIn stockIn)` - 处理入库（更新库存）

3. **StockOutService** - 出库业务逻辑服务
   - `createStockOut(StockOut stockOut)` - 创建出库记录（自动检查库存并扣减）
   - `findAll()` - 获取所有出库记录
   - `findByMedicineId(Long medicineId)` - 按药品查询
   - `findByDateRange(LocalDate start, LocalDate end)` - 按日期范围查询
   - `processStockOut(StockOut stockOut)` - 处理出库（检查库存充足性并扣减）

4. **PrescriptionService** - 处方业务逻辑服务
   - `createPrescription(Prescription prescription, List<PrescriptionDetail> details)` - 创建处方（包含明细）
   - `findById(Long id)` - 根据ID查询
   - `findByPrescriptionNumber(String number)` - 根据处方号查询
   - `findAll()` - 获取所有处方
   - `findByStatus(String status)` - 按状态查询
   - `updateStatus(Long id, String status)` - 更新处方状态
   - `submitForAudit(Long prescriptionId)` - 提交审核（预留Python服务集成接口）
   - `dispense(Long prescriptionId)` - 发药（更新状态，扣减库存）

5. **PrescriptionDetailService** - 处方明细业务逻辑服务
   - `findByPrescriptionId(Long prescriptionId)` - 查询处方明细
   - `save(PrescriptionDetail detail)` - 保存明细
   - `saveAll(List<PrescriptionDetail> details)` - 批量保存明细
   - `deleteByPrescriptionId(Long prescriptionId)` - 根据处方ID删除明细

6. **AuditRecordService** - 审核记录业务逻辑服务
   - `save(AuditRecord record)` - 保存审核记录
   - `findByPrescriptionId(Long prescriptionId)` - 查询审核历史（按时间倒序）
   - `findLatestByPrescriptionId(Long prescriptionId)` - 查询最新审核记录
   - `findById(Long id)` - 根据ID查询
   - `findAll()` - 获取所有审核记录
   - `findByAuditResult(String auditResult)` - 根据审核结果查询

### 实现要点

1. 事务管理：关键方法使用 `@Transactional` 确保数据一致性
2. 业务逻辑：
   - 入库/出库自动更新库存
   - 出库前检查库存充足性
   - 处方创建时自动生成处方号
   - 发药时自动扣减库存并创建出库记录
3. 异常处理：使用 `RuntimeException` 处理业务异常（后续可替换为自定义异常）
4. Python 服务集成：`PrescriptionService.submitForAudit()` 已预留接口，待后续集成
5. 代码质量：已修复所有编译警告和错误



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