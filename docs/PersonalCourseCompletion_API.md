# 个人课程完成情况查询接口文档

## 1. 接口概述

本接口用于查询个人名下的课程完成情况，包括不同训战分类下的课程总数、目标课程数、实际完课数和完课占比。

## 2. 接口信息

### 2.1 查询个人课程完成情况

**接口路径：** `/personal-course/completion`

**请求方式：** `GET`

**接口描述：** 从cookie中获取用户信息，查询该用户在不同训战分类下的课程完成情况。

**请求参数：**

| 参数名 | 参数类型 | 参数位置 | 是否必填 | 说明 |
|--------|---------|---------|---------|------|
| account | String | Cookie | 否 | 用户工号（从cookie中获取，如果未提供则从HttpServletRequest中获取） |

**请求示例：**

```http
GET /personal-course/completion HTTP/1.1
Host: example.com
Cookie: account=Z123456
```

**响应参数：**

| 参数名 | 参数类型 | 说明 |
|--------|---------|------|
| code | Integer | 响应状态码，200表示成功，其他表示失败 |
| message | String | 响应消息 |
| data | PersonalCourseCompletionResponseVO | 课程完成情况数据 |

**PersonalCourseCompletionResponseVO 结构：**

| 参数名 | 参数类型 | 说明 |
|--------|---------|------|
| empNum | String | 员工工号（不带首字母，如 123456） |
| empName | String | 员工姓名 |
| courseStatistics | List<CourseCategoryStatisticsVO> | 各训战分类的课程统计列表 |

**CourseCategoryStatisticsVO 结构：**

| 参数名 | 参数类型 | 说明 |
|--------|---------|------|
| courseLevel | String | 训战分类（课程级别） |
| totalCourses | Integer | 课程总数 |
| targetCourses | Integer | 目标课程数（等于课程总数） |
| completedCourses | Integer | 实际完课数 |
| completionRate | Double | 完课占比（百分比，保留2位小数） |
| courseList | List<CourseInfoVO> | 该分类下的所有目标课程列表（包含已完成和未完成的课程） |

**CourseInfoVO 结构：**

| 参数名 | 参数类型 | 说明 |
|--------|---------|------|
| courseName | String | 课程名称 |
| courseNumber | String | 课程编码 |
| isCompleted | Boolean | 是否已完成（true表示已完成，false表示未完成） |

**说明：** `courseList` 包含该分类下的所有目标课程信息，每个课程都包含 `isCompleted` 字段来标识该用户是否已完成该课程。

**响应示例（成功）：**

```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "empNum": "123456",
    "empName": "张三",
    "courseStatistics": [
      {
        "courseLevel": "L1",
        "totalCourses": 10,
        "targetCourses": 10,
        "completedCourses": 8,
        "completionRate": 80.00,
        "courseList": [
          {
            "courseName": "AI基础课程",
            "courseNumber": "COURSE001",
            "isCompleted": true
          },
          {
            "courseName": "机器学习入门",
            "courseNumber": "COURSE002",
            "isCompleted": false
          }
        ]
      },
      {
        "courseLevel": "L2",
        "totalCourses": 5,
        "targetCourses": 5,
        "completedCourses": 3,
        "completionRate": 60.00,
        "courseList": [
          {
            "courseName": "深度学习进阶",
            "courseNumber": "COURSE003",
            "isCompleted": true
          }
        ]
      }
    ]
  }
}
```

**响应示例（失败 - 未获取到用户信息）：**

```json
{
  "code": 400,
  "message": "未获取到用户信息，请先登录",
  "data": null
}
```

**响应示例（失败 - 系统异常）：**

```json
{
  "code": 500,
  "message": "系统异常：具体错误信息",
  "data": null
}
```

## 3. 内部方法说明

### 3.1 从Cookie获取用户信息

**方法名：** `getUserAccountFromCookie`

**方法描述：** 从HTTP请求的Cookie中获取用户工号信息，返回包含 `empNum`（带首字母的工号）和 `w3Account`（去除首字母的工号）的用户信息对象

**调用方式：** 使用 `userConfigService.getUserAccountFromCookie(request, accountCookie)` 方法

**参数：**
- `HttpServletRequest request`: HTTP请求对象
- `@CookieValue(value = "account", required = false) String accountCookie`: 从Cookie注解获取的account值

**返回值：** `UserAccountResponseVO` - 用户工号信息对象，如果未获取到则返回null

**UserAccountResponseVO 结构：**
- `empNum`: String - 不带首字母的工号（如 123456），**此字段作为用户工号用于查询完课情况**
- `w3Account`: String - 带首字母的工号（如 Z123456），不在返回数据中使用

**逻辑说明：**
1. 优先使用 `@CookieValue` 注解获取的 `account` cookie值
2. 如果未获取到，则从 `HttpServletRequest` 中遍历所有cookie，查找名称为 `account` 的cookie
3. 如果工号以字母开头，去除首字母得到 `empNum`；否则 `empNum` 与原始值相同
4. `w3Account` 为带首字母的工号（如 Z123456）
5. 返回 `UserAccountResponseVO` 对象，其中 `empNum` 字段（不带首字母的工号）用于后续的完课情况查询

### 3.2 获取课程总数（按训战分类）

**方法名：** `getCourseStatisticsByLevel`

**方法描述：** 统计 `ai_course_planning_info` 表中不同 `course_level` 分类下的课程信息

**参数：** 无

**返回值：** `List<CourseCategoryStatisticsVO>` - 各分类的课程统计列表

**SQL逻辑：**
```sql
SELECT 
    course_level AS courseLevel,
    course_name AS courseName,
    course_number AS courseNumber
FROM ai_course_planning_info
GROUP BY course_level, course_name, course_number
ORDER BY course_level
```

**说明：**
- 根据 `course_level` 字段进行分类
- 查询每个分类下的 `course_name`（课程名称）和 `course_number`（课程编码）
- 统计每个分类下的课程总数

### 3.3 获取实际完课数

**方法名：** `getCompletedCoursesCount`

**方法描述：** 统计用户在指定课程分类下的实际完课数

**参数：**
- `String empNum`: 用户工号（使用 `UserAccountResponseVO` 的 `empNum` 字段，即不带首字母的工号）
- `List<String> courseNumbers`: 目标课程编码列表

**返回值：** `Map<String, Integer>` - key为课程编码，value为是否完成（1表示完成，0表示未完成）

**SQL逻辑：**
```sql
-- 查询微学习完课记录
SELECT DISTINCT course_team_code AS courseNumber
FROM t_micro_study_info_sync
WHERE emp_num = #{empNum}
  AND course_team_code IN 
  <foreach collection="courseNumbers" item="courseNumber" open="(" separator="," close=")">
    #{courseNumber}
  </foreach>
  AND is_pass = '1'

UNION

-- 查询MOOC完课记录
SELECT DISTINCT course_team_code AS courseNumber
FROM t_mooc_study_info_sync
WHERE emp_num = #{empNum}
  AND course_team_code IN 
  <foreach collection="courseNumbers" item="courseNumber" open="(" separator="," close=")">
    #{courseNumber}
  </foreach>
  AND is_pass = '1'
```

**说明：**
- 从 `t_micro_study_info_sync` 和 `t_mooc_study_info_sync` 两个表中查询
- 根据 `emp_num`（员工工号，使用 `empNum` 字段值，即不带首字母的工号）和 `course_team_code`（课程编码）进行过滤
- 只统计 `is_pass = '1'` 的记录（表示已通过/已完成）
- 使用 UNION 去重，确保同一课程只统计一次
- **重要：** 查询时使用 `empNum`（不带首字母的工号）作为 `emp_num` 的值

### 3.4 计算完课占比

**计算公式：**
```
完课占比 = (实际完课数 / 目标课程数) × 100
```

**保留精度：** 保留2位小数

## 4. 数据流程

1. **获取用户信息**
   - 调用 `userConfigService.getUserAccountFromCookie(request, accountCookie)` 方法
   - 从Cookie中获取用户工号信息，返回 `UserAccountResponseVO` 对象
   - 如果未获取到，返回错误提示
   - 从返回对象中提取 `empNum` 字段（不带首字母的工号）作为用户工号（用于后续查询）

2. **获取课程统计信息**
   - 查询 `ai_course_planning_info` 表
   - 按 `course_level` 分类统计课程总数
   - 获取每个分类下的课程名称和编码

3. **计算目标课程数**
   - 目标课程数 = 课程总数

4. **统计实际完课数**
   - 使用 `empNum`（不带首字母的工号）和课程编码列表
   - 查询 `t_micro_study_info_sync` 和 `t_mooc_study_info_sync` 表
   - 使用 `empNum` 作为 `emp_num` 的查询条件
   - 统计 `is_pass = '1'` 的记录数

5. **计算完课占比**
   - 完课占比 = (实际完课数 / 目标课程数) × 100

6. **组装返回数据**
   - 按分类组织数据
   - 返回完整的课程完成情况统计
   - 返回数据中的 `empNum` 使用 `UserAccountResponseVO` 的 `empNum` 字段（不带首字母的工号）
   - 不返回 `w3Account` 字段

## 5. 错误处理

| 错误场景 | HTTP状态码 | 错误消息 |
|---------|-----------|---------|
| 未获取到用户信息 | 400 | "未获取到用户信息，请先登录" |
| 数据库查询异常 | 500 | "系统异常：具体错误信息" |
| 参数校验失败 | 400 | "参数校验失败：具体错误信息" |

## 6. 注意事项

1. **用户工号获取：** 使用 `userConfigService.getUserAccountFromCookie()` 方法获取用户信息，该方法返回 `UserAccountResponseVO` 对象
   - `empNum`: 不带首字母的工号（如 123456），**用于查询完课情况时的 `emp_num` 字段，并在返回数据中展示**
   - `w3Account`: 带首字母的工号（如 Z123456），不在返回数据中使用
2. **完课统计查询：** 查询 `t_micro_study_info_sync` 和 `t_mooc_study_info_sync` 表时，必须使用 `empNum`（不带首字母的工号）作为 `emp_num` 的查询条件
3. **完课统计需要同时查询两个表（`t_micro_study_info_sync` 和 `t_mooc_study_info_sync`），使用UNION去重**
4. 只有 `is_pass = '1'` 的记录才视为完课
5. 完课占比保留2位小数，使用百分比格式
6. 如果某个分类下没有课程，该分类仍应返回，但各项数值为0

## 7. 数据库表结构说明

### 7.1 ai_course_planning_info（课程规划信息表）

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | Integer | 主键ID |
| course_level | String | 课程级别（训战分类） |
| course_name | String | 课程名称 |
| course_number | String | 课程编码 |
| ... | ... | 其他字段 |

### 7.2 t_micro_study_info_sync（微学习信息同步表）

| 字段名 | 类型 | 说明 |
|--------|------|------|
| emp_num | String | 员工工号 |
| course_team_code | String | 课程编码 |
| is_pass | String | 是否通过（'1'表示通过） |
| ... | ... | 其他字段 |

### 7.3 t_mooc_study_info_sync（MOOC学习信息同步表）

| 字段名 | 类型 | 说明 |
|--------|------|------|
| emp_num | String | 员工工号 |
| course_team_code | String | 课程编码 |
| is_pass | String | 是否通过（'1'表示通过） |
| ... | ... | 其他字段 |

