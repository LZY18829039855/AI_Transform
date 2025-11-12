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
     * 根据部门层级和部门名称查询部门信息
     * @param deptLevel 部门层级
     * @param deptName 部门名称
     * @return 部门信息
     */
    DepartmentInfoVO getDepartmentByLevelAndName(@Param("deptLevel") String deptLevel, @Param("deptName") String deptName);

    /**
     * 根据父部门编码查询所有子部门
     * @param parentDeptCode 父部门编码
     * @return 子部门列表
     */
    List<DepartmentInfoVO> getChildDepartments(@Param("parentDeptCode") String parentDeptCode);
}

