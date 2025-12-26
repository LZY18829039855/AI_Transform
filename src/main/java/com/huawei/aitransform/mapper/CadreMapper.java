package com.huawei.aitransform.mapper;

import com.huawei.aitransform.entity.CadreInfoVO;
import com.huawei.aitransform.entity.CadreQualificationVO;
import com.huawei.aitransform.entity.EmployeeDetailVO;
import com.huawei.aitransform.entity.EmployeeWithCategoryVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 干部信息Mapper接口
 */
@Mapper
public interface CadreMapper {

    /**
     * 根据部门编码列表查询干部工号列表
     * @param deptCodes 部门编码列表
     * @return 干部工号列表
     */
    List<String> getCadreEmployeeNumbersByDeptCodes(@Param("deptCodes") List<String> deptCodes);

    /**
     * 根据部门编码列表查询干部工号和职位类
     * @param deptCodes 部门编码列表
     * @return 干部工号和职位类列表
     */
    List<EmployeeWithCategoryVO> getCadreEmployeesWithCategoryByDeptCodes(@Param("deptCodes") List<String> deptCodes);

    /**
     * 根据部门编码列表查询干部信息（工号、部门编码、职位类）
     * @param deptCodes 部门编码列表
     * @return 干部信息列表
     */
    List<CadreInfoVO> getCadreInfoByDeptCodes(@Param("deptCodes") List<String> deptCodes);

    /**
     * 根据部门编码列表、AI成熟度和职位类查询干部认证详细信息
     * @param deptCodes 部门编码列表
     * @param aiMaturity AI成熟度（L5代表查询L2和L3的数据）
     * @param jobCategory 职位类
     * @param queryType 查询类型（1-任职人数，2-基线人数），默认为1
     * @return 干部认证详细信息列表
     */
    List<EmployeeDetailVO> getCadreCertDetailsByConditions(
            @Param("deptCodes") List<String> deptCodes,
            @Param("aiMaturity") String aiMaturity,
            @Param("jobCategory") String jobCategory,
            @Param("queryType") Integer queryType);

    /**
     * 根据部门编码列表、AI成熟度和职位类查询干部任职详细信息
     * @param deptCodes 部门编码列表
     * @param aiMaturity AI成熟度
     * @param jobCategory 职位类
     * @param queryType 查询类型（1-任职人数，2-基线人数）
     * @return 干部任职详细信息列表
     */
    List<EmployeeDetailVO> getCadreQualifiedDetailsByConditions(
            @Param("deptCodes") List<String> deptCodes,
            @Param("aiMaturity") String aiMaturity,
            @Param("jobCategory") String jobCategory,
            @Param("queryType") Integer queryType);

    /**
     * 查询所有L2、L3干部及其最高AI任职级别
     * @return 干部任职信息列表
     */
    List<CadreQualificationVO> getL2L3CadreWithHighestQualification();

    /**
     * 批量更新干部的is_qualifications_standard字段
     * @param employeeNumbers 需要更新为达标的干部工号列表
     * @return 更新的记录数
     */
    int batchUpdateQualificationStandard(@Param("employeeNumbers") List<String> employeeNumbers);

    /**
     * 批量将干部的is_qualifications_standard字段重置为0
     * @param employeeNumbers 需要重置的干部工号列表
     * @return 更新的记录数
     */
    int batchResetQualificationStandard(@Param("employeeNumbers") List<String> employeeNumbers);

    /**
     * 批量更新干部的is_cert_standard字段为1
     * @param employeeNumbers 需要更新为达标的干部工号列表
     * @return 更新的记录数
     */
    int batchUpdateCertStandard(@Param("employeeNumbers") List<String> employeeNumbers);

    /**
     * 批量将干部的is_cert_standard字段重置为0
     * @param employeeNumbers 需要重置的干部工号列表
     * @return 更新的记录数
     */
    int batchResetCertStandard(@Param("employeeNumbers") List<String> employeeNumbers);

    /**
     * 查询所有L2、L3干部及其专业级证书和专业级科目二通过情况
     * @return 干部认证信息列表
     */
    List<CadreQualificationVO> getL2L3CadreWithCertInfo();

    /**
     * 统计各三级部门的干部岗位数据
     * @param deptCodes 部门编码列表（包含三级部门及其下所有四级、五级部门的dept_code）
     * @return 三级部门统计数据列表
     */
    List<com.huawei.aitransform.entity.DepartmentPositionStatisticsVO> getL3DepartmentPositionStatistics(@Param("deptCodes") List<String> deptCodes);

    /**
     * 统计各三级部门的L2干部数据（按软件类/非软件类）
     * @param deptCodes 部门编码列表（包含三级部门及其下所有四级、五级部门的dept_code）
     * @return 三级部门L2统计数据列表
     */
    List<com.huawei.aitransform.entity.DepartmentL2L3StatisticsVO> getL3DepartmentL2Statistics(@Param("deptCodes") List<String> deptCodes);

    /**
     * 统计各三级部门的L3干部数据（按软件类/非软件类）
     * @param deptCodes 部门编码列表（包含三级部门及其下所有四级、五级部门的dept_code）
     * @return 三级部门L3统计数据列表
     */
    List<com.huawei.aitransform.entity.DepartmentL2L3StatisticsVO> getL3DepartmentL3Statistics(@Param("deptCodes") List<String> deptCodes);

    /**
     * 统计各四级部门的干部岗位数据
     * @param deptCodes 部门编码列表（包含四级部门及其下所有五级部门的dept_code）
     * @return 四级部门统计数据列表
     */
    List<com.huawei.aitransform.entity.DepartmentPositionStatisticsVO> getL4DepartmentPositionStatistics(@Param("deptCodes") List<String> deptCodes);

    /**
     * 统计各四级部门的L2干部数据（按软件类/非软件类）
     * @param deptCodes 部门编码列表（包含四级部门及其下所有五级部门的dept_code）
     * @return 四级部门L2统计数据列表
     */
    List<com.huawei.aitransform.entity.DepartmentL2L3StatisticsVO> getL4DepartmentL2Statistics(@Param("deptCodes") List<String> deptCodes);

    /**
     * 统计各四级部门的L3干部数据（按软件类/非软件类）
     * @param deptCodes 部门编码列表（包含四级部门及其下所有五级部门的dept_code）
     * @return 四级部门L3统计数据列表
     */
    List<com.huawei.aitransform.entity.DepartmentL2L3StatisticsVO> getL4DepartmentL3Statistics(@Param("deptCodes") List<String> deptCodes);

    /**
     * 统计汇总数据（云核心网产品线下所有三级部门，不包括研发管理部下的四级部门）
     * @param deptCodes 部门编码列表
     * @param excludeL4DeptCodes 需要排除的四级部门编码列表（研发管理部下的四级部门）
     * @return 汇总统计数据
     */
    com.huawei.aitransform.entity.SummaryStatisticsVO getSummaryPositionStatistics(
            @Param("deptCodes") List<String> deptCodes,
            @Param("excludeL4DeptCodes") List<String> excludeL4DeptCodes);

    /**
     * 统计汇总的L2干部数据（按软件类/非软件类）
     * @param deptCodes 部门编码列表
     * @param excludeL4DeptCodes 需要排除的四级部门编码列表（研发管理部下的四级部门）
     * @return L2汇总统计数据
     */
    com.huawei.aitransform.entity.L2L3StatisticsVO getSummaryL2Statistics(
            @Param("deptCodes") List<String> deptCodes,
            @Param("excludeL4DeptCodes") List<String> excludeL4DeptCodes);

    /**
     * 统计汇总的L3干部数据（按软件类/非软件类）
     * @param deptCodes 部门编码列表
     * @param excludeL4DeptCodes 需要排除的四级部门编码列表（研发管理部下的四级部门）
     * @return L3汇总统计数据
     */
    com.huawei.aitransform.entity.L2L3StatisticsVO getSummaryL3Statistics(
            @Param("deptCodes") List<String> deptCodes,
            @Param("excludeL4DeptCodes") List<String> excludeL4DeptCodes);

    /**
     * 统计研发管理部下所有四级部门的汇总数据
     * @param deptCodes 部门编码列表（研发管理部下所有四级部门及其下所有五级部门的dept_code）
     * @return 汇总统计数据
     */
    com.huawei.aitransform.entity.SummaryStatisticsVO getL4SummaryPositionStatistics(@Param("deptCodes") List<String> deptCodes);

    /**
     * 统计研发管理部下所有四级部门的L2汇总数据
     * @param deptCodes 部门编码列表（研发管理部下所有四级部门及其下所有五级部门的dept_code）
     * @return L2汇总统计数据
     */
    com.huawei.aitransform.entity.L2L3StatisticsVO getL4SummaryL2Statistics(@Param("deptCodes") List<String> deptCodes);

    /**
     * 统计研发管理部下所有四级部门的L3汇总数据
     * @param deptCodes 部门编码列表（研发管理部下所有四级部门及其下所有五级部门的dept_code）
     * @return L3汇总统计数据
     */
    com.huawei.aitransform.entity.L2L3StatisticsVO getL4SummaryL3Statistics(@Param("deptCodes") List<String> deptCodes);
}

