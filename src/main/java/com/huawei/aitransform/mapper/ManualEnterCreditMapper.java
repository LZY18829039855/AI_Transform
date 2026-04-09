package com.huawei.aitransform.mapper;

import com.huawei.aitransform.entity.ManualEnterCredit;
import com.huawei.aitransform.entity.PersonalCreditNameRow;
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

    /**
     * 从全员学分信息表 t_personal_credit 按工号批量查询 last_name（用于工号校验与姓名回填）。
     *
     * @param numbers 非空、已 trim 的工号列表（调用方保证非空以避免 IN ()）
     */
    List<PersonalCreditNameRow> selectPersonalCreditNamesByEmployeeNumbers(@Param("numbers") List<String> numbers);
}
