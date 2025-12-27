package com.huawei.aitransform.service;

import com.huawei.aitransform.entity.CadreAiCertOverviewResponseVO;
import com.huawei.aitransform.entity.CadrePositionOverviewResponseVO;

/**
 * 干部岗位概述统计Service接口
 */
public interface CadrePositionOverviewService {

    /**
     * 获取干部岗位概述统计数据
     *
     * @return 统计结果
     */
    CadrePositionOverviewResponseVO getCadrePositionOverview();

    /**
     * 获取干部AI任职认证统计数据
     *
     * @return 统计结果
     */
    CadreAiCertOverviewResponseVO getCadreAiCertificationOverview();
}

