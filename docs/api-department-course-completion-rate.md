# 部门课程完成率查询接口文档

## 1. 概述

本接口用于查询**传入部门下一层级所有部门**的课程完成率统计信息。入参为**父部门ID（deptId）**，返回**待统计部门列表**中每个部门的统计（基线人数、基础/进阶/实战课程数、平均完课人数、平均完课率）。**待统计部门列表**与**目标课程**规则约定如下：**deptId 为 0 或二级部门ID（CLOUD_CORE_NETWORK_DEPT_CODE = "031562"）时，仅返回白名单内的四级部门**（当前为：047375、047374、043539、041852、038460、030699、038462、038461、047376，按此顺序返回，以节省接口时间）；**deptId 为三级部门时，返回其下一层级（四级）子部门**；**deptId 为四/五/六级部门时，返回其下一层级子部门**，且目标课程使用**对应的父四级部门**配置。实现时先根据 5.1 确定待统计部门列表（复用 **getLevel4DepartmentsByCodes** / **getChildDepartments** 等），再对每个部门单独统计本部门数据（不包含该部门下级）。数据来源于部门信息表 **department_info_hrms** 与全员训战课程表 **t_employee_training_info**，目标课程来源于四级部门选课（或全部课程）。

---

## 2. 接口说明

| 项目         | 说明 |
|--------------|------|
| 接口名称     | 部门课程完成率查询（下一层级子部门列表） |
| 请求路径     | `GET /personal-course/department-completion-rate` |
| 请求方式     | GET |
| 请求参数     | 部门ID（deptId，父部门）、人员类型（personType） |
| 是否需要认证 | 视现有接口策略而定 |

---

## 3. 请求参数

| 参数名     | 类型   | 参数位置 | 是否必填 | 说明 |
|------------|--------|----------|----------|------|
| deptId     | String | Query    | 是       | **父部门**ID（部门编码，与 department_info_hrms.dept_code 对应）。**为 0 或二级部门（031562）**时仅返回**白名单内四级部门**统计（047375、047374、043539、041852、038460、030699、038462、038461、047376，按此顺序）；**为三级部门**时返回其下一层级（四级）子部门统计；**为四/五/六级部门**时返回其下一层级子部门统计，目标课程使用对应**父四级部门**配置。见 5.1、5.3。 |
| personType | Integer| Query    | 否       | 人员类型；当前**仅处理 0**，其他值可由实现忽略或按 0 处理 |

**请求示例：**

```http
GET /personal-course/department-completion-rate?deptId=030681&personType=0 HTTP/1.1
Host: example.com
```

---

## 4. 响应参数（data 为列表）

- **data**：数组。元素个数 = 待统计部门个数（5.1）：deptId 为 0 或二级（031562）时 = 白名单内四级部门数（当前 9 个，按固定顺序）；为三级/四级/五级时 = 下一层级子部门数；为六级时 = 0，即 `[]`。
- 每个元素（单部门统计）结构如下：

| 参数名                         | 类型    | 说明 |
|--------------------------------|---------|------|
| deptId                         | String  | 部门ID（待统计部门的 dept_code） |
| deptName                       | String  | 部门名称（待统计部门的 dept_name） |
| baselineCount                  | Integer | 基线人数（该部门本部门人员数量，t_employee_training_info 记录数，不含该部门下级） |
| basicCourseCount               | Integer | 基础目标课程数（该部门所有人的 basic_target_courses_num 之和，即总目标课程数-基础，见 5.3） |
| advancedCourseCount            | Integer | 进阶目标课程数（该部门所有人的 advanced_target_courses_num 之和，即总目标课程数-进阶） |
| practicalCourseCount           | Integer | 实战目标课程数（该部门所有人的 practical_target_courses_num 之和，即总目标课程数-实战） |
| basicAvgCompletedCount         | Double  | 基础课程平均完课人数（计算方式见 5.4） |
| advancedAvgCompletedCount      | Double  | 进阶课程平均完课人数（计算方式见 5.4） |
| practicalAvgCompletedCount     | Double  | 实战课程平均完课人数（计算方式见 5.4） |
| basicAvgCompletionRate         | Double  | 基础课程平均完课率（百分比，计算方式见 5.5） |
| advancedAvgCompletionRate      | Double  | 进阶课程平均完课率（百分比，计算方式见 5.5） |
| practicalAvgCompletionRate     | Double  | 实战课程平均完课率（百分比，计算方式见 5.5） |

**响应示例（成功，父部门下有两个子部门）：**

```json
{
  "code": 200,
  "message": "查询成功",
  "data": [
    {
      "deptId": "030681001",
      "deptName": "子部门A",
      "baselineCount": 80,
      "basicCourseCount": 10,
      "advancedCourseCount": 5,
      "practicalCourseCount": 3,
      "basicAvgCompletedCount": 6.2,
      "advancedAvgCompletedCount": 1.8,
      "practicalAvgCompletedCount": 0.9,
      "basicAvgCompletionRate": 62.00,
      "advancedAvgCompletionRate": 36.00,
      "practicalAvgCompletionRate": 30.00
    },
    {
      "deptId": "030681002",
      "deptName": "子部门B",
      "baselineCount": 40,
      "basicCourseCount": 10,
      "advancedCourseCount": 5,
      "practicalCourseCount": 3,
      "basicAvgCompletedCount": 5.5,
      "advancedAvgCompletedCount": 2.1,
      "practicalAvgCompletedCount": 1.0,
      "basicAvgCompletionRate": 55.00,
      "advancedAvgCompletionRate": 42.00,
      "practicalAvgCompletionRate": 33.33
    }
  ]
}
```

**响应示例（成功，父部门下无子部门）：**

```json
{
  "code": 200,
  "message": "查询成功",
  "data": []
}
```

---

## 5. 查询与计算逻辑

### 5.1 根据父部门ID（deptId）确定返回的部门列表

目标课程以**四级部门**为维度配置。返回的统计列表按入参 **deptId** 分情况确定**待统计部门列表**（每个部门一条统计记录），规则如下：

| 入参 deptId | 返回的部门列表 | 说明 |
|-------------|----------------|------|
| **0** 或 **二级部门ID**（**CLOUD_CORE_NETWORK_DEPT_CODE = "031562"**） | **白名单内四级部门** | 仅返回指定四级部门（047375、047374、043539、041852、038460、030699、038462、038461、047376），按此顺序；每个四级部门一条统计，目标课程见 5.3。节省接口时间。 |
| **三级部门ID** | 该三级部门的**下一层级子部门**（四级部门） | 调用 **getChildDepartments(deptId)** 或现有 **children** 接口，得到四级子部门列表。 |
| **四级部门ID** | 该四级部门的**下一层级子部门**（五级部门） | 调用 **getChildDepartments(deptId)**，得到五级子部门列表；目标课程使用**该四级部门（入参）**的配置，见 5.3。 |
| **五级部门ID** | 该五级部门的**下一层级子部门**（六级部门） | 调用 **getChildDepartments(deptId)**，得到六级子部门列表；目标课程使用**每个六级部门对应的父四级部门**的配置，见 5.3。 |
| **六级部门ID** | 空列表 **[]** | 无下一层级，返回 **data: []**。 |

- 上述“白名单内四级部门”在 **deptId = 0** 或 **deptId = 031562** 时：仅取 **COMPLETION_RATE_LEVEL4_DEPT_CODES** 中定义的四级部门（**getLevel4DepartmentsByCodes**），并按该列表顺序返回。
- 待统计部门列表中的每个部门需包含 **dept_code**、**dept_name**、**dept_level**（及五/六级部门时用于查找“父四级”的层级信息）。
- 若根据 deptId 未查到父部门或得到的部门列表为空，返回 **data: []**（deptId 无效时也可返回 400/404，由实现约定）。

### 5.2 对每个待统计部门确定统计范围内的人员完课数据

- **统计范围**：对**每一个待统计部门**分别统计，**仅统计该部门本部门**，不包含该部门的下级。
- **人员类型**：当前仅处理 **personType = 0**（如“全员”或默认类型），其他值可按 0 处理或忽略。
- 对当前部门，根据其 **dept_level** 与 **dept_code**，在 **t_employee_training_info** 表中筛选**该部门**的人员记录：按层级使用对应层级部门编码字段**等于**该部门 dept_code。例如：
  - 一级：firstdeptcode = 该部门 dept_code
  - 二级：seconddeptcode = 该部门 dept_code
  - 三级：thirddeptcode = 该部门 dept_code
  - 四级：fourthdeptcode = 该部门 dept_code
  - 五级：fifthdeptcode = 该部门 dept_code
  - 六级：sixthdeptcode = 该部门 dept_code
- 查询该部门人员时，除 **basic_courses**、**advanced_courses**、**practical_courses** 外，还需查询每人对应的 **basic_target_courses_num**、**advanced_target_courses_num**、**practical_target_courses_num**（分别表示该人员的基础、进阶、实战目标课程数）。
- 上述过滤得到的记录集合，用于该部门的**基线人数**、**总完课数**、**总目标课程数**等统计。

### 5.3 目标课程数与基础/进阶/实战目标课程数（按待统计部门）

- 查询 **t_employee_training_info**（或承载训战数据的表）时，需**一并查询**每条人员记录的：
  - **basic_target_courses_num**：该人员的基础目标课程数  
  - **advanced_target_courses_num**：该人员的进阶目标课程数  
  - **practical_target_courses_num**：该人员的实战目标课程数  

- 对**每个待统计部门**，基础/进阶/实战目标课程数取该部门**所有人员对应字段之和**，作为响应中的课程数及后续计算分母：
  - **basicCourseCount**（基础目标课程数）= 该部门所有人 **basic_target_courses_num** 之和（即**总目标课程数-基础**）
  - **advancedCourseCount**（进阶目标课程数）= 该部门所有人 **advanced_target_courses_num** 之和（即**总目标课程数-进阶**）
  - **practicalCourseCount**（实战目标课程数）= 该部门所有人 **practical_target_courses_num** 之和（即**总目标课程数-实战**）

- 若表中无上述三个字段，需在实现时通过扩展表结构或从其他数据源补齐；具体表结构以实际库表为准。

### 5.4 平均完课人数（基础/进阶/实战，按待统计部门）

- 对**每个待统计部门**计算时，先算**总完课数**与**总目标课程数**：
  - **总完课数（基础）** = 该部门所有人 **basic_courses** 列表数量的总和（即每人 basic_courses 解析后的课程 ID 个数相加）。
  - **总目标课程数（基础）** = 该部门所有人 **basic_target_courses_num** 之和（即 5.3 中的 **basicCourseCount**）。
  - **总完课数（进阶）** = 该部门所有人 **advanced_courses** 列表数量的总和。
  - **总目标课程数（进阶）** = 该部门所有人 **advanced_target_courses_num** 之和（即 **advancedCourseCount**）。
  - **总完课数（实战）** = 该部门所有人 **practical_courses** 列表数量的总和。
  - **总目标课程数（实战）** = 该部门所有人 **practical_target_courses_num** 之和（即 **practicalCourseCount**）。

- **平均完课人数**（总目标课程数为 0 时可按 0 或约定规则处理）：
  - **基础课程平均完课人数** = **总完课数（基础）** / **总目标课程数（基础）**。
  - **进阶课程平均完课人数** = **总完课数（进阶）** / **总目标课程数（进阶）**。
  - **实战课程平均完课人数** = **总完课数（实战）** / **总目标课程数（实战）**。

### 5.5 平均完课率（基础/进阶/实战，按待统计部门）

- 对**每个待统计部门**计算（总目标课程数为 0 时可按 0 或约定规则处理）：
  - **基础课程平均完课率** = (**总完课数（基础）** / **总目标课程数（基础）**) × 100（单位：百分比，建议保留 2 位小数）。其中总完课数（基础）、总目标课程数（基础）与 5.4 一致。
  - **进阶课程平均完课率** = (**总完课数（进阶）** / **总目标课程数（进阶）**) × 100。
  - **实战课程平均完课率** = (**总完课数（实战）** / **总目标课程数（实战）**) × 100。

### 5.6 基线人数（按待统计部门）

- **基线人数（baselineCount）**：对每个待统计部门，在 t_employee_training_info 中按 5.2 确定的**该部门**范围与人员类型（当前仅 personType=0）统计得到的**人员记录数**（即该部门本部门成员数，不含该部门下级）。

---

## 6. 数据流程摘要

1. 入参校验：deptId 必填（父部门）；personType 当前仅处理 0。
2. 根据 **deptId** 按 5.1 确定**待统计部门列表**：
   - **deptId = 0 或 二级部门ID（031562）**：仅查询**白名单内四级部门**（**getLevel4DepartmentsByCodes(COMPLETION_RATE_LEVEL4_DEPT_CODES)**），按固定顺序返回。
   - **deptId = 三级**：**getChildDepartments(deptId)** → 四级子部门列表。
   - **deptId = 四级**：**getChildDepartments(deptId)** → 五级子部门列表。
   - **deptId = 五级**：**getChildDepartments(deptId)** → 六级子部门列表。
   - **deptId = 六级**：待统计列表为空，返回 **data: []**。
3. **对每个待统计部门**循环：
   - 根据该部门的 dept_level、dept_code 在 t_employee_training_info 中过滤出**该部门**人员（personType=0，仅该部门不含其下级），并查询每条记录的 basic_courses、advanced_courses、practical_courses 以及 **basic_target_courses_num**、**advanced_target_courses_num**、**practical_target_courses_num**。
   - 按 5.3 计算该部门基础/进阶/实战目标课程数：basicCourseCount = 所有人 basic_target_courses_num 之和，advancedCourseCount、practicalCourseCount 同理。
   - 统计该部门总完课数：总完课数（基础）= 所有人 basic_courses 列表数量之和，进阶、实战同理。
   - 按 5.4、5.5 用总完课数与总目标课程数计算该部门平均完课人数与平均完课率，组装一条结果加入列表。
4. 返回 **data** 为上述待统计部门统计列表（数组）。

---

## 7. 失败响应示例

- **父部门不存在**（如 deptId 在 department_info_hrms 中无记录）：

```json
{
  "code": 404,
  "message": "父部门不存在",
  "data": null
}
```

- **参数错误**（如 deptId 为空）：

```json
{
  "code": 400,
  "message": "父部门ID不能为空",
  "data": null
}
```

- **系统异常**：

```json
{
  "code": 500,
  "message": "系统异常：具体错误信息",
  "data": null
}
```

---

## 8. 依赖表与说明

| 表名 / 接口 / 常量                        | 用途 |
|------------------------------------------|------|
| department_info_hrms                     | 部门信息与层级；getChildDepartments 通过 parent_dept_code 查下一层级子部门；五/六级部门查父四级时用层级链 |
| getChildDepartments / children           | 根据父部门ID查询**下一层级子部门**列表（已存在接口/方法） |
| getLevel4DepartmentsByCodes              | deptId 为 0 或 031562 时，按白名单查询**指定四级部门**（COMPLETION_RATE_LEVEL4_DEPT_CODES），节省时间 |
| CLOUD_CORE_NETWORK_DEPT_CODE（031562）   | 二级部门常量；deptId 为该值或 0 时，返回白名单内四级部门统计（COMPLETION_RATE_LEVEL4_DEPT_CODES） |
| t_employee_training_info                 | 全员训战课程；按待统计部门层级字段过滤**该部门**人员（不含其下级）；读取 basic_courses、advanced_courses、practical_courses 以及 **basic_target_courses_num**、**advanced_target_courses_num**、**practical_target_courses_num**（每人目标课程数，用于汇总为部门总目标课程数及平均完课人数/完课率计算） |

---

## 9. 待确认项（Check 时可与产品/实现约定）

1. **请求路径**：最终 URL 与项目路由规范（如是否放在 /external-api 或其它前缀下）。
2. **deptId = 0 时“所有四级部门”范围**：全系统四级部门，还是某业务根下的四级部门（与现有接口约定一致即可）。
3. **人员类型**：personType 未来扩展值及对应过滤规则（如是否关联 t_employee_sync 等）。
4. **除数为 0**：baselineCount、basicCourseCount 等为 0 时，平均完课人数与完课率返回 0 或 null、以及前端展示约定。
5. **数值精度**：平均完课人数、完课率的小数位数（建议完课率保留 2 位小数）。

请你 check 上述文档，确认入参、响应字段、计算逻辑及待确认项后再进行代码修改。
