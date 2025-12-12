package com.hpims.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户信息响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String username;
    private String role;
    private String realName;
    private String employeeNumber; // 职工号
    private String title; // 职称
    private String department;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

