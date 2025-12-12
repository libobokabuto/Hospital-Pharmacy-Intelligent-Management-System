-- 数据库结构更新脚本
-- 根据Java实体类的最新定义更新数据库结构
-- 创建时间: 2024-12-10

-- ============================================
-- 1. 更新 prescription 表
-- ============================================

-- 扩展 patient_gender 字段长度（从 VARCHAR(10) 改为 VARCHAR(20)）
ALTER TABLE prescription 
MODIFY COLUMN patient_gender VARCHAR(20);

-- ============================================
-- 2. 更新 audit_record 表
-- ============================================

-- 将 audit_type 从 VARCHAR(20) 改为 ENUM
-- 注意：需要先清空或转换现有数据
-- 步骤1：备份现有数据到临时列（如果需要保留数据）
ALTER TABLE audit_record 
ADD COLUMN audit_type_backup VARCHAR(20);

UPDATE audit_record 
SET audit_type_backup = audit_type;

-- 步骤2：删除旧列
ALTER TABLE audit_record 
DROP COLUMN audit_type;

-- 步骤3：创建新的ENUM列
ALTER TABLE audit_record 
ADD COLUMN audit_type ENUM('auto','manual') DEFAULT 'auto' 
COMMENT 'auto=自动审核, manual=人工审核';

-- 步骤4：转换备份数据（如果需要）
-- 将中文值转换为英文ENUM值
UPDATE audit_record 
SET audit_type = CASE 
    WHEN audit_type_backup = '自动审核' OR audit_type_backup = 'auto' THEN 'auto'
    WHEN audit_type_backup = '人工审核' OR audit_type_backup = 'manual' THEN 'manual'
    ELSE 'auto'
END;

-- 步骤5：删除备份列
ALTER TABLE audit_record 
DROP COLUMN audit_type_backup;

-- 将 audit_result 从 VARCHAR(20) 改为 ENUM
-- 步骤1：备份现有数据
ALTER TABLE audit_record 
ADD COLUMN audit_result_backup VARCHAR(20);

UPDATE audit_record 
SET audit_result_backup = audit_result;

-- 步骤2：删除旧列
ALTER TABLE audit_record 
DROP COLUMN audit_result;

-- 步骤3：创建新的ENUM列
ALTER TABLE audit_record 
ADD COLUMN audit_result ENUM('pass','warning','reject') NOT NULL DEFAULT 'pass' 
COMMENT 'pass=通过, warning=警告, reject=拒绝';

-- 步骤4：转换备份数据
UPDATE audit_record 
SET audit_result = CASE 
    WHEN audit_result_backup = '通过' OR audit_result_backup = 'pass' THEN 'pass'
    WHEN audit_result_backup = '警告' OR audit_result_backup = 'warning' THEN 'warning'
    WHEN audit_result_backup = '拒绝' OR audit_result_backup = 'reject' OR audit_result_backup = '已拒绝' THEN 'reject'
    WHEN audit_result_backup = '待审核' THEN 'pass'  -- 默认转换为通过
    ELSE 'pass'
END;

-- 步骤5：删除备份列
ALTER TABLE audit_record 
DROP COLUMN audit_result_backup;

-- ============================================
-- 3. 更新 stock_out 表
-- ============================================

-- 将 reason 从 VARCHAR(100) 改为 ENUM
-- 步骤1：备份现有数据
ALTER TABLE stock_out 
ADD COLUMN reason_backup VARCHAR(100);

UPDATE stock_out 
SET reason_backup = reason;

-- 步骤2：删除旧列
ALTER TABLE stock_out 
DROP COLUMN reason;

-- 步骤3：创建新的ENUM列
ALTER TABLE stock_out 
ADD COLUMN reason ENUM('prescription', 'loss', 'expired', 'other') 
COMMENT 'prescription=处方发药, loss=盘亏, expired=过期, other=其他';

-- 步骤4：转换备份数据
UPDATE stock_out 
SET reason = CASE 
    WHEN reason_backup = '处方发药' OR reason_backup = 'prescription' THEN 'prescription'
    WHEN reason_backup = '盘亏' OR reason_backup = 'loss' THEN 'loss'
    WHEN reason_backup = '过期' OR reason_backup = 'expired' THEN 'expired'
    WHEN reason_backup = '其他' OR reason_backup = 'other' THEN 'other'
    ELSE 'other'  -- 默认值
END;

-- 步骤5：删除备份列
ALTER TABLE stock_out 
DROP COLUMN reason_backup;

-- ============================================
-- 4. 验证更新结果
-- ============================================

-- 查看 prescription 表结构
DESCRIBE prescription;

-- 查看 audit_record 表结构
DESCRIBE audit_record;

-- 查看 stock_out 表结构
DESCRIBE stock_out;

-- 检查数据转换是否成功
SELECT id, audit_type, audit_result FROM audit_record LIMIT 5;
SELECT id, reason FROM stock_out LIMIT 5;

