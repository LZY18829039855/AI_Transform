package com.huawei.aitransform.service.impl;

import com.huawei.aitransform.entity.CourseInfoByLevelVO;
import com.huawei.aitransform.entity.DeptCourseSelection;
import com.huawei.aitransform.entity.EmployeeSyncDataVO;
import com.huawei.aitransform.entity.EmployeeTrainingInfoPO;
import com.huawei.aitransform.mapper.CoursePlanningInfoMapper;
import com.huawei.aitransform.mapper.EmployeeMapper;
import com.huawei.aitransform.mapper.EmployeeTrainingInfoMapper;
import com.huawei.aitransform.mapper.PersonalCourseCompletionMapper;
import com.huawei.aitransform.service.EmployeeTrainingInfoSyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 全体员工训战信息同步服务实现
 * 参考文档 api-sync-employee-data-full.md
 */
@Service
public class EmployeeTrainingInfoSyncServiceImpl implements EmployeeTrainingInfoSyncService {

    private static final int BATCH_SIZE = 1000;
    private static final DateTimeFormatter UPDATED_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private EmployeeMapper employeeMapper;
    @Autowired
    private EmployeeTrainingInfoMapper employeeTrainingInfoMapper;
    @Autowired
    private CoursePlanningInfoMapper coursePlanningInfoMapper;
    @Autowired
    private PersonalCourseCompletionMapper personalCourseCompletionMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> syncEmployeeTrainingInfo(String periodId) {
        if (periodId == null || periodId.trim().isEmpty()) {
            throw new IllegalArgumentException("Period ID cannot be empty");
        }

        // 1. 从 t_employee_sync 查询该 periodId 下成员工号及基本信息（本次同步的基本信息）
        List<EmployeeSyncDataVO> basicInfoList = employeeMapper.getEmployeeSyncBasicInfoByPeriodId(periodId);
        if (basicInfoList == null || basicInfoList.isEmpty()) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "No employees in scope for period " + periodId);
            result.put("periodId", periodId);
            result.put("totalSource", 0);
            result.put("insertCount", 0);
            result.put("updateCount", 0);
            result.put("deleteCount", 0);
            result.put("ignoreCount", 0);
            return result;
        }

        String updatedTime = LocalDateTime.now().format(UPDATED_TIME_FORMAT);

        // 2. 对每条员工数据计算训战课程字段并构建源列表
        List<EmployeeTrainingInfoPO> sourceList = new ArrayList<>();
        for (EmployeeSyncDataVO emp : basicInfoList) {
            EmployeeTrainingInfoPO po = buildTrainingInfoPO(emp, periodId, updatedTime);
            fillTrainingCourseFields(emp.getEmployeeNumber(), emp.getFourthdeptcode(), po);
            sourceList.add(po);
        }

        // 3. 从 t_employee_training_info 查询全量目标
        List<EmployeeTrainingInfoPO> targetList = employeeTrainingInfoMapper.getAll();
        Map<String, EmployeeTrainingInfoPO> targetMap = targetList.stream()
                .collect(Collectors.toMap(EmployeeTrainingInfoPO::getEmployeeNumber, Function.identity(), (a, b) -> a));

        int insertCount = 0;
        int updateCount = 0;
        int deleteCount = 0;
        int ignoreCount = 0;
        List<EmployeeTrainingInfoPO> insertList = new ArrayList<>();
        List<EmployeeTrainingInfoPO> updateList = new ArrayList<>();
        List<String> deleteList = new ArrayList<>();

        // 4. 全量对比
        for (EmployeeTrainingInfoPO source : sourceList) {
            EmployeeTrainingInfoPO target = targetMap.get(source.getEmployeeNumber());
            if (target == null) {
                insertList.add(source);
            } else {
                if (isDifferent(source, target)) {
                    updateList.add(source);
                } else {
                    ignoreCount++;
                }
                targetMap.remove(source.getEmployeeNumber());
            }
        }
        deleteList.addAll(targetMap.keySet());

        // 5. 批量执行
        for (int i = 0; i < insertList.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, insertList.size());
            employeeTrainingInfoMapper.batchInsert(insertList.subList(i, end));
        }
        insertCount = insertList.size();

        for (int i = 0; i < updateList.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, updateList.size());
            employeeTrainingInfoMapper.batchUpdate(updateList.subList(i, end));
        }
        updateCount = updateList.size();

        for (int i = 0; i < deleteList.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, deleteList.size());
            employeeTrainingInfoMapper.batchDeleteByEmployeeNumbers(deleteList.subList(i, end));
        }
        deleteCount = deleteList.size();

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Sync completed successfully");
        result.put("periodId", periodId);
        result.put("totalSource", sourceList.size());
        result.put("insertCount", insertCount);
        result.put("updateCount", updateCount);
        result.put("deleteCount", deleteCount);
        result.put("ignoreCount", ignoreCount);
        return result;
    }

    private EmployeeTrainingInfoPO buildTrainingInfoPO(EmployeeSyncDataVO emp, String periodId, String updatedTime) {
        EmployeeTrainingInfoPO po = new EmployeeTrainingInfoPO();
        po.setEmployeeNumber(emp.getEmployeeNumber());
        po.setLastName(emp.getLastName());
        po.setFirstdeptcode(emp.getFirstdeptcode());
        po.setSeconddeptcode(emp.getSeconddeptcode());
        po.setThirddeptcode(emp.getThirddeptcode());
        po.setFourthdeptcode(emp.getFourthdeptcode());
        po.setFifthdeptcode(emp.getFifthdeptcode());
        po.setSixthdeptcode(emp.getSixthdeptcode());
        po.setLowestdeptid(emp.getLowestDeptNumber());
        po.setFirstdept(emp.getFirstdept());
        po.setSeconddept(emp.getSeconddept());
        po.setThirddept(emp.getThirddept());
        po.setFourthdept(emp.getFourthdept());
        po.setFifthdept(emp.getFifthdept());
        po.setSixthdept(emp.getSixthdept());
        po.setLowestdept(emp.getLowestDept());
        po.setJobType(emp.getJobType());
        po.setJobCategory(emp.getJobCategory());
        po.setJobSubcategory(emp.getJobSubcategory());
        po.setPeriodId(periodId);
        po.setUpdatedTime(updatedTime);
        return po;
    }

    /**
     * 按文档 4.6：四级部门目标课程 + 完课判断，填充 basicCourses、advancedCourses、practicalCourses
     * 四级部门从本次同步的基本信息（emp 的 fourthdeptcode）获取
     */
    private void fillTrainingCourseFields(String empNum, String fourthDeptCode, EmployeeTrainingInfoPO po) {
        List<CourseInfoByLevelVO> targetCourses = getTargetCoursesByFourthDept(fourthDeptCode);
        List<String> targetCourseNumbers = targetCourses.stream()
                .map(CourseInfoByLevelVO::getCourseNumber)
                .distinct()
                .collect(Collectors.toList());

        List<String> completedCourseNumbers = new ArrayList<>();
        if (!targetCourseNumbers.isEmpty()) {
            completedCourseNumbers = personalCourseCompletionMapper.getCompletedCourseNumbers(empNum, targetCourseNumbers);
        }
        Map<String, Boolean> completedMap = new HashMap<>();
        for (String cn : completedCourseNumbers) {
            completedMap.put(cn, true);
        }

        Map<String, List<CourseInfoByLevelVO>> byLevel = targetCourses.stream()
                .collect(Collectors.groupingBy(CourseInfoByLevelVO::getCourseLevel));

        po.setBasicCourses(joinCompletedByLevel(byLevel.getOrDefault("基础", new ArrayList<>()), completedMap));
        po.setAdvancedCourses(joinCompletedByLevel(byLevel.getOrDefault("进阶", new ArrayList<>()), completedMap));
        po.setPracticalCourses(joinCompletedByLevel(byLevel.getOrDefault("实战", new ArrayList<>()), completedMap));
    }

    private List<CourseInfoByLevelVO> getTargetCoursesByFourthDept(String fourthDeptCode) {
        boolean useAllCourses = true;
        List<Integer> targetCourseIds = new ArrayList<>();
        if (fourthDeptCode != null && !fourthDeptCode.trim().isEmpty()) {
            DeptCourseSelection selection = coursePlanningInfoMapper.getDeptSelectionByDeptCode(fourthDeptCode);
            if (selection != null && selection.getCourseSelections() != null && !selection.getCourseSelections().trim().isEmpty()) {
                String[] parts = selection.getCourseSelections().split(",");
                for (String s : parts) {
                    s = s.trim();
                    if (!s.isEmpty()) {
                        try {
                            targetCourseIds.add(Integer.parseInt(s));
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
                if (!targetCourseIds.isEmpty()) {
                    useAllCourses = false;
                }
            }
        }
        if (useAllCourses) {
            return personalCourseCompletionMapper.getCourseInfoByLevel();
        } else {
            return personalCourseCompletionMapper.getCourseInfoByLevelAndIds(targetCourseIds);
        }
    }

    /**
     * 按级别拼接已完课课程的主键 ID（basicCourses/advancedCourses/practicalCourses 存课程主键 ID）。
     * ID 按升序排序后再拼接，便于后续数据对比。
     */
    private String joinCompletedByLevel(List<CourseInfoByLevelVO> coursesInLevel, Map<String, Boolean> completedMap) {
        List<Integer> completedIds = new ArrayList<>();
        for (CourseInfoByLevelVO c : coursesInLevel) {
            if (c.getId() != null && c.getCourseNumber() != null && Boolean.TRUE.equals(completedMap.get(c.getCourseNumber()))) {
                completedIds.add(c.getId());
            }
        }
        if (completedIds.isEmpty()) {
            return null;
        }
        completedIds.sort(Integer::compareTo);
        return completedIds.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    private boolean isDifferent(EmployeeTrainingInfoPO source, EmployeeTrainingInfoPO target) {
        if (!Objects.equals(source.getLastName(), target.getLastName())) {
            return true;
        }
        if (!Objects.equals(source.getFirstdeptcode(), target.getFirstdeptcode())) {
            return true;
        }
        if (!Objects.equals(source.getSeconddeptcode(), target.getSeconddeptcode())) {
            return true;
        }
        if (!Objects.equals(source.getThirddeptcode(), target.getThirddeptcode())) {
            return true;
        }
        if (!Objects.equals(source.getFourthdeptcode(), target.getFourthdeptcode())) {
            return true;
        }
        if (!Objects.equals(source.getFifthdeptcode(), target.getFifthdeptcode())) {
            return true;
        }
        if (!Objects.equals(source.getSixthdeptcode(), target.getSixthdeptcode())) {
            return true;
        }
        if (!Objects.equals(source.getLowestdeptid(), target.getLowestdeptid())) {
            return true;
        }
        if (!Objects.equals(source.getFirstdept(), target.getFirstdept())) {
            return true;
        }
        if (!Objects.equals(source.getSeconddept(), target.getSeconddept())) {
            return true;
        }
        if (!Objects.equals(source.getThirddept(), target.getThirddept())) {
            return true;
        }
        if (!Objects.equals(source.getFourthdept(), target.getFourthdept())) {
            return true;
        }
        if (!Objects.equals(source.getFifthdept(), target.getFifthdept())) {
            return true;
        }
        if (!Objects.equals(source.getSixthdept(), target.getSixthdept())) {
            return true;
        }
        if (!Objects.equals(source.getLowestdept(), target.getLowestdept())) {
            return true;
        }
        if (!Objects.equals(source.getJobType(), target.getJobType())) {
            return true;
        }
        if (!Objects.equals(source.getJobCategory(), target.getJobCategory())) {
            return true;
        }
        if (!Objects.equals(source.getJobSubcategory(), target.getJobSubcategory())) {
            return true;
        }
        if (!Objects.equals(source.getBasicCourses(), target.getBasicCourses())) {
            return true;
        }
        if (!Objects.equals(source.getAdvancedCourses(), target.getAdvancedCourses())) {
            return true;
        }
        if (!Objects.equals(source.getPracticalCourses(), target.getPracticalCourses())) {
            return true;
        }
        return false;
    }
}
