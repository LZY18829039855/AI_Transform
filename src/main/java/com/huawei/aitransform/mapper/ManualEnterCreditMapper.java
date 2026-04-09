package com.huawei.aitransform.mapper;

import com.huawei.aitransform.entity.ManualEnterCredit;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 手动录入学分 Mapper
 */
@Mapper
public interface ManualEnterCreditMapper {

    ManualEnterCredit selectById(@Param("id") Integer id);

    long countByCondition(
            @Param("employeeNumber") String employeeNumber,
            @Param("employeeName") String employeeName);

    List<ManualEnterCredit> selectPage(
            @Param("employeeNumber") String employeeNumber,
            @Param("employeeName") String employeeName,
            @Param("offset") int offset,
            @Param("limit") int limit);

    int insert(ManualEnterCredit record);

    /**
     * 批量插入（一次 SQL 多行）
     */
    int insertBatch(@Param("list") List<ManualEnterCredit> list);

    int updateById(ManualEnterCredit record);

    int deleteById(@Param("id") Integer id);
}
