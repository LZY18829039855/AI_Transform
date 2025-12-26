# 接口文档：AI干部岗位概述

## 接口概述

**接口路径**：`GET /cadre-cert-statistics/cadre-position-overview`

**接口描述**：统计云核心网产品线下面所有三级部门以及研发管理部下面所有四级部门的干部总岗位数，以及L2/L3干部岗位总数、L2/L3干部岗位占比，以及L2干部中软件类与非软件类的数量，L3干部下面软件类与非软件类的数量。

**业务逻辑**：
1. 统计范围：
   - 云核心网产品线（部门编码：031562）下面所有三级部门的干部岗位
   - 研发管理部（部门编码：030681）下面所有四级部门的干部岗位
2. 统计维度：
   - 干部总岗位数：统计所有符合条件的干部岗位总数
   - L2/L3干部岗位总数：统计`position_ai_maturity`为'L2'或'L3'的干部岗位总数
   - L2/L3干部岗位占比：L2/L3干部岗位总数 / 干部总岗位数
   - L2干部软件类数量：统计`position_ai_maturity`为'L2'且`cadre_competence_category`为'软件类'的干部岗位数
   - L2干部非软件类数量：统计`position_ai_maturity`为'L2'且`cadre_competence_category`不为'软件类'的干部岗位数
   - L3干部软件类数量：统计`position_ai_maturity`为'L3'且`cadre_competence_category`为'软件类'的干部岗位数
   - L3干部非软件类数量：统计`position_ai_maturity`为'L3'且`cadre_competence_category`不为'软件类'的干部岗位数
3. **按部门维度组织响应数据**：
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

### 1. 统计范围确定

#### 1.1 云核心网产品线下的所有三级部门

**说明**：
- `t_cadre`表中没有各级部门的编码字段，只有`mini_departname_id`字段（最小部门编码）
- 需要通过`mini_departname_id`关联`department_info_hrms`表来获取部门层级信息
- **重要查询逻辑**：
  1. 从`department_info_hrms`表中查询云核心网产品线（031562）下的所有三级部门编码列表
  2. 对于每个三级部门，查询该三级部门及其下所有四级、五级部门的`dept_code`（即`mini_departname_id`）列表
  3. **关键点**：查询时需要包含该三级部门本身的`dept_code`、该三级部门下所有四级部门的`dept_code`、该三级部门下所有五级部门的`dept_code`
  4. **原因**：因为有些干部的最小部门（`mini_departname_id`）可能为三级部门、四级部门或五级部门，所以需要将这些部门的`dept_code`都加入到查询列表中
  5. **注意**：六级部门没有干部数据，所以不需要查询六级部门
  6. 在`t_cadre`表中查询`mini_departname_id`在这些列表中的干部数据，按三级部门分组统计

**实现方式**：
- 使用`DepartmentInfoMapper`的方法查询云核心网产品线（031562）下的所有三级部门
- 对于每个三级部门，查询该三级部门本身的`dept_code` + 该三级部门下所有四级部门的`dept_code` + 该三级部门下所有五级部门的`dept_code`，组成查询列表
- 在`t_cadre`表中使用`WHERE mini_departname_id IN (...)`查询这些部门的干部数据

#### 1.2 研发管理部下的所有四级部门

**说明**：
- `t_cadre`表中没有各级部门的编码字段，只有`mini_departname_id`字段（最小部门编码）
- 需要通过`mini_departname_id`关联`department_info_hrms`表来获取部门层级信息
- **重要**：统计四级部门的干部数量时，四级下面的五级部门的干部也算该四级部门的
- **重要查询逻辑**：
  1. 从`department_info_hrms`表中查询研发管理部（030681）下的所有四级部门编码列表
  2. 对于每个四级部门，查询该四级部门及其下所有五级部门的`dept_code`（即`mini_departname_id`）列表
  3. **关键点**：查询时需要包含该四级部门本身的`dept_code`、该四级部门下所有五级部门的`dept_code`
  4. **原因**：因为有些干部的最小部门（`mini_departname_id`）可能为四级部门或五级部门，所以需要将这些部门的`dept_code`都加入到查询列表中
  5. 在`t_cadre`表中查询`mini_departname_id`在这些列表中的干部数据，按四级部门分组统计

**实现方式**：
- 使用`DepartmentInfoMapper`的方法查询研发管理部（030681）下的所有四级部门
- 对于每个四级部门，查询该四级部门本身的`dept_code` + 该四级部门下所有五级部门的`dept_code`，组成查询列表
- 在`t_cadre`表中使用`WHERE mini_departname_id IN (...)`查询这些部门的干部数据

### 2. 统计云核心网产品线下各三级部门的干部岗位数据

#### 2.1 统计各三级部门的总体数据

**SQL逻辑**：
```sql
-- t_cadre表只有mini_departname_id字段，需要通过department_info_hrms表获取部门层级
-- 步骤1：查询云核心网产品线（031562）下的所有三级部门及其下所有子部门的mini_departname_id列表
-- （这一步在应用层完成，通过DepartmentInfoMapper查询）

-- 步骤2：对于每个三级部门，统计该部门及其下所有子部门的干部数据
SELECT 
    -- 通过关联department_info_hrms表获取三级部门编码和名称
    MAX(dept.l3_dept_code) AS deptCode,
    MAX(dept.l3_dept_name) AS deptName,
    COUNT(*) AS totalPositionCount,
    SUM(CASE WHEN c.position_ai_maturity IN ('L2', 'L3') THEN 1 ELSE 0 END) AS l2L3PositionCount,
    CASE 
        WHEN COUNT(*) = 0 THEN 0.0
        ELSE ROUND(SUM(CASE WHEN c.position_ai_maturity IN ('L2', 'L3') THEN 1 ELSE 0 END) * 1.0 / COUNT(*), 4)
    END AS l2L3PositionRatio
FROM t_cadre c
INNER JOIN department_info_hrms dept ON (c.mini_departname_id = dept.dept_code AND DATE(dept.update_time) = (SELECT MAX(DATE(update_time)) FROM department_info_hrms))
WHERE c.mini_departname_id IN (
    -- 该三级部门本身的dept_code + 该三级部门下所有四级部门的dept_code + 该三级部门下所有五级部门的dept_code
    -- （在应用层通过DepartmentInfoMapper查询得到）
    -- 注意：六级部门没有干部数据，所以不需要查询六级部门
)
GROUP BY dept.l3_dept_code
ORDER BY dept.l3_dept_code
```

**说明**：
- 需要先通过`DepartmentInfoMapper`查询云核心网产品线（031562）下的所有三级部门
- 对于每个三级部门，查询该三级部门本身的`dept_code` + 该三级部门下所有四级部门的`dept_code` + 该三级部门下所有五级部门的`dept_code`，组成查询列表
- **重要**：因为有些干部的最小部门（`mini_departname_id`）可能为三级部门、四级部门或五级部门，所以需要将这些部门的`dept_code`都加入到查询列表中
- 在`t_cadre`表中使用`WHERE mini_departname_id IN (...)`查询这些部门的干部数据
- 通过关联`department_info_hrms`表获取三级部门的编码和名称
- **注意**：六级部门没有干部数据，所以不需要查询六级部门

#### 2.2 统计各三级部门的L2干部数据（按软件类/非软件类）

**SQL逻辑**：
```sql
-- t_cadre表只有mini_departname_id字段，需要通过department_info_hrms表获取部门层级
SELECT 
    MAX(dept.l3_dept_code) AS deptCode,
    COUNT(*) AS l2TotalCount,
    SUM(CASE WHEN c.cadre_competence_category = '软件类' THEN 1 ELSE 0 END) AS l2SoftwareCount,
    SUM(CASE WHEN c.cadre_competence_category != '软件类' OR c.cadre_competence_category IS NULL THEN 1 ELSE 0 END) AS l2NonSoftwareCount
FROM t_cadre c
INNER JOIN department_info_hrms dept ON (c.mini_departname_id = dept.dept_code AND DATE(dept.update_time) = (SELECT MAX(DATE(update_time)) FROM department_info_hrms))
WHERE c.mini_departname_id IN (
    -- 该三级部门本身的dept_code + 该三级部门下所有四级部门的dept_code + 该三级部门下所有五级部门的dept_code
    -- （在应用层通过DepartmentInfoMapper查询得到）
    -- 注意：六级部门没有干部数据，所以不需要查询六级部门
)
  AND c.position_ai_maturity = 'L2'
GROUP BY dept.l3_dept_code
ORDER BY dept.l3_dept_code
```

#### 2.3 统计各三级部门的L3干部数据（按软件类/非软件类）

**SQL逻辑**：
```sql
-- t_cadre表只有mini_departname_id字段，需要通过department_info_hrms表获取部门层级
SELECT 
    MAX(dept.l3_dept_code) AS deptCode,
    COUNT(*) AS l3TotalCount,
    SUM(CASE WHEN c.cadre_competence_category = '软件类' THEN 1 ELSE 0 END) AS l3SoftwareCount,
    SUM(CASE WHEN c.cadre_competence_category != '软件类' OR c.cadre_competence_category IS NULL THEN 1 ELSE 0 END) AS l3NonSoftwareCount
FROM t_cadre c
INNER JOIN department_info_hrms dept ON (c.mini_departname_id = dept.dept_code AND DATE(dept.update_time) = (SELECT MAX(DATE(update_time)) FROM department_info_hrms))
WHERE c.mini_departname_id IN (
    -- 该三级部门本身的dept_code + 该三级部门下所有四级部门的dept_code + 该三级部门下所有五级部门的dept_code
    -- （在应用层通过DepartmentInfoMapper查询得到）
    -- 注意：六级部门没有干部数据，所以不需要查询六级部门
)
  AND c.position_ai_maturity = 'L3'
GROUP BY dept.l3_dept_code
ORDER BY dept.l3_dept_code
```

### 3. 统计研发管理部下各四级部门的干部岗位数据

#### 3.1 统计各四级部门的总体数据

**SQL逻辑**：
```sql
-- t_cadre表只有mini_departname_id字段，需要通过department_info_hrms表获取部门层级
-- 重要：统计四级部门的干部数量时，四级下面的五级部门的干部也算该四级部门的
SELECT 
    MAX(dept.l4_dept_code) AS deptCode,
    MAX(dept.l4_dept_name) AS deptName,
    COUNT(*) AS totalPositionCount,
    SUM(CASE WHEN c.position_ai_maturity IN ('L2', 'L3') THEN 1 ELSE 0 END) AS l2L3PositionCount,
    CASE 
        WHEN COUNT(*) = 0 THEN 0.0
        ELSE ROUND(SUM(CASE WHEN c.position_ai_maturity IN ('L2', 'L3') THEN 1 ELSE 0 END) * 1.0 / COUNT(*), 4)
    END AS l2L3PositionRatio
FROM t_cadre c
INNER JOIN department_info_hrms dept ON (c.mini_departname_id = dept.dept_code AND DATE(dept.update_time) = (SELECT MAX(DATE(update_time)) FROM department_info_hrms))
WHERE c.mini_departname_id IN (
    -- 该四级部门本身的dept_code + 该四级部门下所有五级部门的dept_code
    -- （在应用层通过DepartmentInfoMapper查询得到）
    -- 因为有些干部的最小部门可能为四级部门或五级部门，所以需要将这些部门的dept_code都加入到查询列表中
)
GROUP BY dept.l4_dept_code
ORDER BY dept.l4_dept_code
```

**说明**：
- 需要先通过`DepartmentInfoMapper`查询研发管理部（030681）下的所有四级部门
- 对于每个四级部门，查询该四级部门本身的`dept_code` + 该四级部门下所有五级部门的`dept_code`，组成查询列表（即`mini_departname_id`列表）
- **重要**：因为有些干部的最小部门（`mini_departname_id`）可能为四级部门或五级部门，所以需要将这些部门的`dept_code`都加入到查询列表中
- 在`t_cadre`表中使用`WHERE mini_departname_id IN (...)`查询这些部门的干部数据
- 通过关联`department_info_hrms`表获取四级部门的编码和名称
- **重要**：统计时包含该四级部门及其下所有五级部门的干部数据

#### 3.2 统计各四级部门的L2干部数据（按软件类/非软件类）

**SQL逻辑**：
```sql
-- t_cadre表只有mini_departname_id字段，需要通过department_info_hrms表获取部门层级
-- 重要：统计四级部门的干部数量时，四级下面的五级部门的干部也算该四级部门的
SELECT 
    MAX(dept.l4_dept_code) AS deptCode,
    COUNT(*) AS l2TotalCount,
    SUM(CASE WHEN c.cadre_competence_category = '软件类' THEN 1 ELSE 0 END) AS l2SoftwareCount,
    SUM(CASE WHEN c.cadre_competence_category != '软件类' OR c.cadre_competence_category IS NULL THEN 1 ELSE 0 END) AS l2NonSoftwareCount
FROM t_cadre c
INNER JOIN department_info_hrms dept ON (c.mini_departname_id = dept.dept_code AND DATE(dept.update_time) = (SELECT MAX(DATE(update_time)) FROM department_info_hrms))
WHERE c.mini_departname_id IN (
    -- 该四级部门本身的dept_code + 该四级部门下所有五级部门的dept_code
    -- （在应用层通过DepartmentInfoMapper查询得到）
    -- 因为有些干部的最小部门可能为四级部门或五级部门，所以需要将这些部门的dept_code都加入到查询列表中
)
  AND c.position_ai_maturity = 'L2'
GROUP BY dept.l4_dept_code
ORDER BY dept.l4_dept_code
```

#### 3.3 统计各四级部门的L3干部数据（按软件类/非软件类）

**SQL逻辑**：
```sql
-- t_cadre表只有mini_departname_id字段，需要通过department_info_hrms表获取部门层级
-- 重要：统计四级部门的干部数量时，四级下面的五级部门的干部也算该四级部门的
SELECT 
    MAX(dept.l4_dept_code) AS deptCode,
    COUNT(*) AS l3TotalCount,
    SUM(CASE WHEN c.cadre_competence_category = '软件类' THEN 1 ELSE 0 END) AS l3SoftwareCount,
    SUM(CASE WHEN c.cadre_competence_category != '软件类' OR c.cadre_competence_category IS NULL THEN 1 ELSE 0 END) AS l3NonSoftwareCount
FROM t_cadre c
INNER JOIN department_info_hrms dept ON (c.mini_departname_id = dept.dept_code AND DATE(dept.update_time) = (SELECT MAX(DATE(update_time)) FROM department_info_hrms))
WHERE c.mini_departname_id IN (
    -- 该四级部门本身的dept_code + 该四级部门下所有五级部门的dept_code
    -- （在应用层通过DepartmentInfoMapper查询得到）
    -- 因为有些干部的最小部门可能为四级部门或五级部门，所以需要将这些部门的dept_code都加入到查询列表中
)
  AND c.position_ai_maturity = 'L3'
GROUP BY dept.l4_dept_code
ORDER BY dept.l4_dept_code
```

### 4. 统计汇总数据

#### 4.1 统计云核心网产品线下所有三级部门以及研发管理部下面所有四级部门的整体数据

**SQL逻辑**：
```sql
-- t_cadre表只有mini_departname_id字段，需要通过department_info_hrms表获取部门层级
-- 步骤1：查询云核心网产品线（031562）下所有三级部门及其下所有子部门的mini_departname_id列表
-- （这一步在应用层完成，通过DepartmentInfoMapper查询）

-- 步骤2：统计云核心网产品线（031562）下所有三级部门的干部岗位（不包括研发管理部下的四级部门，避免重复统计）
SELECT 
    COUNT(*) AS totalPositionCount,
    SUM(CASE WHEN c.position_ai_maturity IN ('L2', 'L3') THEN 1 ELSE 0 END) AS l2L3PositionCount,
    CASE 
        WHEN COUNT(*) = 0 THEN 0.0
        ELSE ROUND(SUM(CASE WHEN c.position_ai_maturity IN ('L2', 'L3') THEN 1 ELSE 0 END) * 1.0 / COUNT(*), 4)
    END AS l2L3PositionRatio
FROM t_cadre c
INNER JOIN department_info_hrms dept ON (c.mini_departname_id = dept.dept_code AND DATE(dept.update_time) = (SELECT MAX(DATE(update_time)) FROM department_info_hrms))
WHERE c.mini_departname_id IN (
    -- 云核心网产品线（031562）下所有三级部门本身的dept_code + 这些三级部门下所有四级部门的dept_code + 这些三级部门下所有五级部门的dept_code
    -- 但排除研发管理部（030681）下的四级部门及其子部门
    -- （在应用层通过DepartmentInfoMapper查询得到）
    -- 注意：六级部门没有干部数据，所以不需要查询六级部门
)
  AND (
    -- 所有三级部门的数据（不包括研发管理部下的四级部门数据）
    (dept.l3_dept_code != '030681')
    OR
    -- 研发管理部（030681）的数据，但只统计三级部门级别的数据，不包括四级部门数据
    (dept.l3_dept_code = '030681' AND dept.l4_dept_code IS NULL)
  )
UNION ALL
-- 统计研发管理部（030681）下所有四级部门及其下所有五级部门的干部岗位
SELECT 
    COUNT(*) AS totalPositionCount,
    SUM(CASE WHEN c.position_ai_maturity IN ('L2', 'L3') THEN 1 ELSE 0 END) AS l2L3PositionCount,
    CASE 
        WHEN COUNT(*) = 0 THEN 0.0
        ELSE ROUND(SUM(CASE WHEN c.position_ai_maturity IN ('L2', 'L3') THEN 1 ELSE 0 END) * 1.0 / COUNT(*), 4)
    END AS l2L3PositionRatio
FROM t_cadre c
INNER JOIN department_info_hrms dept ON (c.mini_departname_id = dept.dept_code AND DATE(dept.update_time) = (SELECT MAX(DATE(update_time)) FROM department_info_hrms))
WHERE c.mini_departname_id IN (
    -- 研发管理部（030681）下所有四级部门本身的dept_code + 这些四级部门下所有五级部门的dept_code
    -- （在应用层通过DepartmentInfoMapper查询得到）
    -- 因为有些干部的最小部门可能为四级部门或五级部门，所以需要将这些部门的dept_code都加入到查询列表中
)
  AND dept.l3_dept_code = '030681'
  AND dept.l4_dept_code IS NOT NULL
```

**说明**：
- 汇总数据包括两部分：
  1. 云核心网产品线（031562）下所有三级部门的干部岗位（不包括研发管理部下的四级部门数据，避免重复统计）
  2. 研发管理部（030681）下所有四级部门及其下所有五级部门的干部岗位
- 在应用层将两部分数据合并计算汇总结果
- **重要**：统计四级部门时，需要包含该四级部门及其下所有五级部门的干部数据

#### 4.2 统计汇总的L2干部数据（按软件类/非软件类）

**SQL逻辑**：
```sql
-- t_cadre表只有mini_departname_id字段，需要通过department_info_hrms表获取部门层级
-- 统计云核心网产品线（031562）下所有三级部门的L2干部（不包括研发管理部下的四级部门）
SELECT 
    COUNT(*) AS l2TotalCount,
    SUM(CASE WHEN c.cadre_competence_category = '软件类' THEN 1 ELSE 0 END) AS l2SoftwareCount,
    SUM(CASE WHEN c.cadre_competence_category != '软件类' OR c.cadre_competence_category IS NULL THEN 1 ELSE 0 END) AS l2NonSoftwareCount
FROM t_cadre c
INNER JOIN department_info_hrms dept ON (c.mini_departname_id = dept.dept_code AND DATE(dept.update_time) = (SELECT MAX(DATE(update_time)) FROM department_info_hrms))
WHERE c.position_ai_maturity = 'L2'
  AND c.mini_departname_id IN (
    -- 云核心网产品线（031562）下所有三级部门及其下所有子部门的mini_departname_id列表
    -- 但排除研发管理部（030681）下的四级部门及其子部门
    -- （在应用层通过DepartmentInfoMapper查询得到）
)
  AND (
    (dept.l3_dept_code != '030681')
    OR
    (dept.l3_dept_code = '030681' AND dept.l4_dept_code IS NULL)
  )
UNION ALL
-- 统计研发管理部（030681）下所有四级部门及其下所有五级部门的L2干部
SELECT 
    COUNT(*) AS l2TotalCount,
    SUM(CASE WHEN c.cadre_competence_category = '软件类' THEN 1 ELSE 0 END) AS l2SoftwareCount,
    SUM(CASE WHEN c.cadre_competence_category != '软件类' OR c.cadre_competence_category IS NULL THEN 1 ELSE 0 END) AS l2NonSoftwareCount
FROM t_cadre c
INNER JOIN department_info_hrms dept ON (c.mini_departname_id = dept.dept_code AND DATE(dept.update_time) = (SELECT MAX(DATE(update_time)) FROM department_info_hrms))
WHERE c.position_ai_maturity = 'L2'
  AND c.mini_departname_id IN (
    -- 研发管理部（030681）下所有四级部门本身的dept_code + 这些四级部门下所有五级部门的dept_code
    -- （在应用层通过DepartmentInfoMapper查询得到）
    -- 因为有些干部的最小部门可能为四级部门或五级部门，所以需要将这些部门的dept_code都加入到查询列表中
)
  AND dept.l3_dept_code = '030681'
  AND dept.l4_dept_code IS NOT NULL
```

#### 4.3 统计汇总的L3干部数据（按软件类/非软件类）

**SQL逻辑**：
```sql
-- t_cadre表只有mini_departname_id字段，需要通过department_info_hrms表获取部门层级
-- 统计云核心网产品线（031562）下所有三级部门的L3干部（不包括研发管理部下的四级部门）
SELECT 
    COUNT(*) AS l3TotalCount,
    SUM(CASE WHEN c.cadre_competence_category = '软件类' THEN 1 ELSE 0 END) AS l3SoftwareCount,
    SUM(CASE WHEN c.cadre_competence_category != '软件类' OR c.cadre_competence_category IS NULL THEN 1 ELSE 0 END) AS l3NonSoftwareCount
FROM t_cadre c
INNER JOIN department_info_hrms dept ON (c.mini_departname_id = dept.dept_code AND DATE(dept.update_time) = (SELECT MAX(DATE(update_time)) FROM department_info_hrms))
WHERE c.position_ai_maturity = 'L3'
  AND c.mini_departname_id IN (
    -- 云核心网产品线（031562）下所有三级部门及其下所有子部门的mini_departname_id列表
    -- 但排除研发管理部（030681）下的四级部门及其子部门
    -- （在应用层通过DepartmentInfoMapper查询得到）
)
  AND (
    (dept.l3_dept_code != '030681')
    OR
    (dept.l3_dept_code = '030681' AND dept.l4_dept_code IS NULL)
  )
UNION ALL
-- 统计研发管理部（030681）下所有四级部门及其下所有五级部门的L3干部
SELECT 
    COUNT(*) AS l3TotalCount,
    SUM(CASE WHEN c.cadre_competence_category = '软件类' THEN 1 ELSE 0 END) AS l3SoftwareCount,
    SUM(CASE WHEN c.cadre_competence_category != '软件类' OR c.cadre_competence_category IS NULL THEN 1 ELSE 0 END) AS l3NonSoftwareCount
FROM t_cadre c
INNER JOIN department_info_hrms dept ON (c.mini_departname_id = dept.dept_code AND DATE(dept.update_time) = (SELECT MAX(DATE(update_time)) FROM department_info_hrms))
WHERE c.position_ai_maturity = 'L3'
  AND c.mini_departname_id IN (
    -- 研发管理部（030681）下所有四级部门本身的dept_code + 这些四级部门下所有五级部门的dept_code
    -- （在应用层通过DepartmentInfoMapper查询得到）
    -- 因为有些干部的最小部门可能为四级部门或五级部门，所以需要将这些部门的dept_code都加入到查询列表中
)
  AND dept.l3_dept_code = '030681'
  AND dept.l4_dept_code IS NOT NULL
```

### 5. 数据说明

**干部岗位成熟度（position_ai_maturity）**：
- `L2`：L2干部岗位
- `L3`：L3干部岗位
- 其他值：非L2/L3干部岗位（不统计在L2/L3范围内）

**职位类（cadre_competence_category）**：
- `'软件类'`：软件类干部
- 其他值或`NULL`：非软件类干部

**统计范围**：
- 云核心网产品线下的所有三级部门：通过`mini_departname_id`关联`department_info_hrms`表，查询云核心网产品线（031562）下的所有三级部门及其下所有四级、五级部门的干部数据
- 研发管理部下的四级部门：通过`mini_departname_id`关联`department_info_hrms`表，查询研发管理部（030681）下的所有四级部门及其下所有五级部门的干部数据
- **重要**：
  1. `t_cadre`表中没有各级部门的编码字段，只有`mini_departname_id`字段（最小部门编码）
  2. 需要通过`mini_departname_id`关联`department_info_hrms`表来获取部门层级信息
  3. **查询三级部门时**：需要包含该三级部门本身的`dept_code` + 该三级部门下所有四级部门的`dept_code` + 该三级部门下所有五级部门的`dept_code`，因为有些干部的最小部门可能为三级、四级或五级部门
  4. **查询四级部门时**：需要包含该四级部门本身的`dept_code` + 该四级部门下所有五级部门的`dept_code`，因为有些干部的最小部门可能为四级或五级部门
  5. **注意**：六级部门没有干部数据，所以不需要查询六级部门
  6. 研发管理部（030681）作为三级部门时，如果其下有四级部门数据，则三级部门级别的数据只统计四级部门编码为NULL的记录，避免与四级部门数据重复

**部门返回规则**：
- 所有在`t_cadre`表中有干部数据的云核心网产品线下的三级部门都会出现在`departmentList`中
- 所有在`t_cadre`表中有干部数据的研发管理部下的四级部门都会出现在`departmentList`中
- 即使某个部门没有L2或L3干部（`l2Statistics.totalCount`或`l3Statistics.totalCount`为0），该部门仍会返回，对应的统计字段值为0
- 如果某个部门没有任何干部数据，该部门不会出现在`departmentList`中

## 注意事项

1. **数据去重**：如果同一员工有多条干部记录，需要根据业务需求决定统计策略（是否按员工去重，还是按记录统计）
2. **统计范围**：
   - 云核心网产品线下的所有三级部门：包括研发管理部（030681）作为三级部门时的数据（但只统计`l4_department_code`为空或NULL的记录）
   - 研发管理部下的四级部门：只统计`l3_department_code = '030681'`且`l4_department_code`不为空的记录
   - 汇总数据：合并云核心网产品线（031562）下所有三级部门（不包括研发管理部下的四级部门）和研发管理部下所有四级部门的数据
3. **软件类与非软件类区分**：
   - 软件类：`cadre_competence_category = '软件类'`
   - 非软件类：`cadre_competence_category != '软件类'` 或 `cadre_competence_category IS NULL`
4. **占比计算**：
   - L2/L3干部岗位占比 = L2/L3干部岗位总数 / 干部总岗位数
   - 当总岗位数为0时，占比应返回0.0
   - 占比保留4位小数，前端可根据需要格式化显示（如保留2位小数或百分比形式）
5. **性能考虑**：如果数据量较大，建议在相关字段上建立索引：
   - `l3_department_code`
   - `l4_department_code`
   - `position_ai_maturity`
   - `cadre_competence_category`
6. **日志记录**：建议记录查询操作的详细日志，便于问题排查

## 待确认事项

1. **统计粒度**：如果同一员工有多条干部记录，是按员工去重统计还是按记录统计？
2. **t_cadre表字段确认**：
   - **已确认**：`t_cadre`表中没有各级部门的编码字段，只有`mini_departname_id`字段（最小部门编码）
   - 需要通过`mini_departname_id`关联`department_info_hrms`表来获取部门层级信息
   - 需要确认`department_info_hrms`表的字段结构，特别是：
     - `dept_code`：部门编码（对应`t_cadre.mini_departname_id`）
     - `l2_dept_code`、`l3_dept_code`、`l4_dept_code`：各级部门编码
     - `l2_dept_name`、`l3_dept_name`、`l4_dept_name`：各级部门名称
     - `update_time`：更新时间（用于获取最新数据）
3. **研发管理部数据处理**：
   - 研发管理部（030681）作为三级部门时，如果其下有四级部门数据，三级部门级别的统计是否只统计`l4_department_code`为空或NULL的记录？
   - 还是需要将研发管理部作为三级部门时，统计其下所有数据（包括四级部门）？
4. **空值处理**：当总岗位数为0时，占比字段应该返回0.0还是null？
5. **数据范围**：是否需要过滤某些状态的数据（如`status`字段）？
6. **部门排序**：部门列表的排序方式（按部门编码排序还是按部门名称排序）？

