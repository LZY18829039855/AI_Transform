# cadre-qualified-details 接口全员查询优化方案

## 一、优化背景

### 1.1 当前实现问题

当前 `cadre-qualified-details` 接口在 `personType=0`（全员）时的查询逻辑存在以下性能问题：

1. **多表关联查询**：
   - 需要关联 `t_employee_sync`、`t_qualifications`、`t_cadre`、`t_expert`、`department_info_hrms` 等多个表
   - 复杂的 JOIN 操作导致查询性能较差

2. **多次数据库查询**：
   - 先查询所有子部门（`getAllSubDepartments`）
   - 然后遍历每个部门，逐个查询员工任职详细信息
   - 如果部门数量多，会导致 N+1 查询问题

3. **内存占用大**：
   - 需要将所有员工数据加载到内存中进行去重处理
   - 对于大部门，内存占用较高

4. **数据提取复杂**：
   - 需要从 `job_category` 字段中提取职位类
   - 需要从部门名称字段中提取部门名称（去除路径）

### 1.2 优化目标

- **性能提升**：减少数据库查询次数，提高查询效率
- **代码简化**：减少复杂的 JOIN 操作，简化查询逻辑
- **内存优化**：减少内存占用，提高系统稳定性
- **数据一致性**：与 `t_employee` 表保持一致，确保数据准确性

## 二、优化方案

### 2.1 使用 t_employee 表的优势

`t_employee` 表是一个预计算的汇总表，已经包含了所有需要的信息：

- ✅ **员工基本信息**：工号、姓名
- ✅ **部门信息**：一级到六级部门的编码和名称（`firstdeptcode` ~ `sixthdeptcode`，`firstdept` ~ `sixthdept`）
- ✅ **职位信息**：职位族、职位类、职位子类（`job_type`、`job_category`、`job_subcategory`）
- ✅ **任职资格信息**：能力族、能力类、能力子类、方向名称、能力等级、能力级别、生效日期、失效日期
- ✅ **达标信息**：任职是否达标（`is_qualifications_standard`）、认证是否达标（`is_cert_standard`）
- ✅ **最小部门信息**：`lowestdeptid`、`lowestdept`

**关键优势**：
- 无需多表 JOIN，直接查询单表即可（`t_employee` 表）
- 职位类字段（`job_category`）已经是提取后的值，无需再处理
- 部门名称字段已经是最终值，无需再提取
- 任职信息已经包含在表中，无需关联 `t_qualifications` 表
- 全员类型不需要统计干部和专家信息，无需关联 `t_cadre` 和 `t_expert` 表

### 2.2 优化后的查询逻辑

#### 2.2.1 核心思路

1. **直接查询 `t_employee` 表**，无需关联任何其他表（包括 `t_cadre`、`t_expert`、`t_qualifications` 等）
2. **根据部门层级和部门编码直接过滤**，支持查询当前部门及其所有子部门
3. **根据 `queryType` 决定是否过滤任职信息**：
   - `queryType=1`：只返回有任职信息的员工（`direction_cn_name` 不为空）
   - `queryType=2`：返回所有员工（包括没有任职信息的员工）
4. **支持职位类过滤**：直接使用 `job_category` 字段进行过滤
5. **一次性查询所有符合条件的员工**，无需遍历部门

#### 2.2.2 部门查询策略

**当前实现**：
- 查询所有子部门，然后遍历每个部门查询员工
- 需要多次数据库查询

**优化后**：
- 根据当前部门的层级，使用对应的部门编码字段进行过滤
- 例如：查询三级部门时，使用 `thirddeptcode = 部门编码` 条件，会自动包含该三级部门及其所有子部门的员工
- 一次 SQL 查询即可获取所有数据

**特殊处理：deptCode="0"**：
- 当 `deptCode="0"` 时，查询表中的所有数据
- 过滤条件只保留 `job_type = '研发族'`，不添加任何部门相关的过滤条件
- 返回所有职位族为研发族的员工数据

## 三、SQL 实现方案

### 3.1 新增 SQL 方法

在 `EmployeeMapper.xml` 中新增以下 SQL 方法：

#### 3.1.1 查询全员任职详细信息（按部门层级）

```xml
<!-- 根据部门层级和部门编码查询员工任职详细信息（全员类型，使用 t_employee 表） -->
<select id="getEmployeeQualifiedDetailsFromEmployeeTable" resultType="com.huawei.aitransform.entity.EmployeeDetailVO">
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
        e.competence_family_cn AS competenceFamilyCn,
        e.competence_category_cn AS competenceCategoryCn,
        e.competence_subcategory_cn AS competenceSubcategoryCn,
        e.direction_cn_name AS directionCnName,
        e.competence_rating_cn AS competenceRatingCn,
        e.competence_grade_cn AS competenceGradeCn,
        e.competence_from AS competenceFrom,
        e.competence_to AS competenceTo,
        -- 全员类型不需要统计干部和专家信息，直接设置为 0
        0 AS isCadre,
        NULL AS cadreType,
        0 AS isExpert,
        -- 岗位AI成熟度（全员类型不需要，设置为 NULL）
        NULL AS aiMaturity,
        e.lowestdept AS miniDeptName,
        NULL AS isQualificationsStandard,
        NULL AS isCertStandard
    FROM t_employee e
    WHERE e.job_type = '研发族'
    AND e.employee_number IS NOT NULL
    AND e.employee_number != ''
    <choose>
        <when test="deptLevel == 1">
            AND e.firstdeptcode = #{deptCode}
        </when>
        <when test="deptLevel == 2">
            AND e.seconddeptcode = #{deptCode}
        </when>
        <when test="deptLevel == 3">
            AND e.thirddeptcode = #{deptCode}
        </when>
        <when test="deptLevel == 4">
            AND e.fourthdeptcode = #{deptCode}
        </when>
        <when test="deptLevel == 5">
            AND e.fifthdeptcode = #{deptCode}
        </when>
        <when test="deptLevel == 6">
            AND e.sixthdeptcode = #{deptCode}
        </when>
        <otherwise>
            AND 1 = 0
        </otherwise>
    </choose>
    <!-- queryType=1 时，只返回有任职信息的员工 -->
    <if test="queryType != null and queryType == 1">
        AND e.direction_cn_name IS NOT NULL
        AND e.direction_cn_name != ''
        AND e.competence_from IS NOT NULL
        AND e.competence_to IS NOT NULL
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

#### 3.1.2 查询所有全员任职详细信息（deptCode="0" 时使用）

```xml
<!-- 查询所有员工任职详细信息（deptCode="0" 时，只过滤职位族为研发族） -->
<select id="getAllEmployeeQualifiedDetailsFromEmployeeTable" resultType="com.huawei.aitransform.entity.EmployeeDetailVO">
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
        e.competence_family_cn AS competenceFamilyCn,
        e.competence_category_cn AS competenceCategoryCn,
        e.competence_subcategory_cn AS competenceSubcategoryCn,
        e.direction_cn_name AS directionCnName,
        e.competence_rating_cn AS competenceRatingCn,
        e.competence_grade_cn AS competenceGradeCn,
        e.competence_from AS competenceFrom,
        e.competence_to AS competenceTo,
        -- 全员类型不需要统计干部和专家信息，直接设置为 0
        0 AS isCadre,
        NULL AS cadreType,
        0 AS isExpert,
        -- 岗位AI成熟度（全员类型不需要，设置为 NULL）
        NULL AS aiMaturity,
        e.lowestdept AS miniDeptName,
        NULL AS isQualificationsStandard,
        NULL AS isCertStandard
    FROM t_employee e
    WHERE e.job_type = '研发族'
    AND e.employee_number IS NOT NULL
    AND e.employee_number != ''
    <!-- queryType=1 时，只返回有任职信息的员工 -->
    <if test="queryType != null and queryType == 1">
        AND e.direction_cn_name IS NOT NULL
        AND e.direction_cn_name != ''
        AND e.competence_from IS NOT NULL
        AND e.competence_to IS NOT NULL
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

### 3.2 Mapper 接口方法

在 `EmployeeMapper.java` 中新增以下方法：

```java
/**
 * 根据部门层级和部门编码查询员工任职详细信息（全员类型，使用 t_employee 表）
 * @param deptLevel 部门层级
 * @param deptCode 部门编码
 * @param jobCategory 职位类（可选）
 * @param queryType 查询类型（1-任职人数，2-基线人数）
 * @return 员工任职详细信息列表
 */
List<EmployeeDetailVO> getEmployeeQualifiedDetailsFromEmployeeTable(
    @Param("deptLevel") Integer deptLevel,
    @Param("deptCode") String deptCode,
    @Param("jobCategory") String jobCategory,
    @Param("queryType") Integer queryType);

/**
 * 查询所有员工任职详细信息（deptCode="0" 时使用，只过滤职位族为研发族）
 * @param jobCategory 职位类（可选）
 * @param queryType 查询类型（1-任职人数，2-基线人数）
 * @return 员工任职详细信息列表
 */
List<EmployeeDetailVO> getAllEmployeeQualifiedDetailsFromEmployeeTable(
    @Param("jobCategory") String jobCategory,
    @Param("queryType") Integer queryType);
```

## 四、Java 代码优化

### 4.1 Service 层优化

优化 `ExpertCertStatisticsService.getCadreQualifiedDetailsByConditions` 方法中 `personType=0` 的处理逻辑：

```java
if (personType == 0) {
    // 全员处理（优化后：直接使用 t_employee 表查询）
    
    // 查询部门信息
    DepartmentInfoVO deptInfo = departmentInfoMapper.getDepartmentByCode(deptCode);
    if (deptInfo == null) {
        throw new IllegalArgumentException("部门不存在：" + deptCode);
    }
    
    String deptLevelStr = deptInfo.getDeptLevel();
    Integer deptLevel = Integer.parseInt(deptLevelStr);
    
    List<EmployeeDetailVO> allEmployeeDetails;
    
    // 特殊处理：deptCode="0" 时，查询表中的所有数据，只过滤职位族为研发族
    if ("0".equals(deptCode.trim())) {
        // 查询所有员工任职详细信息（只过滤职位族为研发族）
        allEmployeeDetails = employeeMapper.getAllEmployeeQualifiedDetailsFromEmployeeTable(
                jobCategory, queryType);
    } else {
        // 普通情况：根据部门层级和部门编码直接查询
        // 注意：使用当前部门的层级查询，会自动包含该部门及其所有子部门的员工
        allEmployeeDetails = employeeMapper.getEmployeeQualifiedDetailsFromEmployeeTable(
                deptLevel, deptCode, jobCategory, queryType);
    }
    
    // 按员工工号去重（如果同一员工在多级部门中出现，保留第一个）
    Map<String, EmployeeDetailVO> employeeMap = new HashMap<>();
    for (EmployeeDetailVO employee : allEmployeeDetails) {
        if (employee.getEmployeeNumber() != null && !employee.getEmployeeNumber().trim().isEmpty()) {
            if (!employeeMap.containsKey(employee.getEmployeeNumber())) {
                employeeMap.put(employee.getEmployeeNumber(), employee);
            }
        }
    }
    
    employeeDetails = new ArrayList<>(employeeMap.values());
}
```

### 4.2 优化前后对比

| 对比项 | 优化前 | 优化后 |
|--------|--------|--------|
| **数据库查询次数** | 1 + N 次（N 为部门数量） | 1 次 |
| **表关联数量** | 5 个表（t_employee_sync、t_qualifications、t_cadre、t_expert、department_info_hrms） | 1 个表（t_employee，无需关联其他表） |
| **数据提取** | 需要从 job_category 提取职位类，从部门名称提取部门名称 | 直接使用字段值 |
| **查询复杂度** | 复杂的 JOIN 和子查询 | 简单的 WHERE 条件过滤 |
| **内存占用** | 需要加载所有员工数据到内存 | 数据库层面过滤，减少内存占用 |
| **性能** | 较慢（特别是大部门） | 快速（单次查询） |

## 五、关键优化点说明

### 5.1 部门查询策略优化

**优化前**：
- 查询所有子部门列表
- 遍历每个部门，逐个查询员工
- 需要多次数据库查询

**优化后**：
- 根据当前部门的层级，使用对应的部门编码字段进行过滤
- 例如：查询三级部门时，使用 `thirddeptcode = 部门编码` 条件
- 该条件会自动包含该三级部门及其所有子部门的员工（因为子部门的 `thirddeptcode` 也是该三级部门的编码）
- 一次 SQL 查询即可获取所有数据

### 5.2 任职信息查询优化

**优化前**：
- 需要关联 `t_qualifications` 表
- 需要复杂的子查询来选择最高级别的任职记录
- 需要判断任职有效期

**优化后**：
- `t_employee` 表中已经包含了任职信息（`direction_cn_name`、`competence_rating_cn` 等字段）
- 直接使用 `direction_cn_name` 字段判断是否有任职信息
- 根据 `queryType` 决定是否过滤：
  - `queryType=1`：只返回 `direction_cn_name` 不为空的员工（该字段有值即为 AI 相关方向，无需再过滤）
  - `queryType=2`：返回所有员工（包括没有任职信息的员工）

### 5.3 职位类过滤优化

**优化前**：
- 需要从 `job_category` 字段中提取职位类
- 使用复杂的字符串处理逻辑

**优化后**：
- `t_employee` 表中的 `job_category` 字段已经是提取后的职位类值
- 直接使用 `job_category` 字段进行过滤
- 支持特殊值处理（"非软件类"、"其他类"）

### 5.4 干部和专家信息简化

**优化说明**：
- 全员类型查询时，不需要统计干部和专家信息
- `isCadre` 和 `isExpert` 字段直接设置为 0
- `cadreType` 和 `aiMaturity` 字段设置为 NULL
- **无需关联 `t_cadre` 和 `t_expert` 表**，进一步简化查询，提升性能

## 六、性能提升预期

### 6.1 查询性能

- **查询次数**：从 1 + N 次减少到 1 次（N 为部门数量）
- **查询时间**：预计减少 50% - 80%（取决于部门数量）
- **数据库负载**：大幅降低，减少数据库连接占用

### 6.2 内存占用

- **内存占用**：减少 30% - 50%（因为减少了数据加载和去重处理）
- **GC 压力**：降低，提高系统稳定性

### 6.3 代码复杂度

- **代码行数**：减少约 50%
- **可维护性**：提高，逻辑更清晰
- **可读性**：提高，SQL 更简单

## 七、注意事项

### 7.1 数据一致性

1. **确保 `t_employee` 表数据已更新**：
   - 确保 `t_employee` 表中的任职信息（`direction_cn_name`、`competence_rating_cn` 等字段）已正确更新
   - 确保 `job_category` 字段已经是提取后的职位类值
   - 确保部门信息字段已正确填充

2. **与现有接口保持一致**：
   - 确保优化后的查询结果与优化前保持一致
   - 确保与 `employee-cert-statistics` 接口使用相同的数据源和过滤逻辑

### 7.2 字段映射

1. **部门名称字段**：
   - `t_employee` 表中的部门名称字段（`firstdept` ~ `sixthdept`）可以直接使用，无需截取
   - 直接使用字段值即可

2. **职位类字段**：
   - `t_employee` 表中的 `job_category` 字段已经是提取后的职位类值
   - 直接使用，无需再提取

3. **任职信息字段**：
   - `t_employee` 表中的任职信息字段（`direction_cn_name`、`competence_rating_cn` 等）已经是最终值
   - 直接使用，无需关联 `t_qualifications` 表

### 7.3 特殊值处理

1. **职位类特殊值**：
   - `"非软件类"`：查询所有不是"软件类"的职位类
   - `"其他类"`：查询所有不是"研究类"、"软件类"、"系统类"、"测试类"、"产品开发项目管理类"的职位类

2. **deptCode="0" 的特殊处理**：
   - 当 `deptCode="0"` 时，查询表中的所有数据
   - 过滤条件只保留 `job_type = '研发族'`，不添加任何部门相关的过滤条件
   - 返回所有职位族为研发族的员工数据

### 7.4 去重逻辑

- 如果同一员工在多级部门中出现，保留第一个
- 使用 `Map<String, EmployeeDetailVO>` 按员工工号去重

## 八、测试建议

### 8.1 功能测试

1. **基本查询测试**：
   - 测试不同部门层级的全员查询
   - 测试 `deptCode="0"` 的特殊情况
   - 测试按职位类过滤功能
   - 测试 `queryType=1` 和 `queryType=2` 的区别

2. **数据一致性测试**：
   - 对比优化前后的查询结果，确保数据一致
   - 对比与 `employee-cert-statistics` 接口的数据一致性

3. **边界测试**：
   - 测试部门下无员工时的返回结果
   - 测试 `deptCode` 为空或不存在的情况
   - 测试职位类过滤的边界情况

### 8.2 性能测试

1. **查询性能测试**：
   - 测试大部门的查询性能
   - 测试多级部门的查询性能
   - 对比优化前后的查询时间

2. **并发测试**：
   - 测试高并发场景下的性能表现
   - 测试数据库连接池的使用情况

### 8.3 回归测试

1. **接口兼容性测试**：
   - 确保接口参数和返回结果格式不变
   - 确保前端调用不受影响

2. **数据准确性测试**：
   - 抽样对比优化前后的数据，确保准确性
   - 验证任职信息、部门信息、职位类信息的正确性

## 九、实施步骤

### 9.1 第一阶段：SQL 实现

1. 在 `EmployeeMapper.xml` 中新增 SQL 方法
2. 在 `EmployeeMapper.java` 中新增接口方法
3. 编写单元测试验证 SQL 正确性

### 9.2 第二阶段：Service 层优化

1. 修改 `ExpertCertStatisticsService.getCadreQualifiedDetailsByConditions` 方法
2. 添加日志记录，便于问题排查
3. 编写单元测试验证逻辑正确性

### 9.3 第三阶段：测试验证

1. 功能测试
2. 性能测试
3. 数据一致性验证

### 9.4 第四阶段：上线部署

1. 代码审查
2. 灰度发布
3. 监控观察
4. 全量发布

## 十、总结

通过使用 `t_employee` 表优化 `cadre-qualified-details` 接口在 `personType=0` 时的查询逻辑，可以：

- ✅ **大幅提升查询性能**：从多次查询优化为单次查询，无需关联任何其他表
- ✅ **简化代码逻辑**：完全消除 JOIN 操作，直接查询单表
- ✅ **降低内存占用**：减少数据加载和去重处理
- ✅ **提高可维护性**：代码更简洁，逻辑更清晰
- ✅ **保持数据一致性**：与 `t_employee` 表保持一致，确保数据准确性
- ✅ **极致性能优化**：全员类型查询时，`isCadre` 和 `isExpert` 直接设置为 0，无需关联干部和专家表

该优化方案与 `competence-category-cert-statistics` 接口的优化思路一致，可以统一代码风格和查询逻辑，提高系统的整体性能和可维护性。

