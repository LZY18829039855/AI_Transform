package com.huawei.aitransform.mapper;

import com.huawei.aitransform.entity.CreditOverviewVO;
import com.huawei.aitransform.entity.EmployeeCreditRow;
import com.huawei.aitransform.entity.PersonalCredit;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.huawei.aitransform.entity.SchoolCreditDetailRequestVO;
import com.huawei.aitransform.entity.SchoolCreditDetailVO;
import com.huawei.aitransform.entity.SchoolRoleSummaryVO;

import java.util.List;

/**
 * 个人学分Mapper接口
 */
@Mapper
public interface PersonalCreditMapper {

    /**
     * 根据工号查询个人学分信息
     * @param employeeNumber 工号
     * @return 个人学分信息
     */
    PersonalCredit getByEmployeeNumber(@Param("employeeNumber") String employeeNumber);

    /**
     * 根据工号列表批量查询个人学分信息
     * @param employeeNumbers 工号列表
     * @return 个人学分信息列表
     */
    List<PersonalCredit> getByEmployeeNumbers(@Param("employeeNumbers") List<String> employeeNumbers);

    /**
     * 批量插入或更新个人学分信息
     * @param list 个人学分信息列表
     * @return 影响行数
     */
    int batchInsertOrUpdate(@Param("list") List<PersonalCredit> list);

    /**
     * 插入或更新个人学分信息
     * @param personalCredit 个人学分信息
     * @return 影响行数
     */
    int insertOrUpdate(PersonalCredit personalCredit);

    /**
     * 批量更新部门标杆学分达成率
     * @param lowestDeptNumber 最小部门编号
     * @param benchmarkRate 标杆达成率
     */
    void updateBenchmarkRateByDept(@Param("lowestDeptNumber") String lowestDeptNumber, @Param("benchmarkRate") java.math.BigDecimal benchmarkRate);

    /**
     * 查询某部门下的最大个人学分达成率
     * @param lowestDeptNumber 最小部门编号
     * @return 最大达成率
     */
    java.math.BigDecimal getMaxCompletionRateByDept(@Param("lowestDeptNumber") String lowestDeptNumber);

    /**
     * 获取所有有变动的最小部门编号列表（基于最近更新的记录）
     * 实际业务中可能直接遍历所有部门，或者在SyncTask中处理
     * 这里提供一个查询所有部门编号的方法
     */
    List<String> getAllLowestDeptNumbers();

    /**
     * 获取职位学分统计
     * @param deptCode 部门编码（可选）
     * @param role 角色 (可选)
     * @return 统计列表
     */
    List<CreditOverviewVO> getPositionStatistics(@Param("deptCode") String deptCode, @Param("role") String role);

    /**
     * 获取部门学分统计
     * @param level 部门层级字段名 (e.g. lowest_dept, firstdept)
     * @param deptCode 部门编码（可选）
     * @param role 角色 (可选)
     * @return 统计列表
     */
    List<CreditOverviewVO> getDepartmentStatistics(@Param("level") String level, @Param("levelCode") String levelCode, @Param("deptCode") String deptCode, @Param("role") String role);

    CreditOverviewVO getTotalStatistics(@Param("deptCode") String deptCode, @Param("role") String role);

    /**
     * 根据指定的部门编码列名查询匹配的记录数
     * @param columnName 列名（如 firstdeptcode, seconddeptcode 等）
     * @param deptCode 部门编码
     * @return 匹配的记录数
     */
    Long countByDeptCodeColumn(@Param("columnName") String columnName, @Param("deptCode") String deptCode);

    /**
     * 删除不存在的
     * @param incomingEmployeeNumbers 不存在列表
     */
    void deleteNotInEmployeeNumbers(@Param("list") List<String> incomingEmployeeNumbers);

    /**
     * 查询学分明细列表（分页用，配合 PageHelper 或手动 LIMIT）
     */
    List<SchoolCreditDetailVO> getSchoolCreditDetailList(SchoolCreditDetailRequestVO request);

    /**
     * 查询学分明细总数
     */
    Long countSchoolCreditDetail(SchoolCreditDetailRequestVO request);

    List<SchoolRoleSummaryVO> getExpertRoleSummary(@Param("deptCode") String deptCode);
    List<SchoolRoleSummaryVO> getCadreRoleSummary(@Param("deptCode") String deptCode);

    /**
     * 批量查询 AI 认证学分（工号 -> 认证学分）。
     * 专业级 15、工作级 10，同一人 MAX 取最高，自然上限 15。
     * 来源表：dwr_t_cert_record_t；过滤：status=1 OR approved_status=1；
     * 标题白名单：华为研究类能力认证（专业级/工作级，AI算法技术/AI决策推理/AI图像语言语义）。
     * 工号匹配：employee_number 或 w3_account 命中任一即可，结果以 COALESCE 聚合。
     */
    List<EmployeeCreditRow> getAiCertCreditsByEmployeeNumbers(@Param("employeeNumbers") List<String> employeeNumbers);

    /**
     * 批量查询 AI 任职学分（工号 -> 任职学分）。
     * 4 级及以上 25、3 级 10，同一人 MAX 取最高，自然上限 25。
     * 来源表：t_qualifications；
     * 范围过滤：direction_cn_name 属于指定 AI 方向 或 competence_subcategory_cn='AI算法及应用'；
     * 有效性：competence_from/competence_to 非空，且 CURDATE() 位于区间内。
     */
    List<EmployeeCreditRow> getAiQualificationCreditsByEmployeeNumbers(@Param("employeeNumbers") List<String> employeeNumbers);
}