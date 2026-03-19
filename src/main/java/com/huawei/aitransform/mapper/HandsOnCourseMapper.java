package com.huawei.aitransform.mapper;

import com.huawei.aitransform.entity.HandsOnCourse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 实战课程个人完课情况Mapper接口
 */
@Mapper
public interface HandsOnCourseMapper {

    /**
     * 查询指定工号在 hands_on_courses 中 task_status='finished' 的完课对应的实战课程 ID 列表
     * （联表 ai_course_planning_info：hands_on_courses.task_type = ai_course_planning_info.syb_type，取 course_level='实战' 的课程 id）
     *
     * @param account 工号
     * @return 已完课实战课程主键 ID 列表
     */
    List<Integer> selectCompletedPracticalCourseIdsByAccount(@Param("account") String account);

    /**
     * 根据工号和课程类型查询记录
     * @param account 工号
     * @param taskType 课程类型
     * @return 记录
     */
    HandsOnCourse selectByAccountAndTaskType(@Param("account") String account, @Param("taskType") String taskType);

    /**
     * 插入记录
     * @param handsOnCourse 实战课程数据
     * @return 影响行数
     */
    int insert(HandsOnCourse handsOnCourse);

    /**
     * 更新记录（根据工号和课程类型）
     * @param handsOnCourse 实战课程数据
     * @return 影响行数
     */
    int updateByAccountAndTaskType(HandsOnCourse handsOnCourse);
}
