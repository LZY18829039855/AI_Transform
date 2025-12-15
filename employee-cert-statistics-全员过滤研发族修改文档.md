# employee-cert-statistics 接口全员查询逻辑修改文档

## 一、需求说明

当前 `employee-cert-statistics` 接口在统计总人数以及各个部门的人数时，需要增加以下过滤条件：

1. **过滤职位族为"研发族"的员工**：
   - 在 `t_employee_sync` 表中，`job_category` 字段的第一个 `-` 前面的文字为"研发族"的员工才计入统计
   - 例如：`job_category = "研发族-研究类-算法"` 应该被统计，`job_category = "市场族-销售类"` 不应该被统计

2. **包含对应部门的干部**：
   - 即使干部不是研发族，也需要包含在统计范围内
   - 干部表为 `t_cadre`，通过 `mini_departname_id` 字段关联部门

## 二、当前实现分析

### 2.1 当前查询逻辑

当前在 `ExpertCertStatisticsService.getEmployeeCertStatistics()` 方法中（`personType=0` 时），查询员工工号的逻辑如下：

```java
// 4.1 查询该部门下的员工工号列表
List<String> deptIdList = new ArrayList<>();
deptIdList.add(dept.getDeptCode());
List<String> employeeNumbers = employeeMapper.getEmployeeNumbersByDeptLevel(queryLevel, deptIdList);
```

### 2.2 当前 SQL 查询

`EmployeeMapper.xml` 中的 `getEmployeeNumbersByDeptLevel` 方法：

```xml
<select id="getEmployeeNumbersByDeptLevel" resultType="java.lang.String">
    SELECT DISTINCT employee_number
    FROM t_employee_sync
    WHERE period_id = 20251126
    <choose>
        <when test="deptLevel == 1">
            AND firstdeptcode IN ...
        </when>
        <!-- 其他层级类似 -->
    </choose>
    AND employee_number IS NOT NULL
    AND employee_number != ''
</select>
```

### 2.3 干部查询方法

已存在 `CadreMapper.getCadreEmployeeNumbersByDeptCodes()` 方法，可以根据部门编码列表查询干部工号：

```java
List<String> getCadreEmployeeNumbersByDeptCodes(@Param("deptCodes") List<String> deptCodes);
```

对应的 SQL 查询：

```xml
<select id="getCadreEmployeeNumbersByDeptCodes" resultType="java.lang.String">
    SELECT DISTINCT account
    FROM t_cadre
    WHERE mini_departname_id IN ...
    AND account IS NOT NULL
    AND account != ''
</select>
```

## 三、修改方案

### 3.1 方案概述

采用**两步查询 + 合并去重**的方式：

1. **第一步**：修改 `EmployeeMapper.getEmployeeNumbersByDeptLevel` 方法，增加过滤条件，只查询职位族为"研发族"的员工
2. **第二步**：在 Service 层查询对应部门的干部工号列表
3. **第三步**：合并员工列表和干部列表，并去重

### 3.2 详细修改内容

#### 3.2.1 方案选择

**重要说明**：`getEmployeeNumbersByDeptLevel` 方法在以下地方也被使用：
- `ExpertCertStatisticsService.getMaturityCertStatistics()`（第 1429 行）- 也是全员查询接口

**方案 A（推荐）**：创建新方法 `getEmployeeNumbersByDeptLevelWithJobFamilyFilter`，专门用于需要过滤职位族的场景

**方案 B**：直接修改 `getEmployeeNumbersByDeptLevel` 方法，但需要同步修改 `getMaturityCertStatistics` 方法

**建议采用方案 A**，保持向后兼容性。

#### 3.2.2 修改 EmployeeMapper.xml（方案 A - 推荐）

**文件路径**：`src/main/resources/mapper/EmployeeMapper.xml`

**新增方法**：`getEmployeeNumbersByDeptLevelWithJobFamilyFilter`

**方法说明**：在原有方法基础上增加职位族过滤条件

**新增的 SQL**：

```xml
<!-- 根据部门层级和部门ID列表查询员工工号列表（过滤职位族为"研发族"） -->
<select id="getEmployeeNumbersByDeptLevelWithJobFamilyFilter" resultType="java.lang.String">
    SELECT DISTINCT employee_number
    FROM t_employee_sync
    WHERE period_id = 20251126
    <choose>
        <when test="deptLevel == 1">
            AND firstdeptcode IN
            <foreach collection="deptIds" item="deptId" open="(" separator="," close=")">
                #{deptId}
            </foreach>
        </when>
        <when test="deptLevel == 2">
            AND seconddeptcode IN
            <foreach collection="deptIds" item="deptId" open="(" separator="," close=")">
                #{deptId}
            </foreach>
        </when>
        <when test="deptLevel == 3">
            AND thirddeptcode IN
            <foreach collection="deptIds" item="deptId" open="(" separator="," close=")">
                #{deptId}
            </foreach>
        </when>
        <when test="deptLevel == 4">
            AND fourthdeptcode IN
            <foreach collection="deptIds" item="deptId" open="(" separator="," close=")">
                #{deptId}
            </foreach>
        </when>
        <when test="deptLevel == 5">
            AND fifthdeptcode IN
            <foreach collection="deptIds" item="deptId" open="(" separator="," close=")">
                #{deptId}
            </foreach>
        </when>
        <when test="deptLevel == 6">
            AND sixthdeptcode IN
            <foreach collection="deptIds" item="deptId" open="(" separator="," close=")">
                #{deptId}
            </foreach>
        </when>
        <when test="deptLevel == 7">
            <!-- 如果deptLevel为7，已经是最高层级，无法再查询下一层 -->
            AND 1 = 0
        </when>
    </choose>
    AND employee_number IS NOT NULL
    AND employee_number != ''
    <!-- 新增：过滤职位族为"研发族"的员工 -->
    AND (
        -- 如果 job_category 包含 '-'，取第一个 '-' 前面的部分
        (job_category LIKE '%-%' AND SUBSTRING_INDEX(job_category, '-', 1) = '研发族')
        -- 如果 job_category 不包含 '-'，直接判断是否等于 '研发族'
        OR (job_category NOT LIKE '%-%' AND job_category = '研发族')
    )
</select>
```

**说明**：
- 使用 `SUBSTRING_INDEX(job_category, '-', 1)` 提取第一个 `-` 前面的部分
- 处理两种情况：
  - 包含 `-` 的情况：如 `"研发族-研究类-算法"` → 提取 `"研发族"`
  - 不包含 `-` 的情况：如 `"研发族"` → 直接判断

#### 3.2.3 修改 EmployeeMapper.java

**文件路径**：`src/main/java/com/huawei/aitransform/mapper/EmployeeMapper.java`

**新增方法**：

```java
/**
 * 根据部门层级和部门ID列表查询员工工号列表（过滤职位族为"研发族"）
 * @param deptLevel 部门层级
 * @param deptIds 部门ID列表
 * @return 员工工号列表
 */
List<String> getEmployeeNumbersByDeptLevelWithJobFamilyFilter(
    @Param("deptLevel") Integer deptLevel,
    @Param("deptIds") List<String> deptIds);
```

#### 3.2.4 修改 ExpertCertStatisticsService.java

**文件路径**：`src/main/java/com/huawei/aitransform/service/ExpertCertStatisticsService.java`

**修改方法**：`getEmployeeCertStatistics()` 方法中的全员处理流程（`personType=0`）

**修改位置**：第 358-361 行附近

**修改前**：

```java
// 4.1 查询该部门下的员工工号列表
List<String> deptIdList = new ArrayList<>();
deptIdList.add(dept.getDeptCode());
List<String> employeeNumbers = employeeMapper.getEmployeeNumbersByDeptLevel(queryLevel, deptIdList);

int deptTotalCount = (employeeNumbers != null) ? employeeNumbers.size() : 0;
```

**修改后**：

```java
// 4.1 查询该部门下的员工工号列表（只包含研发族）
List<String> deptIdList = new ArrayList<>();
deptIdList.add(dept.getDeptCode());
List<String> employeeNumbers = employeeMapper.getEmployeeNumbersByDeptLevelWithJobFamilyFilter(queryLevel, deptIdList);

// 4.1.1 查询该部门下的干部工号列表（包含所有干部，不限制职位族）
List<String> cadreEmployeeNumbers = cadreMapper.getCadreEmployeeNumbersByDeptCodes(deptIdList);

// 4.1.2 合并员工列表和干部列表，并去重
Set<String> allEmployeeNumbersSet = new HashSet<>();
if (employeeNumbers != null && !employeeNumbers.isEmpty()) {
    allEmployeeNumbersSet.addAll(employeeNumbers);
}
if (cadreEmployeeNumbers != null && !cadreEmployeeNumbers.isEmpty()) {
    allEmployeeNumbersSet.addAll(cadreEmployeeNumbers);
}
List<String> allEmployeeNumbers = new ArrayList<>(allEmployeeNumbersSet);

int deptTotalCount = (allEmployeeNumbers != null) ? allEmployeeNumbers.size() : 0;
```

**后续代码修改**：

将后续所有使用 `employeeNumbers` 的地方改为使用 `allEmployeeNumbers`：

- 第 367-370 行：查询认证人数
- 第 374-377 行：查询任职人数

**修改后的完整代码段**：

```java
// 4.1 查询该部门下的员工工号列表（只包含研发族）
List<String> deptIdList = new ArrayList<>();
deptIdList.add(dept.getDeptCode());
List<String> employeeNumbers = employeeMapper.getEmployeeNumbersByDeptLevel(queryLevel, deptIdList);

// 4.1.1 查询该部门下的干部工号列表（包含所有干部，不限制职位族）
List<String> cadreEmployeeNumbers = cadreMapper.getCadreEmployeeNumbersByDeptCodes(deptIdList);

// 4.1.2 合并员工列表和干部列表，并去重
Set<String> allEmployeeNumbersSet = new HashSet<>();
if (employeeNumbers != null && !employeeNumbers.isEmpty()) {
    allEmployeeNumbersSet.addAll(employeeNumbers);
}
if (cadreEmployeeNumbers != null && !cadreEmployeeNumbers.isEmpty()) {
    allEmployeeNumbersSet.addAll(cadreEmployeeNumbers);
}
List<String> allEmployeeNumbers = new ArrayList<>(allEmployeeNumbersSet);

int deptTotalCount = (allEmployeeNumbers != null) ? allEmployeeNumbers.size() : 0;

// 4.2 查询该部门已通过认证的员工工号列表
int deptCertifiedCount = 0;
if (allEmployeeNumbers != null && !allEmployeeNumbers.isEmpty()) {
    List<String> certifiedNumbers = getCertifiedEmployeeNumbers(allEmployeeNumbers);
    deptCertifiedCount = (certifiedNumbers != null) ? certifiedNumbers.size() : 0;
}

// 4.3 查询该部门已获得任职的员工工号列表
int deptQualifiedCount = 0;
if (allEmployeeNumbers != null && !allEmployeeNumbers.isEmpty()) {
    List<String> qualifiedNumbers = getQualifiedEmployeeNumbers(allEmployeeNumbers);
    deptQualifiedCount = (qualifiedNumbers != null) ? qualifiedNumbers.size() : 0;
}
```

#### 3.2.5 确认必要的导入和注入

**文件路径**：`src/main/java/com/huawei/aitransform/service/ExpertCertStatisticsService.java`

**已存在的导入**（无需添加）：
- `import java.util.HashSet;`（第 46 行）
- `import java.util.Set;`（第 49 行）

**已存在的注入**（无需添加）：
- `@Autowired private CadreMapper cadreMapper;`（第 71 行）

**说明**：以上导入和注入已存在，无需额外修改。

## 四、修改影响范围

### 4.1 影响的方法

1. **EmployeeMapper.getEmployeeNumbersByDeptLevelWithJobFamilyFilter()**（新增方法）
   - 新方法，不影响现有功能
   - 专门用于需要过滤职位族的场景

2. **ExpertCertStatisticsService.getEmployeeCertStatistics()**
   - 仅影响 `personType=0`（全员）的查询逻辑
   - 不影响 `personType=1`（干部）和 `personType=2`（专家）的查询逻辑
   - 不影响 `getMaturityCertStatistics()` 等其他方法（如果采用方案 A）

### 4.2 测试建议

1. **单元测试**：
   - 测试 `getEmployeeNumbersByDeptLevel` 方法是否正确过滤职位族
   - 测试合并员工和干部列表的逻辑

2. **集成测试**：
   - 测试 `employee-cert-statistics` 接口，验证：
     - 只统计研发族员工
     - 包含所有干部（无论职位族）
     - 去重逻辑正确
     - 认证人数和任职人数统计正确
   - 测试 `getMaturityCertStatistics` 接口，确认未受影响（如果采用方案 A）

3. **边界情况测试**：
   - `job_category` 为 `NULL` 的情况
   - `job_category` 不包含 `-` 的情况
   - `job_category` 包含多个 `-` 的情况
   - 员工和干部工号重复的情况

## 五、SQL 逻辑说明

### 5.1 职位族过滤逻辑

```sql
AND (
    -- 如果 job_category 包含 '-'，取第一个 '-' 前面的部分
    (job_category LIKE '%-%' AND SUBSTRING_INDEX(job_category, '-', 1) = '研发族')
    -- 如果 job_category 不包含 '-'，直接判断是否等于 '研发族'
    OR (job_category NOT LIKE '%-%' AND job_category = '研发族')
)
```

**示例**：
- `"研发族-研究类-算法"` → `SUBSTRING_INDEX(..., '-', 1)` = `"研发族"` → ✅ 通过
- `"市场族-销售类"` → `SUBSTRING_INDEX(..., '-', 1)` = `"市场族"` → ❌ 不通过
- `"研发族"` → 直接判断 = `"研发族"` → ✅ 通过

### 5.2 干部查询逻辑

干部通过 `t_cadre.mini_departname_id` 字段关联部门，不限制职位族，所有干部都会被包含。

## 六、注意事项

1. **性能考虑**：
   - 合并两个列表时使用 `HashSet` 去重，时间复杂度为 O(n)
   - 如果数据量很大，需要注意内存使用

2. **数据一致性**：
   - 确保 `t_cadre.mini_departname_id` 与 `t_employee_sync` 中的部门编码字段对应关系正确
   - 确保 `t_cadre.account` 与 `t_employee_sync.employee_number` 格式一致（都是无首字母工号）

3. **向后兼容性**：
   - 采用方案 A（新增方法）可以保持完全的向后兼容性
   - 原有的 `getEmployeeNumbersByDeptLevel` 方法保持不变，不影响其他功能

## 七、修改清单

- [ ] 在 `EmployeeMapper.xml` 中新增 `getEmployeeNumbersByDeptLevelWithJobFamilyFilter` 方法
- [ ] 在 `EmployeeMapper.java` 中新增 `getEmployeeNumbersByDeptLevelWithJobFamilyFilter` 方法声明
- [ ] 修改 `ExpertCertStatisticsService.java` 中的 `getEmployeeCertStatistics` 方法：
  - [ ] 将 `getEmployeeNumbersByDeptLevel` 改为 `getEmployeeNumbersByDeptLevelWithJobFamilyFilter`
  - [ ] 增加干部查询逻辑
  - [ ] 增加合并和去重逻辑
- [ ] 确认必要的导入和注入已存在（`HashSet`、`Set`、`CadreMapper`）
- [ ] 编写/更新单元测试
- [ ] 进行集成测试验证：
  - [ ] 测试 `employee-cert-statistics` 接口
  - [ ] 测试 `getMaturityCertStatistics` 接口（确认未受影响）

## 八、回滚方案

如果修改后出现问题，可以：

1. **回滚 SQL 修改**：移除 `EmployeeMapper.xml` 中新增的职位族过滤条件
2. **回滚 Service 修改**：恢复使用 `employeeNumbers` 而不是 `allEmployeeNumbers`
3. **保留干部查询**：如果只保留干部查询逻辑，可以只回滚 SQL 修改部分

