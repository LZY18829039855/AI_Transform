-- 验证D006的父部门是否已更新
SET NAMES utf8mb4;

-- 查询D006的当前信息
SELECT 
    dept_code,
    dept_name,
    dept_level,
    parent_dept_code
FROM department_info_hrms
WHERE dept_code = 'D006';

-- 如果 parent_dept_code 不是 'D001'，请执行更新：
-- UPDATE department_info_hrms SET parent_dept_code = 'D001' WHERE dept_code = 'D006';

