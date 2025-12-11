-- 医院药房智能管理系统数据库初始化脚本
-- 创建时间: 2024-12-10

-- 创建用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'administrator',
    real_name VARCHAR(50),
    department VARCHAR(50),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 创建药品表
CREATE TABLE IF NOT EXISTS medicine (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    generic_name VARCHAR(100),
    specification VARCHAR(50),
    manufacturer VARCHAR(100),
    price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    stock_quantity INT NOT NULL DEFAULT 0,
    min_stock INT DEFAULT 10,
    category VARCHAR(50),
    approval_number VARCHAR(50),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 创建入库记录表
CREATE TABLE IF NOT EXISTS stock_in (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    medicine_id BIGINT NOT NULL,
    batch_number VARCHAR(50),
    quantity INT NOT NULL,
    supplier VARCHAR(100),
    in_date DATE NOT NULL,
    operator VARCHAR(50),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (medicine_id) REFERENCES medicine(id)
);

-- 创建出库记录表
CREATE TABLE IF NOT EXISTS stock_out (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    medicine_id BIGINT NOT NULL,
    batch_number VARCHAR(50),
    quantity INT NOT NULL,
    out_date DATE NOT NULL,
    operator VARCHAR(50),
    reason VARCHAR(100),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (medicine_id) REFERENCES medicine(id)
);

-- 创建处方表
CREATE TABLE IF NOT EXISTS prescription (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    prescription_number VARCHAR(50) NOT NULL UNIQUE,
    patient_name VARCHAR(50) NOT NULL,
    patient_age INT,
    patient_gender VARCHAR(10),
    doctor_name VARCHAR(50),
    department VARCHAR(50),
    create_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT '未审核',
    audit_result TEXT,
    audit_time TIMESTAMP NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 创建处方明细表
CREATE TABLE IF NOT EXISTS prescription_detail (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    prescription_id BIGINT NOT NULL,
    medicine_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    dosage VARCHAR(100),
    frequency VARCHAR(50),
    days INT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (prescription_id) REFERENCES prescription(id),
    FOREIGN KEY (medicine_id) REFERENCES medicine(id)
);

-- 创建审核记录表
CREATE TABLE IF NOT EXISTS audit_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    prescription_id BIGINT NOT NULL,
    audit_type VARCHAR(20) DEFAULT '自动审核',
    audit_result VARCHAR(20) NOT NULL,
    audit_score DECIMAL(5,2),
    issues_found TEXT,
    suggestions TEXT,
    auditor VARCHAR(50),
    audit_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (prescription_id) REFERENCES prescription(id)
);

-- 插入默认用户数据（避免重复插入，密码已使用BCrypt加密，明文为"123456"）
INSERT IGNORE INTO users (username, password, role, real_name, department) VALUES
('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'admin', '系统管理员', '信息部'),
('doctor', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'doctor', '李医生', '内科'),
('pharmacist', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'pharmacist', '王药师', '药房');

-- 插入测试药品数据（避免重复插入）
INSERT IGNORE INTO medicine (name, generic_name, specification, manufacturer, price, stock_quantity, min_stock, category, approval_number) VALUES
('阿莫西林胶囊', '阿莫西林', '0.25g*24粒', '哈药集团', 15.50, 100, 20, '抗生素', 'H20051234'),
('布洛芬缓释胶囊', '布洛芬', '0.3g*20粒', '拜耳医药', 28.80, 50, 15, '解热镇痛', 'H20059876'),
('维生素C片', '维生素C', '100mg*100片', '施尔康', 12.90, 200, 30, '维生素', 'H20051111'),
('感冒灵颗粒', '感冒灵', '10g*12袋', '白云山制药', 8.50, 80, 25, '感冒药', 'H20052222'),
('葡萄糖注射液', '葡萄糖', '10% 500ml', '华润双鹤', 5.20, 150, 40, '注射剂', 'H20053333'),
('头孢拉定胶囊', '头孢拉定', '0.25g*12粒', '石药集团', 22.00, 75, 20, '抗生素', 'H20054444');

-- 插入测试处方数据（避免重复插入）
INSERT IGNORE INTO prescription (prescription_number, patient_name, patient_age, patient_gender, doctor_name, department, create_date, status) VALUES
('RX20241209001', '张三', 35, '男', '李医生', '内科', '2024-12-09', '未发药'),
('RX20241209002', '李四', 28, '女', '王医生', '外科', '2024-12-09', '已发药'),
('RX20241209003', '王五', 42, '男', '赵医生', '儿科', '2024-12-09', '审核中');

-- 插入处方明细数据（避免重复插入）
INSERT IGNORE INTO prescription_detail (prescription_id, medicine_id, quantity, dosage, frequency, days) VALUES
(1, 1, 2, '每次2粒', '每日3次', 7),
(1, 3, 1, '每次1片', '每日2次', 7),
(2, 2, 1, '每次1粒', '每日2次', 5),
(2, 4, 2, '每次1袋', '每日3次', 3),
(3, 5, 1, '每次1瓶', '每日1次', 3),
(3, 6, 1, '每次1粒', '每日3次', 7);

-- 插入入库记录（避免重复插入）
INSERT IGNORE INTO stock_in (medicine_id, batch_number, quantity, supplier, in_date, operator) VALUES
(1, 'BATCH20241201', 100, '医药公司A', '2024-12-01', '张库管'),
(2, 'BATCH20241202', 50, '医药公司B', '2024-12-02', '李库管'),
(3, 'BATCH20241203', 200, '医药公司C', '2024-12-03', '王库管');

-- 插入出库记录（避免重复插入）
INSERT IGNORE INTO stock_out (medicine_id, batch_number, quantity, out_date, operator, reason) VALUES
(1, 'BATCH20241201', 20, '2024-12-08', '药房管理员', '处方发药'),
(2, 'BATCH20241202', 10, '2024-12-08', '药房管理员', '处方发药');

-- 插入审核记录（避免重复插入）
INSERT IGNORE INTO audit_record (prescription_id, audit_type, audit_result, audit_score, issues_found, suggestions, auditor) VALUES
(1, '自动审核', '通过', 95.5, '无明显问题', '建议按时服药', '系统审核'),
(2, '人工审核', '通过', 98.0, '剂量合理', '继续治疗', '王药师'),
(3, '自动审核', '待审核', NULL, '正在审核中', NULL, '系统审核');

-- 检查用户表
SELECT id, username, role, real_name FROM users;

-- 检查药品表
SELECT id, name, stock_quantity, price FROM medicine;

-- 检查处方表
SELECT id, prescription_number, patient_name, status FROM prescription;

