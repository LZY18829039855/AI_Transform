package com.huawei.aitransform.mapper;

import com.huawei.aitransform.entity.EmployeeWithCategoryVO;
import com.huawei.aitransform.entity.ExpertInfoVO;
import com.huawei.aitransform.entity.ExpertQualificationVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 专家信息Mapper接口
 */
@Mapper
public interface ExpertMapper {

    /**
     * 根据部门编码和部门层级查询L2/L3专家信息（关联t_employee_sync表获取job_category）
     * @param deptCode 部门编码（单个部门ID）
     * @param deptLevel 部门层级（1-7），用于确定使用哪个部门字段进行过滤
     * @return 专家信息列表（包含工号、AI成熟度、职位类）
     */
    List<ExpertInfoVO> getExpertInfoByDeptCode(
            @Param("deptCode") String deptCode,
            @Param("deptLevel") Integer deptLevel);

    /**
     * 查询所有L2、L3专家及其最高AI任职级别和职位类
     * @return 专家任职信息列表
     */
    List<ExpertQualificationVO> getL2L3ExpertWithHighestQualification();

    /**
     * 查询所有L2、L3专家及其专业级证书和科目二通过情况
     * @return 专家认证信息列表
     */
    List<ExpertQualificationVO> getL2L3ExpertWithCertInfo();

    /**
     * 批量更新专家的is_qualifications_standard字段为1
     * @param employeeNumbers 专家工号列表
     * @return 更新的记录数
     */
    int batchUpdateQualificationStandard(@Param("employeeNumbers") List<String> employeeNumbers);

    /**
     * 批量将专家的is_qualifications_standard字段重置为0
     * @param employeeNumbers 专家工号列表
     * @return 更新的记录数
     */
    int batchResetQualificationStandard(@Param("employeeNumbers") List<String> employeeNumbers);

    /**
     * 批量更新专家的is_cert_standard字段为1
     * @param employeeNumbers 专家工号列表
     * @return 更新的记录数
     */
    int batchUpdateCertStandard(@Param("employeeNumbers") List<String> employeeNumbers);

    /**
     * 批量将专家的is_cert_standard字段重置为0
     * @param employeeNumbers 专家工号列表
     * @return 更新的记录数
     */
    int batchResetCertStandard(@Param("employeeNumbers") List<String> employeeNumbers);

    /**
     * 根据部门层级和部门ID列表查询专家工号列表
     * @param deptLevel 部门层级（1-7），用于确定使用哪个部门字段进行过滤
     * @param deptIds 部门ID列表
     * @return 专家工号列表
     */
    List<String> getExpertNumbersByDeptLevel(
            @Param("deptLevel") Integer deptLevel,
            @Param("deptIds") List<String> deptIds);

    /**
     * 根据部门层级和部门编码列表查询专家工号和职位族
     * @param deptLevel 部门层级（1-7），用于确定使用哪个部门字段进行过滤
     * @param deptCodes 部门编码列表
     * @return 专家工号和职位族列表（包含job_category字段，格式：职位族-职位类-职位子类）
     */
    List<EmployeeWithCategoryVO> getExpertsWithJobCategoryByDeptCodes(
            @Param("deptLevel") Integer deptLevel,
            @Param("deptCodes") List<String> deptCodes);
}


