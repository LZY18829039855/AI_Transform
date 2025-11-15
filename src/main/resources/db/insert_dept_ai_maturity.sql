-- 插入部门AI成熟度数据
-- 表名：department_ai_maturity
-- 字段说明：
--   dept_code: 部门编码（主键，varchar(10)）
--   ai_maturity: AI成熟度（L1/L2/L3，varchar(10)）
--   ai_maturity_target: 目标AI成熟度（L1/L2/L3，varchar(10)）

INSERT INTO `department_ai_maturity` (`dept_code`, `ai_maturity`, `ai_maturity_target`) VALUES
('DEPT_3_001', 'L1', 'L2'),
('DEPT_3_002', 'L2', 'L3'),
('DEPT_3_003', 'L1', 'L3'),
('DEPT_3_004', 'L2', 'L2'),
('DEPT_3_005', 'L3', 'L3'),
('DEPT_4_001', 'L1', 'L2'),
('DEPT_4_002', 'L2', 'L3'),
('DEPT_4_003', 'L1', 'L3'),
('DEPT_4_004', 'L2', 'L2'),
('DEPT_4_005', 'L3', 'L3');

