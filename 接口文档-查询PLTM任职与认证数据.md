# 接口文档：查询PL、TM、PM（项目经理）任职与认证数据

## 接口概述

**接口路径**：`GET /entry-level-manager/pl-tm-cert-statistics`

**接口描述**：查询研发管理部下各四级部门的PL、TM、PM（项目经理）任职与认证统计数据。PL和TM合并统计（作为一个整体），PM（项目经理）单独统计，两者分开统计。分别统计各四级部门以及研发管理部整体的PL/TM总人数、PM总人数、通过任职标准的人数及占比、通过认证标准的人数及占比。

**业务逻辑**：
1. 查询`t_entry_level_manager`表中`job_name_cn`为'PL'、'TM'或'项目经理'的数据
2. 筛选`l3_department_code`为研发管理部（部门编码：030681）的数据
3. **PL/TM合并统计**：
   - 查询`job_name_cn`为'PL'或'TM'的数据，将两者合并统计
   - 统计研发管理部下各个四级部门（`l4_department_code`）的PL/TM信息
   - 对每个四级部门统计：PL/TM总人数、通过任职标准的人数及占比、通过认证标准的人数及占比
   - 汇总研发管理部整体的PL/TM数据
4. **PM单独统计**：
   - 查询`job_name_cn`为'项目经理'的数据，单独统计
   - 统计研发管理部下各个四级部门（`l4_department_code`）的PM信息
   - 对每个四级部门统计：PM总人数、通过任职标准的人数及占比、通过认证标准的人数及占比
   - 汇总研发管理部整体的PM数据
5. **按部门维度组织响应数据**：
   - 汇总数据（`summary`）：包含研发管理部整体的PL/TM和PM两套统计数据
   - 部门列表（`departmentList`）：按四级部门组织，每个部门包含该部门的PL/TM和PM两套统计数据
   - **重要**：所有在`t_entry_level_manager`表中有PL/TM或PM数据的四级部门都会出现在列表中
   - **数据合并规则**：
     - 查询所有有PL/TM数据的四级部门列表
     - 查询所有有PM数据的四级部门列表
     - 合并两个列表，得到所有需要返回的部门（去重）
     - 对于每个部门，分别查询PL/TM和PM的统计数据
     - 如果某个部门只有PL/TM数据没有PM数据，该部门的`pm`字段中所有值都为0或0.0
     - 如果某个部门只有PM数据没有PL/TM数据，该部门的`plTm`字段中所有值都为0或0.0
     - 如果某个部门既有PL/TM数据也有PM数据，两个字段都有对应的统计数据

**常量配置**：
- 研发管理部部门编码：`030681`（`l3_department_code`）
- PL/TM职位名称：`job_name_cn IN ('PL', 'TM')`（合并统计）
- PM职位名称：`job_name_cn = '项目经理'`（单独统计）
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
      "plTm": {
        "totalCount": 150,
        "qualifiedCount": 120,
        "qualifiedRatio": 0.80,
        "certCount": 100,
        "certRatio": 0.67
      },
      "pm": {
        "totalCount": 80,
        "qualifiedCount": 60,
        "qualifiedRatio": 0.75,
        "certCount": 50,
        "certRatio": 0.63
      }
    },
    "departmentList": [
      {
        "deptCode": "030681001",
        "deptName": "部门A",
        "plTm": {
          "totalCount": 50,
          "qualifiedCount": 40,
          "qualifiedRatio": 0.80,
          "certCount": 35,
          "certRatio": 0.70
        },
        "pm": {
          "totalCount": 30,
          "qualifiedCount": 22,
          "qualifiedRatio": 0.73,
          "certCount": 18,
          "certRatio": 0.60
        }
      },
      {
        "deptCode": "030681002",
        "deptName": "部门B",
        "plTm": {
          "totalCount": 60,
          "qualifiedCount": 50,
          "qualifiedRatio": 0.83,
          "certCount": 40,
          "certRatio": 0.67
        },
        "pm": {
          "totalCount": 35,
          "qualifiedCount": 28,
          "qualifiedRatio": 0.80,
          "certCount": 22,
          "certRatio": 0.63
        }
      },
      {
        "deptCode": "030681003",
        "deptName": "部门C",
        "plTm": {
          "totalCount": 40,
          "qualifiedCount": 30,
          "qualifiedRatio": 0.75,
          "certCount": 25,
          "certRatio": 0.63
        },
        "pm": {
          "totalCount": 15,
          "qualifiedCount": 10,
          "qualifiedRatio": 0.67,
          "certCount": 10,
          "certRatio": 0.67
        }
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
| data.summary | Object | 研发管理部汇总数据（按部门维度） |
| data.summary.deptCode | String | 研发管理部部门编码（030681） |
| data.summary.deptName | String | 研发管理部部门名称 |
| data.summary.plTm | Object | PL/TM汇总统计数据（PL和TM合并统计） |
| data.summary.plTm.totalCount | Integer | PL/TM总人数（PL和TM合并统计） |
| data.summary.plTm.qualifiedCount | Integer | PL/TM通过任职标准的人数（is_qualifications_standard=1） |
| data.summary.plTm.qualifiedRatio | Double | PL/TM任职占比（qualifiedCount/totalCount，保留4位小数） |
| data.summary.plTm.certCount | Integer | PL/TM通过认证标准的人数（is_cert_standard=1） |
| data.summary.plTm.certRatio | Double | PL/TM认证占比（certCount/totalCount，保留4位小数） |
| data.summary.pm | Object | PM（项目经理）汇总统计数据（单独统计） |
| data.summary.pm.totalCount | Integer | PM总人数 |
| data.summary.pm.qualifiedCount | Integer | PM通过任职标准的人数（is_qualifications_standard=1） |
| data.summary.pm.qualifiedRatio | Double | PM任职占比（qualifiedCount/totalCount，保留4位小数） |
| data.summary.pm.certCount | Integer | PM通过认证标准的人数（is_cert_standard=1） |
| data.summary.pm.certRatio | Double | PM认证占比（certCount/totalCount，保留4位小数） |
| data.departmentList | Array | 各四级部门统计数据列表（按部门维度，每个部门包含PL/TM和PM统计） |
| data.departmentList[].deptCode | String | 四级部门编码（l4_department_code） |
| data.departmentList[].deptName | String | 四级部门名称（l4_department_cn_name） |
| data.departmentList[].plTm | Object | 该部门PL/TM统计数据（PL和TM合并统计） |
| data.departmentList[].plTm.totalCount | Integer | 该部门PL/TM总人数 |
| data.departmentList[].plTm.qualifiedCount | Integer | 该部门PL/TM通过任职标准的人数 |
| data.departmentList[].plTm.qualifiedRatio | Double | 该部门PL/TM任职占比 |
| data.departmentList[].plTm.certCount | Integer | 该部门PL/TM通过认证标准的人数 |
| data.departmentList[].plTm.certRatio | Double | 该部门PL/TM认证占比 |
| data.departmentList[].pm | Object | 该部门PM（项目经理）统计数据（单独统计） |
| data.departmentList[].pm.totalCount | Integer | 该部门PM总人数 |
| data.departmentList[].pm.qualifiedCount | Integer | 该部门PM通过任职标准的人数 |
| data.departmentList[].pm.qualifiedRatio | Double | 该部门PM任职占比 |
| data.departmentList[].pm.certCount | Integer | 该部门PM通过认证标准的人数 |
| data.departmentList[].pm.certRatio | Double | 该部门PM认证占比 |

### 失败响应

```json
{
  "code": 500,
  "message": "错误信息描述",
  "data": null
}
```

## 业务逻辑详细说明

### 1. PL/TM合并统计

#### 1.1 统计各四级部门PL/TM数据

**SQL逻辑**：
```sql
SELECT 
    l4_department_code AS deptCode,
    l4_department_cn_name AS deptName,
    COUNT(*) AS totalCount,
    SUM(CASE WHEN is_qualifications_standard = 1 THEN 1 ELSE 0 END) AS qualifiedCount,
    CASE 
        WHEN COUNT(*) = 0 THEN 0.0
        ELSE ROUND(SUM(CASE WHEN is_qualifications_standard = 1 THEN 1 ELSE 0 END) * 1.0 / COUNT(*), 4)
    END AS qualifiedRatio,
    SUM(CASE WHEN is_cert_standard = 1 THEN 1 ELSE 0 END) AS certCount,
    CASE 
        WHEN COUNT(*) = 0 THEN 0.0
        ELSE ROUND(SUM(CASE WHEN is_cert_standard = 1 THEN 1 ELSE 0 END) * 1.0 / COUNT(*), 4)
    END AS certRatio
FROM t_entry_level_manager
WHERE l3_department_code = '030681'
  AND job_name_cn IN ('PL', 'TM')
  AND l4_department_code IS NOT NULL
  AND l4_department_code != ''
GROUP BY l4_department_code, l4_department_cn_name
ORDER BY l4_department_code
```

**说明**：
- 统计每个四级部门的PL/TM总人数（PL和TM合并统计）
- 统计每个四级部门中`is_qualifications_standard = 1`的人数（通过任职标准）
- 计算任职占比：通过任职标准人数 / 总人数（当总人数为0时，占比为0.0）
- 统计每个四级部门中`is_cert_standard = 1`的人数（通过认证标准）
- 计算认证占比：通过认证标准人数 / 总人数（当总人数为0时，占比为0.0）
- 占比保留4位小数（前端可自行格式化显示）
- **注意**：即使某个部门没有PL/TM人员（totalCount为0），该部门也会出现在结果中

#### 1.2 统计研发管理部PL/TM汇总数据

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
- 统计研发管理部整体（所有四级部门汇总）的PL/TM总人数（PL和TM合并统计）
- 统计研发管理部整体中`is_qualifications_standard = 1`的人数
- 计算研发管理部整体的任职占比
- 统计研发管理部整体中`is_cert_standard = 1`的人数
- 计算研发管理部整体的认证占比

### 2. PM（项目经理）单独统计

#### 2.1 统计各四级部门PM数据

**SQL逻辑**：
```sql
SELECT 
    l4_department_code AS deptCode,
    l4_department_cn_name AS deptName,
    COUNT(*) AS totalCount,
    SUM(CASE WHEN is_qualifications_standard = 1 THEN 1 ELSE 0 END) AS qualifiedCount,
    CASE 
        WHEN COUNT(*) = 0 THEN 0.0
        ELSE ROUND(SUM(CASE WHEN is_qualifications_standard = 1 THEN 1 ELSE 0 END) * 1.0 / COUNT(*), 4)
    END AS qualifiedRatio,
    SUM(CASE WHEN is_cert_standard = 1 THEN 1 ELSE 0 END) AS certCount,
    CASE 
        WHEN COUNT(*) = 0 THEN 0.0
        ELSE ROUND(SUM(CASE WHEN is_cert_standard = 1 THEN 1 ELSE 0 END) * 1.0 / COUNT(*), 4)
    END AS certRatio
FROM t_entry_level_manager
WHERE l3_department_code = '030681'
  AND job_name_cn = '项目经理'
  AND l4_department_code IS NOT NULL
  AND l4_department_code != ''
GROUP BY l4_department_code, l4_department_cn_name
ORDER BY l4_department_code
```

**说明**：
- 统计每个四级部门的PM总人数（项目经理单独统计）
- 统计每个四级部门中`is_qualifications_standard = 1`的人数（通过任职标准）
- 计算任职占比：通过任职标准人数 / 总人数（当总人数为0时，占比为0.0）
- 统计每个四级部门中`is_cert_standard = 1`的人数（通过认证标准）
- 计算认证占比：通过认证标准人数 / 总人数（当总人数为0时，占比为0.0）
- 占比保留4位小数（前端可自行格式化显示）
- **注意**：即使某个部门没有PM人员（totalCount为0），该部门也会出现在结果中

#### 2.2 统计研发管理部PM汇总数据

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
  AND job_name_cn = '项目经理'
```

**说明**：
- 统计研发管理部整体（所有四级部门汇总）的PM总人数（项目经理单独统计）
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
- PL/TM统计：只统计`job_name_cn`为'PL'或'TM'的数据（合并统计）
- PM统计：只统计`job_name_cn`为'项目经理'的数据（单独统计）
- 只统计`l3_department_code`为'030681'（研发管理部）的数据
- 只统计有四级部门编码的数据（`l4_department_code IS NOT NULL AND l4_department_code != ''`）

**部门返回规则**：
- 所有在`t_entry_level_manager`表中有PL/TM或PM数据的四级部门都会出现在响应中
- 即使某个部门没有PL/TM人员（PL/TM的`totalCount`为0），该部门仍会返回，`plTm`字段中`totalCount`、`qualifiedCount`、`certCount`都为0，占比为0.0
- 即使某个部门没有PM人员（PM的`totalCount`为0），该部门仍会返回，`pm`字段中`totalCount`、`qualifiedCount`、`certCount`都为0，占比为0.0
- 如果某个部门既没有PL/TM也没有PM数据，该部门不会出现在响应中（因为该部门在表中不存在任何记录）

## 注意事项

1. **数据去重**：如果同一员工有多条PL、TM或项目经理记录，需要根据业务需求决定统计策略（是否按员工去重，还是按记录统计）
2. **统计分离**：
   - PL和TM合并统计，作为一个整体出现在每个部门的`plTm`字段中
   - PM单独统计，独立出现在每个部门的`pm`字段中
   - PL/TM和PM的统计数据完全分开，互不影响
3. **数据组织方式**：
   - 响应数据按部门维度组织，每个部门包含PL/TM和PM两套统计数据
   - 汇总数据（`summary`）包含研发管理部整体的PL/TM和PM统计
   - 部门列表（`departmentList`）中每个部门都包含该部门的PL/TM和PM统计
4. **空值处理**：
   - **重要**：所有在`t_entry_level_manager`表中有PL/TM或PM数据的四级部门都会出现在`departmentList`中，即使某个部门的人数为0
   - 如果某个四级部门没有PL/TM人员，该部门的`plTm`字段中`totalCount`为0，`qualifiedCount`为0，`certCount`为0，`qualifiedRatio`为0.0，`certRatio`为0.0
   - 如果某个四级部门没有PM人员，该部门的`pm`字段中`totalCount`为0，`qualifiedCount`为0，`certCount`为0，`qualifiedRatio`为0.0，`certRatio`为0.0
   - 如果某个四级部门只有PL/TM数据没有PM数据，该部门会返回，`plTm`字段有统计数据，`pm`字段中所有值都为0或0.0
   - 如果某个四级部门只有PM数据没有PL/TM数据，该部门会返回，`pm`字段有统计数据，`plTm`字段中所有值都为0或0.0
   - 如果某个四级部门既没有PL/TM人员也没有PM人员，且该部门在表中不存在任何记录，该部门不会出现在`departmentList`中
   - 如果研发管理部下没有任何PL/TM人员，`summary.plTm.totalCount`为0，`qualifiedCount`为0，`certCount`为0，`qualifiedRatio`为0.0，`certRatio`为0.0
   - 如果研发管理部下没有任何PM人员，`summary.pm.totalCount`为0，`qualifiedCount`为0，`certCount`为0，`qualifiedRatio`为0.0，`certRatio`为0.0
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

1. **统计粒度**：如果同一员工有多条PL、TM或项目经理记录，是按员工去重统计还是按记录统计？
2. **统计分离**：PL/TM合并统计和PM单独统计的确认，确保两者完全分开统计。
2. **空值处理**：当总人数为0时，占比字段应该返回0还是null？
3. **数据范围**：是否需要过滤某些状态的数据（如`status`字段）？
4. **部门排序**：四级部门列表的排序方式（按部门编码排序还是按部门名称排序）？

