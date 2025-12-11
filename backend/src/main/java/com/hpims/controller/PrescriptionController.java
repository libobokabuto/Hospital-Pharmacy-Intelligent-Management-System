package com.hpims.controller;

import com.hpims.dto.ApiResponse;
import com.hpims.dto.PageResponse;
import com.hpims.model.Prescription;
import com.hpims.model.PrescriptionDetail;
import com.hpims.service.PrescriptionService;
import com.hpims.service.PrescriptionDetailService;
import com.hpims.service.AuditRecordService;
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

    @Autowired
    private PrescriptionService prescriptionService;

    @Autowired
    private PrescriptionDetailService prescriptionDetailService;

    @Autowired
    private AuditRecordService auditRecordService;

    /**
     * 创建处方
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Prescription>> createPrescription(
            @Valid @RequestBody Prescription prescription,
            @RequestBody(required = false) List<PrescriptionDetail> details) {
        try {
            // 检查权限：医生可以创建处方
            if (!isDoctorOrAdmin()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("权限不足，需要医生或管理员权限"));
            }

            // 设置医生信息
            String doctorName = getCurrentUsername();
            if (prescription.getDoctorName() == null || prescription.getDoctorName().isEmpty()) {
                prescription.setDoctorName(doctorName != null ? doctorName : "系统");
            }

            Prescription savedPrescription = prescriptionService.createPrescription(prescription, details);
            return ResponseEntity.ok(ApiResponse.success("创建处方成功", savedPrescription));
        } catch (Exception e) {
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

            // 简单分页实现
            int start = page * size;
            int end = Math.min(start + size, prescriptions.size());
            List<Prescription> pagedPrescriptions = prescriptions.subList(Math.min(start, prescriptions.size()), end);

            PageResponse<Prescription> pageResponse = PageResponse.of(
                    pagedPrescriptions, prescriptions.size(), page, size);

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
            @RequestBody Map<String, String> auditData) {
        try {
            // 检查权限：药师或管理员
            if (!isPharmacistOrAdmin()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("权限不足，需要药师或管理员权限"));
            }

            String auditResult = auditData.get("auditResult");
            String suggestions = auditData.get("suggestions");

            // 更新处方状态
            if ("通过".equals(auditResult) || "已通过".equals(auditResult)) {
                prescriptionService.updateStatus(id, "已通过");
            } else if ("拒绝".equals(auditResult) || "已拒绝".equals(auditResult)) {
                prescriptionService.updateStatus(id, "已拒绝");
            }

            // 创建审核记录
            com.hpims.model.AuditRecord auditRecord = com.hpims.model.AuditRecord.builder()
                    .prescriptionId(id)
                    .auditType("人工审核")
                    .auditResult(auditResult)
                    .suggestions(suggestions)
                    .auditor(getCurrentUsername())
                    .auditTime(java.time.LocalDateTime.now())
                    .build();

            auditRecordService.save(auditRecord);

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
            return ResponseEntity.ok(ApiResponse.success("发药成功", null));
        } catch (Exception e) {
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

