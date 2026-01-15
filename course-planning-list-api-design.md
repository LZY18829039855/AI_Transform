# 训战课程规划表 - 全量课程及部门选课情况查询接口文档

**文档版本**：V1.0  
**修改日期**：2026-01-15  
**接口状态**：设计中/待开发

---

## 1. 接口概述

**接口名称**：查询全量课程及部门选课情况  
**接口路径**：`/list` (建议全路径: `/api/course/list` 或根据现有 controller 前缀)  
**请求方式**：`GET`  
**描述**：  
查询所有的训战课程基础信息，并聚合展示每个课程被哪些部门选中了。
后端需要处理 `dept_course_selections` 表中的 `course_selections` 字段（逗号分隔的字符串），将其反向映射到具体的课程上，以便前端直接渲染“课程-部门”规划表。

---

## 2. 请求参数

| 参数名 | 位置 | 类型 | 必填 | 描述 | 示例值 |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `courseName` | Query | String | 否 | 课程名称模糊搜索 | "AI" |
| `courseType` | Query | String | 否 | 课程类型筛选 | "专业核心课" |
| `pageNum` | Query | Integer | 否 | 页码 (若支持分页) | 1 |
| `pageSize` | Query | Integer | 否 | 每页数量 (若支持分页) | 20 |

---

## 3. 响应结构

响应体为一个标准的 Result 包装对象，`data` 字段为课程列表。

### 3.1 响应参数说明

| 字段名 | 类型 | 描述 | 备注 |
| :--- | :--- | :--- | :--- |
| `code` | Integer | 状态码 | 200 表示成功 |
| `msg` | String | 提示信息 | |
| `data` | Array | 课程列表数据 | `List<CourseVO>` |
| ├─ `courseId` | Long/String | 课程主键ID | 对应 `course_selections` 中的值 |
| ├─ `courseName` | String | 课程名称 | |
| ├─ `courseType` | String | 课程类型 | |
| ├─ `credit` | Double | 学分 | |
| ├─ `...` | ... | 其他原有课程字段 | |
| └─ `selectedDepts` | Array | **[新增]** 已选该课程的部门列表 | 若无部门选择，返回空数组 `[]` |
| &nbsp;&nbsp;&nbsp;&nbsp;├─ `deptCode` | String | 部门编码/ID | 对应表 `dept_code` |
| &nbsp;&nbsp;&nbsp;&nbsp;└─ `deptName` | String | 部门名称 | 对应表 `dept_name` |

### 3.2 响应示例 (JSON)

```json
{
  "code": 200,
  "msg": "success",
  "data": [
    {
      "courseId": "1001",
      "courseName": "AI算法基础",
      "courseType": "专业核心课",
      "credit": 3.0,
      "description": "介绍AI的基本概念与算法...",
      "selectedDepts": [
        {
          "deptCode": "DEPT_001",
          "deptName": "算力基础设施部"
        },
        {
          "deptCode": "DEPT_002",
          "deptName": "AI算法研究部"
        }
      ]
    },
    {
      "courseId": "1002",
      "courseName": "项目管理进阶",
      "courseType": "通用技能",
      "credit": 1.5,
      "description": "敏捷开发流程实战...",
      "selectedDepts": [
        {
          "deptCode": "DEPT_001",
          "deptName": "算力基础设施部"
        }
      ]
    },
    {
      "courseId": "1003",
      "courseName": "未知领域探索",
      "courseType": "选修",
      "credit": 1.0,
      "description": "前沿技术分享...",
      "selectedDepts": [] 
    }
  ]
}
```

---

## 4. 后端实现逻辑建议

由于数据库中 `dept_course_selections` 表是“一个部门 -> 多个课程”的存储结构，而接口需要返回“一个课程 -> 多个部门”的展示结构，后端需在 Service 层进行数据重组。

### 4.1 数据源
1.  **课程基础数据**：查询课程表 (e.g., `course_info`) 获取所有课程。
2.  **部门选课数据**：查询 `dept_course_selections` 表，获取所有部门及其选课字符串 (`course_selections` 字段，格式如 `"1001,1002,1005"`).

### 4.2 处理流程
1.  **查询**：并行查询出 `List<Course>` 和 `List<DeptSelection>`。
2.  **构建映射 (Mapping)**：
    *   初始化一个 Map: `Map<String, List<DeptVO>> courseIdToDeptsMap`。
    *   遍历 `List<DeptSelection>`：
        *   将 `course_selections` 字符串按逗号 `,` 分割得到 `courseId` 数组。
        *   遍历分割后的 `courseId`：
            *   在 Map 中找到对应的 list（若无则创建）。
            *   将当前部门信息 (`deptCode`, `deptName`) 添加到 list 中。
3.  **结果组装**：
    *   遍历 `List<Course>`。
    *   使用课程的 `courseId` 去 Map 中获取对应的 `List<DeptVO>`。
    *   将获取到的部门列表设置到 `CourseVO.selectedDepts` 字段中（若 Map 中不存在，设为空列表）。
4.  **返回**：返回组装好的 `List<CourseVO>`。

### 4.3 实体类定义 (参考)

**DeptVO**:
```java
public class DeptVO {
    private String deptCode;
    private String deptName;
    // getter, setter...
}
```

**CourseVO** (继承自 Course 实体或新建 VO):
```java
public class CourseVO extends Course {
    // ... 原有字段
    
    // 新增字段
    private List<DeptVO> selectedDepts;
    
    // getter, setter...
}
```

