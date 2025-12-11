package com.hpims.controller;

import com.hpims.dto.ApiResponse;
import com.hpims.dto.PageResponse;
import com.hpims.model.Medicine;
import com.hpims.service.MedicineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 药品管理控制器
 */
@RestController
@RequestMapping("/medicines")
@CrossOrigin
public class MedicineController {

    @Autowired
    private MedicineService medicineService;

    /**
     * 获取药品列表（分页、搜索、筛选）
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<Medicine>>> getMedicines(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            List<Medicine> medicines;

            // 根据关键词搜索
            if (keyword != null && !keyword.trim().isEmpty()) {
                medicines = medicineService.search(keyword.trim());
            }
            // 根据分类筛选
            else if (category != null && !category.trim().isEmpty()) {
                medicines = medicineService.findByCategory(category.trim());
            }
            // 获取所有
            else {
                medicines = medicineService.findAll();
            }

            // 简单分页实现
            int start = page * size;
            int end = Math.min(start + size, medicines.size());
            List<Medicine> pagedMedicines = medicines.subList(Math.min(start, medicines.size()), end);

            PageResponse<Medicine> pageResponse = PageResponse.of(
                    pagedMedicines, medicines.size(), page, size);

            return ResponseEntity.ok(ApiResponse.success(pageResponse));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取药品列表失败: " + e.getMessage()));
        }
    }

    /**
     * 获取药品详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Medicine>> getMedicineById(@PathVariable Long id) {
        try {
            Medicine medicine = medicineService.findById(id);
            return ResponseEntity.ok(ApiResponse.success(medicine));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 创建药品（管理员/药师）
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Medicine>> createMedicine(@Valid @RequestBody Medicine medicine) {
        try {
            // 检查权限：管理员或药师
            if (!isAdminOrPharmacist()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("权限不足，需要管理员或药师权限"));
            }

            Medicine savedMedicine = medicineService.save(medicine);
            return ResponseEntity.ok(ApiResponse.success("创建药品成功", savedMedicine));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("创建药品失败: " + e.getMessage()));
        }
    }

    /**
     * 更新药品信息
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Medicine>> updateMedicine(
            @PathVariable Long id,
            @Valid @RequestBody Medicine medicine) {
        try {
            // 检查权限：管理员或药师
            if (!isAdminOrPharmacist()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("权限不足，需要管理员或药师权限"));
            }

            Medicine updatedMedicine = medicineService.update(id, medicine);
            return ResponseEntity.ok(ApiResponse.success("更新药品成功", updatedMedicine));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("更新药品失败: " + e.getMessage()));
        }
    }

    /**
     * 删除药品（管理员）
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteMedicine(@PathVariable Long id) {
        try {
            // 检查权限：管理员
            if (!isAdmin()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("权限不足，需要管理员权限"));
            }

            medicineService.delete(id);
            return ResponseEntity.ok(ApiResponse.success("删除药品成功", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("删除药品失败: " + e.getMessage()));
        }
    }

    /**
     * 搜索药品
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<Medicine>>> searchMedicines(
            @RequestParam String keyword) {
        try {
            List<Medicine> medicines = medicineService.search(keyword);
            return ResponseEntity.ok(ApiResponse.success(medicines));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("搜索药品失败: " + e.getMessage()));
        }
    }

    /**
     * 按分类查询
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<List<Medicine>>> getMedicinesByCategory(
            @PathVariable String category) {
        try {
            List<Medicine> medicines = medicineService.findByCategory(category);
            return ResponseEntity.ok(ApiResponse.success(medicines));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("查询药品失败: " + e.getMessage()));
        }
    }

    /**
     * 查询低库存药品
     */
    @GetMapping("/low-stock")
    public ResponseEntity<ApiResponse<List<Medicine>>> getLowStockMedicines() {
        try {
            List<Medicine> allMedicines = medicineService.findAll();
            List<Medicine> lowStockMedicines = allMedicines.stream()
                    .filter(m -> m.getMinStock() != null && m.getStockQuantity() != null
                            && m.getStockQuantity() <= m.getMinStock())
                    .collect(java.util.stream.Collectors.toList());
            return ResponseEntity.ok(ApiResponse.success(lowStockMedicines));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("查询低库存药品失败: " + e.getMessage()));
        }
    }

    /**
     * 检查是否为管理员
     */
    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
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

