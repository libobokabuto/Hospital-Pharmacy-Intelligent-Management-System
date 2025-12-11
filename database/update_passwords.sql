-- 密码更新脚本
-- 将数据库中的明文密码更新为BCrypt加密后的密码
-- 注意：此脚本使用BCrypt加密的"123456"密码
-- BCrypt哈希值每次生成都不同，但都可以用来验证同一个明文密码

-- 更新admin用户密码 (BCrypt加密的"123456")
UPDATE users 
SET password = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy' 
WHERE username = 'admin';

-- 更新doctor用户密码 (BCrypt加密的"123456")
UPDATE users 
SET password = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy' 
WHERE username = 'doctor';

-- 更新pharmacist用户密码 (BCrypt加密的"123456")
UPDATE users 
SET password = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy' 
WHERE username = 'pharmacist';

-- 验证更新结果
SELECT username, role, 
       CASE 
         WHEN password LIKE '$2a$%' THEN '已加密'
         ELSE '未加密'
       END as password_status
FROM users;

