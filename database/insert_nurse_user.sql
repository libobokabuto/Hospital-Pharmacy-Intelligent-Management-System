-- 插入护士用户
-- 密码已使用BCrypt加密，明文为"123456"，与现有用户密码相同
-- 执行此SQL前，请确保数据库已创建users表

-- 方法1：使用INSERT IGNORE（如果用户已存在则忽略，不会报错）
INSERT IGNORE INTO users (username, password, role, real_name, department) VALUES
('nurse', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'nurse', '张护士', '护理部');

-- 方法2：如果方法1执行失败，可以尝试使用ON DUPLICATE KEY UPDATE（如果username有唯一约束）
-- INSERT INTO users (username, password, role, real_name, department) VALUES
-- ('nurse', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'nurse', '张护士', '护理部')
-- ON DUPLICATE KEY UPDATE 
--     password = VALUES(password),
--     role = VALUES(role),
--     real_name = VALUES(real_name),
--     department = VALUES(department);

-- 验证插入结果
SELECT id, username, role, real_name, department FROM users WHERE username = 'nurse';







