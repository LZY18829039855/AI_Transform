# 训战课程规划表 - 查询全量课程及部门选课情况接口文档

## 1. 接口概述
**接口名称**：查询全量课程及部门选课情况  
**接口地址**：`/list` (假设为 /course/list 或 /course-planning/list)  
**请求方式**：GET  
**描述**：查询所有的训战课程基础信息，并聚合展示每个课程被哪些部门选中了。后端需要处理 `dept_course_selections` 表中的 `course_selections` 字段（逗号分隔的字符串），将其反向映射到具体的课程上。

## 2. 请求参数
*(保持原有逻辑，通常可能包含分页或搜索参数)*
| 参数名 | 类型 | 是否必选 | 描述 |
| :--- | :--- | :--- | :--- |
| `pageNum` | Integer | 否 | 页码 (如适用) |
| `pageSize` | Integer | 否 | 每页条数 (如适用) |
| `courseName` | String | 否 | 课程名称模糊查询 |

## 3. 响应参数 (Response Body)

响应数据主要是一个课程列表 `List<CourseVO>`。

### 通用响应结构
| 字段名 | 类型 | 描述 | 备注 |
| :--- | :--- | :--- | :--- |
| `code` | Integer | 状态码 | 200 表示成功 |
| `msg` | String | 提示信息 | |
| `data` | Array | 课程列表数据 | |

### data 数组元素结构 (CourseVO)
| 字段名 | 类型 | 描述 | 备注 |
| :--- | :--- | :--- | :--- |
| `courseId` | Long/String | 课程主键ID | 对应 `course_selections` 中的值 |
| `courseName` | String | 课程名称 | |
| `courseType` | String | 课程类型 | |
| `credit` | Double | 学分 | |
| `description`| String | 课程描述 | |
| `...` | ... | 其他原有课程字段 | |
| `selectedDepts` | Array | **[新增]** 已选该课程的部门列表 | 若无部门选择，返回空数组 `[]` |

### selectedDepts 数组元素结构 (DepartmentVO)
| 字段名 | 类型 | 描述 | 备注 |
| :--- | :--- | :--- | :--- |
| `deptCode` | String | 部门编码/ID | 对应表 `dept_code` |
| `deptName` | String | 部门名称 | 对应表 `dept_name` |

## 4. 响应示例 (JSON)

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
      "description": "介绍AI基础算法...",
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
      "description": "敏捷开发流程...",
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

## 5. 后端逻辑实现建议

由于数据库中存的是 `One-to-Many` (一个部门对应多个课程字符串)，但接口返回需要 `One-to-Many` (一个课程对应多个部门)，后端需要做一次**数据重组**：

1.  **查询阶段**：
    *   查询出 `List<Course>` (所有课程)。
    *   查询出 `List<DeptSelection>` (所有部门选课记录，包含 `dept_code`, `dept_name`, `course_selections`)。

2.  **处理阶段 (内存匹配)**：
    *   创建一个 Map：`Map<String, List<DeptVO>> courseIdToDeptsMap`。
    *   遍历部门记录列表：
        *   获取 `course_selections` 字符串（例如 `"1001,1002"`）。
        *   使用逗号分割字符串得到课程ID数组。
        *   遍历这些课程ID，将当前的 `dept_code` 和 `dept_name` 添加到 Map 对应的课程ID下。

3.  **组装阶段**：
    *   遍历最终要返回的 `List<Course>`。
    *   根据 `courseId` 从 Map 中取出对应的部门列表，赋值给 `selectedDepts` 字段。
    *   如果 Map 中没有该课程ID，则赋值为空列表 `[]`。

