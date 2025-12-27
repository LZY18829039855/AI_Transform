# 接口文档：AI干部任职认证统计

## 接口概述

**接口路径**：`GET /cadre-cert-statistics/cadre-ai-certification-overview`

**接口描述**：查询干部AI任职认证的数据。统计所有三级部门以及研发管理部下面的四级部门的数据。

**业务逻辑**：
1.  **查询范围**：
    -   云核心网产品线（031562）下的所有三级部门。
    -   研发管理部（030681）下的所有四级部门。
2.  **统计逻辑**：
    -   针对每个部门，统计以下数据：
        -   **干部总人数** (`totalCadreCount`)：该部门下的所有干部总数。
        -   **L2L3人数** (`l2L3Count`)：`position_ai_maturity` 为 'L2' 或 'L3' 的干部人数。
        -   **软件L2人数** (`softwareL2Count`)：`position_ai_maturity` 为 'L2' 且 `cadre_competence_category` 为 '软件类' 的人数。
        -   **软件L3人数** (`softwareL3Count`)：`position_ai_maturity` 为 'L3' 且 `cadre_competence_category` 为 '软件类' 的人数。
        -   **非软件L2L3人数** (`nonSoftwareL2L3Count`)：`position_ai_maturity` 为 'L2' 或 'L3'，且 `cadre_competence_category` 不为 '软件类' (或为 NULL) 的人数。
        -   **满足岗位AI要求的L2L3人数** (`qualifiedL2L3Count`)：`position_ai_maturity` 为 'L2' 或 'L3'，且 `is_qualifications_standard` (任职) 为 1，且 `is_cert_standard` (认证) 为 1 的人数。
        -   **满足岗位AI要求的L2L3干部占比** (`qualifiedL2L3Ratio`)：`qualifiedL2L3Count` / `totalCadreCount`（保留 4 位小数）。若 `totalCadreCount` 为 0，则占比为 0。

3.  **数据汇总**：
    -   返回一个汇总数据 (`summary`)，包含上述所有部门数据的累加值。
    -   返回部门列表 (`departmentList`)，包含各部门的详细统计数据。对于研发管理部，其下属的四级部门数据将作为子节点 (`children`) 或平铺返回（参考现有 `cadre-position-overview` 接口，研发管理部下的四级部门是嵌套在研发管理部节点下的）。

## 请求参数

无

## 响应结果

### 成功响应示例

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "summary": {
      "totalCadreCount": 1000,
      "l2L3Count": 600,
      "softwareL2Count": 200,
      "softwareL3Count": 100,
      "nonSoftwareL2L3Count": 300,
      "qualifiedL2L3Count": 400,
      "qualifiedL2L3Ratio": 0.4000
    },
    "departmentList": [
      {
        "deptCode": "030681",
        "deptName": "研发管理部",
        "deptLevel": "L3",
        "totalCadreCount": 200,
        "l2L3Count": 150,
        "softwareL2Count": 50,
        "softwareL3Count": 30,
        "nonSoftwareL2L3Count": 70,
        "qualifiedL2L3Count": 100,
        "qualifiedL2L3Ratio": 0.5000,
        "children": [
            {
                "deptCode": "03068101",
                "deptName": "研发管理部四级部门A",
                "deptLevel": "L4",
                "totalCadreCount": 100,
                "l2L3Count": 80,
                // ... 其他字段
            }
        ]
      },
      {
        "deptCode": "03xxxx",
        "deptName": "某三级部门",
        "deptLevel": "L3",
        "totalCadreCount": 500,
        "l2L3Count": 300,
        "softwareL2Count": 100,
        "softwareL3Count": 50,
        "nonSoftwareL2L3Count": 150,
        "qualifiedL2L3Count": 200,
        "qualifiedL2L3Ratio": 0.4000,
        "children": null
      }
    ]
  }
}
```

### 响应字段说明

| 字段名 | 类型 | 说明 |
| :--- | :--- | :--- |
| **summary** | Object | 所有部门的汇总数据 |
| **departmentList** | Array | 部门列表 |
| **deptCode** | String | 部门编码 |
| **deptName** | String | 部门名称 |
| **deptLevel** | String | 部门层级 (L3/L4) |
| **totalCadreCount** | Integer | 干部总人数 |
| **l2L3Count** | Integer | L2L3人数 |
| **softwareL2Count** | Integer | 软件L2人数 |
| **softwareL3Count** | Integer | 软件L3人数 |
| **nonSoftwareL2L3Count** | Integer | 非软件L2L3人数 |
| **qualifiedL2L3Count** | Integer | 满足岗位AI要求的L2L3人数 (任职=1且认证=1) |
| **qualifiedL2L3Ratio** | Double | 满足岗位AI要求的L2L3干部占比 (qualified/total) |
| **children** | Array | 子部门列表 (如研发管理部下的四级部门) |

## SQL 逻辑参考

针对每个部门的统计 SQL 大致如下：

```sql
SELECT
    COUNT(*) AS total_count,
    SUM(CASE WHEN position_ai_maturity IN ('L2', 'L3') THEN 1 ELSE 0 END) AS l2_l3_count,
    SUM(CASE WHEN position_ai_maturity = 'L2' AND cadre_competence_category = '软件类' THEN 1 ELSE 0 END) AS software_l2_count,
    SUM(CASE WHEN position_ai_maturity = 'L3' AND cadre_competence_category = '软件类' THEN 1 ELSE 0 END) AS software_l3_count,
    SUM(CASE WHEN position_ai_maturity IN ('L2', 'L3') AND (cadre_competence_category != '软件类' OR cadre_competence_category IS NULL) THEN 1 ELSE 0 END) AS non_software_l2_l3_count,
    SUM(CASE WHEN position_ai_maturity IN ('L2', 'L3') AND is_qualifications_standard = 1 AND is_cert_standard = 1 THEN 1 ELSE 0 END) AS qualified_l2_l3_count
FROM t_cadre
WHERE l3_department_code = #{deptCode} -- 或 l4_department_code
```

