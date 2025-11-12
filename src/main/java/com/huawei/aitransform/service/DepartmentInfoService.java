package com.huawei.aitransform.service;

import com.huawei.aitransform.entity.DepartmentInfoVO;
import com.huawei.aitransform.mapper.DepartmentInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 部门信息服务类
 */
@Service
public class DepartmentInfoService {

    @Autowired
    private DepartmentInfoMapper departmentInfoMapper;

    /**
     * 查询云核心网产品线的一级部门及其所有下属部门信息（树形结构）
     * 只返回云核心网产品线这一个2层部门及其下属部门，不包含其他2层部门
     * @return 部门信息树形结构（一级部门为根节点）
     */
    public DepartmentInfoVO getDepartmentInfoList() {
        // 查询dept_level='2'且dept_name='云核心网产品线'的部门
        DepartmentInfoVO cloudCoreDept = departmentInfoMapper.getDepartmentByLevelAndName("2", "云核心网产品线");
        
        if (cloudCoreDept == null) {
            return null;
        }

        // 如果云核心网产品线没有父部门，直接返回它本身及其下属部门
        if (cloudCoreDept.getParentDeptCode() == null || cloudCoreDept.getParentDeptCode().trim().isEmpty()) {
            // 递归构建树形结构（只包含云核心网产品线及其下属部门）
            buildDepartmentTree(cloudCoreDept);
            return cloudCoreDept;
        }

        // 查询一级部门（云核心网产品线的父部门）
        DepartmentInfoVO rootDept = departmentInfoMapper.getDepartmentByCode(cloudCoreDept.getParentDeptCode());
        
        if (rootDept == null) {
            // 如果父部门不存在，返回云核心网产品线本身及其下属部门
            buildDepartmentTree(cloudCoreDept);
            return cloudCoreDept;
        }

        // 对于一级部门，只添加云核心网产品线作为子部门（不查询其他2层部门）
        List<DepartmentInfoVO> children = new ArrayList<>();
        children.add(cloudCoreDept);
        rootDept.setChildren(children);
        
        // 递归构建云核心网产品线及其下属部门的树形结构
        buildDepartmentTree(cloudCoreDept);

        return rootDept;
    }

    /**
     * 递归构建部门树形结构
     * @param parentDept 父部门
     */
    private void buildDepartmentTree(DepartmentInfoVO parentDept) {
        if (parentDept == null || parentDept.getDeptCode() == null) {
            return;
        }

        // 查询直接子部门
        List<DepartmentInfoVO> childDepts = departmentInfoMapper.getChildDepartments(parentDept.getDeptCode());
        
        if (childDepts != null && !childDepts.isEmpty()) {
            // 设置子部门列表
            parentDept.setChildren(childDepts);
            
            // 递归构建每个子部门的树形结构
            for (DepartmentInfoVO childDept : childDepts) {
                buildDepartmentTree(childDept);
            }
        }
    }
}

