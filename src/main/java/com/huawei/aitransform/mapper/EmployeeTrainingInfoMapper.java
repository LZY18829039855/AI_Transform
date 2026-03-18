package com.huawei.aitransform.mapper;

import com.huawei.aitransform.entity.EmployeeTrainingInfoPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 全员训战课程信息表 t_employee_training_info Mapper
 */
@Mapper
public interface EmployeeTrainingInfoMapper {

    /**
     * 查询 t_employee_training_info 全量数据
     */
    List<EmployeeTrainingInfoPO> getAll();

    /**
     * 批量插入
     */
    void batchInsert(List<EmployeeTrainingInfoPO> list);

    /**
     * 批量更新
     */
    void batchUpdate(List<EmployeeTrainingInfoPO> list);

    /**
     * 批量按工号删除
     */
    void batchDeleteByEmployeeNumbers(List<String> employeeNumbers);

    /**
     * 按部门层级与部门编码查询本部门人员训战信息（不含下级）
     * @param deptLevel 部门层级 '1'~'6'
     * @param deptCode  部门编码
     * @return 该部门下人员记录列表
     */
    List<EmployeeTrainingInfoPO> listByDeptLevelAndCode(@Param("deptLevel") String deptLevel, @Param("deptCode") String deptCode);

    /**
     * 按工号查询一条训战信息（含 practical_courses）
     *
     * @param employeeNumber 工号
     * @return 训战信息，未找到返回 null
     */
    EmployeeTrainingInfoPO selectByEmployeeNumber(@Param("employeeNumber") String employeeNumber);

    /**
     * 按工号更新实战完课列表及更新时间
     *
     * @param employeeNumber  工号
     * @param practicalCourses 逗号分隔的实战课程 ID 列表
     * @return 更新行数
     */
    int updatePracticalCoursesByEmployeeNumber(@Param("employeeNumber") String employeeNumber,
                                               @Param("practicalCourses") String practicalCourses);
}
