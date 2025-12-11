-- 检查用户数据脚本
-- 用于验证数据库中的用户是否正确设置

-- 查看所有用户
SELECT 
    id,
    username,
    role,
    real_name,
    department,
    CASE 
        WHEN password LIKE '$2a$%' THEN '已加密 (BCrypt)'
        WHEN password LIKE '$2y$%' THEN '已加密 (BCrypt)'
        ELSE '未加密或格式错误'
    END as password_status,
    LENGTH(password) as password_length,
    LEFT(password, 30) as password_preview
FROM users;

-- 检查特定用户（替换 'admin' 为你要检查的用户名）
SELECT 
    username,
    role,
    real_name,
    CASE 
        WHEN password LIKE '$2a$%' THEN 'BCrypt加密'
        ELSE '密码格式错误'
    END as password_format
FROM users 
WHERE username = 'admin';

-- 验证密码哈希格式
-- BCrypt 哈希应该以 $2a$ 或 $2y$ 开头，长度为 60 字符
SELECT 
    username,
    CASE 
        WHEN password LIKE '$2a$10$%' AND LENGTH(password) = 60 THEN '格式正确'
        WHEN password LIKE '$2y$10$%' AND LENGTH(password) = 60 THEN '格式正确'
        ELSE '格式错误'
    END as format_check
FROM users;

