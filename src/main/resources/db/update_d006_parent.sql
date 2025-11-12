-- 更新D006（云核心网产品线）的父部门为D001（研发一部）
SET NAMES utf8mb4;

-- 更新D006的父部门编码
UPDATE department_info_hrms
SET parent_dept_code = 'D001'
WHERE dept_code = 'D006';

-- 验证更新结果
SELECT 
    dept_code,
    dept_name,
    dept_level,
    parent_dept_code
FROM department_info_hrms
WHERE dept_code = 'D006';

