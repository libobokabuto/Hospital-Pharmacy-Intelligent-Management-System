-- ============================================================
-- 更新护士用户密码哈希（推荐方法）
-- ============================================================
-- 此SQL会清理密码字段并更新为正确的BCrypt哈希
-- ============================================================

-- 方法1：清理并更新密码哈希（推荐）
-- 先清理可能的空格和特殊字符，然后更新为正确的哈希
UPDATE users 
SET password = TRIM('$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy')
WHERE username = 'nurse';

-- 验证更新结果
SELECT 
    username,
    role,
    LENGTH(password) as password_length,
    password as password_hash,
    CASE 
        WHEN password LIKE '$2a$10$%' AND LENGTH(password) = 60 THEN '✓ 格式正确'
        ELSE '✗ 格式有问题'
    END as status
FROM users 
WHERE username = 'nurse';

-- ============================================================
-- 如果方法1仍然不行，使用方法2：通过API生成新哈希
-- ============================================================
-- 1. 确保后端服务正在运行
-- 2. 在浏览器中访问：
--    http://localhost:8080/api/auth/generate-hash?password=123456
-- 3. 复制返回的BCrypt哈希值
-- 4. 执行以下SQL（替换YOUR_NEW_HASH为复制的哈希值）：
--
-- UPDATE users 
-- SET password = 'YOUR_NEW_HASH'
-- WHERE username = 'nurse';
--
-- ============================================================
-- 方法3：如果以上都不行，删除并重新插入用户
-- ============================================================
-- DELETE FROM users WHERE username = 'nurse';
-- 
-- INSERT INTO users (username, password, role, real_name, department) 
-- VALUES ('nurse', 'YOUR_NEW_HASH', 'nurse', '张护士', '护理部');
-- 
-- 注意：YOUR_NEW_HASH需要通过方法2获取
-- ============================================================







