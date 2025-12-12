package com.hpims.controller;

import com.hpims.dto.ApiResponse;
import com.hpims.dto.PageResponse;
import com.hpims.dto.request.PrescriptionAuditRequest;
import com.hpims.model.Prescription;
import com.hpims.model.PrescriptionDetail;
import com.hpims.repository.PrescriptionRepository;
import com.hpims.service.PrescriptionService;
import com.hpims.service.PrescriptionDetailService;
import com.hpims.service.AuditRecordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 处方管理控制器
 */
@RestController
@RequestMapping("/prescriptions")
@CrossOrigin
public class PrescriptionController {

    private static final Logger logger = LoggerFactory.getLogger(PrescriptionController.class);

    @Autowired
    private PrescriptionService prescriptionService;

    @Autowired
    private PrescriptionDetailService prescriptionDetailService;

    @Autowired
    private AuditRecordService auditRecordService;

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    /**
     * 创建处方
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Prescription>> createPrescription(
            @RequestBody Map<String, Object> requestMap) {
        try {
            // 检查权限：医生可以创建处方
            if (!isDoctorOrAdmin()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("权限不足，需要医生或管理员权限"));
            }

            logger.info("接收到的原始请求键: {}", requestMap.keySet());
            logger.info("接收到的原始请求 details 键是否存在: {}", requestMap.containsKey("details"));
            if (requestMap.containsKey("details")) {
                logger.info("details 值: {}", requestMap.get("details"));
            }
            
            // 手动解析请求
            Prescription prescription = new Prescription();
            prescription.setPatientName((String) requestMap.get("patientName"));
            prescription.setPatientAge(requestMap.get("patientAge") != null ? 
                ((Number) requestMap.get("patientAge")).intValue() : null);
            prescription.setPatientGender((String) requestMap.get("patientGender"));
            prescription.setDepartment((String) requestMap.get("department"));
            prescription.setStatus(requestMap.get("status") != null ? (String) requestMap.get("status") : "未审核");
            
            // 处理日期
            if (requestMap.get("createDate") != null) {
                Object dateObj = requestMap.get("createDate");
                if (dateObj instanceof String) {
                    prescription.setCreateDate(java.time.LocalDate.parse((String) dateObj));
                } else if (dateObj instanceof java.time.LocalDate) {
                    prescription.setCreateDate((java.time.LocalDate) dateObj);
                }
            }
            
            // 设置医生信息
            String doctorName = getCurrentUsername();
            if (requestMap.get("doctorName") != null && !((String) requestMap.get("doctorName")).isEmpty()) {
                prescription.setDoctorName((String) requestMap.get("doctorName"));
            } else {
                prescription.setDoctorName(doctorName != null ? doctorName : "系统");
            }
            
            // 手动解析 details
            Object detailsObj = requestMap.get("details");
            logger.info("details 对象: {}, 类型: {}", detailsObj, detailsObj != null ? detailsObj.getClass().getName() : "null");
            
            List<PrescriptionDetail> details = new java.util.ArrayList<>();
            if (detailsObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> detailsList = (List<Map<String, Object>>) detailsObj;
                logger.info("details 列表大小: {}", detailsList.size());
                
                for (Map<String, Object> detailMap : detailsList) {
                    PrescriptionDetail detail = new PrescriptionDetail();
                    Object medicineIdObj = detailMap.get("medicineId");
                    if (medicineIdObj != null) {
                        detail.setMedicineId(((Number) medicineIdObj).longValue());
                    }
                    Object quantityObj = detailMap.get("quantity");
                    if (quantityObj != null) {
                        detail.setQuantity(((Number) quantityObj).intValue());
                    }
                    detail.setDosage((String) detailMap.get("dosage"));
                    detail.setFrequency((String) detailMap.get("frequency"));
                    Object daysObj = detailMap.get("days");
                    if (daysObj != null) {
                        detail.setDays(((Number) daysObj).intValue());
                    } else {
                        detail.setDays(1);
                    }
                    details.add(detail);
                    logger.debug("添加明细: medicineId={}, quantity={}", detail.getMedicineId(), detail.getQuantity());
                }
            }
            
            logger.info("解析后的 details 数量: {}", details.size());
            
            if (details.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("处方明细不能为空，请至少添加一个有效的药品明细"));
            }

            Prescription savedPrescription = prescriptionService.createPrescription(prescription, details);
            return ResponseEntity.ok(ApiResponse.success("创建处方成功", savedPrescription));
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // 处理数据库约束错误
            String errorMsg = e.getMessage();
            if (errorMsg != null && errorMsg.contains("patient_gender")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("创建处方失败: 患者性别字段长度超出限制，请检查数据库表结构"));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("创建处方失败: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("创建处方失败", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("创建处方失败: " + e.getMessage()));
        }
    }

    /**
     * 获取处方列表（分页、筛选）
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<Prescription>>> getPrescriptions(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            List<Prescription> prescriptions;

            if (status != null && !status.trim().isEmpty()) {
                prescriptions = prescriptionService.findByStatus(status.trim());
            } else {
                prescriptions = prescriptionService.findAll();
            }
            
            // 按创建时间倒序排序（最新的在前）
            prescriptions.sort((p1, p2) -> {
                if (p1.getCreateTime() == null && p2.getCreateTime() == null) return 0;
                if (p1.getCreateTime() == null) return 1;
                if (p2.getCreateTime() == null) return -1;
                return p2.getCreateTime().compareTo(p1.getCreateTime());
            });

            // 简单分页实现
            int start = page * size;
            int end = Math.min(start + size, prescriptions.size());
            List<Prescription> pagedPrescriptions;
            if (start >= prescriptions.size() || prescriptions.isEmpty()) {
                pagedPrescriptions = new java.util.ArrayList<>();
            } else {
                pagedPrescriptions = prescriptions.subList(start, end);
            }

            PageResponse<Prescription> pageResponse = PageResponse.of(
                    pagedPrescriptions, prescriptions.size(), page, size);

            logger.info("查询处方列表: 总数量={}, 当前页={}, 每页大小={}, 返回数量={}", 
                    prescriptions.size(), page, size, pagedPrescriptions.size());

            return ResponseEntity.ok(ApiResponse.success(pageResponse));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取处方列表失败: " + e.getMessage()));
        }
    }

    /**
     * 获取处方详情（包含明细）
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPrescriptionById(@PathVariable Long id) {
        try {
            Prescription prescription = prescriptionService.findById(id);
            List<PrescriptionDetail> details = prescriptionDetailService.findByPrescriptionId(id);

            Map<String, Object> result = new HashMap<>();
            result.put("prescription", prescription);
            result.put("details", details);

            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 更新处方信息
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Prescription>> updatePrescription(
            @PathVariable Long id,
            @Valid @RequestBody Prescription prescription) {
        try {
            Prescription existingPrescription = prescriptionService.findById(id);

            // 更新字段
            if (prescription.getPatientName() != null) {
                existingPrescription.setPatientName(prescription.getPatientName());
            }
            if (prescription.getPatientAge() != null) {
                existingPrescription.setPatientAge(prescription.getPatientAge());
            }
            if (prescription.getPatientGender() != null) {
                existingPrescription.setPatientGender(prescription.getPatientGender());
            }
            if (prescription.getDepartment() != null) {
                existingPrescription.setDepartment(prescription.getDepartment());
            }

            // 保存更新
            Prescription updatedPrescription = prescriptionService.createPrescription(
                    existingPrescription, null);

            return ResponseEntity.ok(ApiResponse.success("更新处方成功", updatedPrescription));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("更新处方失败: " + e.getMessage()));
        }
    }

    /**
     * 提交审核（调用Python服务）
     */
    @PostMapping("/{id}/submit-audit")
    public ResponseEntity<ApiResponse<String>> submitForAudit(@PathVariable Long id) {
        try {
            // 检查权限：医生或管理员
            if (!isDoctorOrAdmin()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("权限不足，需要医生或管理员权限"));
            }

            prescriptionService.submitForAudit(id);
            return ResponseEntity.ok(ApiResponse.success("提交审核成功", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("提交审核失败: " + e.getMessage()));
        }
    }

    /**
     * 人工审核
     */
    @PostMapping("/{id}/audit")
    public ResponseEntity<ApiResponse<String>> auditPrescription(
            @PathVariable Long id,
            @Valid @RequestBody PrescriptionAuditRequest auditRequest) {
        try {
            // 检查权限：药师或管理员
            if (!isPharmacistOrAdmin()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("权限不足，需要药师或管理员权限"));
            }

            String auditResult = auditRequest.getAuditResult();
            String suggestions = auditRequest.getSuggestions();

            // 将中文审核结果转换为数据库ENUM值
            // 数据库字段类型是 enum('pass','warning','reject')
            String dbAuditResult;
            String normalizedAuditResult;
            if ("通过".equals(auditResult) || "已通过".equals(auditResult)) {
                dbAuditResult = "pass";
                normalizedAuditResult = "通过";
            } else if ("拒绝".equals(auditResult) || "已拒绝".equals(auditResult)) {
                dbAuditResult = "reject";
                normalizedAuditResult = "拒绝";
            } else if ("警告".equals(auditResult) || "warning".equalsIgnoreCase(auditResult)) {
                dbAuditResult = "warning";
                normalizedAuditResult = "警告";
            } else {
                // 如果已经是英文值，直接使用；否则默认使用 pass
                if ("pass".equalsIgnoreCase(auditResult) || "reject".equalsIgnoreCase(auditResult) || "warning".equalsIgnoreCase(auditResult)) {
                    dbAuditResult = auditResult.toLowerCase();
                    normalizedAuditResult = auditResult;
                } else {
                    dbAuditResult = "pass";
                    normalizedAuditResult = "通过";
                }
            }

            // 获取处方对象并更新
            Prescription prescription = prescriptionService.findById(id);
            
            // 更新处方状态和审核结果
            if ("通过".equals(auditResult) || "已通过".equals(auditResult)) {
                prescription.setStatus("已通过");
                prescription.setAuditResult("通过");
            } else if ("拒绝".equals(auditResult) || "已拒绝".equals(auditResult)) {
                prescription.setStatus("已拒绝");
                prescription.setAuditResult("拒绝");
            } else {
                prescription.setStatus(auditResult);
                prescription.setAuditResult(normalizedAuditResult);
            }
            
            prescription.setAuditTime(java.time.LocalDateTime.now());
            prescription.setUpdateTime(java.time.LocalDateTime.now());
            
            logger.info("更新处方审核状态: ID={}, status={}, auditResult={}", 
                    id, prescription.getStatus(), prescription.getAuditResult());
            
            try {
                prescriptionRepository.save(prescription);
                logger.info("处方状态更新成功");
            } catch (Exception e) {
                logger.error("更新处方状态失败", e);
                throw new RuntimeException("更新处方状态失败: " + e.getMessage(), e);
            }

            // 创建审核记录
            logger.info("创建审核记录: prescriptionId={}, auditResult={}, auditScore={}, issuesFound长度={}, suggestions长度={}", 
                    id, normalizedAuditResult, auditRequest.getAuditScore(),
                    auditRequest.getIssuesFound() != null ? auditRequest.getIssuesFound().length() : 0,
                    suggestions != null ? suggestions.length() : 0);
            
            // 数据库 audit_type 字段是 enum('auto','manual')
            String dbAuditType = "manual";
            
            com.hpims.model.AuditRecord auditRecord = com.hpims.model.AuditRecord.builder()
                    .prescriptionId(id)
                    .auditType(dbAuditType)
                    .auditResult(dbAuditResult)  // 使用数据库ENUM值
                    .auditScore(auditRequest.getAuditScore())
                    .issuesFound(auditRequest.getIssuesFound())
                    .suggestions(suggestions)
                    .auditor(auditRequest.getAuditor() != null ? auditRequest.getAuditor() : getCurrentUsername())
                    .auditTime(java.time.LocalDateTime.now())
                    .build();

            try {
                auditRecordService.save(auditRecord);
                logger.info("审核记录保存成功");
            } catch (Exception e) {
                logger.error("保存审核记录失败", e);
                throw new RuntimeException("保存审核记录失败: " + e.getMessage(), e);
            }

            return ResponseEntity.ok(ApiResponse.success("审核完成", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("审核失败: " + e.getMessage()));
        }
    }

    /**
     * 发药
     */
    @PostMapping("/{id}/dispense")
    public ResponseEntity<ApiResponse<String>> dispensePrescription(@PathVariable Long id) {
        try {
            // 检查权限：药师或管理员
            if (!isPharmacistOrAdmin()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("权限不足，需要药师或管理员权限"));
            }

            prescriptionService.dispense(id);
            logger.info("发药成功，处方ID: {}", id);
            return ResponseEntity.ok(ApiResponse.success("发药成功", null));
        } catch (Exception e) {
            logger.error("发药失败，处方ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("发药失败: " + e.getMessage()));
        }
    }

    /**
     * 取消处方
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<String>> cancelPrescription(@PathVariable Long id) {
        try {
            prescriptionService.findById(id); // 验证处方存在
            prescriptionService.updateStatus(id, "已取消");
            return ResponseEntity.ok(ApiResponse.success("取消处方成功", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("取消处方失败: " + e.getMessage()));
        }
    }

    /**
     * 获取处方明细
     */
    @GetMapping("/{id}/details")
    public ResponseEntity<ApiResponse<List<PrescriptionDetail>>> getPrescriptionDetails(@PathVariable Long id) {
        try {
            List<PrescriptionDetail> details = prescriptionDetailService.findByPrescriptionId(id);
            return ResponseEntity.ok(ApiResponse.success(details));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取审核历史
     */
    @GetMapping("/{id}/audit-history")
    public ResponseEntity<ApiResponse<List<com.hpims.model.AuditRecord>>> getAuditHistory(@PathVariable Long id) {
        try {
            List<com.hpims.model.AuditRecord> records = auditRecordService.findByPrescriptionId(id);
            return ResponseEntity.ok(ApiResponse.success(records));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取审核历史失败: " + e.getMessage()));
        }
    }

    /**
     * 获取当前用户名
     */
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : null;
    }

    /**
     * 检查是否为医生或管理员
     */
    private boolean isDoctorOrAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(a -> {
                    String authority = a.getAuthority();
                    return authority.equals("ROLE_ADMIN") || authority.equals("ROLE_DOCTOR");
                });
    }

    /**
     * 检查是否为药师或管理员
     */
    private boolean isPharmacistOrAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(a -> {
                    String authority = a.getAuthority();
                    return authority.equals("ROLE_ADMIN") || authority.equals("ROLE_PHARMACIST");
                });
    }
}

