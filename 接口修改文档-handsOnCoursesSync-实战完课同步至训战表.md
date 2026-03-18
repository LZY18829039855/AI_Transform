# 接口修改文档：handsOnCoursesSync 同步时更新实战完课数据

## 1. 修改概述

**接口路径**：`POST /external-api/handsOnCoursesSync`

**修改目的**：在现有「工号 + 课程类型」同步到 `hands_on_courses` 表的基础上，当**任务状态为已完成（finished）**时，**同步更新**该员工在 `t_employee_training_info` 表中的**实战完课列表**（`practical_courses`）：若当前课程 ID 不在列表中则追加，已存在则不更新。

**文档用途**：供评审与确认，check 通过后再进行开发。

---

## 2. 当前逻辑（保持不变部分）

以下逻辑**保持原样**，仅在其后**增加**“状态为 finished 时写训战表”的步骤。

1. **参数校验**：校验必填参数 `account`、`task_type`、`task_status` 非空。
2. **hands_on_courses 同步**：
   - 根据 `account` + `task_type` 查询 `hands_on_courses` 是否已有记录。
   - **存在**：更新该记录的 `task_status`、`task_info`，`update_time` 置为当前时间。
   - **不存在**：插入新记录（account、task_type、task_status、task_info、update_time）。
3. **返回**：统一返回成功或失败及简要说明（含 action、account、task_type 等）。

---

## 3. 新增逻辑（仅当 task_status = finished 时执行）

当且仅当 **`task_status` 为 `finished`** 时，在完成上述 `hands_on_courses` 的插入/更新之后，执行以下步骤，将本次完课同步到 `t_employee_training_info` 的实战完课列表。

### 3.1 步骤一：根据 task_type 查询课程主键 ID

- **表**：`ai_practical_course_info`
- **目的**：根据请求中的 `task_type` 找到对应实战课程的**主键 ID**（用于写入完课列表）。
- **待确认**：
  - 表 `ai_practical_course_info` 的**主键字段名**（文档假定为 `id`，若不同请注明）。
  - 与 `task_type` 匹配的**字段名**（例如：课程名称、课程类型编码、`task_type` 等）。  
    即：`WHERE <匹配字段> = #{taskType}` 中的 `<匹配字段>` 需在开发前确认。
- **结果**：得到课程主键 ID（整数或可转为唯一标识的类型）。若查不到则**不执行**后续“更新训战表”步骤，仅完成 hands_on_courses 的同步即可（也可在文档/实现中约定是否返回提示，见下文“可选行为”）。

### 3.2 步骤二：根据工号查询训战表中的实战完课列表

- **表**：`t_employee_training_info`
- **条件**：`employee_number = account`（请求中的工号，注意工号前后空格需与库表一致，建议 trim 后使用）。
- **读取字段**：`practical_courses`
- **格式约定**：`practical_courses` 为**逗号分隔**的课程 ID 列表（与现有 `EmployeeTrainingInfoPO` 注释一致），例如：`"1,3,5"`。解析时需考虑空串、null、仅空格等情况，视为“空列表”。

### 3.3 步骤三：判断是否已在完课列表中并更新

- **判断**：当前课程主键 ID 是否已存在于 `practical_courses` 解析后的列表中（按字符串或数字比较，需与库表存储格式一致）。
  - **已存在**：不更新 `t_employee_training_info`，仅完成 hands_on_courses 的同步。
  - **不存在**：将当前课程 ID **追加**到列表中，写回 `t_employee_training_info.practical_courses`，并建议同时更新 `updated_time`（若表中有该字段）。
- **写入格式**：与现有存储格式一致，即逗号分隔的 ID 字符串，例如追加后为 `"1,3,5,7"`。注意去重、首尾无多余逗号。

### 3.4 可选行为（建议在评审时确认）

- **未在 ai_practical_course_info 中查到课程**：仅不同步训战表，hands_on_courses 仍按原逻辑插入/更新；是否在返回中增加提示（如 “未找到对应实战课程，未更新训战完课列表”）可由业务决定。
- **t_employee_training_info 中无该工号记录**：可选择仅更新 hands_on_courses，不新增训战表记录；或约定由其它同步任务（如全员训战同步）负责该表的新增，本接口只做“已有记录时的 practical_courses 更新”。

---

## 4. 状态值约定

- **触发“同步到训战表”的状态**：`task_status == "finished"`（大小写建议在实现时统一，如全转小写再比较）。
- 若与调用方约定使用中文“已完成”等，请在开发前统一为一种取值，并在文档与代码中保持一致。

---

## 5. 涉及表与字段汇总

| 表名 | 用途 | 关键字段 |
|------|------|----------|
| `hands_on_courses` | 原有同步 | account, task_type, task_status, task_info, update_time |
| `ai_practical_course_info` | 按 task_type 查课程 ID | 主键 id；与 task_type 匹配的字段（待确认） |
| `t_employee_training_info` | 更新实战完课列表 | employee_number, practical_courses（逗号分隔）, updated_time（如有） |

---

## 6. 开发需新增/修改点（供实现参考）

1. **Mapper / 表 ai_practical_course_info**
   - 新增或复用 Mapper：根据“与 task_type 匹配的字段”查询主键 ID（如 `selectIdByTaskType(String taskType)`）。
   - 需确认表结构及匹配字段后再写 SQL。

2. **Mapper t_employee_training_info**
   - 按工号查询一条记录（含 `practical_courses`）：若已有可复用方法则用，否则新增如 `selectByEmployeeNumber(String employeeNumber)`。
   - 按工号更新 `practical_courses`（及 `updated_time`）：新增如 `updatePracticalCoursesByEmployeeNumber(String employeeNumber, String practicalCourses)`，避免影响其它字段。

3. **Service（HandsOnCourseService）**
   - 在现有 `syncHandsOnCourse` 流程中，在 hands_on_courses 插入/更新成功后：
     - 若 `task_status` 为 `finished`：
       - 调用 Mapper 根据 `task_type` 查 `ai_practical_course_info` 得课程 ID；
       - 若 ID 存在，再根据 `account` 查 `t_employee_training_info` 的 `practical_courses`；
       - 解析列表，若当前课程 ID 不在列表中则追加并写回。
   - 事务：建议整段逻辑仍在同一事务中（含对 `t_employee_training_info` 的更新），保持与现有 `@Transactional` 一致。

4. **兼容与异常**
   - 未查到课程 ID 或未查到员工训战记录时，不抛异常，仅不同步训战表；原有返回结构保持不变。
   - 若后续需要在返回中区分“仅同步了 hands_on_courses”与“同时更新了训战表”，可在 `data` 中增加字段（如 `trainingInfoUpdated`: true/false）。

---

## 7. 流程简图（task_status = finished 时）

```
请求入参(account, task_type, task_status, task_info)
    ↓
参数校验 → 失败则返回
    ↓
hands_on_courses 存在则更新 / 不存在则插入
    ↓
task_status == "finished" ?
   否 → 返回成功
   是 ↓
根据 task_type 查 ai_practical_course_info 得课程 ID
    ↓
未查到 ID → 返回成功（可选提示）
    ↓
根据 account 查 t_employee_training_info.practical_courses
    ↓
无该工号记录 → 返回成功（不新增训战记录，见可选行为）
    ↓
解析 practical_courses，判断课程 ID 是否已在列表中
    ↓
已在列表中 → 返回成功（不更新）
    ↓
未在列表中 → 追加课程 ID，写回 practical_courses（及 updated_time）→ 返回成功
```

---

## 8. 待确认清单（check 时请确认）

- [ ] **task_status 触发值**：确认为 `"finished"` 还是 `"已完成"` 或其它，是否忽略大小写。
- [ ] **ai_practical_course_info**：表结构、主键名、与 `task_type` 匹配的字段名。
- [ ] **t_employee_training_info**：工号字段与请求 `account` 的对应关系（是否需去掉首字母等）；`practical_courses` 存储格式确认为逗号分隔的课程 ID。
- [ ] **无训战记录**：该工号在 `t_employee_training_info` 无记录时，本接口是否只更新 hands_on_courses，不创建训战记录。
- [ ] **未查到课程 ID**：是否在响应中增加说明“未找到对应实战课程”。

---

**文档版本**：v1.0  
**编写目的**：供评审与 check，确认后再进行开发实现。
