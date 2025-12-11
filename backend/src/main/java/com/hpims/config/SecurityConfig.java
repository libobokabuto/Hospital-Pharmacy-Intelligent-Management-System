package com.hpims.config;

import com.hpims.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Spring Security配置类
 * 
 * 已实现功能:
 * - JWT认证过滤器集成
 * - CORS跨域配置
 * - 密码加密器（BCrypt）
 * - 认证管理器配置
 * - 公开路径配置: /auth/login, /auth/register, /swagger-ui/**
 * - 其他路径需要JWT认证
 *
 * 控制器开发任务 (参考 javaWork.md 第四部分)
 * ===================================
 *
 * ✅ 认证相关控制器 (已完成):
 * ✅ AuthController - 用户认证控制器
 *   - POST /auth/login - 用户登录（返回JWT，使用UserService和JwtUtil）✅
 *   - POST /auth/register - 用户注册（使用UserService和PasswordEncoder）✅
 *   - GET /auth/me - 获取当前用户信息（从SecurityContext获取）✅
 *   - POST /auth/logout - 登出（可选，JWT无状态，客户端删除token即可）✅
 *   - POST /auth/refresh - 刷新JWT令牌（验证旧token，生成新token）✅
 *
 * ✅ 用户管理控制器 (已完成):
 * ✅ UserController - 用户管理控制器
 *   - GET /users - 获取用户列表（分页，管理员，使用UserService）✅
 *   - POST /users - 创建用户（管理员，使用UserService和PasswordEncoder）✅
 *   - GET /users/{id} - 获取用户详情（使用UserService.findById）✅
 *   - PUT /users/{id} - 更新用户信息（使用UserService.save）✅
 *   - DELETE /users/{id} - 删除用户（管理员，使用UserService.deleteById）✅
 *   - PUT /users/{id}/role - 修改用户角色（管理员，使用UserService）✅
 *
 * ✅ 其他控制器 (已完成):
 * ✅ MedicineController - 药品管理API
 *   - MedicineService已实现 ✅
 *   - 接口: GET /medicines, POST /medicines, PUT /medicines/{id}, DELETE /medicines/{id} ✅
 *   - 额外接口: GET /medicines/search, GET /medicines/category/{category}, GET /medicines/low-stock ✅
 *   - 权限: 管理员/药师可创建/更新/删除，所有用户可查询 ✅
 *
 * ✅ StockController - 库存管理API（入库/出库）
 *   - StockInService和StockOutService已实现 ✅
 *   - 接口: POST /stock/in, POST /stock/out, GET /stock/in, GET /stock/out ✅
 *   - 额外接口: GET /stock/medicine/{medicineId}, GET /stock/statistics ✅
 *   - 权限: 管理员/药师可操作，所有用户可查询 ✅
 *
 * ✅ PrescriptionController - 处方管理API
 *   - PrescriptionService和PrescriptionDetailService已实现 ✅
 *   - 接口: POST /prescriptions, GET /prescriptions, POST /prescriptions/{id}/submit-audit ✅
 *   - 额外接口: PUT /prescriptions/{id}, POST /prescriptions/{id}/audit, POST /prescriptions/{id}/dispense ✅
 *   - 额外接口: POST /prescriptions/{id}/cancel, GET /prescriptions/{id}/details, GET /prescriptions/{id}/audit-history ✅
 *   - 权限: 医生可创建，药师可审核/发药，所有用户可查询 ✅
 *
 * ✅ AuditController - 审核记录管理API
 *   - AuditRecordService已实现 ✅
 *   - 接口: GET /audit/records, GET /audit/records/{id} ✅
 *   - 额外接口: GET /audit/records/prescription/{prescriptionId}, GET /audit/statistics ✅
 *   - 权限: 管理员/药师可查看 ✅
 *
 * ⏳ 权限验证注解 (可选，待实现):
 * TODO: @RequireRole("ADMIN") - 角色权限注解（方法级权限控制）
 *   说明: 当前使用方法内权限检查（isAdmin(), isAdminOrPharmacist()等），
 *         如需更优雅的方式，可考虑实现自定义注解
 * TODO: @RequirePermission("USER_MANAGE") - 权限验证注解（细粒度权限控制）
 *   说明: 当前使用基于角色的权限控制（RBAC），如需更细粒度的权限控制，可考虑实现此注解
 * 
 * 注意: 当前配置中，除公开路径外，所有请求都需要JWT认证。
 *       实现Controller时，可以通过SecurityContext获取当前认证用户信息。
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors().configurationSource(corsConfigurationSource())
            .and()
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
                // 允许访问的路径
                .antMatchers("/auth/login", "/auth/register", "/auth/generate-hash").permitAll()
                .antMatchers("/swagger-ui/**", "/api-docs/**").permitAll()
                .antMatchers("/public/**").permitAll()
                // 其他请求需要认证
                .anyRequest().authenticated()
            .and()
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("http://localhost:3000", "http://127.0.0.1:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
