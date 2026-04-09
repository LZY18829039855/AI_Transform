package com.huawei.aitransform.service;

import com.huawei.aitransform.entity.CoursePlanningInfoVO;
import com.huawei.aitransform.entity.CreditOverviewVO;
import com.huawei.aitransform.entity.CreditStatisticsResponseVO;
import com.huawei.aitransform.entity.DeptCourseSelection;
import com.huawei.aitransform.entity.DepartmentInfoVO;
import com.huawei.aitransform.entity.EmployeeSyncDataVO;
import com.huawei.aitransform.entity.PersonalCredit;
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
    private DepartmentInfoService departmentService;

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
                .collect(Collectors.toList());

        Map<Integer, BigDecimal> courseCreditMap = new HashMap<>();
        Map<String, BigDecimal> courseNumberCreditMap = new HashMap<>(); // Number -> Credit

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
            }
            if (course.getCourseNumber() != null) {
                courseNumberCreditMap.put(course.getCourseNumber(), credit);
            }
        }

        // 3.2 所有部门选课信息 (Map: DeptCode -> List<CourseID>)
        List<DeptCourseSelection> allDeptSelections = coursePlanningInfoMapper.getAllDeptSelections();
        Map<String, List<Integer>> deptSelectionMap = new HashMap<>();
        for (DeptCourseSelection selection : allDeptSelections) {
            List<Integer> courseIds = new ArrayList<>();
            if (selection.getCourseSelections() != null && !selection.getCourseSelections().trim().isEmpty()) {
                String[] ids = selection.getCourseSelections().split(",");
                for (String idStr : ids) {
                    try {
                        courseIds.add(Integer.parseInt(idStr.trim()));
                    } catch (NumberFormatException e) {
                        // ignore
                    }
                }
            }
            deptSelectionMap.put(selection.getDeptCode(), courseIds);
        }

        // 4. 遍历员工计算学分
        List<String> employeeNumbers = employees.stream()
                .map(EmployeeSyncDataVO::getEmployeeNumber)
                .collect(Collectors.toList());

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

        List<PersonalCredit> toSaveList = new ArrayList<>();
        for (EmployeeSyncDataVO employee : employees) {
            PersonalCredit credit = calculateEmployeeCredit(employee, courseCreditMap, courseNumberCreditMap, deptSelectionMap, allCourses, existingCreditMap);
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

    private PersonalCredit calculateEmployeeCredit(EmployeeSyncDataVO employee,
                                                   Map<Integer, BigDecimal> courseCreditMap,
                                                   Map<String, BigDecimal> courseNumberCreditMap,
                                                   Map<String, List<Integer>> deptSelectionMap,
                                                   List<CoursePlanningInfoVO> allCourses,
                                                   Map<String, PersonalCredit> existingCreditMap) {
        String empNum = employee.getEmployeeNumber();
        String fourthDeptCode = employee.getFourthdeptcode();

        // 计算目标学分
        BigDecimal targetCredit = BigDecimal.ZERO;
        List<String> targetCourseNumbers = new ArrayList<>();

        List<Integer> selectedCourseIds = deptSelectionMap.get(fourthDeptCode);

        // 如果部门没有选课，或者是空列表，默认使用所有课程？
        // 原逻辑：if (targetCourseIds.isEmpty()) useAllCourses = true;
        // 这里沿用原逻辑：如果没选课，则是所有课程
        boolean useAllCourses = (selectedCourseIds == null || selectedCourseIds.isEmpty());

        if (useAllCourses) {
            for (CoursePlanningInfoVO course : allCourses) {
                BigDecimal credit = BigDecimal.ZERO;
                try {
                    if (course.getCredit() != null) credit = new BigDecimal(course.getCredit());
                } catch (Exception e) {}
                targetCredit = targetCredit.add(credit);
                targetCourseNumbers.add(course.getCourseNumber());
            }
        } else {
            for (Integer courseId : selectedCourseIds) {
                BigDecimal credit = courseCreditMap.getOrDefault(courseId, BigDecimal.ZERO);
                targetCredit = targetCredit.add(credit);
                // 找到对应的courseNumber
                allCourses.stream().filter(c -> c.getId().equals(courseId)).findFirst()
                        .ifPresent(c -> targetCourseNumbers.add(c.getCourseNumber()));
            }
        }

        // 计算当前学分
        BigDecimal currentCredit = BigDecimal.ZERO;
        if (!targetCourseNumbers.isEmpty()) {
            List<String> completedCourseNumbers = personalCourseCompletionMapper.getCompletedCourseNumbers(empNum, targetCourseNumbers);
            for (String courseNum : completedCourseNumbers) {
                BigDecimal credit = courseNumberCreditMap.getOrDefault(courseNum, BigDecimal.ZERO);
                currentCredit = currentCredit.add(credit);
            }
        }

        // 计算达成率
        BigDecimal completionRate = BigDecimal.ZERO;
        if (targetCredit.compareTo(BigDecimal.ZERO) > 0) {
            completionRate = currentCredit.divide(targetCredit, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP);
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
        toSave.setCurrentCredit(currentCredit);
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
        if (targetCredit.compareTo(BigDecimal.ZERO) > 0 && currentCredit.compareTo(targetCredit) >= 0) {
            if (toSave.getCreditCompletionDate() == null) {
                toSave.setCreditCompletionDate(new Date());
            }
        } else if (targetCredit.compareTo(BigDecimal.ZERO) == 0 && currentCredit.compareTo(BigDecimal.ZERO) >= 0) {
            // 目标为0，视为达标？通常应该有学分。这里假设不处理或视为达标
            if (toSave.getCreditCompletionDate() == null) {
                toSave.setCreditCompletionDate(new Date());
            }
        }

        return toSave;
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