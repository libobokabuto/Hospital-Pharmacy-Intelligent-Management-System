package com.hpims.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * 用户实体类
 *
 * 实体类状态 (参考 javaWork.md)
 * ================================
 *
 * ✅ 已完成的实体类 (6个):
 * ✅ Medicine.java - 药品信息实体类
 *   - id, name, genericName, specification, manufacturer, price
 *   - stockQuantity, minStock, category, approvalNumber
 *   - createTime, updateTime
 *
 * ✅ StockIn.java - 入库记录实体类
 *   - id, medicineId, batchNumber, quantity, supplier
 *   - inDate, operator, createTime
 *   - 与 Medicine 多对一关系 (@ManyToOne)
 *
 * ✅ StockOut.java - 出库记录实体类
 *   - id, medicineId, batchNumber, quantity
 *   - outDate, operator, reason, createTime
 *   - 与 Medicine 多对一关系 (@ManyToOne)
 *
 * ✅ Prescription.java - 处方主表实体类
 *   - id, prescriptionNumber, patientName, patientAge, patientGender
 *   - doctorName, department, createDate, status
 *   - auditResult, auditTime, createTime, updateTime
 *   - 与 PrescriptionDetail 一对多关系 (@OneToMany)
 *
 * ✅ PrescriptionDetail.java - 处方明细实体类
 *   - id, prescriptionId, medicineId, quantity
 *   - dosage, frequency, days, createTime
 *   - 与 Prescription 多对一关系 (@ManyToOne)
 *   - 与 Medicine 多对一关系 (@ManyToOne)
 *
 * ✅ AuditRecord.java - 审核记录实体类
 *   - id, prescriptionId, auditType, auditResult, auditScore
 *   - issuesFound, suggestions, auditor, auditTime, createTime
 *   - 与 Prescription 多对一关系 (@ManyToOne)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度必须在6-100个字符之间")
    @Column(nullable = false)
    private String password;

    @NotBlank(message = "角色不能为空")
    @Size(max = 20, message = "角色长度不能超过20个字符")
    @Column(nullable = false, length = 20)
    private String role; // admin, doctor, nurse, pharmacist

    @Size(max = 50, message = "真实姓名长度不能超过50个字符")
    @Column(length = 50)
    private String realName;

    @Column(length = 100)
    private String department;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;
}
