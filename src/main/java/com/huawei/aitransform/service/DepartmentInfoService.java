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
     * 查询云核心网产品线及其所有下属部门信息（树形结构）
     * @return 部门信息树形结构
     */
    public DepartmentInfoVO getDepartmentInfoList() {
        // 查询dept_level='2'且dept_name='云核心网产品线'的部门
        DepartmentInfoVO rootDept = departmentInfoMapper.getDepartmentByLevelAndName("2", "云核心网产品线");
        
        if (rootDept == null) {
            return null;
        }

        // 递归构建树形结构
        buildDepartmentTree(rootDept);

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

