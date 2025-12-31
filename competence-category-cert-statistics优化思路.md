# competence-category-cert-statistics 接口优化思路

## 当前问题分析

当前 `getEmployeeCompetenceCategoryCertStatistics` 方法的性能问题：

1. **多次数据库查询**：
   - 先查询所有员工信息（`getEmployeesWithJobCategoryByDeptCodes`）
   - 再批量查询认证状态（`getCertifiedEmployeeNumbers`）
   - 再批量查询任职状态（`getQualifiedEmployeeNumbers`）
   - 在 Java 代码中循环处理每个员工

2. **查询所有层级子部门**：
   - 使用 `getAllSubDepartments` 查询所有层级的子部门
   - 可能导致查询范围过大

3. **使用 t_employee_sync 表**：
   - 需要依赖 `period_id = 20251126` 的固定周期数据
   - 需要额外 JOIN 认证表和任职表

## 优化思路（参考 employee-cert-statistics）

### 1. 使用 t_employee 表的预计算字段

**优化点**：
- `t_employee` 表中有 `is_cert_standard` 和 `is_qualifications_standard` 字段
- 这些字段已经预先计算好了认证和任职状态
- 可以直接在 SQL 中使用，无需额外 JOIN 认证表和任职表

**优势**：
- 减少数据库查询次数
- 避免复杂的 JOIN 操作
- 提高查询性能

### 2. 在 SQL 中按职位类批量统计

**优化点**：
- 创建新的 SQL 方法 `getCompetenceCategoryStatisticsByLevel`
- 在 SQL 层面直接按职位类分组统计
- **重要**：`t_employee` 表中的 `job_category` 字段就是职位类，不需要再进行分割提取
- 直接使用 `job_category` 字段作为职位类进行分组
- 使用聚合函数统计：
  ```sql
  COUNT(1) AS totalCount,
  SUM(CASE WHEN is_cert_standard = 1 THEN 1 ELSE 0 END) AS certifiedCount,
  SUM(CASE WHEN is_qualifications_standard = 1 THEN 1 ELSE 0 END) AS qualifiedCount
  ```

**优势**：
- 一次 SQL 查询即可得到按职位类分组的统计数据
- 避免在 Java 代码中循环处理
- 数据库层面计算更高效
- 简化 SQL 逻辑，直接使用 `job_category` 字段

### 3. 只查询下一层子部门

**优化点**：
- 参考 `employee-cert-statistics` 的逻辑
- 使用 `getChildDepartments` 只查询下一层子部门
- 不再使用 `getAllSubDepartments` 查询所有层级

**优势**：
- 减少查询范围
- 提高查询效率
- 与 `employee-cert-statistics` 保持一致

### 4. 特殊处理 deptCode="0"

**优化点**：
- 当 `deptCode = "0"` 时，查询云核心网产品线部门下的所有四级部门
- 使用 `getLevel4DepartmentsUnderLevel2` 方法
- 创建对应的 SQL 方法 `getLevel4CompetenceCategoryStatisticsUnderLevel2`

**优势**：
- 与 `employee-cert-statistics` 接口行为保持一致
- 支持特殊业务场景

### 5. 新增 SQL 方法设计

需要在 `EmployeeMapper.xml` 中新增以下 SQL 方法：

#### 5.1 按职位类统计下一层部门的统计数据
```sql
<select id="getCompetenceCategoryStatisticsByLevel" resultType="...">
    SELECT 
        <choose>
            <when test="currentLevel == 1">seconddeptcode</when>
            <when test="currentLevel == 2">thirddeptcode</when>
            <when test="currentLevel == 3">fourthdeptcode</when>
            <when test="currentLevel == 4">fifthdeptcode</when>
            <when test="currentLevel == 5">sixthdeptcode</when>
            <when test="currentLevel == 6">lowestdeptid</when>
            <otherwise>NULL</otherwise>
        </choose> AS deptCode,
        job_category AS competenceCategory,
        COUNT(1) AS totalCount,
        SUM(CASE WHEN is_cert_standard = 1 THEN 1 ELSE 0 END) AS certifiedCount,
        SUM(CASE WHEN is_qualifications_standard = 1 THEN 1 ELSE 0 END) AS qualifiedCount
    FROM t_employee
    WHERE job_type = '研发族'
    AND employee_number IS NOT NULL
    AND employee_number != ''
    <choose>
        <when test="currentLevel == 1">
            AND firstdeptcode = #{deptCode}
            GROUP BY seconddeptcode, job_category
        </when>
        <when test="currentLevel == 2">
            AND seconddeptcode = #{deptCode}
            GROUP BY thirddeptcode, job_category
        </when>
        <when test="currentLevel == 3">
            AND thirddeptcode = #{deptCode}
            GROUP BY fourthdeptcode, job_category
        </when>
        <when test="currentLevel == 4">
            AND fourthdeptcode = #{deptCode}
            GROUP BY fifthdeptcode, job_category
        </when>
        <when test="currentLevel == 5">
            AND fifthdeptcode = #{deptCode}
            GROUP BY sixthdeptcode, job_category
        </when>
        <when test="currentLevel == 6">
            AND sixthdeptcode = #{deptCode}
            GROUP BY lowestdeptid, job_category
        </when>
        <otherwise>
            AND 1 = 0
        </otherwise>
    </choose>
</select>
```

#### 5.2 按职位类统计二级部门下的四级部门（deptCode="0" 时使用）
```sql
<select id="getLevel4CompetenceCategoryStatisticsUnderLevel2" resultType="...">
    SELECT 
        fourthdeptcode AS deptCode,
        job_category AS competenceCategory,
        COUNT(1) AS totalCount,
        SUM(CASE WHEN is_cert_standard = 1 THEN 1 ELSE 0 END) AS certifiedCount,
        SUM(CASE WHEN is_qualifications_standard = 1 THEN 1 ELSE 0 END) AS qualifiedCount
    FROM t_employee
    WHERE job_type = '研发族'
    AND employee_number IS NOT NULL
    AND employee_number != ''
    AND seconddeptcode = #{deptCode}
    GROUP BY fourthdeptcode, job_category
</select>
```

#### 5.3 查询总计数据（统计当前部门及其所有子部门的研发族人员）
```sql
<select id="getTotalCompetenceCategoryStatisticsByLevel" resultType="...">
    SELECT 
        COUNT(1) AS totalCount,
        SUM(CASE WHEN is_cert_standard = 1 THEN 1 ELSE 0 END) AS certifiedCount,
        SUM(CASE WHEN is_qualifications_standard = 1 THEN 1 ELSE 0 END) AS qualifiedCount
    FROM t_employee
    WHERE job_type = '研发族'
    AND employee_number IS NOT NULL
    AND employee_number != ''
    <choose>
        <when test="currentLevel == 1">
            AND firstdeptcode = #{deptCode}
        </when>
        <when test="currentLevel == 2">
            AND seconddeptcode = #{deptCode}
        </when>
        <when test="currentLevel == 3">
            AND thirddeptcode = #{deptCode}
        </when>
        <when test="currentLevel == 4">
            AND fourthdeptcode = #{deptCode}
        </when>
        <when test="currentLevel == 5">
            AND fifthdeptcode = #{deptCode}
        </when>
        <when test="currentLevel == 6">
            AND sixthdeptcode = #{deptCode}
        </when>
        <otherwise>
            AND 1 = 0
        </otherwise>
    </choose>
</select>
```

#### 5.4 查询总计数据（deptCode="0" 时使用，统计整个表的研发族人员）
```sql
<select id="getTotalCompetenceCategoryStatisticsForAllEmployees" resultType="...">
    SELECT 
        COUNT(1) AS totalCount,
        SUM(CASE WHEN is_cert_standard = 1 THEN 1 ELSE 0 END) AS certifiedCount,
        SUM(CASE WHEN is_qualifications_standard = 1 THEN 1 ELSE 0 END) AS qualifiedCount
    FROM t_employee
    WHERE job_type = '研发族'
    AND employee_number IS NOT NULL
    AND employee_number != ''
</select>
```

### 6. Java 代码优化流程

优化后的 `getEmployeeCompetenceCategoryCertStatistics` 方法流程：

1. **处理 deptCode="0" 的特殊情况**
   - 查询云核心网产品线下的所有四级部门
   - 使用 `getLevel4CompetenceCategoryStatisticsUnderLevel2` 批量查询明细统计数据（四级部门的数据）
   - 使用 `getTotalCompetenceCategoryStatisticsForAllEmployees` 查询总计数据（整个表中的研发族人员数据，按职位类分组）

2. **普通情况处理**
   - 查询部门信息
   - 查询下一层子部门列表（`getChildDepartments`）
   - 使用 `getCompetenceCategoryStatisticsByLevel` 批量查询明细统计数据（下一层部门的数据）
   - 使用 `getTotalCompetenceCategoryStatisticsByLevel` 查询总计数据（当前部门及其所有子部门的数据）

3. **数据聚合处理**
   - 将 SQL 返回的统计数据按职位类分组（按 deptCode + competenceCategory 分组）
   - 计算每个职位类的认证率和任职率
   - **计算总计数据**（重要）：
     - 使用 `getTotalCompetenceCategoryStatisticsByLevel` 方法单独查询总计数据
     - 查询条件：当前部门ID + 研发族（不需要按职位类分组）
     - 例如：查询三级部门时，总计数据查询条件为 `thirddeptcode = 三级部门ID AND job_type = '研发族'`
     - 总计总人数 = COUNT(*) WHERE 查询条件（所有符合条件的研发族人员总数）
     - 总计认证人数 = SUM(CASE WHEN is_cert_standard = 1 THEN 1 ELSE 0 END) WHERE 查询条件
     - 总计任职人数 = SUM(CASE WHEN is_qualifications_standard = 1 THEN 1 ELSE 0 END) WHERE 查询条件
     - 总计认证率 = 总计认证人数 / 总计总人数 × 100%
     - 总计任职率 = 总计任职人数 / 总计总人数 × 100%
   
   **总计计算说明**：
   - **明细数据**：返回的是下一层部门的统计数据（例如：三级部门返回四级部门的数据，按四级部门 + 职位类分组）
   - **总计数据**：统计的是当前部门及其所有子部门的研发族人员（例如：三级部门的总计 = 三级部门及其所有子部门的研发族人员）
   - 总计数据需要单独查询，不能简单累加明细数据（因为明细数据只包含下一层部门）
   - 总计数据的查询逻辑：
     - 查询条件：当前部门ID + 研发族（例如：`thirddeptcode = 三级部门ID AND job_type = '研发族'`）
     - 直接统计所有符合条件的员工，不需要按职位类分组
     - 总人数 = 所有符合条件的员工数量（基数）
     - 认证人数 = 其中已认证的员工数量
     - 任职人数 = 其中已任职的员工数量
     - 然后计算占比（认证率、任职率）

4. **构建返回结果**

### 7. 性能提升预期

- **查询次数**：从 3+N 次减少到 1-2 次（N 为员工数量）
- **数据量**：从查询所有员工明细减少到只查询统计结果
- **计算效率**：从 Java 循环计算改为 SQL 聚合计算
- **内存占用**：大幅减少，不再需要加载所有员工数据到内存

### 8. 注意事项

1. **数据一致性**：
   - 确保 `t_employee` 表的 `is_cert_standard` 和 `is_qualifications_standard` 字段已正确更新
   - 与 `employee-cert-statistics` 接口使用相同的数据源

2. **职位类字段使用**：
   - `t_employee` 表中的 `job_category` 字段就是职位类，不需要再进行分割提取
   - 直接使用 `job_category` 字段作为职位类进行分组和统计
   - 不再需要 `extractCompetenceCategory` 方法

3. **部门过滤**：
   - 保持与 `employee-cert-statistics` 相同的部门过滤逻辑
   - 过滤掉 "C Lab（模块）" 和 "云核心网产品组合与生命周期管理部"

4. **总计统计逻辑（重要）**：
   
   **总计计算规则**：
   - 当用户传入三级部门ID时：
     - **总计总人数** = 三级部门及其所有子部门的研发族人员总数
     - **总计认证/任职人数** = 四级部门（下一层）的认证/任职数据
   
   **实现方式**：
   - SQL `getCompetenceCategoryStatisticsByLevel` 查询的是下一层部门的统计数据
   - 例如：查询三级部门时，SQL 会查询所有 `thirddeptcode = 三级部门ID` 的员工，按 `fourthdeptcode` 分组
   - 每个四级部门的统计数据 = 该四级部门及其所有子部门的员工
   - 总计总人数 = 所有四级部门的 totalCount 累加 = 三级部门及其所有子部门的研发族人员总数 ✓
   - 总计认证/任职人数 = 所有四级部门的 certifiedCount/qualifiedCount 累加 = 四级部门的认证/任职数据 ✓
   
   **注意**：
   - 明细数据：返回的是下一层部门的统计数据（例如：三级部门返回四级部门的数据，按四级部门 + 职位类分组）
   - 总计数据：需要单独查询，查询条件为当前部门ID + 研发族（不分组，直接统计所有符合条件的员工）
   - 总计总人数 = 当前部门及其所有子部门的研发族人员总数（基数）
   - 总计认证/任职人数 = 当前部门及其所有子部门的研发族人员中已认证/任职的人数
   - 总计认证率 = 总计认证人数 / 总计总人数 × 100%
   - 总计任职率 = 总计任职人数 / 总计总人数 × 100%
   - 当 deptCode="0" 时，总计数据使用 `getTotalCompetenceCategoryStatisticsForAllEmployees` 查询整个表中的研发族人员数据（不分组，直接统计所有符合条件的员工）
   
   **示例说明**：
   - 假设用户传入三级部门ID（deptCode = "三级部门A"）
   
   **明细数据查询**：
   - SQL 查询条件：`thirddeptcode = "三级部门A"`，按 `fourthdeptcode` 和 `job_category` 分组
   - SQL 返回结果示例（四级部门的统计数据）：
     ```
     四级部门1 - 软件类: totalCount=100, certifiedCount=50, qualifiedCount=60
     四级部门1 - 硬件类: totalCount=80, certifiedCount=40, qualifiedCount=45
     四级部门2 - 软件类: totalCount=120, certifiedCount=60, qualifiedCount=70
     四级部门2 - 硬件类: totalCount=90, certifiedCount=45, qualifiedCount=50
     ```
   - 按职位类聚合后（跨四级部门）：
     ```
     软件类: totalCount=220, certifiedCount=110, qualifiedCount=130
     硬件类: totalCount=170, certifiedCount=85, qualifiedCount=95
     ```
   
   **总计数据查询**：
   - SQL 查询条件：`thirddeptcode = "三级部门A" AND job_type = '研发族'`（不分组，直接统计所有符合条件的员工）
   - SQL 返回结果示例（三级部门及其所有子部门的统计数据）：
     ```
     总计: totalCount=450, certifiedCount=225, qualifiedCount=270
     总计认证率 = 225/450 × 100% = 50%
     总计任职率 = 270/450 × 100% = 60%
     ```
   - **说明**：
     - 明细数据：返回的是四级部门的统计数据（下一层部门，按四级部门 + 职位类分组）
     - 总计数据：统计的是三级部门A及其所有子部门的研发族人员总数（包括三级部门本身、四级部门及其所有子部门的员工）
     - 总计数据需要单独查询，查询条件为 `thirddeptcode = "三级部门A" AND job_type = '研发族'`，直接统计所有符合条件的员工，不需要按职位类分组
     - 总计总人数 = 所有符合条件的研发族人员数量（基数）
     - 总计认证人数 = 其中已认证的员工数量
     - 总计任职人数 = 其中已任职的员工数量
     - 然后计算占比（认证率、任职率）

## 总结

通过以上优化，可以将 `competence-category-cert-statistics` 接口的性能提升到与 `employee-cert-statistics` 接口相当的水平，同时保持代码逻辑的一致性。

