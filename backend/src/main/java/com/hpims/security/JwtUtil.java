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
 * TODO: 李教博需要实现的Repository接口
 * ====================================
 *
 * 数据访问层接口 (需要创建):
 * TODO: UserRepository.java - 用户数据访问接口
 *   - extends JpaRepository<User, Long>
 *   - findByUsername(String username)
 *   - findByRole(String role)
 *   - existsByUsername(String username)
 *
 * TODO: MedicineRepository.java - 药品数据访问接口
 *   - extends JpaRepository<Medicine, Long>
 *   - findByNameContaining(String name)
 *   - findByCategory(String category)
 *   - findByExpiryDateBefore(LocalDateTime date)
 *
 * TODO: InventoryRepository.java - 库存数据访问接口
 *   - extends JpaRepository<Inventory, Long>
 *   - findByMedicineId(Long medicineId)
 *   - findByCurrentStockLessThan(Integer threshold)
 *
 * TODO: PrescriptionRepository.java - 处方数据访问接口
 *   - extends JpaRepository<Prescription, Long>
 *   - findByPatientId(String patientId)
 *   - findByDoctorId(Long doctorId)
 *   - findByStatus(String status)
 *   - findByCreateTimeBetween(LocalDateTime start, LocalDateTime end)
 *
 * TODO: AuditRepository.java - 审核记录数据访问接口
 *   - extends JpaRepository<AuditRecord, Long>
 *   - findByPrescriptionId(Long prescriptionId)
 *   - findByAuditorId(Long auditorId)
 *   - findByResult(String result)
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
