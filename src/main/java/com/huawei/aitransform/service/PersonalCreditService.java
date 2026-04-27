package com.huawei.aitransform.service;

import com.huawei.aitransform.entity.CoursePlanningInfoVO;
import com.huawei.aitransform.entity.CreditOverviewVO;
import com.huawei.aitransform.entity.CreditStatisticsResponseVO;
import com.huawei.aitransform.entity.DeptCourseSelection;
import com.huawei.aitransform.entity.DepartmentInfoVO;
import com.huawei.aitransform.entity.EmployeeCreditRow;
import com.huawei.aitransform.entity.EmployeeSyncDataVO;
import com.huawei.aitransform.entity.ManualCreditSumRow;
import com.huawei.aitransform.entity.PersonalCredit;
import com.huawei.aitransform.mapper.HandsOnCourseMapper;
import com.huawei.aitransform.mapper.ManualEnterCreditMapper;
import com.huawei.aitransform.mapper.CoursePlanningInfoMapper;
import com.huawei.aitransform.mapper.PersonalCourseCompletionMapper;
import com.huawei.aitransform.mapper.PersonalCreditMapper;
import com.huawei.aitransform.entity.SchoolCreditDetailRequestVO;
import com.huawei.aitransform.entity.SchoolCreditDetailResponseVO;
import com.huawei.aitransform.entity.SchoolCreditDetailVO;
import com.huawei.aitransform.entity.SchoolRoleSummaryResponseVO;
import com.huawei.aitransform.entity.SchoolRoleSummaryVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 个人学分服务类
 */
@Service
public class PersonalCreditService {

    private static final Logger logger = LoggerFactory.getLogger(PersonalCreditService.class);

    @Autowired
    private PersonalCreditMapper personalCreditMapper;

    @Autowired
    private PersonalCourseCompletionMapper personalCourseCompletionMapper;

    @Autowired
    private CoursePlanningInfoMapper coursePlanningInfoMapper;

    @Autowired
    private ManualEnterCreditMapper manualEnterCreditMapper;

    @Autowired
    private HandsOnCourseMapper handsOnCourseMapper;

    @Autowired
    private DepartmentInfoService departmentService;

    private static final Set<String> TARGET_COURSE_LEVELS =
            new HashSet<>(Arrays.asList("基础", "进阶", "实战"));

    /**
     * 根据工号获取个人学分概览
     * @param employeeNumber 工号
     * @return 个人学分信息
     */
    public PersonalCredit getPersonalCreditOverview(String employeeNumber) {
        return personalCreditMapper.getByEmployeeNumber(employeeNumber);
    }

    /**
     * 同步计算所有用户的个人学分
     */
    @Transactional(rollbackFor = Exception.class)
    public void syncAllPersonalCredits() {
        logger.info("Start syncing personal credits...");

        // 1. 获取最新周期ID
        Integer latestPeriodId = personalCourseCompletionMapper.getLatestPeriodId();
        if (latestPeriodId == null) {
            logger.warn("No period_id found in t_employee_sync.");
            return;
        }

        // 2. 获取该周期的所有员工
        List<EmployeeSyncDataVO> employees = personalCourseCompletionMapper.getEmployeesByPeriodId(latestPeriodId);
        if (employees == null || employees.isEmpty()) {
            logger.info("No employees found for period_id: {}", latestPeriodId);
            return;
        }

        // 3. 预加载课程信息和部门选课信息
        // 3.1 所有课程信息 (Map: ID -> Credit)
        List<CoursePlanningInfoVO> allCoursesRaw = coursePlanningInfoMapper.getAllCoursePlanningInfo();
        // 过滤无效课程，保持与PersonalCourseCompletionMapper.getCourseInfoByLevel一致的逻辑
        // WHERE course_level IS NOT NULL AND course_name IS NOT NULL AND course_number IS NOT NULL
        List<CoursePlanningInfoVO> allCourses = allCoursesRaw.stream()
                .filter(c -> c.getCourseLevel() != null && c.getCourseName() != null && c.getCourseNumber() != null)
                .filter(c -> TARGET_COURSE_LEVELS.contains(c.getCourseLevel()))
                .collect(Collectors.toList());

        Map<Integer, BigDecimal> courseCreditMap = new HashMap<>();
        Map<Integer, CoursePlanningInfoVO> courseById = new HashMap<>();

        for (CoursePlanningInfoVO course : allCourses) {
            BigDecimal credit = BigDecimal.ZERO;
            try {
                if (course.getCredit() != null && !course.getCredit().isEmpty()) {
                    credit = new BigDecimal(course.getCredit());
                }
            } catch (Exception e) {
                logger.warn("Invalid credit format for course {}: {}", course.getId(), course.getCredit());
            }
            if (course.getId() != null) {
                courseCreditMap.put(course.getId(), credit);
                courseById.put(course.getId(), course);
            }
        }

        // 3.2 所有部门选课信息 (Map: DeptCode -> Selection)
        List<DeptCourseSelection> allDeptSelections = coursePlanningInfoMapper.getAllDeptSelections();
        Map<String, DeptCourseSelection> deptSelectionMap = new HashMap<>();
        for (DeptCourseSelection selection : allDeptSelections) {
            if (selection != null && selection.getDeptCode() != null && !selection.getDeptCode().trim().isEmpty()) {
                deptSelectionMap.put(selection.getDeptCode().trim(), selection);
            }
        }

        // 4. 遍历员工计算学分
        List<String> employeeNumbers = employees.stream()
                .map(EmployeeSyncDataVO::getEmployeeNumber)
                .collect(Collectors.toList());

        // 4.1 批量查询手工录入学分汇总（employee_number -> SUM(credits)）
        Map<String, BigDecimal> manualCreditSumMap = new HashMap<>();
        if (!employeeNumbers.isEmpty()) {
            int batchSize = 1000;
            for (int i = 0; i < employeeNumbers.size(); i += batchSize) {
                int end = Math.min(i + batchSize, employeeNumbers.size());
                List<String> subList = employeeNumbers.subList(i, end);
                List<ManualCreditSumRow> rows = manualEnterCreditMapper.sumCreditsByEmployeeNumbers(subList);
                if (rows == null || rows.isEmpty()) {
                    continue;
                }
                for (ManualCreditSumRow row : rows) {
                    if (row == null || row.getEmployeeNumber() == null) {
                        continue;
                    }
                    manualCreditSumMap.put(row.getEmployeeNumber(),
                            row.getTotalCredits() != null ? row.getTotalCredits() : BigDecimal.ZERO);
                }
            }
        }

        // 批量查询现有记录
        Map<String, PersonalCredit> existingCreditMap = new HashMap<>();
        if (!employeeNumbers.isEmpty()) {
            int batchSize = 1000;
            for (int i = 0; i < employeeNumbers.size(); i += batchSize) {
                int end = Math.min(i + batchSize, employeeNumbers.size());
                List<String> subList = employeeNumbers.subList(i, end);
                List<PersonalCredit> existingList = personalCreditMapper.getByEmployeeNumbers(subList);
                for (PersonalCredit pc : existingList) {
                    existingCreditMap.put(pc.getEmployeeNumber(), pc);
                }
            }
        }

        // 4.2 批量查询 AI 认证学分（工号 -> 认证学分，自然上限 15）
        Map<String, BigDecimal> certCreditMap = loadCreditMap(
                personalCreditMapper::getAiCertCreditsByEmployeeNumbers, employeeNumbers);
        // 4.3 批量查询 AI 任职学分（工号 -> 任职学分，自然上限 25）
        Map<String, BigDecimal> qualCreditMap = loadCreditMap(
                personalCreditMapper::getAiQualificationCreditsByEmployeeNumbers, employeeNumbers);

        List<PersonalCredit> toSaveList = new ArrayList<>();
        for (EmployeeSyncDataVO employee : employees) {
            PersonalCredit credit = calculateEmployeeCredit(
                    employee,
                    courseCreditMap,
                    deptSelectionMap,
                    allCourses,
                    courseById,
                    existingCreditMap,
                    manualCreditSumMap,
                    certCreditMap,
                    qualCreditMap
            );
            if (credit != null) {
                toSaveList.add(credit);
            }
        }

        // 批量保存
        if (!toSaveList.isEmpty()) {
            int batchSize = 1000;
            for (int i = 0; i < toSaveList.size(); i += batchSize) {
                int end = Math.min(i + batchSize, toSaveList.size());
                personalCreditMapper.batchInsertOrUpdate(toSaveList.subList(i, end));
            }
        }
        List<String> incomingEmployeeNumbers = toSaveList.stream()
                .map(PersonalCredit::getEmployeeNumber)
                .collect(Collectors.toList());

        // 删除不在传入列表中的记录
        personalCreditMapper.deleteNotInEmployeeNumbers(incomingEmployeeNumbers);

        // 5. 计算并更新部门标杆
        updateDeptBenchmarks();

        logger.info("Finished syncing personal credits for {} employees.", employees.size());
    }

    /**
     * 增量同步：仅重算并更新指定工号的个人学分，并刷新相关部门标杆。
     * <p>
     * 规则：基于 t_employee_sync 最新周期（period_id=MAX），查询指定工号的人员信息；
     * 若任一工号在最新周期不存在，则抛异常并回滚外层事务。
     */
    @Transactional(rollbackFor = Exception.class)
    public void syncPersonalCreditsForEmployees(Set<String> employeeNumbers) {
        if (employeeNumbers == null || employeeNumbers.isEmpty()) {
            return;
        }
        Set<String> empNums = employeeNumbers.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (empNums.isEmpty()) {
            return;
        }

        Integer latestPeriodId = personalCourseCompletionMapper.getLatestPeriodId();
        if (latestPeriodId == null) {
            throw new IllegalStateException("未找到最新周期（t_employee_sync.period_id），无法刷新个人学分");
        }

        List<EmployeeSyncDataVO> employees =
                personalCourseCompletionMapper.getEmployeesByPeriodIdAndEmployeeNumbers(latestPeriodId, empNums);
        Map<String, EmployeeSyncDataVO> employeeMap = new HashMap<>();
        if (employees != null) {
            for (EmployeeSyncDataVO e : employees) {
                if (e != null && e.getEmployeeNumber() != null) {
                    employeeMap.put(e.getEmployeeNumber(), e);
                }
            }
        }
        List<String> missing = empNums.stream()
                .filter(n -> !employeeMap.containsKey(n))
                .collect(Collectors.toList());
        if (!missing.isEmpty()) {
            throw new IllegalArgumentException("以下工号在最新周期人员信息中不存在，无法刷新个人学分：" + String.join("、", missing));
        }

        // 预加载课程信息与部门选课信息（与全量同步保持一致）
        List<CoursePlanningInfoVO> allCoursesRaw = coursePlanningInfoMapper.getAllCoursePlanningInfo();
        List<CoursePlanningInfoVO> allCourses = allCoursesRaw.stream()
                .filter(c -> c.getCourseLevel() != null && c.getCourseName() != null && c.getCourseNumber() != null)
                .filter(c -> TARGET_COURSE_LEVELS.contains(c.getCourseLevel()))
                .collect(Collectors.toList());

        Map<Integer, BigDecimal> courseCreditMap = new HashMap<>();
        Map<Integer, CoursePlanningInfoVO> courseById = new HashMap<>();
        for (CoursePlanningInfoVO course : allCourses) {
            BigDecimal credit = BigDecimal.ZERO;
            try {
                if (course.getCredit() != null && !course.getCredit().isEmpty()) {
                    credit = new BigDecimal(course.getCredit());
                }
            } catch (Exception e) {
                logger.warn("Invalid credit format for course {}: {}", course.getId(), course.getCredit());
            }
            if (course.getId() != null) {
                courseCreditMap.put(course.getId(), credit);
                courseById.put(course.getId(), course);
            }
        }

        List<DeptCourseSelection> allDeptSelections = coursePlanningInfoMapper.getAllDeptSelections();
        Map<String, DeptCourseSelection> deptSelectionMap = new HashMap<>();
        for (DeptCourseSelection selection : allDeptSelections) {
            if (selection != null && selection.getDeptCode() != null && !selection.getDeptCode().trim().isEmpty()) {
                deptSelectionMap.put(selection.getDeptCode().trim(), selection);
            }
        }

        // 手工录入学分汇总（employee_number -> SUM(credits)）
        Map<String, BigDecimal> manualCreditSumMap = new HashMap<>();
        List<String> empList = new ArrayList<>(empNums);
        List<ManualCreditSumRow> manualRows = manualEnterCreditMapper.sumCreditsByEmployeeNumbers(empList);
        if (manualRows != null) {
            for (ManualCreditSumRow row : manualRows) {
                if (row == null || row.getEmployeeNumber() == null) {
                    continue;
                }
                manualCreditSumMap.put(row.getEmployeeNumber(),
                        row.getTotalCredits() != null ? row.getTotalCredits() : BigDecimal.ZERO);
            }
        }

        // 批量查询现有 personal_credit（用于保留 creditCompletionDate 等）
        Map<String, PersonalCredit> existingCreditMap = new HashMap<>();
        List<PersonalCredit> existingList = personalCreditMapper.getByEmployeeNumbers(empList);
        if (existingList != null) {
            for (PersonalCredit pc : existingList) {
                if (pc != null && pc.getEmployeeNumber() != null) {
                    existingCreditMap.put(pc.getEmployeeNumber(), pc);
                }
            }
        }

        // AI 认证学分（工号 -> 认证学分，自然上限 15）
        Map<String, BigDecimal> certCreditMap = loadCreditMap(
                personalCreditMapper::getAiCertCreditsByEmployeeNumbers, empList);
        // AI 任职学分（工号 -> 任职学分，自然上限 25）
        Map<String, BigDecimal> qualCreditMap = loadCreditMap(
                personalCreditMapper::getAiQualificationCreditsByEmployeeNumbers, empList);

        List<PersonalCredit> toSaveList = new ArrayList<>(empNums.size());
        Set<String> affectedLowestDeptNumbers = new LinkedHashSet<>();
        for (String emp : empNums) {
            EmployeeSyncDataVO employee = employeeMap.get(emp);
            PersonalCredit credit = calculateEmployeeCredit(
                    employee,
                    courseCreditMap,
                    deptSelectionMap,
                    allCourses,
                    courseById,
                    existingCreditMap,
                    manualCreditSumMap,
                    certCreditMap,
                    qualCreditMap
            );
            if (credit != null) {
                toSaveList.add(credit);
                if (credit.getLowestDeptNumber() != null && !credit.getLowestDeptNumber().trim().isEmpty()) {
                    affectedLowestDeptNumbers.add(credit.getLowestDeptNumber().trim());
                }
            }
        }

        if (!toSaveList.isEmpty()) {
            personalCreditMapper.batchInsertOrUpdate(toSaveList);
        }

        // 局部刷新部门标杆（仅本次涉及的部门）
        updateDeptBenchmarksForDeptNumbers(affectedLowestDeptNumbers);
    }

    private PersonalCredit calculateEmployeeCredit(EmployeeSyncDataVO employee,
                                                   Map<Integer, BigDecimal> courseCreditMap,
                                                   Map<String, DeptCourseSelection> deptSelectionMap,
                                                   List<CoursePlanningInfoVO> allCourses,
                                                   Map<Integer, CoursePlanningInfoVO> courseById,
                                                   Map<String, PersonalCredit> existingCreditMap,
                                                   Map<String, BigDecimal> manualCreditSumMap,
                                                   Map<String, BigDecimal> certCreditMap,
                                                   Map<String, BigDecimal> qualCreditMap) {
        String empNum = employee.getEmployeeNumber();
        String fourthDeptCode = employee.getFourthdeptcode();

        DeptCourseSelection selection = (fourthDeptCode == null || fourthDeptCode.trim().isEmpty())
                ? null
                : deptSelectionMap.get(fourthDeptCode.trim());

        Set<Integer> selectedCourseIds = new LinkedHashSet<>();
        if (selection != null) {
            selectedCourseIds.addAll(parseCourseIds(selection.getCourseSelections()));
            selectedCourseIds.addAll(parseCourseIds(selection.getPracticalSelections()));
        }

        boolean useAllCourses = selectedCourseIds.isEmpty();

        // 计算目标学分（包含实战课程）
        BigDecimal targetCredit = BigDecimal.ZERO;
        // 基础/进阶目标课程（按课程表主键ID锁定唯一课程，即唯一 (bigType, number)）
        List<CoursePlanningInfoVO> targetBasicAdvancedCourses = new ArrayList<>();
        // 仅用于查询完课（完课表只认 number，不区分 bigType）
        Set<String> targetCourseNumberSet = new LinkedHashSet<>();

        if (useAllCourses) {
            for (CoursePlanningInfoVO course : allCourses) {
                BigDecimal credit = courseCreditMap.getOrDefault(course.getId(), BigDecimal.ZERO);
                targetCredit = targetCredit.add(credit);
                if ("基础".equals(course.getCourseLevel()) || "进阶".equals(course.getCourseLevel())) {
                    targetBasicAdvancedCourses.add(course);
                    if (course.getCourseNumber() != null && !course.getCourseNumber().trim().isEmpty()) {
                        targetCourseNumberSet.add(course.getCourseNumber().trim());
                    }
                }
            }
        } else {
            for (Integer courseId : selectedCourseIds) {
                CoursePlanningInfoVO c = courseById.get(courseId);
                if (c == null || c.getCourseLevel() == null || !TARGET_COURSE_LEVELS.contains(c.getCourseLevel())) {
                    continue;
                }
                BigDecimal credit = courseCreditMap.getOrDefault(courseId, BigDecimal.ZERO);
                targetCredit = targetCredit.add(credit);
                if ("基础".equals(c.getCourseLevel()) || "进阶".equals(c.getCourseLevel())) {
                    targetBasicAdvancedCourses.add(c);
                    if (c.getCourseNumber() != null && !c.getCourseNumber().trim().isEmpty()) {
                        targetCourseNumberSet.add(c.getCourseNumber().trim());
                    }
                }
            }
        }

        // 计算当前课程学分：基础/进阶完课 + 实战完课
        BigDecimal courseCompletedCredit = BigDecimal.ZERO;

        // 1) 基础/进阶完课（micro/mooc）
        if (!targetCourseNumberSet.isEmpty() && !targetBasicAdvancedCourses.isEmpty()) {
            List<String> targetCourseNumbers = new ArrayList<>(targetCourseNumberSet);
            List<String> completedCourseNumbers = personalCourseCompletionMapper.getCompletedCourseNumbers(empNum, targetCourseNumbers);
            Set<String> completedSet = completedCourseNumbers == null ? Collections.emptySet()
                    : completedCourseNumbers.stream()
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toSet());

            // 注意：完课表按 number 认定完课；若某 number 完课，则该 number 对应的所有目标课程（不同 bigType）均视为完课
            for (CoursePlanningInfoVO course : targetBasicAdvancedCourses) {
                if (course == null || course.getId() == null || course.getCourseNumber() == null) {
                    continue;
                }
                String num = course.getCourseNumber().trim();
                if (num.isEmpty()) {
                    continue;
                }
                if (completedSet.contains(num)) {
                    courseCompletedCredit = courseCompletedCredit.add(
                            courseCreditMap.getOrDefault(course.getId(), BigDecimal.ZERO)
                    );
                }
            }
        }

        // 2) 实战完课（hands_on_courses + ai_course_planning_info.syb_type）
        List<Integer> completedPracticalCourseIds = handsOnCourseMapper.selectCompletedPracticalCourseIdsByAccount(empNum);
        if (completedPracticalCourseIds != null && !completedPracticalCourseIds.isEmpty()) {
            for (Integer cid : completedPracticalCourseIds) {
                if (cid == null) {
                    continue;
                }
                if (!useAllCourses && !selectedCourseIds.contains(cid)) {
                    // 有选课时，仅统计目标范围内的完课
                    continue;
                }
                courseCompletedCredit = courseCompletedCredit.add(courseCreditMap.getOrDefault(cid, BigDecimal.ZERO));
            }
        }

        // 叠加手工录入学分（一个人可能多条，已在同步前按工号汇总）
        BigDecimal manualCredit = safeGet(manualCreditSumMap, empNum);
        // 叠加 AI 认证学分（专业级 15 / 工作级 10，同人 MAX，自然上限 15）
        BigDecimal certCredit = safeGet(certCreditMap, empNum);
        // 叠加 AI 任职学分（4 级及以上 25 / 3 级 10，同人 MAX，自然上限 25，仅当前有效）
        BigDecimal qualCredit = safeGet(qualCreditMap, empNum);
        BigDecimal totalCurrentCredit = courseCompletedCredit
                .add(manualCredit)
                .add(certCredit)
                .add(qualCredit);

        // 计算达成率
        BigDecimal completionRate = BigDecimal.ZERO;
        if (targetCredit.compareTo(BigDecimal.ZERO) > 0) {
            completionRate = totalCurrentCredit.divide(targetCredit, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        // 准备保存数据
        PersonalCredit existing = existingCreditMap.get(empNum);
        PersonalCredit toSave = new PersonalCredit();
        toSave.setEmployeeNumber(empNum);
        toSave.setLastName(employee.getLastName());
        toSave.setLowestDeptNumber(employee.getLowestDeptNumber());
        toSave.setLowestDept(employee.getLowestDept());
        toSave.setL0DepartmentCode(employee.getL0DepartmentCode());
        toSave.setL0DepartmentCnName(employee.getL0DepartmentCnName());
        toSave.setFirstdeptcode(employee.getFirstdeptcode());
        toSave.setFirstdept(getChineseDeptName(employee.getFirstdept()));
        toSave.setSeconddeptcode(employee.getSeconddeptcode());
        toSave.setSeconddept(getChineseDeptName(employee.getSeconddept()));
        toSave.setThirddeptcode(employee.getThirddeptcode());
        toSave.setThirddept(getChineseDeptName(employee.getThirddept()));
        toSave.setFourthdeptcode(employee.getFourthdeptcode());
        toSave.setFourthdept(getChineseDeptName(employee.getFourthdept()));
        toSave.setFifthdeptcode(employee.getFifthdeptcode());
        toSave.setFifthdept(getChineseDeptName(employee.getFifthdept()));
        toSave.setSixthdeptcode(employee.getSixthdeptcode());
        toSave.setSixthdept(getChineseDeptName(employee.getSixthdept()));

        // 处理岗位信息：t_employee_sync.job_category (岗位族-岗位类-岗位子类)
        String fullJobCategory = employee.getJobCategory();
        if (fullJobCategory != null && !fullJobCategory.isEmpty()) {
            String[] parts = fullJobCategory.split("-");
            if (parts.length >= 3) {
                toSave.setJobFamily(parts[0]);
                toSave.setJobCategory(parts[1]);
                toSave.setJobSubcategory(parts[2]);
            } else if (parts.length == 2) {
                toSave.setJobFamily(parts[0]);
                toSave.setJobCategory(parts[1]);
                toSave.setJobSubcategory(null);
            } else {
                toSave.setJobCategory(fullJobCategory);
                toSave.setJobFamily(null);
                toSave.setJobSubcategory(null);
            }
        } else {
            toSave.setJobFamily(null);
            toSave.setJobCategory(null);
            toSave.setJobSubcategory(null);
        }

        toSave.setTargetCredit(targetCredit);
        toSave.setCurrentCredit(totalCurrentCredit);
        toSave.setPersonalCreditCompletionRate(completionRate);
        toSave.setDeptBenchmarkCompletionRate(BigDecimal.ZERO); // 先置0，后续统一更新

        // 设置AI成熟度字段
        toSave.setCadrePositionAiMaturity(employee.getCadrePositionAiMaturity());
        toSave.setExpertPositionAiMaturity(employee.getExpertPositionAiMaturity());

        // 处理达成日期
        if (existing != null) {
            toSave.setCreditCompletionDate(existing.getCreditCompletionDate());
        }

        // 如果当前已达标（current >= target）且之前没有日期，则设置当前时间
        // 注意：targetCredit可能为0，需处理
        if (targetCredit.compareTo(BigDecimal.ZERO) > 0 && totalCurrentCredit.compareTo(targetCredit) >= 0) {
            if (toSave.getCreditCompletionDate() == null) {
                toSave.setCreditCompletionDate(new Date());
            }
        } else if (targetCredit.compareTo(BigDecimal.ZERO) == 0 && totalCurrentCredit.compareTo(BigDecimal.ZERO) >= 0) {
            // 目标为0，视为达标？通常应该有学分。这里假设不处理或视为达标
            if (toSave.getCreditCompletionDate() == null) {
                toSave.setCreditCompletionDate(new Date());
            }
        }

        return toSave;
    }

    private static Set<Integer> parseCourseIds(String commaSeparated) {
        Set<Integer> ids = new LinkedHashSet<>();
        if (commaSeparated == null || commaSeparated.trim().isEmpty()) {
            return ids;
        }
        for (String s : commaSeparated.split(",")) {
            if (s == null) {
                continue;
            }
            String t = s.trim();
            if (t.isEmpty()) {
                continue;
            }
            try {
                ids.add(Integer.parseInt(t));
            } catch (NumberFormatException ignore) {
            }
        }
        return ids;
    }

    /**
     * 分批加载 "工号 -> 学分" 映射，按 1000/批调用指定查询函数。
     * 用于 AI 认证学分、AI 任职学分等按工号聚合的批量查询。
     */
    private Map<String, BigDecimal> loadCreditMap(
            Function<List<String>, List<EmployeeCreditRow>> queryFn,
            List<String> employeeNumbers) {
        Map<String, BigDecimal> map = new HashMap<>();
        if (employeeNumbers == null || employeeNumbers.isEmpty()) {
            return map;
        }
        int batchSize = 1000;
        for (int i = 0; i < employeeNumbers.size(); i += batchSize) {
            int end = Math.min(i + batchSize, employeeNumbers.size());
            List<EmployeeCreditRow> rows = queryFn.apply(employeeNumbers.subList(i, end));
            if (rows == null) {
                continue;
            }
            for (EmployeeCreditRow r : rows) {
                if (r == null || r.getEmployeeNumber() == null) {
                    continue;
                }
                map.put(r.getEmployeeNumber(),
                        r.getCredit() != null ? r.getCredit() : BigDecimal.ZERO);
            }
        }
        return map;
    }

    private static BigDecimal safeGet(Map<String, BigDecimal> m, String k) {
        if (m == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal v = m.get(k);
        return v != null ? v : BigDecimal.ZERO;
    }

    private void updateDeptBenchmarks() {
        // 1. 获取所有涉及的最小部门
        List<String> lowestDeptNumbers = personalCreditMapper.getAllLowestDeptNumbers();

        // 2. 遍历部门，计算最大达成率并更新
        for (String deptNum : lowestDeptNumbers) {
            BigDecimal maxRate = personalCreditMapper.getMaxCompletionRateByDept(deptNum);
            if (maxRate == null) maxRate = BigDecimal.ZERO;

            personalCreditMapper.updateBenchmarkRateByDept(deptNum, maxRate);
        }
    }

    private void updateDeptBenchmarksForDeptNumbers(Collection<String> lowestDeptNumbers) {
        if (lowestDeptNumbers == null || lowestDeptNumbers.isEmpty()) {
            return;
        }
        for (String deptNum : lowestDeptNumbers) {
            if (deptNum == null || deptNum.trim().isEmpty()) {
                continue;
            }
            BigDecimal maxRate = personalCreditMapper.getMaxCompletionRateByDept(deptNum.trim());
            if (maxRate == null) {
                maxRate = BigDecimal.ZERO;
            }
            personalCreditMapper.updateBenchmarkRateByDept(deptNum.trim(), maxRate);
        }
    }

    /**
     * 获取职位学分统计
     * @param deptCode 部门编码
     * @param role 角色 (1:干部, 2:专家, 3:基层管理者)
     * @return 统计结果
     */
    public CreditStatisticsResponseVO getPositionStatistics(String deptCode, String role) {
        // 如果deptCode为空或"0"，默认为ICT BG / 云核心网产品线
        if (deptCode == null || deptCode.trim().isEmpty() || "0".equals(deptCode.trim())) {
            deptCode = "031562"; // 云核心网产品线编码
        }

        List<CreditOverviewVO> list = personalCreditMapper.getPositionStatistics(deptCode, role);
        calculateTimeProgressAndWarning(list);

        CreditStatisticsResponseVO response = new CreditStatisticsResponseVO();
        response.setDeptCode(deptCode);

        String deptName = "未知部门";
        try {
            DepartmentInfoVO deptInfo = departmentService.getDepartmentInfo(deptCode);
            if (deptInfo != null) {
                deptName = deptInfo.getDeptName();
            } else if ("031562".equals(deptCode)) {
                deptName = "云核心网产品线";
            }
        } catch (Exception e) {
            logger.warn("Failed to get department info for {}: {}", deptCode, e.getMessage());
            if ("031562".equals(deptCode)) {
                deptName = "云核心网产品线";
            }
        }
        response.setDeptName(deptName);
        response.setStatistics(list);

        // 计算总计
        response.setTotalStatistics(calculateTotalStatistics(list, deptCode, role));

        return response;
    }

    /**
     * 获取部门学分统计
     * @param deptCode 部门编码
     * @param role 角色 (1:干部, 2:专家, 3:基层管理者)
     * @return 统计结果
     */
    public CreditStatisticsResponseVO getDepartmentStatistics(String deptCode, String role) {
        boolean useDefaultStrategy = false;
        // 如果deptCode为空或"0"，默认为ICT BG / 云核心网产品线，查询下级（4级）
        if (deptCode == null || deptCode.trim().isEmpty() || "0".equals(deptCode.trim())) {
            deptCode = "031562"; // 云核心网产品线编码
            useDefaultStrategy = true;
        }

        // 获取部门信息以确定层级
        DepartmentInfoVO deptInfo = null;
        try {
            deptInfo = departmentService.getDepartmentInfo(deptCode);
        } catch (Exception e) {
            logger.warn("Failed to get department info for {}: {}", deptCode, e.getMessage());
        }

        String level = "lowest_dept"; // 默认兜底

        if (useDefaultStrategy) {
            // 默认查询（云核心网产品线）时，查询两级下的部门（即四级部门）
            level = "fourthdept";
        } else if (deptInfo != null && deptInfo.getDeptLevel() != null) {
            try {
                int currentLevel = Integer.parseInt(deptInfo.getDeptLevel());
                // 查询下一级
                level = getDeptLevelColumnName(currentLevel + 1);
            } catch (NumberFormatException e) {
                logger.warn("Invalid dept level format: {}", deptInfo.getDeptLevel());
            }
        } else {
            // 如果查不到部门信息，尝试默认处理：云核心网(3级) -> 查4级
            if ("031562".equals(deptCode)) {
                level = "fourthdept";
            }
        }

        // SQL注入防护：校验level参数是否为合法的部门列名
        if (!isValidDeptLevel(level)) {
            logger.warn("Invalid department level column name: {}, falling back to lowest_dept", level);
            level = "lowest_dept";
        }
        String levelCode = getDeptLevelCodeColumnName(level);
        if (!isValidDeptLevel(levelCode)) {
            levelCode = "lowest_dept_number";
        }

        List<CreditOverviewVO> list = personalCreditMapper.getDepartmentStatistics(level, levelCode, deptCode, role);
        calculateTimeProgressAndWarning(list);

        // 只展示指定顺序的部门，但总计仍然基于数据库全量数据
        // 指定顺序：
        // 1. 分组核心网产品部
        // 2. 云核心网CS&IMS产品部
        // 3. 融合视频产品部
        // 4. 云核心网软件平台部
        // 5. 云核心网解决方案增值开发部
        // 6. 云核心网解决方案部
        // 7. 云核心网架构与设计部
        // 8. 云核心网技术规划部
        // 9. 云核心网研究部
        // 10. 云核心网产品工程与IT装备部
        List<String> orderedDeptNames = Arrays.asList(
                "分组核心网产品部",
                "云核心网CS&IMS产品部",
                "融合视频产品部",
                "云核心网软件平台部",
                "云核心网解决方案增值开发部",
                "云核心网解决方案部",
                "云核心网架构与设计部",
                "云核心网技术规划部",
                "云核心网研究部",
                "云核心网产品工程与IT装备部"
        );
        Map<String, Integer> orderMap = new HashMap<>();
        for (int i = 0; i < orderedDeptNames.size(); i++) {
            orderMap.put(orderedDeptNames.get(i), i);
        }

        List<CreditOverviewVO> finalList;
        if (useDefaultStrategy) {
            // 默认查询四级部门时，按白名单过滤并排序
            finalList = list.stream()
                    .filter(vo -> {
                        String name = getChineseDeptName(vo.getCategoryName());
                        return orderMap.containsKey(name);
                    })
                    .sorted(Comparator.comparingInt(vo -> {
                        String name = getChineseDeptName(((CreditOverviewVO) vo).getCategoryName());
                        return orderMap.getOrDefault(name, Integer.MAX_VALUE);
                    }))
                    .collect(Collectors.toList());
        } else {
            // 选了具体部门下钻时，直接返回，不做白名单过滤
            finalList = list;
        }

        CreditStatisticsResponseVO response = new CreditStatisticsResponseVO();
        response.setDeptCode(deptCode);

        String deptName = "未知部门";
        if (deptInfo != null) {
            deptName = deptInfo.getDeptName();
        } else if ("031562".equals(deptCode)) {
            deptName = "云核心网产品线";
        }
        response.setDeptName(deptName);
        response.setStatistics(finalList);

        // 计算总计（保留全量数据库统计）
        response.setTotalStatistics(calculateTotalStatistics(list, deptCode, role));

        return response;
    }

    private String getDeptLevelColumnName(int level) {
        switch (level) {
            case 1: return "firstdept";
            case 2: return "seconddept";
            case 3: return "thirddept";
            case 4: return "fourthdept";
            case 5: return "fifthdept";
            case 6: return "sixthdept";
            default: return "lowest_dept";
        }
    }

    private String getDeptLevelCodeColumnName(String levelName) {
        switch (levelName) {
            case "firstdept": return "firstdeptcode";
            case "seconddept": return "seconddeptcode";
            case "thirddept": return "thirddeptcode";
            case "fourthdept": return "fourthdeptcode";
            case "fifthdept": return "fifthdeptcode";
            case "sixthdept": return "sixthdeptcode";
            case "lowest_dept": return "lowest_dept_number";
            default: return "lowest_dept_number";
        }
    }

    private CreditOverviewVO calculateTotalStatistics(List<CreditOverviewVO> list, String deptCode, String role) {
        // 使用数据库直接查询总计数据，避免因人员挂靠导致手动累加不准确的问题
        CreditOverviewVO total = personalCreditMapper.getTotalStatistics(deptCode, role);

        if (total == null) {
            total = new CreditOverviewVO();
            total.setCategoryName("总计");
            total.setBaselineHeadcount(0);
            total.setMaxScore(BigDecimal.ZERO);
            total.setMinScore(BigDecimal.ZERO);
            total.setAchievementRate(BigDecimal.ZERO);
            total.setAverageCurrentCredit(BigDecimal.ZERO);
            total.setAverageTargetCredit(BigDecimal.ZERO);
        }

        // 总计的时间进度和预警
        Calendar calendar = Calendar.getInstance();
        int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
        int totalDays = calendar.getActualMaximum(Calendar.DAY_OF_YEAR);
        BigDecimal timeProgress = new BigDecimal(dayOfYear).divide(new BigDecimal(totalDays), 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP);
        total.setTimeProgress(timeProgress);

        if (total.getAchievementRate() != null) {
            total.setIsWarning(total.getAchievementRate().compareTo(timeProgress) < 0);
        } else {
            total.setIsWarning(true);
        }

        return total;
    }

    private String getChineseDeptName(String deptName) {
        if (deptName == null || deptName.isEmpty()) {
            return deptName;
        }
        // 如果包含'/'，取第一部分作为中文名
        if (deptName.contains("/")) {
            return deptName.split("/")[0].trim();
        }
        return deptName;
    }

    /**
     * 从 t_personal_credit 表推断 deptCode 所属的部门层级
     * @param deptCode 部门编码
     * @return 部门层级 (1-6)，如果无法推断则返回 null
     */
    private Integer inferDeptLevelFromCredit(String deptCode) {
        if (deptCode == null || deptCode.trim().isEmpty()) {
            return null;
        }

        // 按优先级检查各层级编码字段
        String[] levelCodeColumns = {
            "firstdeptcode", "seconddeptcode", "thirddeptcode",
            "fourthdeptcode", "fifthdeptcode", "sixthdeptcode"
        };

        for (int i = 0; i < levelCodeColumns.length; i++) {
            Long count = personalCreditMapper.countByDeptCodeColumn(levelCodeColumns[i], deptCode);
            if (count != null && count > 0) {
                return i + 1; // 返回层级 1-6
            }
        }

        return null;
    }

    private boolean isValidDeptLevel(String level) {
        Set<String> validLevels = new HashSet<>(Arrays.asList(
            "lowest_dept", "firstdept", "seconddept", "thirddept", "fourthdept", "fifthdept", "sixthdept",
            "lowest_dept_number", "firstdeptcode", "seconddeptcode", "thirddeptcode", "fourthdeptcode", "fifthdeptcode", "sixthdeptcode"
        ));
        return validLevels.contains(level);
    }

    private void calculateTimeProgressAndWarning(List<CreditOverviewVO> list) {
        // 计算时间进度：当前天数 / 365
        Calendar calendar = Calendar.getInstance();
        int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
        int totalDays = calendar.getActualMaximum(Calendar.DAY_OF_YEAR);
        BigDecimal timeProgress = new BigDecimal(dayOfYear).divide(new BigDecimal(totalDays), 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP);

        for (CreditOverviewVO vo : list) {
            vo.setTimeProgress(timeProgress);
            // 预警判断：达成率 < 时间进度
            if (vo.getAchievementRate() != null) {
                vo.setIsWarning(vo.getAchievementRate().compareTo(timeProgress) < 0);
            } else {
                vo.setIsWarning(true); // 无达成率视为预警
            }
        }
    }

    /**
     * 获取 AI School 看板 - 专家 & 干部学分总览
     * @param deptCode 部门编码，null / "" / "0" 时查全量
     */
    public SchoolRoleSummaryResponseVO getRoleSummary(String deptCode) {
        String dept = (deptCode == null || deptCode.trim().isEmpty() || "0".equals(deptCode.trim()))
                ? null : deptCode;

        List<SchoolRoleSummaryVO> expertList = personalCreditMapper.getExpertRoleSummary(dept);
        List<SchoolRoleSummaryVO> cadreList  = personalCreditMapper.getCadreRoleSummary(dept);

        fillRoleSummaryStatus(expertList);
        fillRoleSummaryStatus(cadreList);

        SchoolRoleSummaryResponseVO vo = new SchoolRoleSummaryResponseVO();
        vo.setExpertSummary(expertList);
        vo.setCadreSummary(cadreList);
        return vo;
    }

    /**
     * 获取 AI School 看板 - 基线人数下钻明细（分页）
     */
    public SchoolCreditDetailResponseVO getSchoolCreditDetailList(SchoolCreditDetailRequestVO request) {
        int pageNum  = (request.getPageNum()  == null || request.getPageNum()  < 1) ? 1  : request.getPageNum();
        int pageSize = (request.getPageSize() == null || request.getPageSize() < 1) ? 50 : request.getPageSize();
        request.setPageNum(pageNum);
        request.setPageSize(pageSize);

        long total = personalCreditMapper.countSchoolCreditDetail(request);
        List<SchoolCreditDetailVO> records = total > 0
                ? personalCreditMapper.getSchoolCreditDetailList(request)
                : Collections.emptyList();

        fillDetailStatus(records);

        SchoolCreditDetailResponseVO vo = new SchoolCreditDetailResponseVO();
        vo.setRecords(records);
        vo.setTotal(total);
        vo.setPageNum(pageNum);
        vo.setPageSize(pageSize);
        vo.setPages((int) Math.ceil((double) total / pageSize));
        return vo;
    }

// ---- 私有辅助方法 ----

    private void fillRoleSummaryStatus(List<SchoolRoleSummaryVO> rows) {
        java.time.LocalDate today = java.time.LocalDate.now();
        double progress = (double) today.getDayOfYear()
                / (today.isLeapYear() ? 366 : 365);

        for (SchoolRoleSummaryVO row : rows) {
            double target = row.getTargetCredits() == null ? 0.0 : row.getTargetCredits();
            row.setScheduleTarget(Math.round(target * progress * 10.0) / 10.0);

            double rate = row.getCompletionRate() == null ? 0.0 : row.getCompletionRate();
            if (rate >= 100.0) {
                row.setStatus("正常");  row.setStatusType("success");
            } else if (rate >= 60.0) {
                row.setStatus("预警");  row.setStatusType("warning");
            } else {
                row.setStatus("滞后");  row.setStatusType("danger");
            }
        }
    }

    private void fillDetailStatus(List<SchoolCreditDetailVO> records) {
        for (SchoolCreditDetailVO row : records) {
            double rate = row.getCompletionRate() == null ? 0.0
                    : row.getCompletionRate().doubleValue();
            if (rate >= 100.0) {
                row.setStatus("正常");  row.setStatusType("success");
            } else if (rate >= 60.0) {
                row.setStatus("预警");  row.setStatusType("warning");
            } else {
                row.setStatus("滞后");  row.setStatusType("danger");
            }
        }
    }
}