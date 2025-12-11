package com.hpims.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码哈希生成工具
 * 用于生成正确的BCrypt密码哈希
 */
public class PasswordHashGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // 生成密码 "123456" 的BCrypt哈希
        String password = "123456";
        String hash = encoder.encode(password);
        
        System.out.println("==========================================");
        System.out.println("密码: " + password);
        System.out.println("BCrypt哈希: " + hash);
        System.out.println("哈希长度: " + hash.length());
        System.out.println("==========================================");
        
        // 验证生成的哈希
        boolean matches = encoder.matches(password, hash);
        System.out.println("验证结果: " + (matches ? "✓ 成功" : "✗ 失败"));
        
        // 生成SQL更新语句
        System.out.println("\nSQL更新语句:");
        System.out.println("UPDATE users SET password = '" + hash + "' WHERE username = 'admin';");
        System.out.println("UPDATE users SET password = '" + hash + "' WHERE username = 'doctor';");
        System.out.println("UPDATE users SET password = '" + hash + "' WHERE username = 'pharmacist';");
    }
}

