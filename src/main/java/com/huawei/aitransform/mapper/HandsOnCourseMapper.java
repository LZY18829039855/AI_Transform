package com.huawei.aitransform.mapper;

import com.huawei.aitransform.entity.HandsOnCourse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 实战课程个人完课情况Mapper接口
 */
@Mapper
public interface HandsOnCourseMapper {

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
