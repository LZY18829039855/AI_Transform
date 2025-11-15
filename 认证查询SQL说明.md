# 查询认证通过的SQL说明

## 查询认证通过的SQL逻辑

### 1. 三层部门查询（getExpertStatisticsByLevel3）

```sql
SELECT 
    e.ai_maturity AS aiMaturity,
    e.job_category AS jobCategory,
    e.employee_number AS employeeNumber,
    CASE 
        WHEN cert.employee_number IS NOT NULL OR cert.w3_account IS NOT NULL THEN 1 
        ELSE 0 
    END AS hasCert
FROM expert_info e
LEFT JOIN (
    SELECT DISTINCT 
        COALESCE(employee_number, w3_account) AS employee_number,
        w3_account
    FROM dwr_t_cert_record_t
    WHERE (cer_title LIKE '华为研究类能力认证（专业级，%'
        OR cer_title LIKE '%华为研究类能力认证（专业级，%')
    AND (status = 1 OR approved_status = 1)
) cert ON (e.employee_number = cert.employee_number OR e.employee_number = cert.w3_account)
WHERE e.org_level = #{deptName}
AND e.on_duty_status = 1
AND e.employee_number IS NOT NULL
AND e.employee_number != ''
ORDER BY e.ai_maturity, e.job_category
```

### 2. 四层部门查询（getExpertStatisticsByLevel4）

```sql
SELECT 
    e.ai_maturity AS aiMaturity,
    e.job_category AS jobCategory,
    e.employee_number AS employeeNumber,
    CASE 
        WHEN cert.employee_number IS NOT NULL OR cert.w3_account IS NOT NULL THEN 1 
        ELSE 0 
    END AS hasCert
FROM expert_info e
LEFT JOIN (
    SELECT DISTINCT 
        COALESCE(employee_number, w3_account) AS employee_number,
        w3_account
    FROM dwr_t_cert_record_t
    WHERE (cer_title LIKE '华为研究类能力认证（专业级，%'
        OR cer_title LIKE '%华为研究类能力认证（专业级，%')
    AND (status = 1 OR approved_status = 1)
) cert ON (e.employee_number = cert.employee_number OR e.employee_number = cert.w3_account)
WHERE e.l4_org_code = #{deptCode}
AND e.on_duty_status = 1
AND e.employee_number IS NOT NULL
AND e.employee_number != ''
ORDER BY e.ai_maturity, e.job_category
```

## 认证通过的条件

### 证书标题条件
- `cer_title LIKE '华为研究类能力认证（专业级，%'` 
- 或 `cer_title LIKE '%华为研究类能力认证（专业级，%'`

### 状态条件（满足其一即可）
- `status = 1` （状态为1表示通过）
- 或 `approved_status = 1` （审批状态为1表示通过）

### 关联条件
- 专家表的 `employee_number` 与证书表的 `employee_number` 匹配
- 或专家表的 `employee_number` 与证书表的 `w3_account` 匹配

## 修复说明

**问题：** 证书表 `dwr_t_cert_record_t` 中，数据插入时使用的是 `w3_account` 字段，而 `employee_number` 字段可能为 NULL。原来的SQL只使用 `employee_number` 进行关联，导致无法匹配到认证记录。

**解决方案：** 
1. 在子查询中同时选择 `employee_number` 和 `w3_account` 字段
2. 使用 `COALESCE(employee_number, w3_account)` 作为备用关联字段
3. JOIN条件改为：`(e.employee_number = cert.employee_number OR e.employee_number = cert.w3_account)`
4. hasCert判断改为：`WHEN cert.employee_number IS NOT NULL OR cert.w3_account IS NOT NULL THEN 1`

这样无论证书表中使用哪个字段存储员工号，都能正确关联到认证记录。



