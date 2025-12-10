package com.hpims.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 用户实体类
 *
 * TODO: 李教博需要实现的其他实体类
 * ================================
 *
 * 药品管理实体类 (需要创建):
 * TODO: Medicine.java - 药品信息实体类
 *   - id, name, genericName, specification, manufacturer
 *   - category, unit, price, expiryDate, batchNumber
 *   - description, sideEffects, contraindications, storage
 *   - createTime, updateTime
 *
 * 库存管理实体类 (需要创建):
 * TODO: Inventory.java - 库存信息实体类
 *   - id, medicineId, currentStock, minStock, maxStock
 *   - location, lastUpdated
 *
 * TODO: StockIn.java - 入库记录实体类
 *   - id, medicineId, quantity, batchNumber, expiryDate
 *   - supplier, operatorId, operateTime, notes
 *
 * TODO: StockOut.java - 出库记录实体类
 *   - id, medicineId, quantity, batchNumber, reason
 *   - operatorId, operateTime, destination, notes
 *
 * 处方管理实体类 (需要创建):
 * TODO: Prescription.java - 处方主表实体类
 *   - id, prescriptionNumber, patientId, patientName
 *   - doctorId, doctorName, department, diagnose
 *   - createTime, status, totalAmount
 *
 * TODO: PrescriptionDetail.java - 处方明细实体类
 *   - id, prescriptionId, medicineId, medicineName
 *   - dosage, frequency, days, quantity, unit
 *   - instructions, price, subtotal
 *
 * 审核记录实体类 (需要创建):
 * TODO: AuditRecord.java - 审核记录实体类
 *   - id, prescriptionId, auditorId, auditTime
 *   - result, score, riskLevel, suggestions
 *   - algorithmVersion, processingTime
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    private Long id;
    private String username;
    private String password;
    private String role; // admin, doctor, nurse, pharmacist
    private String realName;
    private String department;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
