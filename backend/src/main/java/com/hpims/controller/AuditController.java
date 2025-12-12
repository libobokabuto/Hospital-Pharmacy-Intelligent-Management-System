package com.hpims.controller;

import com.hpims.dto.ApiResponse;
import com.hpims.dto.PageResponse;
import com.hpims.model.AuditRecord;
import com.hpims.service.AuditRecordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(AuditController.class);

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
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            logger.info("审核统计接口被调用，当前认证用户: {}", auth != null ? auth.getName() : "未认证");
            
            // 检查权限：管理员或药师
            if (!isAdminOrPharmacist()) {
                String userInfo = auth != null ? auth.getName() + ", authorities: " + auth.getAuthorities() : "未认证";
                logger.warn("权限不足，访问审核统计接口需要管理员或药师权限。用户信息: {}", userInfo);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("权限不足，需要管理员或药师权限"));
            }

            logger.debug("开始查询所有审核记录");
            List<AuditRecord> allRecords = auditRecordService.findAll();
            if (allRecords == null) {
                logger.warn("审核记录列表为null，初始化为空列表");
                allRecords = new java.util.ArrayList<>();
            }
            logger.debug("查询到审核记录数量: {}", allRecords.size());

            int totalRecords = allRecords.size();
            int passedCount = 0;
            int rejectedCount = 0;
            int pendingCount = 0;
            int autoAuditCount = 0;
            int manualAuditCount = 0;

            for (AuditRecord record : allRecords) {
                if (record == null) {
                    continue;
                }
                
                String result = record.getAuditResult();
                if (result != null) {
                    if ("通过".equals(result) || "已通过".equals(result)) {
                        passedCount++;
                    } else if ("拒绝".equals(result) || "已拒绝".equals(result)) {
                        rejectedCount++;
                    } else if ("待审核".equals(result)) {
                        pendingCount++;
                    }
                }

                String auditType = record.getAuditType();
                if (auditType != null) {
                    if ("自动审核".equals(auditType)) {
                        autoAuditCount++;
                    } else if ("人工审核".equals(auditType)) {
                        manualAuditCount++;
                    }
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

            logger.debug("审核统计查询成功，总记录数: {}", totalRecords);
            return ResponseEntity.ok(ApiResponse.success(statistics));
        } catch (Exception e) {
            logger.error("获取审核统计失败", e);
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

