-- 数据库重置脚本 - 完全清空并重新初始化数据
-- 警告：此脚本会删除所有现有数据！

-- 删除外键约束
SET FOREIGN_KEY_CHECKS = 0;

-- 清空所有表
TRUNCATE TABLE audit_record;
TRUNCATE TABLE prescription_detail;
TRUNCATE TABLE prescription;
TRUNCATE TABLE stock_in;
TRUNCATE TABLE stock_out;
TRUNCATE TABLE medicine;
TRUNCATE TABLE sys_user;

-- 恢复外键约束
SET FOREIGN_KEY_CHECKS = 1;

-- 重新插入默认用户数据
INSERT INTO sys_user (username, password, role, real_name, department) VALUES
('admin', '123456', 'admin', '系统管理员', '信息部'),
('doctor', '123456', 'doctor', '李医生', '内科'),
('pharmacist', '123456', 'pharmacist', '王药师', '药房');

-- 重新插入测试药品数据
INSERT INTO medicine (name, generic_name, specification, manufacturer, price, stock_quantity, min_stock, category, approval_number) VALUES
('阿莫西林胶囊', '阿莫西林', '0.25g*24粒', '哈药集团', 15.50, 100, 20, '抗生素', 'H20051234'),
('布洛芬缓释胶囊', '布洛芬', '0.3g*20粒', '拜耳医药', 28.80, 50, 15, '解热镇痛', 'H20059876'),
('维生素C片', '维生素C', '100mg*100片', '施尔康', 12.90, 200, 30, '维生素', 'H20051111'),
('感冒灵颗粒', '感冒灵', '10g*12袋', '白云山制药', 8.50, 80, 25, '感冒药', 'H20052222'),
('葡萄糖注射液', '葡萄糖', '10% 500ml', '华润双鹤', 5.20, 150, 40, '注射剂', 'H20053333'),
('头孢拉定胶囊', '头孢拉定', '0.25g*12粒', '石药集团', 22.00, 75, 20, '抗生素', 'H20054444');

-- 重新插入测试处方数据
INSERT INTO prescription (prescription_number, patient_name, patient_age, patient_gender, doctor_name, department, create_date, status) VALUES
('RX20241209001', '张三', 35, '男', '李医生', '内科', '2024-12-09', '未发药'),
('RX20241209002', '李四', 28, '女', '王医生', '外科', '2024-12-09', '已发药'),
('RX20241209003', '王五', 42, '男', '赵医生', '儿科', '2024-12-09', '审核中');

-- 重新插入处方明细数据
INSERT INTO prescription_detail (prescription_id, medicine_id, quantity, dosage, frequency, days) VALUES
(1, 1, 2, '每次2粒', '每日3次', 7),
(1, 3, 1, '每次1片', '每日2次', 7),
(2, 2, 1, '每次1粒', '每日2次', 5),
(2, 4, 2, '每次1袋', '每日3次', 3),
(3, 5, 1, '每次1瓶', '每日1次', 3),
(3, 6, 1, '每次1粒', '每日3次', 7);

-- 重新插入入库记录
INSERT INTO stock_in (medicine_id, batch_number, quantity, supplier, in_date, operator) VALUES
(1, 'BATCH20241201', 100, '医药公司A', '2024-12-01', '张库管'),
(2, 'BATCH20241202', 50, '医药公司B', '2024-12-02', '李库管'),
(3, 'BATCH20241203', 200, '医药公司C', '2024-12-03', '王库管');

-- 重新插入出库记录
INSERT INTO stock_out (medicine_id, batch_number, quantity, out_date, operator, reason) VALUES
(1, 'BATCH20241201', 20, '2024-12-08', '药房管理员', '处方发药'),
(2, 'BATCH20241202', 10, '2024-12-08', '药房管理员', '处方发药');

-- 重新插入审核记录
INSERT INTO audit_record (prescription_id, audit_type, audit_result, audit_score, issues_found, suggestions, auditor) VALUES
(1, '自动审核', '通过', 95.5, '无明显问题', '建议按时服药', '系统审核'),
(2, '人工审核', '通过', 98.0, '剂量合理', '继续治疗', '王药师'),
(3, '自动审核', '待审核', NULL, '正在审核中', NULL, '系统审核');

-- 验证数据
SELECT '用户表' as table_name, COUNT(*) as count FROM sys_user
UNION ALL
SELECT '药品表', COUNT(*) FROM medicine
UNION ALL
SELECT '处方表', COUNT(*) FROM prescription
UNION ALL
SELECT '处方明细表', COUNT(*) FROM prescription_detail;
