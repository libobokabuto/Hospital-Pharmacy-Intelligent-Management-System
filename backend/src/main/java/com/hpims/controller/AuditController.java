package com.hpims.controller;

import com.hpims.dto.ApiResponse;
import com.hpims.dto.PageResponse;
import com.hpims.model.AuditRecord;
import com.hpims.service.AuditRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 审核记录控制器
 */
@RestController
@RequestMapping("/audit")
@CrossOrigin
public class AuditController {

    @Autowired
    private AuditRecordService auditRecordService;

    /**
     * 获取审核记录列表（分页、筛选）
     */
    @GetMapping("/records")
    public ResponseEntity<ApiResponse<PageResponse<AuditRecord>>> getAuditRecords(
            @RequestParam(required = false) String auditResult,
            @RequestParam(required = false) Long prescriptionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            // 检查权限：管理员或药师
            if (!isAdminOrPharmacist()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("权限不足，需要管理员或药师权限"));
            }

            List<AuditRecord> records;

            if (prescriptionId != null) {
                records = auditRecordService.findByPrescriptionId(prescriptionId);
            } else if (auditResult != null && !auditResult.trim().isEmpty()) {
                records = auditRecordService.findByAuditResult(auditResult.trim());
            } else {
                records = auditRecordService.findAll();
            }

            // 简单分页实现
            int start = page * size;
            int end = Math.min(start + size, records.size());
            List<AuditRecord> pagedRecords = records.subList(Math.min(start, records.size()), end);

            PageResponse<AuditRecord> pageResponse = PageResponse.of(
                    pagedRecords, records.size(), page, size);

            return ResponseEntity.ok(ApiResponse.success(pageResponse));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取审核记录失败: " + e.getMessage()));
        }
    }

    /**
     * 获取审核记录详情
     */
    @GetMapping("/records/{id}")
    public ResponseEntity<ApiResponse<AuditRecord>> getAuditRecordById(@PathVariable Long id) {
        try {
            // 检查权限：管理员或药师
            if (!isAdminOrPharmacist()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("权限不足，需要管理员或药师权限"));
            }

            AuditRecord record = auditRecordService.findById(id);
            return ResponseEntity.ok(ApiResponse.success(record));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取处方的审核历史
     */
    @GetMapping("/records/prescription/{prescriptionId}")
    public ResponseEntity<ApiResponse<List<AuditRecord>>> getPrescriptionAuditHistory(
            @PathVariable Long prescriptionId) {
        try {
            // 检查权限：管理员或药师
            if (!isAdminOrPharmacist()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("权限不足，需要管理员或药师权限"));
            }

            List<AuditRecord> records = auditRecordService.findByPrescriptionId(prescriptionId);
            return ResponseEntity.ok(ApiResponse.success(records));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取审核历史失败: " + e.getMessage()));
        }
    }

    /**
     * 审核统计
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAuditStatistics() {
        try {
            // 检查权限：管理员或药师
            if (!isAdminOrPharmacist()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("权限不足，需要管理员或药师权限"));
            }

            List<AuditRecord> allRecords = auditRecordService.findAll();

            int totalRecords = allRecords.size();
            int passedCount = 0;
            int rejectedCount = 0;
            int pendingCount = 0;
            int autoAuditCount = 0;
            int manualAuditCount = 0;

            for (AuditRecord record : allRecords) {
                String result = record.getAuditResult();
                if ("通过".equals(result) || "已通过".equals(result)) {
                    passedCount++;
                } else if ("拒绝".equals(result) || "已拒绝".equals(result)) {
                    rejectedCount++;
                } else if ("待审核".equals(result)) {
                    pendingCount++;
                }

                if ("自动审核".equals(record.getAuditType())) {
                    autoAuditCount++;
                } else if ("人工审核".equals(record.getAuditType())) {
                    manualAuditCount++;
                }
            }

            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalRecords", totalRecords);
            statistics.put("passedCount", passedCount);
            statistics.put("rejectedCount", rejectedCount);
            statistics.put("pendingCount", pendingCount);
            statistics.put("autoAuditCount", autoAuditCount);
            statistics.put("manualAuditCount", manualAuditCount);
            statistics.put("passRate", totalRecords > 0 ? (double) passedCount / totalRecords * 100 : 0);

            return ResponseEntity.ok(ApiResponse.success(statistics));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取审核统计失败: " + e.getMessage()));
        }
    }

    /**
     * 检查是否为管理员或药师
     */
    private boolean isAdminOrPharmacist() {
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

