package com.huawei.aitransform.mapper;

import com.huawei.aitransform.entity.SchoolCreditDetailRequestVO;
import com.huawei.aitransform.entity.SchoolCreditDetailVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * AI School学分数据明细查询Mapper接口
 */
@Mapper
public interface SchoolCreditDetailMapper {

    /**
     * 查询AI School学分数据明细列表
     *
     * @param request 查询条件
     * @param offset  分页偏移量
     * @return 学分数据明细列表
     */
    List<SchoolCreditDetailVO> getCreditDetailList(@Param("request") SchoolCreditDetailRequestVO request,
                                                    @Param("offset") int offset);

    /**
     * 查询AI School学分数据明细总数
     *
     * @param request 查询条件
     * @return 总记录数
     */
    Long getCreditDetailCount(@Param("request") SchoolCreditDetailRequestVO request);
}
