# /person-cert-details 接口扩展支持全员认证数据查询

## 一、接口概述

### 1.1 接口信息
- **接口路径**: `/expert-cert-statistics/person-cert-details`
- **请求方式**: `GET`
- **接口描述**: 查询干部/专家/全员认证详细信息，支持按AI成熟度、职位类等条件筛选

### 1.2 当前支持情况
- ✅ **personType=1**: 干部认证数据查询（已支持）
- ✅ **personType=2**: 专家认证数据查询（已支持）
- ❌ **personType=0**: 全员认证数据查询（**本次新增支持**）

## 二、接口参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| deptCode | String | 是 | 部门ID（部门编码），当为"0"或为空时，默认查询云核心网产品线部门ID |
| aiMaturity | String | 否 | 岗位AI成熟度（可选，如：L2、L3、L5等） |
| jobCategory | String | 否 | 职位类（可选，如：软件类、研究类、非软件类、其他类等） |
| personType | Integer | 是 | 人员类型：0-全员，1-干部，2-专家 |
| queryType | Integer | 否 | 查询类型：1-认证人数（默认），2-基线人数。全员、干部、专家类型都支持 |

### 2.1 参数说明

#### deptCode
- 部门编码，用于指定查询的部门
- 当 `deptCode="0"`、空字符串或未提供时：
  - **全员类型（personType=0）**：使用云核心网产品线部门ID
  - **干部类型（personType=1）**：使用云核心网产品线部门ID
  - **专家类型（personType=2）**：使用云核心网产品线部门ID

#### personType
- **0**: 全员数据（**新增支持**）
- **1**: 干部数据
- **2**: 专家数据

#### queryType
- **1**: 认证人数（默认值）
  - 全员类型：只返回已通过AI专业级认证的员工详细信息（INNER JOIN dwr_t_cert_record_t）
  - 干部类型：只返回已通过AI专业级认证的干部详细信息
  - 专家类型：只返回已通过AI专业级认证的专家详细信息
- **2**: 基线人数
  - 全员类型：返回所有员工信息（不限制是否认证），如果有认证信息则一并返回（LEFT JOIN dwr_t_cert_record_t）
  - 干部类型：返回所有干部（不限制是否认证）
  - 专家类型：返回所有专家（不限制是否认证）

#### aiMaturity（可选）
- 岗位AI成熟度，如：L2、L3、L5等
- 全员类型：**不支持**，如果传入aiMaturity参数，应忽略该参数（全员数据不按成熟度过滤）
- 干部类型：支持按成熟度过滤
- 专家类型：支持按成熟度过滤，L5代表查询L2和L3

#### jobCategory（可选）
- 职位类，如：软件类、研究类、非软件类、其他类等
- 全员类型：支持按职位类过滤
- 干部类型：支持按职位类过滤
- 专家类型：支持按职位类过滤

## 三、全员认证数据查询实现逻辑

### 3.1 查询流程

当 `personType=0` 时，按以下步骤查询：

1. **参数校验**
   - 如果 `queryType` 为空或未提供，默认为1（认证人数）
   - 如果 `queryType` 不为1且不为2，返回错误："查询类型参数错误，只支持1（认证人数）或2（基线人数）"
   - 如果传入了 `aiMaturity` 参数，应忽略该参数（全员数据不按成熟度过滤）

2. **特殊处理：当 deptCode 为 "0" 时**
   - 将 `deptCode` 设置为云核心网产品线部门ID：`deptCode = DepartmentConstants.CLOUD_CORE_NETWORK_DEPT_CODE`
   - 继续执行步骤3

3. **查询部门信息**
   - 根据 `deptCode` 查询部门信息，获取部门层级（`deptLevel`）
   - 如果部门不存在，返回错误
   
4. **确定查询层级**
   - 直接使用当前部门的层级进行查询
   - `queryLevel = currentLevel`（使用当前部门层级，不查询子部门）

5. **查询员工认证详细信息**
   - 对于当前部门，执行以下步骤：
     - **5.1 查询该部门下的员工工号列表**
       - 调用 `EmployeeMapper.getEmployeeNumbersByDeptLevel(queryLevel, deptIdList)` 方法
       - 该方法查询逻辑：
         - 查询 `t_employee_sync` 表，过滤条件：`period_id = 20251126`
         - 根据当前部门的层级（`queryLevel`）使用对应的部门字段进行过滤：
           - `queryLevel=1`: `e.firstdeptcode = 当前部门编码`
           - `queryLevel=2`: `e.seconddeptcode = 当前部门编码`
           - `queryLevel=3`: `e.thirddeptcode = 当前部门编码`
           - `queryLevel=4`: `e.fourthdeptcode = 当前部门编码`
           - `queryLevel=5`: `e.fifthdeptcode = 当前部门编码`
           - `queryLevel=6`: `e.sixthdeptcode = 当前部门编码`
         - 返回该部门下的员工工号列表
     
     - **5.2 按职位类过滤（如果提供了jobCategory参数）**
       - 如果 `jobCategory` 不为空，需要从 `t_employee_sync` 表的 `job_category` 字段中提取职位类进行过滤
       - 职位类提取逻辑：
         - 如果 `job_category` 格式为 `xxx-职位类-xxx`，提取中间部分
         - 如果 `job_category` 格式为 `xxx-职位类`，提取最后部分
         - 否则直接使用 `job_category` 值
       - 特殊值处理：
         - `jobCategory="非软件类"`：查询所有不是"软件类"的职位类
         - `jobCategory="其他类"`：查询所有不是"研究类"、"软件类"、"系统类"、"测试类"的职位类
         - 其他值：精确匹配
     
     - **5.3 查询员工认证详细信息**
       - 根据 `queryType` 的值，采用不同的查询策略：
       - **当 queryType=1（认证人数）时**：
         - 使用 INNER JOIN 关联 `dwr_t_cert_record_t` 表
         - 只返回已通过AI专业级认证的员工详细信息
         - 认证筛选条件：
           - 证书状态：`status = 1 OR approved_status = 1`
           - 证书类型：华为研究类能力认证（专业级）的三种类型之一：
             - `华为研究类能力认证（专业级，AI算法技术）`
             - `华为研究类能力认证（专业级，AI决策推理）`
             - `华为研究类能力认证（专业级，AI图像语言语义）`
         - 如果同一员工有多条认证记录，选择最新的记录（按 `start_time` 降序）
       - **当 queryType=2（基线人数）时**：
         - 使用 LEFT JOIN 关联 `dwr_t_cert_record_t` 表
         - 返回所有员工信息（不限制是否认证）
         - 如果员工有认证信息，则一并返回认证详细信息
         - 如果员工没有认证信息，认证相关字段（certTitle、certStartTime等）返回 null
         - 如果员工有多条认证记录，选择最新的记录（按 `start_time` 降序）
       - 需要查询的字段包括：
         - 基本信息：姓名、工号、职位类
         - 部门信息：一级到六级部门名称
         - 认证信息：证书标题、证书开始时间（queryType=2时可能为null）
         - 其他信息：是否通过科目二、是否为干部、干部类型、是否认证达标、岗位AI成熟度（全员类型可能为空）、最小部门名称

6. **构建返回结果**
   - `employeeDetails`: 包含所有符合条件的员工认证详细信息列表
   - 去重（按工号去重，如果同一员工有多条认证记录，保留最新的记录）

### 3.2 认证详细信息查询逻辑

#### 3.2.1 数据来源
- 员工基本信息：`t_employee_sync` 表（`period_id = 20251126`）
- 认证信息：`dwr_t_cert_record_t` 表
- 科目二考试信息：`t_exam_record` 表（LEFT JOIN，用于判断是否通过科目二）
- 干部信息：`t_cadre` 表（LEFT JOIN，用于判断是否为干部、获取干部类型和干部认证达标情况）
- 专家信息：`t_expert` 表（LEFT JOIN，用于判断是否为专家、获取专家岗位成熟度）
- 最小部门信息：`department_info_hrms` 表（LEFT JOIN，通过 `sixthdeptcode` 关联）

#### 3.2.1.1 字段获取说明

**是否干部（isCadre）**：
- 通过 LEFT JOIN `t_cadre` 表，关联条件：`e.employee_number = c.account`
- 如果 `c.account` 存在，则 `isCadre = 1`，否则 `isCadre = 0`
- SQL示例：
  ```sql
  CASE 
      WHEN c.account IS NOT NULL THEN 1 
      ELSE 0 
  END AS isCadre
  ```

**干部类型（cadreType）**：
- 从 `t_cadre` 表的 `cadre_type` 字段获取
- 如果员工不是干部，则返回 `null`
- SQL示例：
  ```sql
  c.cadre_type AS cadreType
  ```

**是否专家（isExpert）**：
- 通过 LEFT JOIN `t_expert` 表，关联条件：`e.employee_number = exp.account`
- 如果 `exp.account` 存在，则 `isExpert = 1`，否则 `isExpert = 0`
- SQL示例：
  ```sql
  CASE 
      WHEN exp.account IS NOT NULL THEN 1 
      ELSE 0 
  END AS isExpert
  ```

**岗位AI成熟度（aiMaturity）**：
- **专家和干部是互斥关系**：如果员工是专家，就一定不会是干部；如果员工是干部，就一定不会是专家
- 如果员工是专家（`t_expert` 表中有记录）：从 `t_expert.position_ai_maturity` 获取
- 如果员工是干部（`t_cadre` 表中有记录）：从 `t_cadre.position_ai_maturity` 获取
- 如果员工既不是专家也不是干部：返回 `null`
- SQL示例：
  ```sql
  COALESCE(exp.position_ai_maturity, c.position_ai_maturity, NULL) AS aiMaturity
  ```

**是否通过科目二（isPassedSubject2）**：
- 通过 LEFT JOIN `t_exam_record` 表，关联条件：`e.employee_number = exam.emp_num`
- 查询条件：`exam.is_pass = 1` 且 `exam.exam_name IS NOT NULL`
- 如果存在记录，则 `isPassedSubject2 = 1`，否则 `isPassedSubject2 = 0`
- SQL示例：
  ```sql
  CASE 
      WHEN exam.emp_num IS NOT NULL THEN 1 
      ELSE 0 
  END AS isPassedSubject2
  ```

**是否认证达标（isCertStandard）**：
- 从 `t_cadre` 表的 `is_cert_standard` 字段获取（仅对干部有效）
- 如果员工不是干部，则返回 `null` 或 `0`
- SQL示例：
  ```sql
  COALESCE(c.is_cert_standard, 0) AS isCertStandard
  ```

#### 3.2.2 认证记录选择规则
- 如果同一员工有多条有效的AI专业级认证记录，应选择最新的记录（按 `start_time` 降序）
- 如果 `start_time` 相同，选择任意一条即可

#### 3.2.3 字段映射

| EmployeeDetailVO字段 | 数据来源 | 说明 |
|---------------------|---------|------|
| name | t_employee_sync.last_name | 姓名 |
| employeeNumber | t_employee_sync.employee_number | 工号 |
| competenceCategory | t_employee_sync.job_category（提取中间部分） | 职位类 |
| departname2 | t_employee_sync.firstdept（提取第一个'/'前的部分） | 一级部门名称 |
| departname3 | t_employee_sync.seconddept（提取第一个'/'前的部分） | 二级部门名称 |
| departname4 | t_employee_sync.thirddept（提取第一个'/'前的部分） | 三级部门名称 |
| departname5 | t_employee_sync.fourthdept（提取第一个'/'前的部分） | 四级部门名称 |
| departname6 | t_employee_sync.fifthdept（提取第一个'/'前的部分） | 五级部门名称 |
| departname7 | t_employee_sync.sixthdept | 六级部门名称 |
| certTitle | dwr_t_cert_record_t.cer_title | 证书标题 |
| certStartTime | dwr_t_cert_record_t.start_time | 证书开始时间 |
| isPassedSubject2 | t_exam_record.emp_num（如果存在则为1，否则为0） | 是否通过科目二 |
| isCadre | t_cadre.account（如果存在则为1，否则为0） | 是否为干部 |
| cadreType | t_cadre.cadre_type | 干部类型 |
| isCertStandard | t_cadre.is_cert_standard | 是否认证达标（仅对干部有效） |
| miniDeptName | department_info_hrms.dept_name（通过sixthdeptcode关联） | 最小部门名称 |
| aiMaturity | 见下方说明 | 岗位AI成熟度 |
| isExpert | t_expert.account（如果存在则为1，否则为0） | 是否为专家 |

**aiMaturity（岗位AI成熟度）获取逻辑**：
- **专家和干部是互斥关系**：如果员工是专家，就一定不会是干部；如果员工是干部，就一定不会是专家
- 如果员工是专家（t_expert 表中有记录）：从 `t_expert.position_ai_maturity` 获取
- 如果员工是干部（t_cadre 表中有记录）：从 `t_cadre.position_ai_maturity` 获取
- 如果员工既不是专家也不是干部：返回 NULL

### 3.3 关键SQL参考

#### 新增：查询全员认证详细信息的SQL（EmployeeMapper.xml）

```xml
<!-- 根据部门层级和部门ID查询员工认证详细信息（全员类型） -->
<!-- 注意：当部门ID不为0时，只查询当前部门，不查询子部门 -->
<select id="getEmployeeCertDetailsByDeptLevel" resultType="com.huawei.aitransform.entity.EmployeeDetailVO">
    SELECT DISTINCT
        e.last_name AS name,
        e.employee_number AS employeeNumber,
        CASE 
            WHEN e.job_category LIKE '%-%-%' THEN SUBSTRING_INDEX(SUBSTRING_INDEX(e.job_category, '-', 2), '-', -1)
            WHEN e.job_category LIKE '%-%' THEN SUBSTRING_INDEX(e.job_category, '-', -1)
            ELSE e.job_category
        END AS competenceCategory,
        SUBSTRING_INDEX(e.firstdept, '/', 1) AS departname2,
        SUBSTRING_INDEX(e.seconddept, '/', 1) AS departname3,
        SUBSTRING_INDEX(e.thirddept, '/', 1) AS departname4,
        SUBSTRING_INDEX(e.fourthdept, '/', 1) AS departname5,
        SUBSTRING_INDEX(e.fifthdept, '/', 1) AS departname6,
        e.sixthdept AS departname7,
        cert.cer_title AS certTitle,
        cert.start_time AS certStartTime,
        CASE 
            WHEN exam.emp_num IS NOT NULL THEN 1 
            ELSE 0 
        END AS isPassedSubject2,
        CASE 
            WHEN c.account IS NOT NULL THEN 1 
            ELSE 0 
        END AS isCadre,
        c.cadre_type AS cadreType,
        COALESCE(c.is_cert_standard, 0) AS isCertStandard,
        CASE 
            WHEN exp.account IS NOT NULL THEN 1 
            ELSE 0 
        END AS isExpert,
        COALESCE(exp.position_ai_maturity, c.position_ai_maturity, NULL) AS aiMaturity,
        dept.dept_name AS miniDeptName,
        NULL AS isQualificationsStandard
    FROM t_employee_sync e
    <choose>
        <when test="queryType != null and queryType == 2">
            <!-- queryType=2: 使用 LEFT JOIN，返回所有人员，即使没有AI专业级证书也显示 -->
            LEFT JOIN (
                SELECT DISTINCT
                    COALESCE(employee_number, w3_account) AS employee_number,
                    w3_account,
                    cer_title,
                    start_time,
                    ROW_NUMBER() OVER (
                        PARTITION BY COALESCE(employee_number, w3_account)
                        ORDER BY start_time DESC
                    ) AS rn
                FROM dwr_t_cert_record_t
                WHERE (status = 1 OR approved_status = 1)
                AND (
                    cer_title = '华为研究类能力认证（专业级，AI算法技术）'
                    OR cer_title = '华为研究类能力认证（专业级，AI决策推理）'
                    OR cer_title = '华为研究类能力认证（专业级，AI图像语言语义）'
                )
            ) cert_ranked ON (
                (e.employee_number = cert_ranked.employee_number OR e.employee_number = cert_ranked.w3_account)
                AND cert_ranked.rn = 1
            )
            LEFT JOIN dwr_t_cert_record_t cert ON (
                (e.employee_number = cert.employee_number OR e.employee_number = cert.w3_account)
                AND cert.cer_title = cert_ranked.cer_title
                AND cert.start_time = cert_ranked.start_time
                AND (cert.status = 1 OR cert.approved_status = 1)
            )
        </when>
        <otherwise>
            <!-- queryType=1: 使用 INNER JOIN，只返回有AI专业级证书的人员 -->
            INNER JOIN (
                SELECT DISTINCT
                    COALESCE(employee_number, w3_account) AS employee_number,
                    w3_account,
                    cer_title,
                    start_time,
                    ROW_NUMBER() OVER (
                        PARTITION BY COALESCE(employee_number, w3_account)
                        ORDER BY start_time DESC
                    ) AS rn
                FROM dwr_t_cert_record_t
                WHERE (status = 1 OR approved_status = 1)
                AND (
                    cer_title = '华为研究类能力认证（专业级，AI算法技术）'
                    OR cer_title = '华为研究类能力认证（专业级，AI决策推理）'
                    OR cer_title = '华为研究类能力认证（专业级，AI图像语言语义）'
                )
            ) cert_ranked ON (
                (e.employee_number = cert_ranked.employee_number OR e.employee_number = cert_ranked.w3_account)
                AND cert_ranked.rn = 1
            )
            INNER JOIN dwr_t_cert_record_t cert ON (
                (e.employee_number = cert.employee_number OR e.employee_number = cert.w3_account)
                AND cert.cer_title = cert_ranked.cer_title
                AND cert.start_time = cert_ranked.start_time
                AND (cert.status = 1 OR cert.approved_status = 1)
            )
        </otherwise>
    </choose>
    LEFT JOIN (
        SELECT DISTINCT emp_num
        FROM t_exam_record
        WHERE exam_name IS NOT NULL
        AND exam_name != ''
        AND is_pass = 1
        AND emp_num IS NOT NULL
        AND emp_num != ''
    ) exam ON (e.employee_number = exam.emp_num)
    LEFT JOIN t_cadre c ON (e.employee_number = c.account)
    LEFT JOIN t_expert exp ON (e.employee_number = exp.account)
    LEFT JOIN department_info_hrms dept ON (
        e.sixthdeptcode = dept.dept_code 
        AND DATE(dept.update_time) = '2025-11-18'
    )
    WHERE e.period_id = 20251126
    AND e.employee_number IS NOT NULL
    AND e.employee_number != ''
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
    <!-- 注意：使用 = 精确匹配当前部门编码，不查询子部门 -->
    <if test="jobCategory != null and jobCategory != ''">
        <choose>
            <when test="jobCategory == '非软件类'">
                AND (
                    CASE 
                        WHEN e.job_category LIKE '%-%-%' THEN SUBSTRING_INDEX(SUBSTRING_INDEX(e.job_category, '-', 2), '-', -1)
                        WHEN e.job_category LIKE '%-%' THEN SUBSTRING_INDEX(e.job_category, '-', -1)
                        ELSE e.job_category
                    END
                ) != '软件类'
            </when>
            <when test="jobCategory == '其他类'">
                AND (
                    CASE 
                        WHEN e.job_category LIKE '%-%-%' THEN SUBSTRING_INDEX(SUBSTRING_INDEX(e.job_category, '-', 2), '-', -1)
                        WHEN e.job_category LIKE '%-%' THEN SUBSTRING_INDEX(e.job_category, '-', -1)
                        ELSE e.job_category
                    END
                ) NOT IN ('研究类', '软件类', '系统类', '测试类')
            </when>
            <otherwise>
                AND (
                    CASE 
                        WHEN e.job_category LIKE '%-%-%' THEN SUBSTRING_INDEX(SUBSTRING_INDEX(e.job_category, '-', 2), '-', -1)
                        WHEN e.job_category LIKE '%-%' THEN SUBSTRING_INDEX(e.job_category, '-', -1)
                        ELSE e.job_category
                    END
                ) = #{jobCategory}
            </otherwise>
        </choose>
    </if>
    ORDER BY e.employee_number, COALESCE(cert.start_time, '1900-01-01')
</select>
```

**说明**：
- 该SQL参考 `ExpertCertStatisticsMapper.getExpertCertDetailsByConditions` 的实现逻辑
- 区别在于：
  - **需要关联 `t_expert` 表**（LEFT JOIN）：用于判断是否为专家、获取专家岗位成熟度
  - **需要关联 `t_cadre` 表**（LEFT JOIN）：用于判断是否为干部、获取干部类型和干部认证达标情况
  - **需要关联 `t_exam_record` 表**（LEFT JOIN）：用于判断是否通过科目二
  - 不需要通过成熟度（`position_ai_maturity`）过滤（全员类型不按成熟度过滤）
  - 只通过 `period_id` 和部门字段过滤
  - 支持按职位类过滤（从 `job_category` 字段中提取）
  - **注意**：当部门ID不为0时，使用 `=` 精确匹配当前部门编码，而不是 `IN` 查询多个部门
  - **queryType参数处理**：
    - `queryType=1`：使用 INNER JOIN，只返回已通过AI专业级认证的员工
    - `queryType=2`：使用 LEFT JOIN，返回所有员工，如果有认证信息则一并返回
  - **认证记录选择逻辑**：
    - 如果同一员工有多条认证记录，使用 `ROW_NUMBER()` 按 `start_time` 降序排序，选择最新的记录（rn=1）
  - **岗位成熟度获取逻辑**：
    - **专家和干部是互斥关系**：如果员工是专家，就一定不会是干部；如果员工是干部，就一定不会是专家
    - 如果员工是专家：从 `t_expert.position_ai_maturity` 获取
    - 如果员工是干部：从 `t_cadre.position_ai_maturity` 获取
    - 如果既不是专家也不是干部，返回 `null`

## 四、返回结果

### 4.1 返回数据结构

```json
{
    "code": 200,
    "message": "查询成功",
    "data": {
        "employeeDetails": [
            {
                "name": "张三",
                "employeeNumber": "123456",
                "competenceCategory": "软件类",
                "departname2": "一级部门",
                "departname3": "二级部门",
                "departname4": "三级部门",
                "departname5": "四级部门",
                "departname6": "五级部门",
                "departname7": "六级部门",
                "certTitle": "华为研究类能力认证（专业级，AI算法技术）",
                "certStartTime": "2024-01-01T00:00:00",
                "isPassedSubject2": 1,
                "isCadre": 0,
                "cadreType": null,
                "isCertStandard": 0,
                "isExpert": 1,
                "aiMaturity": "L2",
                "miniDeptName": "最小部门名称",
                "isQualificationsStandard": null
            }
        ]
    }
}
```

### 4.2 字段说明

| 字段名 | 类型 | 说明 |
|--------|------|------|
| employeeDetails | List | 员工认证详细信息列表 |
| name | String | 姓名 |
| employeeNumber | String | 工号 |
| competenceCategory | String | 职位类（从job_category字段提取） |
| departname2 | String | 一级部门名称 |
| departname3 | String | 二级部门名称 |
| departname4 | String | 三级部门名称 |
| departname5 | String | 四级部门名称 |
| departname6 | String | 五级部门名称 |
| departname7 | String | 六级部门名称 |
| certTitle | String | 证书标题（queryType=2时可能为null） |
| certStartTime | Date | 证书开始时间（queryType=2时可能为null） |
| isPassedSubject2 | Integer | 是否通过科目二（0-否，1-是） |
| isCadre | Integer | 是否为干部（0-否，1-是） |
| cadreType | String | 干部类型（如果不是干部则为null） |
| isCertStandard | Integer | 是否认证达标（仅对干部有效，0-否，1-是） |
| miniDeptName | String | 最小部门名称（通过sixthdeptcode查询） |
| aiMaturity | String | 岗位AI成熟度（如：L2、L3等，如果不是专家或干部则为null） |
| isExpert | Integer | 是否为专家（0-否，1-是） |
| isQualificationsStandard | Integer | 是否按要求任职达标（全员类型为null） |

### 4.3 全员数据查询的特殊说明

- **employeeDetails**: 包含所有符合条件的员工认证详细信息
- **认证记录选择**: 如果同一员工有多条有效的AI专业级认证记录，只返回最新的记录（按 `start_time` 降序）
- **数据一致性**: 查询的人员全集与 `employee-cert-statistics` 接口一致（当queryType=1时）

## 五、错误处理

### 5.1 错误码说明

| 错误码 | 说明 |
|--------|------|
| 400 | 参数错误（部门ID为空、人员类型为空或不支持、queryType不支持等） |
| 404 | 部门不存在 |
| 500 | 系统异常 |

### 5.2 错误响应示例

**示例1：查询类型参数错误**
```json
{
    "code": 400,
    "message": "查询类型参数错误，只支持1（认证人数）或2（基线人数）",
    "data": null
}
```

**示例2：不支持的人员类型**
```json
{
    "code": 400,
    "message": "不支持的人员类型：3，只支持0（全员）、1（干部）和2（专家）",
    "data": null
}
```

## 六、接口调用示例

### 6.1 查询全员认证数据（指定部门）

**请求示例**:
```
GET /expert-cert-statistics/person-cert-details?deptCode=123456&personType=0&queryType=1
```

**响应示例**:
```json
{
    "code": 200,
    "message": "查询成功",
    "data": {
        "employeeDetails": [
            {
                "name": "张三",
                "employeeNumber": "123456",
                "competenceCategory": "软件类",
                "departname2": "云核心网产品线",
                "departname3": "产品开发部",
                "departname4": "软件开发部",
                "departname5": "AI算法组",
                "departname6": null,
                "departname7": null,
                "certTitle": "华为研究类能力认证（专业级，AI算法技术）",
                "certStartTime": "2024-01-01T00:00:00",
                "isPassedSubject2": 1,
                "isCadre": 0,
                "cadreType": null,
                "isCertStandard": 0,
                "isExpert": 1,
                "aiMaturity": "L2",
                "miniDeptName": "AI算法组",
                "isQualificationsStandard": null
            }
        ]
    }
}
```

### 6.2 查询全员认证数据（按职位类过滤）

**请求示例**:
```
GET /expert-cert-statistics/person-cert-details?deptCode=123456&personType=0&queryType=1&jobCategory=软件类
```

### 6.3 查询全员基线数据（queryType=2，返回所有员工）

**请求示例**:
```
GET /expert-cert-statistics/person-cert-details?deptCode=123456&personType=0&queryType=2
```

**响应示例**:
```json
{
    "code": 200,
    "message": "查询成功",
    "data": {
        "employeeDetails": [
            {
                "name": "张三",
                "employeeNumber": "123456",
                "competenceCategory": "软件类",
                "departname2": "云核心网产品线",
                "departname3": "产品开发部",
                "departname4": "软件开发部",
                "departname5": "AI算法组",
                "departname6": null,
                "departname7": null,
                "certTitle": "华为研究类能力认证（专业级，AI算法技术）",
                "certStartTime": "2024-01-01T00:00:00",
                "isPassedSubject2": 1,
                "isCadre": 0,
                "cadreType": null,
                "isCertStandard": 0,
                "isExpert": 1,
                "aiMaturity": "L2",
                "miniDeptName": "AI算法组",
                "isQualificationsStandard": null
            },
            {
                "name": "李四",
                "employeeNumber": "123457",
                "competenceCategory": "软件类",
                "departname2": "云核心网产品线",
                "departname3": "产品开发部",
                "departname4": "软件开发部",
                "departname5": "AI算法组",
                "departname6": null,
                "departname7": null,
                "certTitle": null,
                "certStartTime": null,
                "isPassedSubject2": 0,
                "isCadre": 0,
                "cadreType": null,
                "isCertStandard": 0,
                "isExpert": 0,
                "aiMaturity": null,
                "miniDeptName": "AI算法组",
                "isQualificationsStandard": null
            }
        ]
    }
}
```

**说明**：当 `queryType=2` 时，返回所有员工信息。示例中：
- 张三有认证信息，返回完整的认证详细信息
- 李四没有认证信息，认证相关字段（certTitle、certStartTime等）为 null

### 6.4 查询全员认证数据（deptCode=0，使用云核心网产品线部门ID）

**请求示例**:
```
GET /expert-cert-statistics/person-cert-details?deptCode=0&personType=0&queryType=1
```

**说明**：当 `deptCode="0"` 时，会自动转换为云核心网产品线部门ID进行查询

## 七、实现要点

### 7.1 复用现有方法
- ✅ `EmployeeMapper.getEmployeeNumbersByDeptLevel()` - 查询员工工号列表（已存在）
- ✅ `ExpertCertStatisticsService.getCertifiedEmployeeNumbers()` - 查询认证人数（已存在）

### 7.2 需要新增的代码

#### Mapper层
- **EmployeeMapper接口**: 新增方法 `getEmployeeCertDetailsByDeptLevel()`
  ```java
  List<EmployeeDetailVO> getEmployeeCertDetailsByDeptLevel(
      @Param("deptLevel") Integer deptLevel,
      @Param("deptId") String deptId,
      @Param("jobCategory") String jobCategory,
      @Param("queryType") Integer queryType);
  ```
  **注意**：
  - 参数从 `deptIds`（列表）改为 `deptId`（单个部门编码），因为只查询当前部门
  - 新增 `queryType` 参数，用于控制查询逻辑（1-认证人数，2-基线人数）

- **EmployeeMapper.xml**: 新增SQL查询方法 `getEmployeeCertDetailsByDeptLevel`
  - 参考 `ExpertCertStatisticsMapper.getExpertCertDetailsByConditions` 的实现
  - 关联表：
    - `t_employee_sync` 表（主表）
    - `dwr_t_cert_record_t` 表（认证信息，根据 queryType 决定 JOIN 方式）
    - `t_exam_record` 表（LEFT JOIN，用于判断是否通过科目二）
    - `t_cadre` 表（LEFT JOIN，用于判断是否为干部、获取干部类型和干部认证达标情况）
    - `t_expert` 表（LEFT JOIN，用于判断是否为专家、获取专家岗位成熟度）
    - `department_info_hrms` 表（LEFT JOIN，获取最小部门名称）
  - 使用 `period_id = 20251126` 过滤
  - 根据部门层级使用对应的部门字段过滤（firstdeptcode、seconddeptcode等）
  - **使用 `=` 精确匹配当前部门编码**（不是 `IN` 查询）
  - 支持按职位类过滤（从 `job_category` 字段中提取）
  - **根据 queryType 参数决定 JOIN 方式**：
    - `queryType=1`：使用 INNER JOIN，只返回已通过AI专业级认证的员工
    - `queryType=2`：使用 LEFT JOIN，返回所有员工，如果有认证信息则一并返回
  - 选择每个员工的最新认证记录（如果有多条记录，按 `start_time` 降序）
  - **字段获取**：
    - `isCadre`：通过 `t_cadre.account` 判断（存在则为1，否则为0）
    - `cadreType`：从 `t_cadre.cadre_type` 获取
    - `isCertStandard`：从 `t_cadre.is_cert_standard` 获取（仅对干部有效）
    - `isExpert`：通过 `t_expert.account` 判断（存在则为1，否则为0）
    - `isPassedSubject2`：通过 `t_exam_record.emp_num` 判断（存在则为1，否则为0）
    - `aiMaturity`：从 `t_expert.position_ai_maturity` 或 `t_cadre.position_ai_maturity` 获取（专家和干部互斥，只能有一个有值）

#### Service层
- **ExpertCertStatisticsService.getPersonCertDetailsByConditions()**
  - 添加 `personType=0` 的处理逻辑
  - **参数校验**：
    - 如果 `queryType` 不为1且不为2，抛出异常："查询类型参数错误，只支持1（认证人数）或2（基线人数）"
    - 如果 `personType=0` 且传入了 `aiMaturity` 参数，忽略该参数（不按成熟度过滤）
  - **特殊处理：当 deptCode="0" 时**：
    - 将 `deptCode` 设置为云核心网产品线部门ID：`deptCode = DepartmentConstants.CLOUD_CORE_NETWORK_DEPT_CODE`
    - 继续执行普通处理流程
  - **普通处理**：
    - 查询当前部门信息，获取部门层级（`deptLevel`）
    - 直接使用当前部门的层级进行查询（`queryLevel = currentLevel`）
    - 调用 `EmployeeMapper.getEmployeeCertDetailsByDeptLevel(queryLevel, deptCode, jobCategory, queryType)` 查询当前部门的员工认证详细信息
    - 该方法内部已经处理了：
      - 按部门层级过滤（使用当前部门的层级）
      - 按职位类过滤（如果提供了jobCategory参数）
      - **根据 queryType 决定返回范围**：
        - `queryType=1`：只返回已通过认证的员工（INNER JOIN dwr_t_cert_record_t）
        - `queryType=2`：返回所有员工，如果有认证信息则一并返回（LEFT JOIN dwr_t_cert_record_t）
      - 选择每个员工的最新认证记录（如果有多条记录，按 `start_time` 降序）
  - **构建返回结果**：
    - 返回格式与干部/专家查询完全一致

#### Controller层
- **ExpertCertStatisticsController.getPersonCertDetailsByConditions()**
  - 修改参数校验，支持 `personType=0`
  - 错误提示更新为："不支持的人员类型：{personType}，只支持0（全员）、1（干部）和2（专家）"

### 7.3 注意事项

1. **queryType参数处理**: 
   - 全员类型支持 `queryType=1`（认证人数）和 `queryType=2`（基线人数）
   - `queryType=1`：只返回已通过AI专业级认证的员工详细信息（使用 INNER JOIN）
   - `queryType=2`：返回所有员工信息，如果有认证信息则一并返回（使用 LEFT JOIN）
   - 如果 `queryType` 不为1且不为2，应返回错误提示

2. **aiMaturity参数处理**: 
   - 全员类型不支持按成熟度过滤
   - 如果传入了 `aiMaturity` 参数，应忽略该参数（不报错，但不起作用）

3. **jobCategory参数处理**: 
   - 支持按职位类过滤
   - 需要从 `t_employee_sync.job_category` 字段中提取职位类进行匹配
   - 支持特殊值："非软件类"、"其他类"

4. **部门层级处理**: 
   - 普通情况下（deptCode不为"0"），直接查询当前部门，不查询子部门
   - 使用当前部门的层级（`queryLevel = currentLevel`）调用 `getEmployeeCertDetailsByDeptLevel()`
   - 注意：`queryLevel` 是当前部门的层级，用于确定查询 `t_employee_sync` 表的哪个部门字段
   - SQL中使用 `=` 精确匹配当前部门编码，而不是 `IN` 查询多个部门

5. **认证记录选择**: 
   - 如果同一员工有多条有效的AI专业级认证记录，应选择最新的记录（按 `start_time` 降序）
   - 使用 `ROW_NUMBER()` 窗口函数实现

6. **数据一致性**: 
   - 当 `queryType=1` 时，查询的人员全集与 `employee-cert-statistics` 接口一致（只返回已通过认证的员工）
   - 当 `queryType=2` 时，返回所有员工信息（不限制是否认证），与 `employee-cert-statistics` 接口的基线人数统计逻辑一致
   - 都使用 `period_id = 20251126` 过滤
   - 都使用相同的部门层级查询逻辑
   - 认证判断标准一致（AI专业级证书、证书状态等）

7. **空数据处理**: 
   - 如果当前部门下没有符合条件的员工，返回空列表

8. **特殊部门过滤**: 
   - 不需要过滤 "C Lab（模块）" 和 "云核心网产品组合与生命周期管理部"（与employee-cert-statistics接口不同，详细信息查询不过滤这些部门）

9. **认证标准**: 
   - 认证标准为华为研究类能力认证（专业级）的三种类型之一：
     - `华为研究类能力认证（专业级，AI算法技术）`
     - `华为研究类能力认证（专业级，AI决策推理）`
     - `华为研究类能力认证（专业级，AI图像语言语义）`
   - 证书状态：`status = 1 OR approved_status = 1`

## 八、测试建议

### 8.1 功能测试
1. 测试 `personType=0` 时，能正确查询全员认证数据
2. 测试不同部门层级的全员查询
3. 测试 `deptCode="0"` 时，能正确转换为云核心网产品线部门ID并查询
4. 测试按职位类过滤功能
5. 测试 `queryType=1` 时，只返回已通过认证的员工
6. 测试 `queryType=2` 时，返回所有员工信息（包括没有认证的员工）
7. 测试 `queryType=2` 时，有认证信息的员工返回认证详细信息，没有认证信息的员工认证相关字段为null
8. 测试传入 `aiMaturity` 参数时，忽略该参数（不报错）
9. 测试同一员工有多条认证记录时，是否正确选择最新的记录

### 8.2 边界测试
1. 测试 `deptCode` 为空或不存在的情况
2. 测试 `personType` 为无效值的情况
3. 测试部门下无员工时的返回结果
4. 测试 `deptCode="0"` 时，能正确转换为云核心网产品线部门ID
5. 测试同一员工有多条认证记录时，是否正确选择最新的记录

### 8.3 对比测试
1. 对比 `employee-cert-statistics` 接口查询的认证人数，确保一致性
2. 对比认证详细信息与 `dwr_t_cert_record_t` 表数据的一致性
3. 验证职位类过滤的准确性
4. 验证部门层级查询的准确性

### 8.4 性能测试
1. 测试大量子部门时的查询性能
2. 测试大量员工时的查询性能
3. 测试复杂职位类过滤时的查询性能

