-- 数据库结构更新脚本 - 添加新字段
-- 根据需求分析报告添加：药品适应症、患者病症、员工职工号等字段
-- 创建时间: 2024-12-10

-- ============================================
-- 1. 更新 medicine 表 - 添加适应症、禁忌症、相互作用字段
-- ============================================
-- 注意：如果字段已存在，需要先手动检查或删除
ALTER TABLE medicine 
ADD COLUMN indication TEXT COMMENT '适应症';
ALTER TABLE medicine 
ADD COLUMN contraindication TEXT COMMENT '禁忌症';
ALTER TABLE medicine 
ADD COLUMN interactions TEXT COMMENT '药物相互作用';

-- ============================================
-- 2. 更新 prescription 表 - 添加患者病症、诊断等字段
-- ============================================
-- 注意：如果字段已存在，需要先手动检查或删除
ALTER TABLE prescription 
ADD COLUMN patient_symptoms TEXT COMMENT '患者症状/病症';
ALTER TABLE prescription 
ADD COLUMN diagnosis VARCHAR(200) COMMENT '诊断';
ALTER TABLE prescription 
ADD COLUMN patient_conditions TEXT COMMENT '患者疾病状况';
ALTER TABLE prescription 
ADD COLUMN allergies TEXT COMMENT '过敏史';

-- ============================================
-- 3. 更新 users 表 - 添加职工号、职称字段
-- ============================================
-- 注意：如果字段已存在，需要先手动检查或删除
ALTER TABLE users 
ADD COLUMN employee_number VARCHAR(50) COMMENT '职工号';
ALTER TABLE users 
ADD COLUMN title VARCHAR(50) COMMENT '职称';

-- 添加唯一约束（如果表已存在且有数据，需要先检查重复值）
-- 执行前请先检查：SELECT employee_number, COUNT(*) FROM users GROUP BY employee_number HAVING COUNT(*) > 1;
-- ALTER TABLE users ADD UNIQUE INDEX uk_employee_number (employee_number);

-- ============================================
-- 4. 验证更新结果
-- ============================================
-- 查看 medicine 表结构
DESCRIBE medicine;

-- 查看 prescription 表结构
DESCRIBE prescription;

-- 查看 users 表结构
DESCRIBE users;

