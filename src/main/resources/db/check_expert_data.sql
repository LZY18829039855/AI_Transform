-- 检查expert_info表中是否有匹配"研发管理部"的数据
SET NAMES utf8mb4;

-- 1. 检查expert_info表中org_level字段的值
SELECT DISTINCT 
    org_level,
    COUNT(*) as count
FROM expert_info
WHERE org_level IS NOT NULL
GROUP BY org_level
ORDER BY org_level;

-- 2. 检查是否有org_level='研发管理部'的数据
SELECT 
    ai_maturity,
    job_category,
    employee_number,
    org_level,
    l4_org_code,
    l4_org_name,
    on_duty_status
FROM expert_info
WHERE org_level = '研发管理部'
  AND on_duty_status = 1
  AND employee_number IS NOT NULL
  AND employee_number != ''
LIMIT 10;

-- 3. 检查L2成熟度的专家数据
SELECT 
    ai_maturity,
    job_category,
    employee_number,
    org_level
FROM expert_info
WHERE org_level = '研发管理部'
  AND ai_maturity = 'L2'
  AND on_duty_status = 1
  AND employee_number IS NOT NULL
  AND employee_number != ''
LIMIT 10;





