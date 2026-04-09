package com.huawei.aitransform.mapper;

import com.huawei.aitransform.entity.CourseInfoByLevelVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * 个人课程完成情况Mapper接口
 */
@Mapper
public interface PersonalCourseCompletionMapper {

    /**
     * 按课程级别分类查询所有课程信息
     * @return 课程信息列表
     */
    List<CourseInfoByLevelVO> getCourseInfoByLevel();

    /**
     * 查询用户已完成的课程编码列表
     * @param empNum 员工工号（不带首字母）
     * @param courseNumbers 课程编码列表
     * @return 已完成的课程编码列表
     */
    List<String> getCompletedCourseNumbers(@Param("empNum") String empNum, 
                                           @Param("courseNumbers") List<String> courseNumbers);

    /**
     * 根据员工工号查询中文名（last_name）
     * @param employeeNumber 员工工号
     * @return 中文名
     */
    String getLastNameByEmployeeNumber(@Param("employeeNumber") String employeeNumber);

    /**
     * 根据员工工号查询四级部门ID（fourthdeptcode）
     * @param employeeNumber 员工工号（不带首字母）
     * @return 四级部门ID，如果未找到返回null
     */
    String getFourthDeptCodeByEmployeeNumber(@Param("employeeNumber") String employeeNumber);

    /**
     * 根据课程ID列表查询课程信息（按级别分类）
     * @param courseIds 课程ID列表
     * @return 课程信息列表
     */
    List<CourseInfoByLevelVO> getCourseInfoByLevelAndIds(@Param("courseIds") List<Integer> courseIds);

    /**
     * 获取最新周期ID
     * @return 最新周期ID
     */
    Integer getLatestPeriodId();

    /**
     * 根据周期ID查询员工列表
     * @param periodId 周期ID
     * @return 员工列表
     */
    List<com.huawei.aitransform.entity.EmployeeSyncDataVO> getEmployeesByPeriodId(@Param("periodId") Integer periodId);

    /**
     * 根据周期ID与工号集合查询员工列表（用于增量刷新个人学分）
     *
     * @param periodId 周期ID
     * @param employeeNumbers 工号集合（不带首字母），非空
     * @return 员工列表
     */
    List<com.huawei.aitransform.entity.EmployeeSyncDataVO> getEmployeesByPeriodIdAndEmployeeNumbers(
            @Param("periodId") Integer periodId,
            @Param("employeeNumbers") Set<String> employeeNumbers);
}

