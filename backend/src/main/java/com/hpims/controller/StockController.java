package com.hpims.controller;

import com.hpims.dto.ApiResponse;
import com.hpims.dto.PageResponse;
import com.hpims.model.Medicine;
import com.hpims.model.StockIn;
import com.hpims.model.StockOut;
import com.hpims.service.MedicineService;
import com.hpims.service.StockInService;
import com.hpims.service.StockOutService;
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
 * 库存管理控制器
 */
@RestController
@RequestMapping("/stock")
@CrossOrigin
public class StockController {

    @Autowired
    private StockInService stockInService;

    @Autowired
    private StockOutService stockOutService;

    @Autowired
    private MedicineService medicineService;

    /**
     * 药品入库
     */
    @PostMapping("/in")
    public ResponseEntity<ApiResponse<StockIn>> stockIn(@Valid @RequestBody StockIn stockIn) {
        try {
            // 检查权限：管理员或药师
            if (!isAdminOrPharmacist()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("权限不足，需要管理员或药师权限"));
            }

            // 设置操作人
            String operator = getCurrentUsername();
            stockIn.setOperator(operator != null ? operator : "系统");

            StockIn savedStockIn = stockInService.createStockIn(stockIn);
            return ResponseEntity.ok(ApiResponse.success("入库成功", savedStockIn));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("入库失败: " + e.getMessage()));
        }
    }

    /**
     * 药品出库
     */
    @PostMapping("/out")
    public ResponseEntity<ApiResponse<StockOut>> stockOut(@Valid @RequestBody StockOut stockOut) {
        try {
            // 检查权限：管理员或药师
            if (!isAdminOrPharmacist()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("权限不足，需要管理员或药师权限"));
            }

            // 设置操作人
            String operator = getCurrentUsername();
            stockOut.setOperator(operator != null ? operator : "系统");

            StockOut savedStockOut = stockOutService.createStockOut(stockOut);
            return ResponseEntity.ok(ApiResponse.success("出库成功", savedStockOut));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("出库失败: " + e.getMessage()));
        }
    }

    /**
     * 获取入库记录列表（分页、筛选）
     */
    @GetMapping("/in")
    public ResponseEntity<ApiResponse<PageResponse<StockIn>>> getStockInRecords(
            @RequestParam(required = false) Long medicineId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            List<StockIn> records;

            if (medicineId != null) {
                records = stockInService.findByMedicineId(medicineId);
            } else if (startDate != null && endDate != null) {
                records = stockInService.findByDateRange(startDate, endDate);
            } else {
                records = stockInService.findAll();
            }

            // 简单分页实现
            int start = page * size;
            int end = Math.min(start + size, records.size());
            List<StockIn> pagedRecords = records.subList(Math.min(start, records.size()), end);

            PageResponse<StockIn> pageResponse = PageResponse.of(
                    pagedRecords, records.size(), page, size);

            return ResponseEntity.ok(ApiResponse.success(pageResponse));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取入库记录失败: " + e.getMessage()));
        }
    }

    /**
     * 获取出库记录列表（分页、筛选）
     */
    @GetMapping("/out")
    public ResponseEntity<ApiResponse<PageResponse<StockOut>>> getStockOutRecords(
            @RequestParam(required = false) Long medicineId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            List<StockOut> records;

            if (medicineId != null) {
                records = stockOutService.findByMedicineId(medicineId);
            } else if (startDate != null && endDate != null) {
                records = stockOutService.findByDateRange(startDate, endDate);
            } else {
                records = stockOutService.findAll();
            }

            // 简单分页实现
            int start = page * size;
            int end = Math.min(start + size, records.size());
            List<StockOut> pagedRecords = records.subList(Math.min(start, records.size()), end);

            PageResponse<StockOut> pageResponse = PageResponse.of(
                    pagedRecords, records.size(), page, size);

            return ResponseEntity.ok(ApiResponse.success(pageResponse));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取出库记录失败: " + e.getMessage()));
        }
    }

    /**
     * 获取入库记录详情
     */
    @GetMapping("/in/{id}")
    public ResponseEntity<ApiResponse<StockIn>> getStockInById(@PathVariable Long id) {
        try {
            // 这里需要添加findById方法到StockInService，暂时返回错误
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                    .body(ApiResponse.error("功能待实现"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取出库记录详情
     */
    @GetMapping("/out/{id}")
    public ResponseEntity<ApiResponse<StockOut>> getStockOutById(@PathVariable Long id) {
        try {
            // 这里需要添加findById方法到StockOutService，暂时返回错误
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                    .body(ApiResponse.error("功能待实现"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 查询药品库存
     */
    @GetMapping("/medicine/{medicineId}")
    public ResponseEntity<ApiResponse<Medicine>> getMedicineStock(@PathVariable Long medicineId) {
        try {
            Medicine medicine = medicineService.findById(medicineId);
            return ResponseEntity.ok(ApiResponse.success(medicine));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 库存统计
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStockStatistics() {
        try {
            List<Medicine> allMedicines = medicineService.findAll();
            
            int totalMedicines = allMedicines.size();
            int lowStockCount = 0;
            int outOfStockCount = 0;
            int totalStockValue = 0;

            for (Medicine medicine : allMedicines) {
                if (medicine.getMinStock() != null && medicine.getStockQuantity() != null) {
                    if (medicine.getStockQuantity() <= 0) {
                        outOfStockCount++;
                    } else if (medicine.getStockQuantity() <= medicine.getMinStock()) {
                        lowStockCount++;
                    }
                }
                if (medicine.getPrice() != null && medicine.getStockQuantity() != null) {
                    totalStockValue += medicine.getPrice().multiply(
                            java.math.BigDecimal.valueOf(medicine.getStockQuantity())).intValue();
                }
            }

            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalMedicines", totalMedicines);
            statistics.put("lowStockCount", lowStockCount);
            statistics.put("outOfStockCount", outOfStockCount);
            statistics.put("totalStockValue", totalStockValue);

            return ResponseEntity.ok(ApiResponse.success(statistics));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取库存统计失败: " + e.getMessage()));
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

