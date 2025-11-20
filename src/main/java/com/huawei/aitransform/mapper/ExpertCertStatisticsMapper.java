package com.huawei.aitransform.mapper;

import com.huawei.aitransform.entity.EmployeeDetailVO;
import com.huawei.aitransform.entity.ExpertCertStatisticsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 专家认证统计Mapper接口
 */
@Mapper
public interface ExpertCertStatisticsMapper {

    /**
     * 查询三层组织的专家信息（按AI成熟度和职位类分组）
     * @param deptCode 部门编码
     * @param deptName 部门名称
     * @return 专家统计信息
     */
    List<ExpertCertStatisticsVO> getExpertStatisticsByLevel3(@Param("deptCode") String deptCode, @Param("deptName") String deptName);

    /**
     * 查询四层组织的专家信息（按AI成熟度和职位类分组）
     * @param deptCode 部门编码
     * @return 专家统计信息
     */
    List<ExpertCertStatisticsVO> getExpertStatisticsByLevel4(@Param("deptCode") String deptCode);

    /**
     * 查询已通过AI认证的专家工号列表
     * @param employeeNumbers 工号列表
     * @return 已认证的工号列表
     */
    List<String> getCertifiedEmployeeNumbers(@Param("employeeNumbers") List<String> employeeNumbers);

    /**
     * 查询获得AI任职的员工工号列表
     * @param employeeNumbers 员工工号列表
     * @return 获得AI任职的员工工号列表
     */
    List<String> getQualifiedEmployeeNumbers(@Param("employeeNumbers") List<String> employeeNumbers);

    /**
     * 查询已通过科目二考试的员工工号列表
     * @param employeeNumbers 员工工号列表
     * @return 已通过科目二的工号列表
     */
    List<String> getSubject2PassedEmployeeNumbers(@Param("employeeNumbers") List<String> employeeNumbers);

    /**
     * 根据部门编码、AI成熟度和职位类查询专家认证详细信息
     * @param deptCode 部门编码
     * @param deptName 部门名称（用于三层部门查询）
     * @param aiMaturity AI成熟度
     * @param jobCategory 职位类
     * @return 专家认证详细信息列表
     */
    List<EmployeeDetailVO> getExpertCertDetailsByConditions(
            @Param("deptCode") String deptCode,
            @Param("deptName") String deptName,
            @Param("aiMaturity") String aiMaturity,
            @Param("jobCategory") String jobCategory);

    /**
     * 根据部门编码、AI成熟度和职位类查询专家任职详细信息
     * @param deptCode 部门编码
     * @param deptName 部门名称（用于三层部门查询）
     * @param aiMaturity AI成熟度
     * @param jobCategory 职位类
     * @return 专家任职详细信息列表
     */
    List<EmployeeDetailVO> getExpertQualifiedDetailsByConditions(
            @Param("deptCode") String deptCode,
            @Param("deptName") String deptName,
            @Param("aiMaturity") String aiMaturity,
            @Param("jobCategory") String jobCategory);
}

