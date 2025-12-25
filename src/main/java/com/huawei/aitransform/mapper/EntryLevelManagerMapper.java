package com.huawei.aitransform.mapper;

import com.huawei.aitransform.entity.EntryLevelManager;
import com.huawei.aitransform.entity.PlTmDepartmentStatisticsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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

    /**
     * 查询获得3级及以上AI任职的员工工号列表
     * @param employeeNumbers 员工工号列表
     * @return 获得3级及以上AI任职的员工工号列表
     */
    List<String> selectQualifiedEmployeeNumbersLevel3Plus(@Param("employeeNumbers") List<String> employeeNumbers);

    /**
     * 统计研发管理部下各四级部门的PL/TM任职与认证数据
     * @param l3DepartmentCode 三级部门编码（研发管理部：030681）
     * @return 各四级部门统计数据列表
     */
    List<PlTmDepartmentStatisticsVO> selectPlTmStatisticsByL4Department(@Param("l3DepartmentCode") String l3DepartmentCode);

    /**
     * 统计研发管理部整体的PL/TM任职与认证数据
     * @param l3DepartmentCode 三级部门编码（研发管理部：030681）
     * @return 研发管理部汇总统计数据
     */
    PlTmDepartmentStatisticsVO selectPlTmStatisticsSummary(@Param("l3DepartmentCode") String l3DepartmentCode);
}

