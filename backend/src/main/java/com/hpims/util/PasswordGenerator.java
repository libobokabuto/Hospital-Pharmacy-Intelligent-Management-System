package com.hpims.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码生成工具类
 * 用于生成BCrypt加密的密码哈希值
 */
public class PasswordGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // 要设置的密码
        String password = "123456";
        
        // 生成加密后的密码
        String encodedPassword = encoder.encode(password);
        
        System.out.println("========================================");
        System.out.println("原始密码: " + password);
        System.out.println("加密后的密码: " + encodedPassword);
        System.out.println("========================================");
        System.out.println("\n请复制上面的加密密码，用于更新数据库");
    }
}

