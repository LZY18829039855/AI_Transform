# 部门课程完成率按职位类统计查询接口文档

## 1. 概述

本接口参考 **GET /personal-course/department-completion-rate**，在**同一部门**范围内按**职位类（job_category）**维度统计课程完成率。入参为**部门ID（deptId）**与**人员类型（personType）**，返回该部门下各职位类对应的统计数据（基线人数、基础/进阶/实战课程数、平均完课人数、平均完课率）。查询逻辑：先根据部门ID在 **t_employee_training_info** 中查询该**部门下所有人员**，再按 **job_category** 分组，分别统计每个职位类内的训战课程数据。数据来源与目标课程规则与部门完成率接口一致（目标课程以四级部门配置）。

---

## 2. 接口说明

| 项目         | 说明 |
|--------------|------|
| 接口名称     | 部门课程完成率按职位类统计查询 |
| 请求路径     | `GET /personal-course/department-completion-rate-by-job-category`（路径以实际项目约定为准） |
| 请求方式     | GET |
| 请求参数     | 部门ID（deptId）、人员类型（personType） |
| 是否需要认证 | 视现有接口策略而定 |

---

## 3. 请求参数

| 参数名     | 类型   | 参数位置 | 是否必填 | 说明 |
|------------|--------|----------|----------|------|
| deptId     | String | Query    | 是       | 部门ID（部门编码，与 department_info_hrms.dept_code 对应）。用于确定统计范围：在该部门下查询所有人员（按部门层级在 t_employee_training_info 中过滤，见 5.2）。 |
| personType | Integer| Query    | 否       | 人员类型；当前**仅处理 0**，其他值可由实现忽略或按 0 处理 |

**请求示例：**

```http
GET /personal-course/department-completion-rate-by-job-category?deptId=030681&personType=0 HTTP/1.1
Host: example.com
```

---

## 4. 响应参数（data 为列表）

- **data**：数组。每个元素对应一个**职位类**的统计；若该部门下无人员或无有效职位类，可为空数组 `[]`。
- 每个元素（单职位类统计）结构如下：

| 参数名                         | 类型    | 说明 |
|--------------------------------|---------|------|
| jobCategory                    | String  | 职位类（job_category 取值，如「软件类」「非软件类」等；空或 null 时可统一展示为「未分类」或约定文案） |
| baselineCount                  | Integer | 基线人数（该职位类在该部门下的人员数量） |
| basicCourseCount               | Integer | 基础课程数（目标课程中基础课程数量，见 5.3） |
| advancedCourseCount            | Integer | 进阶课程数（目标课程中进阶课程数量） |
| practicalCourseCount           | Integer | 实战课程数（目标课程中实战课程数量） |
| basicAvgCompletedCount         | Double  | 基础课程平均完课人数（计算方式见 5.4） |
| advancedAvgCompletedCount      | Double  | 进阶课程平均完课人数（计算方式见 5.4） |
| practicalAvgCompletedCount     | Double  | 实战课程平均完课人数（计算方式见 5.4） |
| basicAvgCompletionRate         | Double  | 基础课程平均完课率（百分比，计算方式见 5.5） |
| advancedAvgCompletionRate      | Double  | 进阶课程平均完课率（百分比，计算方式见 5.5） |
| practicalAvgCompletionRate     | Double  | 实战课程平均完课率（百分比，计算方式见 5.5） |

**响应示例（成功，部门下有两个职位类）：**

```json
{
  "code": 200,
  "message": "查询成功",
  "data": [
    {
      "jobCategory": "软件类",
      "baselineCount": 60,
      "basicCourseCount": 10,
      "advancedCourseCount": 5,
      "practicalCourseCount": 3,
      "basicAvgCompletedCount": 6.5,
      "advancedAvgCompletedCount": 2.0,
      "practicalAvgCompletedCount": 1.0,
      "basicAvgCompletionRate": 65.00,
      "advancedAvgCompletionRate": 40.00,
      "practicalAvgCompletionRate": 33.33
    },
    {
      "jobCategory": "非软件类",
      "baselineCount": 40,
      "basicCourseCount": 10,
      "advancedCourseCount": 5,
      "practicalCourseCount": 3,
      "basicAvgCompletedCount": 5.2,
      "advancedAvgCompletedCount": 1.5,
      "practicalAvgCompletedCount": 0.8,
      "basicAvgCompletionRate": 52.00,
      "advancedAvgCompletionRate": 37.50,
      "practicalAvgCompletionRate": 26.67
    }
  ]
}
```

**响应示例（成功，部门下无人员）：**

```json
{
  "code": 200,
  "message": "查询成功",
  "data": []
}
```

---

## 5. 查询与计算逻辑

### 5.1 根据部门ID查询部门信息与层级

- 从 **department_info_hrms** 表中根据入参 **deptId** 查询部门信息，获取 **dept_level**（及实现过滤所需的字段）。
- 若未查到部门，可返回 404 或 **data: []**，由实现与前端约定。

### 5.2 确定部门下人员范围

- **人员类型**：当前仅处理 **personType = 0**。
- **部门下人员**：在 **t_employee_training_info** 中，根据入参部门的**层级（dept_level）**与 **deptId**，按对应层级部门编码字段**等于** deptId 过滤，得到该**部门下所有人员**记录。
  - 约定与部门完成率接口一致：**仅统计本部门**（不含下级），即：
    - 一级部门：firstdeptcode = deptId
    - 二级部门：seconddeptcode = deptId
    - 三级部门：thirddeptcode = deptId
    - 四级部门：fourthdeptcode = deptId
    - 五级部门：fifthdeptcode = deptId
    - 六级部门：sixthdeptcode = deptId
- 若业务上「部门下」需包含下级部门，则需先根据 department_info_hrms 解析该部门及其所有下级部门编码，再在 t_employee_training_info 中用对应层级字段 IN 过滤；具体以产品约定为准。
- 上述记录按 **job_category** 分组，得到多个职位类，每个职位类内的人员用于后续统计。

### 5.3 目标课程与基础/进阶/实战课程数（部门统一）

- **目标课程**以**四级部门**为维度配置（与部门完成率接口一致，getDeptSelectionByDeptCode、dept_course_selections）。
- 本接口对**整个入参部门**使用**同一套目标课程**，用于所有职位类的「基础/进阶/实战课程数」及后续平均完课人数、完课率计算。确定方式建议：
  - **入参部门为四级部门**：直接使用该部门选课（getDeptSelectionByDeptCode(deptId)）；无选课或选课为空则目标课程视为**全部课程**。
  - **入参部门为二/三级部门**：可先解析该部门下所有四级部门，取这些四级部门目标课程的**并集**作为部门目标课程；若无则用全部课程。
  - **入参部门为五/六级部门**：使用该部门**对应父四级部门**的目标课程（沿 department_info_hrms 父链找到 dept_level='4' 的部门）。
- 在目标课程范围内，按 **course_level** 区分为基础、进阶、实战，得到 **basicCourseCount**、**advancedCourseCount**、**practicalCourseCount**（三个数对所有职位类一致）。

### 5.4 按职位类：平均完课人数（基础/进阶/实战）

- 对**每个职位类**分别计算：
  - **基础课程平均完课人数** = 该职位类内全员在**基础课程**上的**总完课数** / **basicCourseCount**（若 basicCourseCount 为 0，则比值按 0 或约定规则处理）。总完课数 = 该职位类内所有人员 basic_courses 解析后的课程 ID 数量之和。
  - **进阶课程平均完课人数**：同上，使用该职位类 **advanced_courses** 总完课数与 **advancedCourseCount**。
  - **实战课程平均完课人数**：同上，使用该职位类 **practical_courses** 总完课数与 **practicalCourseCount**。

### 5.5 按职位类：平均完课率（基础/进阶/实战）

- 对**每个职位类**分别计算：
  - **基础课程平均完课率** = (该职位类全员基础课程总完课数 / (该职位类 baselineCount × basicCourseCount)) × 100（百分比，建议保留 2 位小数）；分母为 0 时按 0 或约定处理。
  - **进阶/实战课程平均完课率**：同上，使用该职位类对应总完课数、baselineCount、advancedCourseCount / practicalCourseCount。

### 5.6 按职位类：基线人数

- **基线人数（baselineCount）**：每个职位类在 5.2 确定的人员范围内，按 **job_category** 分组后，该组的**人员记录数**。

---

## 6. 数据流程摘要

1. 入参校验：deptId 必填；personType 当前仅处理 0。
2. 根据 deptId 查询 department_info_hrms，得到部门层级。
3. 根据层级与 deptId 在 t_employee_training_info 中过滤出**该部门下所有人员**（personType=0，仅本部门或按约定含下级）。
4. 按 5.3 确定该部门使用的**目标课程**，得到部门统一的 basicCourseCount、advancedCourseCount、practicalCourseCount。
5. 将上述人员按 **job_category** 分组；对每个职位类统计 baselineCount、基础/进阶/实战总完课数（解析 basic_courses、advanced_courses、practical_courses 汇总）。
6. 按 5.4、5.5 计算每个职位类的平均完课人数与平均完课率，组装 data 列表返回。

---

## 7. 失败响应示例

- **部门不存在**：

```json
{
  "code": 404,
  "message": "部门不存在",
  "data": null
}
```

- **参数错误**（如 deptId 为空）：

```json
{
  "code": 400,
  "message": "部门ID不能为空",
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

| 表名 / 接口                | 用途 |
|----------------------------|------|
| department_info_hrms       | 按 deptId 查部门层级，用于确定 t_employee_training_info 过滤字段 |
| t_employee_training_info   | 按部门层级+deptId 过滤部门下人员，按 job_category 分组；读取 basic_courses、advanced_courses、practical_courses |
| dept_course_selections     | 四级部门选课，用于确定目标课程（与部门完成率接口一致） |
| ai_course_planning_info    | 无选课时作为「全部课程」来源，并区分基础/进阶/实战 |

---

## 9. 与 department-completion-rate 的对比

| 项目           | department-completion-rate           | department-completion-rate-by-job-category（本接口） |
|----------------|--------------------------------------|------------------------------------------------------|
| 统计维度       | 下一层级**部门**列表（每个部门一条） | 同一部门下按**职位类**分组（每个职位类一条）         |
| 人员范围       | 每个待统计部门**本部门**人员         | 入参部门下**本部门**所有人员（再按 job_category 分） |
| 目标课程       | 按部门/父四级分别确定                 | 部门统一一套目标课程                                 |
| 响应主键含义   | deptId / deptName                    | jobCategory                                         |

---

## 10. 待确认项（Check 时可与产品/实现约定）

1. **请求路径**：最终 URL（如是否与示例一致或放在其他前缀下）。
2. **部门下人员范围**：仅本部门（与 5.2 当前约定一致），还是包含所有下级部门人员。
3. **job_category 为空**：t_employee_training_info 中 job_category 为 null 或空字符串时，是否单独归为「未分类」一条，或合并到某类。
4. **除数为 0**：baselineCount、basicCourseCount 等为 0 时，平均完课人数与完课率返回 0 或 null 及前端展示约定。
5. **数值精度**：平均完课人数、完课率的小数位数（建议完课率保留 2 位小数）。
6. **入参 deptId = 0 或二级部门**：是否与部门完成率接口一致（如 0 视为二级部门 031562 下），仅影响「部门下人员」与「目标课程」的解析方式。

请你 check 上述文档，确认入参、响应字段、统计范围与目标课程逻辑后再进行开发。
