# 接口文档：查询PL、TM任职与认证数据

## 接口概述

**接口路径**：`GET /entry-level-manager/pl-tm-cert-statistics`

**接口描述**：查询研发管理部下各四级部门的PL、TM任职与认证统计数据。统计各四级部门以及研发管理部整体的PL/TM总人数、通过任职标准的人数及占比、通过认证标准的人数及占比。

**业务逻辑**：
1. 查询`t_entry_level_manager`表中`job_name_cn`为'PL'或'TM'的数据
2. 筛选`l3_department_code`为研发管理部（部门编码：030681）的数据
3. 统计研发管理部下各个四级部门（`l4_department_code`）的信息
4. 对每个四级部门统计：
   - PL/TM总人数
   - 通过任职标准的人数（`is_qualifications_standard = 1`）及任职占比
   - 通过认证标准的人数（`is_cert_standard = 1`）及认证占比
5. 汇总研发管理部整体数据：
   - PL/TM总人数
   - 通过任职标准的人数及任职占比
   - 通过认证标准的人数及认证占比

**常量配置**：
- 研发管理部部门编码：`030681`（`l3_department_code`）
- 职位名称：`job_name_cn IN ('PL', 'TM')`
- 任职标准：`is_qualifications_standard = 1`（1-达标，0-不达标）
- 认证标准：`is_cert_standard = 1`（1-达标，0-不达标）

## 请求参数

**说明**：本接口无需任何请求参数，所有查询条件均为固定常量。

### 请求示例

```bash
GET /entry-level-manager/pl-tm-cert-statistics
```

## 响应结果

### 成功响应

```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "summary": {
      "deptCode": "030681",
      "deptName": "研发管理部",
      "totalCount": 150,
      "qualifiedCount": 120,
      "qualifiedRatio": 0.80,
      "certCount": 100,
      "certRatio": 0.67
    },
    "departmentList": [
      {
        "deptCode": "030681001",
        "deptName": "部门A",
        "totalCount": 50,
        "qualifiedCount": 40,
        "qualifiedRatio": 0.80,
        "certCount": 35,
        "certRatio": 0.70
      },
      {
        "deptCode": "030681002",
        "deptName": "部门B",
        "totalCount": 60,
        "qualifiedCount": 50,
        "qualifiedRatio": 0.83,
        "certCount": 40,
        "certRatio": 0.67
      },
      {
        "deptCode": "030681003",
        "deptName": "部门C",
        "totalCount": 40,
        "qualifiedCount": 30,
        "qualifiedRatio": 0.75,
        "certCount": 25,
        "certRatio": 0.63
      }
    ]
  }
}
```

### 响应字段说明

| 字段名 | 类型 | 说明 |
|--------|------|------|
| code | Integer | 响应码，200表示成功 |
| message | String | 响应消息 |
| data | Object | 响应数据 |
| data.summary | Object | 研发管理部汇总数据 |
| data.summary.deptCode | String | 研发管理部部门编码（030681） |
| data.summary.deptName | String | 研发管理部部门名称 |
| data.summary.totalCount | Integer | PL/TM总人数 |
| data.summary.qualifiedCount | Integer | 通过任职标准的人数（is_qualifications_standard=1） |
| data.summary.qualifiedRatio | Double | 任职占比（qualifiedCount/totalCount，保留2位小数） |
| data.summary.certCount | Integer | 通过认证标准的人数（is_cert_standard=1） |
| data.summary.certRatio | Double | 认证占比（certCount/totalCount，保留2位小数） |
| data.departmentList | Array | 各四级部门统计数据列表 |
| data.departmentList[].deptCode | String | 四级部门编码（l4_department_code） |
| data.departmentList[].deptName | String | 四级部门名称（l4_department_cn_name） |
| data.departmentList[].totalCount | Integer | 该部门PL/TM总人数 |
| data.departmentList[].qualifiedCount | Integer | 该部门通过任职标准的人数 |
| data.departmentList[].qualifiedRatio | Double | 该部门任职占比 |
| data.departmentList[].certCount | Integer | 该部门通过认证标准的人数 |
| data.departmentList[].certRatio | Double | 该部门认证占比 |

### 失败响应

```json
{
  "code": 500,
  "message": "错误信息描述",
  "data": null
}
```

## 业务逻辑详细说明

### 1. 查询四级部门列表

**SQL逻辑**：
```sql
SELECT DISTINCT 
    l4_department_code AS deptCode,
    l4_department_cn_name AS deptName
FROM t_entry_level_manager
WHERE l3_department_code = '030681'
  AND job_name_cn IN ('PL', 'TM')
  AND l4_department_code IS NOT NULL
  AND l4_department_code != ''
ORDER BY l4_department_code
```

**说明**：查询研发管理部（`l3_department_code = '030681'`）下所有有PL或TM人员的四级部门信息，去重后返回部门编码和部门名称。

### 2. 统计各四级部门数据

**SQL逻辑**：
```sql
SELECT 
    l4_department_code AS deptCode,
    l4_department_cn_name AS deptName,
    COUNT(*) AS totalCount,
    SUM(CASE WHEN is_qualifications_standard = 1 THEN 1 ELSE 0 END) AS qualifiedCount,
    ROUND(SUM(CASE WHEN is_qualifications_standard = 1 THEN 1 ELSE 0 END) * 1.0 / COUNT(*), 4) AS qualifiedRatio,
    SUM(CASE WHEN is_cert_standard = 1 THEN 1 ELSE 0 END) AS certCount,
    ROUND(SUM(CASE WHEN is_cert_standard = 1 THEN 1 ELSE 0 END) * 1.0 / COUNT(*), 4) AS certRatio
FROM t_entry_level_manager
WHERE l3_department_code = '030681'
  AND job_name_cn IN ('PL', 'TM')
  AND l4_department_code IS NOT NULL
  AND l4_department_code != ''
GROUP BY l4_department_code, l4_department_cn_name
ORDER BY l4_department_code
```

**说明**：
- 统计每个四级部门的PL/TM总人数
- 统计每个四级部门中`is_qualifications_standard = 1`的人数（通过任职标准）
- 计算任职占比：通过任职标准人数 / 总人数
- 统计每个四级部门中`is_cert_standard = 1`的人数（通过认证标准）
- 计算认证占比：通过认证标准人数 / 总人数
- 占比保留4位小数（前端可自行格式化显示）

### 3. 统计研发管理部汇总数据

**SQL逻辑**：
```sql
SELECT 
    '030681' AS deptCode,
    MAX(l3_department_cn_name) AS deptName,
    COUNT(*) AS totalCount,
    SUM(CASE WHEN is_qualifications_standard = 1 THEN 1 ELSE 0 END) AS qualifiedCount,
    ROUND(SUM(CASE WHEN is_qualifications_standard = 1 THEN 1 ELSE 0 END) * 1.0 / COUNT(*), 4) AS qualifiedRatio,
    SUM(CASE WHEN is_cert_standard = 1 THEN 1 ELSE 0 END) AS certCount,
    ROUND(SUM(CASE WHEN is_cert_standard = 1 THEN 1 ELSE 0 END) * 1.0 / COUNT(*), 4) AS certRatio
FROM t_entry_level_manager
WHERE l3_department_code = '030681'
  AND job_name_cn IN ('PL', 'TM')
```

**说明**：
- 统计研发管理部整体（所有四级部门汇总）的PL/TM总人数
- 统计研发管理部整体中`is_qualifications_standard = 1`的人数
- 计算研发管理部整体的任职占比
- 统计研发管理部整体中`is_cert_standard = 1`的人数
- 计算研发管理部整体的认证占比

### 4. 数据说明

**任职标准（is_qualifications_standard）**：
- `1`：达标（获得3+（包括3级）的AI任职）
- `0`：不达标

**认证标准（is_cert_standard）**：
- `1`：达标（通过专业级）
- `0`：不达标

**统计范围**：
- 只统计`job_name_cn`为'PL'或'TM'的数据
- 只统计`l3_department_code`为'030681'（研发管理部）的数据
- 只统计有四级部门编码的数据（`l4_department_code IS NOT NULL AND l4_department_code != ''`）

## 注意事项

1. **数据去重**：如果同一员工有多条PL或TM记录，需要根据业务需求决定统计策略（是否按员工去重，还是按记录统计）
2. **空值处理**：
   - 如果某个四级部门没有PL/TM人员，该部门不会出现在`departmentList`中
   - 如果研发管理部下没有任何PL/TM人员，`summary.totalCount`为0，占比为0或null
3. **占比计算**：
   - 占比 = 通过标准人数 / 总人数
   - 当总人数为0时，占比应返回0或null
   - 占比保留4位小数，前端可根据需要格式化显示（如保留2位小数或百分比形式）
4. **性能考虑**：如果数据量较大，建议在相关字段上建立索引：
   - `l3_department_code`
   - `job_name_cn`
   - `l4_department_code`
   - `is_qualifications_standard`
   - `is_cert_standard`
5. **日志记录**：建议记录查询操作的详细日志，便于问题排查

## 待确认事项

1. **统计粒度**：如果同一员工有多条PL或TM记录，是按员工去重统计还是按记录统计？
2. **空值处理**：当总人数为0时，占比字段应该返回0还是null？
3. **数据范围**：是否需要过滤某些状态的数据（如`status`字段）？
4. **部门排序**：四级部门列表的排序方式（按部门编码排序还是按部门名称排序）？

