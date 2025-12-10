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
 * TODO: 李教博需要实现的认证控制器
 * ===================================
 *
 * 认证相关控制器 (需要创建):
 * TODO: AuthController - 用户认证控制器
 *   - POST /auth/login - 用户登录
 *   - POST /auth/register - 用户注册
 *   - GET /auth/me - 获取当前用户信息
 *   - POST /auth/logout - 用户登出
 *   - POST /auth/refresh - JWT令牌刷新
 *
 * 用户管理控制器 (需要创建):
 * TODO: UserController - 用户管理控制器
 *   - GET /users - 获取用户列表 (管理员)
 *   - POST /users - 创建用户 (管理员)
 *   - PUT /users/{id} - 更新用户信息
 *   - DELETE /users/{id} - 删除用户 (管理员)
 *   - PUT /users/{id}/role - 修改用户角色 (管理员)
 *
 * 权限验证注解 (需要创建):
 * TODO: @RequireRole - 角色权限注解
 * TODO: @RequirePermission - 权限验证注解
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
                .antMatchers("/auth/login", "/auth/register").permitAll()
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
