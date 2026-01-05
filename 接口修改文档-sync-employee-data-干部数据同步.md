# 接口修改文档：/sync-employee-data 接口增加干部数据同步功能

## 一、需求概述

当前 `/sync-employee-data` 接口只同步职位族为"研发族"的人员数据到 `t_employee` 表。现在需要增加干部数据的同步功能，将 `t_cadre` 表中的所有干部数据也同步到 `t_employee` 表中。

## 二、当前实现逻辑

### 2.1 接口信息
- **接口路径**：`POST /external-api/sync-employee-data`
- **功能**：同步 `t_employee_sync` 表数据到 `t_employee` 表
- **当前过滤条件**：
  - `period_id = #{periodId}`（自动推算：当前日期减2天，格式yyyyMMdd）
  - `SUBSTRING_INDEX(e.job_category, '-', 1) = '研发族'`（职位族为研发族）

### 2.2 当前数据流程
1. 调用 `EmployeeMapper.getEmployeeSyncData(periodId)` 查询研发族数据
2. 获取 `t_employee` 表全量数据
3. 对比源数据和目标数据，执行新增/更新/删除操作
4. 批量执行数据库操作（分批处理，每批1000条）

### 2.3 当前查询方法
- **方法名**：`getEmployeeSyncData`
- **位置**：`EmployeeMapper.xml`
- **查询表**：`t_employee_sync`
- **过滤条件**：
  ```sql
  WHERE e.period_id = #{periodId}
  AND SUBSTRING_INDEX(e.job_category, '-', 1) = '研发族'
  ```

## 三、修改方案

### 3.1 修改目标
1. 保留现有的研发族数据同步逻辑
2. 新增干部数据同步逻辑：
   - 从 `t_cadre` 表查询所有干部的工号（`account` 字段）
   - 通过工号查询 `t_employee_sync` 表中的干部数据
   - 过滤条件：只有 `period_id` 和工号，**不过滤研发族**
3. 合并研发族数据和干部数据（去重，以工号为唯一标识）
4. 执行统一的同步逻辑

### 3.2 需要修改的文件

#### 3.2.1 EmployeeMapper.xml
**新增查询方法**：`getEmployeeSyncDataByEmployeeNumbers`

**功能**：根据工号列表和 period_id 查询员工同步数据（不过滤职位族）

**参数**：
- `periodId`：期号（String）
- `employeeNumbers`：工号列表（List<String>）

**SQL 逻辑**：
- 查询 `t_employee_sync` 表
- 过滤条件：
  - `e.period_id = #{periodId}`
  - `e.employee_number IN (工号列表)`
- **不添加职位族过滤条件**
- 其他 JOIN 逻辑与 `getEmployeeSyncData` 保持一致（关联任职、认证、考试等表）

**示例 SQL 结构**：
```sql
<select id="getEmployeeSyncDataByEmployeeNumbers" resultType="com.huawei.aitransform.entity.EmployeeSyncDataVO">
    SELECT 
        e.employee_number AS employeeNumber,
        e.last_name AS lastName,
        -- ... 其他字段与 getEmployeeSyncData 保持一致
    FROM t_employee_sync e
    LEFT JOIN t_qualifications q ON (...)
    LEFT JOIN dwr_t_cert_record_t cert ON (...)
    LEFT JOIN (...) exam ON (...)
    LEFT JOIN t_qualifications q_detail ON (...)
    WHERE e.period_id = #{periodId}
    AND e.employee_number IN
    <foreach collection="employeeNumbers" item="empNo" open="(" separator="," close=")">
        #{empNo}
    </foreach>
    GROUP BY e.employee_number
</select>
```

#### 3.2.2 CadreMapper.xml（或新增方法）
**查询方法**：获取所有干部的工号列表

**方法名**：`getAllCadreEmployeeNumbers`

**功能**：查询 `t_cadre` 表中所有干部的工号

**SQL 逻辑**：
```sql
<select id="getAllCadreEmployeeNumbers" resultType="java.lang.String">
    SELECT DISTINCT account
    FROM t_cadre
    WHERE account IS NOT NULL
    AND account != ''
</select>
```

#### 3.2.3 EmployeeSyncServiceImpl.java
**修改方法**：`syncEmployeeData(String periodId)`

**修改逻辑**：
1. **保留现有逻辑**：查询研发族数据
   ```java
   List<EmployeeSyncDataVO> rndList = employeeMapper.getEmployeeSyncData(periodId);
   ```

2. **新增逻辑**：查询干部数据
   ```java
   // 2.1 查询所有干部的工号
   List<String> cadreEmployeeNumbers = cadreMapper.getAllCadreEmployeeNumbers();
   
   // 2.2 如果干部工号列表不为空，查询干部数据
   List<EmployeeSyncDataVO> cadreList = new ArrayList<>();
   if (cadreEmployeeNumbers != null && !cadreEmployeeNumbers.isEmpty()) {
       cadreList = employeeMapper.getEmployeeSyncDataByEmployeeNumbers(periodId, cadreEmployeeNumbers);
   }
   ```

3. **合并数据**（去重，以工号为唯一标识）
   ```java
   // 3.1 将研发族数据转换为 Map（工号 -> 数据）
   Map<String, EmployeeSyncDataVO> sourceMap = new HashMap<>();
   for (EmployeeSyncDataVO vo : rndList) {
       sourceMap.put(vo.getEmployeeNumber(), vo);
   }
   
   // 3.2 将干部数据合并到 Map 中（如果工号已存在，干部数据优先覆盖）
   for (EmployeeSyncDataVO vo : cadreList) {
       if (vo.getEmployeeNumber() != null) {
           sourceMap.put(vo.getEmployeeNumber(), vo);
       }
   }
   
   // 3.3 转换为 List
   List<EmployeeSyncDataVO> sourceList = new ArrayList<>(sourceMap.values());
   ```

4. **后续逻辑保持不变**：校验数据量、对比目标数据、执行同步操作

**注意事项**：
- 如果干部工号在研发族数据中已存在，干部数据会覆盖研发族数据（因为干部数据可能包含更完整的信息）
- 数据量校验逻辑需要调整：研发族数据 + 干部数据（去重后）的总数需要 >= 2000

#### 3.2.4 CadreMapper.java（接口）
**新增方法**：
```java
/**
 * 查询所有干部的工号列表
 * @return 干部工号列表
 */
List<String> getAllCadreEmployeeNumbers();
```

#### 3.2.5 EmployeeMapper.java（接口）
**新增方法**：
```java
/**
 * 根据工号列表和期号查询员工同步数据（不过滤职位族）
 * @param periodId 期号
 * @param employeeNumbers 工号列表
 * @return 员工同步数据列表
 */
List<EmployeeSyncDataVO> getEmployeeSyncDataByEmployeeNumbers(@Param("periodId") String periodId, 
                                                                @Param("employeeNumbers") List<String> employeeNumbers);
```

### 3.3 数据去重策略

**去重规则**：
- 以 `employee_number`（工号）为唯一标识
- 如果同一个工号既在研发族数据中，又在干部数据中，**干部数据优先**（覆盖研发族数据）
- 原因：干部数据可能包含更完整的信息，且干部身份更重要

### 3.4 数据量校验调整

**当前校验逻辑**：
```java
if (sourceList == null || sourceList.size() < 2000) {
    // 中止同步
}
```

**修改后逻辑**：
- 校验合并后的数据总量（研发族 + 干部，去重后）
- 如果总数 < 2000，中止同步
- 可以在返回结果中分别记录研发族数据量和干部数据量，便于排查问题

**建议返回信息**：
```java
result.put("rndCount", rndList.size());  // 研发族数据量
result.put("cadreCount", cadreList.size());  // 干部数据量（去重前）
result.put("totalSource", sourceList.size());  // 合并后总数据量（去重后）
```

## 四、实现步骤

### 步骤1：在 CadreMapper.xml 中新增查询方法
- 方法名：`getAllCadreEmployeeNumbers`
- 功能：查询所有干部的工号列表

### 步骤2：在 CadreMapper.java 中新增接口方法
- 方法签名：`List<String> getAllCadreEmployeeNumbers();`

### 步骤3：在 EmployeeMapper.xml 中新增查询方法
- 方法名：`getEmployeeSyncDataByEmployeeNumbers`
- 功能：根据工号列表和 period_id 查询员工同步数据（不过滤职位族）
- 注意：SQL 结构与 `getEmployeeSyncData` 保持一致，但：
  - WHERE 条件中不包含职位族过滤
  - 使用 `employee_number IN (工号列表)` 过滤

### 步骤4：在 EmployeeMapper.java 中新增接口方法
- 方法签名：`List<EmployeeSyncDataVO> getEmployeeSyncDataByEmployeeNumbers(@Param("periodId") String periodId, @Param("employeeNumbers") List<String> employeeNumbers);`

### 步骤5：修改 EmployeeSyncServiceImpl.java
- 在 `syncEmployeeData` 方法中：
  1. 保留研发族数据查询逻辑
  2. 新增干部工号查询逻辑
  3. 新增干部数据查询逻辑
  4. 合并数据（去重）
  5. 调整数据量校验逻辑
  6. 在返回结果中增加研发族和干部的数据量统计

### 步骤6：注入 CadreMapper
- 在 `EmployeeSyncServiceImpl` 中注入 `CadreMapper`

## 五、注意事项

### 5.1 数据一致性
- 确保干部数据查询时，`t_employee_sync` 表中存在对应的数据
- 如果干部工号在 `t_employee_sync` 表中不存在，该干部数据不会被同步

### 5.2 性能考虑
- 干部工号列表可能较大，建议分批查询（如果工号数量超过1000，可以分批查询）
- 或者使用 `IN` 查询，但需要注意数据库的 `IN` 子句长度限制

### 5.3 数据覆盖策略
- 如果同一个工号既在研发族数据中，又在干部数据中，干部数据会覆盖研发族数据
- 这是合理的，因为干部身份更重要，且干部数据可能包含更完整的信息

### 5.4 错误处理
- 如果查询干部工号失败，建议记录日志，但不影响研发族数据的同步
- 如果查询干部数据失败，建议记录日志，但不影响研发族数据的同步

### 5.5 日志记录
- 建议在关键步骤记录日志：
  - 研发族数据量
  - 干部工号数量
  - 干部数据量
  - 合并后总数据量
  - 去重数量（如果有）

## 六、测试建议

### 6.1 单元测试
1. 测试研发族数据查询（保持原有逻辑）
2. 测试干部工号查询
3. 测试干部数据查询（根据工号列表）
4. 测试数据合并和去重逻辑
5. 测试数据量校验逻辑

### 6.2 集成测试
1. 测试完整同步流程：
   - 只有研发族数据的情况
   - 只有干部数据的情况（理论上不会发生，因为干部应该也在研发族中）
   - 研发族数据和干部数据有交集的情况
   - 研发族数据和干部数据无交集的情况
2. 测试数据覆盖逻辑（同一工号在研发族和干部数据中都存在）
3. 测试异常情况：
   - 干部工号查询失败
   - 干部数据查询失败
   - 数据量不足的情况

### 6.3 数据验证
1. 验证同步后的 `t_employee` 表中：
   - 研发族人员数据正确
   - 干部数据正确
   - 数据不重复（同一工号只有一条记录）
   - 干部数据覆盖了研发族数据（如果存在）

## 七、回滚方案

如果修改后出现问题，可以：
1. 回滚代码到修改前的版本
2. 或者临时禁用干部数据查询逻辑（通过配置开关或注释代码）

## 八、预期效果

修改完成后：
1. 接口会同步研发族人员数据（保持原有功能）
2. 接口会同步所有干部数据（新增功能）
3. 如果同一工号既是研发族又是干部，干部数据会覆盖研发族数据
4. 返回结果中包含研发族和干部的数据量统计信息

---

**文档版本**：v1.0  
**创建日期**：2024-12-19  
**待确认后开始开发**

