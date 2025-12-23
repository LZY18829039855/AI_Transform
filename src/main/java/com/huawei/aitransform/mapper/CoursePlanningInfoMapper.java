package com.huawei.aitransform.mapper;

import com.huawei.aitransform.entity.CoursePlanningInfoVO;
import org.apache.ibatis.annotations.Mapper;

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
}

