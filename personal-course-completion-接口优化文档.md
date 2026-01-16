# 个人课程完成情况接口优化文档

## 1. 优化目标

修改 `/personal-course/completion` 接口，将**目标完课数**从"所有规划课程"改为"部门选定的目标课程"，即只统计用户所在四级部门选定的目标课程的完课情况。

## 2. 当前实现逻辑

### 2.1 当前数据获取方式

- **课程总数**：从 `ai_course_planning_info` 表查询所有课程，按 `course_level` 分组统计
- **目标完课数**：等于课程总数（所有规划课程都是目标课程）
- **实际完课数**：从 `t_micro_study_info_sync` 和 `t_mooc_study_info_sync` 查询用户已完成的课程

### 2.2 当前问题

当前实现统计的是所有规划课程的完课情况，但实际应该只统计用户所在部门选定的目标课程的完课情况。

## 3. 优化后的实现逻辑

### 3.1 数据获取流程

```
1. 获取用户工号（empNum，不带首字母）
   ↓
2. 通过工号查询 t_employee_sync 表，获取 fourthdeptcode（四级部门ID）
   ↓
3. 通过四级部门ID，调用 coursePlanningInfoMapper.getAllDeptSelections() 获取部门选课信息
   ↓
4. 判断部门是否有选课信息
   ├─ 有选课信息 → 从部门选课信息中解析目标课程ID列表（courseSelections 字段，逗号分隔的字符串）
   └─ 无选课信息 → 使用所有课程作为目标课程（fallback逻辑）
   ↓
5. 查询目标课程详细信息
   ├─ 有选课信息 → 通过目标课程ID列表，查询 ai_course_planning_info 表获取目标课程详细信息
   └─ 无选课信息 → 查询 ai_course_planning_info 表获取所有课程信息
   ↓
6. 按课程级别（course_level）分组统计目标课程
   ↓
7. 只统计目标课程中的完课数据（从 t_micro_study_info_sync 和 t_mooc_study_info_sync 查询）
   ↓
8. 计算完课占比 = (实际完课数 / 目标课程数) × 100
```

### 3.2 关键改动点

1. **目标课程来源**：优先使用"部门选定的目标课程"，如果部门没有选课信息，则默认使用"所有规划课程"
2. **目标完课数**：等于部门选定的目标课程数量；如果部门没有选课信息，则等于所有课程数量
3. **完课统计范围**：只统计目标课程中的完课数据
4. **课程列表**：只返回目标课程列表，非目标课程不显示
5. **默认行为**：如果用户所在四级部门没有对应的选课信息，则默认选课为所有课程，即目标课程与课程总数一致

## 4. 需要新增的方法

### 4.1 Mapper 层新增方法

#### 4.1.1 PersonalCourseCompletionMapper.java

新增方法：根据员工工号查询四级部门ID

```java
/**
 * 根据员工工号查询四级部门ID（fourthdeptcode）
 * @param employeeNumber 员工工号（不带首字母）
 * @return 四级部门ID，如果未找到返回null
 */
String getFourthDeptCodeByEmployeeNumber(@Param("employeeNumber") String employeeNumber);
```

#### 4.1.2 PersonalCourseCompletionMapper.xml

新增SQL查询：

```xml
<!-- 根据员工工号查询四级部门ID -->
<select id="getFourthDeptCodeByEmployeeNumber" resultType="java.lang.String">
    SELECT fourthdeptcode
    FROM t_employee_sync
    WHERE employee_number = #{employeeNumber}
    LIMIT 1
</select>
```

#### 4.1.3 PersonalCourseCompletionMapper.java

新增方法：根据课程ID列表查询课程信息（按级别分类）

```java
/**
 * 根据课程ID列表查询课程信息（按级别分类）
 * @param courseIds 课程ID列表
 * @return 课程信息列表
 */
List<CourseInfoByLevelVO> getCourseInfoByLevelAndIds(@Param("courseIds") List<Integer> courseIds);
```

#### 4.1.4 PersonalCourseCompletionMapper.xml

新增SQL查询：

```xml
<!-- 根据课程ID列表查询课程信息（按级别分类） -->
<select id="getCourseInfoByLevelAndIds" resultType="com.huawei.aitransform.entity.CourseInfoByLevelVO">
    SELECT 
        course_level AS courseLevel,
        course_name AS courseName,
        course_number AS courseNumber,
        big_type AS bigType,
        course_link AS courseLink
    FROM ai_course_planning_info
    WHERE id IN
    <foreach collection="courseIds" item="courseId" open="(" separator="," close=")">
        #{courseId}
    </foreach>
      AND course_level IS NOT NULL
      AND course_name IS NOT NULL
      AND course_number IS NOT NULL
    ORDER BY course_level, course_number
</select>
```

### 4.2 Service 层修改

#### 4.2.1 PersonalCourseCompletionService.java

需要注入 `CoursePlanningInfoMapper`：

```java
@Autowired
private CoursePlanningInfoMapper coursePlanningInfoMapper;
```

#### 4.2.2 修改 getPersonalCourseCompletion 方法逻辑

主要修改点：

1. **获取四级部门ID**
   ```java
   // 查询员工四级部门ID
   String fourthDeptCode = personalCourseCompletionMapper.getFourthDeptCodeByEmployeeNumber(empNum);
   if (fourthDeptCode == null || fourthDeptCode.trim().isEmpty()) {
       // 如果未找到部门信息，可以返回空数据或抛出异常（根据业务需求决定）
       // 这里建议返回空数据，避免影响用户体验
   }
   ```

2. **获取部门选定的目标课程**
   ```java
   // 获取所有部门选课信息
   List<DeptCourseSelection> allDeptSelections = coursePlanningInfoMapper.getAllDeptSelections();
   
   // 查找当前用户所在部门的选课信息
   DeptCourseSelection userDeptSelection = null;
   if (fourthDeptCode != null && !fourthDeptCode.trim().isEmpty()) {
       for (DeptCourseSelection selection : allDeptSelections) {
           if (selection.getDeptCode().equals(fourthDeptCode)) {
               userDeptSelection = selection;
               break;
           }
       }
   }
   
   // 解析目标课程ID列表
   List<Integer> targetCourseIds = new ArrayList<>();
   boolean useAllCourses = false; // 标记是否使用所有课程
   
   if (userDeptSelection != null && userDeptSelection.getCourseSelections() != null 
           && !userDeptSelection.getCourseSelections().trim().isEmpty()) {
       // 部门有选课信息，解析选定的课程ID
       String courseSelectionsStr = userDeptSelection.getCourseSelections();
       String[] courseIdStrs = courseSelectionsStr.split(",");
       for (String courseIdStr : courseIdStrs) {
           courseIdStr = courseIdStr.trim();
           if (!courseIdStr.isEmpty()) {
               try {
                   targetCourseIds.add(Integer.parseInt(courseIdStr));
               } catch (NumberFormatException e) {
                   // 忽略无效的课程ID
               }
           }
       }
       // 如果解析后没有有效的课程ID，则使用所有课程
       if (targetCourseIds.isEmpty()) {
           useAllCourses = true;
       }
   } else {
       // 部门没有选课信息，使用所有课程作为默认目标课程
       useAllCourses = true;
   }
   ```

3. **查询目标课程信息**
   ```java
   List<CourseInfoByLevelVO> targetCourses;
   
   if (useAllCourses) {
       // 使用所有课程作为目标课程
       targetCourses = personalCourseCompletionMapper.getCourseInfoByLevel();
   } else {
       // 根据目标课程ID列表查询课程信息
       targetCourses = personalCourseCompletionMapper.getCourseInfoByLevelAndIds(targetCourseIds);
   }
   ```

4. **后续统计逻辑保持不变**
   - 按课程级别分组
   - 查询完课数据（只查询目标课程的完课数据）
   - 计算完课占比

## 5. Fallback 逻辑说明

### 5.1 默认行为

**重要规则**：如果用户所在四级部门没有对应的选课信息，则默认选课为所有课程，即目标课程与课程总数一致。

### 5.2 触发 Fallback 的场景

以下情况会触发 fallback 逻辑，使用所有课程作为目标课程：

1. **部门没有选课记录**
   - 在 `dept_course_selections` 表中找不到对应 `fourthdeptcode` 的记录

2. **部门选课信息为空**
   - `course_selections` 字段为 `null` 或空字符串

3. **部门选课信息解析后为空**
   - `course_selections` 字段解析后，所有课程ID都无效或不存在，导致 `targetCourseIds` 列表为空

### 5.3 Fallback 逻辑实现

```java
// 判断是否使用所有课程
boolean useAllCourses = false;

if (userDeptSelection == null || 
    userDeptSelection.getCourseSelections() == null || 
    userDeptSelection.getCourseSelections().trim().isEmpty()) {
    // 场景1和2：部门没有选课信息或选课信息为空
    useAllCourses = true;
} else {
    // 解析选课信息
    // ... 解析逻辑 ...
    
    if (targetCourseIds.isEmpty()) {
        // 场景3：解析后没有有效的课程ID
        useAllCourses = true;
    }
}

// 根据标志位决定查询方式
if (useAllCourses) {
    // 使用所有课程（fallback）
    targetCourses = personalCourseCompletionMapper.getCourseInfoByLevel();
} else {
    // 使用部门选定的目标课程
    targetCourses = personalCourseCompletionMapper.getCourseInfoByLevelAndIds(targetCourseIds);
}
```

### 5.4 Fallback 后的数据表现

当触发 fallback 逻辑时：
- `totalCourses` = 所有规划课程数量
- `targetCourses` = 所有规划课程数量（与 `totalCourses` 一致）
- `completedCourses` = 所有课程中的完课数
- `courseList` = 所有规划课程列表
- `completionRate` = (所有课程中的完课数 / 所有课程数) × 100

**注意**：fallback 后的行为与优化前的行为一致，确保向后兼容。

## 6. 详细实现步骤

### 6.1 步骤1：新增查询四级部门ID的方法

**文件**：`PersonalCourseCompletionMapper.java` 和 `PersonalCourseCompletionMapper.xml`

**实现**：
- 在 Mapper 接口中新增 `getFourthDeptCodeByEmployeeNumber` 方法
- 在 XML 中新增对应的 SQL 查询语句

### 6.2 步骤2：新增根据课程ID列表查询课程信息的方法

**文件**：`PersonalCourseCompletionMapper.java` 和 `PersonalCourseCompletionMapper.xml`

**实现**：
- 在 Mapper 接口中新增 `getCourseInfoByLevelAndIds` 方法
- 在 XML 中新增对应的 SQL 查询语句，使用 `<foreach>` 标签处理课程ID列表

### 6.3 步骤3：修改 Service 层逻辑

**文件**：`PersonalCourseCompletionService.java`

**修改内容**：

1. **注入 CoursePlanningInfoMapper**
   ```java
   @Autowired
   private CoursePlanningInfoMapper coursePlanningInfoMapper;
   ```

2. **修改 getPersonalCourseCompletion 方法**
   - 在方法开始处，先查询员工的四级部门ID
   - 如果未找到部门信息，根据业务需求处理（建议返回空数据）
   - 调用 `coursePlanningInfoMapper.getAllDeptSelections()` 获取所有部门选课信息
   - 根据四级部门ID查找对应的部门选课信息
   - **判断部门是否有选课信息**：
     - 如果有选课信息：解析 `courseSelections` 字段（逗号分隔的课程ID字符串）为整数列表，使用 `getCourseInfoByLevelAndIds` 方法查询目标课程信息
     - 如果没有选课信息或选课信息为空：使用 `getCourseInfoByLevel()` 方法查询所有课程作为目标课程（fallback逻辑）
   - 后续统计逻辑保持不变，但只针对目标课程

### 6.4 步骤4：处理边界情况

**需要考虑的边界情况**：

1. **员工未找到或没有四级部门信息**
   - 处理方式：返回空统计数据，或返回错误提示（根据业务需求）

2. **部门未选定任何课程或选课信息为空**
   - 处理方式：**触发 fallback 逻辑，默认使用所有课程作为目标课程**，即目标完课数与课程总数一致，统计所有课程的完课情况
   - 实现：设置 `useAllCourses = true`，调用 `getCourseInfoByLevel()` 查询所有课程

3. **部门选定的课程ID无效或不存在**
   - 处理方式：在解析课程ID时过滤掉无效的ID，只统计有效的课程
   - 如果解析后所有课程ID都无效（`targetCourseIds.isEmpty()`），**触发 fallback 逻辑，使用所有课程**

4. **目标课程中没有完课记录**
   - 处理方式：正常返回，完课数为0，完课占比为0.00

## 7. 数据流程对比

### 6.1 优化前

```
获取工号
  ↓
查询所有课程（ai_course_planning_info）
  ↓
按级别分组
  ↓
统计所有课程的完课情况
  ↓
返回结果（目标完课数 = 所有课程数）
```

### 6.2 优化后

```
获取工号
  ↓
查询四级部门ID（t_employee_sync.fourthdeptcode）
  ↓
获取部门选课信息（dept_course_selections）
  ↓
判断部门是否有选课信息
  ├─ 有选课信息 → 解析目标课程ID列表
  │                ↓
  │              查询目标课程信息（ai_course_planning_info，根据ID过滤）
  │                ↓
  │              目标完课数 = 目标课程数
  │
  └─ 无选课信息 → 使用所有课程（fallback）
                  ↓
                 查询所有课程信息（ai_course_planning_info）
                  ↓
                 目标完课数 = 所有课程数
  ↓
按级别分组
  ↓
只统计目标课程的完课情况
  ↓
返回结果
```

## 8. 返回数据结构变化

### 7.1 优化前

- `totalCourses`：所有规划课程数量
- `targetCourses`：等于 `totalCourses`（所有课程都是目标课程）
- `completedCourses`：所有课程中的完课数
- `courseList`：所有规划课程列表

### 7.2 优化后

- `totalCourses`：目标课程数量（部门选定的课程，如果部门没有选课信息则等于所有课程数量）
- `targetCourses`：等于 `totalCourses`（目标课程数量）
- `completedCourses`：目标课程中的完课数
- `courseList`：只包含目标课程列表（如果部门没有选课信息则包含所有课程）

## 9. 数据库表说明

### 8.1 t_employee_sync（员工同步表）

| 字段名 | 类型 | 说明 |
|--------|------|------|
| employee_number | String | 员工工号（不带首字母） |
| fourthdeptcode | String | 四级部门编码 |
| last_name | String | 员工中文名 |

### 8.2 dept_course_selections（部门选课表）

| 字段名 | 类型 | 说明 |
|--------|------|------|
| dept_code | String | 部门编码（对应 fourthdeptcode） |
| dept_name | String | 部门名称 |
| course_selections | String | 选定的课程ID列表（逗号分隔，如："1,2,3,4"） |

### 8.3 ai_course_planning_info（课程规划信息表）

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | Integer | 课程主键ID |
| course_level | String | 课程级别（训战分类） |
| course_name | String | 课程名称 |
| course_number | String | 课程编码 |
| big_type | String | 课程主分类 |
| course_link | String | 课程链接 |

## 10. 注意事项

1. **性能考虑**
   - `getAllDeptSelections()` 会查询所有部门的选课信息，如果部门数量很大，可以考虑优化为按部门ID查询单个部门的选课信息
   - 如果后续需要优化，可以在 `CoursePlanningInfoMapper` 中新增按部门ID查询的方法

2. **数据一致性**
   - 确保 `dept_course_selections.dept_code` 与 `t_employee_sync.fourthdeptcode` 的格式一致
   - 确保 `course_selections` 中的课程ID在 `ai_course_planning_info` 表中存在

3. **错误处理**
   - 如果员工没有四级部门信息，需要明确处理方式
   - **如果部门未选定课程或选课信息为空，默认使用所有课程作为目标课程**，确保用户始终能看到完课统计

4. **向后兼容**
   - 接口路径和返回数据结构保持不变，只是数据来源和计算逻辑发生变化
   - 前端无需修改

## 11. 测试建议

1. **正常流程测试**
   - 员工有四级部门信息
   - 部门已选定目标课程
   - 目标课程中有完课记录

2. **边界情况测试**
   - 员工没有四级部门信息
   - **部门未选定任何课程（应使用所有课程作为默认目标课程）**
   - **部门选课信息为空字符串（应使用所有课程作为默认目标课程）**
   - 部门选定的课程ID无效或不存在（应过滤无效ID，如果全部无效则使用所有课程）
   - 目标课程中没有完课记录

3. **数据准确性测试**
   - 验证目标课程数量是否正确
   - 验证完课统计是否只包含目标课程
   - 验证完课占比计算是否正确

## 12. 后续优化建议

1. **性能优化**
   - 如果 `getAllDeptSelections()` 性能有问题，可以新增按部门ID查询单个部门选课信息的方法
   - 方法名建议：`getDeptSelectionByDeptCode(String deptCode)`

2. **缓存优化**
   - 部门选课信息可以加入缓存，减少数据库查询

3. **日志记录**
   - 记录关键步骤的日志，便于排查问题

