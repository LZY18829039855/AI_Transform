# /employee-cert-statistics 接口扩展支持专家数据查询

## 一、接口概述

### 1.1 接口信息
- **接口路径**: `/expert-cert-statistics/employee-cert-statistics`
- **请求方式**: `GET`
- **接口描述**: 查询部门下员工（全员/干部/专家）的任职认证统计信息，按子部门维度统计认证人数和任职人数

### 1.2 当前支持情况
- ✅ **personType=0**: 全员数据查询（已支持）
- ✅ **personType=1**: 干部数据查询（已支持）
- ❌ **personType=2**: 专家数据查询（**本次新增支持**）

## 二、接口参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| deptCode | String | 是 | 部门ID（部门编码） |
| personType | Integer | 是 | 人员类型：0-全员，1-干部，**2-专家**（新增） |

### 2.1 参数说明

#### deptCode
- 部门编码，用于指定查询的部门
- 当 `deptCode="0"` 时：
  - **全员类型（personType=0）**：查询云核心网产品线部门下的所有四级部门
  - **干部类型（personType=1）**：查询云核心网产品线部门下的所有四级部门
  - **专家类型（personType=2）**：查询云核心网产品线部门下的所有四级部门的专家数据

#### personType
- **0**: 全员数据
- **1**: 干部数据
- **2**: 专家数据（**新增支持**）

## 三、专家数据查询实现逻辑

### 3.1 查询流程

当 `personType=2` 时，按以下步骤查询（**与全员/干部查询逻辑保持一致，按子部门维度统计**）：

1. **特殊处理：当 deptCode 为 "0" 时**
   - 调用 `DepartmentInfoMapper.getLevel4DepartmentsUnderLevel2(DepartmentConstants.CLOUD_CORE_NETWORK_DEPT_CODE)` 方法
   - 查询云核心网产品线部门下的所有四级部门
   - 如果没有四级部门，返回空统计
   - 设置 `queryLevel = 4`（用于后续查询四级部门的专家）
   - 跳转到步骤3，遍历这些四级部门

2. **普通处理：当 deptCode 不为 "0" 时**
   - **2.1 查询部门信息**
     - 根据 `deptCode` 查询部门信息，获取部门层级（`deptLevel`）
     - 如果部门不存在，返回错误
   
   - **2.2 查询下一级子部门列表**
     - 调用 `DepartmentInfoMapper.getChildDepartments(deptCode)` 方法
     - 查询当前部门的所有下一级子部门
     - 如果没有子部门，返回空统计
   
   - **2.3 确定查询层级**
     - 根据当前部门层级，确定查询的部门层级（下一层）
     - `queryLevel = currentLevel + 1`

3. **遍历每个子部门（或四级部门），分别统计专家数据**
   - 对每个子部门，执行以下步骤：
     - **3.1 查询该子部门下的专家工号列表**
       - 调用新增方法：`ExpertMapper.getExpertNumbersByDeptLevel(queryLevel, deptIdList)`
       - 该方法查询逻辑：
         - 查询 `t_expert` 表，通过工号（`exp.account = e.employee_number`）关联 `t_employee_sync` 表
         - 过滤条件：`e.period_id = 20251126`（使用period_id过滤）
         - 根据子部门的层级（`queryLevel`）使用对应的部门字段进行过滤：
           - `queryLevel=1`: `e.firstdeptcode IN (子部门编码列表)`
           - `queryLevel=2`: `e.seconddeptcode IN (子部门编码列表)`
           - `queryLevel=3`: `e.thirddeptcode IN (子部门编码列表)`
           - `queryLevel=4`: `e.fourthdeptcode IN (子部门编码列表)`
           - `queryLevel=5`: `e.fifthdeptcode IN (子部门编码列表)`
           - `queryLevel=6`: `e.sixthdeptcode IN (子部门编码列表)`
         - 返回该子部门下的专家工号列表
     
     - **3.2 查询该子部门已通过认证的专家工号列表**
       - 复用已有接口：`ExpertCertStatisticsService.getCertifiedEmployeeNumbers(employeeNumbers)`
       - 该方法查询 `dwr_t_cert_record_t` 表，筛选条件：
         - 证书状态：`status = 1 OR approved_status = 1`
         - 证书类型：华为研究类能力认证（专业级）
           - `华为研究类能力认证（专业级，AI算法技术）`
           - `华为研究类能力认证（专业级，AI决策推理）`
           - `华为研究类能力认证（专业级，AI图像语言语义）`
       - 返回已通过认证的专家工号列表
     
     - **3.3 查询该子部门已获得任职的专家工号列表**
       - 复用已有接口：`ExpertCertStatisticsService.getQualifiedEmployeeNumbers(employeeNumbers)`
       - 该方法查询 `t_qualifications` 表，筛选条件：
         - 方向名称包含AI相关方向
         - 返回已获得AI任职的专家工号列表
     
     - **3.4 计算该子部门的认证率和任职率**
       - 认证率 = 认证人数 / 总人数 * 100
       - 任职率 = 任职人数 / 总人数 * 100
       - 保留4位小数
     
     - **3.5 构建子部门统计对象**
       - 包含：部门编码、部门名称、总人数、认证人数、任职人数、认证率、任职率

4. **汇总总计统计**
   - 累加所有子部门的总人数、认证人数、任职人数
   - 计算总计的认证率和任职率
   - 构建总计统计对象

5. **构建返回结果**
   - `departmentStatistics`: 包含所有子部门的统计信息
   - `totalStatistics`: 包含总计统计信息
   - 返回格式与全员/干部统计完全一致

### 3.2 关键SQL参考

#### 新增：根据部门层级查询专家工号的SQL（ExpertMapper.xml）
```xml
<!-- 根据部门层级和部门ID列表查询专家工号列表 -->
<!-- 注意：查询的部门层级为传入的deptLevel，即deptLevel=1时查询firstdeptcode -->
<select id="getExpertNumbersByDeptLevel" resultType="java.lang.String">
    SELECT DISTINCT exp.account AS employeeNumber
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
            <foreach collection="deptIds" item="deptId" open="(" separator="," close=")">
                #{deptId}
            </foreach>
        </when>
        <when test="deptLevel == 2">
            AND e.seconddeptcode IN
            <foreach collection="deptIds" item="deptId" open="(" separator="," close=")">
                #{deptId}
            </foreach>
        </when>
        <when test="deptLevel == 3">
            AND e.thirddeptcode IN
            <foreach collection="deptIds" item="deptId" open="(" separator="," close=")">
                #{deptId}
            </foreach>
        </when>
        <when test="deptLevel == 4">
            AND e.fourthdeptcode IN
            <foreach collection="deptIds" item="deptId" open="(" separator="," close=")">
                #{deptId}
            </foreach>
        </when>
        <when test="deptLevel == 5">
            AND e.fifthdeptcode IN
            <foreach collection="deptIds" item="deptId" open="(" separator="," close=")">
                #{deptId}
            </foreach>
        </when>
        <when test="deptLevel == 6">
            AND e.sixthdeptcode IN
            <foreach collection="deptIds" item="deptId" open="(" separator="," close=")">
                #{deptId}
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
- 该SQL参考 `EmployeeMapper.getEmployeeNumbersByDeptLevel` 的实现逻辑
- 区别在于：需要关联 `t_expert` 表，通过工号关联 `t_employee_sync` 表
- 使用 `period_id` 进行过滤（`e.period_id = 20251126`）
- 根据部门层级使用对应的部门字段进行过滤（firstdeptcode、seconddeptcode等）
- **注意**：不需要通过成熟度（`position_ai_maturity`）过滤，只通过 `period_id` 和部门字段过滤

## 四、返回结果

### 4.1 返回数据结构

```json
{
    "code": 200,
    "message": "查询成功",
    "data": {
        "departmentStatistics": [
            {
                "deptCode": "部门编码",
                "deptName": "部门名称",
                "totalCount": 100,
                "certifiedCount": 80,
                "qualifiedCount": 75,
                "certRate": 80.0000,
                "qualifiedRate": 75.0000
            }
        ],
        "totalStatistics": {
            "deptCode": "总计",
            "deptName": "总计",
            "totalCount": 100,
            "certifiedCount": 80,
            "qualifiedCount": 75,
            "certRate": 80.0000,
            "qualifiedRate": 75.0000
        }
    }
}
```

### 4.2 字段说明

| 字段名 | 类型 | 说明 |
|--------|------|------|
| departmentStatistics | List | 各部门统计列表（专家类型时，该列表为空或包含单个部门统计） |
| totalStatistics | Object | 总计统计信息 |
| deptCode | String | 部门编码（总计时固定为"总计"） |
| deptName | String | 部门名称（总计时固定为"总计"） |
| totalCount | Integer | 总人数（专家总数） |
| certifiedCount | Integer | 认证人数（通过华为研究类能力认证的专家数） |
| qualifiedCount | Integer | 任职人数（获得AI任职的专家数） |
| certRate | BigDecimal | 认证率（certifiedCount / totalCount * 100，保留4位小数） |
| qualifiedRate | BigDecimal | 任职率（qualifiedCount / totalCount * 100，保留4位小数） |

### 4.3 专家数据查询的特殊说明

- **departmentStatistics**: 专家数据查询时，该列表包含所有子部门的统计信息（与全员/干部查询逻辑一致）
- **totalStatistics**: 包含所有子部门专家的汇总统计信息

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
GET /expert-cert-statistics/employee-cert-statistics?deptCode=123456&personType=2
```

**请求示例2（查询所有四级部门，deptCode=0）**:
```
GET /expert-cert-statistics/employee-cert-statistics?deptCode=0&personType=2
```

**响应示例**:
```json
{
    "code": 200,
    "message": "查询成功",
    "data": {
        "departmentStatistics": [
            {
                "deptCode": "子部门1编码",
                "deptName": "子部门1名称",
                "totalCount": 20,
                "certifiedCount": 16,
                "qualifiedCount": 15,
                "certRate": 80.0000,
                "qualifiedRate": 75.0000
            },
            {
                "deptCode": "子部门2编码",
                "deptName": "子部门2名称",
                "totalCount": 30,
                "certifiedCount": 24,
                "qualifiedCount": 20,
                "certRate": 80.0000,
                "qualifiedRate": 66.6667
            }
        ],
        "totalStatistics": {
            "deptCode": "总计",
            "deptName": "总计",
            "totalCount": 50,
            "certifiedCount": 40,
            "qualifiedCount": 35,
            "certRate": 80.0000,
            "qualifiedRate": 70.0000
        }
    }
}
```

## 七、实现要点

### 7.1 复用现有方法
- ✅ `DepartmentInfoMapper.getChildDepartments()` - 查询子部门列表（已存在）
- ✅ `DepartmentInfoMapper.getLevel4DepartmentsUnderLevel2()` - 查询四级部门列表（已存在，用于deptCode="0"时）
- ✅ `ExpertCertStatisticsService.getCertifiedEmployeeNumbers()` - 查询认证人数（已存在）
- ✅ `ExpertCertStatisticsService.getQualifiedEmployeeNumbers()` - 查询任职人数（已存在）

### 7.2 需要新增的代码

#### Mapper层
- **ExpertMapper接口**: 新增方法 `getExpertNumbersByDeptLevel()`
  ```java
  List<String> getExpertNumbersByDeptLevel(
      @Param("deptLevel") Integer deptLevel,
      @Param("deptIds") List<String> deptIds);
  ```

- **ExpertMapper.xml**: 新增SQL查询方法 `getExpertNumbersByDeptLevel`
  - 参考 `EmployeeMapper.getEmployeeNumbersByDeptLevel` 的实现
  - 关联 `t_expert` 表，通过工号关联 `t_employee_sync` 表
  - 使用 `period_id = 20251126` 过滤（不需要通过成熟度过滤）
  - 根据部门层级使用对应的部门字段过滤（firstdeptcode、seconddeptcode等）

#### Service层
- **ExpertCertStatisticsService.getEmployeeCertStatistics()**
  - 添加 `personType=2` 的处理逻辑
  - **特殊处理：当 deptCode="0" 时**
    - 调用 `DepartmentInfoMapper.getLevel4DepartmentsUnderLevel2(DepartmentConstants.CLOUD_CORE_NETWORK_DEPT_CODE)` 查询所有四级部门
    - 设置 `queryLevel = 4`
    - 如果没有四级部门，返回空统计
  - **普通处理：当 deptCode 不为 "0" 时**
    - 查询当前部门的下一级子部门列表（`getChildDepartments`）
    - 根据当前部门层级，确定查询层级（`queryLevel = currentLevel + 1`）
    - 如果没有子部门，返回空统计
  - **遍历每个子部门（或四级部门）**：
    - 调用 `ExpertMapper.getExpertNumbersByDeptLevel(queryLevel, deptIdList)` 查询该部门的专家工号
    - 调用 `getCertifiedEmployeeNumbers()` 查询认证人数
    - 调用 `getQualifiedEmployeeNumbers()` 查询任职人数
    - 计算认证率和任职率
    - 构建部门统计对象
  - 汇总所有部门的统计数据，构建总计统计
  - 返回格式与全员/干部统计完全一致

#### Controller层
- **ExpertCertStatisticsController.getEmployeeCertStatistics()**
  - 修改参数校验，支持 `personType=2`
  - 错误提示更新为："暂不支持该人员类型，目前只支持全员（personType=0）、干部（personType=1）和专家（personType=2）"

### 7.3 注意事项
1. **特殊处理 deptCode="0"**: 
   - 当 `deptCode="0"` 时，查询云核心网产品线部门下的所有四级部门
   - 使用 `getLevel4DepartmentsUnderLevel2()` 方法查询四级部门列表
   - 设置 `queryLevel = 4`，用于查询这些四级部门的专家

2. **部门层级处理**: 
   - 普通情况下，查询当前部门的下一级子部门列表
   - 根据子部门的层级（`queryLevel = currentLevel + 1`）调用 `getExpertNumbersByDeptLevel()`
   - 注意：`queryLevel` 是子部门的层级，用于确定查询 `t_employee_sync` 表的哪个部门字段

3. **空数据处理**: 
   - 如果部门下没有子部门，返回空统计（departmentStatistics为空列表，totalCount=0）
   - 如果某个子部门下没有专家，该子部门的统计为 totalCount=0

3. **数据一致性**: 
   - 专家数据查询按子部门维度统计，与全员/干部查询逻辑保持一致
   - 每个子部门独立统计，最后汇总总计

4. **认证标准**: 
   - 专家认证标准为华为研究类能力认证（专业级）的三种类型之一
   - 查询专家工号时，只通过 `period_id` 和部门字段过滤，不需要通过成熟度过滤

5. **SQL实现参考**: 
   - 参考 `EmployeeMapper.getEmployeeNumbersByDeptLevel` 的实现方式
   - 区别在于需要关联 `t_expert` 表，通过工号关联 `t_employee_sync` 表
   - 只通过 `period_id` 和部门字段过滤，不需要通过成熟度过滤

## 八、测试建议

### 8.1 功能测试
1. 测试 `personType=2` 时，能正确查询专家数据
2. 测试不同部门层级的专家查询
3. 测试 `deptCode="0"` 时，能正确查询所有四级部门的专家数据
4. 测试部门下无专家时的返回结果
5. 测试认证人数和任职人数的统计准确性

### 8.2 边界测试
1. 测试 `deptCode` 为空或不存在的情况
2. 测试 `personType` 为无效值的情况
3. 测试部门下专家数量为0的情况
4. 测试 `deptCode="0"` 时，如果云核心网产品线下没有四级部门的情况

### 8.3 对比测试
1. 对比 `/person-cert-details` 接口查询的专家数量，确保一致性
2. 对比认证人数统计与证书表数据的一致性
3. 对比专家数据查询与全员/干部数据查询的返回格式一致性
4. 验证子部门统计的准确性（每个子部门的专家数量、认证人数、任职人数）

