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
     * @param cadreList 需要更新为达标的干部列表
     * @return 更新的记录数
     */
    int batchUpdateQualificationStandard(@Param("cadreList") List<CadreQualificationVO> cadreList);

    /**
     * 批量将干部的is_qualifications_standard字段重置为0
     * @param cadreList 需要重置的干部列表
     * @return 更新的记录数
     */
    int batchResetQualificationStandard(@Param("cadreList") List<CadreQualificationVO> cadreList);

    /**
     * 批量更新干部的is_cert_standard字段为1
     * @param cadreList 需要更新为达标的干部列表
     * @return 更新的记录数
     */
    int batchUpdateCertStandard(@Param("cadreList") List<CadreQualificationVO> cadreList);

    /**
     * 批量将干部的is_cert_standard字段重置为0
     * @param cadreList 需要重置的干部列表
     * @return 更新的记录数
     */
    int batchResetCertStandard(@Param("cadreList") List<CadreQualificationVO> cadreList);

    /**
     * 查询所有L2、L3干部及其专业级证书和专业级科目二通过情况
     * @return 干部认证信息列表
     */
    List<CadreQualificationVO> getL2L3CadreWithCertInfo();


    /**
     * 查询所有干部及其最小部门ID
     * @return 干部信息列表（包含account和mini_departname_id）
     */
    List<com.huawei.aitransform.entity.CadreDepartmentRefreshVO> getAllCadreWithMiniDepartment();

    /**
     * 批量更新干部的部门编码字段
     * @param cadreList 干部部门信息列表
     * @return 更新的记录数
     */
    int batchUpdateCadreDepartmentCodes(@Param("cadreList") List<com.huawei.aitransform.entity.CadreDepartmentRefreshVO> cadreList);

    /**
     * 单个更新干部的部门编码字段
     * @param cadre 干部部门信息
     * @return 更新的记录数
     */
    int updateCadreDepartmentCodes(com.huawei.aitransform.entity.CadreDepartmentRefreshVO cadre);

    /**
     * 根据二级部门编码统计干部岗位数据
     * @param deptCode 二级部门编码
     * @return 统计结果
     */
    com.huawei.aitransform.entity.CadreStatisticsCountVO getCadreStatisticsByL2DeptCode(@Param("deptCode") String deptCode);

    /**
     * 根据三级部门编码统计干部岗位数据
     * @param deptCode 三级部门编码
     * @return 统计结果
     */
    com.huawei.aitransform.entity.CadreStatisticsCountVO getCadreStatisticsByL3DeptCode(@Param("deptCode") String deptCode);

    /**
     * 根据四级部门编码统计干部岗位数据
     * @param deptCode 四级部门编码
     * @return 统计结果
     */
    com.huawei.aitransform.entity.CadreStatisticsCountVO getCadreStatisticsByL4DeptCode(@Param("deptCode") String deptCode);

    /**
     * 根据二级部门编码统计干部AI任职认证数据
     * @param deptCode 二级部门编码
     * @return 统计结果
     */
    com.huawei.aitransform.entity.CadreAiCertCountVO getCadreAiCertStatisticsByL2DeptCode(@Param("deptCode") String deptCode);

    /**
     * 根据三级部门编码统计干部AI任职认证数据
     * @param deptCode 三级部门编码
     * @return 统计结果
     */
    com.huawei.aitransform.entity.CadreAiCertCountVO getCadreAiCertStatisticsByL3DeptCode(@Param("deptCode") String deptCode);

    /**
     * 根据四级部门编码统计干部AI任职认证数据
     * @param deptCode 四级部门编码
     * @return 统计结果
     */
    com.huawei.aitransform.entity.CadreAiCertCountVO getCadreAiCertStatisticsByL4DeptCode(@Param("deptCode") String deptCode);

    /**
     * 查询所有干部的工号列表
     * @return 干部工号列表
     */
    List<String> getAllCadreEmployeeNumbers();
}

