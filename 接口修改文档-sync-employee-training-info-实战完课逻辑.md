# 接口修改文档：sync-employee-training-info 实战课程目标与完课逻辑

## 1. 修改概述

**接口路径**：`POST /external-api/sync-employee-training-info`

**修改目的**：在现有「训战课程字段」查询逻辑中，**仅对“实战”部分**调整数据来源与完课判断方式：
- **实战目标课程**：改为从 `dept_course_selections.practical_selections` + `ai_practical_course_info` 获取（不再从 `ai_course_planning_info` 的“实战”级别取）。
- **实战已完课数据**：改为从 `hands_on_courses`（工号 + task_status=finished）联表 `ai_practical_course_info` 得到完课课程 ID（不再从 `t_micro_study_info_sync` / `t_mooc_study_info_sync` 的 course_team_code 取）。

**不变部分**：基础、进阶的目标课程与完课逻辑**保持原样**（仍使用 `dept_course_selections.course_selections`、`ai_course_planning_info` 及 micro/mooc 完课表）。

**文档用途**：供评审与确认，check 通过后再进行代码开发。

---

## 2. 当前逻辑（保持不变部分）

以下逻辑**保持原样**：

- **基础、进阶目标课程**：用员工四级部门编码查 `dept_course_selections`（getDeptSelectionByDeptCode），取 `course_selections`（课程 ID 逗号分隔）；有选课则用这些 ID 查 `ai_course_planning_info`，无选课则查全部。按课程级别“基础”“进阶”筛选，得到目标课程列表（含 id、course_number、course_level 等）。
- **基础、进阶已完课**：在目标课程范围内，用 `personalCourseCompletionMapper.getCompletedCourseNumbers(empNum, targetCourseNumbers)`，即从 `t_micro_study_info_sync`、`t_mooc_study_info_sync` 中按 `emp_num`、`course_team_code`（对应 course_number）、`is_pass='1'` 查询已完课课程编码，再按课程 ID 写入 `basic_courses`、`advanced_courses`（逗号分隔）。
- **同步流程**：期号计算、t_employee_sync 范围、t_employee 基本信息、全量对比与批量增/改/删等均不变。

---

## 3. 修改后逻辑（仅“实战”部分）

### 3.1 实战目标课程列表的获取

| 步骤 | 说明 |
|------|------|
| 1 | 用员工**四级部门编码** `fourthDeptCode` 查询 `dept_course_selections`（沿用 `getDeptSelectionByDeptCode(fourthDeptCode)`），读取部门配置中的 **`practical_selections`**（实战课程 ID 逗号分隔）。 |
| 2 | **若部门有选课**：即 `practical_selections` 非空且解析后 ID 列表不为空。用这些课程 ID 查询 **`ai_practical_course_info`**，条件：id IN (解析出的 ID)，且仅查有效课程（若表中有有效状态字段，则按约定过滤）。得到列表字段：**id**（主键）、**task_type**、**task_name**。 |
| 3 | **若部门无选课或为空**：即未配置或 `practical_selections` 为空。则查询 **`ai_practical_course_info`** 的**全部有效课程**，同样得到：id、task_type、task_name。 |
| 4 | 将上述结果作为本员工的**「实战目标课程列表」**，用于后续完课匹配与 `practical_target_courses_num` 统计。 |

**说明**：
- `practical_selections` 为 **`dept_course_selections` 表新增字段**（若表中尚无），类型建议与 `course_selections` 一致（如 varchar，存逗号分隔的 ID）。
- **实战目标课程数** `practical_target_courses_num`：若部门有选课且 `dept_course_selections` 已配置 `practical_target_courses_num`，则优先用配置值；否则用本次得到的「实战目标课程列表」条数。

### 3.2 实战已完课数据的获取

| 步骤 | 说明 |
|------|------|
| 1 | 按**工号** `account`（与当前同步员工工号一致，注意与 `hands_on_courses.account` 口径统一）查询 **`hands_on_courses`**，条件：`task_status = 'finished'`（已完成）。得到该员工所有已完课的 **`task_type`** 列表。 |
| 2 | 用上述 `task_type` 列表与 **`ai_practical_course_info`** 联表（或 IN 查询）：通过 `ai_practical_course_info.task_type` 与 `hands_on_courses.task_type` 对应，查询得到这些已完课任务对应的 **课程主键 id**。 |
| 3 | 上述 **id 集合**即为该员工在「实战」维度上的**已完课课程 ID**。仅当该 id 同时属于「实战目标课程列表」时，才计入本员工在训战表中的实战完课（即写入 `practical_courses` 的 ID 需在目标列表内，避免写入非目标课程）。 |

**说明**：
- 完课记录仅以 `hands_on_courses` 为准，不再从 micro/mooc 表取实战完课。
- 若 `hands_on_courses` 中某 `task_type` 在 `ai_practical_course_info` 中无对应记录，则该条不产生课程 id，不写入 `practical_courses`。

### 3.3 practical_courses 字段的写入

- **内容**：该员工在「实战目标课程列表」范围内、且已在 `hands_on_courses` 中完课（task_status=finished）所对应的 **`ai_practical_course_info.id`**，**逗号分隔**，升序排序，写入 `t_employee_training_info.practical_courses`。
- **与现有格式一致**：仍为逗号分隔的课程 ID 字符串，例如 `"1,3,5"`；无完课时可为 null 或空串（与现有实现保持一致）。

---

## 4. 涉及表与字段汇总

| 表名 | 用途 | 关键字段 / 说明 |
|------|------|------------------|
| **dept_course_selections** | 部门选课配置 | 现有：dept_code, course_selections, basic/advanced/practical_target_courses_num。<br>**新增**：**practical_selections**（实战课程 ID 逗号分隔，对应 ai_practical_course_info.id）。 |
| **ai_practical_course_info** | 实战课程主数据 | **id**（主键）、**task_type**（与 hands_on_courses.task_type 对应）、**task_name**；若有“有效”标识字段（如 status、is_valid 等）需在“全部有效课程”查询中约定。 |
| **hands_on_courses** | 实战完课记录 | **account**（工号）、**task_type**、**task_status**（完课条件：'finished'）。 |
| **t_employee_training_info** | 训战表 | employee_number, **practical_courses**（逗号分隔的实战课程 ID）, practical_target_courses_num, updated_time 等。 |
| **ai_course_planning_info** | 基础/进阶课程（本次不改） | 仍用于 basic_courses、advanced_courses 的目标与完课。 |
| **t_micro_study_info_sync / t_mooc_study_info_sync** | 基础/进阶完课（本次不改） | 仍仅用于基础、进阶的已完课判断。 |

---

## 5. 数据流简图（仅实战部分）

```
员工四级部门 fourthDeptCode
    ↓
getDeptSelectionByDeptCode(fourthDeptCode) → practical_selections
    ↓
有选课？── 是 → 用 ID 列表查 ai_practical_course_info（有效）→ 实战目标课程列表
    └── 否/空 → 查 ai_practical_course_info 全部有效课程 → 实战目标课程列表
    ↓
hands_on_courses: account + task_status='finished' → task_type 列表
    ↓
task_type 联表 ai_practical_course_info → 得到已完课课程 id 集合
    ↓
取「在实战目标课程列表内的 id」→ 升序、逗号拼接 → practical_courses
```

---

## 6. 开发需新增/修改点（供实现参考）

1. **表 dept_course_selections**
   - 若尚无 **practical_selections** 字段：新增字段（如 varchar，逗号分隔的实战课程 ID）；并确认与 `course_selections` 的区分（前者仅实战，后者为基础+进阶等）。

2. **实体 DeptCourseSelection**
   - 增加属性 **practicalSelections**（String），与表字段 practical_selections 映射。

3. **CoursePlanningInfoMapper / 或新建 PracticalCourseMapper**
   - 根据 **课程 ID 列表**查询 `ai_practical_course_info`：返回 id、task_type、task_name（仅有效课程）。
   - 查询 **全部有效课程**：无 ID 条件，按有效状态查 ai_practical_course_info，返回同上。

4. **PersonalCourseCompletionMapper 或新建 HandsOnCourse 相关 Mapper**
   - 按**工号** + **task_status = 'finished'** 查询 `hands_on_courses`，返回 **task_type** 列表（去重）。
   - 或：按工号 + task_status=finished 联表 `ai_practical_course_info`（on task_type），直接返回**已完课对应的课程 id 列表**，便于与目标列表求交。

5. **EmployeeTrainingInfoSyncServiceImpl**
   - **getTargetCoursesByFourthDept**（或等价逻辑）：
     - 基础、进阶分支不变（仍用 course_selections + ai_course_planning_info）。
     - **实战分支**：改为读 `selection.getPracticalSelections()`，解析 ID → 有则按 ID 查 ai_practical_course_info，无则查全部有效；得到「实战目标课程列表」（可用新 VO 如 PracticalCourseInfoVO：id, taskType, taskName）。
   - **fillTrainingCourseFields** 中实战部分：
     - 目标列表：使用上一步得到的实战目标课程列表。
     - 已完课：调用 hands_on_courses + ai_practical_course_info 得到该员工完课实战课程 id 列表；与目标列表求交后，得到最终写入的 id 集合。
     - **joinCompletedByLevel** 对“实战”的调用：改为传入「实战目标课程列表」与「实战已完课 id 集合」，拼接为逗号分隔的 id 字符串写入 `po.setPracticalCourses(...)`。
   - 保持 **basic_courses**、**advanced_courses** 的现有实现不变。

6. **兼容与边界**
   - 若 `ai_practical_course_info` 无数据或 hands_on_courses 无 finished 记录，则实战目标列表或已完课列表可能为空，`practical_courses` 写 null 或空串即可。
   - 工号口径：sync 中使用的员工工号与 `hands_on_courses.account` 需一致（是否去首字母等与现有约定保持一致）。

---

## 7. 待确认清单（check 时请确认）

- [ ] **dept_course_selections.practical_selections**：字段名、类型及是否必填；若部门未配置，是否视为“无选课”走全部有效实战课程。
- [ ] **ai_practical_course_info**：表结构、主键名、与 `task_type` 对应字段名；“有效课程”的判定字段（如 status、is_valid）及取值。
- [ ] **hands_on_courses.task_status**：完课状态确认为 `'finished'` 或其它，是否忽略大小写。
- [ ] **工号一致性与格式**：t_employee_sync / t_employee 中的员工工号与 hands_on_courses.account 的对应关系（是否需 trim、去首字母等）。
- [ ] **practical_target_courses_num**：部门有选课时，优先取 dept_course_selections 的配置值，还是始终用本次计算出的实战目标课程列表条数。
- [ ] **仅目标内的完课才写入**：是否严格要求「只有属于本次“实战目标课程列表”的 id 才写入 practical_courses」；若 hands_on_courses 有其它 task_type 完课但不在目标列表中，是否一律不写入。

---

## 8. 数据库变更（实施时执行）

- **dept_course_selections**：若表中尚无 `practical_selections` 字段，需执行：
  ```sql
  ALTER TABLE dept_course_selections ADD COLUMN practical_selections VARCHAR(500) NULL COMMENT '实战课程ID逗号分隔，对应ai_practical_course_info.id' AFTER course_selections;
  ```
- **ai_practical_course_info**：若表不存在，需按业务约定建表，至少包含：`id`（主键）、`task_type`、`task_name`（与 hands_on_courses.task_type 对应）。

---

**文档版本**：v1.0  
**编写目的**：供评审与 check，确认后再进行代码开发实现。  
**实现状态**：已按本文档完成代码整改。
