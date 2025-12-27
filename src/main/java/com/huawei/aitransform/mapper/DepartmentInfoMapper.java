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

    /**
     * 根据部门编码查询部门信息
     * @param deptCode 部门编码
     * @return 部门信息
     */
    DepartmentInfoVO getDepartmentByCode(@Param("deptCode") String deptCode);

    /**
     * 查询所有四级部门
     * @return 四级部门列表
     */
    List<DepartmentInfoVO> getAllLevel4Departments();

    /**
     * 查询指定2级部门下的所有四级部门
     * @param level2DeptCode 2级部门编码
     * @return 四级部门列表
     */
    List<DepartmentInfoVO> getLevel4DepartmentsUnderLevel2(@Param("level2DeptCode") String level2DeptCode);

    /**
     * 递归查询指定部门下的所有六级部门
     * @param deptCode 部门编码
     * @return 六级部门列表
     */
    List<DepartmentInfoVO> getAllLevel6DepartmentsUnderDept(@Param("deptCode") String deptCode);

    /**
     * 递归查询指定部门下的所有子部门（包括所有层级的子部门）
     * @param deptCode 部门编码
     * @return 所有子部门列表
     */
    List<DepartmentInfoVO> getAllSubDepartments(@Param("deptCode") String deptCode);

    /**
     * 查询指定父部门下的所有三级部门
     * @param parentDeptCode 父部门编码
     * @return 三级部门列表
     */
    List<DepartmentInfoVO> getLevel3DepartmentsUnderParent(@Param("parentDeptCode") String parentDeptCode);

    /**
     * 查询指定父部门下的所有四级部门
     * @param parentDeptCode 父部门编码
     * @return 四级部门列表
     */
    List<DepartmentInfoVO> getLevel4DepartmentsUnderParent(@Param("parentDeptCode") String parentDeptCode);

}

