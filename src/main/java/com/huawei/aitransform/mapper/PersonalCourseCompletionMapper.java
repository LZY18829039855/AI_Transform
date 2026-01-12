package com.huawei.aitransform.mapper;

import com.huawei.aitransform.entity.CourseInfoByLevelVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

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
}

