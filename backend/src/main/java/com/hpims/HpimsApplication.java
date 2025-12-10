package com.hpims;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 医院药房智能管理系统主应用类
 *
 * TODO: 李教博需要实现的实体类和控制器
 * =========================================
 *
 * 核心实体类 (需要创建):
 * TODO: Medicine - 药品信息实体类
 * TODO: Inventory - 库存信息实体类
 * TODO: StockIn - 入库记录实体类
 * TODO: StockOut - 出库记录实体类
 * TODO: Prescription - 处方主表实体类
 * TODO: PrescriptionDetail - 处方明细实体类
 * TODO: AuditRecord - 审核记录实体类
 *
 * 核心控制器 (需要创建):
 * TODO: MedicineController - 药品管理API
 * TODO: InventoryController - 库存管理API
 * TODO: StockController - 入库出库管理API
 * TODO: PrescriptionController - 处方管理API
 * TODO: AuditController - 审核记录管理API
 *
 * 业务服务类 (需要创建):
 * TODO: MedicineService - 药品业务逻辑
 * TODO: InventoryService - 库存业务逻辑
 * TODO: PrescriptionService - 处方业务逻辑
 * TODO: AuditService - 审核业务逻辑
 *
 * Repository接口 (需要创建):
 * TODO: MedicineRepository - 药品数据访问
 * TODO: InventoryRepository - 库存数据访问
 * TODO: PrescriptionRepository - 处方数据访问
 * TODO: AuditRepository - 审核数据访问
 *
 * @author 李教博 (Java后端负责人)
 */
@SpringBootApplication
public class HpimsApplication {

    public static void main(String[] args) {
        SpringApplication.run(HpimsApplication.class, args);
    }
}
