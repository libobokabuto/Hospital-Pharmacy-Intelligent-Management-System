package com.hpims.controller;

import com.hpims.dto.ApiResponse;
import com.hpims.dto.request.LoginRequest;
import com.hpims.dto.request.RegisterRequest;
import com.hpims.dto.request.RefreshTokenRequest;
import com.hpims.dto.response.LoginResponse;
import com.hpims.dto.response.UserResponse;
import com.hpims.model.User;
import com.hpims.security.JwtUtil;
import com.hpims.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/auth")
@CrossOrigin
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        try {
            // 先检查用户是否存在
            User user = userService.findByUsername(request.getUsername());
            if (user == null) {
                System.out.println("登录失败: 用户不存在 - " + request.getUsername());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("用户名或密码错误"));
            }

            System.out.println("尝试登录用户: " + request.getUsername());
            System.out.println("前端发送的密码长度: " + (request.getPassword() != null ? request.getPassword().length() : 0));
            System.out.println("前端发送的密码（隐藏）: " + (request.getPassword() != null ? "*".repeat(request.getPassword().length()) : "null"));
            System.out.println("数据库中的密码哈希长度: " + user.getPassword().length());
            System.out.println("数据库中的密码哈希: " + user.getPassword());
            System.out.println("密码哈希格式检查: " + (user.getPassword().startsWith("$2a$") || user.getPassword().startsWith("$2y$") ? "正确" : "错误"));
            
            // 手动测试密码验证
            boolean matches = passwordEncoder.matches(request.getPassword(), user.getPassword());
            System.out.println("手动密码验证结果: " + matches);
            if (!matches) {
                System.out.println("密码不匹配！尝试的密码: " + request.getPassword());
                System.out.println("存储的哈希: " + user.getPassword());
            }

            // 验证用户
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 获取用户信息
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User authenticatedUser = userService.findByUsername(userDetails.getUsername());

            if (authenticatedUser == null) {
                System.out.println("登录失败: 认证后用户不存在");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("用户不存在"));
            }

            // 生成JWT令牌
            String token = jwtUtil.generateToken(authenticatedUser.getUsername(), authenticatedUser.getRole());

            // 构建响应
            LoginResponse response = LoginResponse.builder()
                    .token(token)
                    .username(authenticatedUser.getUsername())
                    .role(authenticatedUser.getRole())
                    .realName(authenticatedUser.getRealName())
                    .userId(authenticatedUser.getId())
                    .build();

            System.out.println("登录成功: " + authenticatedUser.getUsername());
            return ResponseEntity.ok(ApiResponse.success("登录成功", response));
        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            System.out.println("登录失败: 密码错误 - " + request.getUsername());
            System.out.println("错误详情: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("用户名或密码错误"));
        } catch (org.springframework.security.core.userdetails.UsernameNotFoundException e) {
            System.out.println("登录失败: 用户未找到 - " + request.getUsername());
            System.out.println("错误详情: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("用户名或密码错误"));
        } catch (Exception e) {
            System.out.println("登录失败: 未知错误 - " + request.getUsername());
            System.out.println("错误类型: " + e.getClass().getName());
            System.out.println("错误消息: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("用户名或密码错误"));
        }
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        try {
            // 检查用户名是否已存在
            if (userService.existsByUsername(request.getUsername())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("用户名已存在"));
            }

            // 创建新用户
            User user = User.builder()
                    .username(request.getUsername())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .role(request.getRole())
                    .realName(request.getRealName())
                    .department(request.getDepartment())
                    .createTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .build();

            User savedUser = userService.save(user);

            // 构建响应
            UserResponse response = UserResponse.builder()
                    .id(savedUser.getId())
                    .username(savedUser.getUsername())
                    .role(savedUser.getRole())
                    .realName(savedUser.getRealName())
                    .department(savedUser.getDepartment())
                    .createTime(savedUser.getCreateTime())
                    .updateTime(savedUser.getUpdateTime())
                    .build();

            return ResponseEntity.ok(ApiResponse.success("注册成功", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("注册失败: " + e.getMessage()));
        }
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            User user = userService.findByUsername(username);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("用户不存在"));
            }

            UserResponse response = UserResponse.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .role(user.getRole())
                    .realName(user.getRealName())
                    .department(user.getDepartment())
                    .createTime(user.getCreateTime())
                    .updateTime(user.getUpdateTime())
                    .build();

            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("获取用户信息失败"));
        }
    }

    /**
     * 刷新JWT令牌
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        try {
            String token = request.getToken();
            String username = jwtUtil.extractUsername(token);

            // 验证令牌
            if (!jwtUtil.validateToken(token, username)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("令牌无效或已过期"));
            }

            // 获取用户信息
            User user = userService.findByUsername(username);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("用户不存在"));
            }

            // 生成新令牌
            String newToken = jwtUtil.generateToken(user.getUsername(), user.getRole());

            LoginResponse response = LoginResponse.builder()
                    .token(newToken)
                    .username(user.getUsername())
                    .role(user.getRole())
                    .realName(user.getRealName())
                    .userId(user.getId())
                    .build();

            return ResponseEntity.ok(ApiResponse.success("令牌刷新成功", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("令牌刷新失败: " + e.getMessage()));
        }
    }

    /**
     * 登出（可选，JWT无状态，客户端删除token即可）
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(ApiResponse.success("登出成功", null));
    }

    /**
     * 临时调试端点：生成密码哈希
     * 用于生成正确的BCrypt密码哈希
     * 访问: GET /auth/generate-hash?password=123456
     */
    @GetMapping("/generate-hash")
    public ResponseEntity<String> generateHash(@RequestParam(defaultValue = "123456") String password) {
        String hash = passwordEncoder.encode(password);
        String sql = String.format(
            "UPDATE users SET password = '%s' WHERE username IN ('admin', 'doctor', 'pharmacist');",
            hash
        );
        
        String result = "========================================\n" +
                       "密码: " + password + "\n" +
                       "BCrypt哈希: " + hash + "\n" +
                       "哈希长度: " + hash.length() + "\n" +
                       "========================================\n\n" +
                       "SQL更新语句:\n" +
                       sql + "\n" +
                       "========================================";
        
        return ResponseEntity.ok(result);
    }
}

