package com.huawei.aitransform.service;

import com.huawei.aitransform.entity.SchoolCreditDetailRequestVO;
import com.huawei.aitransform.entity.SchoolCreditDetailResponseVO;

/**
 * AI School学分数据明细查询Service接口
 */
public interface SchoolCreditDetailService {

    /**
     * 查询AI School学分数据明细
     *
     * @param request 查询请求参数
     * @return 学分数据明细列表（分页）
     */
    SchoolCreditDetailResponseVO getCreditDetailList(SchoolCreditDetailRequestVO request);
}
