package com.huawei.aitransform.mapper;

import com.huawei.aitransform.entity.DepartmentMaturityVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 部门成熟度Mapper接口
 */
@Mapper
public interface DepartmentMaturityMapper {

    /**
     * 批量查询部门AI成熟度信息
     * @param deptCodes 部门编码列表
     * @return 部门编码和AI成熟度的列表
     */
    List<DepartmentMaturityVO> getDepartmentMaturities(@Param("deptCodes") List<String> deptCodes);
}

