package com.huawei.aitransform.mapper;

import com.huawei.aitransform.entity.AiCertStatisticsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * AI认证统计Mapper接口
 */
@Mapper
public interface AiCertStatisticsMapper {

    /**
     * 根据部门层级获取部门编码列表
     * @param deptLevel 部门层级（2-4）
     * @return 部门编码列表
     */
    List<String> getDeptCodesByLevel(@Param("deptLevel") Integer deptLevel);

    /**
     * 统计各个部门的总人数
     * @param deptLevel 部门层级
     * @return 统计结果
     */
    List<AiCertStatisticsVO> countTotalEmployeesByDept(@Param("deptLevel") Integer deptLevel);

    /**
     * 统计各个部门持有AI证书的人数
     * @param deptLevel 部门层级
     * @return 统计结果
     */
    List<AiCertStatisticsVO> countCertEmployeesByDept(@Param("deptLevel") Integer deptLevel);
}

