package com.huawei.aitransform.mapper;

import com.huawei.aitransform.entity.EntryLevelManager;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 基层主管数据Mapper接口
 */
@Mapper
public interface EntryLevelManagerMapper {

    /**
     * 查询所有状态为有效的PL和TM人员
     * @return PL和TM人员列表
     */
    List<EntryLevelManager> selectValidPlAndTm();

    /**
     * 查询所有任期结束的PL和TM
     * @return 任期结束的PL和TM人员列表
     */
    List<EntryLevelManager> selectExpiredPlAndTm();

    /**
     * 根据employee_appointment_id查询记录
     * @param employeeAppointmentId 主键ID
     * @return 记录
     */
    EntryLevelManager selectByEmployeeAppointmentId(Long employeeAppointmentId);

    /**
     * 根据employee_number查询记录
     * @param employeeNumber 员工工号
     * @return 记录
     */
    EntryLevelManager selectByEmployeeNumber(String employeeNumber);

    /**
     * 插入记录
     * @param entryLevelManager 基层主管数据
     * @return 影响行数
     */
    int insert(EntryLevelManager entryLevelManager);

    /**
     * 更新记录（根据employee_appointment_id）
     * @param entryLevelManager 基层主管数据
     * @return 影响行数
     */
    int update(EntryLevelManager entryLevelManager);

    /**
     * 根据employee_number更新记录
     * @param entryLevelManager 基层主管数据
     * @return 影响行数
     */
    int updateByEmployeeNumber(EntryLevelManager entryLevelManager);

    /**
     * 批量插入或更新（使用ON DUPLICATE KEY UPDATE）
     * @param list 基层主管数据列表
     * @return 影响行数
     */
    int batchInsertOrUpdate(List<EntryLevelManager> list);

    /**
     * 查询目标表中所有的employee_number
     * @return 员工工号列表
     */
    List<String> selectAllEmployeeNumbers();

    /**
     * 根据employee_number删除记录
     * @param employeeNumber 员工工号
     * @return 影响行数
     */
    int deleteByEmployeeNumber(String employeeNumber);

    /**
     * 批量根据employee_number删除记录
     * @param employeeNumbers 员工工号列表
     * @return 影响行数
     */
    int batchDeleteByEmployeeNumbers(List<String> employeeNumbers);
}

