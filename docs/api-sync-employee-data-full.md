# 全体员工训战信息同步接口文档

## 1. 概述

本接口将指定期号下的**全部成员**与 **t_employee_training_info** 表做全量对比，通过批量新增、更新、删除，使目标表与源数据一致。**用户基本信息**参考原接口从 **t_employee** 表查询；**训战课程字段**（basic_courses、advanced_courses、practical_courses）按「四级部门目标课程 + 完课判断」逻辑刷新，可参考 **GET /personal-course/completion** 接口中获取实际完课数的逻辑，将已完课课程的 ID 更新到对应字段。

与现有 `sync-employee-data` 的差异：
- **人员范围**：从 t_employee_sync 按期号查询全部成员（不过滤研发族/干部）。
- **用户基本信息来源**：从 **t_employee** 表查询（与原接口一致），不从 t_employee_sync 取业务字段。
- **期号逻辑**：与现有接口一致（当前日期减 2 天，格式 yyyyMMdd）。

---

## 2. 接口说明

| 项目       | 说明 |
|------------|------|
| 接口名称   | 同步全体员工训战信息 |
| 请求路径   | `POST /external-api/sync-employee-training-info` |
| 请求方式   | POST |
| 请求参数   | 无（期号由服务端自动计算） |
| 是否需要认证 | 视现有 external-api 策略而定 |

---

## 3. 期号计算规则

- **规则**：当前日期减 2 天，格式为 `yyyyMMdd`。
- **示例**：当前日期为 2025-02-11 时，期号 `periodId = "20250209"`。

---

## 4. 业务逻辑

### 4.1 流程概览

1. 计算期号 periodId（当前日期 - 2 天，yyyyMMdd）。
2. 从 **t_employee_sync** 查询该 periodId 下的**所有成员工号**（不过滤研发族/干部），确定本期同步范围。
3. 根据上述工号，从 **t_employee** 表查询对应用户**基本信息**（姓名、多级部门、岗位等），作为源数据。
4. 对每条员工数据，按 [4.6 训战课程字段刷新逻辑](#46-训战课程字段-basic_coursesadvanced_coursespractical_courses-刷新逻辑) 计算 basic_courses、advanced_courses、practical_courses。
5. 从 t_employee_training_info 查询全量数据作为目标。
6. 以**工号**为唯一标识，做全量对比，得到：待新增、待更新、待删除。
7. 在事务内批量执行：插入、更新、删除（如每批 1000 条）。
8. 返回同步结果统计。

### 4.2 数据源

- **人员范围**：从 `t_employee_sync` 按 `period_id = periodId` 查询本期号下所有记录（不区分研发族/干部），得到待同步工号列表。
- **用户基本信息**：从 **t_employee** 表按上述工号查询（last_name、各级部门、岗位等）。
- **训战课程字段**：按 4.6 节逻辑，基于员工四级部门目标课程与完课数据刷新。

### 4.3 目标数据

- **表**：`t_employee_training_info`（全员训战课程信息）。
- **范围**：全表（全量），用于与源数据对比。
- **目标表建表语句**见 [附录：目标表建表语句](#附录目标表建表语句)。

### 4.4 对比与分类规则

- **唯一键**：工号（employee_number）。
- **待新增**：源数据中存在该工号，但 t_employee_training_info 中不存在。
- **待更新**：源与目标均存在该工号，且除 period_id、updated_time 外的业务字段有差异。
- **忽略**：源与目标均存在该工号，且业务字段无差异（仅计入统计，不写库）。
- **待删除**：t_employee_training_info 中存在该工号，但本周期号下源数据中不存在（即从目标表中删除）。

### 4.5 字段映射与比较

- **用户基本信息**（与 t_employee 表对应）：工号、姓名（last_name）、多级部门编码/名称（firstdeptcode ~ sixthdeptcode、firstdept ~ lowestdept、lowestdeptid）、职位族/类/子类（job_type、job_category、job_subcategory）、period_id、updated_time。
- **训战课程字段**：basic_courses、advanced_courses、practical_courses 按 [4.6](#46-训战课程字段-basic_coursesadvanced_coursespractical_courses-刷新逻辑) 逻辑刷新，存储已完课课程 ID 列表（逗号分隔）。
- 比较是否需更新时，**不比较** period_id、updated_time，业务字段含基本信息与训战课程三字段。

### 4.6 训战课程字段（basic_courses、advanced_courses、practical_courses）刷新逻辑

三字段含义分别为**基础**、**进阶**、**实战**目标课程的**完课列表**（即已完课课程的 ID 列表，逗号分隔）。刷新逻辑参考 **GET /personal-course/completion** 接口（`PersonalCourseCompletionService.getPersonalCourseCompletion`）中获取实际完课数的逻辑，步骤如下：

1. **获取员工四级部门 ID**  
   当前员工的四级部门编码（fourthdeptcode），**须从本次同步的基本信息中获取**，不从 t_employee 表单独查询。

2. **获取该四级部门下选定的目标课程**  
   - 按四级部门 ID 查询部门选课信息（参考 completion 接口：如 `getDeptSelectionByDeptCode(fourthDeptCode)`），得到该部门选定的目标课程 ID 列表（如 `courseSelections` 逗号分隔的课程 ID）。  
   - 若部门无选课或选课为空，则使用全部课程作为目标课程（与 completion 接口 fallback 一致）。

3. **判断目标课程的完课数据**  
   - 参考 completion 接口：在目标课程范围内，查询该员工已完成的课程列表。  
   - 即：目标课程编码列表 → 调用与 `getCompletedCourseNumbers(empNum, targetCourseNumbers)` 等效的完课查询，得到该员工在目标课程中**已完课**的课程编码（或课程 ID）列表。

4. **按课程级别写入对应字段**  
   - 将目标课程按课程级别（基础、进阶、实战）分组，在已完课列表中按同一级别筛选。  
   - **基础** → 已完课的基础课程 ID 列表 → 写入 **basic_courses**（逗号分隔）。  
   - **进阶** → 已完课的进阶课程 ID 列表 → 写入 **advanced_courses**（逗号分隔）。  
   - **实战** → 已完课的实战课程 ID 列表 → 写入 **practical_courses**（逗号分隔）。  
   - 若某级别无完课，对应字段为空字符串或 NULL。

**参考实现**：`PersonalCourseCompletionService.getPersonalCourseCompletion`（四级部门 → 部门选课 → 目标课程 → `getCompletedCourseNumbers` 获取完课编码 → 按 courseLevel 分组统计）。同步时复用相同数据源与完课判断逻辑，仅将「已完课课程 ID」按级别写入上述三字段。

### 4.7 批量操作与事务

- **批量大小**：建议与现有逻辑一致，每批 1000 条（插入、更新、删除分别分批）。
- **事务**：整次同步在一个事务中执行，失败时整体回滚（`@Transactional(rollbackFor = Exception.class)`）。

### 4.8 数据量校验（可选）

- 现有接口有「源数据 &lt; 2000 则中止」的熔断。
- 本接口是否保留类似校验（如低于某阈值则拒绝同步）、阈值是否仍为 2000，由你在 check 时决定，并在文档/实现中注明。

---

## 5. 响应格式

### 5.1 成功响应

- **HTTP 状态码**：200。
- **Body**：与现有统一封装一致，例如：

```json
{
  "code": 200,
  "message": "同步成功",
  "data": {
    "success": true,
    "message": "Sync completed successfully",
    "periodId": "20250209",
    "totalSource": 15000,
    "insertCount": 100,
    "updateCount": 200,
    "deleteCount": 50,
    "ignoreCount": 14650
  }
}
```

### 5.2 失败响应

- **HTTP 状态码**：200（与现有 external-api 一致时）或 5xx。
- **Body**：例如：

```json
{
  "code": 500,
  "message": "系统异常：xxx"
}
```

或业务熔断（若保留数据量校验）：

```json
{
  "code": 200,
  "message": "同步失败",
  "data": {
    "success": false,
    "message": "Source data count is too low (1500), sync aborted.",
    "periodId": "20250209",
    "totalSource": 1500
  }
}
```

---

## 6. 返回字段说明（data）

| 字段        | 类型    | 说明 |
|-------------|---------|------|
| success     | boolean | 是否执行成功（含是否通过数据量校验） |
| message     | string  | 结果描述 |
| periodId    | string  | 本次使用的期号 |
| totalSource | int     | 源数据条数（本期号 t_employee_sync 人数） |
| insertCount | int     | 新增条数 |
| updateCount | int     | 更新条数 |
| deleteCount | int     | 删除条数 |
| ignoreCount | int     | 无变化忽略条数 |

（若保留数据量校验，可增加 totalSource 等便于排查。）

---

## 7. 异常与约束

- **periodId 为空**：由服务端计算，一般不出现；若扩展为可传入 periodId，则需校验非空。
- **数据库异常**：事务回滚，返回 500 及异常信息。
- **并发**：若同一时间多请求同步，建议在实现层考虑防重或串行化（可根据需要补充到文档）。

---

## 8. 与现有 sync-employee-data 的对比摘要

| 项目           | sync-employee-data（现有）        | sync-employee-training-info（本接口）     |
|----------------|-----------------------------------|--------------------------------------|
| 源数据         | 研发族(t_employee_sync) + 干部(同表按工号)，干部覆盖 | 人员范围：t_employee_sync 该期号；基本信息：t_employee；训战课程：四级部门目标课程 + 完课逻辑（参考 /personal-course/completion） |
| 期号           | 当前日期 - 2 天，yyyyMMdd         | 相同                                 |
| 对比与写库逻辑 | 全量对比，批量增/改/删             | 相同                                 |
| 数据量熔断     | 源 &lt; 2000 中止                  | 可选，由你 check 后决定              |

---

## 附录：目标表建表语句

目标表 **t_employee_training_info**（全员训战课程信息）建表语句如下：

```sql
CREATE TABLE `t_employee_training_info` (
  `employee_number` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '工号',
  `last_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '姓名',
  `firstdeptcode` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '一级部门',
  `seconddeptcode` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '二级部门',
  `thirddeptcode` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `fourthdeptcode` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `fifthdeptcode` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `sixthdeptcode` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `lowestdeptid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '最小部门',
  `firstdept` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `seconddept` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `thirddept` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `fourthdept` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `fifthdept` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `sixthdept` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `lowestdept` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `job_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '职位族',
  `job_category` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '职位类',
  `job_subcategory` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '职位子类',
  `period_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '更新字段',
  `updated_time` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '更新时间',
  `basic_courses` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '基础目标课程完课列表',
  `advanced_courses` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '进阶目标课程完课列表',
  `practical_courses` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '实战目标课程完课列表',
  PRIMARY KEY (`employee_number`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '全员训战课程信息' ROW_FORMAT = DYNAMIC;
```

**说明**：basic_courses、advanced_courses、practical_courses 按文档 [4.6 训战课程字段刷新逻辑](#46-训战课程字段-basic_coursesadvanced_coursespractical_courses-刷新逻辑) 刷新（四级部门目标课程 + 完课判断）（四级部门目标课程 + 完课判断，参考 /personal-course/completion）。

---

请你先 check 上述文档，确认路径、是否保留数据量校验及阈值、返回字段等。确认后再进行代码修改。
