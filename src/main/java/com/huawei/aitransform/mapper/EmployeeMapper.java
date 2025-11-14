package com.huawei.aitransform.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 员工信息Mapper接口
 */
@Mapper
public interface EmployeeMapper {

    /**
     * 根据部门层级和部门ID列表查询员工工号列表
     * @param deptLevel 部门层级（1-7）
     * @param deptIds 部门ID列表
     * @return 员工工号列表（非离职人员，expired_date为空）
     */
    List<String> getEmployeeNumbersByDeptLevel(
            @Param("deptLevel") Integer deptLevel,
            @Param("deptIds") List<String> deptIds);
}

