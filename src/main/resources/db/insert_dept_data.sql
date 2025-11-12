-- 为t_mes_ct_dept表插入部门层级数据
SET NAMES utf8mb4;

-- 清空现有数据（如果需要）
-- TRUNCATE TABLE t_mes_ct_dept;

-- 2层部门：云核心网产品线（只有一个）
INSERT INTO t_mes_ct_dept (dept_id, dept_code, dept_name, dept_level, parent_dept_code, enabled, virtual, create_date, create_by) VALUES
('DEPT_2_001', 'DEPT_2_001', '云核心网产品线', 2, NULL, 1, 0, NOW(), 'system');

-- 3层部门：装备部、研发部、系统部、研究院（多个）
INSERT INTO t_mes_ct_dept (dept_id, dept_code, dept_name, dept_level, parent_dept_code, enabled, virtual, create_date, create_by) VALUES
('DEPT_3_001', 'DEPT_3_001', '装备部', 3, 'DEPT_2_001', 1, 0, NOW(), 'system'),
('DEPT_3_002', 'DEPT_3_002', '研发部', 3, 'DEPT_2_001', 1, 0, NOW(), 'system'),
('DEPT_3_003', 'DEPT_3_003', '系统部', 3, 'DEPT_2_001', 1, 0, NOW(), 'system'),
('DEPT_3_004', 'DEPT_3_004', '研究院', 3, 'DEPT_2_001', 1, 0, NOW(), 'system'),
('DEPT_3_005', 'DEPT_3_005', '测试部', 3, 'DEPT_2_001', 1, 0, NOW(), 'system');

-- 4层部门：工具部、开发组、测试组等
INSERT INTO t_mes_ct_dept (dept_id, dept_code, dept_name, dept_level, parent_dept_code, enabled, virtual, create_date, create_by) VALUES
-- 装备部下设
('DEPT_4_001', 'DEPT_4_001', '工具部', 4, 'DEPT_3_001', 1, 0, NOW(), 'system'),
('DEPT_4_002', 'DEPT_4_002', '设备部', 4, 'DEPT_3_001', 1, 0, NOW(), 'system'),
('DEPT_4_003', 'DEPT_4_003', '维护部', 4, 'DEPT_3_001', 1, 0, NOW(), 'system'),
-- 研发部下设
('DEPT_4_004', 'DEPT_4_004', '前端开发组', 4, 'DEPT_3_002', 1, 0, NOW(), 'system'),
('DEPT_4_005', 'DEPT_4_005', '后端开发组', 4, 'DEPT_3_002', 1, 0, NOW(), 'system'),
('DEPT_4_006', 'DEPT_4_006', 'AI算法组', 4, 'DEPT_3_002', 1, 0, NOW(), 'system'),
-- 系统部下设
('DEPT_4_007', 'DEPT_4_007', '系统架构组', 4, 'DEPT_3_003', 1, 0, NOW(), 'system'),
('DEPT_4_008', 'DEPT_4_008', '运维组', 4, 'DEPT_3_003', 1, 0, NOW(), 'system'),
('DEPT_4_009', 'DEPT_4_009', '安全组', 4, 'DEPT_3_003', 1, 0, NOW(), 'system'),
-- 研究院下设
('DEPT_4_010', 'DEPT_4_010', '算法研究组', 4, 'DEPT_3_004', 1, 0, NOW(), 'system'),
('DEPT_4_011', 'DEPT_4_011', '技术研究组', 4, 'DEPT_3_004', 1, 0, NOW(), 'system'),
('DEPT_4_012', 'DEPT_4_012', '创新研究组', 4, 'DEPT_3_004', 1, 0, NOW(), 'system'),
-- 测试部下设
('DEPT_4_013', 'DEPT_4_013', '功能测试组', 4, 'DEPT_3_005', 1, 0, NOW(), 'system'),
('DEPT_4_014', 'DEPT_4_014', '性能测试组', 4, 'DEPT_3_005', 1, 0, NOW(), 'system');

-- 5层部门
INSERT INTO t_mes_ct_dept (dept_id, dept_code, dept_name, dept_level, parent_dept_code, enabled, virtual, create_date, create_by) VALUES
-- 工具部下设
('DEPT_5_001', 'DEPT_5_001', '工具开发小组', 5, 'DEPT_4_001', 1, 0, NOW(), 'system'),
('DEPT_5_002', 'DEPT_5_002', '工具维护小组', 5, 'DEPT_4_001', 1, 0, NOW(), 'system'),
-- 前端开发组下设
('DEPT_5_003', 'DEPT_5_003', 'Vue开发小组', 5, 'DEPT_4_004', 1, 0, NOW(), 'system'),
('DEPT_5_004', 'DEPT_5_004', 'React开发小组', 5, 'DEPT_4_004', 1, 0, NOW(), 'system'),
-- 后端开发组下设
('DEPT_5_005', 'DEPT_5_005', 'Java开发小组', 5, 'DEPT_4_005', 1, 0, NOW(), 'system'),
('DEPT_5_006', 'DEPT_5_006', 'Python开发小组', 5, 'DEPT_4_005', 1, 0, NOW(), 'system'),
-- AI算法组下设
('DEPT_5_007', 'DEPT_5_007', '机器学习小组', 5, 'DEPT_4_006', 1, 0, NOW(), 'system'),
('DEPT_5_008', 'DEPT_5_008', '深度学习小组', 5, 'DEPT_4_006', 1, 0, NOW(), 'system'),
-- 系统架构组下设
('DEPT_5_009', 'DEPT_5_009', '架构设计小组', 5, 'DEPT_4_007', 1, 0, NOW(), 'system'),
('DEPT_5_010', 'DEPT_5_010', '技术评审小组', 5, 'DEPT_4_007', 1, 0, NOW(), 'system'),
-- 算法研究组下设
('DEPT_5_011', 'DEPT_5_011', '算法优化小组', 5, 'DEPT_4_010', 1, 0, NOW(), 'system'),
('DEPT_5_012', 'DEPT_5_012', '算法验证小组', 5, 'DEPT_4_010', 1, 0, NOW(), 'system');

-- 6层部门
INSERT INTO t_mes_ct_dept (dept_id, dept_code, dept_name, dept_level, parent_dept_code, enabled, virtual, create_date, create_by) VALUES
-- 工具开发小组下设
('DEPT_6_001', 'DEPT_6_001', '开发团队A', 6, 'DEPT_5_001', 1, 0, NOW(), 'system'),
('DEPT_6_002', 'DEPT_6_002', '开发团队B', 6, 'DEPT_5_001', 1, 0, NOW(), 'system'),
-- Java开发小组下设
('DEPT_6_003', 'DEPT_6_003', 'Spring团队', 6, 'DEPT_5_005', 1, 0, NOW(), 'system'),
('DEPT_6_004', 'DEPT_6_004', 'MyBatis团队', 6, 'DEPT_5_005', 1, 0, NOW(), 'system'),
-- 机器学习小组下设
('DEPT_6_005', 'DEPT_6_005', '模型训练团队', 6, 'DEPT_5_007', 1, 0, NOW(), 'system'),
('DEPT_6_006', 'DEPT_6_006', '模型评估团队', 6, 'DEPT_5_007', 1, 0, NOW(), 'system'),
-- 架构设计小组下设
('DEPT_6_007', 'DEPT_6_007', '架构设计团队', 6, 'DEPT_5_009', 1, 0, NOW(), 'system'),
('DEPT_6_008', 'DEPT_6_008', '架构优化团队', 6, 'DEPT_5_009', 1, 0, NOW(), 'system');

-- 7层部门
INSERT INTO t_mes_ct_dept (dept_id, dept_code, dept_name, dept_level, parent_dept_code, enabled, virtual, create_date, create_by) VALUES
-- 开发团队A下设
('DEPT_7_001', 'DEPT_7_001', '项目组1', 7, 'DEPT_6_001', 1, 0, NOW(), 'system'),
('DEPT_7_002', 'DEPT_7_002', '项目组2', 7, 'DEPT_6_001', 1, 0, NOW(), 'system'),
-- Spring团队下设
('DEPT_7_003', 'DEPT_7_003', 'Spring Boot项目组', 7, 'DEPT_6_003', 1, 0, NOW(), 'system'),
('DEPT_7_004', 'DEPT_7_004', 'Spring Cloud项目组', 7, 'DEPT_6_003', 1, 0, NOW(), 'system'),
-- 模型训练团队下设
('DEPT_7_005', 'DEPT_7_005', 'NLP项目组', 7, 'DEPT_6_005', 1, 0, NOW(), 'system'),
('DEPT_7_006', 'DEPT_7_006', 'CV项目组', 7, 'DEPT_6_005', 1, 0, NOW(), 'system'),
-- 架构设计团队下设
('DEPT_7_007', 'DEPT_7_007', '微服务架构组', 7, 'DEPT_6_007', 1, 0, NOW(), 'system'),
('DEPT_7_008', 'DEPT_7_008', '分布式架构组', 7, 'DEPT_6_007', 1, 0, NOW(), 'system');

