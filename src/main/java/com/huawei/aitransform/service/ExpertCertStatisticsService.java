package com.huawei.aitransform.service;

import com.huawei.aitransform.constant.DepartmentConstants;
import com.huawei.aitransform.entity.CadreInfoVO;
import com.huawei.aitransform.entity.CadreJobCategoryCertStatisticsVO;
import com.huawei.aitransform.entity.CadreJobCategoryQualifiedStatisticsVO;
import com.huawei.aitransform.entity.CadreMaturityCertStatisticsVO;
import com.huawei.aitransform.entity.CadreMaturityJobCategoryCertStatisticsResponseVO;
import com.huawei.aitransform.entity.CadreMaturityJobCategoryQualifiedStatisticsResponseVO;
import com.huawei.aitransform.entity.CadreMaturityQualifiedStatisticsVO;
import com.huawei.aitransform.entity.CadreQualificationVO;
import com.huawei.aitransform.entity.ExpertQualificationVO;
import com.huawei.aitransform.entity.CompetenceCategoryCertStatisticsResponseVO;
import com.huawei.aitransform.entity.CompetenceCategoryCertStatisticsVO;
import com.huawei.aitransform.entity.CompetenceCategoryDeptStatisticsVO;
import com.huawei.aitransform.entity.DepartmentCertStatisticsVO;
import com.huawei.aitransform.entity.DepartmentInfoVO;
import com.huawei.aitransform.entity.DepartmentMaturityVO;
import com.huawei.aitransform.entity.EmployeeCertStatisticsResponseVO;
import com.huawei.aitransform.entity.EmployeeDetailVO;
import com.huawei.aitransform.entity.EmployeeDrillDownResponseVO;
import com.huawei.aitransform.entity.EmployeeWithCategoryVO;
import com.huawei.aitransform.entity.ExpertAiCertStatisticsResponseVO;
import com.huawei.aitransform.entity.ExpertAiQualifiedStatisticsResponseVO;
import com.huawei.aitransform.entity.ExpertCertStatisticsResponseVO;
import com.huawei.aitransform.entity.ExpertCertStatisticsVO;
import com.huawei.aitransform.entity.ExpertInfoVO;
import com.huawei.aitransform.entity.ExpertJobCategoryCertStatisticsVO;
import com.huawei.aitransform.entity.ExpertJobCategoryQualifiedStatisticsVO;
import com.huawei.aitransform.entity.ExpertMaturityCertStatisticsVO;
import com.huawei.aitransform.entity.ExpertMaturityQualifiedStatisticsVO;
import com.huawei.aitransform.entity.MaturityCertStatisticsResponseVO;
import com.huawei.aitransform.entity.MaturityCertStatisticsVO;
import com.huawei.aitransform.mapper.CadreMapper;
import com.huawei.aitransform.mapper.DepartmentInfoMapper;
import com.huawei.aitransform.mapper.DepartmentMaturityMapper;
import com.huawei.aitransform.mapper.EmployeeMapper;
import com.huawei.aitransform.mapper.ExpertCertStatisticsMapper;
import com.huawei.aitransform.mapper.ExpertMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 专家认证统计服务类
 */
@Service
public class ExpertCertStatisticsService {

    @Autowired
    private ExpertCertStatisticsMapper expertCertStatisticsMapper;

    @Autowired
    private DepartmentInfoMapper departmentInfoMapper;

    @Autowired
    private EmployeeMapper employeeMapper;

    @Autowired
    private DepartmentMaturityMapper departmentMaturityMapper;

    @Autowired
    private CadreMapper cadreMapper;

    @Autowired
    private ExpertMapper expertMapper;

    /**
     * 查询专家任职认证数据
     * @param deptCode 部门ID（部门编码）
     * @return 统计结果
     */
    public ExpertCertStatisticsResponseVO getExpertCertStatistics(String deptCode) {
        // 1. 查询部门信息
        DepartmentInfoVO deptInfo = departmentInfoMapper.getDepartmentByCode(deptCode);
        if (deptInfo == null) {
            throw new IllegalArgumentException("部门不存在：" + deptCode);
        }

        // 2. 根据部门层级查询专家数据（联表查询证书信息）
        List<ExpertCertStatisticsVO> expertList;
        if ("3".equals(deptInfo.getDeptLevel())) {
            // 三层部门：通过部门名称查询专家信息
            expertList = expertCertStatisticsMapper.getExpertStatisticsByLevel3(deptCode, deptInfo.getDeptName());
        } else if ("4".equals(deptInfo.getDeptLevel())) {
            // 四层部门：通过部门编码查询专家信息
            expertList = expertCertStatisticsMapper.getExpertStatisticsByLevel4(deptCode);
        } else {
            throw new IllegalArgumentException("只支持查询三层或四层部门的专家数据");
        }

        // 3. 构建树形结构：按成熟度分组，每个成熟度下按职位类分组
        // 3.1 按成熟度分组统计
        Map<String, Map<String, ExpertCertStatisticsVO>> maturityMap = new HashMap<>(); // 成熟度 -> 职位类 -> 统计信息

        for (ExpertCertStatisticsVO expert : expertList) {
            String aiMaturity = expert.getAiMaturity();
            if (aiMaturity == null || aiMaturity.trim().isEmpty()) {
                aiMaturity = "未知";
            }

            String jobCategory = expert.getJobCategory() != null ? expert.getJobCategory() : "未知";
            Integer hasCert = expert.getHasCert() != null ? expert.getHasCert() : 0;

            // 获取或创建成熟度对应的职位类Map
            Map<String, ExpertCertStatisticsVO> jobCategoryMap = maturityMap.getOrDefault(aiMaturity, new HashMap<>());

            // 获取或创建职位类统计对象
            ExpertCertStatisticsVO jobCategoryStat = jobCategoryMap.getOrDefault(jobCategory, new ExpertCertStatisticsVO());
            jobCategoryStat.setAiMaturity(aiMaturity);
            jobCategoryStat.setJobCategory(jobCategory);
            
            // 累加基线人数
            if (jobCategoryStat.getBaselineCount() == null) {
                jobCategoryStat.setBaselineCount(0);
            }
            jobCategoryStat.setBaselineCount(jobCategoryStat.getBaselineCount() + 1);
            
            // 累加认证人数
            if (hasCert == 1) {
                if (jobCategoryStat.getCertCount() == null) {
                    jobCategoryStat.setCertCount(0);
                }
                jobCategoryStat.setCertCount(jobCategoryStat.getCertCount() + 1);
            }

            jobCategoryMap.put(jobCategory, jobCategoryStat);
            maturityMap.put(aiMaturity, jobCategoryMap);
        }

        // 4. 构建树形结构结果
        List<ExpertCertStatisticsVO> maturityStatistics = new ArrayList<>();

        for (Map.Entry<String, Map<String, ExpertCertStatisticsVO>> maturityEntry : maturityMap.entrySet()) {
            String aiMaturity = maturityEntry.getKey();
            Map<String, ExpertCertStatisticsVO> jobCategoryMap = maturityEntry.getValue();

            // 创建成熟度统计对象（父节点）
            ExpertCertStatisticsVO maturityStat = new ExpertCertStatisticsVO();
            maturityStat.setAiMaturity(aiMaturity);
            
            int maturityBaselineCount = 0;
            int maturityCertCount = 0;
            List<ExpertCertStatisticsVO> jobCategoryStatistics = new ArrayList<>();

            // 遍历该成熟度下的所有职位类
            for (ExpertCertStatisticsVO jobCategoryStat : jobCategoryMap.values()) {
                // 计算职位类认证率
                if (jobCategoryStat.getBaselineCount() != null && jobCategoryStat.getBaselineCount() > 0) {
                    if (jobCategoryStat.getCertCount() == null) {
                        jobCategoryStat.setCertCount(0);
                    }
                    BigDecimal rate = new BigDecimal(jobCategoryStat.getCertCount())
                            .divide(new BigDecimal(jobCategoryStat.getBaselineCount()), 4, RoundingMode.HALF_UP)
                            .multiply(new BigDecimal(100));
                    jobCategoryStat.setCertRate(rate);
                } else {
                    jobCategoryStat.setCertRate(BigDecimal.ZERO);
                }

                // 累加成熟度总体数据
                maturityBaselineCount += jobCategoryStat.getBaselineCount();
                maturityCertCount += jobCategoryStat.getCertCount() != null ? jobCategoryStat.getCertCount() : 0;

                // 创建职位类统计对象（只包含必要字段，清除多余字段）
                ExpertCertStatisticsVO cleanJobCategoryStat = new ExpertCertStatisticsVO();
                cleanJobCategoryStat.setAiMaturity(jobCategoryStat.getAiMaturity());
                cleanJobCategoryStat.setJobCategory(jobCategoryStat.getJobCategory());
                cleanJobCategoryStat.setBaselineCount(jobCategoryStat.getBaselineCount());
                cleanJobCategoryStat.setCertCount(jobCategoryStat.getCertCount());
                cleanJobCategoryStat.setCertRate(jobCategoryStat.getCertRate());
                // 不设置 employeeNumber、hasCert、jobCategoryStatistics

                // 添加到职位类统计列表
                jobCategoryStatistics.add(cleanJobCategoryStat);
            }

            // 设置成熟度总体统计
            maturityStat.setBaselineCount(maturityBaselineCount);
            maturityStat.setCertCount(maturityCertCount);
            if (maturityBaselineCount > 0) {
                BigDecimal maturityRate = new BigDecimal(maturityCertCount)
                        .divide(new BigDecimal(maturityBaselineCount), 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal(100));
                maturityStat.setCertRate(maturityRate);
            } else {
                maturityStat.setCertRate(BigDecimal.ZERO);
            }

            // 设置职位类统计列表（子节点）
            maturityStat.setJobCategoryStatistics(jobCategoryStatistics);
            // 不设置 employeeNumber、hasCert、jobCategory（成熟度级别不需要这些字段）

            maturityStatistics.add(maturityStat);
        }

        // 5. 构建返回结果
        ExpertCertStatisticsResponseVO response = new ExpertCertStatisticsResponseVO();
        response.setMaturityStatistics(maturityStatistics);

        return response;
    }

    /**
     * 获取部门信息（供调试接口使用）
     */
    public DepartmentInfoVO getDepartmentInfo(String deptCode) {
        return departmentInfoMapper.getDepartmentByCode(deptCode);
    }

    /**
     * 获取三层部门的专家列表（供调试接口使用）
     */
    public List<ExpertCertStatisticsVO> getExpertListByLevel3(String deptCode, String deptName) {
        return expertCertStatisticsMapper.getExpertStatisticsByLevel3(deptCode, deptName);
    }

    /**
     * 获取四层部门的专家列表（供调试接口使用）
     */
    public List<ExpertCertStatisticsVO> getExpertListByLevel4(String deptCode) {
        return expertCertStatisticsMapper.getExpertStatisticsByLevel4(deptCode);
    }

    /**
     * 根据工号列表查询已通过华为研究类能力认证的员工工号
     * @param employeeNumbers 员工工号列表
     * @return 已通过认证的员工工号列表
     */
    public List<String> getCertifiedEmployeeNumbers(List<String> employeeNumbers) {
        if (employeeNumbers == null || employeeNumbers.isEmpty()) {
            return new ArrayList<>();
        }
        return expertCertStatisticsMapper.getCertifiedEmployeeNumbers(employeeNumbers);
    }

    /**
     * 根据工号列表查询获得AI任职的员工工号
     * @param employeeNumbers 员工工号列表
     * @return 获得AI任职的员工工号列表
     */
    public List<String> getQualifiedEmployeeNumbers(List<String> employeeNumbers) {
        if (employeeNumbers == null || employeeNumbers.isEmpty()) {
            return new ArrayList<>();
        }
        return expertCertStatisticsMapper.getQualifiedEmployeeNumbers(employeeNumbers);
    }

    /**
     * 根据工号列表查询已通过科目二考试的员工工号
     * @param employeeNumbers 员工工号列表
     * @return 已通过科目二的员工工号列表
     */
    public List<String> getSubject2PassedEmployeeNumbers(List<String> employeeNumbers) {
        if (employeeNumbers == null || employeeNumbers.isEmpty()) {
            return new ArrayList<>();
        }
        return expertCertStatisticsMapper.getSubject2PassedEmployeeNumbers(employeeNumbers);
    }

    /**
     * 查询全员任职认证信息
     * @param deptCode 部门ID（部门编码），当为"0"时查询云核心网产品线部门下的所有四级部门
     * @param personType 人员类型（0-全员，1-干部）
     * @return 认证和任职统计信息（包含各部门统计和总计，包含认证人数和任职人数）
     */
    public EmployeeCertStatisticsResponseVO getEmployeeCertStatistics(String deptCode, Integer personType) {
        // 干部处理流程（personType=1）
        if (personType != null && personType == 1) {
            return getCadreCertStatistics(deptCode);
        }

        // 专家处理流程（personType=2）
        if (personType != null && personType == 2) {
            return getExpertCertStatisticsForEmployee(deptCode);
        }

        // 全员处理流程（personType=0）
        List<DepartmentInfoVO> targetDepts;
        Integer currentLevel;
        String actualDeptCode;

        // 特殊处理：当 deptCode 为 "0" 时，查询云核心网产品线部门下的所有四级部门
        if ("0".equals(deptCode)) {
            // 查询云核心网产品线部门下的所有四级部门
            targetDepts = departmentInfoMapper.getLevel4DepartmentsUnderLevel2(DepartmentConstants.CLOUD_CORE_NETWORK_DEPT_CODE);
            if (targetDepts == null || targetDepts.isEmpty()) {
                // 如果没有四级部门，返回空统计
                EmployeeCertStatisticsResponseVO response = new EmployeeCertStatisticsResponseVO();
                response.setDepartmentStatistics(new ArrayList<>());
                DepartmentCertStatisticsVO total = new DepartmentCertStatisticsVO();
                total.setDeptCode("总计");
                total.setDeptName("总计");
                total.setTotalCount(0);
                total.setCertifiedCount(0);
                total.setQualifiedCount(0);
                total.setCertRate(BigDecimal.ZERO);
                total.setQualifiedRate(BigDecimal.ZERO);
                response.setTotalStatistics(total);
                return response;
            }
            // 查询四级部门的员工，云核心网产品线是二级部门，需要查询其下的四级部门
            currentLevel = 2; // 云核心网产品线是二级部门（虽然不会用到，但需要初始化）
            actualDeptCode = DepartmentConstants.CLOUD_CORE_NETWORK_DEPT_CODE;
        } else {
            // 1. 查询部门信息
            DepartmentInfoVO deptInfo = departmentInfoMapper.getDepartmentByCode(deptCode);
            if (deptInfo == null) {
                throw new IllegalArgumentException("部门不存在：" + deptCode);
            }

            // 2. 查询下一层子部门列表
            targetDepts = departmentInfoMapper.getChildDepartments(deptCode);
            if (targetDepts == null || targetDepts.isEmpty()) {
                // 如果没有子部门，返回空统计
                EmployeeCertStatisticsResponseVO response = new EmployeeCertStatisticsResponseVO();
                response.setDepartmentStatistics(new ArrayList<>());
                DepartmentCertStatisticsVO total = new DepartmentCertStatisticsVO();
                total.setDeptCode("总计");
                total.setDeptName("总计");
                total.setTotalCount(0);
                total.setCertifiedCount(0);
                total.setQualifiedCount(0);
                total.setCertRate(BigDecimal.ZERO);
                total.setQualifiedRate(BigDecimal.ZERO);
                response.setTotalStatistics(total);
                return response;
            }

            // 3. 根据当前部门层级，确定查询的部门层级
            currentLevel = Integer.parseInt(deptInfo.getDeptLevel());
            actualDeptCode = deptCode;
        }

        // 4. 批量查询所有子部门的统计数据（从 t_employee 表，只统计研发族）
        List<DepartmentCertStatisticsVO> statisticsList;
        if ("0".equals(deptCode)) {
            // 特殊处理：查询二级部门（云核心网产品线）下的四级部门
            statisticsList = employeeMapper.getLevel4DeptStatisticsUnderLevel2(actualDeptCode);
        } else {
            // 普通查询：查询当前部门下的下一层部门
            statisticsList = employeeMapper.getDeptStatisticsByLevel(currentLevel, actualDeptCode);
        }
        
        // 5. 将统计结果转换为 Map，key 为 deptCode，方便快速查找
        Map<String, DepartmentCertStatisticsVO> statisticsMap = new HashMap<>();
        if (statisticsList != null) {
            for (DepartmentCertStatisticsVO stat : statisticsList) {
                if (stat.getDeptCode() != null && !stat.getDeptCode().trim().isEmpty()) {
                    statisticsMap.put(stat.getDeptCode(), stat);
                }
            }
        }

        // 6. 遍历目标部门列表，组装统计数据
        List<DepartmentCertStatisticsVO> departmentStats = new ArrayList<>();

        for (DepartmentInfoVO dept : targetDepts) {
            if (dept.getDeptCode() == null || dept.getDeptCode().trim().isEmpty()) {
                continue;
            }

            // 过滤掉不需要展示的部门：C Lab（模块）和云核心网产品组合与生命周期管理部
            String deptName = dept.getDeptName();
            if (deptName != null && (deptName.equals("C Lab（模块）") || deptName.equals("云核心网产品组合与生命周期管理部"))) {
                continue;
            }

            // 6.1 从 Map 中获取该部门的统计数据，如果没有则使用默认值0
            DepartmentCertStatisticsVO stat = statisticsMap.get(dept.getDeptCode());
            int deptTotalCount = (stat != null && stat.getTotalCount() != null) ? stat.getTotalCount() : 0;
            int deptCertifiedCount = (stat != null && stat.getCertifiedCount() != null) ? stat.getCertifiedCount() : 0;
            int deptQualifiedCount = (stat != null && stat.getQualifiedCount() != null) ? stat.getQualifiedCount() : 0;

            // 6.2 计算该部门的认证率
            BigDecimal deptCertRate = BigDecimal.ZERO;
            if (deptTotalCount > 0) {
                BigDecimal total = new BigDecimal(deptTotalCount);
                BigDecimal certified = new BigDecimal(deptCertifiedCount);
                deptCertRate = certified.divide(total, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal(100));
            }

            // 6.3 计算该部门的任职率
            BigDecimal deptQualifiedRate = BigDecimal.ZERO;
            if (deptTotalCount > 0) {
                BigDecimal total = new BigDecimal(deptTotalCount);
                BigDecimal qualified = new BigDecimal(deptQualifiedCount);
                deptQualifiedRate = qualified.divide(total, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal(100));
            }

            // 6.4 构建部门统计对象
            DepartmentCertStatisticsVO deptStat = new DepartmentCertStatisticsVO();
            deptStat.setDeptCode(dept.getDeptCode());
            deptStat.setDeptName(dept.getDeptName());
            deptStat.setTotalCount(deptTotalCount);
            deptStat.setCertifiedCount(deptCertifiedCount);
            deptStat.setQualifiedCount(deptQualifiedCount);
            deptStat.setCertRate(deptCertRate);
            deptStat.setQualifiedRate(deptQualifiedRate);

            departmentStats.add(deptStat);
        }

        // 7. 计算总计数据
        DepartmentCertStatisticsVO totalStatistics = new DepartmentCertStatisticsVO();
        totalStatistics.setDeptCode("总计");
        totalStatistics.setDeptName("总计");
        
        if ("0".equals(deptCode)) {
            // 当 deptCode="0" 时，总计直接统计整个表中的数据
            DepartmentCertStatisticsVO totalStat = employeeMapper.getTotalStatisticsForAllEmployees();
            if (totalStat != null) {
                totalStatistics.setTotalCount(totalStat.getTotalCount() != null ? totalStat.getTotalCount() : 0);
                totalStatistics.setCertifiedCount(totalStat.getCertifiedCount() != null ? totalStat.getCertifiedCount() : 0);
                totalStatistics.setQualifiedCount(totalStat.getQualifiedCount() != null ? totalStat.getQualifiedCount() : 0);
            } else {
                totalStatistics.setTotalCount(0);
                totalStatistics.setCertifiedCount(0);
                totalStatistics.setQualifiedCount(0);
            }
        } else {
            // 普通情况：直接查询当前部门及其所有子部门的研发族人员
            DepartmentCertStatisticsVO totalStat = employeeMapper.getTotalDeptStatisticsByLevel(currentLevel, actualDeptCode);
            if (totalStat != null) {
                totalStatistics.setTotalCount(totalStat.getTotalCount() != null ? totalStat.getTotalCount() : 0);
                totalStatistics.setCertifiedCount(totalStat.getCertifiedCount() != null ? totalStat.getCertifiedCount() : 0);
                totalStatistics.setQualifiedCount(totalStat.getQualifiedCount() != null ? totalStat.getQualifiedCount() : 0);
            } else {
                totalStatistics.setTotalCount(0);
                totalStatistics.setCertifiedCount(0);
                totalStatistics.setQualifiedCount(0);
            }
        }

        // 8. 计算总计的认证率
        BigDecimal totalCertRate = BigDecimal.ZERO;
        if (totalStatistics.getTotalCount() != null && totalStatistics.getTotalCount() > 0) {
            BigDecimal total = new BigDecimal(totalStatistics.getTotalCount());
            BigDecimal certified = new BigDecimal(totalStatistics.getCertifiedCount() != null ? totalStatistics.getCertifiedCount() : 0);
            totalCertRate = certified.divide(total, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(100));
        }
        totalStatistics.setCertRate(totalCertRate);

        // 9. 计算总计的任职率
        BigDecimal totalQualifiedRate = BigDecimal.ZERO;
        if (totalStatistics.getTotalCount() != null && totalStatistics.getTotalCount() > 0) {
            BigDecimal total = new BigDecimal(totalStatistics.getTotalCount());
            BigDecimal qualified = new BigDecimal(totalStatistics.getQualifiedCount() != null ? totalStatistics.getQualifiedCount() : 0);
            totalQualifiedRate = qualified.divide(total, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(100));
        }
        totalStatistics.setQualifiedRate(totalQualifiedRate);

        // 8. 构建返回结果
        EmployeeCertStatisticsResponseVO response = new EmployeeCertStatisticsResponseVO();
        response.setDepartmentStatistics(departmentStats);
        response.setTotalStatistics(totalStatistics);

        return response;
    }

    /**
     * 查询专家任职认证信息
     * @param deptCode 部门ID（部门编码）
     * @return 认证和任职统计信息（包含各部门统计和总计，包含认证人数和任职人数）
     */
    private EmployeeCertStatisticsResponseVO getExpertCertStatisticsForEmployee(String deptCode) {
        List<DepartmentInfoVO> targetDepts;
        Integer queryLevel;

        // 特殊处理：当 deptCode 为 "0" 时，查询云核心网产品线部门下的所有四级部门
        if ("0".equals(deptCode)) {
            // 查询云核心网产品线部门下的所有四级部门
            targetDepts = departmentInfoMapper.getLevel4DepartmentsUnderLevel2(DepartmentConstants.CLOUD_CORE_NETWORK_DEPT_CODE);
            if (targetDepts == null || targetDepts.isEmpty()) {
                // 如果没有四级部门，返回空统计
                EmployeeCertStatisticsResponseVO response = new EmployeeCertStatisticsResponseVO();
                response.setDepartmentStatistics(new ArrayList<>());
                DepartmentCertStatisticsVO total = new DepartmentCertStatisticsVO();
                total.setDeptCode("总计");
                total.setDeptName("总计");
                total.setTotalCount(0);
                total.setCertifiedCount(0);
                total.setQualifiedCount(0);
                total.setCertRate(BigDecimal.ZERO);
                total.setQualifiedRate(BigDecimal.ZERO);
                response.setTotalStatistics(total);
                return response;
            }
            // 查询四级部门的专家，使用 deptLevel=4
            queryLevel = 4;
        } else {
            // 1. 查询部门信息
            DepartmentInfoVO deptInfo = departmentInfoMapper.getDepartmentByCode(deptCode);
            if (deptInfo == null) {
                throw new IllegalArgumentException("部门不存在：" + deptCode);
            }

            // 2. 查询下一层子部门列表
            targetDepts = departmentInfoMapper.getChildDepartments(deptCode);
            if (targetDepts == null || targetDepts.isEmpty()) {
                // 如果没有子部门，返回空统计
                EmployeeCertStatisticsResponseVO response = new EmployeeCertStatisticsResponseVO();
                response.setDepartmentStatistics(new ArrayList<>());
                DepartmentCertStatisticsVO total = new DepartmentCertStatisticsVO();
                total.setDeptCode("总计");
                total.setDeptName("总计");
                total.setTotalCount(0);
                total.setCertifiedCount(0);
                total.setQualifiedCount(0);
                total.setCertRate(BigDecimal.ZERO);
                total.setQualifiedRate(BigDecimal.ZERO);
                response.setTotalStatistics(total);
                return response;
            }

            // 3. 根据当前部门层级，确定查询的部门层级（下一层）
            Integer currentLevel = Integer.parseInt(deptInfo.getDeptLevel());
            queryLevel = currentLevel + 1;
        }

        // 4. 遍历每个部门，分别统计
        List<DepartmentCertStatisticsVO> departmentStats = new ArrayList<>();
        int totalCountSum = 0;
        int certifiedCountSum = 0;
        int qualifiedCountSum = 0;

        for (DepartmentInfoVO dept : targetDepts) {
            if (dept.getDeptCode() == null || dept.getDeptCode().trim().isEmpty()) {
                continue;
            }

            // 过滤掉不需要展示的部门：C Lab（模块）和云核心网产品组合与生命周期管理部
            String deptName = dept.getDeptName();
            if (deptName != null && (deptName.equals("C Lab（模块）") || deptName.equals("云核心网产品组合与生命周期管理部"))) {
                continue;
            }

            // 4.1 查询该部门下的专家工号列表
            List<String> deptIdList = new ArrayList<>();
            deptIdList.add(dept.getDeptCode());
            List<String> employeeNumbers = expertMapper.getExpertNumbersByDeptLevel(queryLevel, deptIdList);

            int deptTotalCount = (employeeNumbers != null) ? employeeNumbers.size() : 0;

            // 4.2 查询该部门已通过认证的专家工号列表
            int deptCertifiedCount = 0;
            if (employeeNumbers != null && !employeeNumbers.isEmpty()) {
                List<String> certifiedNumbers = getCertifiedEmployeeNumbers(employeeNumbers);
                deptCertifiedCount = (certifiedNumbers != null) ? certifiedNumbers.size() : 0;
            }

            // 4.3 查询该部门已获得任职的专家工号列表
            int deptQualifiedCount = 0;
            if (employeeNumbers != null && !employeeNumbers.isEmpty()) {
                List<String> qualifiedNumbers = getQualifiedEmployeeNumbers(employeeNumbers);
                deptQualifiedCount = (qualifiedNumbers != null) ? qualifiedNumbers.size() : 0;
            }

            // 4.4 计算该部门的认证率
            BigDecimal deptCertRate = BigDecimal.ZERO;
            if (deptTotalCount > 0) {
                BigDecimal total = new BigDecimal(deptTotalCount);
                BigDecimal certified = new BigDecimal(deptCertifiedCount);
                deptCertRate = certified.divide(total, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal(100));
            }

            // 4.5 计算该部门的任职率
            BigDecimal deptQualifiedRate = BigDecimal.ZERO;
            if (deptTotalCount > 0) {
                BigDecimal total = new BigDecimal(deptTotalCount);
                BigDecimal qualified = new BigDecimal(deptQualifiedCount);
                deptQualifiedRate = qualified.divide(total, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal(100));
            }

            // 4.6 构建部门统计对象
            DepartmentCertStatisticsVO deptStat = new DepartmentCertStatisticsVO();
            deptStat.setDeptCode(dept.getDeptCode());
            deptStat.setDeptName(dept.getDeptName());
            deptStat.setTotalCount(deptTotalCount);
            deptStat.setCertifiedCount(deptCertifiedCount);
            deptStat.setQualifiedCount(deptQualifiedCount);
            deptStat.setCertRate(deptCertRate);
            deptStat.setQualifiedRate(deptQualifiedRate);

            departmentStats.add(deptStat);

            // 4.7 累加总计
            totalCountSum += deptTotalCount;
            certifiedCountSum += deptCertifiedCount;
            qualifiedCountSum += deptQualifiedCount;
        }

        // 5. 计算总计的认证率
        BigDecimal totalCertRate = BigDecimal.ZERO;
        if (totalCountSum > 0) {
            BigDecimal total = new BigDecimal(totalCountSum);
            BigDecimal certified = new BigDecimal(certifiedCountSum);
            totalCertRate = certified.divide(total, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(100));
        }

        // 6. 计算总计的任职率
        BigDecimal totalQualifiedRate = BigDecimal.ZERO;
        if (totalCountSum > 0) {
            BigDecimal total = new BigDecimal(totalCountSum);
            BigDecimal qualified = new BigDecimal(qualifiedCountSum);
            totalQualifiedRate = qualified.divide(total, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(100));
        }

        // 7. 构建总计统计对象
        DepartmentCertStatisticsVO totalStatistics = new DepartmentCertStatisticsVO();
        totalStatistics.setDeptCode("总计");
        totalStatistics.setDeptName("总计");
        totalStatistics.setTotalCount(totalCountSum);
        totalStatistics.setCertifiedCount(certifiedCountSum);
        totalStatistics.setQualifiedCount(qualifiedCountSum);
        totalStatistics.setCertRate(totalCertRate);
        totalStatistics.setQualifiedRate(totalQualifiedRate);

        // 8. 构建返回结果
        EmployeeCertStatisticsResponseVO response = new EmployeeCertStatisticsResponseVO();
        response.setDepartmentStatistics(departmentStats);
        response.setTotalStatistics(totalStatistics);

        return response;
    }

    /**
     * 查询干部任职认证信息
     * @param deptCode 部门ID（部门编码）
     * @return 认证和任职统计信息（包含各部门统计和总计，包含认证人数和任职人数）
     */
    private EmployeeCertStatisticsResponseVO getCadreCertStatistics(String deptCode) {
        List<DepartmentInfoVO> childDepts;
        
        // 特殊处理：当 deptCode 为 "0" 时，查询云核心网产品线部门下的所有四级部门
        if ("0".equals(deptCode)) {
            // 查询云核心网产品线部门下的所有四级部门
            childDepts = departmentInfoMapper.getLevel4DepartmentsUnderLevel2(DepartmentConstants.CLOUD_CORE_NETWORK_DEPT_CODE);
            if (childDepts == null || childDepts.isEmpty()) {
                // 如果没有四级部门，返回空统计
                EmployeeCertStatisticsResponseVO response = new EmployeeCertStatisticsResponseVO();
                response.setDepartmentStatistics(new ArrayList<>());
                DepartmentCertStatisticsVO total = new DepartmentCertStatisticsVO();
                total.setDeptCode("总计");
                total.setDeptName("总计");
                total.setTotalCount(0);
                total.setCertifiedCount(0);
                total.setQualifiedCount(0);
                total.setCertRate(BigDecimal.ZERO);
                total.setQualifiedRate(BigDecimal.ZERO);
                response.setTotalStatistics(total);
                return response;
            }
        } else {
            // 1. 查询部门信息
            DepartmentInfoVO deptInfo = departmentInfoMapper.getDepartmentByCode(deptCode);
            if (deptInfo == null) {
                throw new IllegalArgumentException("部门不存在：" + deptCode);
            }

            // 2. 查询下一级部门列表
            childDepts = departmentInfoMapper.getChildDepartments(deptCode);
            if (childDepts == null || childDepts.isEmpty()) {
                // 如果没有子部门，返回空统计
                EmployeeCertStatisticsResponseVO response = new EmployeeCertStatisticsResponseVO();
                response.setDepartmentStatistics(new ArrayList<>());
                DepartmentCertStatisticsVO total = new DepartmentCertStatisticsVO();
                total.setDeptCode("总计");
                total.setDeptName("总计");
                total.setTotalCount(0);
                total.setCertifiedCount(0);
                total.setQualifiedCount(0);
                total.setCertRate(BigDecimal.ZERO);
                total.setQualifiedRate(BigDecimal.ZERO);
                response.setTotalStatistics(total);
                return response;
            }
        }

        // 3. 遍历每个下级部门，查询所有子部门并统计干部信息
        List<DepartmentCertStatisticsVO> departmentStats = new ArrayList<>();
        int totalCountSum = 0;
        int certifiedCountSum = 0;
        int qualifiedCountSum = 0;

        for (DepartmentInfoVO childDept : childDepts) {
            if (childDept.getDeptCode() == null || childDept.getDeptCode().trim().isEmpty()) {
                continue;
            }

            // 过滤掉不需要展示的部门：C Lab（模块）和云核心网产品组合与生命周期管理部
            String deptName = childDept.getDeptName();
            if (deptName != null && (deptName.equals("C Lab（模块）") || deptName.equals("云核心网产品组合与生命周期管理部"))) {
                continue;
            }

            // 3.1 查询该下级部门的所有子部门（包括所有层级的子部门）
            List<DepartmentInfoVO> allSubDepts = departmentInfoMapper.getAllSubDepartments(childDept.getDeptCode());
            
            // 构造部门编码列表（包括当前下级部门本身和所有子部门）
            List<String> deptCodeList = new ArrayList<>();
            deptCodeList.add(childDept.getDeptCode());
            if (allSubDepts != null && !allSubDepts.isEmpty()) {
                for (DepartmentInfoVO subDept : allSubDepts) {
                    if (subDept.getDeptCode() != null && !subDept.getDeptCode().trim().isEmpty()) {
                        deptCodeList.add(subDept.getDeptCode());
                    }
                }
            }

            // 3.2 从干部表中查询属于这些部门的干部工号
            List<String> cadreEmployeeNumbers = cadreMapper.getCadreEmployeeNumbersByDeptCodes(deptCodeList);
            int deptTotalCount = (cadreEmployeeNumbers != null) ? cadreEmployeeNumbers.size() : 0;

            // 3.3 查询该部门已通过认证的干部工号列表
            int deptCertifiedCount = 0;
            if (cadreEmployeeNumbers != null && !cadreEmployeeNumbers.isEmpty()) {
                List<String> certifiedNumbers = getCertifiedEmployeeNumbers(cadreEmployeeNumbers);
                deptCertifiedCount = (certifiedNumbers != null) ? certifiedNumbers.size() : 0;
            }

            // 3.4 查询该部门已获得任职的干部工号列表（直接使用干部工号查询，不去除首字母）
            int deptQualifiedCount = 0;
            if (cadreEmployeeNumbers != null && !cadreEmployeeNumbers.isEmpty()) {
                // 直接使用干部工号查询任职信息，不去除首字母
                List<String> qualifiedNumbers = getQualifiedEmployeeNumbers(cadreEmployeeNumbers);
                deptQualifiedCount = (qualifiedNumbers != null) ? qualifiedNumbers.size() : 0;
            }

            // 3.5 计算该部门的认证率
            BigDecimal deptCertRate = BigDecimal.ZERO;
            if (deptTotalCount > 0) {
                BigDecimal total = new BigDecimal(deptTotalCount);
                BigDecimal certified = new BigDecimal(deptCertifiedCount);
                deptCertRate = certified.divide(total, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal(100));
            }

            // 3.6 计算该部门的任职率
            BigDecimal deptQualifiedRate = BigDecimal.ZERO;
            if (deptTotalCount > 0) {
                BigDecimal total = new BigDecimal(deptTotalCount);
                BigDecimal qualified = new BigDecimal(deptQualifiedCount);
                deptQualifiedRate = qualified.divide(total, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal(100));
            }

            // 3.7 构建部门统计对象
            DepartmentCertStatisticsVO deptStat = new DepartmentCertStatisticsVO();
            deptStat.setDeptCode(childDept.getDeptCode());
            deptStat.setDeptName(childDept.getDeptName());
            deptStat.setTotalCount(deptTotalCount);
            deptStat.setCertifiedCount(deptCertifiedCount);
            deptStat.setQualifiedCount(deptQualifiedCount);
            deptStat.setCertRate(deptCertRate);
            deptStat.setQualifiedRate(deptQualifiedRate);

            departmentStats.add(deptStat);

            // 3.8 累加总计
            totalCountSum += deptTotalCount;
            certifiedCountSum += deptCertifiedCount;
            qualifiedCountSum += deptQualifiedCount;
        }

        // 4. 计算总计的认证率
        BigDecimal totalCertRate = BigDecimal.ZERO;
        if (totalCountSum > 0) {
            BigDecimal total = new BigDecimal(totalCountSum);
            BigDecimal certified = new BigDecimal(certifiedCountSum);
            totalCertRate = certified.divide(total, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(100));
        }

        // 5. 计算总计的任职率
        BigDecimal totalQualifiedRate = BigDecimal.ZERO;
        if (totalCountSum > 0) {
            BigDecimal total = new BigDecimal(totalCountSum);
            BigDecimal qualified = new BigDecimal(qualifiedCountSum);
            totalQualifiedRate = qualified.divide(total, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(100));
        }

        // 6. 构建总计统计对象
        DepartmentCertStatisticsVO totalStatistics = new DepartmentCertStatisticsVO();
        totalStatistics.setDeptCode("总计");
        totalStatistics.setDeptName("总计");
        totalStatistics.setTotalCount(totalCountSum);
        totalStatistics.setCertifiedCount(certifiedCountSum);
        totalStatistics.setQualifiedCount(qualifiedCountSum);
        totalStatistics.setCertRate(totalCertRate);
        totalStatistics.setQualifiedRate(totalQualifiedRate);

        // 7. 构建返回结果
        EmployeeCertStatisticsResponseVO response = new EmployeeCertStatisticsResponseVO();
        response.setDepartmentStatistics(departmentStats);
        response.setTotalStatistics(totalStatistics);

        return response;
    }

    /**
     * 按职位类统计部门下不同职位类人数中的认证和任职人数
     * @param deptCode 部门ID（部门编码），当为"0"时查询云核心网产品线部门下的所有四级部门
     * @param personType 人员类型（0-全员，1-干部）
     * @return 按职位类统计的认证和任职信息（包含认证人数和任职人数）
     */
    public CompetenceCategoryCertStatisticsResponseVO getCompetenceCategoryCertStatistics(String deptCode, Integer personType) {
        // 干部处理流程（personType=1）
        if (personType != null && personType == 1) {
            return getCadreCompetenceCategoryCertStatistics(deptCode);
        }

        // 专家处理流程（personType=2）
        if (personType != null && personType == 2) {
            return getExpertCompetenceCategoryCertStatistics(deptCode);
        }

        // 全员处理流程（personType=0）
        return getEmployeeCompetenceCategoryCertStatistics(deptCode);
    }

    /**
     * 从职位族字符串中提取职位类
     * 格式：职位族-职位类-职位子类，需要提取中间的职位类
     * @param jobCategory 职位族字符串
     * @return 职位类，如果格式不正确则返回"未知"
     */
    private String extractCompetenceCategory(String jobCategory) {
        if (jobCategory == null || jobCategory.trim().isEmpty()) {
            return "未知";
        }
        
        String[] parts = jobCategory.split("-");
        if (parts.length >= 2) {
            // 提取中间的职位类（第二个部分）
            return parts[1].trim();
        } else if (parts.length == 1) {
            // 如果只有一个部分，直接返回
            return parts[0].trim();
        } else {
            return "未知";
        }
    }

    /**
     * 按职位类统计全员任职认证信息
     * @param deptCode 部门ID（部门编码）
     * @return 按职位类统计的认证和任职信息（包含认证人数和任职人数）
     */
    private CompetenceCategoryCertStatisticsResponseVO getEmployeeCompetenceCategoryCertStatistics(String deptCode) {
        List<DepartmentInfoVO> targetDepts;
        Integer currentLevel;
        String actualDeptCode;
        DepartmentInfoVO deptInfo;

        // 特殊处理：当 deptCode 为 "0" 时，查询云核心网产品线部门下的所有四级部门
        if ("0".equals(deptCode)) {
            // 查询云核心网产品线部门下的所有四级部门
            targetDepts = departmentInfoMapper.getLevel4DepartmentsUnderLevel2(DepartmentConstants.CLOUD_CORE_NETWORK_DEPT_CODE);
            if (targetDepts == null || targetDepts.isEmpty()) {
                // 如果没有四级部门，返回空统计
                CompetenceCategoryCertStatisticsResponseVO response = new CompetenceCategoryCertStatisticsResponseVO();
                response.setDeptCode(deptCode);
                response.setDeptName("云核心网产品线");
                response.setCategoryStatistics(new ArrayList<>());
                CompetenceCategoryCertStatisticsVO total = new CompetenceCategoryCertStatisticsVO();
                total.setCompetenceCategory("总计");
                total.setTotalCount(0);
                total.setCertifiedCount(0);
                total.setQualifiedCount(0);
                total.setCertRate(BigDecimal.ZERO);
                total.setQualifiedRate(BigDecimal.ZERO);
                response.setTotalStatistics(total);
                return response;
            }
            currentLevel = 2; // 云核心网产品线是二级部门
            actualDeptCode = DepartmentConstants.CLOUD_CORE_NETWORK_DEPT_CODE;
            deptInfo = departmentInfoMapper.getDepartmentByCode(actualDeptCode);
            if (deptInfo == null) {
                throw new IllegalArgumentException("部门不存在：" + actualDeptCode);
            }
        } else {
            // 1. 查询部门信息
            deptInfo = departmentInfoMapper.getDepartmentByCode(deptCode);
            if (deptInfo == null) {
                throw new IllegalArgumentException("部门不存在：" + deptCode);
            }

            // 2. 查询下一层子部门列表
            targetDepts = departmentInfoMapper.getChildDepartments(deptCode);
            if (targetDepts == null || targetDepts.isEmpty()) {
                // 如果没有子部门，返回空统计
                CompetenceCategoryCertStatisticsResponseVO response = new CompetenceCategoryCertStatisticsResponseVO();
                response.setDeptCode(deptCode);
                response.setDeptName(deptInfo.getDeptName());
                response.setCategoryStatistics(new ArrayList<>());
                CompetenceCategoryCertStatisticsVO total = new CompetenceCategoryCertStatisticsVO();
                total.setCompetenceCategory("总计");
                total.setTotalCount(0);
                total.setCertifiedCount(0);
                total.setQualifiedCount(0);
                total.setCertRate(BigDecimal.ZERO);
                total.setQualifiedRate(BigDecimal.ZERO);
                response.setTotalStatistics(total);
                return response;
            }

            // 3. 根据当前部门层级，确定查询的部门层级
            currentLevel = Integer.parseInt(deptInfo.getDeptLevel());
            actualDeptCode = deptCode;
        }

        // 4. 批量查询明细统计数据（下一层部门的统计数据）
        List<CompetenceCategoryDeptStatisticsVO> statisticsList;
        if ("0".equals(deptCode)) {
            // 特殊处理：查询二级部门（云核心网产品线）下的四级部门
            statisticsList = employeeMapper.getLevel4CompetenceCategoryStatisticsUnderLevel2(actualDeptCode);
        } else {
            // 普通查询：查询当前部门下的下一层部门
            statisticsList = employeeMapper.getCompetenceCategoryStatisticsByLevel(currentLevel, actualDeptCode);
        }

        // 5. 按职位类分组统计（跨所有下一层部门）
        Map<String, CompetenceCategoryCertStatisticsVO> categoryMap = new HashMap<>();
        if (statisticsList != null && !statisticsList.isEmpty()) {
            for (CompetenceCategoryDeptStatisticsVO stat : statisticsList) {
                if (stat.getCompetenceCategory() == null || stat.getCompetenceCategory().trim().isEmpty()) {
                    continue;
                }

                String category = stat.getCompetenceCategory();
                CompetenceCategoryCertStatisticsVO categoryStat = categoryMap.getOrDefault(category, new CompetenceCategoryCertStatisticsVO());
                categoryStat.setCompetenceCategory(category);

                // 累加统计数据
                if (categoryStat.getTotalCount() == null) {
                    categoryStat.setTotalCount(0);
                }
                if (stat.getTotalCount() != null) {
                    categoryStat.setTotalCount(categoryStat.getTotalCount() + stat.getTotalCount());
                }

                if (categoryStat.getCertifiedCount() == null) {
                    categoryStat.setCertifiedCount(0);
                }
                if (stat.getCertifiedCount() != null) {
                    categoryStat.setCertifiedCount(categoryStat.getCertifiedCount() + stat.getCertifiedCount());
                }

                if (categoryStat.getQualifiedCount() == null) {
                    categoryStat.setQualifiedCount(0);
                }
                if (stat.getQualifiedCount() != null) {
                    categoryStat.setQualifiedCount(categoryStat.getQualifiedCount() + stat.getQualifiedCount());
                }

                categoryMap.put(category, categoryStat);
            }
        }

        // 6. 计算每个职位类的认证率和任职率
        List<CompetenceCategoryCertStatisticsVO> categoryStats = new ArrayList<>();
        for (CompetenceCategoryCertStatisticsVO categoryStat : categoryMap.values()) {
            if (categoryStat.getTotalCount() != null && categoryStat.getTotalCount() > 0) {
                if (categoryStat.getCertifiedCount() == null) {
                    categoryStat.setCertifiedCount(0);
                }
                // 计算认证率
                BigDecimal certRate = new BigDecimal(categoryStat.getCertifiedCount())
                        .divide(new BigDecimal(categoryStat.getTotalCount()), 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal(100));
                categoryStat.setCertRate(certRate);

                // 计算任职率
                if (categoryStat.getQualifiedCount() == null) {
                    categoryStat.setQualifiedCount(0);
                }
                BigDecimal qualifiedRate = new BigDecimal(categoryStat.getQualifiedCount())
                        .divide(new BigDecimal(categoryStat.getTotalCount()), 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal(100));
                categoryStat.setQualifiedRate(qualifiedRate);
            } else {
                categoryStat.setCertRate(BigDecimal.ZERO);
                categoryStat.setQualifiedRate(BigDecimal.ZERO);
            }
            categoryStats.add(categoryStat);
        }

        // 7. 查询总计数据
        DepartmentCertStatisticsVO totalStat;
        if ("0".equals(deptCode)) {
            // 当 deptCode="0" 时，总计直接统计整个表中的数据
            totalStat = employeeMapper.getTotalCompetenceCategoryStatisticsForAllEmployees();
        } else {
            // 普通情况：查询当前部门及其所有子部门的研发族人员
            totalStat = employeeMapper.getTotalCompetenceCategoryStatisticsByLevel(currentLevel, actualDeptCode);
        }

        // 8. 构建总计统计对象
        CompetenceCategoryCertStatisticsVO totalStatistics = new CompetenceCategoryCertStatisticsVO();
        totalStatistics.setCompetenceCategory("总计");
        if (totalStat != null) {
            totalStatistics.setTotalCount(totalStat.getTotalCount() != null ? totalStat.getTotalCount() : 0);
            totalStatistics.setCertifiedCount(totalStat.getCertifiedCount() != null ? totalStat.getCertifiedCount() : 0);
            totalStatistics.setQualifiedCount(totalStat.getQualifiedCount() != null ? totalStat.getQualifiedCount() : 0);
        } else {
            totalStatistics.setTotalCount(0);
            totalStatistics.setCertifiedCount(0);
            totalStatistics.setQualifiedCount(0);
        }

        // 9. 计算总计的认证率
        BigDecimal totalCertRate = BigDecimal.ZERO;
        if (totalStatistics.getTotalCount() != null && totalStatistics.getTotalCount() > 0) {
            BigDecimal total = new BigDecimal(totalStatistics.getTotalCount());
            BigDecimal certified = new BigDecimal(totalStatistics.getCertifiedCount() != null ? totalStatistics.getCertifiedCount() : 0);
            totalCertRate = certified.divide(total, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(100));
        }
        totalStatistics.setCertRate(totalCertRate);

        // 10. 计算总计的任职率
        BigDecimal totalQualifiedRate = BigDecimal.ZERO;
        if (totalStatistics.getTotalCount() != null && totalStatistics.getTotalCount() > 0) {
            BigDecimal total = new BigDecimal(totalStatistics.getTotalCount());
            BigDecimal qualified = new BigDecimal(totalStatistics.getQualifiedCount() != null ? totalStatistics.getQualifiedCount() : 0);
            totalQualifiedRate = qualified.divide(total, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(100));
        }
        totalStatistics.setQualifiedRate(totalQualifiedRate);

        // 11. 构建返回结果
        CompetenceCategoryCertStatisticsResponseVO response = new CompetenceCategoryCertStatisticsResponseVO();
        response.setDeptCode(deptCode);
        response.setDeptName(deptInfo.getDeptName());
        response.setCategoryStatistics(categoryStats);
        response.setTotalStatistics(totalStatistics);

        return response;
    }

    /**
     * 按职位类统计干部任职认证信息
     * @param deptCode 部门ID（部门编码）
     * @return 按职位类统计的认证和任职信息（包含认证人数和任职人数）
     */
    private CompetenceCategoryCertStatisticsResponseVO getCadreCompetenceCategoryCertStatistics(String deptCode) {
        // 1. 查询部门信息
        DepartmentInfoVO deptInfo = departmentInfoMapper.getDepartmentByCode(deptCode);
        if (deptInfo == null) {
            throw new IllegalArgumentException("部门不存在：" + deptCode);
        }

        // 2. 查询所有层级子部门信息
        List<DepartmentInfoVO> allSubDepts = departmentInfoMapper.getAllSubDepartments(deptCode);
        
        // 构造部门编码列表（包括本部门本身和所有子部门）
        List<String> deptCodeList = new ArrayList<>();
        deptCodeList.add(deptCode);
        if (allSubDepts != null && !allSubDepts.isEmpty()) {
            for (DepartmentInfoVO subDept : allSubDepts) {
                if (subDept.getDeptCode() != null && !subDept.getDeptCode().trim().isEmpty()) {
                    deptCodeList.add(subDept.getDeptCode());
                }
            }
        }

        // 3. 从干部表中查询属于这些部门的干部工号和职位类
        List<EmployeeWithCategoryVO> allCadres = cadreMapper.getCadreEmployeesWithCategoryByDeptCodes(deptCodeList);

        if (allCadres == null || allCadres.isEmpty()) {
            // 如果没有干部，返回空统计
            CompetenceCategoryCertStatisticsResponseVO response = new CompetenceCategoryCertStatisticsResponseVO();
            response.setDeptCode(deptCode);
            response.setDeptName(deptInfo.getDeptName());
            response.setCategoryStatistics(new ArrayList<>());
            CompetenceCategoryCertStatisticsVO total = new CompetenceCategoryCertStatisticsVO();
            total.setCompetenceCategory("总计");
            total.setTotalCount(0);
            total.setCertifiedCount(0);
            total.setQualifiedCount(0);
            total.setCertRate(BigDecimal.ZERO);
            total.setQualifiedRate(BigDecimal.ZERO);
            response.setTotalStatistics(total);
            return response;
        }

        // 4. 提取所有干部工号
        List<String> cadreEmployeeNumbers = allCadres.stream()
                .map(EmployeeWithCategoryVO::getEmployeeNumber)
                .filter(num -> num != null && !num.trim().isEmpty())
                .distinct()
                .collect(Collectors.toList());

        // 5. 查询已通过认证的干部工号列表
        List<String> certifiedNumbers = new ArrayList<>();
        if (!cadreEmployeeNumbers.isEmpty()) {
            certifiedNumbers = getCertifiedEmployeeNumbers(cadreEmployeeNumbers);
        }

        // 5.1 查询已获得任职的干部工号列表（直接使用干部工号查询）
        List<String> qualifiedNumbers = new ArrayList<>();
        if (!cadreEmployeeNumbers.isEmpty()) {
            qualifiedNumbers = getQualifiedEmployeeNumbers(cadreEmployeeNumbers);
        }

        // 6. 按职位类分组统计
        Map<String, CompetenceCategoryCertStatisticsVO> categoryMap = new HashMap<>();
        int totalCountSum = 0;
        int certifiedCountSum = 0;
        int qualifiedCountSum = 0;

        for (EmployeeWithCategoryVO cadre : allCadres) {
            String category = cadre.getCompetenceCategory();
            if (category == null || category.trim().isEmpty()) {
                category = "未知";
            }

            // 获取或创建职位类统计对象
            CompetenceCategoryCertStatisticsVO categoryStat = categoryMap.getOrDefault(category, new CompetenceCategoryCertStatisticsVO());
            categoryStat.setCompetenceCategory(category);

            // 累加总人数
            if (categoryStat.getTotalCount() == null) {
                categoryStat.setTotalCount(0);
            }
            categoryStat.setTotalCount(categoryStat.getTotalCount() + 1);
            totalCountSum++;

            // 检查是否已认证
            String employeeNumber = cadre.getEmployeeNumber();
            if (employeeNumber != null && certifiedNumbers.contains(employeeNumber)) {
                if (categoryStat.getCertifiedCount() == null) {
                    categoryStat.setCertifiedCount(0);
                }
                categoryStat.setCertifiedCount(categoryStat.getCertifiedCount() + 1);
                certifiedCountSum++;
            }

            // 检查是否已任职
            if (employeeNumber != null && qualifiedNumbers.contains(employeeNumber)) {
                if (categoryStat.getQualifiedCount() == null) {
                    categoryStat.setQualifiedCount(0);
                }
                categoryStat.setQualifiedCount(categoryStat.getQualifiedCount() + 1);
                qualifiedCountSum++;
            }

            categoryMap.put(category, categoryStat);
        }

        // 7. 计算每个职位类的认证率和任职率
        List<CompetenceCategoryCertStatisticsVO> categoryStats = new ArrayList<>();
        for (CompetenceCategoryCertStatisticsVO categoryStat : categoryMap.values()) {
            if (categoryStat.getTotalCount() != null && categoryStat.getTotalCount() > 0) {
                if (categoryStat.getCertifiedCount() == null) {
                    categoryStat.setCertifiedCount(0);
                }
                // 计算认证率
                BigDecimal certRate = new BigDecimal(categoryStat.getCertifiedCount())
                        .divide(new BigDecimal(categoryStat.getTotalCount()), 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal(100));
                categoryStat.setCertRate(certRate);
                
                // 计算任职率
                if (categoryStat.getQualifiedCount() == null) {
                    categoryStat.setQualifiedCount(0);
                }
                BigDecimal qualifiedRate = new BigDecimal(categoryStat.getQualifiedCount())
                        .divide(new BigDecimal(categoryStat.getTotalCount()), 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal(100));
                categoryStat.setQualifiedRate(qualifiedRate);
            } else {
                categoryStat.setCertRate(BigDecimal.ZERO);
                categoryStat.setQualifiedRate(BigDecimal.ZERO);
            }
            categoryStats.add(categoryStat);
        }

        // 8. 构建总计统计对象
        CompetenceCategoryCertStatisticsVO totalStatistics = new CompetenceCategoryCertStatisticsVO();
        totalStatistics.setCompetenceCategory("总计");
        totalStatistics.setTotalCount(totalCountSum);
        totalStatistics.setCertifiedCount(certifiedCountSum);
        totalStatistics.setQualifiedCount(qualifiedCountSum);
        if (totalCountSum > 0) {
            // 计算总计认证率
            BigDecimal totalCertRate = new BigDecimal(certifiedCountSum)
                    .divide(new BigDecimal(totalCountSum), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(100));
            totalStatistics.setCertRate(totalCertRate);
            
            // 计算总计任职率
            BigDecimal totalQualifiedRate = new BigDecimal(qualifiedCountSum)
                    .divide(new BigDecimal(totalCountSum), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(100));
            totalStatistics.setQualifiedRate(totalQualifiedRate);
        } else {
            totalStatistics.setCertRate(BigDecimal.ZERO);
            totalStatistics.setQualifiedRate(BigDecimal.ZERO);
        }

        // 9. 构建返回结果
        CompetenceCategoryCertStatisticsResponseVO response = new CompetenceCategoryCertStatisticsResponseVO();
        response.setDeptCode(deptCode);
        response.setDeptName(deptInfo.getDeptName());
        response.setCategoryStatistics(categoryStats);
        response.setTotalStatistics(totalStatistics);

        return response;
    }

    /**
     * 按职位类统计专家任职认证信息
     * @param deptCode 部门ID（部门编码）
     * @return 按职位类统计的认证和任职信息（包含认证人数和任职人数）
     */
    private CompetenceCategoryCertStatisticsResponseVO getExpertCompetenceCategoryCertStatistics(String deptCode) {
        // 1. 查询部门信息
        DepartmentInfoVO deptInfo = departmentInfoMapper.getDepartmentByCode(deptCode);
        if (deptInfo == null) {
            throw new IllegalArgumentException("部门不存在：" + deptCode);
        }

        // 2. 构造部门编码列表（只包含当前部门本身）
        List<String> deptCodeList = new ArrayList<>();
        deptCodeList.add(deptCode);

        // 3. 根据部门层级确定查询的层级
        Integer deptLevel = Integer.parseInt(deptInfo.getDeptLevel());

        // 4. 从专家表中查询属于当前部门的专家工号和职位族
        List<EmployeeWithCategoryVO> allExperts = expertMapper.getExpertsWithJobCategoryByDeptCodes(deptLevel, deptCodeList);

        if (allExperts == null || allExperts.isEmpty()) {
            // 如果没有专家，返回空统计
            CompetenceCategoryCertStatisticsResponseVO response = new CompetenceCategoryCertStatisticsResponseVO();
            response.setDeptCode(deptCode);
            response.setDeptName(deptInfo.getDeptName());
            response.setCategoryStatistics(new ArrayList<>());
            CompetenceCategoryCertStatisticsVO total = new CompetenceCategoryCertStatisticsVO();
            total.setCompetenceCategory("总计");
            total.setTotalCount(0);
            total.setCertifiedCount(0);
            total.setQualifiedCount(0);
            total.setCertRate(BigDecimal.ZERO);
            total.setQualifiedRate(BigDecimal.ZERO);
            response.setTotalStatistics(total);
            return response;
        }

        // 5. 提取所有专家工号
        List<String> expertEmployeeNumbers = allExperts.stream()
                .map(EmployeeWithCategoryVO::getEmployeeNumber)
                .filter(num -> num != null && !num.trim().isEmpty())
                .distinct()
                .collect(Collectors.toList());

        // 6. 查询已通过认证的专家工号列表
        List<String> certifiedNumbers = new ArrayList<>();
        if (!expertEmployeeNumbers.isEmpty()) {
            certifiedNumbers = getCertifiedEmployeeNumbers(expertEmployeeNumbers);
        }

        // 7. 查询已获得任职的专家工号列表
        List<String> qualifiedNumbers = new ArrayList<>();
        if (!expertEmployeeNumbers.isEmpty()) {
            qualifiedNumbers = getQualifiedEmployeeNumbers(expertEmployeeNumbers);
        }

        // 8. 按职位类分组统计（从职位族中提取职位类）
        Map<String, CompetenceCategoryCertStatisticsVO> categoryMap = new HashMap<>();
        int totalCountSum = 0;
        int certifiedCountSum = 0;
        int qualifiedCountSum = 0;

        for (EmployeeWithCategoryVO expert : allExperts) {
            // 从职位族中提取职位类
            String jobCategory = expert.getCompetenceCategory();
            String category = extractCompetenceCategory(jobCategory);

            // 获取或创建职位类统计对象
            CompetenceCategoryCertStatisticsVO categoryStat = categoryMap.getOrDefault(category, new CompetenceCategoryCertStatisticsVO());
            categoryStat.setCompetenceCategory(category);

            // 累加总人数
            if (categoryStat.getTotalCount() == null) {
                categoryStat.setTotalCount(0);
            }
            categoryStat.setTotalCount(categoryStat.getTotalCount() + 1);
            totalCountSum++;

            // 检查是否已认证
            String employeeNumber = expert.getEmployeeNumber();
            if (employeeNumber != null && certifiedNumbers.contains(employeeNumber)) {
                if (categoryStat.getCertifiedCount() == null) {
                    categoryStat.setCertifiedCount(0);
                }
                categoryStat.setCertifiedCount(categoryStat.getCertifiedCount() + 1);
                certifiedCountSum++;
            }

            // 检查是否已任职
            if (employeeNumber != null && qualifiedNumbers.contains(employeeNumber)) {
                if (categoryStat.getQualifiedCount() == null) {
                    categoryStat.setQualifiedCount(0);
                }
                categoryStat.setQualifiedCount(categoryStat.getQualifiedCount() + 1);
                qualifiedCountSum++;
            }

            categoryMap.put(category, categoryStat);
        }

        // 9. 计算每个职位类的认证率和任职率
        List<CompetenceCategoryCertStatisticsVO> categoryStats = new ArrayList<>();
        for (CompetenceCategoryCertStatisticsVO categoryStat : categoryMap.values()) {
            if (categoryStat.getTotalCount() != null && categoryStat.getTotalCount() > 0) {
                if (categoryStat.getCertifiedCount() == null) {
                    categoryStat.setCertifiedCount(0);
                }
                // 计算认证率
                BigDecimal certRate = new BigDecimal(categoryStat.getCertifiedCount())
                        .divide(new BigDecimal(categoryStat.getTotalCount()), 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal(100));
                categoryStat.setCertRate(certRate);
                
                // 计算任职率
                if (categoryStat.getQualifiedCount() == null) {
                    categoryStat.setQualifiedCount(0);
                }
                BigDecimal qualifiedRate = new BigDecimal(categoryStat.getQualifiedCount())
                        .divide(new BigDecimal(categoryStat.getTotalCount()), 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal(100));
                categoryStat.setQualifiedRate(qualifiedRate);
            } else {
                categoryStat.setCertRate(BigDecimal.ZERO);
                categoryStat.setQualifiedRate(BigDecimal.ZERO);
            }
            categoryStats.add(categoryStat);
        }

        // 10. 构建总计统计对象
        CompetenceCategoryCertStatisticsVO totalStatistics = new CompetenceCategoryCertStatisticsVO();
        totalStatistics.setCompetenceCategory("总计");
        totalStatistics.setTotalCount(totalCountSum);
        totalStatistics.setCertifiedCount(certifiedCountSum);
        totalStatistics.setQualifiedCount(qualifiedCountSum);
        if (totalCountSum > 0) {
            // 计算总计认证率
            BigDecimal totalCertRate = new BigDecimal(certifiedCountSum)
                    .divide(new BigDecimal(totalCountSum), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(100));
            totalStatistics.setCertRate(totalCertRate);
            
            // 计算总计任职率
            BigDecimal totalQualifiedRate = new BigDecimal(qualifiedCountSum)
                    .divide(new BigDecimal(totalCountSum), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(100));
            totalStatistics.setQualifiedRate(totalQualifiedRate);
        } else {
            totalStatistics.setCertRate(BigDecimal.ZERO);
            totalStatistics.setQualifiedRate(BigDecimal.ZERO);
        }

        // 11. 构建返回结果
        CompetenceCategoryCertStatisticsResponseVO response = new CompetenceCategoryCertStatisticsResponseVO();
        response.setDeptCode(deptCode);
        response.setDeptName(deptInfo.getDeptName());
        response.setCategoryStatistics(categoryStats);
        response.setTotalStatistics(totalStatistics);

        return response;
    }

    /**
     * 按组织成熟度统计通过认证和任职的人数
     * @param deptCode 部门ID（部门编码），当为"0"时查询云核心网产品线部门下的所有六级部门
     * @param personType 人员类型（0-全员）
     * @return 按成熟度统计的认证和任职信息（包含认证人数和任职人数）
     */
    public MaturityCertStatisticsResponseVO getMaturityCertStatistics(String deptCode, Integer personType) {
        List<DepartmentInfoVO> level6Depts;
        String deptName;

        // 特殊处理：当 deptCode 为 "0" 时，查询云核心网产品线部门下的所有六级部门
        if ("0".equals(deptCode)) {
            // 查询云核心网产品线部门下的所有六级部门（复用已有逻辑）
            level6Depts = departmentInfoMapper.getAllLevel6DepartmentsUnderDept(DepartmentConstants.CLOUD_CORE_NETWORK_DEPT_CODE);
            if (level6Depts == null || level6Depts.isEmpty()) {
                // 如果没有六级部门，返回空统计
                MaturityCertStatisticsResponseVO response = new MaturityCertStatisticsResponseVO();
                response.setDeptCode(deptCode);
                response.setDeptName("云核心网下属所有六级部门");
                response.setMaturityStatistics(new ArrayList<>());
                MaturityCertStatisticsVO total = new MaturityCertStatisticsVO();
                total.setMaturityLevel("总计");
                total.setBaselineCount(0);
                total.setCertifiedCount(0);
                total.setQualifiedCount(0);
                total.setCertRate(BigDecimal.ZERO);
                total.setQualifiedRate(BigDecimal.ZERO);
                response.setTotalStatistics(total);
                return response;
            }
            deptName = "云核心网下属所有六级部门";
        } else {
            // 1. 查询部门信息
            DepartmentInfoVO deptInfo = departmentInfoMapper.getDepartmentByCode(deptCode);
            if (deptInfo == null) {
                throw new IllegalArgumentException("部门不存在：" + deptCode);
            }

            // 2. 查询该部门下所有六级部门
            level6Depts = departmentInfoMapper.getAllLevel6DepartmentsUnderDept(deptCode);
            if (level6Depts == null || level6Depts.isEmpty()) {
                // 如果没有六级部门，返回空统计
                MaturityCertStatisticsResponseVO response = new MaturityCertStatisticsResponseVO();
                response.setDeptCode(deptCode);
                response.setDeptName(deptInfo.getDeptName());
                response.setMaturityStatistics(new ArrayList<>());
                MaturityCertStatisticsVO total = new MaturityCertStatisticsVO();
                total.setMaturityLevel("总计");
                total.setBaselineCount(0);
                total.setCertifiedCount(0);
                total.setQualifiedCount(0);
                total.setCertRate(BigDecimal.ZERO);
                total.setQualifiedRate(BigDecimal.ZERO);
                response.setTotalStatistics(total);
                return response;
            }
            deptName = deptInfo.getDeptName();
        }

        // 3. 提取所有六级部门编码
        List<String> level6DeptCodes = level6Depts.stream()
                .map(DepartmentInfoVO::getDeptCode)
                .filter(code -> code != null && !code.trim().isEmpty())
                .collect(Collectors.toList());

        // 4. 查询这些六级部门的AI成熟度信息
        List<DepartmentMaturityVO> maturityList = departmentMaturityMapper.getDepartmentMaturities(level6DeptCodes);
        // 构建部门编码到成熟度的映射
        Map<String, String> deptMaturityMap = new HashMap<>();
        for (DepartmentMaturityVO maturity : maturityList) {
            if (maturity.getDeptCode() != null && maturity.getAiMaturity() != null) {
                deptMaturityMap.put(maturity.getDeptCode(), maturity.getAiMaturity());
            }
        }

        // 5. 按部门分组，然后按成熟度分组统计
        // 5.1 先按部门分组员工（使用deptLevel=6，查询department7_id）
        Map<String, List<String>> deptEmployeeMap = new HashMap<>();
        List<String> allEmployeeNumbers = new ArrayList<>();
        for (String deptCode6 : level6DeptCodes) {
            List<String> deptIdList = new ArrayList<>();
            deptIdList.add(deptCode6);
            List<String> deptEmployees = employeeMapper.getEmployeeNumbersByDeptLevel(6, deptIdList);
            if (deptEmployees != null && !deptEmployees.isEmpty()) {
                deptEmployeeMap.put(deptCode6, deptEmployees);
                allEmployeeNumbers.addAll(deptEmployees);
            }
        }

        if (allEmployeeNumbers.isEmpty()) {
            // 如果没有员工，返回空统计
            MaturityCertStatisticsResponseVO response = new MaturityCertStatisticsResponseVO();
            response.setDeptCode(deptCode);
            response.setDeptName(deptName);
            response.setMaturityStatistics(new ArrayList<>());
            MaturityCertStatisticsVO total = new MaturityCertStatisticsVO();
            total.setMaturityLevel("总计");
            total.setBaselineCount(0);
            total.setCertifiedCount(0);
            total.setQualifiedCount(0);
            total.setCertRate(BigDecimal.ZERO);
            response.setTotalStatistics(total);
            return response;
        }

        // 6. 查询已通过认证的员工工号列表（复用现有方法）
        List<String> certifiedNumbers = getCertifiedEmployeeNumbers(allEmployeeNumbers);

        // 6.1 查询已获得任职的员工工号列表
        List<String> qualifiedNumbers = new ArrayList<>();
        if (!allEmployeeNumbers.isEmpty()) {
            List<String> qualifiedNumbersResult = getQualifiedEmployeeNumbers(allEmployeeNumbers);
            if (qualifiedNumbersResult != null) {
                qualifiedNumbers.addAll(qualifiedNumbersResult);
            }
        }

        // 7. 按成熟度分组统计
        Map<String, MaturityCertStatisticsVO> maturityMap = new HashMap<>();
        int totalBaselineCount = 0;
        int totalCertifiedCount = 0;
        int totalQualifiedCount = 0;

        for (Map.Entry<String, List<String>> entry : deptEmployeeMap.entrySet()) {
            String deptCode6 = entry.getKey();
            List<String> employees = entry.getValue();
            
            // 获取该部门的成熟度
            String maturity = deptMaturityMap.getOrDefault(deptCode6, "未知");
            if (maturity == null || maturity.trim().isEmpty()) {
                maturity = "未知";
            }

            // 获取或创建成熟度统计对象
            MaturityCertStatisticsVO maturityStat = maturityMap.getOrDefault(maturity, new MaturityCertStatisticsVO());
            maturityStat.setMaturityLevel(maturity);

            // 累加基线人数
            if (maturityStat.getBaselineCount() == null) {
                maturityStat.setBaselineCount(0);
            }
            maturityStat.setBaselineCount(maturityStat.getBaselineCount() + employees.size());
            totalBaselineCount += employees.size();

            // 统计认证人数
            int certifiedCount = 0;
            for (String employeeNumber : employees) {
                if (employeeNumber != null && certifiedNumbers.contains(employeeNumber)) {
                    certifiedCount++;
                }
            }
            if (maturityStat.getCertifiedCount() == null) {
                maturityStat.setCertifiedCount(0);
            }
            maturityStat.setCertifiedCount(maturityStat.getCertifiedCount() + certifiedCount);
            totalCertifiedCount += certifiedCount;

            // 统计任职人数
            int qualifiedCount = 0;
            for (String employeeNumber : employees) {
                if (employeeNumber != null && qualifiedNumbers.contains(employeeNumber)) {
                    qualifiedCount++;
                }
            }
            if (maturityStat.getQualifiedCount() == null) {
                maturityStat.setQualifiedCount(0);
            }
            maturityStat.setQualifiedCount(maturityStat.getQualifiedCount() + qualifiedCount);
            totalQualifiedCount += qualifiedCount;

            maturityMap.put(maturity, maturityStat);
        }

        // 8. 计算每个成熟度的认证率和任职率
        List<MaturityCertStatisticsVO> maturityStats = new ArrayList<>();
        for (MaturityCertStatisticsVO maturityStat : maturityMap.values()) {
            if (maturityStat.getBaselineCount() != null && maturityStat.getBaselineCount() > 0) {
                if (maturityStat.getCertifiedCount() == null) {
                    maturityStat.setCertifiedCount(0);
                }
                // 计算认证率
                BigDecimal certRate = new BigDecimal(maturityStat.getCertifiedCount())
                        .divide(new BigDecimal(maturityStat.getBaselineCount()), 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal(100));
                maturityStat.setCertRate(certRate);
                
                // 计算任职率
                if (maturityStat.getQualifiedCount() == null) {
                    maturityStat.setQualifiedCount(0);
                }
                BigDecimal qualifiedRate = new BigDecimal(maturityStat.getQualifiedCount())
                        .divide(new BigDecimal(maturityStat.getBaselineCount()), 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal(100));
                maturityStat.setQualifiedRate(qualifiedRate);
            } else {
                maturityStat.setCertRate(BigDecimal.ZERO);
                maturityStat.setQualifiedRate(BigDecimal.ZERO);
            }
            maturityStats.add(maturityStat);
        }

        // 9. 构建总计统计对象
        MaturityCertStatisticsVO totalStatistics = new MaturityCertStatisticsVO();
        totalStatistics.setMaturityLevel("总计");
        totalStatistics.setBaselineCount(totalBaselineCount);
        totalStatistics.setCertifiedCount(totalCertifiedCount);
        totalStatistics.setQualifiedCount(totalQualifiedCount);
        if (totalBaselineCount > 0) {
            // 计算总计认证率
            BigDecimal totalCertRate = new BigDecimal(totalCertifiedCount)
                    .divide(new BigDecimal(totalBaselineCount), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(100));
            totalStatistics.setCertRate(totalCertRate);
            
            // 计算总计任职率
            BigDecimal totalQualifiedRate = new BigDecimal(totalQualifiedCount)
                    .divide(new BigDecimal(totalBaselineCount), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(100));
            totalStatistics.setQualifiedRate(totalQualifiedRate);
        } else {
            totalStatistics.setCertRate(BigDecimal.ZERO);
            totalStatistics.setQualifiedRate(BigDecimal.ZERO);
        }

        // 10. 构建返回结果
        MaturityCertStatisticsResponseVO response = new MaturityCertStatisticsResponseVO();
        response.setDeptCode(deptCode);
        response.setDeptName(deptName);
        response.setMaturityStatistics(maturityStats);
        response.setTotalStatistics(totalStatistics);

        return response;
    }

    /**
     * 查询部门维度的下钻信息
     * @param deptCode 部门ID（部门编码）
     * @param personType 人员类型（0：全员数据）
     * @param dataType 数据类型（1：任职数据，2：认证数据）
     * @return 员工详细信息列表
     */
    public EmployeeDrillDownResponseVO getEmployeeDrillDownInfo(String deptCode, Integer personType, Integer dataType) {
        // 1. 参数校验
        if (deptCode == null || deptCode.trim().isEmpty()) {
            throw new IllegalArgumentException("部门ID不能为空");
        }

        if (personType == null) {
            throw new IllegalArgumentException("人员类型不能为空");
        }

        if (personType != 0) {
            throw new IllegalArgumentException("暂不支持该人员类型，目前只支持全员（personType=0）");
        }

        if (dataType == null) {
            throw new IllegalArgumentException("数据类型不能为空");
        }

        // 2. 查询部门信息
        DepartmentInfoVO deptInfo = departmentInfoMapper.getDepartmentByCode(deptCode);
        if (deptInfo == null) {
            throw new IllegalArgumentException("部门不存在：" + deptCode);
        }

        // 3. 根据数据类型查询不同的信息
        List<EmployeeDetailVO> employeeDetails = new ArrayList<>();

        if (dataType == 2) {
            // 认证数据：查询员工认证详细信息
            Integer deptLevel = Integer.parseInt(deptInfo.getDeptLevel());
            // 默认查询认证人数（queryType=1），不按职位类过滤（jobCategory=null）
            employeeDetails = employeeMapper.getEmployeeCertDetailsByDeptLevel(deptLevel, deptCode, null, 1);
        } else if (dataType == 1) {
            // 任职数据：暂时返回空列表，等用户提供具体需求
            employeeDetails = new ArrayList<>();
        } else {
            throw new IllegalArgumentException("不支持的数据类型：" + dataType + "，只支持1（任职数据）和2（认证数据）");
        }

        // 4. 构建返回结果
        EmployeeDrillDownResponseVO response = new EmployeeDrillDownResponseVO();
        response.setEmployeeDetails(employeeDetails);

        return response;
    }

    /**
     * 查询干部或专家认证类信息（默认查询认证数据）
     * @param deptCode 部门ID（部门编码）
     * @param aiMaturity 岗位AI成熟度
     * @param jobCategory 职位类
     * @param personType 人员类型（0-全员，1-干部，2-专家）
     * @param queryType 查询类型（1-认证人数，2-基线人数）
     * @return 员工详细信息列表
     */
    public EmployeeDrillDownResponseVO getPersonCertDetailsByConditions(
            String deptCode, String aiMaturity, String jobCategory, Integer personType, Integer queryType) {
        // 1. 参数校验
        if (deptCode == null || deptCode.trim().isEmpty()) {
            throw new IllegalArgumentException("部门ID不能为空");
        }

        if (personType == null) {
            throw new IllegalArgumentException("人员类型不能为空");
        }

        if (personType != 0 && personType != 1 && personType != 2) {
            throw new IllegalArgumentException("不支持的人员类型：" + personType + "，只支持0（全员）、1（干部）和2（专家）");
        }

        // 验证 queryType 参数
        if (queryType == null) {
            queryType = 1; // 默认为认证人数
        }
        if (queryType != 1 && queryType != 2) {
            throw new IllegalArgumentException("查询类型参数错误，只支持1（认证人数）或2（基线人数）");
        }

        // 全员类型不支持按成熟度过滤，如果传入了aiMaturity参数，忽略该参数
        if (personType == 0 && aiMaturity != null && !aiMaturity.trim().isEmpty()) {
            // 忽略aiMaturity参数，不报错
            aiMaturity = null;
        }

        // 2. 查询部门信息
        DepartmentInfoVO deptInfo = departmentInfoMapper.getDepartmentByCode(deptCode);
        if (deptInfo == null) {
            throw new IllegalArgumentException("部门不存在：" + deptCode);
        }

        // 3. 根据人员类型查询认证数据（默认查询认证数据，dataType=2）
        List<EmployeeDetailVO> employeeDetails = new ArrayList<>();

        if (personType == 0) {
            // 全员处理（使用 t_employee 表优化）
            // 特殊处理：当 deptCode 为 "0" 时
            String actualDeptCode = deptCode;
            if ("0".equals(deptCode.trim())) {
                actualDeptCode = DepartmentConstants.CLOUD_CORE_NETWORK_DEPT_CODE;
            }
            
            // 查询部门信息，获取部门层级
            DepartmentInfoVO actualDeptInfo = departmentInfoMapper.getDepartmentByCode(actualDeptCode);
            if (actualDeptInfo == null) {
                throw new IllegalArgumentException("部门不存在：" + actualDeptCode);
            }
            
            // 直接使用当前部门的层级进行查询（不查询子部门）
            Integer deptLevel = Integer.parseInt(actualDeptInfo.getDeptLevel());
            
            // 调用 EmployeeMapper.getEmployeeCertDetailsByDeptLevelFromEmployee 查询当前部门的员工认证详细信息
            // 使用 t_employee 表，无需 JOIN 其他表
            employeeDetails = employeeMapper.getEmployeeCertDetailsByDeptLevelFromEmployee(
                    deptLevel, actualDeptCode, jobCategory, queryType);
        } else if (personType == 1) {
            // 干部处理
            // 查询该部门下的所有子部门（包括所有层级）
            List<DepartmentInfoVO> allSubDepts = departmentInfoMapper.getAllSubDepartments(deptCode);
            
            // 构造部门编码列表（包括本部门本身和所有子部门）
            List<String> deptCodeList = new ArrayList<>();
            deptCodeList.add(deptCode);
            if (allSubDepts != null && !allSubDepts.isEmpty()) {
                for (DepartmentInfoVO subDept : allSubDepts) {
                    if (subDept.getDeptCode() != null && !subDept.getDeptCode().trim().isEmpty()) {
                        deptCodeList.add(subDept.getDeptCode());
                    }
                }
            }

            // 干部认证数据（queryType参数暂时保留，但认证数据查询不使用）
            employeeDetails = cadreMapper.getCadreCertDetailsByConditions(deptCodeList, aiMaturity, jobCategory, queryType);
        } else if (personType == 2) {
            // 专家处理 - 参考getExpertAiCertStatistics的逻辑
            String actualDeptCode = deptCode;
            
            // 当deptCode为"0"时，使用云核心网产品线部门ID
            if ("0".equals(deptCode)) {
                actualDeptCode = DepartmentConstants.CLOUD_CORE_NETWORK_DEPT_CODE;
            }
            
            // 获取部门层级，用于后续的部门过滤
            String deptLevelStr = deptInfo.getDeptLevel();
            Integer deptLevel = Integer.parseInt(deptLevelStr);
            
            // 1. 调用Mapper方法查询专家数据
            List<ExpertInfoVO> expertList = expertMapper.getExpertInfoByDeptCode(actualDeptCode, deptLevel);
            
            if (expertList == null || expertList.isEmpty()) {
                employeeDetails = new ArrayList<>();
            } else {
                // 2. 根据条件过滤专家
                List<ExpertInfoVO> filteredExpertList = new ArrayList<>();
                for (ExpertInfoVO expert : expertList) {
                    String expertAiMaturity = expert.getAiMaturity();
                    String expertJobCategory = extractJobCategory(expert.getJobCategory());
                    
                    // 过滤AI成熟度
                    if (aiMaturity != null && !aiMaturity.trim().isEmpty()) {
                        if ("L5".equals(aiMaturity)) {
                            // L5代表查询L2和L3
                            if (expertAiMaturity == null || (!expertAiMaturity.equals("L2") && !expertAiMaturity.equals("L3"))) {
                                continue;
                            }
                        } else {
                            if (expertAiMaturity == null || !expertAiMaturity.equals(aiMaturity)) {
                                continue;
                            }
                        }
                    }
                    
                    // 过滤职位类
                    if (jobCategory != null && !jobCategory.trim().isEmpty()) {
                        if ("非软件类".equals(jobCategory)) {
                            // 非软件类：查询所有不是"软件类"的职位类
                            if (expertJobCategory == null || "软件类".equals(expertJobCategory)) {
                                continue;
                            }
                        } else if ("其他类".equals(jobCategory)) {
                            // 其他类：查询所有不是"研究类"、"软件类"、"系统类"、"测试类"的职位类
                            if (expertJobCategory == null || "研究类".equals(expertJobCategory) 
                                    || "软件类".equals(expertJobCategory) || "系统类".equals(expertJobCategory) 
                                    || "测试类".equals(expertJobCategory)) {
                                continue;
                            }
                        } else {
                            // 其他职位类：精确匹配
                            if (expertJobCategory == null || !expertJobCategory.equals(jobCategory)) {
                                continue;
                            }
                        }
                    }
                    
                    filteredExpertList.add(expert);
                }
                
                // 3. 根据queryType决定是否只返回已认证的专家
                List<String> employeeNumbers = filteredExpertList.stream()
                        .map(ExpertInfoVO::getEmployeeNumber)
                        .filter(num -> num != null && !num.trim().isEmpty())
                        .distinct()
                        .collect(Collectors.toList());
                
                if (employeeNumbers.isEmpty()) {
                    employeeDetails = new ArrayList<>();
                } else {
                    // 如果queryType=1，只返回已认证的专家
                    if (queryType == 1) {
                        List<String> certifiedNumbers = getCertifiedEmployeeNumbers(employeeNumbers);
                        Set<String> certifiedSet = new HashSet<>(certifiedNumbers != null ? certifiedNumbers : new ArrayList<>());
                        employeeNumbers = employeeNumbers.stream()
                                .filter(certifiedSet::contains)
                                .collect(Collectors.toList());
                    }
                    
                    // 4. 查询专家的详细信息和证书信息
                    if (employeeNumbers.isEmpty()) {
                        employeeDetails = new ArrayList<>();
                    } else {
                        employeeDetails = expertCertStatisticsMapper.getExpertCertDetailsByEmployeeNumbers(
                                employeeNumbers, aiMaturity, jobCategory, queryType);
                    }
                }
            }
        }

        // 4. 构建返回结果
        EmployeeDrillDownResponseVO response = new EmployeeDrillDownResponseVO();
        response.setEmployeeDetails(employeeDetails);

        return response;
    }

    /**
     * 查询干部任职数据
     * @param deptCode 部门ID（部门编码）
     * @param aiMaturity 岗位AI成熟度
     * @param jobCategory 职位类
     * @param personType 人员类型（1-干部，当前只处理干部类型）
     * @return 员工详细信息列表
     */
    public EmployeeDrillDownResponseVO getCadreQualifiedDetailsByConditions(
            String deptCode, String aiMaturity, String jobCategory, Integer personType, Integer queryType) {
        // 1. 参数校验
        if (deptCode == null || deptCode.trim().isEmpty()) {
            throw new IllegalArgumentException("部门ID不能为空");
        }

        if (personType == null) {
            throw new IllegalArgumentException("人员类型不能为空");
        }

        if (personType != 0 && personType != 1 && personType != 2) {
            throw new IllegalArgumentException("不支持的人员类型：" + personType + "，只支持0（全员）、1（干部）和2（专家）");
        }

        // 验证 queryType 参数
        if (queryType == null) {
            queryType = 1; // 默认为任职人数
        }
        if (queryType != 1 && queryType != 2) {
            throw new IllegalArgumentException("查询类型参数错误，只支持1（任职人数）或2（基线人数）");
        }

        // 全员类型不支持按成熟度过滤，如果传入了aiMaturity参数，忽略该参数
        if (personType == 0 && aiMaturity != null && !aiMaturity.trim().isEmpty()) {
            // 忽略aiMaturity参数，不报错
            aiMaturity = null;
        }

        List<EmployeeDetailVO> employeeDetails = new ArrayList<>();

        if (personType == 0) {
            // 全员处理（与competence-category-cert-statistics保持一致）
            // 查询部门信息
            DepartmentInfoVO deptInfo = departmentInfoMapper.getDepartmentByCode(deptCode);
            if (deptInfo == null) {
                throw new IllegalArgumentException("部门不存在：" + deptCode);
            }
            
            // 查询所有层级子部门信息（与competence-category-cert-statistics保持一致）
            List<DepartmentInfoVO> allSubDepts = departmentInfoMapper.getAllSubDepartments(deptCode);
            
            // 构造部门编码列表（包括本部门本身和所有子部门）
            List<DepartmentInfoVO> allDepts = new ArrayList<>();
            allDepts.add(deptInfo);
            if (allSubDepts != null && !allSubDepts.isEmpty()) {
                allDepts.addAll(allSubDepts);
            }
            
            // 遍历所有部门，查询每个部门的员工任职详细信息
            List<EmployeeDetailVO> allEmployeeDetails = new ArrayList<>();
            
            for (DepartmentInfoVO dept : allDepts) {
                if (dept.getDeptCode() != null && !dept.getDeptCode().trim().isEmpty()) {
                    String deptLevelStr = dept.getDeptLevel();
                    Integer queryLevel = Integer.parseInt(deptLevelStr);
                    
                    List<EmployeeDetailVO> deptEmployeeDetails = employeeMapper.getEmployeeQualifiedDetailsByDeptLevel(
                            queryLevel, dept.getDeptCode(), jobCategory, queryType);
                    if (deptEmployeeDetails != null && !deptEmployeeDetails.isEmpty()) {
                        allEmployeeDetails.addAll(deptEmployeeDetails);
                    }
                }
            }
            
            // 按员工工号去重，确保每个员工只出现一次（与competence-category-cert-statistics保持一致）
            Map<String, EmployeeDetailVO> employeeMap = new HashMap<>();
            for (EmployeeDetailVO employee : allEmployeeDetails) {
                if (employee.getEmployeeNumber() != null && !employee.getEmployeeNumber().trim().isEmpty()) {
                    // 如果该员工已存在，保留第一个（或者可以根据业务需求选择保留最新的）
                    if (!employeeMap.containsKey(employee.getEmployeeNumber())) {
                        employeeMap.put(employee.getEmployeeNumber(), employee);
                    }
                }
            }
            
            employeeDetails = new ArrayList<>(employeeMap.values());
        } else if (personType == 1) {
            // 2. 查询部门信息
            DepartmentInfoVO deptInfo = departmentInfoMapper.getDepartmentByCode(deptCode);
            if (deptInfo == null) {
                throw new IllegalArgumentException("部门不存在：" + deptCode);
            }
            // 干部处理
            // 3. 查询该部门下的所有子部门（包括所有层级）
            List<DepartmentInfoVO> allSubDepts = departmentInfoMapper.getAllSubDepartments(deptCode);
            
            // 构造部门编码列表（包括本部门本身和所有子部门）
            List<String> deptCodeList = new ArrayList<>();
            deptCodeList.add(deptCode);
            if (allSubDepts != null && !allSubDepts.isEmpty()) {
                for (DepartmentInfoVO subDept : allSubDepts) {
                    if (subDept.getDeptCode() != null && !subDept.getDeptCode().trim().isEmpty()) {
                        deptCodeList.add(subDept.getDeptCode());
                    }
                }
            }

            // 4. 查询干部任职数据
            employeeDetails = cadreMapper.getCadreQualifiedDetailsByConditions(
                    deptCodeList, aiMaturity, jobCategory, queryType);
        } else if (personType == 2) {
            // 专家处理 - 参考getExpertAiQualifiedStatistics的逻辑
            // 查询部门信息
            DepartmentInfoVO deptInfo = departmentInfoMapper.getDepartmentByCode(deptCode);
            if (deptInfo == null) {
                throw new IllegalArgumentException("部门不存在：" + deptCode);
            }
            
            String actualDeptCode = deptCode;
            String deptName = null;
            
            // 当deptCode为"0"时，使用云核心网产品线部门ID
            if ("0".equals(deptCode)) {
                actualDeptCode = DepartmentConstants.CLOUD_CORE_NETWORK_DEPT_CODE;
                deptName = "云核心网";
            } else {
                deptName = deptInfo.getDeptName();
            }
            
            // 获取部门层级，用于后续的部门过滤
            String deptLevelStr = deptInfo.getDeptLevel();
            Integer deptLevel = Integer.parseInt(deptLevelStr);
            
            // 1. 调用Mapper方法查询专家数据
            List<ExpertInfoVO> expertList = expertMapper.getExpertInfoByDeptCode(actualDeptCode, deptLevel);
            
            if (expertList == null || expertList.isEmpty()) {
                employeeDetails = new ArrayList<>();
            } else {
                // 2. 根据条件过滤专家
                List<ExpertInfoVO> filteredExpertList = new ArrayList<>();
                for (ExpertInfoVO expert : expertList) {
                    String expertAiMaturity = expert.getAiMaturity();
                    String expertJobCategory = extractJobCategory(expert.getJobCategory());
                    
                    // 只处理L2和L3的成熟度
                    if (expertAiMaturity == null || (!expertAiMaturity.equals("L2") && !expertAiMaturity.equals("L3"))) {
                        continue;
                    }
                    
                    // 过滤AI成熟度
                    if (aiMaturity != null && !aiMaturity.trim().isEmpty()) {
                        if ("L5".equals(aiMaturity)) {
                            // L5代表查询L2和L3，已经在上面的判断中处理了
                            // 不需要额外判断
                        } else {
                            if (!expertAiMaturity.equals(aiMaturity)) {
                                continue;
                            }
                        }
                    }
                    
                    // 过滤职位类
                    if (jobCategory != null && !jobCategory.trim().isEmpty()) {
                        if ("非软件类".equals(jobCategory)) {
                            // 非软件类：查询所有不是"软件类"的职位类
                            if (expertJobCategory == null || "软件类".equals(expertJobCategory)) {
                                continue;
                            }
                        } else if ("其他类".equals(jobCategory)) {
                            // 其他类：查询所有不是"研究类"、"软件类"、"系统类"、"测试类"的职位类
                            if (expertJobCategory == null || "研究类".equals(expertJobCategory) 
                                    || "软件类".equals(expertJobCategory) || "系统类".equals(expertJobCategory) 
                                    || "测试类".equals(expertJobCategory)) {
                                continue;
                            }
                        } else {
                            // 其他职位类：精确匹配
                            if (expertJobCategory == null || !expertJobCategory.equals(jobCategory)) {
                                continue;
                            }
                        }
                    }
                    
                    filteredExpertList.add(expert);
                }
                
                // 3. 根据queryType决定是否只返回已任职的专家
                List<String> employeeNumbers = filteredExpertList.stream()
                        .map(ExpertInfoVO::getEmployeeNumber)
                        .filter(num -> num != null && !num.trim().isEmpty())
                        .distinct()
                        .collect(Collectors.toList());
                
                if (employeeNumbers.isEmpty()) {
                    employeeDetails = new ArrayList<>();
                } else {
                    // 如果queryType=1，只返回已任职的专家
                    if (queryType == 1) {
                        List<String> qualifiedNumbers = getQualifiedEmployeeNumbers(employeeNumbers);
                        Set<String> qualifiedSet = new HashSet<>(qualifiedNumbers != null ? qualifiedNumbers : new ArrayList<>());
                        employeeNumbers = employeeNumbers.stream()
                                .filter(qualifiedSet::contains)
                                .collect(Collectors.toList());
                    }
                    
                    // 4. 查询专家的任职详细信息
                    if (employeeNumbers.isEmpty()) {
                        employeeDetails = new ArrayList<>();
                    } else {
                        // 使用getExpertQualifiedDetailsByConditions查询专家任职详情
                        // 需要根据部门编码、部门名称和部门层级来查询
                        employeeDetails = expertCertStatisticsMapper.getExpertQualifiedDetailsByConditions(
                                actualDeptCode, deptName, aiMaturity, jobCategory, deptLevel);
                        
                        // 进一步过滤，只返回符合条件的专家
                        Set<String> employeeNumberSet = new HashSet<>(employeeNumbers);
                        employeeDetails = employeeDetails.stream()
                                .filter(detail -> detail.getEmployeeNumber() != null 
                                        && employeeNumberSet.contains(detail.getEmployeeNumber()))
                                .collect(Collectors.toList());
                    }
                }
            }
        }

        // 5. 构建返回结果
        EmployeeDrillDownResponseVO response = new EmployeeDrillDownResponseVO();
        response.setEmployeeDetails(employeeDetails);

        return response;
    }

    /**
     * 查询干部任职认证数据（按成熟度和职位类统计）
     * @param deptCode 部门ID（部门编码），当为"0"时查询云核心网（030681）下的所有部门
     * @return 干部成熟度职位类认证统计响应
     */
    public CadreMaturityJobCategoryCertStatisticsResponseVO getCadreMaturityJobCategoryCertStatistics(String deptCode) {
        String actualDeptCode = deptCode;
        String deptName;
        
        // 特殊处理：当 deptCode 为 "0" 时，查询云核心网产品线下的所有部门
        if ("0".equals(deptCode)) {
            actualDeptCode = DepartmentConstants.CLOUD_CORE_NETWORK_DEPT_CODE;
            deptName = "云核心网";
        } else {
            deptName = null; // 稍后从数据库查询
        }
        
        // 1. 查询部门信息
        DepartmentInfoVO deptInfo = departmentInfoMapper.getDepartmentByCode(actualDeptCode);
        if (deptInfo == null) {
            throw new IllegalArgumentException("部门不存在：" + actualDeptCode);
        }
        
        // 如果deptName还没有设置，使用查询到的部门名称
        if (deptName == null) {
            deptName = deptInfo.getDeptName();
        }

        // 2. 查询该部门下的所有子部门（包括所有层级）
        List<DepartmentInfoVO> allSubDepts = departmentInfoMapper.getAllSubDepartments(actualDeptCode);
        
        // 构造部门编码列表（包括本部门本身和所有子部门）
        List<String> deptCodeList = new ArrayList<>();
        deptCodeList.add(actualDeptCode);
        if (allSubDepts != null && !allSubDepts.isEmpty()) {
            for (DepartmentInfoVO subDept : allSubDepts) {
                if (subDept.getDeptCode() != null && !subDept.getDeptCode().trim().isEmpty()) {
                    deptCodeList.add(subDept.getDeptCode());
                }
            }
        }

        // 3. 查询这些部门下的所有干部信息（工号、部门编码、职位类）
        List<CadreInfoVO> allCadres = cadreMapper.getCadreInfoByDeptCodes(deptCodeList);
        if (allCadres == null || allCadres.isEmpty()) {
            // 如果没有干部，返回空统计
            CadreMaturityJobCategoryCertStatisticsResponseVO response = 
                new CadreMaturityJobCategoryCertStatisticsResponseVO();
            response.setDeptCode(deptCode);
            response.setDeptName(deptName);
            response.setMaturityStatistics(new ArrayList<>());
            CadreMaturityCertStatisticsVO total = 
                new CadreMaturityCertStatisticsVO();
            total.setMaturityLevel("总计");
            total.setBaselineCount(0);
            total.setCertifiedCount(0);
            total.setSubject2PassCount(0);
            total.setCertRate(BigDecimal.ZERO);
            total.setSubject2PassRate(BigDecimal.ZERO);
            total.setJobCategoryStatistics(null);
            response.setTotalStatistics(total);
            return response;
        }

        // 4. 提取所有干部工号，查询认证和科目二通过情况
        List<String> allEmployeeNumbers = allCadres.stream()
                .map(CadreInfoVO::getEmployeeNumber)
                .filter(num -> num != null && !num.trim().isEmpty())
                .distinct()
                .collect(Collectors.toList());

        // 5.1 查询已通过认证的干部工号列表
        List<String> certifiedNumbers = getCertifiedEmployeeNumbers(allEmployeeNumbers);
        Set<String> certifiedSet = new HashSet<>(certifiedNumbers != null ? certifiedNumbers : new ArrayList<>());

        // 5.2 查询已通过科目二的干部工号列表
        List<String> subject2PassedNumbers = getSubject2PassedEmployeeNumbers(allEmployeeNumbers);
        Set<String> subject2PassedSet = new HashSet<>(subject2PassedNumbers != null ? subject2PassedNumbers : new ArrayList<>());

        // 6. 按成熟度和职位类分组统计
        // 结构：成熟度 -> 职位类 -> 统计信息
        Map<String, Map<String, CadreJobCategoryCertStatisticsVO>> maturityJobCategoryMap = new HashMap<>();
        
        // 用于统计L2和L3的总基数（包含所有职位类）
        Map<String, Integer> maturityTotalBaselineMap = new HashMap<>();
        Map<String, Integer> maturityTotalCertifiedMap = new HashMap<>();
        Map<String, Integer> maturityTotalSubject2PassMap = new HashMap<>();
        Map<String, Integer> maturityTotalCertStandardMap = new HashMap<>();
        
        int totalBaselineCount = 0;
        int totalCertifiedCount = 0;
        int totalSubject2PassCount = 0;
        int totalCertStandardCount = 0;

        for (CadreInfoVO cadre : allCadres) {
            // 直接从干部信息中获取AI成熟度（position_ai_maturity字段）
            String aiMaturity = cadre.getAiMaturity();
            if (aiMaturity == null || aiMaturity.trim().isEmpty()) {
                aiMaturity = "未知";
            }

            // 只统计L2和L3的成熟度
            if (!"L2".equals(aiMaturity) && !"L3".equals(aiMaturity)) {
                continue;
            }

            String jobCategory = cadre.getJobCategory();
            if (jobCategory == null || jobCategory.trim().isEmpty()) {
                jobCategory = "未知";
            }

            String employeeNumber = cadre.getEmployeeNumber();

            // 统计成熟度的总基数（所有职位类）
            // 注意：对于L2和L3成熟度，总基数需要统计软件类以及非软件类员工
            maturityTotalBaselineMap.put(aiMaturity, maturityTotalBaselineMap.getOrDefault(aiMaturity, 0) + 1);
            totalBaselineCount++;
            
            // 统计成熟度的总认证人数（所有职位类）
            // 注意：对于L2和L3成熟度，总认证人数需要统计软件类以及非软件类员工
            if (employeeNumber != null && certifiedSet.contains(employeeNumber)) {
                maturityTotalCertifiedMap.put(aiMaturity, maturityTotalCertifiedMap.getOrDefault(aiMaturity, 0) + 1);
                totalCertifiedCount++;
            }
            
            // 统计成熟度的总科目二通过人数（所有职位类）
            // 注意：对于L2和L3成熟度，总科目二通过人数需要统计软件类以及非软件类员工
            if (employeeNumber != null && subject2PassedSet.contains(employeeNumber)) {
                maturityTotalSubject2PassMap.put(aiMaturity, maturityTotalSubject2PassMap.getOrDefault(aiMaturity, 0) + 1);
                totalSubject2PassCount++;
            }
            
            // 统计成熟度的总持证人数（所有职位类）
            // 注意：根据is_cert_standard字段统计，1代表持证
            if (cadre.getIsCertStandard() != null && cadre.getIsCertStandard() == 1) {
                maturityTotalCertStandardMap.put(aiMaturity, maturityTotalCertStandardMap.getOrDefault(aiMaturity, 0) + 1);
                totalCertStandardCount++;
            }

            // L2和L3都返回软件类和非软件类员工数据（即所有职位类）
            // 注意：L2和L3的总基数、总认证人数、总科目二通过人数已经在上面的统计中包含了所有职位类
            boolean shouldInclude = false;
            if ("L2".equals(aiMaturity) || "L3".equals(aiMaturity)) {
                // L2和L3都返回所有职位类（软件类和非软件类）
                shouldInclude = true;
            }

            // 只有符合条件的职位类才加入到jobCategoryStatistics中（L2和L3都包含所有职位类）
            if (shouldInclude) {
                // 获取或创建成熟度对应的职位类Map
                Map<String, CadreJobCategoryCertStatisticsVO> jobCategoryMap = 
                    maturityJobCategoryMap.getOrDefault(aiMaturity, new HashMap<>());

                // 获取或创建职位类统计对象
                CadreJobCategoryCertStatisticsVO jobCategoryStat = 
                    jobCategoryMap.getOrDefault(jobCategory, new CadreJobCategoryCertStatisticsVO());
                jobCategoryStat.setJobCategory(jobCategory);

                // 累加基数人数（只统计符合条件的职位类）
                if (jobCategoryStat.getBaselineCount() == null) {
                    jobCategoryStat.setBaselineCount(0);
                }
                jobCategoryStat.setBaselineCount(jobCategoryStat.getBaselineCount() + 1);

                // 检查是否已认证
                if (employeeNumber != null && certifiedSet.contains(employeeNumber)) {
                    if (jobCategoryStat.getCertifiedCount() == null) {
                        jobCategoryStat.setCertifiedCount(0);
                    }
                    jobCategoryStat.setCertifiedCount(jobCategoryStat.getCertifiedCount() + 1);
                }

                // 检查是否已通过科目二
                if (employeeNumber != null && subject2PassedSet.contains(employeeNumber)) {
                    if (jobCategoryStat.getSubject2PassCount() == null) {
                        jobCategoryStat.setSubject2PassCount(0);
                    }
                    jobCategoryStat.setSubject2PassCount(jobCategoryStat.getSubject2PassCount() + 1);
                }

                // 检查是否持证达标（根据is_cert_standard字段，1代表持证）
                if (cadre.getIsCertStandard() != null && cadre.getIsCertStandard() == 1) {
                    if (jobCategoryStat.getCertStandardCount() == null) {
                        jobCategoryStat.setCertStandardCount(0);
                    }
                    jobCategoryStat.setCertStandardCount(jobCategoryStat.getCertStandardCount() + 1);
                }

                jobCategoryMap.put(jobCategory, jobCategoryStat);
                maturityJobCategoryMap.put(aiMaturity, jobCategoryMap);
            }
        }

        // 7. 计算每个职位类的认证率和科目二通过率，并构建成熟度统计对象
        List<CadreMaturityCertStatisticsVO> maturityStatistics = new ArrayList<>();
        
        // 按L2、L3的顺序处理
        for (String aiMaturity : new String[]{"L2", "L3"}) {
            Map<String, CadreJobCategoryCertStatisticsVO> jobCategoryMap = maturityJobCategoryMap.get(aiMaturity);
            if (jobCategoryMap == null) {
                jobCategoryMap = new HashMap<>();
            }

            // 创建成熟度统计对象
            CadreMaturityCertStatisticsVO maturityStat = 
                new CadreMaturityCertStatisticsVO();
            maturityStat.setMaturityLevel(aiMaturity);

            // 使用所有职位类的总基数（从maturityTotalBaselineMap获取）
            int maturityBaselineCount = maturityTotalBaselineMap.getOrDefault(aiMaturity, 0);
            int maturityCertifiedCount = maturityTotalCertifiedMap.getOrDefault(aiMaturity, 0);
            int maturitySubject2PassCount = maturityTotalSubject2PassMap.getOrDefault(aiMaturity, 0);
            int maturityCertStandardCount = maturityTotalCertStandardMap.getOrDefault(aiMaturity, 0);
            
            List<CadreJobCategoryCertStatisticsVO> jobCategoryStatistics = new ArrayList<>();

            // 遍历该成熟度下的所有职位类（包含所有职位类）
            for (CadreJobCategoryCertStatisticsVO jobCategoryStat : jobCategoryMap.values()) {
                // L2和L3都返回所有职位类（软件类和非软件类）
                
                // 计算职位类认证率（基于该职位类的基数）
                if (jobCategoryStat.getBaselineCount() != null && jobCategoryStat.getBaselineCount() > 0) {
                    if (jobCategoryStat.getCertifiedCount() == null) {
                        jobCategoryStat.setCertifiedCount(0);
                    }
                    BigDecimal certRate = new BigDecimal(jobCategoryStat.getCertifiedCount())
                            .divide(new BigDecimal(jobCategoryStat.getBaselineCount()), 4, RoundingMode.HALF_UP)
                            .multiply(new BigDecimal(100));
                    jobCategoryStat.setCertRate(certRate);
                } else {
                    jobCategoryStat.setCertRate(BigDecimal.ZERO);
                }

                // 计算职位类科目二通过率（基于该职位类的基数）
                if (jobCategoryStat.getBaselineCount() != null && jobCategoryStat.getBaselineCount() > 0) {
                    if (jobCategoryStat.getSubject2PassCount() == null) {
                        jobCategoryStat.setSubject2PassCount(0);
                    }
                    BigDecimal subject2PassRate = new BigDecimal(jobCategoryStat.getSubject2PassCount())
                            .divide(new BigDecimal(jobCategoryStat.getBaselineCount()), 4, RoundingMode.HALF_UP)
                            .multiply(new BigDecimal(100));
                    jobCategoryStat.setSubject2PassRate(subject2PassRate);
                } else {
                    jobCategoryStat.setSubject2PassRate(BigDecimal.ZERO);
                }

                // 计算职位类持证率（基于该职位类的基数）
                if (jobCategoryStat.getBaselineCount() != null && jobCategoryStat.getBaselineCount() > 0) {
                    if (jobCategoryStat.getCertStandardCount() == null) {
                        jobCategoryStat.setCertStandardCount(0);
                    }
                    BigDecimal certStandardRate = new BigDecimal(jobCategoryStat.getCertStandardCount())
                            .divide(new BigDecimal(jobCategoryStat.getBaselineCount()), 4, RoundingMode.HALF_UP)
                            .multiply(new BigDecimal(100));
                    jobCategoryStat.setCertStandardRate(certStandardRate);
                } else {
                    jobCategoryStat.setCertStandardRate(BigDecimal.ZERO);
                }

                jobCategoryStatistics.add(jobCategoryStat);
            }

            // 设置成熟度统计数据（使用所有职位类的总数）
            maturityStat.setBaselineCount(maturityBaselineCount);
            maturityStat.setCertifiedCount(maturityCertifiedCount);
            maturityStat.setSubject2PassCount(maturitySubject2PassCount);
            maturityStat.setCertStandardCount(maturityCertStandardCount);
            maturityStat.setJobCategoryStatistics(jobCategoryStatistics);

            // 计算成熟度认证率（基于所有职位类的基数）
            if (maturityBaselineCount > 0) {
                BigDecimal certRate = new BigDecimal(maturityCertifiedCount)
                        .divide(new BigDecimal(maturityBaselineCount), 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal(100));
                maturityStat.setCertRate(certRate);
            } else {
                maturityStat.setCertRate(BigDecimal.ZERO);
            }

            // 计算成熟度科目二通过率（基于所有职位类的基数）
            if (maturityBaselineCount > 0) {
                BigDecimal subject2PassRate = new BigDecimal(maturitySubject2PassCount)
                        .divide(new BigDecimal(maturityBaselineCount), 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal(100));
                maturityStat.setSubject2PassRate(subject2PassRate);
            } else {
                maturityStat.setSubject2PassRate(BigDecimal.ZERO);
            }

            // 计算成熟度持证率（基于所有职位类的基数）
            if (maturityBaselineCount > 0) {
                BigDecimal certStandardRate = new BigDecimal(maturityCertStandardCount)
                        .divide(new BigDecimal(maturityBaselineCount), 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal(100));
                maturityStat.setCertStandardRate(certStandardRate);
            } else {
                maturityStat.setCertStandardRate(BigDecimal.ZERO);
            }

            maturityStatistics.add(maturityStat);
        }

        // 8. 计算总计统计
        // 总计只统计L2和L3的成熟度
        // totalBaselineCount、totalCertifiedCount、totalSubject2PassCount已经在第6步中统计了（只包含L2和L3）

        CadreMaturityCertStatisticsVO totalStatistics = 
            new CadreMaturityCertStatisticsVO();
        totalStatistics.setMaturityLevel("总计");
        totalStatistics.setBaselineCount(totalBaselineCount);
        totalStatistics.setCertifiedCount(totalCertifiedCount);
        totalStatistics.setSubject2PassCount(totalSubject2PassCount);
        totalStatistics.setCertStandardCount(totalCertStandardCount);
        totalStatistics.setJobCategoryStatistics(null);

        // 计算总计认证率
        if (totalBaselineCount > 0) {
            BigDecimal totalCertRate = new BigDecimal(totalCertifiedCount)
                    .divide(new BigDecimal(totalBaselineCount), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(100));
            totalStatistics.setCertRate(totalCertRate);
        } else {
            totalStatistics.setCertRate(BigDecimal.ZERO);
        }

        // 计算总计科目二通过率
        if (totalBaselineCount > 0) {
            BigDecimal totalSubject2PassRate = new BigDecimal(totalSubject2PassCount)
                    .divide(new BigDecimal(totalBaselineCount), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(100));
            totalStatistics.setSubject2PassRate(totalSubject2PassRate);
        } else {
            totalStatistics.setSubject2PassRate(BigDecimal.ZERO);
        }

        // 计算总计持证率
        if (totalBaselineCount > 0) {
            BigDecimal totalCertStandardRate = new BigDecimal(totalCertStandardCount)
                    .divide(new BigDecimal(totalBaselineCount), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(100));
            totalStatistics.setCertStandardRate(totalCertStandardRate);
        } else {
            totalStatistics.setCertStandardRate(BigDecimal.ZERO);
        }

        // 9. 构建返回结果
        CadreMaturityJobCategoryCertStatisticsResponseVO response = 
            new CadreMaturityJobCategoryCertStatisticsResponseVO();
        response.setDeptCode(deptCode);
        response.setDeptName(deptName);
        response.setMaturityStatistics(maturityStatistics);
        response.setTotalStatistics(totalStatistics);

        return response;
    }

    /**
     * 查询干部任职数据（按成熟度和职位类统计，仅L2和L3）
     * @param deptCode 部门ID（部门编码），当为"0"时查询云核心网（030681）下的所有部门
     * @return 干部成熟度职位类任职统计响应
     */
    public CadreMaturityJobCategoryQualifiedStatisticsResponseVO getCadreMaturityJobCategoryQualifiedStatistics(String deptCode) {
        String actualDeptCode = deptCode;
        String deptName;
        
        // 特殊处理：当 deptCode 为 "0" 时，查询云核心网产品线下的所有部门
        if ("0".equals(deptCode)) {
            actualDeptCode = DepartmentConstants.CLOUD_CORE_NETWORK_DEPT_CODE;
            deptName = "云核心网";
        } else {
            deptName = null; // 稍后从数据库查询
        }
        
        // 1. 查询部门信息
        DepartmentInfoVO deptInfo = departmentInfoMapper.getDepartmentByCode(actualDeptCode);
        if (deptInfo == null) {
            throw new IllegalArgumentException("部门不存在：" + actualDeptCode);
        }
        
        // 如果deptName还没有设置，使用查询到的部门名称
        if (deptName == null) {
            deptName = deptInfo.getDeptName();
        }

        // 2. 查询该部门下的所有子部门（包括所有层级）
        List<DepartmentInfoVO> allSubDepts = departmentInfoMapper.getAllSubDepartments(actualDeptCode);
        
        // 构造部门编码列表（包括本部门本身和所有子部门）
        List<String> deptCodeList = new ArrayList<>();
        deptCodeList.add(actualDeptCode);
        if (allSubDepts != null && !allSubDepts.isEmpty()) {
            for (DepartmentInfoVO subDept : allSubDepts) {
                if (subDept.getDeptCode() != null && !subDept.getDeptCode().trim().isEmpty()) {
                    deptCodeList.add(subDept.getDeptCode());
                }
            }
        }

        // 3. 查询这些部门下的所有干部信息（工号、部门编码、职位类）
        List<CadreInfoVO> allCadres = cadreMapper.getCadreInfoByDeptCodes(deptCodeList);
        if (allCadres == null || allCadres.isEmpty()) {
            // 如果没有干部，返回空统计
            CadreMaturityJobCategoryQualifiedStatisticsResponseVO response = 
                new CadreMaturityJobCategoryQualifiedStatisticsResponseVO();
            response.setDeptCode(deptCode);
            response.setDeptName(deptName);
            response.setMaturityStatistics(new ArrayList<>());
            CadreMaturityQualifiedStatisticsVO total = 
                new CadreMaturityQualifiedStatisticsVO();
            total.setMaturityLevel("总计");
            total.setBaselineCount(0);
            total.setQualifiedCount(0);
            total.setQualifiedRate(BigDecimal.ZERO);
            total.setJobCategoryStatistics(null);
            response.setTotalStatistics(total);
            return response;
        }

        // 4. 提取所有干部工号，查询任职情况
        List<String> allEmployeeNumbers = allCadres.stream()
                .map(CadreInfoVO::getEmployeeNumber)
                .filter(num -> num != null && !num.trim().isEmpty())
                .distinct()
                .collect(Collectors.toList());

        // 5. 查询已获得任职的干部工号列表（直接使用干部工号查询）
        List<String> qualifiedNumbers = getQualifiedEmployeeNumbers(allEmployeeNumbers);
        Set<String> qualifiedSet = new HashSet<>(qualifiedNumbers != null ? qualifiedNumbers : new ArrayList<>());

        // 6. 按成熟度和职位类分组统计（仅统计L2和L3）
        // 结构：成熟度 -> 职位类 -> 统计信息
        Map<String, Map<String, CadreJobCategoryQualifiedStatisticsVO>> maturityJobCategoryMap = new HashMap<>();
        
        // 用于统计L2和L3的总基数（包含所有职位类）
        Map<String, Integer> maturityTotalBaselineMap = new HashMap<>();
        Map<String, Integer> maturityTotalQualifiedMap = new HashMap<>();
        Map<String, Integer> maturityTotalQualifiedByRequirementMap = new HashMap<>();
        
        int totalBaselineCount = 0;
        int totalQualifiedCount = 0;
        int totalQualifiedByRequirementCount = 0;

        for (CadreInfoVO cadre : allCadres) {
            // 直接从干部信息中获取AI成熟度（position_ai_maturity字段）
            String aiMaturity = cadre.getAiMaturity();
            if (aiMaturity == null || aiMaturity.trim().isEmpty()) {
                aiMaturity = "未知";
            }

            // 只统计L2和L3的成熟度
            if (!"L2".equals(aiMaturity) && !"L3".equals(aiMaturity)) {
                continue;
            }

            String jobCategory = cadre.getJobCategory();
            if (jobCategory == null || jobCategory.trim().isEmpty()) {
                jobCategory = "未知";
            }

            String employeeNumber = cadre.getEmployeeNumber();
            Integer isQualificationsStandard = cadre.getIsQualificationsStandard();

            // 统计成熟度的总基数（所有职位类）
            // 注意：对于L2和L3成熟度，总基数需要统计软件类以及非软件类员工
            maturityTotalBaselineMap.put(aiMaturity, maturityTotalBaselineMap.getOrDefault(aiMaturity, 0) + 1);
            totalBaselineCount++;
            
            // 统计成熟度的总任职人数（所有职位类）
            // 注意：对于L2和L3成熟度，总任职人数需要统计软件类以及非软件类员工
            if (employeeNumber != null && qualifiedSet.contains(employeeNumber)) {
                maturityTotalQualifiedMap.put(aiMaturity, maturityTotalQualifiedMap.getOrDefault(aiMaturity, 0) + 1);
                totalQualifiedCount++;
            }
            
            // 统计成熟度的总按要求任职人数（所有职位类）
            // is_qualifications_standard=1表示按要求达标
            if (isQualificationsStandard != null && isQualificationsStandard == 1) {
                maturityTotalQualifiedByRequirementMap.put(aiMaturity, maturityTotalQualifiedByRequirementMap.getOrDefault(aiMaturity, 0) + 1);
                totalQualifiedByRequirementCount++;
            }

            // L2和L3都返回软件类和非软件类员工数据（即所有职位类）
            // 注意：L2和L3的总基数、总任职人数已经在上面的统计中包含了所有职位类
            boolean shouldInclude = false;
            if ("L2".equals(aiMaturity) || "L3".equals(aiMaturity)) {
                // L2和L3都返回所有职位类（软件类和非软件类）
                shouldInclude = true;
            }

            // 所有符合条件的职位类都加入到jobCategoryStatistics中（L2和L3都包含所有职位类）
            if (shouldInclude) {
                // 获取或创建成熟度对应的职位类Map
                Map<String, CadreJobCategoryQualifiedStatisticsVO> jobCategoryMap = 
                    maturityJobCategoryMap.getOrDefault(aiMaturity, new HashMap<>());

                // 获取或创建职位类统计对象
                CadreJobCategoryQualifiedStatisticsVO jobCategoryStat = 
                    jobCategoryMap.getOrDefault(jobCategory, new CadreJobCategoryQualifiedStatisticsVO());
                jobCategoryStat.setJobCategory(jobCategory);

                // 累加基数人数（只统计符合条件的职位类）
                if (jobCategoryStat.getBaselineCount() == null) {
                    jobCategoryStat.setBaselineCount(0);
                }
                jobCategoryStat.setBaselineCount(jobCategoryStat.getBaselineCount() + 1);

                // 检查是否已任职
                if (employeeNumber != null && qualifiedSet.contains(employeeNumber)) {
                    if (jobCategoryStat.getQualifiedCount() == null) {
                        jobCategoryStat.setQualifiedCount(0);
                    }
                    jobCategoryStat.setQualifiedCount(jobCategoryStat.getQualifiedCount() + 1);
                }
                
                // 统计按要求AI任职人数（is_qualifications_standard=1）
                if (isQualificationsStandard != null && isQualificationsStandard == 1) {
                    if (jobCategoryStat.getQualifiedByRequirementCount() == null) {
                        jobCategoryStat.setQualifiedByRequirementCount(0);
                    }
                    jobCategoryStat.setQualifiedByRequirementCount(jobCategoryStat.getQualifiedByRequirementCount() + 1);
                }

                jobCategoryMap.put(jobCategory, jobCategoryStat);
                maturityJobCategoryMap.put(aiMaturity, jobCategoryMap);
            }
        }

        // 7. 计算每个职位类的任职率，并构建成熟度统计对象
        List<CadreMaturityQualifiedStatisticsVO> maturityStatistics = new ArrayList<>();
        
        // 按L2、L3的顺序处理
        for (String aiMaturity : new String[]{"L2", "L3"}) {
            Map<String, CadreJobCategoryQualifiedStatisticsVO> jobCategoryMap = maturityJobCategoryMap.get(aiMaturity);
            if (jobCategoryMap == null) {
                jobCategoryMap = new HashMap<>();
            }

            // 创建成熟度统计对象
            CadreMaturityQualifiedStatisticsVO maturityStat = 
                new CadreMaturityQualifiedStatisticsVO();
            maturityStat.setMaturityLevel(aiMaturity);

            // 使用所有职位类的总基数（从maturityTotalBaselineMap获取）
            int maturityBaselineCount = maturityTotalBaselineMap.getOrDefault(aiMaturity, 0);
            int maturityQualifiedCount = maturityTotalQualifiedMap.getOrDefault(aiMaturity, 0);
            int maturityQualifiedByRequirementCount = maturityTotalQualifiedByRequirementMap.getOrDefault(aiMaturity, 0);
            
            List<CadreJobCategoryQualifiedStatisticsVO> jobCategoryStatistics = new ArrayList<>();

            // 遍历该成熟度下的所有职位类（包含所有职位类：软件类和非软件类）
            for (CadreJobCategoryQualifiedStatisticsVO jobCategoryStat : jobCategoryMap.values()) {
                // L2和L3都返回所有职位类数据（包括软件类和非软件类）
                
                // 计算职位类任职率（基于该职位类的基数）
                if (jobCategoryStat.getBaselineCount() != null && jobCategoryStat.getBaselineCount() > 0) {
                    if (jobCategoryStat.getQualifiedCount() == null) {
                        jobCategoryStat.setQualifiedCount(0);
                    }
                    BigDecimal qualifiedRate = new BigDecimal(jobCategoryStat.getQualifiedCount())
                            .divide(new BigDecimal(jobCategoryStat.getBaselineCount()), 4, RoundingMode.HALF_UP)
                            .multiply(new BigDecimal(100));
                    jobCategoryStat.setQualifiedRate(qualifiedRate);
                    
                    // 计算职位类按要求任职人数占比
                    if (jobCategoryStat.getQualifiedByRequirementCount() == null) {
                        jobCategoryStat.setQualifiedByRequirementCount(0);
                    }
                    BigDecimal qualifiedByRequirementRate = new BigDecimal(jobCategoryStat.getQualifiedByRequirementCount())
                            .divide(new BigDecimal(jobCategoryStat.getBaselineCount()), 4, RoundingMode.HALF_UP)
                            .multiply(new BigDecimal(100));
                    jobCategoryStat.setQualifiedByRequirementRate(qualifiedByRequirementRate);
                } else {
                    jobCategoryStat.setQualifiedRate(BigDecimal.ZERO);
                    jobCategoryStat.setQualifiedByRequirementRate(BigDecimal.ZERO);
                }

                jobCategoryStatistics.add(jobCategoryStat);
            }

            // 设置成熟度统计数据（使用所有职位类的总数）
            maturityStat.setBaselineCount(maturityBaselineCount);
            maturityStat.setQualifiedCount(maturityQualifiedCount);
            maturityStat.setQualifiedByRequirementCount(maturityQualifiedByRequirementCount);
            maturityStat.setJobCategoryStatistics(jobCategoryStatistics);

            // 计算成熟度任职率（基于所有职位类的基数）
            if (maturityBaselineCount > 0) {
                BigDecimal qualifiedRate = new BigDecimal(maturityQualifiedCount)
                        .divide(new BigDecimal(maturityBaselineCount), 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal(100));
                maturityStat.setQualifiedRate(qualifiedRate);
                
                // 计算成熟度按要求任职人数占比
                BigDecimal qualifiedByRequirementRate = new BigDecimal(maturityQualifiedByRequirementCount)
                        .divide(new BigDecimal(maturityBaselineCount), 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal(100));
                maturityStat.setQualifiedByRequirementRate(qualifiedByRequirementRate);
            } else {
                maturityStat.setQualifiedRate(BigDecimal.ZERO);
                maturityStat.setQualifiedByRequirementRate(BigDecimal.ZERO);
            }

            maturityStatistics.add(maturityStat);
        }

        // 8. 计算总计统计
        // 总计只统计L2和L3的成熟度
        // totalBaselineCount、totalQualifiedCount已经在第6步中统计了（只包含L2和L3）

        CadreMaturityQualifiedStatisticsVO totalStatistics = 
            new CadreMaturityQualifiedStatisticsVO();
        totalStatistics.setMaturityLevel("总计");
        totalStatistics.setBaselineCount(totalBaselineCount);
        totalStatistics.setQualifiedCount(totalQualifiedCount);
        totalStatistics.setQualifiedByRequirementCount(totalQualifiedByRequirementCount);
        totalStatistics.setJobCategoryStatistics(null);

        // 计算总计任职率
        if (totalBaselineCount > 0) {
            BigDecimal totalQualifiedRate = new BigDecimal(totalQualifiedCount)
                    .divide(new BigDecimal(totalBaselineCount), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(100));
            totalStatistics.setQualifiedRate(totalQualifiedRate);
            
            // 计算总计按要求任职人数占比
            BigDecimal totalQualifiedByRequirementRate = new BigDecimal(totalQualifiedByRequirementCount)
                    .divide(new BigDecimal(totalBaselineCount), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(100));
            totalStatistics.setQualifiedByRequirementRate(totalQualifiedByRequirementRate);
        } else {
            totalStatistics.setQualifiedRate(BigDecimal.ZERO);
            totalStatistics.setQualifiedByRequirementRate(BigDecimal.ZERO);
        }

        // 9. 构建返回结果
        CadreMaturityJobCategoryQualifiedStatisticsResponseVO response = 
            new CadreMaturityJobCategoryQualifiedStatisticsResponseVO();
        response.setDeptCode(deptCode);
        response.setDeptName(deptName);
        response.setMaturityStatistics(maturityStatistics);
        response.setTotalStatistics(totalStatistics);

        return response;
    }

    /**
     * 更新L2、L3干部的AI任职达标情况和认证达标情况
     * 
     * 任职要求：
     * - L3干部的AI任职需要达到4+（不包括四级），即5级、6级、7级、8级
     * - L2专家的AI任职需要达到3+（不包括3级），即4级、5级、6级、7级、8级
     * 如果满足要求，将干部表中的is_qualifications_standard字段更新为1
     * 
     * 认证要求：
     * - 软件类的L2L3干部需要有专业级证书，才算达标，刷新表is_cert_standard字段为1
     * - L2L3的非软件类，需要通过工作级科目二或者专业级科目二，即t_exam_record表中存在exam_code为
     *   （EXCN022303075ZA20，EXCN022303075ZA2E，EXCN022303075ZA2A）且is_pass为1的数据，
     *   如果满足，将is_cert_standard设为1
     * 
     * @return 更新结果信息（包含更新的干部数量）
     */
    public Map<String, Object> updateCadreQualificationStandard() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 1. 查询所有L2、L3干部及其最高AI任职级别
            List<CadreQualificationVO> cadreList = cadreMapper.getL2L3CadreWithHighestQualification();
            
            if (cadreList == null || cadreList.isEmpty()) {
                result.put("success", true);
                result.put("message", "未找到L2、L3干部数据");
                result.put("totalCount", 0);
                result.put("qualifiedCount", 0);
                result.put("unqualifiedCount", 0);
                return result;
            }
            
            // 2. 收集所有L2、L3干部（用于重置）
            List<CadreQualificationVO> allL2L3Cadres = new ArrayList<>();
            
            // 3. 收集任职达标的干部
            List<CadreQualificationVO> qualifiedCadres = new ArrayList<>();
            // 4. 收集认证达标的干部
            List<CadreQualificationVO> certQualifiedCadres = new ArrayList<>();
            
            int l2QualifiedCount = 0;
            int l3QualifiedCount = 0;
            int l2UnqualifiedCount = 0;
            int l3UnqualifiedCount = 0;
            int l2CertQualifiedCount = 0;
            int l3CertQualifiedCount = 0;
            int l2CertUnqualifiedCount = 0;
            int l3CertUnqualifiedCount = 0;
            
            // 5. 遍历每个干部，判断是否达标
            for (CadreQualificationVO cadre : cadreList) {
                String employeeNumber = cadre.getEmployeeNumber();
                String aiMaturity = cadre.getAiMaturity();
                String highestQualificationLevel = cadre.getHighestQualificationLevel();
                String jobCategory = cadre.getJobCategory();
                Integer hasProfessionalCert = cadre.getHasProfessionalCert();
                Integer hasPassedSubject2 = cadre.getHasPassedSubject2();
                
                if (employeeNumber == null || employeeNumber.trim().isEmpty()) {
                    continue;
                }
                
                allL2L3Cadres.add(cadre);
                
                // 判断任职是否达标
                boolean isQualified = false;
                
                if ("L3".equals(aiMaturity)) {
                    // L3干部的AI任职需要达到4+（包括四级），即4级、5级、6级、7级、8级
                    if (highestQualificationLevel != null && !highestQualificationLevel.trim().isEmpty()) {
                        if ("4级".equals(highestQualificationLevel)
                                || "5级".equals(highestQualificationLevel) 
                                || "6级".equals(highestQualificationLevel)
                                || "7级".equals(highestQualificationLevel)
                                || "8级".equals(highestQualificationLevel)) {
                            isQualified = true;
                            l3QualifiedCount++;
                        } else {
                            l3UnqualifiedCount++;
                        }
                    } else {
                        // 没有任职记录，不达标
                        l3UnqualifiedCount++;
                    }
                } else if ("L2".equals(aiMaturity)) {
                    // L2专家的AI任职需要达到3+（包括三级），即3级、4级、5级、6级、7级、8级
                    if (highestQualificationLevel != null && !highestQualificationLevel.trim().isEmpty()) {
                        if ("3级".equals(highestQualificationLevel)
                                || "4级".equals(highestQualificationLevel)
                                || "5级".equals(highestQualificationLevel)
                                || "6级".equals(highestQualificationLevel)
                                || "7级".equals(highestQualificationLevel)
                                || "8级".equals(highestQualificationLevel)) {
                            isQualified = true;
                            l2QualifiedCount++;
                        } else {
                            l2UnqualifiedCount++;
                        }
                    } else {
                        // 没有任职记录，不达标
                        l2UnqualifiedCount++;
                    }
                }
                
                if (isQualified) {
                    qualifiedCadres.add(cadre);
                }
                
                // 判断认证是否达标
                boolean isCertQualified = false;
                boolean isSoftwareCategory = jobCategory != null && jobCategory.equals("软件类");
                
                if (isSoftwareCategory) {
                    // 软件类的L2L3干部需要有专业级证书，才算达标
                    if (hasProfessionalCert != null && hasProfessionalCert == 1) {
                        isCertQualified = true;
                        if ("L2".equals(aiMaturity)) {
                            l2CertQualifiedCount++;
                        } else if ("L3".equals(aiMaturity)) {
                            l3CertQualifiedCount++;
                        }
                    } else {
                        if ("L2".equals(aiMaturity)) {
                            l2CertUnqualifiedCount++;
                        } else if ("L3".equals(aiMaturity)) {
                            l3CertUnqualifiedCount++;
                        }
                    }
                } else {
                    // L2L3的非软件类，需要通过工作级科目二或者专业级科目二
                    // 即t_exam_record表中存在exam_code为（EXCN022303075ZA20，EXCN022303075ZA2E，EXCN022303075ZA2A）且is_pass为1的数据
                    if (hasPassedSubject2 != null && hasPassedSubject2 == 1) {
                        isCertQualified = true;
                        if ("L2".equals(aiMaturity)) {
                            l2CertQualifiedCount++;
                        } else if ("L3".equals(aiMaturity)) {
                            l3CertQualifiedCount++;
                        }
                    } else {
                        if ("L2".equals(aiMaturity)) {
                            l2CertUnqualifiedCount++;
                        } else if ("L3".equals(aiMaturity)) {
                            l3CertUnqualifiedCount++;
                        }
                    }
                }
                
                if (isCertQualified) {
                    certQualifiedCadres.add(cadre);
                }
            }
            
            // 6. 先重置所有L2、L3干部的is_qualifications_standard字段为0
            if (!allL2L3Cadres.isEmpty()) {
                cadreMapper.batchResetQualificationStandard(allL2L3Cadres);
            }
            
            // 7. 批量更新任职达标的干部is_qualifications_standard字段为1
            int updatedQualificationCount = 0;
            if (!qualifiedCadres.isEmpty()) {
                updatedQualificationCount = cadreMapper.batchUpdateQualificationStandard(qualifiedCadres);
            }
            
            // 8. 先重置所有L2、L3干部的is_cert_standard字段为0
            if (!allL2L3Cadres.isEmpty()) {
                cadreMapper.batchResetCertStandard(allL2L3Cadres);
            }
            
            // 9. 批量更新认证达标的干部is_cert_standard字段为1
            int updatedCertCount = 0;
            if (!certQualifiedCadres.isEmpty()) {
                updatedCertCount = cadreMapper.batchUpdateCertStandard(certQualifiedCadres);
            }
            
            // 10. 构建返回结果
            result.put("success", true);
            result.put("message", "更新成功");
            result.put("totalCount", allL2L3Cadres.size());
            // 任职达标统计
            result.put("qualifiedCount", qualifiedCadres.size());
            result.put("unqualifiedCount", allL2L3Cadres.size() - qualifiedCadres.size());
            result.put("updatedQualificationCount", updatedQualificationCount);
            result.put("l2QualifiedCount", l2QualifiedCount);
            result.put("l2UnqualifiedCount", l2UnqualifiedCount);
            result.put("l3QualifiedCount", l3QualifiedCount);
            result.put("l3UnqualifiedCount", l3UnqualifiedCount);
            // 认证达标统计
            result.put("certQualifiedCount", certQualifiedCadres.size());
            result.put("certUnqualifiedCount", allL2L3Cadres.size() - certQualifiedCadres.size());
            result.put("updatedCertCount", updatedCertCount);
            result.put("l2CertQualifiedCount", l2CertQualifiedCount);
            result.put("l2CertUnqualifiedCount", l2CertUnqualifiedCount);
            result.put("l3CertQualifiedCount", l3CertQualifiedCount);
            result.put("l3CertUnqualifiedCount", l3CertUnqualifiedCount);
            
            return result;
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "更新失败：" + e.getMessage());
            result.put("error", e.getClass().getName());
            return result;
        }
    }

    /**
     * 更新L2、L3干部的AI认证达标情况
     * 
     * 认证达标规则：
     * - 所有干部（软件类和非软件类）如果持有AI专业级证书，视为认证达标
     * - 非软件类干部如果通过了专业级科目二考试（exam_code为'EXCN022303075ZA20'、'EXCN022303075ZA2E'或'EXCN022303075ZA2A'之一），视为认证达标
     * 如果满足任一条件，将干部表中的is_cert_standard字段更新为1
     * 
     * @return 更新结果信息（包含更新的干部数量）
     */
    public Map<String, Object> updateCadreCertStandard() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 1. 查询所有L2、L3干部及其专业级证书和专业级科目二通过情况
            List<CadreQualificationVO> cadreList = cadreMapper.getL2L3CadreWithCertInfo();
            
            if (cadreList == null || cadreList.isEmpty()) {
                result.put("success", true);
                result.put("message", "未找到L2、L3干部数据");
                result.put("totalCount", 0);
                result.put("certQualifiedCount", 0);
                result.put("certUnqualifiedCount", 0);
                result.put("updatedCertCount", 0);
                result.put("l2CertQualifiedCount", 0);
                result.put("l2CertUnqualifiedCount", 0);
                result.put("l3CertQualifiedCount", 0);
                result.put("l3CertUnqualifiedCount", 0);
                return result;
            }
            
            // 2. 收集所有L2、L3干部（用于重置）
            List<CadreQualificationVO> allL2L3Cadres = new ArrayList<>();
            // 3. 收集认证达标的干部
            List<CadreQualificationVO> certQualifiedCadres = new ArrayList<>();
            
            int l2CertQualifiedCount = 0;
            int l3CertQualifiedCount = 0;
            int l2CertUnqualifiedCount = 0;
            int l3CertUnqualifiedCount = 0;
            
            // 4. 遍历每个干部，判断是否达标
            for (CadreQualificationVO cadre : cadreList) {
                String employeeNumber = cadre.getEmployeeNumber();
                String aiMaturity = cadre.getAiMaturity();
                String jobCategory = cadre.getJobCategory();
                Integer hasProfessionalCert = cadre.getHasProfessionalCert();
                Integer hasPassedProfessionalSubject2 = cadre.getHasPassedProfessionalSubject2();
                
                if (employeeNumber == null || employeeNumber.trim().isEmpty()) {
                    continue;
                }
                
                // 只处理L2和L3的成熟度
                if (!"L2".equals(aiMaturity) && !"L3".equals(aiMaturity)) {
                    continue;
                }
                
                allL2L3Cadres.add(cadre);
                
                // 判断认证是否达标
                boolean isCertQualified = false;
                
                // 规则1：所有干部持有AI专业级证书，视为达标
                if (hasProfessionalCert != null && hasProfessionalCert == 1) {
                    isCertQualified = true;
                }
                
                // 规则2：非软件类干部通过专业级科目二考试，视为达标
                if (!isCertQualified) {
                    boolean isSoftwareCategory = jobCategory != null && jobCategory.equals("软件类");
                    if (!isSoftwareCategory && hasPassedProfessionalSubject2 != null && hasPassedProfessionalSubject2 == 1) {
                        isCertQualified = true;
                    }
                }
                
                if (isCertQualified) {
                    certQualifiedCadres.add(cadre);
                    if ("L2".equals(aiMaturity)) {
                        l2CertQualifiedCount++;
                    } else if ("L3".equals(aiMaturity)) {
                        l3CertQualifiedCount++;
                    }
                } else {
                    if ("L2".equals(aiMaturity)) {
                        l2CertUnqualifiedCount++;
                    } else if ("L3".equals(aiMaturity)) {
                        l3CertUnqualifiedCount++;
                    }
                }
            }
            
            // 5. 先重置所有L2、L3干部的is_cert_standard字段为0
            if (!allL2L3Cadres.isEmpty()) {
                cadreMapper.batchResetCertStandard(allL2L3Cadres);
            }
            
            // 6. 批量更新认证达标的干部is_cert_standard字段为1
            int updatedCertCount = 0;
            if (!certQualifiedCadres.isEmpty()) {
                updatedCertCount = cadreMapper.batchUpdateCertStandard(certQualifiedCadres);
            }
            
            // 7. 构建返回结果
            result.put("success", true);
            result.put("message", "更新成功");
            result.put("totalCount", allL2L3Cadres.size());
            result.put("certQualifiedCount", certQualifiedCadres.size());
            result.put("certUnqualifiedCount", allL2L3Cadres.size() - certQualifiedCadres.size());
            result.put("updatedCertCount", updatedCertCount);
            result.put("l2CertQualifiedCount", l2CertQualifiedCount);
            result.put("l2CertUnqualifiedCount", l2CertUnqualifiedCount);
            result.put("l3CertQualifiedCount", l3CertQualifiedCount);
            result.put("l3CertUnqualifiedCount", l3CertUnqualifiedCount);
            
            return result;
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "更新失败：" + e.getMessage());
            result.put("error", e.getClass().getName());
            return result;
        }
    }

    /**
     * 更新L2、L3专家的AI任职达标情况
     * 
     * 任职要求：
     * - L2软件类专家：AI任职需要达到3+（包括三级），即3级、4级、5级、6级、7级、8级
     * - L3级别的所有职位类专家：根据专家的职级判断（职级在t_expert表的orig_position_grade字段）
     *   - 19级专家：要求4+AI任职（4级、5级、6级、7级、8级）
     *   - 20级专家：要求5+AI任职（5级、6级、7级、8级）
     *   - 21级专家：要求6+AI任职（6级、7级、8级）
     *   - 22级专家：要求7+AI任职（7级、8级）
     *   - 23+级专家：要求8级AI任职
     * 如果满足要求，将专家表中的is_qualifications_standard字段更新为1
     * 
     * @return 更新结果信息（包含更新的专家数量）
     */
    public Map<String, Object> updateExpertQualificationStandard() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 1. 查询所有L2、L3专家及其最高AI任职级别和职位类
            List<ExpertQualificationVO> expertList = expertMapper.getL2L3ExpertWithHighestQualification();
            
            if (expertList == null || expertList.isEmpty()) {
                result.put("success", true);
                result.put("message", "未找到L2、L3专家数据");
                result.put("totalCount", 0);
                result.put("qualifiedCount", 0);
                result.put("unqualifiedCount", 0);
                result.put("updatedQualificationCount", 0);
                result.put("l2QualifiedCount", 0);
                result.put("l2UnqualifiedCount", 0);
                result.put("l3QualifiedCount", 0);
                result.put("l3UnqualifiedCount", 0);
                return result;
            }
            
            // 2. 收集所有L2、L3专家的工号（用于重置）
            List<String> allL2L3EmployeeNumbers = new ArrayList<>();
            // 3. 收集任职达标的专家工号
            List<String> qualifiedEmployeeNumbers = new ArrayList<>();
            
            int l2QualifiedCount = 0;
            int l3QualifiedCount = 0;
            int l2UnqualifiedCount = 0;
            int l3UnqualifiedCount = 0;
            
            // 4. 遍历每个专家，判断是否达标
            for (ExpertQualificationVO expert : expertList) {
                String employeeNumber = expert.getEmployeeNumber();
                String aiMaturity = expert.getAiMaturity();
                String highestQualificationLevel = expert.getHighestQualificationLevel();
                String jobCategoryFull = expert.getJobCategory();
                Integer origPositionGrade = expert.getOrigPositionGrade();
                
                if (employeeNumber == null || employeeNumber.trim().isEmpty()) {
                    continue;
                }
                
                // 只处理L2和L3的成熟度
                if (!"L2".equals(aiMaturity) && !"L3".equals(aiMaturity)) {
                    continue;
                }
                
                // 提取职位类（从"职位族-职位类-职位子类"格式中提取中间的职位类）
                String jobCategory = extractJobCategory(jobCategoryFull);
                
                // 判断任职是否达标
                boolean isQualified = false;
                
                if ("L3".equals(aiMaturity)) {
                    // L3级别的所有职位类专家：根据专家的职级判断
                    // 19级专家要求4+AI任职，20级要求5+AI任职，21级要求6+AI任职，22级要求7+AI任职，23+级要求8级AI任职
                    allL2L3EmployeeNumbers.add(employeeNumber);
                    
                    if (highestQualificationLevel == null || highestQualificationLevel.trim().isEmpty()) {
                        // 没有任职记录，不达标
                        l3UnqualifiedCount++;
                    } else {
                        // 根据职级判断是否达标
                        boolean meetsRequirement = false;
                        
                        if (origPositionGrade != null) {
                            if (origPositionGrade == 19) {
                                // 19级：要求4+AI任职（4级、5级、6级、7级、8级）
                                meetsRequirement = "4级".equals(highestQualificationLevel)
                                        || "5级".equals(highestQualificationLevel)
                                        || "6级".equals(highestQualificationLevel)
                                        || "7级".equals(highestQualificationLevel)
                                        || "8级".equals(highestQualificationLevel);
                            } else if (origPositionGrade == 20) {
                                // 20级：要求5+AI任职（5级、6级、7级、8级）
                                meetsRequirement = "5级".equals(highestQualificationLevel)
                                        || "6级".equals(highestQualificationLevel)
                                        || "7级".equals(highestQualificationLevel)
                                        || "8级".equals(highestQualificationLevel);
                            } else if (origPositionGrade == 21) {
                                // 21级：要求6+AI任职（6级、7级、8级）
                                meetsRequirement = "6级".equals(highestQualificationLevel)
                                        || "7级".equals(highestQualificationLevel)
                                        || "8级".equals(highestQualificationLevel);
                            } else if (origPositionGrade == 22) {
                                // 22级：要求7+AI任职（7级、8级）
                                meetsRequirement = "7级".equals(highestQualificationLevel)
                                        || "8级".equals(highestQualificationLevel);
                            } else if (origPositionGrade >= 23) {
                                // 23+级：要求8级AI任职
                                meetsRequirement = "8级".equals(highestQualificationLevel);
                            }
                        }
                        
                        if (meetsRequirement) {
                            isQualified = true;
                            l3QualifiedCount++;
                        } else {
                            l3UnqualifiedCount++;
                        }
                    }
                } else if ("L2".equals(aiMaturity)) {
                    // L2软件类专家：AI任职需要达到3+（包括三级），即3级、4级、5级、6级、7级、8级
                    boolean isSoftwareCategory = jobCategory != null && jobCategory.equals("软件类");
                    if (!isSoftwareCategory) {
                        continue;
                    }
                    
                    allL2L3EmployeeNumbers.add(employeeNumber);
                    
                    if (highestQualificationLevel != null && !highestQualificationLevel.trim().isEmpty()) {
                        if ("3级".equals(highestQualificationLevel)
                                || "4级".equals(highestQualificationLevel)
                                || "5级".equals(highestQualificationLevel)
                                || "6级".equals(highestQualificationLevel)
                                || "7级".equals(highestQualificationLevel)
                                || "8级".equals(highestQualificationLevel)) {
                            isQualified = true;
                            l2QualifiedCount++;
                        } else {
                            l2UnqualifiedCount++;
                        }
                    } else {
                        // 没有任职记录，不达标
                        l2UnqualifiedCount++;
                    }
                }
                
                if (isQualified) {
                    qualifiedEmployeeNumbers.add(employeeNumber);
                }
            }
            
            // 5. 先重置所有L2软件类专家和L3所有职位类专家的is_qualifications_standard字段为0
            if (!allL2L3EmployeeNumbers.isEmpty()) {
                expertMapper.batchResetQualificationStandard(allL2L3EmployeeNumbers);
            }
            
            // 6. 批量更新任职达标的专家is_qualifications_standard字段为1
            int updatedQualificationCount = 0;
            if (!qualifiedEmployeeNumbers.isEmpty()) {
                updatedQualificationCount = expertMapper.batchUpdateQualificationStandard(qualifiedEmployeeNumbers);
            }
            
            // 7. 构建返回结果
            result.put("success", true);
            result.put("message", "更新成功");
            result.put("totalCount", allL2L3EmployeeNumbers.size());
            result.put("qualifiedCount", qualifiedEmployeeNumbers.size());
            result.put("unqualifiedCount", allL2L3EmployeeNumbers.size() - qualifiedEmployeeNumbers.size());
            result.put("updatedQualificationCount", updatedQualificationCount);
            result.put("l2QualifiedCount", l2QualifiedCount);
            result.put("l2UnqualifiedCount", l2UnqualifiedCount);
            result.put("l3QualifiedCount", l3QualifiedCount);
            result.put("l3UnqualifiedCount", l3UnqualifiedCount);
            
            return result;
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "更新失败：" + e.getMessage());
            result.put("error", e.getClass().getName());
            return result;
        }
    }

    /**
     * 更新L2、L3专家的认证达标情况
     * 
     * 认证达标规则：
     * - 所有L2、L3专家（所有职位类）：如果持有专业级证书即为达标，否则视为不达标
     * 如果满足条件，将专家表中的is_cert_standard字段更新为1，不达标为0
     * 
     * @return 更新结果信息（包含更新的专家数量）
     */
    public Map<String, Object> updateExpertCertStandard() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 1. 查询所有L2、L3专家及其专业级证书情况
            List<ExpertQualificationVO> expertList = expertMapper.getL2L3ExpertWithCertInfo();
            
            if (expertList == null || expertList.isEmpty()) {
                result.put("success", true);
                result.put("message", "未找到L2、L3专家数据");
                result.put("totalCount", 0);
                result.put("certQualifiedCount", 0);
                result.put("certUnqualifiedCount", 0);
                result.put("updatedCertCount", 0);
                result.put("l2CertQualifiedCount", 0);
                result.put("l2CertUnqualifiedCount", 0);
                result.put("l3CertQualifiedCount", 0);
                result.put("l3CertUnqualifiedCount", 0);
                return result;
            }
            
            // 2. 收集所有L2、L3专家的工号（用于重置）
            List<String> allL2L3EmployeeNumbers = new ArrayList<>();
            // 3. 收集认证达标的专家工号
            List<String> certQualifiedEmployeeNumbers = new ArrayList<>();
            
            int l2CertQualifiedCount = 0;
            int l3CertQualifiedCount = 0;
            int l2CertUnqualifiedCount = 0;
            int l3CertUnqualifiedCount = 0;
            
            // 4. 遍历每个专家，判断是否达标
            for (ExpertQualificationVO expert : expertList) {
                String employeeNumber = expert.getEmployeeNumber();
                String aiMaturity = expert.getAiMaturity();
                Integer hasProfessionalCert = expert.getHasProfessionalCert();
                
                if (employeeNumber == null || employeeNumber.trim().isEmpty()) {
                    continue;
                }
                
                // 只处理L2和L3的成熟度
                if (!"L2".equals(aiMaturity) && !"L3".equals(aiMaturity)) {
                    continue;
                }
                
                allL2L3EmployeeNumbers.add(employeeNumber);
                
                // 判断认证是否达标：所有专家持有专业级证书即为达标
                boolean isCertQualified = false;
                if (hasProfessionalCert != null && hasProfessionalCert == 1) {
                    isCertQualified = true;
                }
                
                if (isCertQualified) {
                    certQualifiedEmployeeNumbers.add(employeeNumber);
                    if ("L2".equals(aiMaturity)) {
                        l2CertQualifiedCount++;
                    } else if ("L3".equals(aiMaturity)) {
                        l3CertQualifiedCount++;
                    }
                } else {
                    if ("L2".equals(aiMaturity)) {
                        l2CertUnqualifiedCount++;
                    } else if ("L3".equals(aiMaturity)) {
                        l3CertUnqualifiedCount++;
                    }
                }
            }
            
            // 5. 先重置所有L2、L3专家的is_cert_standard字段为0
            if (!allL2L3EmployeeNumbers.isEmpty()) {
                expertMapper.batchResetCertStandard(allL2L3EmployeeNumbers);
            }
            
            // 6. 批量更新认证达标的专家is_cert_standard字段为1
            int updatedCertCount = 0;
            if (!certQualifiedEmployeeNumbers.isEmpty()) {
                updatedCertCount = expertMapper.batchUpdateCertStandard(certQualifiedEmployeeNumbers);
            }
            
            // 7. 构建返回结果
            result.put("success", true);
            result.put("message", "更新成功");
            result.put("totalCount", allL2L3EmployeeNumbers.size());
            result.put("certQualifiedCount", certQualifiedEmployeeNumbers.size());
            result.put("certUnqualifiedCount", allL2L3EmployeeNumbers.size() - certQualifiedEmployeeNumbers.size());
            result.put("updatedCertCount", updatedCertCount);
            result.put("l2CertQualifiedCount", l2CertQualifiedCount);
            result.put("l2CertUnqualifiedCount", l2CertUnqualifiedCount);
            result.put("l3CertQualifiedCount", l3CertQualifiedCount);
            result.put("l3CertUnqualifiedCount", l3CertUnqualifiedCount);
            
            return result;
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "更新失败：" + e.getMessage());
            result.put("error", e.getClass().getName());
            return result;
        }
    }

    /**
     * 查询专家AI认证数据
     * @param deptCode 部门ID（部门编码），当为"0"时，自动赋值为"云核心网产品线"部门ID
     * @return 专家AI认证统计结果
     */
    public ExpertAiCertStatisticsResponseVO getExpertAiCertStatistics(String deptCode) {
        String actualDeptCode = deptCode;
        String deptName;
        
        // 1. 参数处理：当deptCode为"0"时，使用云核心网产品线部门ID
        if ("0".equals(deptCode)) {
            actualDeptCode = DepartmentConstants.CLOUD_CORE_NETWORK_DEPT_CODE;
            deptName = "云核心网";
        } else {
            deptName = null; // 稍后从数据库查询
        }
        
        // 2. 查询部门信息
        DepartmentInfoVO deptInfo = departmentInfoMapper.getDepartmentByCode(actualDeptCode);
        if (deptInfo == null) {
            throw new IllegalArgumentException("部门不存在：" + actualDeptCode);
        }
        
        // 如果deptName还没有设置，使用查询到的部门名称
        if (deptName == null) {
            deptName = deptInfo.getDeptName();
        }
        
        // 获取部门层级，用于后续的部门过滤
        String deptLevelStr = deptInfo.getDeptLevel();
        Integer deptLevel = Integer.parseInt(deptLevelStr); // 转换为Integer类型，用于SQL判断
        
        // 3. 调用Mapper方法查询专家数据
        List<ExpertInfoVO> expertList = expertMapper.getExpertInfoByDeptCode(actualDeptCode, deptLevel);
        
        if (expertList == null || expertList.isEmpty()) {
            // 如果没有专家数据，返回空统计
            ExpertAiCertStatisticsResponseVO response = new ExpertAiCertStatisticsResponseVO();
            response.setDeptCode(deptCode);
            response.setDeptName(deptName);
            response.setMaturityStatistics(new ArrayList<>());
            ExpertMaturityCertStatisticsVO total = new ExpertMaturityCertStatisticsVO();
            total.setMaturityLevel("总计");
            total.setBaselineCount(0);
            total.setCertifiedCount(0);
            total.setCertRate(BigDecimal.ZERO);
            total.setJobCategoryStatistics(null);
            response.setTotalStatistics(total);
            return response;
        }
        
        // 4. 提取所有专家工号，查询认证状态
        List<String> allEmployeeNumbers = expertList.stream()
                .map(ExpertInfoVO::getEmployeeNumber)
                .filter(num -> num != null && !num.trim().isEmpty())
                .distinct()
                .collect(Collectors.toList());
        
        // 查询已通过认证的专家工号列表
        List<String> certifiedNumbers = getCertifiedEmployeeNumbers(allEmployeeNumbers);
        Set<String> certifiedSet = new HashSet<>(certifiedNumbers != null ? certifiedNumbers : new ArrayList<>());
        
        // 5. 按成熟度和职位类分组统计
        // 结构：成熟度 -> 职位类 -> 统计信息
        Map<String, Map<String, ExpertJobCategoryCertStatisticsVO>> maturityJobCategoryMap = new HashMap<>();
        
        // 用于统计L2和L3的总基数（包含所有职位类）
        Map<String, Integer> maturityTotalBaselineMap = new HashMap<>();
        Map<String, Integer> maturityTotalCertifiedMap = new HashMap<>();
        
        int totalBaselineCount = 0;
        int totalCertifiedCount = 0;
        
        for (ExpertInfoVO expert : expertList) {
            String aiMaturity = expert.getAiMaturity();
            if (aiMaturity == null || !aiMaturity.matches("L[23]")) {
                continue; // 只处理L2和L3
            }
            
            // 提取职位类
            String jobCategory = extractJobCategory(expert.getJobCategory());
            
            String employeeNumber = expert.getEmployeeNumber();
            
            // 统计成熟度的总基数（所有职位类）
            maturityTotalBaselineMap.put(aiMaturity, 
                maturityTotalBaselineMap.getOrDefault(aiMaturity, 0) + 1);
            totalBaselineCount++;
            
            // 统计成熟度的总认证人数（所有职位类）
            if (employeeNumber != null && certifiedSet.contains(employeeNumber)) {
                maturityTotalCertifiedMap.put(aiMaturity, 
                    maturityTotalCertifiedMap.getOrDefault(aiMaturity, 0) + 1);
                totalCertifiedCount++;
            }
            
            // 获取或创建成熟度对应的职位类Map
            Map<String, ExpertJobCategoryCertStatisticsVO> jobCategoryMap = 
                maturityJobCategoryMap.getOrDefault(aiMaturity, new HashMap<>());
            
            // 获取或创建职位类统计对象
            ExpertJobCategoryCertStatisticsVO jobCategoryStat = 
                jobCategoryMap.getOrDefault(jobCategory, new ExpertJobCategoryCertStatisticsVO());
            jobCategoryStat.setJobCategory(jobCategory);
            
            // 累加基数人数
            if (jobCategoryStat.getBaselineCount() == null) {
                jobCategoryStat.setBaselineCount(0);
            }
            jobCategoryStat.setBaselineCount(jobCategoryStat.getBaselineCount() + 1);
            
            // 检查是否已认证
            if (employeeNumber != null && certifiedSet.contains(employeeNumber)) {
                if (jobCategoryStat.getCertifiedCount() == null) {
                    jobCategoryStat.setCertifiedCount(0);
                }
                jobCategoryStat.setCertifiedCount(jobCategoryStat.getCertifiedCount() + 1);
            }
            
            jobCategoryMap.put(jobCategory, jobCategoryStat);
            maturityJobCategoryMap.put(aiMaturity, jobCategoryMap);
        }
        
        // 6. 计算每个职位类的认证率，并构建成熟度统计对象
        List<ExpertMaturityCertStatisticsVO> maturityStatistics = new ArrayList<>();
        
        // 按L2、L3的顺序处理
        for (String aiMaturity : new String[]{"L2", "L3"}) {
            Map<String, ExpertJobCategoryCertStatisticsVO> jobCategoryMap = 
                maturityJobCategoryMap.get(aiMaturity);
            if (jobCategoryMap == null) {
                jobCategoryMap = new HashMap<>();
            }
            
            // 创建成熟度统计对象
            ExpertMaturityCertStatisticsVO maturityStat = new ExpertMaturityCertStatisticsVO();
            maturityStat.setMaturityLevel(aiMaturity);
            
            // 使用所有职位类的总基数
            int maturityBaselineCount = maturityTotalBaselineMap.getOrDefault(aiMaturity, 0);
            int maturityCertifiedCount = maturityTotalCertifiedMap.getOrDefault(aiMaturity, 0);
            
            List<ExpertJobCategoryCertStatisticsVO> jobCategoryStatistics = new ArrayList<>();
            
            // 遍历该成熟度下的所有职位类
            for (ExpertJobCategoryCertStatisticsVO jobCategoryStat : jobCategoryMap.values()) {
                // 计算职位类认证率
                if (jobCategoryStat.getBaselineCount() != null && jobCategoryStat.getBaselineCount() > 0) {
                    if (jobCategoryStat.getCertifiedCount() == null) {
                        jobCategoryStat.setCertifiedCount(0);
                    }
                    BigDecimal certRate = new BigDecimal(jobCategoryStat.getCertifiedCount())
                            .divide(new BigDecimal(jobCategoryStat.getBaselineCount()), 4, RoundingMode.HALF_UP)
                            .multiply(new BigDecimal(100));
                    jobCategoryStat.setCertRate(certRate);
                } else {
                    jobCategoryStat.setCertRate(BigDecimal.ZERO);
                }
                
                jobCategoryStatistics.add(jobCategoryStat);
            }
            
            // 设置成熟度统计数据
            maturityStat.setBaselineCount(maturityBaselineCount);
            maturityStat.setCertifiedCount(maturityCertifiedCount);
            maturityStat.setJobCategoryStatistics(jobCategoryStatistics);
            
            // 计算成熟度认证率
            if (maturityBaselineCount > 0) {
                BigDecimal certRate = new BigDecimal(maturityCertifiedCount)
                        .divide(new BigDecimal(maturityBaselineCount), 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal(100));
                maturityStat.setCertRate(certRate);
            } else {
                maturityStat.setCertRate(BigDecimal.ZERO);
            }
            
            maturityStatistics.add(maturityStat);
        }
        
        // 7. 计算总计统计
        ExpertMaturityCertStatisticsVO totalStatistics = new ExpertMaturityCertStatisticsVO();
        totalStatistics.setMaturityLevel("总计");
        totalStatistics.setBaselineCount(totalBaselineCount);
        totalStatistics.setCertifiedCount(totalCertifiedCount);
        totalStatistics.setJobCategoryStatistics(null);
        
        // 计算总计认证率
        if (totalBaselineCount > 0) {
            BigDecimal totalCertRate = new BigDecimal(totalCertifiedCount)
                    .divide(new BigDecimal(totalBaselineCount), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(100));
            totalStatistics.setCertRate(totalCertRate);
        } else {
            totalStatistics.setCertRate(BigDecimal.ZERO);
        }
        
        // 8. 构建返回结果
        ExpertAiCertStatisticsResponseVO response = new ExpertAiCertStatisticsResponseVO();
        response.setDeptCode(deptCode);
        response.setDeptName(deptName);
        response.setMaturityStatistics(maturityStatistics);
        response.setTotalStatistics(totalStatistics);
        
        return response;
    }
    
    /**
     * 查询专家AI任职数据
     * @param deptCode 部门ID（部门编码），当为"0"时，自动赋值为"云核心网产品线"部门ID
     * @return 专家AI任职统计结果
     */
    public ExpertAiQualifiedStatisticsResponseVO getExpertAiQualifiedStatistics(String deptCode) {
        String actualDeptCode = deptCode;
        String deptName;
        
        // 1. 参数处理：当deptCode为"0"时，使用云核心网产品线部门ID
        if ("0".equals(deptCode)) {
            actualDeptCode = DepartmentConstants.CLOUD_CORE_NETWORK_DEPT_CODE;
            deptName = "云核心网";
        } else {
            deptName = null; // 稍后从数据库查询
        }
        
        // 2. 查询部门信息
        DepartmentInfoVO deptInfo = departmentInfoMapper.getDepartmentByCode(actualDeptCode);
        if (deptInfo == null) {
            throw new IllegalArgumentException("部门不存在：" + actualDeptCode);
        }
        
        // 如果deptName还没有设置，使用查询到的部门名称
        if (deptName == null) {
            deptName = deptInfo.getDeptName();
        }
        
        // 获取部门层级，用于后续的部门过滤
        String deptLevelStr = deptInfo.getDeptLevel();
        Integer deptLevel = Integer.parseInt(deptLevelStr); // 转换为Integer类型，用于SQL判断
        
        // 3. 调用Mapper方法查询专家数据
        List<ExpertInfoVO> expertList = expertMapper.getExpertInfoByDeptCode(actualDeptCode, deptLevel);
        
        if (expertList == null || expertList.isEmpty()) {
            // 如果没有专家数据，返回空统计
            ExpertAiQualifiedStatisticsResponseVO response = new ExpertAiQualifiedStatisticsResponseVO();
            response.setDeptCode(deptCode);
            response.setDeptName(deptName);
            response.setMaturityStatistics(new ArrayList<>());
            ExpertMaturityQualifiedStatisticsVO total = new ExpertMaturityQualifiedStatisticsVO();
            total.setMaturityLevel("总计");
            total.setBaselineCount(0);
            total.setQualifiedCount(0);
            total.setQualifiedByRequirementCount(0);
            total.setBaselineCountByRequirement(0);
            total.setQualifiedRate(BigDecimal.ZERO);
            total.setQualifiedByRequirementRate(BigDecimal.ZERO);
            total.setJobCategoryStatistics(null);
            response.setTotalStatistics(total);
            return response;
        }
        
        // 4. 提取所有专家工号，查询任职状态
        List<String> allEmployeeNumbers = expertList.stream()
                .map(ExpertInfoVO::getEmployeeNumber)
                .filter(num -> num != null && !num.trim().isEmpty())
                .distinct()
                .collect(Collectors.toList());
        
        // 查询已获得AI任职的专家工号列表
        List<String> qualifiedNumbers = getQualifiedEmployeeNumbers(allEmployeeNumbers);
        Set<String> qualifiedSet = new HashSet<>(qualifiedNumbers != null ? qualifiedNumbers : new ArrayList<>());
        
        // 5. 按成熟度和职位类分组统计
        // 结构：成熟度 -> 职位类 -> 统计信息
        Map<String, Map<String, ExpertJobCategoryQualifiedStatisticsVO>> maturityJobCategoryMap = new HashMap<>();
        
        // 用于统计L2和L3的总基数（包含所有职位类）
        Map<String, Integer> maturityTotalBaselineMap = new HashMap<>();
        Map<String, Integer> maturityTotalQualifiedMap = new HashMap<>();
        Map<String, Integer> maturityTotalQualifiedByRequirementMap = new HashMap<>();
        
        int totalBaselineCount = 0;
        int totalQualifiedCount = 0;
        int totalQualifiedByRequirementCount = 0;
        
        for (ExpertInfoVO expert : expertList) {
            String aiMaturity = expert.getAiMaturity();
            if (aiMaturity == null || !aiMaturity.matches("L[23]")) {
                continue; // 只处理L2和L3
            }
            
            // 提取职位类
            String jobCategory = extractJobCategory(expert.getJobCategory());
            
            String employeeNumber = expert.getEmployeeNumber();
            Integer isQualificationsStandard = expert.getIsQualificationsStandard();
            
            // 统计成熟度的总基数（所有职位类）
            maturityTotalBaselineMap.put(aiMaturity, 
                maturityTotalBaselineMap.getOrDefault(aiMaturity, 0) + 1);
            totalBaselineCount++;
            
            // 统计成熟度的总任职人数（所有职位类）
            if (employeeNumber != null && qualifiedSet.contains(employeeNumber)) {
                maturityTotalQualifiedMap.put(aiMaturity, 
                    maturityTotalQualifiedMap.getOrDefault(aiMaturity, 0) + 1);
                totalQualifiedCount++;
            }
            
            // 统计成熟度的总按要求任职人数（所有职位类，is_qualifications_standard=1）
            if (isQualificationsStandard != null && isQualificationsStandard == 1) {
                maturityTotalQualifiedByRequirementMap.put(aiMaturity, 
                    maturityTotalQualifiedByRequirementMap.getOrDefault(aiMaturity, 0) + 1);
                totalQualifiedByRequirementCount++;
            }
            
            // 获取或创建成熟度对应的职位类Map
            Map<String, ExpertJobCategoryQualifiedStatisticsVO> jobCategoryMap = 
                maturityJobCategoryMap.getOrDefault(aiMaturity, new HashMap<>());
            
            // 获取或创建职位类统计对象
            ExpertJobCategoryQualifiedStatisticsVO jobCategoryStat = 
                jobCategoryMap.getOrDefault(jobCategory, new ExpertJobCategoryQualifiedStatisticsVO());
            jobCategoryStat.setJobCategory(jobCategory);
            
            // 累加基数人数
            if (jobCategoryStat.getBaselineCount() == null) {
                jobCategoryStat.setBaselineCount(0);
            }
            jobCategoryStat.setBaselineCount(jobCategoryStat.getBaselineCount() + 1);
            
            // 检查是否已任职
            if (employeeNumber != null && qualifiedSet.contains(employeeNumber)) {
                if (jobCategoryStat.getQualifiedCount() == null) {
                    jobCategoryStat.setQualifiedCount(0);
                }
                jobCategoryStat.setQualifiedCount(jobCategoryStat.getQualifiedCount() + 1);
            }
            
            // 统计按要求任职人数（is_qualifications_standard=1）
            if (isQualificationsStandard != null && isQualificationsStandard == 1) {
                if (jobCategoryStat.getQualifiedByRequirementCount() == null) {
                    jobCategoryStat.setQualifiedByRequirementCount(0);
                }
                jobCategoryStat.setQualifiedByRequirementCount(jobCategoryStat.getQualifiedByRequirementCount() + 1);
            }
            
            jobCategoryMap.put(jobCategory, jobCategoryStat);
            maturityJobCategoryMap.put(aiMaturity, jobCategoryMap);
        }
        
        // 6. 计算每个职位类的任职率，并构建成熟度统计对象
        List<ExpertMaturityQualifiedStatisticsVO> maturityStatistics = new ArrayList<>();
        
        // 按L2、L3的顺序处理
        for (String aiMaturity : new String[]{"L2", "L3"}) {
            Map<String, ExpertJobCategoryQualifiedStatisticsVO> jobCategoryMap = 
                maturityJobCategoryMap.get(aiMaturity);
            if (jobCategoryMap == null) {
                jobCategoryMap = new HashMap<>();
            }
            
            // 创建成熟度统计对象
            ExpertMaturityQualifiedStatisticsVO maturityStat = new ExpertMaturityQualifiedStatisticsVO();
            maturityStat.setMaturityLevel(aiMaturity);
            
            // 使用所有职位类的总基数
            int maturityBaselineCount = maturityTotalBaselineMap.getOrDefault(aiMaturity, 0);
            int maturityQualifiedCount = maturityTotalQualifiedMap.getOrDefault(aiMaturity, 0);
            int maturityQualifiedByRequirementCount = maturityTotalQualifiedByRequirementMap.getOrDefault(aiMaturity, 0);
            
            List<ExpertJobCategoryQualifiedStatisticsVO> jobCategoryStatistics = new ArrayList<>();
            
            // 遍历该成熟度下的所有职位类
            for (ExpertJobCategoryQualifiedStatisticsVO jobCategoryStat : jobCategoryMap.values()) {
                // 计算职位类任职率
                if (jobCategoryStat.getBaselineCount() != null && jobCategoryStat.getBaselineCount() > 0) {
                    if (jobCategoryStat.getQualifiedCount() == null) {
                        jobCategoryStat.setQualifiedCount(0);
                    }
                    // 返回0-1之间的比率，不乘100，让前端统一格式化
                    BigDecimal qualifiedRate = new BigDecimal(jobCategoryStat.getQualifiedCount())
                            .divide(new BigDecimal(jobCategoryStat.getBaselineCount()), 4, RoundingMode.HALF_UP);
                    jobCategoryStat.setQualifiedRate(qualifiedRate);
                    
                    // 计算按岗位要求AI任职基线人数（L2非软件类为0，其他等于baselineCount）
                    String jobCategory = jobCategoryStat.getJobCategory();
                    boolean isL2NonSoftware = "L2".equals(aiMaturity) && (jobCategory == null || !"软件类".equals(jobCategory));
                    int baselineCountByRequirement = isL2NonSoftware ? 0 : jobCategoryStat.getBaselineCount();
                    jobCategoryStat.setBaselineCountByRequirement(baselineCountByRequirement);
                    
                    // 计算职位类按要求任职人数占比（使用baselineCountByRequirement作为分母）
                    if (jobCategoryStat.getQualifiedByRequirementCount() == null) {
                        jobCategoryStat.setQualifiedByRequirementCount(0);
                    }
                    if (baselineCountByRequirement > 0) {
                        // 返回0-1之间的比率，不乘100，让前端统一格式化
                        BigDecimal qualifiedByRequirementRate = new BigDecimal(jobCategoryStat.getQualifiedByRequirementCount())
                                .divide(new BigDecimal(baselineCountByRequirement), 4, RoundingMode.HALF_UP);
                        jobCategoryStat.setQualifiedByRequirementRate(qualifiedByRequirementRate);
                    } else {
                        jobCategoryStat.setQualifiedByRequirementRate(BigDecimal.ZERO);
                    }
                } else {
                    jobCategoryStat.setQualifiedRate(BigDecimal.ZERO);
                    jobCategoryStat.setBaselineCountByRequirement(0);
                    jobCategoryStat.setQualifiedByRequirementRate(BigDecimal.ZERO);
                }
                
                jobCategoryStatistics.add(jobCategoryStat);
            }
            
            // 设置成熟度统计数据
            maturityStat.setBaselineCount(maturityBaselineCount);
            maturityStat.setQualifiedCount(maturityQualifiedCount);
            maturityStat.setQualifiedByRequirementCount(maturityQualifiedByRequirementCount);
            maturityStat.setJobCategoryStatistics(jobCategoryStatistics);
            
            // 计算按岗位要求AI任职基线人数（L2非软件类为0，其他等于baselineCount）
            // 对于成熟度级别，需要统计所有职位类的baselineCountByRequirement
            int maturityBaselineCountByRequirement = 0;
            for (ExpertJobCategoryQualifiedStatisticsVO jobCategoryStat : jobCategoryStatistics) {
                if (jobCategoryStat.getBaselineCountByRequirement() != null) {
                    maturityBaselineCountByRequirement += jobCategoryStat.getBaselineCountByRequirement();
                }
            }
            maturityStat.setBaselineCountByRequirement(maturityBaselineCountByRequirement);
            
            // 计算成熟度任职率
            if (maturityBaselineCount > 0) {
                // 返回0-1之间的比率，不乘100，让前端统一格式化
                BigDecimal qualifiedRate = new BigDecimal(maturityQualifiedCount)
                        .divide(new BigDecimal(maturityBaselineCount), 4, RoundingMode.HALF_UP);
                maturityStat.setQualifiedRate(qualifiedRate);
            } else {
                maturityStat.setQualifiedRate(BigDecimal.ZERO);
            }
            
            // 计算成熟度按要求任职人数占比（使用baselineCountByRequirement作为分母）
            if (maturityBaselineCountByRequirement > 0) {
                // 返回0-1之间的比率，不乘100，让前端统一格式化
                BigDecimal qualifiedByRequirementRate = new BigDecimal(maturityQualifiedByRequirementCount)
                        .divide(new BigDecimal(maturityBaselineCountByRequirement), 4, RoundingMode.HALF_UP);
                maturityStat.setQualifiedByRequirementRate(qualifiedByRequirementRate);
            } else {
                maturityStat.setQualifiedByRequirementRate(BigDecimal.ZERO);
            }
            
            maturityStatistics.add(maturityStat);
        }
        
        // 7. 计算总计统计
        ExpertMaturityQualifiedStatisticsVO totalStatistics = new ExpertMaturityQualifiedStatisticsVO();
        totalStatistics.setMaturityLevel("总计");
        totalStatistics.setBaselineCount(totalBaselineCount);
        totalStatistics.setQualifiedCount(totalQualifiedCount);
        totalStatistics.setQualifiedByRequirementCount(totalQualifiedByRequirementCount);
        totalStatistics.setJobCategoryStatistics(null);
        
        // 计算总计按岗位要求AI任职基线人数（统计所有成熟度的baselineCountByRequirement）
        int totalBaselineCountByRequirement = 0;
        for (ExpertMaturityQualifiedStatisticsVO maturityStat : maturityStatistics) {
            if (maturityStat.getBaselineCountByRequirement() != null) {
                totalBaselineCountByRequirement += maturityStat.getBaselineCountByRequirement();
            }
        }
        totalStatistics.setBaselineCountByRequirement(totalBaselineCountByRequirement);
        
        // 计算总计任职率
        if (totalBaselineCount > 0) {
            // 返回0-1之间的比率，不乘100，让前端统一格式化
            BigDecimal totalQualifiedRate = new BigDecimal(totalQualifiedCount)
                    .divide(new BigDecimal(totalBaselineCount), 4, RoundingMode.HALF_UP);
            totalStatistics.setQualifiedRate(totalQualifiedRate);
        } else {
            totalStatistics.setQualifiedRate(BigDecimal.ZERO);
        }
        
        // 计算总计按要求任职人数占比（使用baselineCountByRequirement作为分母）
        if (totalBaselineCountByRequirement > 0) {
            // 返回0-1之间的比率，不乘100，让前端统一格式化
            BigDecimal totalQualifiedByRequirementRate = new BigDecimal(totalQualifiedByRequirementCount)
                    .divide(new BigDecimal(totalBaselineCountByRequirement), 4, RoundingMode.HALF_UP);
            totalStatistics.setQualifiedByRequirementRate(totalQualifiedByRequirementRate);
        } else {
            totalStatistics.setQualifiedByRequirementRate(BigDecimal.ZERO);
        }
        
        // 8. 构建返回结果
        ExpertAiQualifiedStatisticsResponseVO response = new ExpertAiQualifiedStatisticsResponseVO();
        response.setDeptCode(deptCode);
        response.setDeptName(deptName);
        response.setMaturityStatistics(maturityStatistics);
        response.setTotalStatistics(totalStatistics);
        
        return response;
    }
    
    /**
     * 从职位族字符串中提取职位类
     * 格式：职位族-职位类-职位子类，需要提取中间的职位类
     * @param jobCategory 职位族字符串
     * @return 职位类，如果格式不正确则返回"未知"
     */
    private String extractJobCategory(String jobCategory) {
        if (jobCategory == null || jobCategory.trim().isEmpty()) {
            return "未知";
        }
        
        String[] parts = jobCategory.split("-");
        if (parts.length >= 2) {
            // 提取中间的职位类（第二个部分）
            return parts[1].trim();
        } else if (parts.length == 1) {
            // 如果只有一个部分，直接返回
            return parts[0].trim();
        } else {
            return "未知";
        }
    }

}

