# 部门课程完成率查询接口文档

## 1. 概述

本接口用于查询**传入部门下一层级所有部门**的课程完成率统计信息。入参为**父部门ID（deptId）**，返回**待统计部门列表**中每个部门的统计（基线人数、基础/进阶/实战课程数、平均完课人数、平均完课率）。**待统计部门列表**与**目标课程**规则约定如下：**deptId 为 0 或二级部门ID（CLOUD_CORE_NETWORK_DEPT_CODE = "031562"）时，返回所有四级部门**；**deptId 为三级部门时，返回其下一层级（四级）子部门**；**deptId 为四/五/六级部门时，返回其下一层级子部门**，且目标课程使用**对应的父四级部门**配置。实现时先根据 5.1 确定待统计部门列表（复用现有 **children** / **getChildDepartments** 或 **getLevel4DepartmentsUnderLevel2** 等），再对每个部门单独统计本部门数据（不包含该部门下级）。数据来源于部门信息表 **department_info_hrms** 与全员训战课程表 **t_employee_training_info**，目标课程来源于四级部门选课（或全部课程）。

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
| deptId     | String | Query    | 是       | **父部门**ID（部门编码，与 department_info_hrms.dept_code 对应）。**为 0 或二级部门**时返回**所有四级部门**统计；**为三级部门**时返回其下一层级（四级）子部门统计；**为四/五/六级部门**时返回其下一层级子部门统计，目标课程使用对应**父四级部门**配置。二级部门特例：**CLOUD_CORE_NETWORK_DEPT_CODE（031562）** 与 **0** 均按“所有四级部门”处理，见 5.1、5.3。 |
| personType | Integer| Query    | 否       | 人员类型；当前**仅处理 0**，其他值可由实现忽略或按 0 处理 |

**请求示例：**

```http
GET /personal-course/department-completion-rate?deptId=030681&personType=0 HTTP/1.1
Host: example.com
```

---

## 4. 响应参数（data 为列表）

- **data**：数组。元素个数 = 待统计部门个数（5.1）：deptId 为 0 或二级时 = 所有四级部门数；为三级/四级/五级时 = 下一层级子部门数；为六级时 = 0，即 `[]`。
- 每个元素（单部门统计）结构如下：

| 参数名                         | 类型    | 说明 |
|--------------------------------|---------|------|
| deptId                         | String  | 部门ID（待统计部门的 dept_code） |
| deptName                       | String  | 部门名称（待统计部门的 dept_name） |
| baselineCount                  | Integer | 基线人数（该部门本部门人员数量，t_employee_training_info 记录数，不含该部门下级） |
| basicCourseCount               | Integer | 基础目标课程数（来源于表字段 **basic_target_courses_num**，见 5.3） |
| advancedCourseCount            | Integer | 进阶目标课程数（来源于表字段 **advanced_target_courses_num**，见 5.3） |
| practicalCourseCount           | Integer | 实战目标课程数（来源于表字段 **practical_target_courses_num**，见 5.3） |
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
| **0** 或 **二级部门ID**（如 **CLOUD_CORE_NETWORK_DEPT_CODE = "031562"**） | **所有四级部门** | 返回全量或该二级下的所有四级部门；每个四级部门一条统计，目标课程见 5.3。 |
| **三级部门ID** | 该三级部门的**下一层级子部门**（四级部门） | 调用 **getChildDepartments(deptId)** 或现有 **children** 接口，得到四级子部门列表。 |
| **四级部门ID** | 该四级部门的**下一层级子部门**（五级部门） | 调用 **getChildDepartments(deptId)**，得到五级子部门列表；目标课程使用**该四级部门（入参）**的配置，见 5.3。 |
| **五级部门ID** | 该五级部门的**下一层级子部门**（六级部门） | 调用 **getChildDepartments(deptId)**，得到六级子部门列表；目标课程使用**每个六级部门对应的父四级部门**的配置，见 5.3。 |
| **六级部门ID** | 空列表 **[]** | 无下一层级，返回 **data: []**。 |

- 上述“所有四级部门”在 **deptId = 0** 时：取系统/业务约定范围内的全部四级部门（如全表 dept_level='4' 或与现有类似接口一致）；在 **deptId = 二级部门ID（031562）** 时：取该二级部门下的所有四级部门（如现有 **getLevel4DepartmentsUnderLevel2(level2DeptCode)**）。
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
- 上述过滤得到的记录集合，用于该部门的**基线人数**、**总完课数**等统计。

### 5.3 目标课程与基础/进阶/实战课程数（按待统计部门）

- 查询 **department-completion-rate 相关表**（如 t_employee_training_info 或部门维度目标课程表）时，应查询以下字段，作为**基础、进阶、实战目标课程数**，并用于 5.4 平均完课人数与 5.5 平均完课率的计算：
  - **basic_target_courses_num** → 对应响应中的 **basicCourseCount**（基础目标课程数）
  - **advanced_target_courses_num** → 对应响应中的 **advancedCourseCount**（进阶目标课程数）
  - **practical_target_courses_num** → 对应响应中的 **practicalCourseCount**（实战目标课程数）
- 上述三字段按**待统计部门**对应的目标课程来源获取（部门层级与目标课程来源规则见下表）。若表中无该三字段，则按原逻辑从四级部门选课与 **ai_course_planning_info** 统计得到基础/进阶/实战课程数作为 fallback。

| 待统计部门层级 | 目标课程来源 | 说明 |
|----------------|--------------|------|
| **四级部门** | 该四级部门自己的选课 | 入参为 **0、二级或三级**时，返回列表为四级部门；对每个四级部门查询其对应的 **basic_target_courses_num**、**advanced_target_courses_num**、**practical_target_courses_num**（或调用 `getDeptSelectionByDeptCode(该四级 dept_code)` 后统计）。若无选课或选课为空，则目标课程视为**全部课程**（与现有个人完课接口 fallback 一致）。 |
| **五级部门** | **父四级部门**的选课 | 入参为**四级部门**时，返回列表为五级子部门；目标课程数使用**入参四级部门**对应的上述三字段（或 `getDeptSelectionByDeptCode(入参 deptId)` 后统计）。 |
| **六级部门** | **对应的父四级部门**的选课 | 入参为**五级部门**时，返回列表为六级子部门；对每个六级部门，根据 department_info_hrms 找到其**祖先四级部门**，再查询该四级部门对应的 **basic_target_courses_num**、**advanced_target_courses_num**、**practical_target_courses_num**（或 getDeptSelectionByDeptCode）。若无选课或选课为空，则目标课程视为**全部课程**。 |

- 实现时优先使用表中 **basic_target_courses_num**、**advanced_target_courses_num**、**practical_target_courses_num** 作为该部门的基础/进阶/实战目标课程数，供 5.4、5.5 计算使用。

### 5.4 平均完课人数（基础/进阶/实战，按待统计部门）

- 对**每个待统计部门**计算（分母使用从表中查询的 **basic_target_courses_num**、**advanced_target_courses_num**、**practical_target_courses_num**，即响应中的 basicCourseCount、advancedCourseCount、practicalCourseCount）：
  - **基础课程平均完课人数**：
    - 分子：该部门全员在**基础课程**上的**总完课数**（即 t_employee_training_info 中该部门范围内，每人 basic_courses 解析后的完课门数之和）。
    - 分母：该部门**基础目标课程数**（**basic_target_courses_num**，即 basicCourseCount）（若为 0，则比值可按 0 或约定规则处理）。
    - **基础课程平均完课人数 = 该部门全员基础课程总完课数 / basic_target_courses_num**。
  - **进阶课程平均完课人数**：同上，使用该部门 **advanced_courses** 总完课数与 **advanced_target_courses_num**（advancedCourseCount）。
  - **实战课程平均完课人数**：同上，使用该部门 **practical_courses** 总完课数与 **practical_target_courses_num**（practicalCourseCount）。

### 5.5 平均完课率（基础/进阶/实战，按待统计部门）

- 对**每个待统计部门**计算（分母中的目标课程数使用表中 **basic_target_courses_num**、**advanced_target_courses_num**、**practical_target_courses_num**）：
  - **基础课程平均完课率**：
    - 分子：该部门全员在基础课程上的**总完课数**（与 5.4 中分子一致）。
    - 分母：**该部门成员数 × 该部门基础目标课程数** = **baselineCount × basic_target_courses_num**（即 baselineCount × basicCourseCount）（若为 0，则完课率可按 0 或约定规则处理）。
    - **基础课程平均完课率 = (该部门全员基础课程总完课数 / (基线人数 × basic_target_courses_num)) × 100**（单位：百分比，建议保留 2 位小数）。
  - **进阶/实战课程平均完课率**：同上，使用该部门对应总完课数、baselineCount、**advanced_target_courses_num** / **practical_target_courses_num**（即 advancedCourseCount / practicalCourseCount）。

### 5.6 基线人数（按待统计部门）

- **基线人数（baselineCount）**：对每个待统计部门，在 t_employee_training_info 中按 5.2 确定的**该部门**范围与人员类型（当前仅 personType=0）统计得到的**人员记录数**（即该部门本部门成员数，不含该部门下级）。

---

## 6. 数据流程摘要

1. 入参校验：deptId 必填（父部门）；personType 当前仅处理 0。
2. 根据 **deptId** 按 5.1 确定**待统计部门列表**：
   - **deptId = 0 或 二级部门ID（031562）**：查询**所有四级部门**（deptId=0 时全量四级；deptId=031562 时可用 **getLevel4DepartmentsUnderLevel2("031562")**）。
   - **deptId = 三级**：**getChildDepartments(deptId)** → 四级子部门列表。
   - **deptId = 四级**：**getChildDepartments(deptId)** → 五级子部门列表。
   - **deptId = 五级**：**getChildDepartments(deptId)** → 六级子部门列表。
   - **deptId = 六级**：待统计列表为空，返回 **data: []**。
3. **对每个待统计部门**循环：
   - 根据该部门的 dept_level、dept_code 在表中过滤出**该部门**人员（personType=0，仅该部门不含其下级），得到该部门基线人数与各人员 basic_courses、advanced_courses、practical_courses；**并查询该部门对应的 basic_target_courses_num、advanced_target_courses_num、practical_target_courses_num**（即基础/进阶/实战目标课程数）。
   - 若表中有上述三字段则直接使用，否则按 5.3 从四级部门选课等逻辑得到该部门基础/进阶/实战课程数。
   - 统计该部门全员在基础/进阶/实战上的总完课数（解析 basic_courses、advanced_courses、practical_courses 汇总）。
   - 按 5.4、5.5 使用 **basic_target_courses_num**、**advanced_target_courses_num**、**practical_target_courses_num** 计算该部门平均完课人数与平均完课率，组装一条结果加入列表。
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
| getLevel4DepartmentsUnderLevel2          | deptId 为二级部门（如 031562）时，查询该二级下的**所有四级部门**（已存在 Mapper 方法） |
| CLOUD_CORE_NETWORK_DEPT_CODE（031562）   | 二级部门常量；deptId 为该值或 0 时，返回所有四级部门统计 |
| t_employee_training_info（或部门完成率相关表） | 全员训战课程；按待统计部门层级字段过滤**该部门**人员（不含其下级），读取 basic_courses、advanced_courses、practical_courses；**并查询 basic_target_courses_num、advanced_target_courses_num、practical_target_courses_num** 作为基础/进阶/实战目标课程数，供平均完课人数与完课率计算使用 |
| dept_course_selections                  | 四级部门选课；getDeptSelectionByDeptCode 得到目标课程 ID 列表（目标课程以四级部门配置） |
| ai_course_planning_info                 | 课程规划；无选课时作为“全部课程”来源，并区分基础/进阶/实战 |

---

## 9. 待确认项（Check 时可与产品/实现约定）

1. **请求路径**：最终 URL 与项目路由规范（如是否放在 /external-api 或其它前缀下）。
2. **deptId = 0 时“所有四级部门”范围**：全系统四级部门，还是某业务根下的四级部门（与现有接口约定一致即可）。
3. **人员类型**：personType 未来扩展值及对应过滤规则（如是否关联 t_employee_sync 等）。
4. **除数为 0**：baselineCount、basicCourseCount 等为 0 时，平均完课人数与完课率返回 0 或 null、以及前端展示约定。
5. **数值精度**：平均完课人数、完课率的小数位数（建议完课率保留 2 位小数）。

请你 check 上述文档，确认入参、响应字段、计算逻辑及待确认项后再进行代码修改。
