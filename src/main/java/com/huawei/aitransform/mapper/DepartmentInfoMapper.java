package com.huawei.aitransform.mapper;

import com.huawei.aitransform.entity.DepartmentInfoVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 部门信息Mapper接口
 */
@Mapper
public interface DepartmentInfoMapper {

    /**
     * 根据父部门编码查询所有子部门
     * @param parentDeptCode 父部门编码
     * @return 子部门列表（只包含部门ID和中文名称）
     */
    List<DepartmentInfoVO> getChildDepartments(@Param("parentDeptCode") String parentDeptCode);
}

