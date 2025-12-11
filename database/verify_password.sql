-- 验证密码哈希脚本
-- 用于检查数据库中的密码哈希是否完整和正确

USE hpims;

-- 检查admin用户的密码哈希
SELECT 
    username,
    role,
    LENGTH(password) as password_length,
    password as full_password_hash,
    CASE 
        WHEN password LIKE '$2a$10$%' AND LENGTH(password) = 60 THEN '格式正确（60字符）'
        WHEN password LIKE '$2a$10$%' AND LENGTH(password) < 60 THEN '格式错误（长度不足）'
        WHEN password LIKE '$2a$10$%' AND LENGTH(password) > 60 THEN '格式错误（长度过长）'
        WHEN password LIKE '$2y$10$%' AND LENGTH(password) = 60 THEN '格式正确（60字符）'
        ELSE '格式错误（不是BCrypt）'
    END as format_check,
    LEFT(password, 7) as prefix,
    RIGHT(password, 10) as suffix
FROM users 
WHERE username = 'admin';

-- 检查所有用户的密码哈希
SELECT 
    username,
    role,
    LENGTH(password) as password_length,
    CASE 
        WHEN password LIKE '$2a$10$%' AND LENGTH(password) = 60 THEN '✓ 正确'
        WHEN password LIKE '$2y$10$%' AND LENGTH(password) = 60 THEN '✓ 正确'
        ELSE '✗ 错误'
    END as format_check
FROM users;

