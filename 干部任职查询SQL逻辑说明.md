# 干部任职数据查询SQL逻辑说明

## 查询方法
`getCadreQualifiedDetailsByConditions` - 根据部门编码列表、AI成熟度和职位类查询干部任职详细信息

## SQL查询逻辑

### 1. 表关联关系

```sql
FROM t_cadre c
INNER JOIN t_employee e ON (SUBSTRING(e.account, 2) = c.account)
INNER JOIN t_qualifications q ON (c.account = q.employee_number)
LEFT JOIN department_info_hrms dept ON (c.mini_departname_id = dept.dept_code AND DATE(dept.update_time) = '2025-11-18')
```

**关联说明：**
- `t_cadre` (c) - 干部表，主表
  - `c.account` - 干部工号（**没有英文首字母**，如：`00123456`）
  - `c.mini_departname_id` - 最小部门编码
  - `c.position_ai_maturity` - 岗位AI成熟度
  - `c.cadre_competence_category` - 干部职位类
  - `c.cadre_type` - 干部类型

- `t_employee` (e) - 员工表
  - `e.account` - 员工工号（**有英文首字母**，如：`l00123456`）
  - `SUBSTRING(e.account, 2)` - 去除首字母后的工号（如：`00123456`）
  - 关联条件：`SUBSTRING(e.account, 2) = c.account`（去除首字母后匹配）

- `t_qualifications` (q) - 任职表
  - `q.employee_number` - 员工工号（**没有英文首字母**，如：`00123456`）
  - 关联条件：`c.account = q.employee_number`（都是无首字母工号，直接匹配）

- `department_info_hrms` (dept) - 部门信息表
  - `dept.dept_code` - 部门编码
  - `dept.dept_name` - 部门名称
  - 关联条件：`c.mini_departname_id = dept.dept_code`

### 2. WHERE条件

#### 2.1 基础条件
```sql
WHERE e.expired_date IS NULL  -- 员工未离职
AND q.employee_number IS NOT NULL  -- 任职表工号不为空
AND q.employee_number != ''  -- 任职表工号不为空字符串
AND c.account IS NOT NULL  -- 干部表工号不为空
AND c.account != ''  -- 干部表工号不为空字符串
AND e.account IS NOT NULL  -- 员工表工号不为空
AND e.account != ''  -- 员工表工号不为空字符串
AND e.account REGEXP '^[a-z]0[0-9]{7}$'  -- 员工工号格式：首字母+0+7位数字
```

#### 2.2 任职信息过滤条件
```sql
AND q.direction_cn_name IN (
    '数据科学与AI工程（ICT）',
    'AI算法及应用（ICT）',
    'AI软件工程与工具（ICT）',
    'AI系统测试（ICT）'
)  -- 只查询AI相关的任职方向
AND q.competence_from IS NOT NULL  -- 任职开始时间不为空
AND q.competence_to IS NOT NULL  -- 任职结束时间不为空
```

**注意：** 当前查询**没有**添加日期范围条件（如：`CURDATE() BETWEEN q.competence_from AND q.competence_to`），这意味着会返回所有历史任职记录，包括已过期的。

#### 2.3 动态条件
```sql
-- 部门编码过滤（可选）
<if test="deptCodes != null and deptCodes.size() > 0">
    AND c.mini_departname_id IN (#{deptCode1}, #{deptCode2}, ...)
</if>

-- AI成熟度过滤（可选）
<if test="aiMaturity != null and aiMaturity != ''">
    AND COALESCE(c.position_ai_maturity, '未知') = #{aiMaturity}
</if>

-- 职位类过滤（可选）
<if test="jobCategory != null and jobCategory != ''">
    AND COALESCE(c.cadre_competence_category, '未知') = #{jobCategory}
</if>
```

### 3. 返回字段

```sql
SELECT DISTINCT
    e.cn_name AS name,  -- 姓名
    SUBSTRING(e.account, 2) AS employeeNumber,  -- 工号（去除首字母）
    e.competence_category AS competenceCategory,  -- 职位类
    e.competence_subcategory AS competenceSubcategory,  -- 职位子类
    e.departname2,  -- 一级部门名称
    e.departname3,  -- 二级部门名称
    e.departname4,  -- 三级部门名称
    e.departname5,  -- 四级部门名称
    e.departname6,  -- 五级部门名称
    q.competence_family_cn AS competenceFamilyCn,  -- 能力族
    q.competence_category_cn AS competenceCategoryCn,  -- 能力类
    q.competence_subcategory_cn AS competenceSubcategoryCn,  -- 能力子类
    q.direction_cn_name AS directionCnName,  -- 方向中文名称
    q.competence_rating_cn AS competenceRatingCn,  -- 能力等级中文
    q.competence_grade_cn AS competenceGradeCn,  -- 能力级别中文
    q.competence_from AS competenceFrom,  -- 任职开始时间
    q.competence_to AS competenceTo,  -- 任职结束时间
    1 AS isCadre,  -- 是否为干部（固定为1）
    dept.dept_name AS miniDeptName,  -- 最小部门名称
    c.cadre_type AS cadreType  -- 干部类型
```

### 4. 排序

```sql
ORDER BY e.account, q.competence_from
```

按员工工号和任职开始时间排序。

## 可能的问题分析

### 问题1：INNER JOIN导致无数据返回
如果 `t_qualifications` 表中没有匹配的记录，`INNER JOIN` 会导致整个查询返回空结果。

**解决方案：**
- 检查 `t_qualifications` 表中是否存在对应的 `employee_number`
- 检查 `direction_cn_name` 是否匹配
- 检查 `competence_from` 和 `competence_to` 是否为空

### 问题2：关联条件可能不匹配
- `t_cadre.account` 和 `t_qualifications.employee_number` 都是无首字母工号，理论上应该能匹配
- 但可能存在数据不一致的情况（如：空格、特殊字符等）

**建议检查：**
```sql
-- 检查干部表中是否有对应的任职记录
SELECT c.account, q.employee_number, q.direction_cn_name
FROM t_cadre c
LEFT JOIN t_qualifications q ON (c.account = q.employee_number)
WHERE c.account = '00123456'  -- 替换为实际工号
LIMIT 10;
```

### 问题3：WHERE条件过滤掉所有数据
- `direction_cn_name` 必须是指定的4个值之一
- `competence_from` 和 `competence_to` 必须不为空

**建议检查：**
```sql
-- 检查t_qualifications表中的数据
SELECT 
    employee_number,
    direction_cn_name,
    competence_from,
    competence_to,
    COUNT(*) as count
FROM t_qualifications
WHERE employee_number IN (
    SELECT account FROM t_cadre LIMIT 10
)
GROUP BY employee_number, direction_cn_name, competence_from, competence_to
LIMIT 20;
```

### 问题4：是否需要添加日期范围条件
当前查询没有限制任职时间范围，如果需要只查询当前有效的任职，可以添加：
```sql
AND CURDATE() BETWEEN q.competence_from AND q.competence_to
```

## 调试建议

1. **先检查基础关联是否正常：**
```sql
SELECT 
    c.account AS cadre_account,
    SUBSTRING(e.account, 2) AS employee_account_without_prefix,
    q.employee_number AS qualification_employee_number,
    q.direction_cn_name
FROM t_cadre c
INNER JOIN t_employee e ON (SUBSTRING(e.account, 2) = c.account)
LEFT JOIN t_qualifications q ON (c.account = q.employee_number)
WHERE c.account IS NOT NULL
LIMIT 10;
```

2. **检查任职表数据：**
```sql
SELECT 
    employee_number,
    direction_cn_name,
    competence_from,
    competence_to
FROM t_qualifications
WHERE direction_cn_name IN (
    '数据科学与AI工程（ICT）',
    'AI算法及应用（ICT）',
    'AI软件工程与工具（ICT）',
    'AI系统测试（ICT）'
)
AND employee_number IN (
    SELECT account FROM t_cadre LIMIT 10
)
LIMIT 20;
```

3. **逐步添加WHERE条件，定位问题：**
   - 先不加 `direction_cn_name` 条件，看能否查到数据
   - 再逐步添加其他条件，找出哪个条件过滤掉了数据

