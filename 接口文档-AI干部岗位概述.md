# 接口文档：AI干部岗位概述

## 接口概述

**接口路径**：`GET /cadre-cert-statistics/cadre-position-overview`

**接口描述**：统计云核心网产品线下面所有三级部门以及研发管理部下面所有四级部门的干部总岗位数，以及L2/L3干部岗位总数、L2/L3干部岗位占比，以及L2干部中软件类与非软件类的数量，L3干部下面软件类与非软件类的数量。

**业务逻辑**：
1. 查询方法：
   - **第一步**：从`t_cadre`表中查询所有干部信息（包括`account`、`mini_departname_id`、`position_ai_maturity`、`cadre_competence_category`等字段）
   - **第二步**：对于每个干部，根据其`mini_departname_id`关联`department_info_hrms`表，通过`parent_dept_code`字段向上查找上级部门，直到找到三级部门（`dept_level = '3'`）
   - **第三步**：对于研发管理部（030681）下的干部，还需要查找其四级部门（`dept_level = '4'`）
   - **第四步**：根据每个干部的三级部门ID和四级部门ID（如果有）对人员进行分类
   - **第五步**：按照分类结果统计各类干部人数
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

#### 1.1 新的查询逻辑

**说明**：
- `t_cadre`表中没有各级部门的编码字段，只有`mini_departname_id`字段（最小部门编码）
- 需要通过`mini_departname_id`关联`department_info_hrms`表来获取部门层级信息
- **重要查询逻辑**：
  1. **第一步**：从`t_cadre`表中查询所有干部信息（包括`account`、`mini_departname_id`、`position_ai_maturity`、`cadre_competence_category`等字段）
  2. **第二步**：对于每个干部，根据其`mini_departname_id`关联`department_info_hrms`表，通过`parent_dept_code`字段向上查找上级部门，直到找到三级部门（`dept_level = '3'`）
  3. **第三步**：对于研发管理部（030681）下的干部，还需要查找其四级部门（`dept_level = '4'`）
  4. **第四步**：根据每个干部的三级部门ID和四级部门ID（如果有）对人员进行分类
  5. **第五步**：按照分类结果统计各类干部人数

**实现方式**：
- 查询所有干部数据：`SELECT * FROM t_cadre WHERE account IS NOT NULL AND account != ''`
- 对于每个干部的`mini_departname_id`，通过`department_info_hrms`表的`parent_dept_code`字段递归向上查找，直到找到`dept_level = '3'`的部门
- 对于研发管理部（030681）下的干部，还需要查找`dept_level = '4'`的部门
- 在内存中根据三级部门ID和四级部门ID对干部进行分类
- 统计每个部门（三级部门或四级部门）的干部人数

#### 1.2 统计范围确定

**云核心网产品线下的所有三级部门**：
- 通过向上查找部门层级，找到所有干部所属的三级部门
- 只统计云核心网产品线（031562）下的三级部门
- 如果干部的三级部门不在云核心网产品线下，则排除

**研发管理部下的所有四级部门**：
- 对于研发管理部（030681）下的干部，需要查找其四级部门
- 统计研发管理部（030681）下所有四级部门的干部数据
- 如果干部的四级部门不在研发管理部下，则排除

### 2. 统计云核心网产品线下各三级部门的干部岗位数据

#### 2.1 统计各三级部门的总体数据

**查询逻辑**：
```sql
-- 步骤1：查询所有干部信息
SELECT 
    c.account,
    c.mini_departname_id,
    c.position_ai_maturity,
    c.cadre_competence_category
FROM t_cadre c
WHERE c.account IS NOT NULL 
  AND c.account != ''
```

**步骤2：对于每个干部，向上查找三级部门**
```sql
-- 通过递归向上查找，找到每个干部所属的三级部门
-- 使用LEFT JOIN多次关联，向上查找直到找到dept_level = '3'的部门
SELECT 
    c.account,
    c.mini_departname_id,
    c.position_ai_maturity,
    c.cadre_competence_category,
    CASE 
        WHEN dept.dept_level = '3' THEN dept.dept_code
        WHEN d1.dept_level = '3' THEN d1.dept_code
        WHEN d2.dept_level = '3' THEN d2.dept_code
        WHEN d3.dept_level = '3' THEN d3.dept_code
        ELSE NULL
    END AS l3_dept_code,
    CASE 
        WHEN dept.dept_level = '3' THEN dept.dept_name
        WHEN d1.dept_level = '3' THEN d1.dept_name
        WHEN d2.dept_level = '3' THEN d2.dept_name
        WHEN d3.dept_level = '3' THEN d3.dept_name
        ELSE NULL
    END AS l3_dept_name
FROM t_cadre c
INNER JOIN department_info_hrms dept ON (
    c.mini_departname_id = dept.dept_code 
    AND DATE(dept.update_time) = (SELECT MAX(DATE(update_time)) FROM department_info_hrms)
)
LEFT JOIN department_info_hrms d1 ON (
    dept.parent_dept_code = d1.dept_code 
    AND DATE(d1.update_time) = (SELECT MAX(DATE(update_time)) FROM department_info_hrms)
)
LEFT JOIN department_info_hrms d2 ON (
    d1.parent_dept_code = d2.dept_code 
    AND DATE(d2.update_time) = (SELECT MAX(DATE(update_time)) FROM department_info_hrms)
)
LEFT JOIN department_info_hrms d3 ON (
    d2.parent_dept_code = d3.dept_code 
    AND DATE(d3.update_time) = (SELECT MAX(DATE(update_time)) FROM department_info_hrms)
)
WHERE c.account IS NOT NULL 
  AND c.account != ''
  AND (
    dept.dept_level = '3' 
    OR d1.dept_level = '3' 
    OR d2.dept_level = '3' 
    OR d3.dept_level = '3'
  )
```

**步骤3：在应用层对干部进行分类统计**
- 过滤出云核心网产品线（031562）下的三级部门
- 按三级部门分组统计：
  - 干部总岗位数
  - L2/L3干部岗位总数
  - L2/L3干部岗位占比
  - L2干部软件类数量
  - L2干部非软件类数量
  - L3干部软件类数量
  - L3干部非软件类数量

**说明**：
- 先查询所有干部信息，然后通过`department_info_hrms`表的`parent_dept_code`字段向上查找，直到找到三级部门
- 在应用层根据三级部门ID对干部进行分类统计
- 只统计云核心网产品线（031562）下的三级部门

#### 2.2 统计各三级部门的L2干部数据（按软件类/非软件类）

**查询逻辑**：
- 使用与2.1相同的查询方法，先查询所有干部信息并向上查找到三级部门
- 在应用层过滤出`position_ai_maturity = 'L2'`的干部
- 按三级部门分组统计：
  - L2干部总数
  - L2干部软件类数量（`cadre_competence_category = '软件类'`）
  - L2干部非软件类数量（`cadre_competence_category != '软件类'` 或 `cadre_competence_category IS NULL`）

#### 2.3 统计各三级部门的L3干部数据（按软件类/非软件类）

**查询逻辑**：
- 使用与2.1相同的查询方法，先查询所有干部信息并向上查找到三级部门
- 在应用层过滤出`position_ai_maturity = 'L3'`的干部
- 按三级部门分组统计：
  - L3干部总数
  - L3干部软件类数量（`cadre_competence_category = '软件类'`）
  - L3干部非软件类数量（`cadre_competence_category != '软件类'` 或 `cadre_competence_category IS NULL`）

### 3. 统计研发管理部下各四级部门的干部岗位数据

#### 3.1 统计各四级部门的总体数据

**查询逻辑**：
```sql
-- 步骤1：查询所有干部信息，并向上查找到三级部门和四级部门
SELECT 
    c.account,
    c.mini_departname_id,
    c.position_ai_maturity,
    c.cadre_competence_category,
    CASE 
        WHEN dept.dept_level = '3' THEN dept.dept_code
        WHEN d1.dept_level = '3' THEN d1.dept_code
        WHEN d2.dept_level = '3' THEN d2.dept_code
        WHEN d3.dept_level = '3' THEN d3.dept_code
        ELSE NULL
    END AS l3_dept_code,
    CASE 
        WHEN dept.dept_level = '4' THEN dept.dept_code
        WHEN d1.dept_level = '4' THEN d1.dept_code
        WHEN d2.dept_level = '4' THEN d2.dept_code
        ELSE NULL
    END AS l4_dept_code,
    CASE 
        WHEN dept.dept_level = '4' THEN dept.dept_name
        WHEN d1.dept_level = '4' THEN d1.dept_name
        WHEN d2.dept_level = '4' THEN d2.dept_name
        ELSE NULL
    END AS l4_dept_name
FROM t_cadre c
INNER JOIN department_info_hrms dept ON (
    c.mini_departname_id = dept.dept_code 
    AND DATE(dept.update_time) = (SELECT MAX(DATE(update_time)) FROM department_info_hrms)
)
LEFT JOIN department_info_hrms d1 ON (
    dept.parent_dept_code = d1.dept_code 
    AND DATE(d1.update_time) = (SELECT MAX(DATE(update_time)) FROM department_info_hrms)
)
LEFT JOIN department_info_hrms d2 ON (
    d1.parent_dept_code = d2.dept_code 
    AND DATE(d2.update_time) = (SELECT MAX(DATE(update_time)) FROM department_info_hrms)
)
LEFT JOIN department_info_hrms d3 ON (
    d2.parent_dept_code = d3.dept_code 
    AND DATE(d3.update_time) = (SELECT MAX(DATE(update_time)) FROM department_info_hrms)
)
WHERE c.account IS NOT NULL 
  AND c.account != ''
  AND (
    CASE 
        WHEN dept.dept_level = '3' THEN dept.dept_code
        WHEN d1.dept_level = '3' THEN d1.dept_code
        WHEN d2.dept_level = '3' THEN d2.dept_code
        WHEN d3.dept_level = '3' THEN d3.dept_code
        ELSE NULL
    END = '030681'
  )
  AND (
    CASE 
        WHEN dept.dept_level = '4' THEN dept.dept_code
        WHEN d1.dept_level = '4' THEN d1.dept_code
        WHEN d2.dept_level = '4' THEN d2.dept_code
        ELSE NULL
    END IS NOT NULL
  )
```

**步骤2：在应用层对干部进行分类统计**
- 过滤出研发管理部（030681）下的四级部门
- 按四级部门分组统计：
  - 干部总岗位数
  - L2/L3干部岗位总数
  - L2/L3干部岗位占比
  - L2干部软件类数量
  - L2干部非软件类数量
  - L3干部软件类数量
  - L3干部非软件类数量

**说明**：
- 先查询所有干部信息，然后通过`department_info_hrms`表的`parent_dept_code`字段向上查找，找到三级部门（研发管理部030681）和四级部门
- 在应用层根据四级部门ID对干部进行分类统计
- 只统计研发管理部（030681）下的四级部门

#### 3.2 统计各四级部门的L2干部数据（按软件类/非软件类）

**查询逻辑**：
- 使用与3.1相同的查询方法，先查询所有干部信息并向上查找到三级部门和四级部门
- 在应用层过滤出`position_ai_maturity = 'L2'`的干部
- 按四级部门分组统计：
  - L2干部总数
  - L2干部软件类数量（`cadre_competence_category = '软件类'`）
  - L2干部非软件类数量（`cadre_competence_category != '软件类'` 或 `cadre_competence_category IS NULL`）

#### 3.3 统计各四级部门的L3干部数据（按软件类/非软件类）

**查询逻辑**：
- 使用与3.1相同的查询方法，先查询所有干部信息并向上查找到三级部门和四级部门
- 在应用层过滤出`position_ai_maturity = 'L3'`的干部
- 按四级部门分组统计：
  - L3干部总数
  - L3干部软件类数量（`cadre_competence_category = '软件类'`）
  - L3干部非软件类数量（`cadre_competence_category != '软件类'` 或 `cadre_competence_category IS NULL`）

### 4. 统计汇总数据

#### 4.1 统计云核心网产品线下所有三级部门以及研发管理部下面所有四级部门的整体数据

**查询逻辑**：
- 使用与2.1和3.1相同的查询方法，先查询所有干部信息并向上查找到三级部门和四级部门
- 在应用层合并统计：
  1. 云核心网产品线（031562）下所有三级部门的干部岗位（不包括研发管理部下的四级部门数据，避免重复统计）
  2. 研发管理部（030681）下所有四级部门及其下所有五级部门的干部岗位
- 汇总统计：
  - 干部总岗位数 = 所有三级部门干部总数 + 研发管理部四级部门干部总数
  - L2/L3干部岗位总数 = 所有三级部门L2/L3干部总数 + 研发管理部四级部门L2/L3干部总数
  - L2/L3干部岗位占比 = L2/L3干部岗位总数 / 干部总岗位数

**说明**：
- 汇总数据包括两部分：
  1. 云核心网产品线（031562）下所有三级部门的干部岗位（不包括研发管理部下的四级部门数据，避免重复统计）
  2. 研发管理部（030681）下所有四级部门及其下所有五级部门的干部岗位
- 在应用层将两部分数据合并计算汇总结果

#### 4.2 统计汇总的L2干部数据（按软件类/非软件类）

**查询逻辑**：
- 使用与2.2和3.2相同的查询方法，先查询所有干部信息并向上查找到三级部门和四级部门
- 在应用层过滤出`position_ai_maturity = 'L2'`的干部
- 合并统计：
  1. 云核心网产品线（031562）下所有三级部门的L2干部（不包括研发管理部下的四级部门）
  2. 研发管理部（030681）下所有四级部门的L2干部
- 汇总统计：
  - L2干部总数 = 所有三级部门L2干部总数 + 研发管理部四级部门L2干部总数
  - L2干部软件类数量 = 所有三级部门L2软件类数量 + 研发管理部四级部门L2软件类数量
  - L2干部非软件类数量 = 所有三级部门L2非软件类数量 + 研发管理部四级部门L2非软件类数量

#### 4.3 统计汇总的L3干部数据（按软件类/非软件类）

**查询逻辑**：
- 使用与2.3和3.3相同的查询方法，先查询所有干部信息并向上查找到三级部门和四级部门
- 在应用层过滤出`position_ai_maturity = 'L3'`的干部
- 合并统计：
  1. 云核心网产品线（031562）下所有三级部门的L3干部（不包括研发管理部下的四级部门）
  2. 研发管理部（030681）下所有四级部门的L3干部
- 汇总统计：
  - L3干部总数 = 所有三级部门L3干部总数 + 研发管理部四级部门L3干部总数
  - L3干部软件类数量 = 所有三级部门L3软件类数量 + 研发管理部四级部门L3软件类数量
  - L3干部非软件类数量 = 所有三级部门L3非软件类数量 + 研发管理部四级部门L3非软件类数量

### 5. 数据说明

**干部岗位成熟度（position_ai_maturity）**：
- `L2`：L2干部岗位
- `L3`：L3干部岗位
- 其他值：非L2/L3干部岗位（不统计在L2/L3范围内）

**职位类（cadre_competence_category）**：
- `'软件类'`：软件类干部
- 其他值或`NULL`：非软件类干部

**统计范围**：
- 云核心网产品线下的所有三级部门：先查询所有干部信息，然后通过`mini_departname_id`关联`department_info_hrms`表，向上查找每个干部所属的三级部门，只统计云核心网产品线（031562）下的三级部门
- 研发管理部下的四级部门：先查询所有干部信息，然后通过`mini_departname_id`关联`department_info_hrms`表，向上查找每个干部所属的三级部门（研发管理部030681）和四级部门，只统计研发管理部（030681）下的四级部门
- **重要**：
  1. `t_cadre`表中没有各级部门的编码字段，只有`mini_departname_id`字段（最小部门编码）
  2. 需要通过`mini_departname_id`关联`department_info_hrms`表，通过`parent_dept_code`字段向上查找上级部门，直到找到三级部门（`dept_level = '3'`）
  3. **查询三级部门时**：对于每个干部，从其`mini_departname_id`开始，通过`parent_dept_code`向上查找，直到找到`dept_level = '3'`的部门，然后判断该三级部门是否属于云核心网产品线（031562）
  4. **查询四级部门时**：对于研发管理部（030681）下的干部，在找到三级部门后，继续向上查找或向下查找，找到`dept_level = '4'`的部门
  5. **注意**：在应用层根据三级部门ID和四级部门ID对干部进行分类统计
  6. 研发管理部（030681）作为三级部门时，如果其下有四级部门数据，则三级部门级别的数据只统计四级部门编码为NULL的记录，避免与四级部门数据重复

**部门返回规则**：
- 所有在`t_cadre`表中有干部数据的云核心网产品线下的三级部门都会出现在`departmentList`中
- 所有在`t_cadre`表中有干部数据的研发管理部下的四级部门都会出现在`departmentList`中
- 即使某个部门没有L2或L3干部（`l2Statistics.totalCount`或`l3Statistics.totalCount`为0），该部门仍会返回，对应的统计字段值为0
- 如果某个部门没有任何干部数据，该部门不会出现在`departmentList`中

## 注意事项

1. **数据去重**：如果同一员工有多条干部记录，需要根据业务需求决定统计策略（是否按员工去重，还是按记录统计）
2. **查询方法**：
   - 先查询所有干部信息，然后通过`mini_departname_id`关联`department_info_hrms`表，向上查找每个干部所属的三级部门
   - 对于研发管理部（030681）下的干部，还需要查找其四级部门
   - 在应用层根据三级部门ID和四级部门ID对干部进行分类统计
3. **统计范围**：
   - 云核心网产品线下的所有三级部门：通过向上查找部门层级，找到所有干部所属的三级部门，只统计云核心网产品线（031562）下的三级部门，包括研发管理部（030681）作为三级部门时的数据（但只统计四级部门编码为NULL的记录）
   - 研发管理部下的四级部门：通过向上查找部门层级，找到研发管理部（030681）下的四级部门，只统计`l3_dept_code = '030681'`且`l4_dept_code`不为空的记录
   - 汇总数据：合并云核心网产品线（031562）下所有三级部门（不包括研发管理部下的四级部门）和研发管理部下所有四级部门的数据
4. **软件类与非软件类区分**：
   - 软件类：`cadre_competence_category = '软件类'`
   - 非软件类：`cadre_competence_category != '软件类'` 或 `cadre_competence_category IS NULL`
5. **占比计算**：
   - L2/L3干部岗位占比 = L2/L3干部岗位总数 / 干部总岗位数
   - 当总岗位数为0时，占比应返回0.0
   - 占比保留4位小数，前端可根据需要格式化显示（如保留2位小数或百分比形式）
6. **性能考虑**：如果数据量较大，建议在相关字段上建立索引：
   - `mini_departname_id`
   - `parent_dept_code`
   - `dept_level`
   - `position_ai_maturity`
   - `cadre_competence_category`
7. **日志记录**：建议记录查询操作的详细日志，便于问题排查

## 待确认事项

1. **统计粒度**：如果同一员工有多条干部记录，是按员工去重统计还是按记录统计？
2. **t_cadre表和department_info_hrms表字段确认**：
   - **已确认**：`t_cadre`表中没有各级部门的编码字段，只有`mini_departname_id`字段（最小部门编码）
   - 需要通过`mini_departname_id`关联`department_info_hrms`表，通过`parent_dept_code`字段向上查找上级部门
   - 需要确认`department_info_hrms`表的字段结构，特别是：
     - `dept_code`：部门编码（对应`t_cadre.mini_departname_id`）
     - `parent_dept_code`：上级部门编码（用于向上查找）
     - `dept_level`：部门层级（'3'表示三级部门，'4'表示四级部门等）
     - `dept_name`：部门名称
     - `update_time`：更新时间（用于获取最新数据）
3. **研发管理部数据处理**：
   - 研发管理部（030681）作为三级部门时，如果其下有四级部门数据，三级部门级别的统计是否只统计`l4_department_code`为空或NULL的记录？
   - 还是需要将研发管理部作为三级部门时，统计其下所有数据（包括四级部门）？
4. **空值处理**：当总岗位数为0时，占比字段应该返回0.0还是null？
5. **数据范围**：是否需要过滤某些状态的数据（如`status`字段）？
6. **部门排序**：部门列表的排序方式（按部门编码排序还是按部门名称排序）？

