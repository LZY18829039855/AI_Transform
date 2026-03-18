package com.huawei.aitransform.mapper;

import com.huawei.aitransform.entity.PracticalCourseInfoVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 实战课程主数据 Mapper（表 ai_practical_course_info）
 */
@Mapper
public interface PracticalCourseMapper {

    /**
     * 根据课程 ID 列表查询实战课程（id、task_type、task_name）
     *
     * @param ids 课程 ID 列表
     * @return 实战课程列表
     */
    List<PracticalCourseInfoVO> listByIds(@Param("ids") List<Integer> ids);

    /**
     * 查询全部实战课程（有效课程，当前表无状态字段则查全部）
     *
     * @return 实战课程列表
     */
    List<PracticalCourseInfoVO> listAll();
}
