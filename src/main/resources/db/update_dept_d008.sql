-- 修正D008部门名称：从"采购管理部"改为"研发管理部"
SET NAMES utf8mb4;

-- 更新D008的部门名称
UPDATE department_info_hrms
SET dept_name = '研发管理部'
WHERE dept_code = 'D008';

-- 验证更新结果
SELECT 
    dept_code,
    dept_name,
    dept_level
FROM department_info_hrms
WHERE dept_code = 'D008';




