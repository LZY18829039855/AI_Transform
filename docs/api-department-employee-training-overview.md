# 部门全员训战总览（下钻）接口文档

## 1. 概述

本接口用于**部门维度训战数据的下钻**，展示指定部门下**全员**的训战总览数据。入参为**部门ID（deptId）**与**人员类型（personType）**，返回该部门下**所有人员**的明细列表，每人一条记录，包含姓名、工号、职位类、职位子类、各级部门、基础/进阶目标课程数、完课数、完课占比及汇总数据。数据来源于训战课程表 **t_employee_training_info**，按部门ID过滤本部门人员（不含下级），完课数由完课字段（逗号分隔的课程ID列表）解析计算，完课占比由完课数与目标课程数比值计算，总目标课程数为各目标课程数之和。

---

## 2. 接口说明

| 项目         | 说明 |
|--------------|------|
| 接口名称     | 部门全员训战总览（下钻） |
| 实现类       | **PersonalCourseCompletionController**（与部门课程完成率等接口同控制器，统一前缀 `/personal-course`） |
| 请求路径     | `GET /personal-course/department-employee-training-overview` |
| 请求方式     | GET |
| 请求参数     | 部门ID（deptId）、人员类型（personType） |
| 是否需要认证 | 视现有接口策略而定 |

---

## 3. 请求参数

| 参数名     | 类型   | 参数位置 | 是否必填 | 说明 |
|------------|--------|----------|----------|------|
| deptId     | String | Query    | 是       | 部门ID（部门编码，与 t_employee_training_info 中对应层级部门编码字段一致）。用于确定统计范围：在该部门下查询**本部门全员**（按部门层级在 t_employee_training_info 中过滤，见 5.2）。 |
| personType | Integer| Query    | 否       | 人员类型；当前**仅处理 0**，其他值可由实现忽略或按 0 处理 |

**请求示例：**

```http
GET /personal-course/department-employee-training-overview?deptId=030681&personType=0 HTTP/1.1
Host: example.com
```

---

## 4. 响应参数（data 为列表）

- **data**：数组。每个元素对应该部门下**一名员工**的训战总览；若该部门下无人员，则为空数组 `[]`。
- 每个元素（单人员）结构如下：

| 参数名                         | 类型    | 说明 |
|--------------------------------|---------|------|
| name                           | String  | 姓名（t_employee_training_info.last_name） |
| employeeNumber                 | String  | 工号（t_employee_training_info.employee_number） |
| jobCategory                    | String  | 职位类（t_employee_training_info.job_category） |
| jobSubcategory                 | String  | 职位子类（t_employee_training_info.job_subcategory） |
| firstDept                      | String  | 一级部门（t_employee_training_info.firstdept） |
| secondDept                     | String  | 二级部门（t_employee_training_info.seconddept） |
| thirdDept                      | String  | 三级部门（t_employee_training_info.thirddept） |
| fourthDept                     | String  | 四级部门（t_employee_training_info.fourthdept） |
| fifthDept                      | String  | 五级部门（t_employee_training_info.fifthdept） |
| lowestDept                     | String  | 最小部门（t_employee_training_info.lowestdept） |
| basicTargetCourseCount         | Integer | 基础目标课程数（该人员 basic_target_courses_num） |
| basicCompletedCount            | Integer | 基础目标课程完课数（由 basic_courses 逗号分割后的列表长度计算，见 5.3） |
| basicCompletionRate            | Double  | 基础目标课程完课占比（百分比，见 5.4） |
| advancedTargetCourseCount      | Integer | 进阶目标课程数（该人员 advanced_target_courses_num） |
| advancedCompletedCount         | Integer | 进阶目标课程完课数（由 advanced_courses 逗号分割后的列表长度计算，见 5.3） |
| advancedCompletionRate        | Double  | 进阶目标课程完课占比（百分比，见 5.4） |
| totalTargetCourseCount         | Integer | 总目标课程数（基础 + 进阶 目标课程数之和；若表含实战则可为 基础+进阶+实战，见 5.5） |
| totalCompletedCount            | Integer | 目标课程完课数（基础完课数 + 进阶完课数；若含实战则加上实战完课数，见 5.5） |
| totalCompletionRate            | Double  | 目标课程完课占比（百分比，见 5.4） |

**响应示例（成功，部门下有两名员工）：**

```json
{
  "code": 200,
  "message": "查询成功",
  "data": [
    {
      "name": "张三",
      "employeeNumber": "12345678",
      "jobCategory": "软件类",
      "jobSubcategory": "开发",
      "firstDept": "一级部门A",
      "secondDept": "二级部门A",
      "thirdDept": "三级部门A",
      "fourthDept": "四级部门A",
      "fifthDept": "五级部门A",
      "lowestDept": "最小部门A",
      "basicTargetCourseCount": 10,
      "basicCompletedCount": 6,
      "basicCompletionRate": 60.00,
      "advancedTargetCourseCount": 5,
      "advancedCompletedCount": 2,
      "advancedCompletionRate": 40.00,
      "totalTargetCourseCount": 15,
      "totalCompletedCount": 8,
      "totalCompletionRate": 53.33
    },
    {
      "name": "李四",
      "employeeNumber": "87654321",
      "jobCategory": "软件类",
      "jobSubcategory": "测试",
      "firstDept": "一级部门A",
      "secondDept": "二级部门A",
      "thirdDept": "三级部门A",
      "fourthDept": "四级部门A",
      "fifthDept": "",
      "lowestDept": "四级部门A",
      "basicTargetCourseCount": 10,
      "basicCompletedCount": 8,
      "basicCompletionRate": 80.00,
      "advancedTargetCourseCount": 5,
      "advancedCompletedCount": 3,
      "advancedCompletionRate": 60.00,
      "totalTargetCourseCount": 15,
      "totalCompletedCount": 11,
      "totalCompletionRate": 73.33
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

- 从 **department_info_hrms**（或现有部门服务）中根据入参 **deptId** 查询部门信息，获取 **dept_level**，用于确定在 t_employee_training_info 中按哪一层级部门编码过滤。
- 若未查到部门，可返回 404 或 **data: []**，由实现与前端约定。

### 5.2 确定部门下人员范围（本部门全员，不含下级）

- **人员类型**：当前仅处理 **personType = 0**。
- **部门下人员**：在 **t_employee_training_info** 中，根据入参部门的**层级（dept_level）**与 **deptId**，按对应层级部门编码字段**等于** deptId 过滤，得到该**部门下所有人员**记录（仅本部门，不含下级）。
  - 一级部门：firstdeptcode = deptId
  - 二级部门：seconddeptcode = deptId
  - 三级部门：thirddeptcode = deptId
  - 四级部门：fourthdeptcode = deptId
  - 五级部门：fifthdeptcode = deptId
  - 六级/最小部门：sixthdeptcode = deptId（或按业务约定使用 lowestdeptid 等）

### 5.3 每人完课数计算

- 查询每条人员记录时，需读取：
  - **basic_courses**、**advanced_courses**（及若包含实战则 **practical_courses**）：逗号分隔的已完课课程ID列表。
  - **basic_target_courses_num**、**advanced_target_courses_num**（及若含实战则 **practical_target_courses_num**）：目标课程数。
- **完课数**：对上述完课字段，按逗号分割为列表，**非空**时完课数 = 列表长度；**空或 null** 时完课数 = 0。
  - **基础目标课程完课数** = 对 basic_courses 按逗号分割后的元素个数（去空后计数，或按实现约定）。
  - **进阶目标课程完课数** = 对 advanced_courses 按逗号分割后的元素个数。
  - （若输出实战）实战目标课程完课数 = 对 practical_courses 按逗号分割后的元素个数。

### 5.4 每人完课占比计算

- **基础目标课程完课占比** =（基础目标课程完课数 / 基础目标课程数）× 100；当基础目标课程数为 0 时，占比按 0 或约定规则处理（建议 0），保留 2 位小数。
- **进阶目标课程完课占比** =（进阶目标课程完课数 / 进阶目标课程数）× 100；当进阶目标课程数为 0 时，占比按 0 处理，保留 2 位小数。
- **目标课程完课占比（总）** =（目标课程完课数 / 总目标课程数）× 100；当总目标课程数为 0 时，占比按 0 处理，保留 2 位小数。

### 5.5 每人总目标课程数与总完课数

- **总目标课程数** = 基础目标课程数 + 进阶目标课程数。若表结构包含实战目标课程数且业务需要汇总，则 总目标课程数 = 基础 + 进阶 + 实战。
- **目标课程完课数（总）** = 基础完课数 + 进阶完课数。若包含实战，则再加上实战完课数。

---

## 6. 数据流程摘要

1. 入参校验：deptId 必填；personType 当前仅处理 0。
2. 根据 **deptId** 查询部门信息，得到 **dept_level**；若部门不存在，返回 404 或 **data: []**。
3. 在 **t_employee_training_info** 中按 5.2 规则过滤出该部门下所有人员（personType=0，仅本部门不含下级），查询字段：姓名、工号、职位类、职位子类、一级部门～最小部门、basic_courses、advanced_courses、practical_courses（若用）、basic_target_courses_num、advanced_target_courses_num、practical_target_courses_num（若用）。
4. 对每条人员记录：
   - 按 5.3 计算基础/进阶（及实战）完课数；
   - 按 5.4 计算基础/进阶/总完课占比；
   - 按 5.5 计算总目标课程数、总完课数；
   - 组装为 data 中一条记录。
5. 返回 **data** 为上述人员列表（数组）。

---

## 7. 失败响应示例

- **部门不存在**（如 deptId 在部门表中无记录）：

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

| 表名 / 接口                     | 用途 |
|---------------------------------|------|
| department_info_hrms            | 根据 deptId 查询部门信息及 dept_level，用于确定按哪一层级在训战表中过滤 |
| t_employee_training_info        | 训战课程表；按 dept_level 对应部门编码字段 = deptId 过滤本部门人员；读取 last_name、employee_number、job_category、job_subcategory、firstdept～lowestdept、basic_courses、advanced_courses、practical_courses、basic_target_courses_num、advanced_target_courses_num、practical_target_courses_num |

---

## 9. 待确认项（Check 时可与产品/实现约定）

1. **实战维度**：当前文档按「基础 + 进阶」输出；若需在列表中展示实战目标课程数/完课数/占比，可在响应中增加对应字段，总目标与总完课数中一并加入实战。
2. **人员类型**：personType 未来扩展值及对应过滤规则（如是否关联其它人员表）。
3. **除数为 0**：目标课程数为 0 时，完课占比返回 0 或 null，以及前端展示约定。
4. **数值精度**：完课占比小数位数（建议保留 2 位小数）。
5. **逗号分割规则**：basic_courses / advanced_courses 为空字符串、仅逗号、前后空格等时的解析规则（建议：trim 后按逗号分割，空串或 null 完课数 = 0）。

请你 check 上述文档，确认入参、响应字段、计算逻辑及待确认项后再进行代码开发。
