package com.huawei.aitransform.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 干部信息Mapper接口
 */
@Mapper
public interface CadreMapper {

    /**
     * 根据部门编码列表查询干部工号列表
     * @param deptCodes 部门编码列表
     * @return 干部工号列表
     */
    List<String> getCadreEmployeeNumbersByDeptCodes(@Param("deptCodes") List<String> deptCodes);
}

