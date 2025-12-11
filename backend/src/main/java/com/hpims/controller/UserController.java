package com.hpims.controller;

import com.hpims.dto.ApiResponse;
import com.hpims.dto.PageResponse;
import com.hpims.dto.request.RegisterRequest;
import com.hpims.dto.response.UserResponse;
import com.hpims.model.User;
import com.hpims.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户管理控制器
 */
@RestController
@RequestMapping("/users")
@CrossOrigin
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 获取用户列表（分页，管理员）
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            // 检查是否为管理员
            if (!isAdmin()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("权限不足，需要管理员权限"));
            }

            List<User> users = userService.findAll();
            
            // 简单分页实现（实际项目中应使用Pageable）
            int start = page * size;
            int end = Math.min(start + size, users.size());
            List<User> pagedUsers = users.subList(Math.min(start, users.size()), end);

            List<UserResponse> content = pagedUsers.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());

            PageResponse<UserResponse> pageResponse = PageResponse.of(
                    content, users.size(), page, size);

            return ResponseEntity.ok(ApiResponse.success(pageResponse));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取用户列表失败: " + e.getMessage()));
        }
    }

    /**
     * 创建用户（管理员）
     */
    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody RegisterRequest request) {
        try {
            // 检查是否为管理员
            if (!isAdmin()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("权限不足，需要管理员权限"));
            }

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
            return ResponseEntity.ok(ApiResponse.success("创建用户成功", convertToResponse(savedUser)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("创建用户失败: " + e.getMessage()));
        }
    }

    /**
     * 获取用户详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        try {
            User user = userService.findById(id)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));

            return ResponseEntity.ok(ApiResponse.success(convertToResponse(user)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @RequestBody RegisterRequest request) {
        try {
            User user = userService.findById(id)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));

            // 检查权限：只能更新自己的信息，或管理员可以更新任何用户
            String currentUsername = getCurrentUsername();
            if (!isAdmin() && !user.getUsername().equals(currentUsername)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("权限不足，只能更新自己的信息"));
            }

            // 更新用户信息
            if (request.getRealName() != null) {
                user.setRealName(request.getRealName());
            }
            if (request.getDepartment() != null) {
                user.setDepartment(request.getDepartment());
            }
            if (request.getPassword() != null && !request.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(request.getPassword()));
            }
            user.setUpdateTime(LocalDateTime.now());

            User updatedUser = userService.save(user);
            return ResponseEntity.ok(ApiResponse.success("更新成功", convertToResponse(updatedUser)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("更新失败: " + e.getMessage()));
        }
    }

    /**
     * 删除用户（管理员）
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable Long id) {
        try {
            // 检查是否为管理员
            if (!isAdmin()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("权限不足，需要管理员权限"));
            }

            userService.deleteById(id);
            return ResponseEntity.ok(ApiResponse.success("删除成功", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("删除失败: " + e.getMessage()));
        }
    }

    /**
     * 修改用户角色（管理员）
     */
    @PutMapping("/{id}/role")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserRole(
            @PathVariable Long id,
            @RequestBody String role) {
        try {
            // 检查是否为管理员
            if (!isAdmin()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("权限不足，需要管理员权限"));
            }

            User user = userService.findById(id)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));

            user.setRole(role);
            user.setUpdateTime(LocalDateTime.now());

            User updatedUser = userService.save(user);
            return ResponseEntity.ok(ApiResponse.success("角色更新成功", convertToResponse(updatedUser)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("更新角色失败: " + e.getMessage()));
        }
    }

    /**
     * 转换User为UserResponse
     */
    private UserResponse convertToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .realName(user.getRealName())
                .department(user.getDepartment())
                .createTime(user.getCreateTime())
                .updateTime(user.getUpdateTime())
                .build();
    }

    /**
     * 获取当前用户名
     */
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : null;
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
}

