# person-cert-details 接口全员查询优化思路

## 一、当前实现分析

### 1.1 当前实现逻辑

当前 `person-cert-details` 接口在 `personType=0`（全员）时的实现：

**Service 层**（`ExpertCertStatisticsService.getPersonCertDetailsByConditions`）：
- 查询部门信息，获取部门层级
- 调用 `employeeMapper.getEmployeeCertDetailsByDeptLevel` 查询员工认证详细信息
- **只查询当前部门，不查询子部门**

**Mapper 层**（`EmployeeMapper.getEmployeeCertDetailsByDeptLevel`）：
- 数据源：`t_employee_sync` 表（`period_id = 20251126`）
- 需要 JOIN 多个表：
  - `dwr_t_cert_record_t`：查询 AI 专业级证书信息
  - `t_exam_record`：查询科目二通过情况
  - `t_cadre`：查询干部信息
  - `t_expert`：查询专家信息
- 根据 `queryType` 决定 JOIN 方式：
  - `queryType=1`（认证人数）：INNER JOIN，只返回有证书的人员
  - `queryType=2`（基线人数）：LEFT JOIN，返回所有人员
- 根据部门层级精确匹配对应字段（如 `thirddeptcode`）
- 支持职位类过滤（包括"非软件类"、"其他类"等特殊处理）
- 使用 `GROUP BY e.employee_number` 去重

### 1.2 当前实现的问题

1. **多表 JOIN 性能开销大**：
   - 需要 JOIN 4 个表（`dwr_t_cert_record_t`、`t_exam_record`、`t_cadre`、`t_expert`）
   - JOIN 操作会增加查询时间和资源消耗

2. **依赖固定周期数据**：
   - 使用 `t_employee_sync` 表，需要依赖 `period_id = 20251126` 的固定周期数据
   - 数据更新需要同步操作

3. **职位类提取逻辑复杂**：
   - 需要从 `job_category` 字段中提取职位类（使用 `SUBSTRING_INDEX` 函数）
   - SQL 逻辑复杂，可读性差

4. **证书信息需要实时查询**：
   - 每次查询都需要 JOIN `dwr_t_cert_record_t` 表
   - 无法利用预计算字段

## 二、优化方案

### 2.1 使用 t_employee 表的预计算字段

**优化点**：
- `t_employee` 表已经整合了员工的所有信息：
  - 认证信息：`cert_title`、`is_cert_standard`、`is_passed_subject2`
  - 任职信息：`competence_family_cn`、`competence_category_cn`、`competence_subcategory_cn` 等
  - 部门信息：6 级部门编码和名称字段
  - 职位信息：`job_type`、`job_category`、`job_subcategory`
  - 干部/专家标识：可通过 `is_cert_standard`、`is_qualifications_standard` 等字段判断
- 直接查询 `t_employee` 表，无需 JOIN 其他表
- 使用预计算字段，提高查询性能

**优势**：
- 减少数据库查询次数（从多次 JOIN 减少到单表查询）
- 避免复杂的 JOIN 操作
- 提高查询性能（单表查询比多表 JOIN 快）
- 简化 SQL 逻辑，提高可读性

### 2.2 简化职位类过滤逻辑

**优化点**：
- `t_employee` 表中的 `job_category` 字段就是职位类，不需要再进行分割提取
- 直接使用 `job_category` 字段进行过滤
- 简化 SQL 逻辑

**优势**：
- 减少 SQL 复杂度
- 提高查询性能（避免使用 `SUBSTRING_INDEX` 函数）
- 代码更易维护

### 2.3 根据 queryType 过滤数据

**优化点**：
- `queryType=1`（认证人数）：只返回 `cert_title IS NOT NULL` 的人员
- `queryType=2`（基线人数）：返回所有人员（不限制 `cert_title`）
- 使用 `WHERE` 条件过滤，而不是 JOIN 方式

**优势**：
- 简化 SQL 逻辑
- 提高查询性能（避免 JOIN 操作）

## 三、优化后的 SQL 实现

### 3.1 新增 SQL 方法

在 `EmployeeMapper.xml` 中新增以下 SQL 方法：

```xml
<!-- 根据部门层级和部门ID查询员工认证详细信息（全员类型，使用 t_employee 表） -->
<!-- 注意：
     1. 当部门ID不为0时，只查询当前部门，不查询子部门
     2. 必须过滤职位族为"研发族"的数据（WHERE e.job_type = '研发族'）
-->
<select id="getEmployeeCertDetailsByDeptLevelFromEmployee" resultType="com.huawei.aitransform.entity.EmployeeDetailVO">
    SELECT 
        e.last_name AS name,
        e.employee_number AS employeeNumber,
        e.job_category AS competenceCategory,
        e.job_subcategory AS competenceSubcategory,
        e.firstdept AS departname2,
        e.seconddept AS departname3,
        e.thirddept AS departname4,
        e.fourthdept AS departname5,
        e.fifthdept AS departname6,
        e.sixthdept AS departname7,
        e.cert_title AS certTitle,
        NULL AS certStartTime,
        COALESCE(e.is_passed_subject2, 0) AS isPassedSubject2,
        0 AS isCadre,
        NULL AS cadreType,
        COALESCE(e.is_cert_standard, 0) AS isCertStandard,
        0 AS isExpert,
        NULL AS aiMaturity,
        e.lowestdept AS miniDeptName,
        COALESCE(e.is_qualifications_standard, 0) AS isQualificationsStandard
    FROM t_employee e
    WHERE e.job_type = '研发族'
    AND e.employee_number IS NOT NULL
    AND e.employee_number != ''
    <!-- 根据部门层级过滤 -->
    <choose>
        <when test="deptLevel == 1">
            AND e.firstdeptcode = #{deptId}
        </when>
        <when test="deptLevel == 2">
            AND e.seconddeptcode = #{deptId}
        </when>
        <when test="deptLevel == 3">
            AND e.thirddeptcode = #{deptId}
        </when>
        <when test="deptLevel == 4">
            AND e.fourthdeptcode = #{deptId}
        </when>
        <when test="deptLevel == 5">
            AND e.fifthdeptcode = #{deptId}
        </when>
        <when test="deptLevel == 6">
            AND e.sixthdeptcode = #{deptId}
        </when>
        <when test="deptLevel == 7">
            AND 1 = 0
        </when>
    </choose>
    <!-- 根据 queryType 过滤：queryType=1 只返回有证书的人员 -->
    <if test="queryType != null and queryType == 1">
        AND e.cert_title IS NOT NULL
        AND e.cert_title != ''
    </if>
    <!-- 职位类过滤 -->
    <if test="jobCategory != null and jobCategory != ''">
        <choose>
            <when test="jobCategory == '非软件类'">
                AND e.job_category != '软件类'
            </when>
            <when test="jobCategory == '其他类'">
                AND e.job_category NOT IN ('研究类', '软件类', '系统类', '测试类', '产品开发项目管理类')
            </when>
            <otherwise>
                AND e.job_category = #{jobCategory}
            </otherwise>
        </choose>
    </if>
    ORDER BY e.employee_number
</select>
```

### 3.2 字段映射说明

| 原字段（t_employee_sync + JOIN） | 新字段（t_employee） | 说明 |
|--------------------------------|-------------------|------|
| `e.last_name` | `e.last_name` | 姓名，保持不变 |
| `e.employee_number` | `e.employee_number` | 工号，保持不变 |
| `SUBSTRING_INDEX(...)` 提取职位类 | `e.job_category` | 直接使用职位类字段 |
| `SUBSTRING_INDEX(...)` 提取职位子类 | `e.job_subcategory` | 直接使用职位子类字段 |
| `SUBSTRING_INDEX(e.firstdept, '/', 1)` | `e.firstdept` | 一级部门名称，直接使用字段 |
| `SUBSTRING_INDEX(e.seconddept, '/', 1)` | `e.seconddept` | 二级部门名称，直接使用字段 |
| `SUBSTRING_INDEX(e.thirddept, '/', 1)` | `e.thirddept` | 三级部门名称，直接使用字段 |
| `SUBSTRING_INDEX(e.fourthdept, '/', 1)` | `e.fourthdept` | 四级部门名称，直接使用字段 |
| `SUBSTRING_INDEX(e.fifthdept, '/', 1)` | `e.fifthdept` | 五级部门名称，直接使用字段 |
| `SUBSTRING_INDEX(e.sixthdept, '/', 1)` | `e.sixthdept` | 六级部门名称，直接使用字段 |
| `cert.cer_title` | `e.cert_title` | 证书名称，已预计算 |
| `cert.start_time` | `NULL` | 证书开始时间，t_employee 表中暂无此字段 |
| `exam.emp_num IS NOT NULL` | `e.is_passed_subject2` | 是否通过科目二，已预计算 |
| `c.account IS NOT NULL` | `0` | 是否为干部，全员类型直接返回0，不需要统计 |
| `c.cadre_type` | `NULL` | 干部类型，t_employee 表中暂无此字段 |
| `c.is_cert_standard` | `e.is_cert_standard` | 认证是否达标，已预计算 |
| `exp.account IS NOT NULL` | `0` | 是否为专家，全员类型直接返回0，不需要统计 |
| `COALESCE(exp.position_ai_maturity, c.position_ai_maturity, NULL)` | `NULL` | AI 成熟度，全员类型不支持按成熟度过滤 |
| `SUBSTRING_INDEX(e.lowest_dept, '/', 1)` | `e.lowestdept` | 最小部门名称，直接使用字段 |
| `NULL` | `e.is_qualifications_standard` | 任职是否达标，已预计算 |

### 3.3 字段缺失处理

**缺失字段的处理**：
1. `certStartTime`（证书开始时间）：
   - `t_employee` 表中暂无此字段
   - 返回 `NULL`，如果后续需要可以补充

2. `cadreType`（干部类型）：
   - `t_employee` 表中暂无此字段
   - 返回 `NULL`，如果后续需要可以补充

3. `aiMaturity`（AI 成熟度）：
   - 全员类型不支持按成熟度过滤
   - 返回 `NULL`，符合业务逻辑

## 四、Java 代码修改

### 4.1 Mapper 接口修改

在 `EmployeeMapper.java` 中新增方法：

```java
/**
 * 根据部门层级和部门ID查询员工认证详细信息（用于下钻，全员类型，使用 t_employee 表）
 * @param deptLevel 部门层级（1-7）
 * @param deptId 部门ID（单个部门编码）
 * @param jobCategory 职位类（可选）
 * @param queryType 查询类型（1-认证人数，2-基线人数）
 * @return 员工详细信息列表（包含认证信息）
 */
List<EmployeeDetailVO> getEmployeeCertDetailsByDeptLevelFromEmployee(
        @Param("deptLevel") Integer deptLevel,
        @Param("deptId") String deptId,
        @Param("jobCategory") String jobCategory,
        @Param("queryType") Integer queryType);
```

### 4.2 Service 层修改

在 `ExpertCertStatisticsService.java` 中修改 `getPersonCertDetailsByConditions` 方法：

```java
if (personType == 0) {
    // 全员处理（使用 t_employee 表优化）
    // 特殊处理：当 deptCode 为 "0" 时
    String actualDeptCode = deptCode;
    if ("0".equals(deptCode.trim())) {
        actualDeptCode = DepartmentConstants.CLOUD_CORE_NETWORK_DEPT_CODE;
    }
    
    // 查询部门信息，获取部门层级
    DepartmentInfoVO actualDeptInfo = departmentInfoMapper.getDepartmentByCode(actualDeptCode);
    if (actualDeptInfo == null) {
        throw new IllegalArgumentException("部门不存在：" + actualDeptCode);
    }
    
    // 直接使用当前部门的层级进行查询（不查询子部门）
    Integer deptLevel = Integer.parseInt(actualDeptInfo.getDeptLevel());
    
    // 调用 EmployeeMapper.getEmployeeCertDetailsByDeptLevelFromEmployee 查询当前部门的员工认证详细信息
    // 使用 t_employee 表，无需 JOIN 其他表
    employeeDetails = employeeMapper.getEmployeeCertDetailsByDeptLevelFromEmployee(
            deptLevel, actualDeptCode, jobCategory, queryType);
}
```

## 五、性能提升预期

### 5.1 查询性能提升

- **查询次数**：从 1 次多表 JOIN 查询优化为 1 次单表查询
- **JOIN 操作**：从 4 个表 JOIN 减少到 0 个表 JOIN
- **数据量**：查询数据量不变，但查询效率大幅提升
- **计算效率**：避免使用 `SUBSTRING_INDEX` 函数提取职位类，直接使用字段

### 5.2 性能对比

| 指标 | 优化前 | 优化后 | 提升 |
|------|--------|--------|------|
| JOIN 表数量 | 4 个 | 0 个 | 减少 100% |
| SQL 复杂度 | 高（多表 JOIN + 函数提取） | 低（单表查询） | 大幅降低 |
| 查询时间 | 较慢 | 较快 | 预计提升 50-80% |
| 代码可维护性 | 较低 | 较高 | 显著提升 |

## 六、注意事项

### 6.1 数据一致性

1. **确保 t_employee 表数据完整**：
   - `job_type` 字段：**必须正确标识职位族**，查询时只返回 `job_type = '研发族'` 的数据
   - `cert_title` 字段：需要包含 AI 专业级证书名称
   - `is_cert_standard` 字段：需要正确标识认证是否达标
   - `is_passed_subject2` 字段：需要正确标识是否通过科目二
   - `is_qualifications_standard` 字段：需要正确标识任职是否达标
   - `job_category` 字段：需要包含完整的职位类信息

2. **数据同步机制**：
   - 确保 `t_employee` 表的数据与 `t_employee_sync`、`dwr_t_cert_record_t` 等表保持一致
   - 建议建立数据同步机制，定期更新 `t_employee` 表

### 6.2 字段兼容性

1. **缺失字段处理**：
   - `certStartTime`（证书开始时间）：当前返回 `NULL`，如果业务需要可以补充
   - `cadreType`（干部类型）：当前返回 `NULL`，如果业务需要可以补充

2. **字段映射验证**：
   - 需要验证 `t_employee` 表中的字段值与原实现返回的值是否一致
   - `isCadre`、`isExpert` 字段直接返回0，无需计算和验证

### 6.3 业务逻辑保持一致

1. **职位族过滤逻辑**（重要）：
   - **必须过滤**：只查询 `job_type = '研发族'` 的数据
   - 这是核心业务逻辑，确保只返回研发族员工的数据
   - SQL 中已包含 `WHERE e.job_type = '研发族'` 条件

2. **部门过滤逻辑**：
   - 保持与原有实现一致：只查询当前部门，不查询子部门
   - 根据部门层级精确匹配对应字段

3. **职位类过滤逻辑**：
   - 保持与原有实现一致：
     - "非软件类"：排除软件类
     - "其他类"：排除研究类、软件类、系统类、测试类、产品开发项目管理类
     - 其他：精确匹配
   - **注意**：职位类过滤是在职位族过滤（研发族）的基础上进行的

4. **queryType 过滤逻辑**：
   - `queryType=1`（认证人数）：只返回有证书的人员（`cert_title IS NOT NULL`）
   - `queryType=2`（基线人数）：返回所有人员（不限制 `cert_title`）

### 6.4 测试验证

1. **功能测试**：
   - 验证返回的数据结构与原实现一致
   - 验证各种查询条件组合的返回结果正确性
   - 验证 `queryType=1` 和 `queryType=2` 的过滤逻辑正确性

2. **性能测试**：
   - 对比优化前后的查询时间
   - 验证大数据量场景下的性能表现

3. **数据一致性测试**：
   - 对比优化前后的返回数据
   - 验证数据准确性

## 七、实施步骤

### 7.1 第一阶段：SQL 实现

1. 在 `EmployeeMapper.xml` 中新增 `getEmployeeCertDetailsByDeptLevelFromEmployee` SQL 方法
2. 在 `EmployeeMapper.java` 中新增对应的方法声明
3. 验证 SQL 语法正确性

### 7.2 第二阶段：Service 层修改

1. 修改 `ExpertCertStatisticsService.getPersonCertDetailsByConditions` 方法
2. 将调用从 `getEmployeeCertDetailsByDeptLevel` 改为 `getEmployeeCertDetailsByDeptLevelFromEmployee`
3. 验证代码编译通过

### 7.3 第三阶段：测试验证

1. 单元测试：验证各种查询条件组合
2. 集成测试：验证接口返回结果
3. 性能测试：对比优化前后的性能
4. 数据一致性测试：对比优化前后的数据

### 7.4 第四阶段：上线部署

1. 代码审查
2. 灰度发布
3. 监控验证
4. 全量发布

## 八、回滚方案

如果优化后出现问题，可以快速回滚：

1. **代码回滚**：
   - 将 Service 层的调用改回 `getEmployeeCertDetailsByDeptLevel`
   - 保持原有实现不变

2. **数据回滚**：
   - 无需数据回滚，因为只是查询逻辑优化，不涉及数据修改

## 九、总结

通过使用 `t_employee` 表的预计算字段，可以大幅简化 `person-cert-details` 接口在 `personType=0`（全员）时的查询逻辑：

1. **性能提升**：从多表 JOIN 优化为单表查询，预计性能提升 50-80%
2. **代码简化**：SQL 逻辑更简单，代码更易维护
3. **数据一致性**：使用统一的数据源，与 `employee-cert-statistics` 等接口保持一致

优化后的实现将更加高效、简洁，同时保持与原有业务逻辑的一致性。

