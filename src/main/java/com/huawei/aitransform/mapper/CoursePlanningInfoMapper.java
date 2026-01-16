package com.huawei.aitransform.mapper;

import com.huawei.aitransform.entity.CoursePlanningInfoVO;
import com.huawei.aitransform.entity.DeptCourseSelection;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * AI课程规划明细表Mapper接口
 */
@Mapper
public interface CoursePlanningInfoMapper {

    /**
     * 查询所有课程规划明细数据
     * @return 课程规划明细列表
     */
    List<CoursePlanningInfoVO> getAllCoursePlanningInfo();

    /**
     * 查询所有部门选课信息
     * @return 部门选课列表
     */
    List<DeptCourseSelection> getAllDeptSelections();

    /**
     * 根据部门编码查询部门选课信息
     * @param deptCode 部门编码
     * @return 部门选课信息，如果未找到返回null
     */
    DeptCourseSelection getDeptSelectionByDeptCode(@Param("deptCode") String deptCode);
}


