package com.huawei.aitransform.mapper;

import com.huawei.aitransform.entity.CadreInfoVO;
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
}

