-- ============================================================
-- 查看用户密码哈希信息
-- ============================================================

-- 查看所有用户的密码哈希信息
SELECT 
    id,
    username,
    role,
    real_name,
    department,
    LENGTH(password) as password_length,
    LEFT(password, 10) as password_prefix,
    password as password_hash,
    CASE 
        WHEN password LIKE '$2a$10$%' AND LENGTH(password) = 60 THEN '✓ BCrypt格式正确'
        WHEN password LIKE '$2a$%' THEN '⚠ BCrypt格式（但strength可能不同）'
        WHEN LENGTH(password) < 50 THEN '✗ 可能不是BCrypt哈希'
        ELSE '? 格式未知'
    END as hash_format_status
FROM users
WHERE username IN ('admin', 'doctor', 'pharmacist', 'nurse')
ORDER BY username;

-- ============================================================
-- 详细查看每个用户的密码哈希（十六进制格式，用于检查隐藏字符）
-- ============================================================
SELECT 
    username,
    role,
    password as password_hash,
    LENGTH(password) as hash_length,
    HEX(password) as password_hex,  -- 十六进制格式，可以看到是否有隐藏字符
    CHAR_LENGTH(password) as char_length  -- 字符长度
FROM users
WHERE username IN ('admin', 'doctor', 'pharmacist', 'nurse')
ORDER BY username;

-- ============================================================
-- 比较所有用户的密码哈希是否相同
-- ============================================================
SELECT 
    password as password_hash,
    COUNT(*) as user_count,
    GROUP_CONCAT(username ORDER BY username) as users_with_same_hash
FROM users
WHERE username IN ('admin', 'doctor', 'pharmacist', 'nurse')
GROUP BY password
ORDER BY user_count DESC;







