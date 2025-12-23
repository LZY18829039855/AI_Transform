package com.huawei.aitransform.service;

import com.huawei.aitransform.entity.CoursePlanningInfoVO;
import com.huawei.aitransform.mapper.CoursePlanningInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * AI课程规划明细表服务类
 */
@Service
public class CoursePlanningInfoService {

    @Autowired
    private CoursePlanningInfoMapper coursePlanningInfoMapper;

    /**
     * 查询所有课程规划明细数据
     * @return 课程规划明细列表
     */
    public List<CoursePlanningInfoVO> getAllCoursePlanningInfo() {
        return coursePlanningInfoMapper.getAllCoursePlanningInfo();
    }
}

