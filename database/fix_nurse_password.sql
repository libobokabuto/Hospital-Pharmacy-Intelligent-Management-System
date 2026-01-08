-- ============================================================
-- 修复护士用户密码哈希
-- ============================================================
-- 问题：nurse用户的密码哈希验证失败
-- 解决方案：更新为正确的BCrypt哈希
-- ============================================================

-- 步骤1：检查当前nurse用户的密码哈希
SELECT 
    id,
    username,
    role,
    real_name,
    LENGTH(password) as password_length,
    LEFT(password, 10) as password_prefix,
    password as password_hash
FROM users 
WHERE username = 'nurse';

-- 步骤2：删除可能存在的多余空格或字符
-- 先备份（可选）
-- CREATE TABLE users_backup AS SELECT * FROM users WHERE username = 'nurse';

-- 步骤3：更新nurse用户密码为正确的BCrypt哈希
-- 注意：BCrypt每次生成的哈希都不同，但都可以验证同一个密码"123456"
-- 如果这个哈希仍然不工作，请使用方法2生成新哈希

-- 方法1：使用与admin/doctor/pharmacist相同的哈希（如果它们能正常工作）
UPDATE users 
SET password = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'
WHERE username = 'nurse';

-- 方法2：如果方法1不行，请访问后端API生成新哈希：
-- GET http://localhost:8080/api/auth/generate-hash?password=123456
-- 然后使用返回的哈希值更新下面的SQL语句并执行

-- UPDATE users 
-- SET password = '新生成的哈希值'
-- WHERE username = 'nurse';

-- 步骤4：验证更新结果
SELECT 
    id,
    username,
    role,
    real_name,
    LENGTH(password) as password_length,
    CASE 
        WHEN password LIKE '$2a$10$%' THEN 'BCrypt格式正确'
        ELSE '格式可能有问题'
    END as password_format_check
FROM users 
WHERE username = 'nurse';

-- 步骤5：检查所有用户的密码哈希格式
SELECT 
    username,
    role,
    LENGTH(password) as password_length,
    LEFT(password, 7) as password_prefix,
    CASE 
        WHEN password LIKE '$2a$10$%' AND LENGTH(password) = 60 THEN '格式正确'
        ELSE '格式可能有问题'
    END as format_status
FROM users
ORDER BY username;







