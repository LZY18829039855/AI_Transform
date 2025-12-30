package com.huawei.aitransform.mapper;

import com.huawei.aitransform.entity.EmployeePO;
import com.huawei.aitransform.entity.EmployeeSyncDataVO;
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
     * 获取全员同步数据
     * @param periodId 期号
     * @return 员工同步数据列表
     */
    List<EmployeeSyncDataVO> getEmployeeSyncData(@Param("periodId") String periodId);

    /**
     * 获取 t_employee 表全量数据
     * @return 员工PO列表
     */
    List<EmployeePO> getAllEmployees();

    /**
     * 插入员工数据到 t_employee
     * @param employeePO 员工PO
     */
    void insertEmployee(EmployeePO employeePO);

    /**
     * 更新 t_employee 表中的员工数据
     * @param employeePO 员工PO
     */
    void updateEmployee(EmployeePO employeePO);

    /**
     * 批量插入员工数据到 t_employee
     * @param employeePOs 员工PO列表
     */
    void batchInsertEmployees(List<EmployeePO> employeePOs);

    /**
     * 批量更新 t_employee 表中的员工数据
     * @param employeePOs 员工PO列表
     */
    void batchUpdateEmployees(List<EmployeePO> employeePOs);

    /**
     * 批量从 t_employee 表中删除员工
     * @param employeeNumbers 工号列表
     */
    void batchDeleteEmployees(List<String> employeeNumbers);

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
     * 根据部门层级和部门ID查询员工认证详细信息（用于下钻，全员类型）
     * @param deptLevel 部门层级（1-7）
     * @param deptId 部门ID（单个部门编码）
     * @param jobCategory 职位类（可选）
     * @param queryType 查询类型（1-认证人数，2-基线人数）
     * @return 员工详细信息列表（包含认证信息）
     */
    List<EmployeeDetailVO> getEmployeeCertDetailsByDeptLevel(
            @Param("deptLevel") Integer deptLevel,
            @Param("deptId") String deptId,
            @Param("jobCategory") String jobCategory,
            @Param("queryType") Integer queryType);

    /**
     * 根据部门层级和部门编码列表查询员工工号和职位族信息
     * @param deptLevel 部门层级（1-7），根据部门本身的层级查询对应字段
     * @param deptCodes 部门编码列表
     * @return 员工工号和职位族列表（包含job_category字段，格式：职位族-职位类-职位子类）
     */
    List<EmployeeWithCategoryVO> getEmployeesWithJobCategoryByDeptCodes(
            @Param("deptLevel") Integer deptLevel,
            @Param("deptCodes") List<String> deptCodes);

    /**
     * 根据部门层级和部门ID查询员工任职详细信息（全员类型）
     * @param deptLevel 部门层级（1-6）
     * @param deptId 部门ID（单个部门编码）
     * @param jobCategory 职位类（可选）
     * @param queryType 查询类型（1-任职人数，2-基线人数）
     * @return 员工任职详细信息列表
     */
    List<EmployeeDetailVO> getEmployeeQualifiedDetailsByDeptLevel(
            @Param("deptLevel") Integer deptLevel,
            @Param("deptId") String deptId,
            @Param("jobCategory") String jobCategory,
            @Param("queryType") Integer queryType);
}

