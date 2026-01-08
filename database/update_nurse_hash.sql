-- ============================================================
-- 更新nurse用户的密码哈希
-- ============================================================
-- 新哈希值: $2b$10$R2AnR4nK4sDyGOLAelZpneEtcQcFdcbk.2/ANKMuNMh7q1swOmhYO
-- ============================================================

-- 更新nurse用户的密码哈希
UPDATE users 
SET password = '$2b$10$R2AnR4nK4sDyGOLAelZpneEtcQcFdcbk.2/ANKMuNMh7q1swOmhYO'
WHERE username = 'nurse';

-- 验证更新结果
SELECT 
    id,
    username,
    role,
    real_name,
    LENGTH(password) as password_length,
    LEFT(password, 7) as password_prefix,
    password as password_hash,
    CASE 
        WHEN password LIKE '$2b$10$%' AND LENGTH(password) = 60 THEN '✓ BCrypt格式正确 ($2b$)'
        WHEN password LIKE '$2a$10$%' AND LENGTH(password) = 60 THEN '✓ BCrypt格式正确 ($2a$)'
        ELSE '✗ 格式可能有问题'
    END as hash_format_status
FROM users 
WHERE username = 'nurse';

-- 查看所有用户的密码哈希前缀（用于对比）
SELECT 
    username,
    role,
    LEFT(password, 7) as hash_prefix,
    LENGTH(password) as hash_length
FROM users
WHERE username IN ('admin', 'doctor', 'pharmacist', 'nurse')
ORDER BY username;







