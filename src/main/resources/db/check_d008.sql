-- 检查D008在数据库中的实际数据
SET NAMES utf8mb4;

-- 1. 检查D008的部门信息
SELECT 
    dept_code,
    dept_name,
    dept_level,
    parent_dept_code
FROM department_info_hrms
WHERE dept_code = 'D008';

-- 2. 检查是否有多个D008的记录
SELECT 
    dept_code,
    dept_name,
    dept_level,
    COUNT(*) as count
FROM department_info_hrms
WHERE dept_code = 'D008'
GROUP BY dept_code, dept_name, dept_level;

-- 3. 检查所有包含"研发"或"管理"的部门
SELECT 
    dept_code,
    dept_name,
    dept_level
FROM department_info_hrms
WHERE dept_name LIKE '%研发%' 
   OR dept_name LIKE '%管理%'
ORDER BY dept_code;

-- 4. 检查expert_info表中org_level字段的值（用于匹配部门名称）
SELECT DISTINCT 
    org_level,
    COUNT(*) as count
FROM expert_info
WHERE org_level IS NOT NULL
GROUP BY org_level
ORDER BY org_level;








