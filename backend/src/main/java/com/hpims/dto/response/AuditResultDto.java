package com.hpims.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

/**
 * Python审核服务返回的审核结果DTO
 * 用于映射Python服务返回的JSON数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditResultDto {
    
    /**
     * 审核结果：PASS（通过）、WARNING（警告）、REJECT（拒绝）
     */
    private String result;
    
    /**
     * 审核得分（0-100）
     */
    private Double score;
    
    /**
     * 发现的问题列表
     */
    private List<AuditIssueDto> issues;
    
    /**
     * 审核建议
     */
    private String suggestions;
    
    /**
     * 审核时间
     */
    private String auditTime;
    
    /**
     * 审核问题详情DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AuditIssueDto {
        /**
         * 问题类型
         */
        private String issueType;
        
        /**
         * 严重程度：HIGH、MEDIUM、LOW
         */
        private String severity;
        
        /**
         * 问题描述
         */
        private String description;
        
        /**
         * 建议
         */
        private String suggestion;
        
        /**
         * 药品名称
         */
        private String drugName;
        
        /**
         * 相关药品列表
         */
        private List<String> relatedDrugs;
    }
    
    /**
     * Python服务响应包装类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuditServiceResponse {
        private Boolean success;
        private String message;
        private AuditResultDto data;
    }
}

