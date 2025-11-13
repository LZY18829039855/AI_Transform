package com.huawei.aitransform.service;

import com.huawei.aitransform.entity.DepartmentInfoVO;
import com.huawei.aitransform.mapper.DepartmentInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 部门信息服务类
 */
@Service
public class DepartmentInfoService {

    @Autowired
    private DepartmentInfoMapper departmentInfoMapper;

    /**
     * 根据部门ID查询下一级部门信息
     * @param deptId 部门ID（部门编码）
     * @return 下一级部门列表（只包含部门ID和中文名称）
     */
    public List<DepartmentInfoVO> getChildDepartments(String deptId) {
        return departmentInfoMapper.getChildDepartments(deptId);
    }
}

