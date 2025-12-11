package com.hpims;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 医院药房智能管理系统主应用类
 *
 * 开发进度跟踪 (参考 javaWork.md)
 * =========================================
 *
 * ✅ 一、实体类（Model 层）- 7个已完成
 * ✅ User - 用户实体类
 * ✅ Medicine - 药品信息实体类
 * ✅ StockIn - 入库记录实体类
 * ✅ StockOut - 出库记录实体类
 * ✅ Prescription - 处方主表实体类
 * ✅ PrescriptionDetail - 处方明细实体类
 * ✅ AuditRecord - 审核记录实体类
 *
 * ✅ 二、数据访问层（Repository）- 7个已完成
 * ✅ UserRepository - 用户数据访问
 * ✅ MedicineRepository - 药品数据访问
 * ✅ StockInRepository - 入库记录数据访问
 * ✅ StockOutRepository - 出库记录数据访问
 * ✅ PrescriptionRepository - 处方数据访问
 * ✅ PrescriptionDetailRepository - 处方明细数据访问
 * ✅ AuditRecordRepository - 审核记录数据访问
 *
 * ✅ 三、安全认证组件 - 已完成
 * ✅ JwtUtil - JWT工具类
 * ✅ JwtAuthenticationFilter - JWT认证过滤器
 * ✅ UserDetailsServiceImpl - 用户详情服务实现
 * ✅ SecurityConfig - Spring Security配置
 * ✅ JwtConfig - JWT配置类
 * ✅ WebConfig - Web MVC配置
 *
 * ✅ 四、业务逻辑层（Service）- 7个已完成
 * ✅ UserService - 用户业务逻辑
 * ✅ MedicineService - 药品业务逻辑
 * ✅ StockInService - 入库业务逻辑
 * ✅ StockOutService - 出库业务逻辑
 * ✅ PrescriptionService - 处方业务逻辑
 * ✅ PrescriptionDetailService - 处方明细业务逻辑
 * ✅ AuditRecordService - 审核记录业务逻辑
 *
 * ⏳ 五、控制器层（Controller）- 6个待实现
 * ⏳ AuthController - 用户认证API
 *   - POST /auth/login, POST /auth/register, GET /auth/me, POST /auth/refresh
 * ⏳ UserController - 用户管理API
 *   - GET /users, POST /users, GET /users/{id}, PUT /users/{id}, DELETE /users/{id}
 * ⏳ MedicineController - 药品管理API
 *   - GET /medicines, POST /medicines, PUT /medicines/{id}, DELETE /medicines/{id}
 * ⏳ StockController - 库存管理API（入库/出库）
 *   - POST /stock/in, POST /stock/out, GET /stock/in, GET /stock/out
 * ⏳ PrescriptionController - 处方管理API
 *   - POST /prescriptions, GET /prescriptions, POST /prescriptions/{id}/submit-audit
 * ⏳ AuditController - 审核记录管理API
 *   - GET /audit/records, GET /audit/records/{id}
 *
 * ⏳ 六、DTO类 - 约15个待实现
 * ⏳ 请求DTO: LoginRequest, RegisterRequest, MedicineCreateRequest, StockInRequest等
 * ⏳ 响应DTO: LoginResponse, UserResponse, MedicineResponse, PrescriptionResponse等
 * ⏳ 通用DTO: ApiResponse<T>, PageResponse<T>
 *
 * ⏳ 七、Python服务集成 - 待实现
 * ⏳ AuditServiceClient - HTTP客户端（调用Python审核服务）
 * ⏳ 审核结果映射 - 将Python服务返回结果映射为Java对象
 *
 * ⏳ 八、异常处理 - 待实现
 * ⏳ 自定义异常类: BusinessException, MedicineNotFoundException, StockInsufficientException等
 * ⏳ GlobalExceptionHandler - 全局异常处理器
 *
 * ⏳ 九、工具类和配置 - 部分已完成
 * ✅ WebConfig - Web MVC配置（已完成）
 * ⏳ ResponseUtil - 响应工具类（待实现）
 * ⏳ DateUtil - 日期工具类（待实现）
 * ⏳ ValidationUtil - 验证工具类（待实现）
 * ⏳ RestTemplateConfig - RestTemplate配置（用于调用Python服务，待实现）
 *
 * ⏳ 十、数据验证 - 待实现
 * ⏳ 实体类验证注解: @NotNull, @NotBlank, @Size等
 * ⏳ Controller参数验证: @Valid注解
 *
 * @author 李教博 (Java后端负责人)
 */
@SpringBootApplication
public class HpimsApplication {

    public static void main(String[] args) {
        SpringApplication.run(HpimsApplication.class, args);
    }
}
