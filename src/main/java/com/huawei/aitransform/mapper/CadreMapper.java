package com.huawei.aitransform.mapper;

import com.huawei.aitransform.entity.CadreInfoVO;
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
}

