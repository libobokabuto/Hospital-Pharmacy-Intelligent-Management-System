package com.hpims.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 药品信息响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicineResponse {
    private Long id;
    private String name;
    private String genericName;
    private String specification;
    private String manufacturer;
    private BigDecimal price;
    private Integer stockQuantity;
    private Integer minStock;
    private String category;
    private String approvalNumber;
    private String indication; // 适应症
    private String contraindication; // 禁忌症
    private String interactions; // 药物相互作用
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

