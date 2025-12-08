# /competence-category-cert-statistics 接口扩展支持专家数据查询

## 一、接口概述

### 1.1 接口信息
- **接口路径**: `/expert-cert-statistics/competence-category-cert-statistics`
- **请求方式**: `GET`
- **接口描述**: 按职位类统计部门下不同职位类人数中的认证和任职人数

### 1.2 当前支持情况
- ✅ **personType=0**: 全员数据查询（已支持）
- ✅ **personType=1**: 干部数据查询（已支持）
- ❌ **personType=2**: 专家数据查询（**本次新增支持**）

## 二、接口参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| deptCode | String | 是 | 部门ID（部门编码），当为"0"时，使用云核心网产品线部门ID |
| personType | Integer | 是 | 人员类型：0-全员，1-干部，**2-专家**（新增） |

### 2.1 参数说明

#### deptCode
- 部门编码，用于指定查询的部门
- 当 `deptCode="0"` 时，使用云核心网产品线部门ID（`DepartmentConstants.CLOUD_CORE_NETWORK_DEPT_CODE`）

#### personType
- **0**: 全员数据
- **1**: 干部数据
- **2**: 专家数据（**新增支持**）

## 三、专家数据查询实现逻辑

### 3.1 查询流程

当 `personType=2` 时，按以下步骤查询：

1. **查询部门信息**
   - 根据 `deptCode` 查询部门信息，获取部门层级（`deptLevel`）
   - 如果部门不存在，返回错误
   - 当 `deptCode="0"` 时，使用云核心网产品线部门ID

2. **查询专家信息（关联全员信息表获取职位族）**
   - 构造部门编码列表（只包含当前部门本身）
   - 调用新增方法：`ExpertMapper.getExpertsWithJobCategoryByDeptCodes(deptLevel, deptCodeList)`
   - 该方法查询逻辑：
     - 查询 `t_expert` 表，通过工号（`exp.account = e.employee_number`）关联 `t_employee_sync` 表
     - 过滤条件：`e.period_id = 20251126`（使用period_id过滤）
     - 根据部门层级（`deptLevel`）使用对应的部门字段进行过滤（只查询当前部门下的人员）：
       - `deptLevel=1`: `e.firstdeptcode = 当前部门编码`
       - `deptLevel=2`: `e.seconddeptcode = 当前部门编码`
       - `deptLevel=3`: `e.thirddeptcode = 当前部门编码`
       - `deptLevel=4`: `e.fourthdeptcode = 当前部门编码`
       - `deptLevel=5`: `e.fifthdeptcode = 当前部门编码`
       - `deptLevel=6`: `e.sixthdeptcode = 当前部门编码`
     - 返回专家工号和职位族（`job_category`）信息列表
     - 职位族格式：`职位族-职位类-职位子类`（例如：`软件-软件类-软件工程`）

4. **提取职位类**
   - 从职位族字符串中提取职位类（使用 `extractCompetenceCategory` 方法）
   - 格式：职位族-职位类-职位子类，需要提取中间的职位类
   - 如果格式不正确或为空，则返回"未知"

5. **查询已通过认证的专家工号列表**
   - 提取所有专家工号
   - 调用已有方法：`ExpertCertStatisticsService.getCertifiedEmployeeNumbers(employeeNumbers)`
   - 该方法查询 `dwr_t_cert_record_t` 表，筛选条件：
     - 证书状态：`status = 1 OR approved_status = 1`
     - 证书类型：华为研究类能力认证（专业级）
       - `华为研究类能力认证（专业级，AI算法技术）`
       - `华为研究类能力认证（专业级，AI决策推理）`
       - `华为研究类能力认证（专业级，AI图像语言语义）`
   - 返回已通过认证的专家工号列表

6. **查询已获得任职的专家工号列表**
   - 调用已有方法：`ExpertCertStatisticsService.getQualifiedEmployeeNumbers(employeeNumbers)`
   - 该方法查询 `t_qualifications` 表，筛选条件：
     - 方向名称包含AI相关方向
   - 返回已获得AI任职的专家工号列表

7. **按职位类分组统计**
   - 遍历所有专家，按职位类分组
   - 对每个职位类：
     - 累加总人数（基线人数）
     - 统计认证人数（在已认证工号列表中的专家数）
     - 统计任职人数（在已任职工号列表中的专家数）
     - 计算认证率 = 认证人数 / 总人数 * 100（保留4位小数）
     - 计算任职率 = 任职人数 / 总人数 * 100（保留4位小数）

8. **汇总总计统计**
   - 累加所有职位类的总人数、认证人数、任职人数
   - 计算总计的认证率和任职率
   - 构建总计统计对象

9. **构建返回结果**
   - `categoryStatistics`: 包含所有职位类的统计信息
   - `totalStatistics`: 包含总计统计信息
   - 返回格式与全员/干部统计完全一致

### 3.2 关键SQL参考

#### 新增：根据部门层级和部门编码列表查询专家工号和职位族（ExpertMapper.xml）

```xml
<!-- 根据部门层级和部门编码列表查询专家工号和职位族 -->
<!-- 注意：查询的部门层级为传入的deptLevel，即deptLevel=1时查询firstdeptcode -->
<!-- 只查询当前部门下的人员，不包含子部门 -->
<select id="getExpertsWithJobCategoryByDeptCodes" resultType="com.huawei.aitransform.entity.EmployeeWithCategoryVO">
    SELECT DISTINCT 
        exp.account AS employeeNumber,
        e.job_category AS competenceCategory
    FROM t_expert exp
    INNER JOIN t_employee_sync e ON (exp.account = e.employee_number)
    WHERE e.period_id = 20251126
    AND exp.account IS NOT NULL
    AND exp.account != ''
    AND e.employee_number IS NOT NULL
    AND e.employee_number != ''
    <choose>
        <when test="deptLevel == 1">
            AND e.firstdeptcode IN
            <foreach collection="deptCodes" item="deptCode" open="(" separator="," close=")">
                #{deptCode}
            </foreach>
        </when>
        <when test="deptLevel == 2">
            AND e.seconddeptcode IN
            <foreach collection="deptCodes" item="deptCode" open="(" separator="," close=")">
                #{deptCode}
            </foreach>
        </when>
        <when test="deptLevel == 3">
            AND e.thirddeptcode IN
            <foreach collection="deptCodes" item="deptCode" open="(" separator="," close=")">
                #{deptCode}
            </foreach>
        </when>
        <when test="deptLevel == 4">
            AND e.fourthdeptcode IN
            <foreach collection="deptCodes" item="deptCode" open="(" separator="," close=")">
                #{deptCode}
            </foreach>
        </when>
        <when test="deptLevel == 5">
            AND e.fifthdeptcode IN
            <foreach collection="deptCodes" item="deptCode" open="(" separator="," close=")">
                #{deptCode}
            </foreach>
        </when>
        <when test="deptLevel == 6">
            AND e.sixthdeptcode IN
            <foreach collection="deptCodes" item="deptCode" open="(" separator="," close=")">
                #{deptCode}
            </foreach>
        </when>
        <when test="deptLevel == 7">
            <!-- 如果deptLevel为7，已经是最高层级，无法再查询 -->
            AND 1 = 0
        </when>
    </choose>
</select>
```

**说明**：
- 该SQL参考 `EmployeeMapper.getEmployeesWithJobCategoryByDeptCodes` 的实现逻辑
- 区别在于：需要关联 `t_expert` 表，通过工号关联 `t_employee_sync` 表
- 使用 `period_id = 20251126` 进行过滤
- 根据部门层级使用对应的部门字段进行过滤（firstdeptcode、seconddeptcode等）
- **只查询当前部门下的人员，不包含子部门**（部门编码列表只包含当前部门本身）
- 返回专家工号和职位族（`job_category`）信息，职位族格式为：`职位族-职位类-职位子类`

## 四、返回结果

### 4.1 返回数据结构

```json
{
    "code": 200,
    "message": "查询成功",
    "data": {
        "deptCode": "部门编码",
        "deptName": "部门名称",
        "categoryStatistics": [
            {
                "competenceCategory": "软件类",
                "totalCount": 50,
                "certifiedCount": 40,
                "qualifiedCount": 35,
                "certRate": 80.0000,
                "qualifiedRate": 70.0000
            },
            {
                "competenceCategory": "硬件类",
                "totalCount": 30,
                "certifiedCount": 24,
                "qualifiedCount": 20,
                "certRate": 80.0000,
                "qualifiedRate": 66.6667
            }
        ],
        "totalStatistics": {
            "competenceCategory": "总计",
            "totalCount": 80,
            "certifiedCount": 64,
            "qualifiedCount": 55,
            "certRate": 80.0000,
            "qualifiedRate": 68.7500
        }
    }
}
```

### 4.2 字段说明

| 字段名 | 类型 | 说明 |
|--------|------|------|
| deptCode | String | 部门编码 |
| deptName | String | 部门名称 |
| categoryStatistics | List | 各职位类统计列表 |
| totalStatistics | Object | 总计统计信息 |
| competenceCategory | String | 职位类（总计时固定为"总计"） |
| totalCount | Integer | 总人数（基线人数） |
| certifiedCount | Integer | 已认证人数（通过华为研究类能力认证的专家数） |
| qualifiedCount | Integer | 已任职人数（获得AI任职的专家数） |
| certRate | BigDecimal | 认证率（certifiedCount / totalCount * 100，保留4位小数） |
| qualifiedRate | BigDecimal | 任职率（qualifiedCount / totalCount * 100，保留4位小数） |

### 4.3 专家数据查询的特殊说明

- **categoryStatistics**: 专家数据查询时，该列表包含所有职位类的统计信息（与全员/干部查询逻辑一致）
- **totalStatistics**: 包含所有职位类专家的汇总统计信息
- **职位类提取**: 从职位族（`job_category`）字段中提取职位类，格式为：`职位族-职位类-职位子类`，提取中间的职位类部分

## 五、错误处理

### 5.1 错误码说明

| 错误码 | 说明 |
|--------|------|
| 400 | 参数错误（部门ID为空、人员类型为空或不支持） |
| 404 | 部门不存在 |
| 500 | 系统异常 |

### 5.2 错误响应示例

```json
{
    "code": 400,
    "message": "暂不支持该人员类型，目前只支持全员（personType=0）、干部（personType=1）和专家（personType=2）",
    "data": null
}
```

## 六、接口调用示例

### 6.1 查询专家数据

**请求示例1（查询指定部门）**:
```
GET /expert-cert-statistics/competence-category-cert-statistics?deptCode=123456&personType=2
```

**请求示例2（查询云核心网产品线，deptCode=0）**:
```
GET /expert-cert-statistics/competence-category-cert-statistics?deptCode=0&personType=2
```

**响应示例**:
```json
{
    "code": 200,
    "message": "查询成功",
    "data": {
        "deptCode": "123456",
        "deptName": "某部门",
        "categoryStatistics": [
            {
                "competenceCategory": "软件类",
                "totalCount": 50,
                "certifiedCount": 40,
                "qualifiedCount": 35,
                "certRate": 80.0000,
                "qualifiedRate": 70.0000
            },
            {
                "competenceCategory": "硬件类",
                "totalCount": 30,
                "certifiedCount": 24,
                "qualifiedCount": 20,
                "certRate": 80.0000,
                "qualifiedRate": 66.6667
            },
            {
                "competenceCategory": "未知",
                "totalCount": 5,
                "certifiedCount": 3,
                "qualifiedCount": 2,
                "certRate": 60.0000,
                "qualifiedRate": 40.0000
            }
        ],
        "totalStatistics": {
            "competenceCategory": "总计",
            "totalCount": 85,
            "certifiedCount": 67,
            "qualifiedCount": 57,
            "certRate": 78.8235,
            "qualifiedRate": 67.0588
        }
    }
}
```

## 七、实现要点

### 7.1 复用现有方法
- ✅ `DepartmentInfoMapper.getDepartmentByCode()` - 查询部门信息（已存在）
- ✅ `ExpertCertStatisticsService.getCertifiedEmployeeNumbers()` - 查询认证人数（已存在）
- ✅ `ExpertCertStatisticsService.getQualifiedEmployeeNumbers()` - 查询任职人数（已存在）
- ✅ `ExpertCertStatisticsService.extractCompetenceCategory()` - 从职位族中提取职位类（已存在，私有方法，需要改为public或复用逻辑）

### 7.2 需要新增的代码

#### Mapper层
- **ExpertMapper接口**: 新增方法 `getExpertsWithJobCategoryByDeptCodes()`
  ```java
  List<EmployeeWithCategoryVO> getExpertsWithJobCategoryByDeptCodes(
      @Param("deptLevel") Integer deptLevel,
      @Param("deptCodes") List<String> deptCodes);
  ```

- **ExpertMapper.xml**: 新增SQL查询方法 `getExpertsWithJobCategoryByDeptCodes`
  - 参考 `EmployeeMapper.getEmployeesWithJobCategoryByDeptCodes` 的实现
  - 关联 `t_expert` 表，通过工号关联 `t_employee_sync` 表
  - 使用 `period_id = 20251126` 过滤
  - 根据部门层级使用对应的部门字段过滤（firstdeptcode、seconddeptcode等）
  - 返回专家工号和职位族（`job_category`）信息

#### Service层
- **ExpertCertStatisticsService.getCompetenceCategoryCertStatistics()**
  - 添加 `personType=2` 的处理逻辑
  - 调用新增方法 `getExpertCompetenceCategoryCertStatistics(deptCode)`

- **ExpertCertStatisticsService.getExpertCompetenceCategoryCertStatistics()**（新增私有方法）
  - 查询部门信息
  - 构造部门编码列表（只包含当前部门本身）
  - 调用 `ExpertMapper.getExpertsWithJobCategoryByDeptCodes()` 查询专家工号和职位族
  - 从职位族中提取职位类（使用 `extractCompetenceCategory` 方法）
  - 查询已通过认证的专家工号列表
  - 查询已获得任职的专家工号列表
  - 按职位类分组统计
  - 计算认证率和任职率
  - 汇总总计统计
  - 构建返回结果

#### Controller层
- **ExpertCertStatisticsController.getCompetenceCategoryCertStatistics()**
  - 修改参数校验，支持 `personType=2`
  - 错误提示更新为："暂不支持该人员类型，目前只支持全员（personType=0）、干部（personType=1）和专家（personType=2）"

### 7.3 注意事项

1. **部门层级处理**: 
   - 只查询当前部门下的人员，不包含子部门
   - 根据当前部门的层级（`deptLevel`）调用 `getExpertsWithJobCategoryByDeptCodes()`
   - 注意：`deptLevel` 是当前部门的层级，用于确定查询 `t_employee_sync` 表的哪个部门字段
   - 部门编码列表只包含当前部门本身

2. **职位类提取**: 
   - 从职位族（`job_category`）字段中提取职位类
   - 职位族格式：`职位族-职位类-职位子类`，需要提取中间的职位类
   - 如果格式不正确或为空，则返回"未知"

3. **空数据处理**: 
   - 如果部门下没有专家，返回空统计（categoryStatistics为空列表，totalCount=0）
   - 如果某个职位类下没有专家，该职位类的统计为 totalCount=0

4. **数据一致性**: 
   - 专家数据查询按职位类维度统计，与全员/干部查询逻辑保持一致
   - 每个职位类独立统计，最后汇总总计

5. **认证标准**: 
   - 专家认证标准为华为研究类能力认证（专业级）的三种类型之一
   - 查询专家工号时，只通过 `period_id` 和部门字段过滤

6. **SQL实现参考**: 
   - 参考 `EmployeeMapper.getEmployeesWithJobCategoryByDeptCodes` 的实现方式
   - 区别在于需要关联 `t_expert` 表，通过工号关联 `t_employee_sync` 表
   - 只通过 `period_id` 和部门字段过滤

7. **特殊处理 deptCode="0"**: 
   - 当 `deptCode="0"` 时，使用云核心网产品线部门ID（`DepartmentConstants.CLOUD_CORE_NETWORK_DEPT_CODE`）

## 八、测试建议

### 8.1 功能测试
1. 测试 `personType=2` 时，能正确查询专家数据
2. 测试不同部门层级的专家查询
3. 测试 `deptCode="0"` 时，能正确查询云核心网产品线部门的专家数据
4. 测试部门下无专家时的返回结果
5. 测试认证人数和任职人数的统计准确性
6. 测试职位类提取的准确性（从职位族中提取职位类）

### 8.2 边界测试
1. 测试 `deptCode` 为空或不存在的情况
2. 测试 `personType` 为无效值的情况
3. 测试部门下专家数量为0的情况
4. 测试职位族格式不正确的情况（提取职位类为"未知"）
5. 测试职位族为空或null的情况

### 8.3 对比测试
1. 对比 `/employee-cert-statistics` 接口查询的专家数量，确保一致性
2. 对比认证人数统计与证书表数据的一致性
3. 对比专家数据查询与全员/干部数据查询的返回格式一致性
4. 验证职位类统计的准确性（每个职位类的专家数量、认证人数、任职人数）
5. 验证职位类提取逻辑的正确性（与全员/干部查询逻辑一致）

