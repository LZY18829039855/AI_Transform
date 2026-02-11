package com.huawei.aitransform.mapper;

import com.huawei.aitransform.entity.EmployeeTrainingInfoPO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 全员训战课程信息表 t_employee_training_info Mapper
 */
@Mapper
public interface EmployeeTrainingInfoMapper {

    /**
     * 查询 t_employee_training_info 全量数据
     */
    List<EmployeeTrainingInfoPO> getAll();

    /**
     * 批量插入
     */
    void batchInsert(List<EmployeeTrainingInfoPO> list);

    /**
     * 批量更新
     */
    void batchUpdate(List<EmployeeTrainingInfoPO> list);

    /**
     * 批量按工号删除
     */
    void batchDeleteByEmployeeNumbers(List<String> employeeNumbers);
}
