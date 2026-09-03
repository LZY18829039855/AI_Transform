package com.huawei.aitransform.mapper;

import org.apache.ibatis.annotations.Mapper;

/**
 * 任职资格表 Mapper
 */
@Mapper
public interface QualificationsMapper {

    /**
     * 刷新 AI算法及应用 子类的初始任职方向
     *
     * @return 更新记录数
     */
    int refreshAiAlgorithmDirection();

    /**
     * 刷新 数据科学与AI工程 子类的初始任职方向
     *
     * @return 更新记录数
     */
    int refreshDataScienceDirection();
}
