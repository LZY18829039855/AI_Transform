package com.huawei.aitransform.mapper;

import com.huawei.aitransform.entity.ExpertInfoVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 专家信息Mapper接口
 */
@Mapper
public interface ExpertMapper {

    /**
     * 根据部门编码和部门层级查询L2/L3专家信息（关联t_employee_sync表获取job_category）
     * @param deptCode 部门编码（单个部门ID）
     * @param deptLevel 部门层级（1-7），用于确定使用哪个部门字段进行过滤
     * @return 专家信息列表（包含工号、AI成熟度、职位类）
     */
    List<ExpertInfoVO> getExpertInfoByDeptCode(
            @Param("deptCode") String deptCode,
            @Param("deptLevel") Integer deptLevel);
}

