# 专家/干部训战查询（按岗位 AI 成熟度维度）

## 1. 概述

- **用途**：在指定部门范围内，按**岗位 AI 成熟度**（L1 / L2 / L3）汇总**干部**或**专家**的训战统计。
- **与** `GET /personal-course/department-completion-rate` **的差异**：原接口按**部门**下钻；本接口按**成熟度**汇总；**不**使用 `dept_course_selections` / 选课表，目标课程数**仅**来自 `t_employee_training_info` 中每人行的 `*_target_courses_num`。
- **实现路径**：`GET /trainning-courses/maturity-trainning-courses`（`TrainingCoursesController`，与 `application.yml` 中 `context-path` 拼接后为完整 URL，如 `/ai_transform_webapi/trainning-courses/maturity-trainning-courses`）

---

## 2. 请求参数

| 参数名       | 类型    | 必填 | 说明 |
|--------------|---------|------|------|
| `deptId`     | String  | 是   | 部门编码。为空返回 400。**传 `0` 时视为云核心网产品线二级部门**，在服务端转换为常量 `DepartmentConstants.CLOUD_CORE_NETWORK_DEPT_CODE`（当前为 `031562`）后再查询。 |
| `personType` | Integer | 是   | **仅** `1` = 干部、`2` = 专家。不支持 `0`（全员）及其它值；非法返回 400。 |

---

## 3. 业务逻辑

### 3.1 部门范围

0. **入参 `deptId` 为 `0` 时**：先替换为二级部门「云核心网产品线」部门编码 `CLOUD_CORE_NETWORK_DEPT_CODE`（与 `department-completion-rate` 等接口对 `0` 的约定一致），再执行后续步骤。
1. 根据（解析后的）`deptId` 查询部门信息，得到 **`deptLevel`**（1～6）。
2. 在 **`t_employee_training_info`** 中按与现有 `listByDeptLevelAndCode` **相同规则**筛选**本部门直属人员（不含下级）**（例如四级部门：`fourthdeptcode = deptId`）。
3. 部门不存在或无法解析层级时，与现有接口风格一致（如返回空列表）。

### 3.2 人员类型与成熟度（仅 1 / 2）

在 3.1 结果上追加（与现有 Mapper 中 `personType` 条件一致）：

| personType | 过滤条件 | 分组字段 |
|------------|----------|----------|
| `1` 干部   | `cadre_position_ai_maturity` 非空（`IS NOT NULL AND TRIM(...) != ''`） | `cadre_position_ai_maturity` |
| `2` 专家   | `expert_position_ai_maturity` 非空 | `expert_position_ai_maturity` |

成熟度枚举：**L1、L2、L3**（若库中存在其它取值，是否仅统计三档需产品确认；建议仅输出 L1/L2/L3 或约定映射）。

### 3.3 完课列表解析

- `basic_courses`、`advanced_courses`、`practical_courses` 为逗号分隔列表；**单人单维度完课数** = 拆分后**非空**片段个数（与 `DepartmentCourseCompletionRateServiceImpl#countCompletedCourses` 一致）。

### 3.4 与选课/课程规划

- **不**查询、**不**依赖 `dept_course_selections` 及基于四级部门选课的 `ai_course_planning_info` 聚合。
- 基础/进阶/实战的「目标课程数」口径**仅**使用表中每人字段：`basic_target_courses_num`、`advanced_target_courses_num`、`practical_target_courses_num`。

---

## 4. 分组内指标（每个成熟度 L1/L2/L3 一组）

设该组人数为 **N**，第 **i** 名成员（i = 1…N）：

- \(B_i\) = `basic_target_courses_num`（空按 0 或与库约定）
- \(A_i\) = `advanced_target_courses_num`
- \(P_i\) = `practical_target_courses_num`
- \(b_i\) = 基础维度完课列表计数，\(a_i\) = 进阶，\(p_i\) = 实战

**组内平均值：**

- \(\overline{B} = \frac{1}{N}\sum_i B_i\)，\(\overline{A}\)、\(\overline{P}\) 同理。

**总完课人次（各维度）：**

- \(T_b = \sum_i b_i\)，\(T_a = \sum_i a_i\)，\(T_p = \sum_i p_i\)。

| 输出字段（对标 `DepartmentCourseCompletionRateVO`，主键改为成熟度） | 计算方式 |
|---------------------------------------------------------------------|----------|
| `basicCourseCount` / `advancedCourseCount` / `practicalCourseCount` | 分别为 \(\overline{B}\)、\(\overline{A}\)、\(\overline{P}\)（「目标课程数」取组内算术平均）。展示为整数时四舍五入规则与产品约定。 |
| `basicAvgCompletedCount` / `advancedAvgCompletedCount` / `practicalAvgCompletedCount` | **平均完课人数** = **总完课数 ÷ 平均目标课程数**：基础 = \(T_b / \overline{B}\)（\(\overline{B}=0\) 时约定为 0）；进阶 = \(T_a / \overline{A}\)；实战 = \(T_p / \overline{P}\)。结果**四舍五入为整数**（与现有 VO 注释风格一致）。 |
| `basicAvgCompletionRate` / `advancedAvgCompletionRate` / `practicalAvgCompletionRate` | 平均完课率 = **该维度总完课人次 ÷（该组人数 × 该维度平均目标课程数）× 100**，即：基础 = \(\dfrac{T_b}{N \cdot \overline{B}} \times 100\)（\(\overline{B}=0\) 或 \(N=0\) 时为 0）；进阶、实战同理。百分比保留 2 位小数（与现有 `round2` 一致）。 |

**等价关系**：当 \(\overline{B}>0\) 时，  
\(\text{basicAvgCompletionRate} = \dfrac{T_b}{N \cdot \overline{B}} \times 100 = \dfrac{T_b / \overline{B}}{N} \times 100\)。

`baselineCount` = 该成熟度组内 **N**。

---

## 5. 响应结构（建议）

外层：`Result<List<PositionAiMaturityCourseCompletionRateVO>>`（与项目统一）。

单条元素字段建议：

| 字段 | 类型 | 说明 |
|------|------|------|
| `positionAiMaturity` | String | L1 / L2 / L3 |
| `personType` | Integer | 回显 1 或 2（可选） |
| `baselineCount` | Integer | N |
| `basicCourseCount` | Number | \(\overline{B}\)（实现时 Integer/Double 与四舍五入规则一致即可） |
| `advancedCourseCount` | Number | \(\overline{A}\) |
| `practicalCourseCount` | Number | \(\overline{P}\) |
| `basicAvgCompletedCount` | Integer | \(T_b/\overline{B}\) 四舍五入 |
| `advancedAvgCompletedCount` | Integer | \(T_a/\overline{A}\) 四舍五入 |
| `practicalAvgCompletedCount` | Integer | \(T_p/\overline{P}\) 四舍五入 |
| `basicAvgCompletionRate` | Double | 百分比，2 位小数 |
| `advancedAvgCompletionRate` | Double | 同上 |
| `practicalAvgCompletionRate` | Double | 同上 |

**排序**：建议 L1 → L2 → L3；无数据档位是否省略由产品定。

---

## 6. 错误与边界

| 场景 | 建议 |
|------|------|
| `deptId` 为空 | 400 |
| `personType` 非 1、2 | 400 |
| 过滤后无人 | `data = []` |
| \(\overline{B}=0\)（或进阶/实战为 0） | 对应维度平均完课人数、完课率按 0 处理，避免除零 |

---

## 7. 定稿摘要

1. **仅处理** `personType = 1`（干部）、`2`（专家）。
2. **平均完课人数** = **该维度总完课数 ÷ 该维度平均目标课程数**（即 \(T/\overline{\text{target}}\)）。
3. **不与** `dept_course_selections` **联动**；目标课程数仅用 `t_employee_training_info` 三列平均值参与计算。

---

## 8. 待与产品确认（可选）

- `*_target_courses_num` 为 `NULL` 时是否按 **0** 参与平均与分母。
- 成熟度除 L1/L2/L3 外其它取值的处理方式。
