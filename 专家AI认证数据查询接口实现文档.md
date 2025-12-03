# 专家AI认证数据查询接口实现文档

## 1. 接口概述

### 1.1 接口名称
查询专家AI认证数据接口

### 1.2 接口路径
`GET /expert-cert-statistics/expert-ai-cert-statistics`

### 1.3 接口描述
根据部门ID查询该部门下所有L2/L3专家的AI认证数据，包括按成熟度（L2/L3）和职位类进行统计，返回基线人数、已完成AI认证人数、AI认证人数占比等统计信息。

### 1.4 参考接口
- 干部认证数据接口：`GET /expert-cert-statistics/cadre-cert-statistics/by-maturity-and-job-category`
- 实现方法：`ExpertCertStatisticsService.getCadreMaturityJobCategoryCertStatistics(String deptCode)`
- 响应结构：`CadreMaturityJobCategoryCertStatisticsResponseVO`

---

## 2. 接口参数

### 2.1 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| deptCode | String | 是 | 部门ID（部门编码），当值为"0"时，自动赋值为"云核心网产品线"部门ID（031562） |

### 2.2 参数处理逻辑

```java
// 当deptCode为"0"时，使用云核心网产品线部门ID
if ("0".equals(deptCode)) {
    deptCode = DepartmentConstants.CLOUD_CORE_NETWORK_DEPT_CODE; // "031562"
}
```

---

## 3. 响应数据结构

### 3.1 响应VO结构

参考`CadreMaturityJobCategoryCertStatisticsResponseVO`，创建以下VO类：

#### 3.1.1 ExpertAiCertStatisticsResponseVO（专家AI认证统计响应VO）

```java
public class ExpertAiCertStatisticsResponseVO implements Serializable {
    /**
     * 部门ID（部门编码）
     */
    private String deptCode;
    
    /**
     * 部门名称
     */
    private String deptName;
    
    /**
     * 各成熟度统计列表（L2、L3）
     */
    private List<ExpertMaturityCertStatisticsVO> maturityStatistics;
    
    /**
     * 总计统计（L2+L3总计，不包含职位类明细）
     */
    private ExpertMaturityCertStatisticsVO totalStatistics;
}
```

#### 3.1.2 ExpertMaturityCertStatisticsVO（专家成熟度认证统计VO）

```java
public class ExpertMaturityCertStatisticsVO implements Serializable {
    /**
     * AI成熟度等级（L2/L3）
     */
    private String maturityLevel;
    
    /**
     * 基线人数（该成熟度下的专家总人数）
     */
    private Integer baselineCount;
    
    /**
     * 已完成AI认证人数（该成熟度下已通过华为研究类能力认证的专家人数）
     */
    private Integer certifiedCount;
    
    /**
     * AI认证人数占比（百分比，保留4位小数）
     */
    private BigDecimal certRate;
    
    /**
     * 该成熟度下各职位类的统计列表
     */
    private List<ExpertJobCategoryCertStatisticsVO> jobCategoryStatistics;
}
```

#### 3.1.3 ExpertJobCategoryCertStatisticsVO（专家职位类认证统计VO）

```java
public class ExpertJobCategoryCertStatisticsVO implements Serializable {
    /**
     * 职位类名称（从job_category字段的中间字段提取）
     */
    private String jobCategory;
    
    /**
     * 基线人数（该职位类下的专家总人数）
     */
    private Integer baselineCount;
    
    /**
     * 已完成AI认证人数（该职位类下已通过华为研究类能力认证的专家人数）
     */
    private Integer certifiedCount;
    
    /**
     * AI认证人数占比（百分比，保留4位小数）
     */
    private BigDecimal certRate;
}
```

### 3.2 响应示例

```json
{
    "code": 200,
    "message": "查询成功",
    "data": {
        "deptCode": "031562",
        "deptName": "云核心网产品线",
        "maturityStatistics": [
            {
                "maturityLevel": "L2",
                "baselineCount": 50,
                "certifiedCount": 30,
                "certRate": 60.0000,
                "jobCategoryStatistics": [
                    {
                        "jobCategory": "软件类",
                        "baselineCount": 30,
                        "certifiedCount": 20,
                        "certRate": 66.6667
                    },
                    {
                        "jobCategory": "系统类",
                        "baselineCount": 20,
                        "certifiedCount": 10,
                        "certRate": 50.0000
                    }
                ]
            },
            {
                "maturityLevel": "L3",
                "baselineCount": 80,
                "certifiedCount": 50,
                "certRate": 62.5000,
                "jobCategoryStatistics": [
                    {
                        "jobCategory": "软件类",
                        "baselineCount": 40,
                        "certifiedCount": 25,
                        "certRate": 62.5000
                    },
                    {
                        "jobCategory": "研究类",
                        "baselineCount": 40,
                        "certifiedCount": 25,
                        "certRate": 62.5000
                    }
                ]
            }
        ],
        "totalStatistics": {
            "maturityLevel": "总计",
            "baselineCount": 130,
            "certifiedCount": 80,
            "certRate": 61.5385,
            "jobCategoryStatistics": null
        }
    }
}
```

---

## 4. 查询逻辑

### 4.1 整体流程

1. **参数处理**：如果deptCode为"0"，赋值为云核心网产品线部门ID
2. **查询部门信息**：根据部门ID查询部门信息，验证部门是否存在
3. **查询专家数据**：从`t_expert`表中查询所有L2/L3的专家（通过`position_ai_maturity`字段过滤）
4. **关联员工信息**：通过工号（`account`字段）关联`t_employee_sync`表，获取`job_category`字段
   - 注意`period_id`过滤（固定为20251126）
   - **重要**：根据部门层级使用对应的部门字段进行过滤
     - 如果提供的部门ID是三层部门（deptLevel=3），使用`thirddeptcode`字段过滤
     - 如果提供的部门ID是四层部门（deptLevel=4），使用`fourthdeptcode`字段过滤
     - 确保只查询属于对应层级部门的专家信息
5. **提取职位类**：从`job_category`字段中提取职位类（中间字段，格式：职位族-职位类-职位子类）
6. **查询认证状态**：通过工号查询是否完成认证（复用`getCertifiedEmployeeNumbers`方法）
7. **分组统计**：按成熟度（L2/L3）和职位类进行分组统计
8. **计算占比**：计算各维度的AI认证人数占比
9. **构建响应**：构建树形结构的响应数据

### 4.2 详细步骤

#### 步骤1：参数处理和部门信息查询

```java
// 1. 参数处理
String actualDeptCode = deptCode;
if ("0".equals(deptCode)) {
    actualDeptCode = DepartmentConstants.CLOUD_CORE_NETWORK_DEPT_CODE; // "031562"
}

// 2. 查询部门信息
DepartmentInfoVO deptInfo = departmentInfoMapper.getDepartmentByCode(actualDeptCode);
if (deptInfo == null) {
    throw new IllegalArgumentException("部门不存在：" + actualDeptCode);
}

String deptName = deptInfo.getDeptName();
// 获取部门层级，用于后续的部门过滤
String deptLevelStr = deptInfo.getDeptLevel();
Integer deptLevel = Integer.parseInt(deptLevelStr); // 转换为Integer类型，用于SQL判断
```

#### 步骤2：查询专家数据

**Mapper方法**：需要在`ExpertMapper`中新增方法

```java
/**
 * 根据部门编码和部门层级查询L2/L3专家信息（关联t_employee_sync表获取job_category）
 * @param deptCode 部门编码（单个部门ID）
 * @param deptLevel 部门层级（1-7），用于确定使用哪个部门字段进行过滤
 * @return 专家信息列表（包含工号、AI成熟度、职位类）
 */
List<ExpertInfoVO> getExpertInfoByDeptCode(
    @Param("deptCode") String deptCode,
    @Param("deptLevel") Integer deptLevel);
```

**SQL查询**（在`ExpertMapper.xml`中）：

```sql
<select id="getExpertInfoByDeptCode" resultType="com.huawei.aitransform.entity.ExpertInfoVO">
    SELECT DISTINCT
        exp.account AS employeeNumber,
        exp.position_ai_maturity AS aiMaturity,
        e.job_category AS jobCategory
    FROM t_expert exp
    INNER JOIN t_employee_sync e ON (exp.account = e.employee_number)
    WHERE e.period_id = 20251126
    AND exp.position_ai_maturity IN ('L2', 'L3')
    AND exp.account IS NOT NULL
    AND exp.account != ''
    AND e.employee_number IS NOT NULL
    AND e.employee_number != ''
    <if test="deptCode != null and deptCode != ''">
        <choose>
            <!-- 根据部门层级使用对应的部门字段进行过滤 -->
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
            <when test="deptLevel == 7">
                <!-- 如果deptLevel为7，已经是最高层级，无法再查询 -->
                AND 1 = 0
            </when>
        </choose>
    </if>
</select>
```

**注意**：
- **部门层级过滤**：根据提供的部门ID对应的部门层级（`deptLevel`），使用对应的部门字段进行过滤
  - 如果部门层级为3，使用`thirddeptcode`字段过滤，只查询属于该三层部门的专家
  - 如果部门层级为4，使用`fourthdeptcode`字段过滤，只查询属于该四层部门的专家
  - 以此类推，确保只查询对应层级部门的专家信息
- `period_id`固定为`20251126`（参考现有代码）
- 只查询`position_ai_maturity`为`L2`或`L3`的专家
- `t_employee_sync`表中的部门字段为：`firstdeptcode`、`seconddeptcode`、`thirddeptcode`、`fourthdeptcode`、`fifthdeptcode`、`sixthdeptcode`

#### 步骤4：提取职位类（从job_category字段中提取职位类）

从`job_category`字段中提取职位类（中间字段），格式：`职位族-职位类-职位子类`

```java
/**
 * 从职位族字符串中提取职位类
 * 格式：职位族-职位类-职位子类，需要提取中间的职位类
 * @param jobCategory 职位族字符串
 * @return 职位类，如果格式不正确则返回"未知"
 */
private String extractJobCategory(String jobCategory) {
    if (jobCategory == null || jobCategory.trim().isEmpty()) {
        return "未知";
    }
    
    String[] parts = jobCategory.split("-");
    if (parts.length >= 2) {
        // 提取中间的职位类（第二个部分）
        return parts[1].trim();
    } else if (parts.length == 1) {
        // 如果只有一个部分，直接返回
        return parts[0].trim();
    } else {
        return "未知";
    }
}
```

#### 步骤3：查询专家数据（调用Mapper方法）

```java
// 调用Mapper方法查询专家数据，传入单个部门编码和部门层级
List<ExpertInfoVO> expertList = expertMapper.getExpertInfoByDeptCode(actualDeptCode, deptLevel);

if (expertList == null || expertList.isEmpty()) {
    // 如果没有专家数据，返回空统计
    ExpertAiCertStatisticsResponseVO response = new ExpertAiCertStatisticsResponseVO();
    response.setDeptCode(deptCode);
    response.setDeptName(deptName);
    response.setMaturityStatistics(new ArrayList<>());
    ExpertMaturityCertStatisticsVO total = new ExpertMaturityCertStatisticsVO();
    total.setMaturityLevel("总计");
    total.setBaselineCount(0);
    total.setCertifiedCount(0);
    total.setCertRate(BigDecimal.ZERO);
    total.setJobCategoryStatistics(null);
    response.setTotalStatistics(total);
    return response;
}
```

#### 步骤5：查询认证状态

复用现有的`getCertifiedEmployeeNumbers`方法：

```java
// 提取所有专家工号
List<String> allEmployeeNumbers = expertList.stream()
        .map(ExpertInfoVO::getEmployeeNumber)
        .filter(num -> num != null && !num.trim().isEmpty())
        .distinct()
        .collect(Collectors.toList());

// 查询已通过认证的专家工号列表
List<String> certifiedNumbers = getCertifiedEmployeeNumbers(allEmployeeNumbers);
Set<String> certifiedSet = new HashSet<>(certifiedNumbers != null ? certifiedNumbers : new ArrayList<>());
```

#### 步骤6：分组统计

按成熟度（L2/L3）和职位类进行分组统计：

```java
// 结构：成熟度 -> 职位类 -> 统计信息
Map<String, Map<String, ExpertJobCategoryCertStatisticsVO>> maturityJobCategoryMap = new HashMap<>();

// 用于统计L2和L3的总基数（包含所有职位类）
Map<String, Integer> maturityTotalBaselineMap = new HashMap<>();
Map<String, Integer> maturityTotalCertifiedMap = new HashMap<>();

int totalBaselineCount = 0;
int totalCertifiedCount = 0;

for (ExpertInfoVO expert : expertList) {
    String aiMaturity = expert.getAiMaturity();
    if (aiMaturity == null || !aiMaturity.matches("L[23]")) {
        continue; // 只处理L2和L3
    }
    
    // 提取职位类
    String jobCategory = extractJobCategory(expert.getJobCategory());
    
    String employeeNumber = expert.getEmployeeNumber();
    
    // 统计成熟度的总基数（所有职位类）
    maturityTotalBaselineMap.put(aiMaturity, 
        maturityTotalBaselineMap.getOrDefault(aiMaturity, 0) + 1);
    totalBaselineCount++;
    
    // 统计成熟度的总认证人数（所有职位类）
    if (employeeNumber != null && certifiedSet.contains(employeeNumber)) {
        maturityTotalCertifiedMap.put(aiMaturity, 
            maturityTotalCertifiedMap.getOrDefault(aiMaturity, 0) + 1);
        totalCertifiedCount++;
    }
    
    // 获取或创建成熟度对应的职位类Map
    Map<String, ExpertJobCategoryCertStatisticsVO> jobCategoryMap = 
        maturityJobCategoryMap.getOrDefault(aiMaturity, new HashMap<>());
    
    // 获取或创建职位类统计对象
    ExpertJobCategoryCertStatisticsVO jobCategoryStat = 
        jobCategoryMap.getOrDefault(jobCategory, new ExpertJobCategoryCertStatisticsVO());
    jobCategoryStat.setJobCategory(jobCategory);
    
    // 累加基数人数
    if (jobCategoryStat.getBaselineCount() == null) {
        jobCategoryStat.setBaselineCount(0);
    }
    jobCategoryStat.setBaselineCount(jobCategoryStat.getBaselineCount() + 1);
    
    // 检查是否已认证
    if (employeeNumber != null && certifiedSet.contains(employeeNumber)) {
        if (jobCategoryStat.getCertifiedCount() == null) {
            jobCategoryStat.setCertifiedCount(0);
        }
        jobCategoryStat.setCertifiedCount(jobCategoryStat.getCertifiedCount() + 1);
    }
    
    jobCategoryMap.put(jobCategory, jobCategoryStat);
    maturityJobCategoryMap.put(aiMaturity, jobCategoryMap);
}
```

#### 步骤7：计算占比并构建响应

```java
// 计算每个职位类的认证率，并构建成熟度统计对象
List<ExpertMaturityCertStatisticsVO> maturityStatistics = new ArrayList<>();

// 按L2、L3的顺序处理
for (String aiMaturity : new String[]{"L2", "L3"}) {
    Map<String, ExpertJobCategoryCertStatisticsVO> jobCategoryMap = 
        maturityJobCategoryMap.get(aiMaturity);
    if (jobCategoryMap == null) {
        jobCategoryMap = new HashMap<>();
    }
    
    // 创建成熟度统计对象
    ExpertMaturityCertStatisticsVO maturityStat = new ExpertMaturityCertStatisticsVO();
    maturityStat.setMaturityLevel(aiMaturity);
    
    // 使用所有职位类的总基数
    int maturityBaselineCount = maturityTotalBaselineMap.getOrDefault(aiMaturity, 0);
    int maturityCertifiedCount = maturityTotalCertifiedMap.getOrDefault(aiMaturity, 0);
    
    List<ExpertJobCategoryCertStatisticsVO> jobCategoryStatistics = new ArrayList<>();
    
    // 遍历该成熟度下的所有职位类
    for (ExpertJobCategoryCertStatisticsVO jobCategoryStat : jobCategoryMap.values()) {
        // 计算职位类认证率
        if (jobCategoryStat.getBaselineCount() != null && jobCategoryStat.getBaselineCount() > 0) {
            if (jobCategoryStat.getCertifiedCount() == null) {
                jobCategoryStat.setCertifiedCount(0);
            }
            BigDecimal certRate = new BigDecimal(jobCategoryStat.getCertifiedCount())
                    .divide(new BigDecimal(jobCategoryStat.getBaselineCount()), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(100));
            jobCategoryStat.setCertRate(certRate);
        } else {
            jobCategoryStat.setCertRate(BigDecimal.ZERO);
        }
        
        jobCategoryStatistics.add(jobCategoryStat);
    }
    
    // 设置成熟度统计数据
    maturityStat.setBaselineCount(maturityBaselineCount);
    maturityStat.setCertifiedCount(maturityCertifiedCount);
    maturityStat.setJobCategoryStatistics(jobCategoryStatistics);
    
    // 计算成熟度认证率
    if (maturityBaselineCount > 0) {
        BigDecimal certRate = new BigDecimal(maturityCertifiedCount)
                .divide(new BigDecimal(maturityBaselineCount), 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal(100));
        maturityStat.setCertRate(certRate);
    } else {
        maturityStat.setCertRate(BigDecimal.ZERO);
    }
    
    maturityStatistics.add(maturityStat);
}

// 计算总计统计
ExpertMaturityCertStatisticsVO totalStatistics = new ExpertMaturityCertStatisticsVO();
totalStatistics.setMaturityLevel("总计");
totalStatistics.setBaselineCount(totalBaselineCount);
totalStatistics.setCertifiedCount(totalCertifiedCount);
totalStatistics.setJobCategoryStatistics(null);

// 计算总计认证率
if (totalBaselineCount > 0) {
    BigDecimal totalCertRate = new BigDecimal(totalCertifiedCount)
            .divide(new BigDecimal(totalBaselineCount), 4, RoundingMode.HALF_UP)
            .multiply(new BigDecimal(100));
    totalStatistics.setCertRate(totalCertRate);
} else {
    totalStatistics.setCertRate(BigDecimal.ZERO);
}

// 构建返回结果
ExpertAiCertStatisticsResponseVO response = new ExpertAiCertStatisticsResponseVO();
response.setDeptCode(deptCode);
response.setDeptName(deptName);
response.setMaturityStatistics(maturityStatistics);
response.setTotalStatistics(totalStatistics);

return response;
```

---

## 5. 需要创建的文件和类

### 5.1 实体类（VO）

1. **ExpertInfoVO.java**
   - 位置：`src/main/java/com/huawei/aitransform/entity/ExpertInfoVO.java`
   - 字段：`employeeNumber`（工号）、`aiMaturity`（AI成熟度）、`jobCategory`（职位族）

2. **ExpertAiCertStatisticsResponseVO.java**
   - 位置：`src/main/java/com/huawei/aitransform/entity/ExpertAiCertStatisticsResponseVO.java`
   - 参考：`CadreMaturityJobCategoryCertStatisticsResponseVO`

3. **ExpertMaturityCertStatisticsVO.java**
   - 位置：`src/main/java/com/huawei/aitransform/entity/ExpertMaturityCertStatisticsVO.java`
   - 参考：`CadreMaturityCertStatisticsVO`（去掉`subject2PassCount`和`subject2PassRate`字段）

4. **ExpertJobCategoryCertStatisticsVO.java**
   - 位置：`src/main/java/com/huawei/aitransform/entity/ExpertJobCategoryCertStatisticsVO.java`
   - 参考：`CadreJobCategoryCertStatisticsVO`（去掉`subject2PassCount`和`subject2PassRate`字段）

### 5.2 Mapper接口

1. **ExpertMapper.java**
   - 位置：`src/main/java/com/huawei/aitransform/mapper/ExpertMapper.java`
   - 新增方法：`getExpertInfoByDeptCode`

2. **ExpertMapper.xml**
   - 位置：`src/main/resources/mapper/ExpertMapper.xml`
   - 新增SQL：`getExpertInfoByDeptCode`

### 5.3 Service层

1. **ExpertCertStatisticsService.java**
   - 位置：`src/main/java/com/huawei/aitransform/service/ExpertCertStatisticsService.java`
   - 新增方法：`getExpertAiCertStatistics(String deptCode)`

### 5.4 Controller层

1. **ExpertCertStatisticsController.java**
   - 位置：`src/main/java/com/huawei/aitransform/controller/ExpertCertStatisticsController.java`
   - 新增接口：`GET /expert-cert-statistics/expert-ai-cert-statistics`

---

## 6. 复用现有接口和方法

### 6.1 复用的方法

1. **部门信息查询**
   - `DepartmentInfoMapper.getDepartmentByCode(String deptCode)`
   - `DepartmentInfoMapper.getAllSubDepartments(String deptCode)`

2. **认证状态查询**
   - `ExpertCertStatisticsService.getCertifiedEmployeeNumbers(List<String> employeeNumbers)`
   - 该方法内部调用`ExpertCertStatisticsMapper.getCertifiedEmployeeNumbers`

3. **职位类提取方法**
   - 参考`ExpertCertStatisticsService.extractCompetenceCategory`方法
   - 创建类似的`extractJobCategory`方法

### 6.2 参考的实现

1. **干部认证数据查询**
   - 方法：`ExpertCertStatisticsService.getCadreMaturityJobCategoryCertStatistics`
   - 参考其分组统计、占比计算、响应构建逻辑

---

## 7. 注意事项

### 7.1 数据过滤

1. **period_id过滤**：关联`t_employee_sync`表时，必须使用`period_id = 20251126`进行过滤
2. **成熟度过滤**：只查询`position_ai_maturity`为`L2`或`L3`的专家
3. **工号非空**：确保`account`字段和`employee_number`字段都不为空
4. **部门层级过滤（重要）**：
   - 根据提供的部门ID对应的部门层级（`deptLevel`），使用对应的部门字段进行过滤
   - 如果部门层级为3，使用`t_employee_sync.thirddeptcode`字段过滤，只查询属于该三层部门的专家
   - 如果部门层级为4，使用`t_employee_sync.fourthdeptcode`字段过滤，只查询属于该四层部门的专家
   - 以此类推，确保只查询对应层级部门的专家信息，避免跨层级查询导致的数据不准确

### 7.2 职位类提取

1. **格式**：`job_category`字段格式为`职位族-职位类-职位子类`，需要提取中间的`职位类`
2. **异常处理**：如果格式不正确，返回"未知"

### 7.3 部门匹配

1. **多层级匹配**：需要匹配`t_employee_sync`表中的多个部门层级字段（`firstdept_code`、`seconddept_code`等）
2. **子部门包含**：需要包含本部门及其所有子部门的专家

### 7.4 数据准确性

1. **基数统计**：成熟度的基线人数应包含该成熟度下所有职位类的专家总数
2. **认证统计**：认证人数必须小于等于基线人数
3. **占比计算**：使用`BigDecimal`进行精确计算，保留4位小数，使用`RoundingMode.HALF_UP`舍入模式

### 7.5 空数据处理

1. **无数据情况**：如果没有专家数据，返回空列表和0值统计，不要返回null
2. **空值处理**：对于可能为null的字段，使用默认值（如0、BigDecimal.ZERO、"未知"）

---

## 8. 测试建议

### 8.1 测试用例

1. **正常情况**
   - 测试部门ID为正常值
   - 测试部门ID为"0"（应自动转换为云核心网产品线）
   - 验证返回的统计数据是否正确

2. **边界情况**
   - 测试部门不存在的情况
   - 测试部门下没有专家的情况
   - 测试部门下只有L2或只有L3的情况
   - 测试没有认证人员的情况

3. **数据准确性**
   - 验证基数人数是否正确
   - 验证认证人数是否正确
   - 验证占比计算是否正确
   - 验证职位类提取是否正确

### 8.2 调试接口

建议新增一个调试接口，用于查看原始专家数据：

```java
@GetMapping("/expert-ai-cert-statistics/debug")
public ResponseEntity<Result<Object>> getExpertAiCertDebugInfo(
        @RequestParam(value = "deptCode", required = true) String deptCode) {
    // 返回原始专家数据列表，包含工号、AI成熟度、职位类、是否认证等信息
}
```

---

## 9. 版本信息

- 接口版本：v1.0
- 创建日期：2024-12-19
- 最后更新：2024-12-19

---

## 10. 附录

### 10.1 相关表结构

#### t_expert表
- `id`：主键ID
- `orig_position_name`：原岗位名称
- `orig_position_grade`：原岗位职级
- `is_on_job`：原在岗情况
- `account`：原在岗人工号（用于关联）
- `name`：在岗人姓名
- `position_ai_maturity`：AI岗位成熟度（L2/L3）

#### t_employee_sync表
- `employee_number`：员工工号（用于关联t_expert.account）
- `job_category`：职位族（格式：职位族-职位类-职位子类）
- `period_id`：周期ID（固定为20251126）
- `firstdept_code`、`seconddept_code`等：部门编码字段

### 10.2 相关常量

```java
// DepartmentConstants.java
public static final String CLOUD_CORE_NETWORK_DEPT_CODE = "031562";
```

### 10.3 认证查询逻辑

认证查询通过`getCertifiedEmployeeNumbers`方法实现，查询`dwr_t_cert_record_t`表中：
- `cer_title`为以下之一：
  - `华为研究类能力认证（专业级，AI算法技术）`
  - `华为研究类能力认证（专业级，AI决策推理）`
  - `华为研究类能力认证（专业级，AI图像语言语义）`
- `status = 1`或`approved_status = 1`

