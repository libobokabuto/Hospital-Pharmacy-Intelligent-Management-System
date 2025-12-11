package com.hpims.security;

import com.hpims.config.JwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT工具类
 * 
 * 提供JWT令牌的生成、验证、解析等功能
 * 
 * 已实现功能:
 * - generateToken(username, role) - 生成JWT令牌
 * - validateToken(token, username) - 验证令牌有效性
 * - extractUsername(token) - 提取用户名
 * - extractRole(token) - 提取用户角色
 * - extractExpiration(token) - 提取过期时间
 * 
 * 项目开发状态 (参考 javaWork.md)
 * ====================================
 *
 * ✅ 一、实体类（Model 层）- 7个已完成
 * ✅ User - 用户实体类
 * ✅ Medicine - 药品信息实体类
 * ✅ StockIn - 入库记录实体类
 * ✅ StockOut - 出库记录实体类
 * ✅ Prescription - 处方主表实体类
 * ✅ PrescriptionDetail - 处方明细实体类
 * ✅ AuditRecord - 审核记录实体类
 *
 * ✅ 二、数据访问层（Repository）- 7个已完成
 * ✅ UserRepository - 用户数据访问接口
 *   - findByUsername, findByRole, existsByUsername
 * ✅ MedicineRepository - 药品数据访问接口
 *   - findByName, findByCategory, findByManufacturer, findByNameContaining
 * ✅ StockInRepository - 入库记录数据访问接口
 *   - findByMedicineId, findByInDateBetween, findByOperator
 * ✅ StockOutRepository - 出库记录数据访问接口
 *   - findByMedicineId, findByOutDateBetween, findByReason
 * ✅ PrescriptionRepository - 处方数据访问接口
 *   - findByPrescriptionNumber, findByStatus, findByPatientName, findByDoctorName, findByCreateDateBetween
 * ✅ PrescriptionDetailRepository - 处方明细数据访问接口
 *   - findByPrescriptionId, findByMedicineId
 * ✅ AuditRecordRepository - 审核记录数据访问接口
 *   - findByPrescriptionId, findByAuditResult, findByAuditTimeBetween
 *
 * ✅ 三、安全相关组件 - 已完成
 * ✅ JwtUtil - JWT工具类（本类）
 * ✅ JwtAuthenticationFilter - JWT认证过滤器
 * ✅ UserDetailsServiceImpl - 用户详情服务实现
 * ✅ SecurityConfig - Spring Security配置
 * ✅ JwtConfig - JWT配置类
 *
 * ⏳ 四、业务逻辑层（Service）- 1个已完成，5个待实现
 * ✅ UserService - 用户业务逻辑（已完成）
 * ⏳ MedicineService - 药品业务逻辑（待实现）
 * ⏳ StockInService - 入库业务逻辑（待实现）
 * ⏳ StockOutService - 出库业务逻辑（待实现）
 * ⏳ PrescriptionService - 处方业务逻辑（待实现）
 * ⏳ PrescriptionDetailService - 处方明细业务逻辑（待实现）
 * ⏳ AuditRecordService - 审核记录业务逻辑（待实现）
 *
 * ⏳ 五、控制器层（Controller）- 6个待实现
 * ⏳ AuthController - 用户认证API
 * ⏳ UserController - 用户管理API
 * ⏳ MedicineController - 药品管理API
 * ⏳ StockController - 库存管理API（入库/出库）
 * ⏳ PrescriptionController - 处方管理API
 * ⏳ AuditController - 审核记录管理API
 */
@Component
public class JwtUtil {

    @Autowired
    private JwtConfig jwtConfig;

    /**
     * 从token中提取用户名
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * 从token中提取过期时间
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * 从token中提取指定声明
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 获取签名密钥
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 提取所有声明
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 检查token是否过期
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * 生成token
     */
    public String generateToken(String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        return createToken(claims, username);
    }

    /**
     * 创建token
     */
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtConfig.getExpiration()))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 验证token
     */
    public Boolean validateToken(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }

    /**
     * 从token中提取角色
     */
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }
}
