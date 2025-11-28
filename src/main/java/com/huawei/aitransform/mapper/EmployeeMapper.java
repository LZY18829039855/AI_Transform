package com.huawei.aitransform.mapper;

import com.huawei.aitransform.entity.EmployeeDetailVO;
import com.huawei.aitransform.entity.EmployeeWithCategoryVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 员工信息Mapper接口
 */
@Mapper
public interface EmployeeMapper {

    /**
     * 根据部门层级和部门ID列表查询员工工号列表
     * @param deptLevel 部门层级（1-7）
     * @param deptIds 部门ID列表
     * @return 员工工号列表（非离职人员，expired_date为空）
     */
    List<String> getEmployeeNumbersByDeptLevel(
            @Param("deptLevel") Integer deptLevel,
            @Param("deptIds") List<String> deptIds);

    /**
     * 根据部门层级和部门ID列表查询员工工号和职位类
     * @param deptLevel 部门层级（1-7）
     * @param deptIds 部门ID列表
     * @return 员工工号和职位类列表（非离职人员，expired_date为空）
     */
    List<EmployeeWithCategoryVO> getEmployeesWithCategoryByDeptLevel(
            @Param("deptLevel") Integer deptLevel,
            @Param("deptIds") List<String> deptIds);

    /**
     * 根据部门层级和部门ID查询员工认证详细信息（用于下钻）
     * @param deptLevel 部门层级（1-7）
     * @param deptId 部门ID
     * @return 员工详细信息列表（包含认证信息）
     */
    List<EmployeeDetailVO> getEmployeeCertDetailsByDeptLevel(
            @Param("deptLevel") Integer deptLevel,
            @Param("deptId") String deptId);

    /**
     * 根据部门层级和部门编码列表查询员工工号和职位族信息
     * @param deptLevel 部门层级（1-7），根据部门本身的层级查询对应字段
     * @param deptCodes 部门编码列表
     * @return 员工工号和职位族列表（包含job_category字段，格式：职位族-职位类-职位子类）
     */
    List<EmployeeWithCategoryVO> getEmployeesWithJobCategoryByDeptCodes(
            @Param("deptLevel") Integer deptLevel,
            @Param("deptCodes") List<String> deptCodes);
}

