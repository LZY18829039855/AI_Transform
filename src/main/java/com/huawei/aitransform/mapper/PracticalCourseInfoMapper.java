package com.huawei.aitransform.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 实战课程信息表 ai_practical_course_info Mapper
 */
@Mapper
public interface PracticalCourseInfoMapper {

    /**
     * 根据课程类型（task_type）查询课程主键 ID
     *
     * @param taskType 课程类型，与 hands_on_courses.task_type 对应
     * @return 课程主键 ID，未找到返回 null
     */
    Integer selectIdByTaskType(@Param("taskType") String taskType);
}
