# 接口文档：AI干部岗位概述

## 接口概述

**接口路径**：`GET /cadre-cert-statistics/cadre-position-overview`

**接口描述**：统计云核心网产品线下面所有三级部门以及研发管理部下面所有四级部门的干部总岗位数，以及L2/L3干部岗位总数、L2/L3干部岗位占比，以及L2干部中软件类与非软件类的数量，L3干部下面软件类与非软件类的数量。

**业务逻辑**：
1. 查询方法：
   - **第一步**：从`department_info_hrms`表中查询云核心网产品线（031562）下所有三级部门的信息，以及研发管理部（030681）下所有四级部门的信息
   - **第二步**：对于每个查询到的部门（三级部门或四级部门），直接在数据库中统计该部门的干部数据：
     - 干部总岗位数
     - L2干部软件类数量
     - L2干部非软件类数量
     - L3干部软件类数量
     - L3干部非软件类数量
   - **第三步**：在代码中计算L2/L3岗位总数（L2总数 + L3总数）和占比
   - **第四步**：汇总统计所有三级部门以及研发管理部下所有四级部门的数据
2. 统计范围：
   - 云核心网产品线（部门编码：031562）下面所有三级部门的干部岗位
   - 研发管理部（部门编码：030681）下面所有四级部门的干部岗位
3. 统计维度：
   - 干部总岗位数：统计所有符合条件的干部岗位总数
   - L2/L3干部岗位总数：统计`position_ai_maturity`为'L2'或'L3'的干部岗位总数
   - L2/L3干部岗位占比：L2/L3干部岗位总数 / 干部总岗位数
   - L2干部软件类数量：统计`position_ai_maturity`为'L2'且`cadre_competence_category`为'软件类'的干部岗位数
   - L2干部非软件类数量：统计`position_ai_maturity`为'L2'且`cadre_competence_category`不为'软件类'的干部岗位数
   - L3干部软件类数量：统计`position_ai_maturity`为'L3'且`cadre_competence_category`为'软件类'的干部岗位数
   - L3干部非软件类数量：统计`position_ai_maturity`为'L3'且`cadre_competence_category`不为'软件类'的干部岗位数
4. **按部门维度组织响应数据**：
   - 汇总数据（`summary`）：包含云核心网产品线下面所有三级部门以及研发管理部下面所有四级部门的整体统计数据
   - 部门列表（`departmentList`）：按云核心网产品线下的三级部门和研发管理部下的四级部门组织，每个部门包含该部门的统计数据
   - **重要**：所有在`t_cadre`表中有干部数据的部门都会出现在列表中

**常量配置**：
- 云核心网产品线部门编码：`031562`（二级部门编码）
- 研发管理部部门编码：`030681`（三级部门编码）
- L2/L3干部岗位：`position_ai_maturity IN ('L2', 'L3')`
- 软件类：`cadre_competence_category = '软件类'`
- 非软件类：`cadre_competence_category != '软件类'` 或 `cadre_competence_category IS NULL`

## 请求参数

**说明**：本接口无需任何请求参数，所有查询条件均为固定常量。

### 请求示例

```bash
GET /cadre-cert-statistics/cadre-position-overview
```

## 响应结果

### 成功响应

```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "summary": {
      "totalPositionCount": 500,
      "l2L3PositionCount": 350,
      "l2L3PositionRatio": 0.7000,
      "l2Statistics": {
        "totalCount": 200,
        "softwareCount": 120,
        "nonSoftwareCount": 80
      },
      "l3Statistics": {
        "totalCount": 150,
        "softwareCount": 90,
        "nonSoftwareCount": 60
      }
    },
    "departmentList": [
      {
        "deptCode": "030681",
        "deptName": "研发管理部",
        "deptLevel": "L3",
        "totalPositionCount": 200,
        "l2L3PositionCount": 150,
        "l2L3PositionRatio": 0.7500,
        "l2Statistics": {
          "totalCount": 80,
          "softwareCount": 50,
          "nonSoftwareCount": 30
        },
        "l3Statistics": {
          "totalCount": 70,
          "softwareCount": 40,
          "nonSoftwareCount": 30
        }
      },
      {
        "deptCode": "030681001",
        "deptName": "部门A",
        "deptLevel": "L4",
        "totalPositionCount": 100,
        "l2L3PositionCount": 70,
        "l2L3PositionRatio": 0.7000,
        "l2Statistics": {
          "totalCount": 40,
          "softwareCount": 25,
          "nonSoftwareCount": 15
        },
        "l3Statistics": {
          "totalCount": 30,
          "softwareCount": 18,
          "nonSoftwareCount": 12
        }
      },
      {
        "deptCode": "030682",
        "deptName": "其他三级部门B",
        "deptLevel": "L3",
        "totalPositionCount": 200,
        "l2L3PositionCount": 130,
        "l2L3PositionRatio": 0.6500,
        "l2Statistics": {
          "totalCount": 80,
          "softwareCount": 45,
          "nonSoftwareCount": 35
        },
        "l3Statistics": {
          "totalCount": 50,
          "softwareCount": 32,
          "nonSoftwareCount": 18
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
| data.summary | Object | 汇总数据（所有三级部门以及研发管理部下面所有四级部门的整体统计） |
| data.summary.totalPositionCount | Integer | 干部总岗位数 |
| data.summary.l2L3PositionCount | Integer | L2/L3干部岗位总数 |
| data.summary.l2L3PositionRatio | Double | L2/L3干部岗位占比（l2L3PositionCount/totalPositionCount，保留4位小数） |
| data.summary.l2Statistics | Object | L2干部统计数据 |
| data.summary.l2Statistics.totalCount | Integer | L2干部岗位总数 |
| data.summary.l2Statistics.softwareCount | Integer | L2干部中软件类数量 |
| data.summary.l2Statistics.nonSoftwareCount | Integer | L2干部中非软件类数量 |
| data.summary.l3Statistics | Object | L3干部统计数据 |
| data.summary.l3Statistics.totalCount | Integer | L3干部岗位总数 |
| data.summary.l3Statistics.softwareCount | Integer | L3干部中软件类数量 |
| data.summary.l3Statistics.nonSoftwareCount | Integer | L3干部中非软件类数量 |
| data.departmentList | Array | 部门统计数据列表 |
| data.departmentList[].deptCode | String | 部门编码（三级部门编码或四级部门编码） |
| data.departmentList[].deptName | String | 部门名称 |
| data.departmentList[].deptLevel | String | 部门层级（L3表示三级部门，L4表示四级部门） |
| data.departmentList[].totalPositionCount | Integer | 该部门干部总岗位数 |
| data.departmentList[].l2L3PositionCount | Integer | 该部门L2/L3干部岗位总数 |
| data.departmentList[].l2L3PositionRatio | Double | 该部门L2/L3干部岗位占比 |
| data.departmentList[].l2Statistics | Object | 该部门L2干部统计数据 |
| data.departmentList[].l2Statistics.totalCount | Integer | 该部门L2干部岗位总数 |
| data.departmentList[].l2Statistics.softwareCount | Integer | 该部门L2干部中软件类数量 |
| data.departmentList[].l2Statistics.nonSoftwareCount | Integer | 该部门L2干部中非软件类数量 |
| data.departmentList[].l3Statistics | Object | 该部门L3干部统计数据 |
| data.departmentList[].l3Statistics.totalCount | Integer | 该部门L3干部岗位总数 |
| data.departmentList[].l3Statistics.softwareCount | Integer | 该部门L3干部中软件类数量 |
| data.departmentList[].l3Statistics.nonSoftwareCount | Integer | 该部门L3干部中非软件类数量 |

### 失败响应

```json
{
  "code": 500,
  "message": "错误信息描述",
  "data": null
}
```

## 业务逻辑详细说明

### 1. 查询方法说明

#### 1.1 查询逻辑

**说明**：
- **核心逻辑**：先从部门表中查询各个三级部门以及研发管理部下面四级部门的信息，然后与干部表的数据进行分类统计
- `t_cadre`表中已存在干部的各级部门信息字段：`l2_department_code`、`l3_department_code`、`l4_department_code`、`l5_department_code`
- **重要查询逻辑**：
  1. **第一步**：从`department_info_hrms`表中查询云核心网产品线（031562）下所有三级部门的信息（`dept_level = '3'`），以及研发管理部（030681）下所有四级部门的信息（`dept_level = '4'`）
  2. **第二步**：对于每个部门，使用SQL聚合查询直接统计各项数据。查询条件为`l3_department_code = 部门ID`（对于三级部门）或`l4_department_code = 部门ID`（对于四级部门）。
  3. **第三步**：在代码中计算L2/L3岗位总数和占比，并汇总所有部门的数据。

**实现方式**：
- 步骤1：查询部门信息
  - 查询云核心网产品线（031562）下所有三级部门
  - 查询研发管理部（030681）下所有四级部门
- 步骤2：SQL聚合统计（对每个部门执行）：
  ```sql
  SELECT 
      COUNT(*) as total_count,
      SUM(CASE WHEN position_ai_maturity = 'L2' AND cadre_competence_category = '软件类' THEN 1 ELSE 0 END) as l2_software_count,
      SUM(CASE WHEN position_ai_maturity = 'L2' AND (cadre_competence_category != '软件类' OR cadre_competence_category IS NULL) THEN 1 ELSE 0 END) as l2_non_software_count,
      SUM(CASE WHEN position_ai_maturity = 'L3' AND cadre_competence_category = '软件类' THEN 1 ELSE 0 END) as l3_software_count,
      SUM(CASE WHEN position_ai_maturity = 'L3' AND (cadre_competence_category != '软件类' OR cadre_competence_category IS NULL) THEN 1 ELSE 0 END) as l3_non_software_count
  FROM t_cadre 
  WHERE l3_department_code = #{deptCode} -- 或 l4_department_code = #{deptCode}
  ```
- 步骤3：代码计算：
  - L2/L3岗位总数 = l2_software_count + l2_non_software_count + l3_software_count + l3_non_software_count
  - 占比 = L2/L3岗位总数 / total_count

#### 1.2 统计范围确定

**云核心网产品线下的所有三级部门**：
- 先从`department_info_hrms`表中查询云核心网产品线（031562）下所有三级部门的信息
- 然后从`t_cadre`表中根据这些三级部门ID查询干部数据
- 只统计云核心网产品线（031562）下的三级部门

**研发管理部下的所有四级部门**：
- 先从`department_info_hrms`表中查询研发管理部（030681）下所有四级部门的信息
- 然后从`t_cadre`表中根据这些四级部门ID查询干部数据
- 只统计研发管理部（030681）下的四级部门

### 2. 统计云核心网产品线下各三级部门的干部岗位数据

#### 2.1 统计各三级部门的数据

**查询逻辑**：

**步骤1：从部门表中查询云核心网产品线下所有三级部门信息**
（同上）

**步骤2：对每个三级部门，使用SQL统计干部数据**
```sql
SELECT 
    COUNT(*) as total_count,
    SUM(CASE WHEN position_ai_maturity = 'L2' AND cadre_competence_category = '软件类' THEN 1 ELSE 0 END) as l2_software_count,
    SUM(CASE WHEN position_ai_maturity = 'L2' AND (cadre_competence_category != '软件类' OR cadre_competence_category IS NULL) THEN 1 ELSE 0 END) as l2_non_software_count,
    SUM(CASE WHEN position_ai_maturity = 'L3' AND cadre_competence_category = '软件类' THEN 1 ELSE 0 END) as l3_software_count,
    SUM(CASE WHEN position_ai_maturity = 'L3' AND (cadre_competence_category != '软件类' OR cadre_competence_category IS NULL) THEN 1 ELSE 0 END) as l3_non_software_count
FROM t_cadre 
WHERE l3_department_code = #{deptCode}
```

**步骤3：在代码中计算L2/L3岗位总数和占比**
- L2/L3岗位总数 = l2_software_count + l2_non_software_count + l3_software_count + l3_non_software_count
- 占比 = L2/L3岗位总数 / total_count

- 删除 2.2 和 2.3 节，因为新的查询逻辑一次性统计了所有分类数据
- 删除 3.2 和 3.3 节
- 删除 4.2 和 4.3 节

### 3. 统计研发管理部下各四级部门的干部岗位数据

#### 3.1 统计各四级部门的数据

**查询逻辑**：

**步骤1：从部门表中查询研发管理部下所有四级部门信息**
（同上）

**步骤2：对每个四级部门，使用SQL统计干部数据**
```sql
SELECT 
    COUNT(*) as total_count,
    SUM(CASE WHEN position_ai_maturity = 'L2' AND cadre_competence_category = '软件类' THEN 1 ELSE 0 END) as l2_software_count,
    SUM(CASE WHEN position_ai_maturity = 'L2' AND (cadre_competence_category != '软件类' OR cadre_competence_category IS NULL) THEN 1 ELSE 0 END) as l2_non_software_count,
    SUM(CASE WHEN position_ai_maturity = 'L3' AND cadre_competence_category = '软件类' THEN 1 ELSE 0 END) as l3_software_count,
    SUM(CASE WHEN position_ai_maturity = 'L3' AND (cadre_competence_category != '软件类' OR cadre_competence_category IS NULL) THEN 1 ELSE 0 END) as l3_non_software_count
FROM t_cadre 
WHERE l4_department_code = #{deptCode}
```

**步骤3：在代码中计算L2/L3岗位总数和占比**
（同上）


### 4. 统计汇总数据

#### 4.1 统计汇总数据

**查询逻辑**：
- 汇总所有三级部门和四级部门的统计结果
- 在代码中累加各项数据：
  - 汇总干部总数 = sum(各部门total_count)
  - 汇总L2软件类 = sum(各部门l2_software_count)
  - ...
- 计算汇总的L2/L3岗位总数和占比

**说明**：
- 汇总数据包括两部分：
  1. 云核心网产品线（031562）下所有三级部门的干部岗位（使用`l3_department_code`字段，不包括研发管理部下的四级部门数据，避免重复统计）
  2. 研发管理部（030681）下所有四级部门的干部岗位（使用`l4_department_code`字段）
- 在应用层将两部分数据合并计算汇总结果


### 5. 数据说明

**干部岗位成熟度（position_ai_maturity）**：
- `L2`：L2干部岗位
- `L3`：L3干部岗位
- 其他值：非L2/L3干部岗位（不统计在L2/L3范围内）

**职位类（cadre_competence_category）**：
- `'软件类'`：软件类干部
- 其他值或`NULL`：非软件类干部

**统计范围**：
- **核心逻辑**：先从部门表中查询各个三级部门以及研发管理部下面四级部门的信息，然后对每个部门使用SQL聚合查询统计干部数据
- 云核心网产品线下的所有三级部门：
  1. 先从`department_info_hrms`表中查询云核心网产品线（031562）下所有三级部门的信息
  2. 对每个三级部门，查询`t_cadre`表中`l3_department_code`等于该部门ID的统计数据
- 研发管理部下的四级部门：
  1. 先从`department_info_hrms`表中查询研发管理部（030681）下所有四级部门的信息
  2. 对每个四级部门，查询`t_cadre`表中`l4_department_code`等于该部门ID的统计数据
- **重要**：
  1. **数据来源**：从`t_cadre`表中直接统计
  2. **计算方式**：数据库负责基础计数（总数、各类L2/L3细分数量），Java代码负责汇总计算（L2/L3总数、占比）

**部门返回规则**：
- 所有在`t_cadre`表中有干部数据的云核心网产品线下的三级部门都会出现在`departmentList`中
- 所有在`t_cadre`表中有干部数据的研发管理部下的四级部门都会出现在`departmentList`中
- 即使某个部门没有L2或L3干部（`l2Statistics.totalCount`或`l3Statistics.totalCount`为0），该部门仍会返回，对应的统计字段值为0
- 如果某个部门没有任何干部数据，该部门不会出现在`departmentList`中

## 注意事项

1. **数据去重**：如果同一员工有多条干部记录，需要根据业务需求决定统计策略（是否按员工去重，还是按记录统计）
2. **查询方法**：
   - **核心逻辑**：先从部门表中查询各个三级部门以及研发管理部下面四级部门的信息，然后对每个部门执行SQL聚合查询
   - 步骤1：从`department_info_hrms`表中查询云核心网产品线（031562）下所有三级部门的信息，以及研发管理部（030681）下所有四级部门的信息
   - 步骤2：对每个部门，执行SQL聚合查询，统计总岗位数以及各类L2/L3细分人数
   - 步骤3：在Java代码中计算L2/L3岗位总数及占比
3. **统计范围**：
   - 云核心网产品线下的所有三级部门：先从`department_info_hrms`表中查询云核心网产品线（031562）下所有三级部门的信息，然后根据部门ID列表从`t_cadre`表中查询干部数据进行统计，包括研发管理部（030681）作为三级部门时的数据（但只统计`l4_department_code`为NULL的记录）
   - 研发管理部下的四级部门：先从`department_info_hrms`表中查询研发管理部（030681）下所有四级部门的信息，然后根据部门ID列表从`t_cadre`表中查询干部数据进行统计
   - 汇总数据：累加所有部门的统计数据，并计算汇总的占比
4. **软件类与非软件类区分**：
   - 软件类：`cadre_competence_category = '软件类'`
   - 非软件类：`cadre_competence_category != '软件类'` 或 `cadre_competence_category IS NULL`
5. **占比计算**：
   - L2/L3干部岗位占比 = L2/L3干部岗位总数 / 干部总岗位数
   - 当总岗位数为0时，占比应返回0.0
   - 占比保留4位小数，前端可根据需要格式化显示（如保留2位小数或百分比形式）
6. **性能考虑**：如果数据量较大，建议在相关字段上建立索引：
   - `l3_department_code`
   - `l4_department_code`
   - `position_ai_maturity`
   - `cadre_competence_category`
   - `dept_level`
   - `parent_dept_code`
7. **日志记录**：建议记录查询操作的详细日志，便于问题排查

## 待确认事项

1. **统计粒度**：如果同一员工有多条干部记录，是按员工去重统计还是按记录统计？
2. **t_cadre表和department_info_hrms表字段确认**：
   - **已确认**：`t_cadre`表中已存在干部的各级部门信息字段：`l2_department_code`、`l3_department_code`、`l4_department_code`、`l5_department_code`
   - 直接从`department_info_hrms`表中查询部门信息，然后使用干部表中的部门编码字段进行统计
   - 需要确认`department_info_hrms`表的字段结构，特别是：
     - `dept_code`：部门编码
     - `parent_dept_code`：上级部门编码（用于查询部门层级关系）
     - `dept_level`：部门层级（'3'表示三级部门，'4'表示四级部门等）
     - `dept_name`：部门名称
3. **研发管理部数据处理**：
   - 研发管理部（030681）作为三级部门时，如果其下有四级部门数据，三级部门级别的统计是否只统计`l4_department_code`为空或NULL的记录？
   - 还是需要将研发管理部作为三级部门时，统计其下所有数据（包括四级部门）？
4. **空值处理**：当总岗位数为0时，占比字段应该返回0.0还是null？
5. **数据范围**：是否需要过滤某些状态的数据（如`status`字段）？
6. **部门排序**：部门列表的排序方式（按部门编码排序还是按部门名称排序）？

