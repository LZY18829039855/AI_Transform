# 专家AI任职数据查询接口实现文档

## 1. 接口概述

### 1.1 接口名称
查询专家AI任职数据接口

### 1.2 接口路径
`GET /expert-cert-statistics/expert-ai-qualified-statistics`

### 1.3 接口描述
根据部门ID查询该部门下所有L2/L3专家的AI任职数据，包括按成熟度（L2/L3）和职位类进行统计，返回基线人数、已完成AI任职人数、AI任职人数占比等统计信息。

### 1.4 参考接口
- 干部任职数据接口：`GET /expert-cert-statistics/cadre-cert-statistics/by-maturity-and-job-category-qualified`
- 实现方法：`ExpertCertStatisticsService.getCadreMaturityJobCategoryQualifiedStatistics(String deptCode)`
- 响应结构：`CadreMaturityJobCategoryQualifiedStatisticsResponseVO`
- 专家认证数据接口：`GET /expert-cert-statistics/expert-ai-cert-statistics`
- 实现方法：`ExpertCertStatisticsService.getExpertAiCertStatistics(String deptCode)`

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

参考`CadreMaturityJobCategoryQualifiedStatisticsResponseVO`，创建以下VO类：

#### 3.1.1 ExpertAiQualifiedStatisticsResponseVO（专家AI任职统计响应VO）

```java
public class ExpertAiQualifiedStatisticsResponseVO implements Serializable {
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
    private List<ExpertMaturityQualifiedStatisticsVO> maturityStatistics;
    
    /**
     * 总计统计（L2+L3总计，不包含职位类明细）
     */
    private ExpertMaturityQualifiedStatisticsVO totalStatistics;
}
```

#### 3.1.2 ExpertMaturityQualifiedStatisticsVO（专家成熟度任职统计VO）

```java
public class ExpertMaturityQualifiedStatisticsVO implements Serializable {
    /**
     * AI成熟度等级（L2/L3）
     */
    private String maturityLevel;
    
    /**
     * 基线人数（该成熟度下的专家总人数）
     */
    private Integer baselineCount;
    
    /**
     * 已完成AI任职人数（该成熟度下已获得AI任职的专家人数）
     */
    private Integer qualifiedCount;
    
    /**
     * AI任职人数占比（百分比，保留4位小数）
     */
    private BigDecimal qualifiedRate;
    
    /**
     * 该成熟度下各职位类的统计列表
     */
    private List<ExpertJobCategoryQualifiedStatisticsVO> jobCategoryStatistics;
}
```

#### 3.1.3 ExpertJobCategoryQualifiedStatisticsVO（专家职位类任职统计VO）

```java
public class ExpertJobCategoryQualifiedStatisticsVO implements Serializable {
    /**
     * 职位类名称（从job_category字段的中间字段提取）
     */
    private String jobCategory;
    
    /**
     * 基线人数（该职位类下的专家总人数）
     */
    private Integer baselineCount;
    
    /**
     * 已完成AI任职人数（该职位类下已获得AI任职的专家人数）
     */
    private Integer qualifiedCount;
    
    /**
     * AI任职人数占比（百分比，保留4位小数）
     */
    private BigDecimal qualifiedRate;
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
                "qualifiedCount": 30,
                "qualifiedRate": 60.0000,
                "jobCategoryStatistics": [
                    {
                        "jobCategory": "软件类",
                        "baselineCount": 30,
                        "qualifiedCount": 20,
                        "qualifiedRate": 66.6667
                    },
                    {
                        "jobCategory": "系统类",
                        "baselineCount": 20,
                        "qualifiedCount": 10,
                        "qualifiedRate": 50.0000
                    }
                ]
            },
            {
                "maturityLevel": "L3",
                "baselineCount": 80,
                "qualifiedCount": 50,
                "qualifiedRate": 62.5000,
                "jobCategoryStatistics": [
                    {
                        "jobCategory": "软件类",
                        "baselineCount": 40,
                        "qualifiedCount": 25,
                        "qualifiedRate": 62.5000
                    },
                    {
                        "jobCategory": "研究类",
                        "baselineCount": 40,
                        "qualifiedCount": 25,
                        "qualifiedRate": 62.5000
                    }
                ]
            }
        ],
        "totalStatistics": {
            "maturityLevel": "总计",
            "baselineCount": 130,
            "qualifiedCount": 80,
            "qualifiedRate": 61.5385,
            "jobCategoryStatistics": null
        }
    }
}
```

---

## 4. 查询逻辑

### 4.1 整体流程

1. **参数处理**：如果deptCode为"0"，赋值为云核心网产品线部门ID
2. **查询部门信息**：根据部门ID查询部门信息，验证部门是否存在，获取部门层级
3. **查询专家数据**：从`t_expert`表中查询所有L2/L3的专家（通过`position_ai_maturity`字段过滤）
   - 通过工号（`account`字段）关联`t_employee_sync`表，获取`job_category`字段
   - 注意`period_id`过滤（固定为20251126）
   - **重要**：根据部门层级使用对应的部门字段进行过滤
     - 如果提供的部门ID是三层部门（deptLevel=3），使用`thirddeptcode`字段过滤
     - 如果提供的部门ID是四层部门（deptLevel=4），使用`fourthdeptcode`字段过滤
     - 确保只查询属于对应层级部门的专家信息
4. **提取职位类**：从`job_category`字段中提取职位类（中间字段，格式：职位族-职位类-职位子类）
5. **查询任职状态**：通过工号查询是否完成AI任职（复用`getQualifiedEmployeeNumbers`方法）
6. **分组统计**：按成熟度（L2/L3）和职位类进行分组统计
7. **计算占比**：计算各维度的AI任职人数占比
8. **构建响应**：构建树形结构的响应数据

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

#### 步骤2：查询专家数据（调用Mapper方法）

**复用已有的Mapper方法**：`ExpertMapper.getExpertInfoByDeptCode`

```java
// 调用Mapper方法查询专家数据，传入单个部门编码和部门层级
List<ExpertInfoVO> expertList = expertMapper.getExpertInfoByDeptCode(actualDeptCode, deptLevel);

if (expertList == null || expertList.isEmpty()) {
    // 如果没有专家数据，返回空统计
    ExpertAiQualifiedStatisticsResponseVO response = new ExpertAiQualifiedStatisticsResponseVO();
    response.setDeptCode(deptCode);
    response.setDeptName(deptName);
    response.setMaturityStatistics(new ArrayList<>());
    ExpertMaturityQualifiedStatisticsVO total = new ExpertMaturityQualifiedStatisticsVO();
    total.setMaturityLevel("总计");
    total.setBaselineCount(0);
    total.setQualifiedCount(0);
    total.setQualifiedRate(BigDecimal.ZERO);
    total.setJobCategoryStatistics(null);
    response.setTotalStatistics(total);
    return response;
}
```

**注意**：
- 复用专家认证接口中已创建的`ExpertMapper.getExpertInfoByDeptCode`方法
- 该方法已经实现了根据部门层级过滤的逻辑

#### 步骤3：提取职位类（从job_category字段中提取职位类）

复用专家认证接口中的`extractJobCategory`方法：

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

#### 步骤4：查询任职状态

复用现有的`getQualifiedEmployeeNumbers`方法：

```java
// 提取所有专家工号
List<String> allEmployeeNumbers = expertList.stream()
        .map(ExpertInfoVO::getEmployeeNumber)
        .filter(num -> num != null && !num.trim().isEmpty())
        .distinct()
        .collect(Collectors.toList());

// 查询已获得AI任职的专家工号列表
List<String> qualifiedNumbers = getQualifiedEmployeeNumbers(allEmployeeNumbers);
Set<String> qualifiedSet = new HashSet<>(qualifiedNumbers != null ? qualifiedNumbers : new ArrayList<>());
```

**注意**：
- `getQualifiedEmployeeNumbers`方法查询`t_qualifications`表中符合条件的记录
- 查询条件包括：
  - `direction_cn_name`为以下之一：
    - `数据科学与AI工程（ICT）`
    - `AI算法及应用（ICT）`
    - `AI软件工程与工具（ICT）`
    - `AI系统测试（ICT）`
  - `competence_from`和`competence_to`不为空
  - `CURDATE() BETWEEN competence_from AND competence_to`（当前日期在任职有效期内）

#### 步骤5：分组统计

按成熟度（L2/L3）和职位类进行分组统计：

```java
// 结构：成熟度 -> 职位类 -> 统计信息
Map<String, Map<String, ExpertJobCategoryQualifiedStatisticsVO>> maturityJobCategoryMap = new HashMap<>();

// 用于统计L2和L3的总基数（包含所有职位类）
Map<String, Integer> maturityTotalBaselineMap = new HashMap<>();
Map<String, Integer> maturityTotalQualifiedMap = new HashMap<>();

int totalBaselineCount = 0;
int totalQualifiedCount = 0;

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
    
    // 统计成熟度的总任职人数（所有职位类）
    if (employeeNumber != null && qualifiedSet.contains(employeeNumber)) {
        maturityTotalQualifiedMap.put(aiMaturity, 
            maturityTotalQualifiedMap.getOrDefault(aiMaturity, 0) + 1);
        totalQualifiedCount++;
    }
    
    // 获取或创建成熟度对应的职位类Map
    Map<String, ExpertJobCategoryQualifiedStatisticsVO> jobCategoryMap = 
        maturityJobCategoryMap.getOrDefault(aiMaturity, new HashMap<>());
    
    // 获取或创建职位类统计对象
    ExpertJobCategoryQualifiedStatisticsVO jobCategoryStat = 
        jobCategoryMap.getOrDefault(jobCategory, new ExpertJobCategoryQualifiedStatisticsVO());
    jobCategoryStat.setJobCategory(jobCategory);
    
    // 累加基数人数
    if (jobCategoryStat.getBaselineCount() == null) {
        jobCategoryStat.setBaselineCount(0);
    }
    jobCategoryStat.setBaselineCount(jobCategoryStat.getBaselineCount() + 1);
    
    // 检查是否已任职
    if (employeeNumber != null && qualifiedSet.contains(employeeNumber)) {
        if (jobCategoryStat.getQualifiedCount() == null) {
            jobCategoryStat.setQualifiedCount(0);
        }
        jobCategoryStat.setQualifiedCount(jobCategoryStat.getQualifiedCount() + 1);
    }
    
    jobCategoryMap.put(jobCategory, jobCategoryStat);
    maturityJobCategoryMap.put(aiMaturity, jobCategoryMap);
}
```

#### 步骤6：计算占比并构建响应

```java
// 计算每个职位类的任职率，并构建成熟度统计对象
List<ExpertMaturityQualifiedStatisticsVO> maturityStatistics = new ArrayList<>();

// 按L2、L3的顺序处理
for (String aiMaturity : new String[]{"L2", "L3"}) {
    Map<String, ExpertJobCategoryQualifiedStatisticsVO> jobCategoryMap = 
        maturityJobCategoryMap.get(aiMaturity);
    if (jobCategoryMap == null) {
        jobCategoryMap = new HashMap<>();
    }
    
    // 创建成熟度统计对象
    ExpertMaturityQualifiedStatisticsVO maturityStat = new ExpertMaturityQualifiedStatisticsVO();
    maturityStat.setMaturityLevel(aiMaturity);
    
    // 使用所有职位类的总基数
    int maturityBaselineCount = maturityTotalBaselineMap.getOrDefault(aiMaturity, 0);
    int maturityQualifiedCount = maturityTotalQualifiedMap.getOrDefault(aiMaturity, 0);
    
    List<ExpertJobCategoryQualifiedStatisticsVO> jobCategoryStatistics = new ArrayList<>();
    
    // 遍历该成熟度下的所有职位类
    for (ExpertJobCategoryQualifiedStatisticsVO jobCategoryStat : jobCategoryMap.values()) {
        // 计算职位类任职率
        if (jobCategoryStat.getBaselineCount() != null && jobCategoryStat.getBaselineCount() > 0) {
            if (jobCategoryStat.getQualifiedCount() == null) {
                jobCategoryStat.setQualifiedCount(0);
            }
            BigDecimal qualifiedRate = new BigDecimal(jobCategoryStat.getQualifiedCount())
                    .divide(new BigDecimal(jobCategoryStat.getBaselineCount()), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(100));
            jobCategoryStat.setQualifiedRate(qualifiedRate);
        } else {
            jobCategoryStat.setQualifiedRate(BigDecimal.ZERO);
        }
        
        jobCategoryStatistics.add(jobCategoryStat);
    }
    
    // 设置成熟度统计数据
    maturityStat.setBaselineCount(maturityBaselineCount);
    maturityStat.setQualifiedCount(maturityQualifiedCount);
    maturityStat.setJobCategoryStatistics(jobCategoryStatistics);
    
    // 计算成熟度任职率
    if (maturityBaselineCount > 0) {
        BigDecimal qualifiedRate = new BigDecimal(maturityQualifiedCount)
                .divide(new BigDecimal(maturityBaselineCount), 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal(100));
        maturityStat.setQualifiedRate(qualifiedRate);
    } else {
        maturityStat.setQualifiedRate(BigDecimal.ZERO);
    }
    
    maturityStatistics.add(maturityStat);
}

// 计算总计统计
ExpertMaturityQualifiedStatisticsVO totalStatistics = new ExpertMaturityQualifiedStatisticsVO();
totalStatistics.setMaturityLevel("总计");
totalStatistics.setBaselineCount(totalBaselineCount);
totalStatistics.setQualifiedCount(totalQualifiedCount);
totalStatistics.setJobCategoryStatistics(null);

// 计算总计任职率
if (totalBaselineCount > 0) {
    BigDecimal totalQualifiedRate = new BigDecimal(totalQualifiedCount)
            .divide(new BigDecimal(totalBaselineCount), 4, RoundingMode.HALF_UP)
            .multiply(new BigDecimal(100));
    totalStatistics.setQualifiedRate(totalQualifiedRate);
} else {
    totalStatistics.setQualifiedRate(BigDecimal.ZERO);
}

// 构建返回结果
ExpertAiQualifiedStatisticsResponseVO response = new ExpertAiQualifiedStatisticsResponseVO();
response.setDeptCode(deptCode);
response.setDeptName(deptName);
response.setMaturityStatistics(maturityStatistics);
response.setTotalStatistics(totalStatistics);

return response;
```

---

## 5. 需要创建的文件和类

### 5.1 实体类（VO）

1. **ExpertAiQualifiedStatisticsResponseVO.java**
   - 位置：`src/main/java/com/huawei/aitransform/entity/ExpertAiQualifiedStatisticsResponseVO.java`
   - 参考：`CadreMaturityJobCategoryQualifiedStatisticsResponseVO`

2. **ExpertMaturityQualifiedStatisticsVO.java**
   - 位置：`src/main/java/com/huawei/aitransform/entity/ExpertMaturityQualifiedStatisticsVO.java`
   - 参考：`CadreMaturityQualifiedStatisticsVO`（去掉`qualifiedByRequirementCount`和`qualifiedByRequirementRate`字段）

3. **ExpertJobCategoryQualifiedStatisticsVO.java**
   - 位置：`src/main/java/com/huawei/aitransform/entity/ExpertJobCategoryQualifiedStatisticsVO.java`
   - 参考：`CadreJobCategoryQualifiedStatisticsVO`（去掉`qualifiedByRequirementCount`和`qualifiedByRequirementRate`字段）

### 5.2 Mapper接口

**无需新增**：复用`ExpertMapper.getExpertInfoByDeptCode`方法

### 5.3 Service层

1. **ExpertCertStatisticsService.java**
   - 位置：`src/main/java/com/huawei/aitransform/service/ExpertCertStatisticsService.java`
   - 新增方法：`getExpertAiQualifiedStatistics(String deptCode)`
   - 复用方法：`extractJobCategory(String jobCategory)`（已存在）

### 5.4 Controller层

1. **ExpertCertStatisticsController.java**
   - 位置：`src/main/java/com/huawei/aitransform/controller/ExpertCertStatisticsController.java`
   - 新增接口：`GET /expert-cert-statistics/expert-ai-qualified-statistics`

---

## 6. 复用现有接口和方法

### 6.1 复用的方法

1. **部门信息查询**
   - `DepartmentInfoMapper.getDepartmentByCode(String deptCode)`

2. **专家数据查询**
   - `ExpertMapper.getExpertInfoByDeptCode(String deptCode, Integer deptLevel)`（已创建）

3. **职位类提取方法**
   - `ExpertCertStatisticsService.extractJobCategory(String jobCategory)`（已存在）

4. **任职状态查询**
   - `ExpertCertStatisticsService.getQualifiedEmployeeNumbers(List<String> employeeNumbers)`
   - 该方法内部调用`ExpertCertStatisticsMapper.getQualifiedEmployeeNumbers`

### 6.2 参考的实现

1. **干部任职数据查询**
   - 方法：`ExpertCertStatisticsService.getCadreMaturityJobCategoryQualifiedStatistics`
   - 参考其分组统计、占比计算、响应构建逻辑

2. **专家认证数据查询**
   - 方法：`ExpertCertStatisticsService.getExpertAiCertStatistics`
   - 参考其查询专家数据、提取职位类、分组统计的逻辑

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

### 7.3 任职查询逻辑

1. **任职条件**：查询`t_qualifications`表中符合条件的记录
   - `direction_cn_name`必须为以下之一：
     - `数据科学与AI工程（ICT）`
     - `AI算法及应用（ICT）`
     - `AI软件工程与工具（ICT）`
     - `AI系统测试（ICT）`
   - `competence_from`和`competence_to`不为空
   - 当前日期在任职有效期内（`CURDATE() BETWEEN competence_from AND competence_to`）

### 7.4 数据准确性

1. **基数统计**：成熟度的基线人数应包含该成熟度下所有职位类的专家总数
2. **任职统计**：任职人数必须小于等于基线人数
3. **占比计算**：使用`BigDecimal`进行精确计算，保留4位小数，使用`RoundingMode.HALF_UP`舍入模式

### 7.5 空数据处理

1. **无数据情况**：如果没有专家数据，返回空列表和0值统计，不要返回null
2. **空值处理**：对于可能为null的字段，使用默认值（如0、BigDecimal.ZERO、"未知"）

### 7.6 与干部任职接口的区别

1. **不需要qualifiedByRequirementCount字段**：干部任职接口中有"按要求AI任职人数"字段，专家任职接口不需要
2. **不需要qualifiedByRequirementRate字段**：干部任职接口中有"按要求AI任职人数占比"字段，专家任职接口不需要
3. **查询逻辑相同**：都使用`getQualifiedEmployeeNumbers`方法查询任职状态

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
   - 测试没有任职人员的情况

3. **数据准确性**
   - 验证基数人数是否正确
   - 验证任职人数是否正确
   - 验证占比计算是否正确
   - 验证职位类提取是否正确
   - 验证任职查询逻辑是否正确（有效期判断）

### 8.2 调试接口

建议新增一个调试接口，用于查看原始专家数据和任职状态：

```java
@GetMapping("/expert-ai-qualified-statistics/debug")
public ResponseEntity<Result<Object>> getExpertAiQualifiedDebugInfo(
        @RequestParam(value = "deptCode", required = true) String deptCode) {
    // 返回原始专家数据列表，包含工号、AI成熟度、职位类、是否任职等信息
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
- `firstdeptcode`、`seconddeptcode`、`thirddeptcode`、`fourthdeptcode`、`fifthdeptcode`、`sixthdeptcode`：部门编码字段

#### t_qualifications表（任职表）
- `employee_number`：员工工号
- `direction_cn_name`：方向中文名称（需要匹配AI相关方向）
- `competence_from`：任职开始日期
- `competence_to`：任职结束日期

### 10.2 相关常量

```java
// DepartmentConstants.java
public static final String CLOUD_CORE_NETWORK_DEPT_CODE = "031562";
```

### 10.3 任职查询逻辑

任职查询通过`getQualifiedEmployeeNumbers`方法实现，查询`t_qualifications`表中：
- `direction_cn_name`为以下之一：
  - `数据科学与AI工程（ICT）`
  - `AI算法及应用（ICT）`
  - `AI软件工程与工具（ICT）`
  - `AI系统测试（ICT）`
- `competence_from IS NOT NULL`且`competence_to IS NOT NULL`
- `CURDATE() BETWEEN competence_from AND competence_to`（当前日期在任职有效期内）

### 10.4 与认证接口的对比

| 对比项 | 认证接口 | 任职接口 |
|--------|---------|---------|
| 接口路径 | `/expert-ai-cert-statistics` | `/expert-ai-qualified-statistics` |
| 查询方法 | `getCertifiedEmployeeNumbers` | `getQualifiedEmployeeNumbers` |
| 查询表 | `dwr_t_cert_record_t` | `t_qualifications` |
| 响应字段 | `certifiedCount`、`certRate` | `qualifiedCount`、`qualifiedRate` |
| 其他差异 | 无 | 无 |

---

## 11. 实现要点总结

1. **高度复用**：与专家认证接口高度相似，可以复用大部分代码逻辑
2. **主要区别**：
   - 查询方法从`getCertifiedEmployeeNumbers`改为`getQualifiedEmployeeNumbers`
   - 响应字段从`certifiedCount`、`certRate`改为`qualifiedCount`、`qualifiedRate`
   - VO类名从`Cert`改为`Qualified`
3. **实现建议**：可以参考`getExpertAiCertStatistics`方法的实现，将认证相关的逻辑替换为任职相关的逻辑即可








