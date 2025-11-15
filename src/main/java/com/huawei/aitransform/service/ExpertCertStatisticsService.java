package com.huawei.aitransform.service;

import com.huawei.aitransform.entity.CompetenceCategoryCertStatisticsResponseVO;
import com.huawei.aitransform.entity.CompetenceCategoryCertStatisticsVO;
import com.huawei.aitransform.entity.DepartmentCertStatisticsVO;
import com.huawei.aitransform.entity.DepartmentInfoVO;
import com.huawei.aitransform.entity.EmployeeCertStatisticsResponseVO;
import com.huawei.aitransform.entity.EmployeeWithCategoryVO;
import com.huawei.aitransform.entity.ExpertCertStatisticsResponseVO;
import com.huawei.aitransform.entity.ExpertCertStatisticsVO;
import com.huawei.aitransform.mapper.DepartmentInfoMapper;
import com.huawei.aitransform.mapper.EmployeeMapper;
import com.huawei.aitransform.mapper.ExpertCertStatisticsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    /**
     * 查询专家任职认证数据
     * @param deptCode 部门ID（部门编码）
     * @return 统计结果
     */
    public ExpertCertStatisticsResponseVO getExpertCertStatistics(String deptCode) {
        // 1. 查询部门信息
        com.huawei.aitransform.entity.DepartmentInfoVO deptInfo = departmentInfoMapper.getDepartmentByCode(deptCode);
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
    public com.huawei.aitransform.entity.DepartmentInfoVO getDepartmentInfo(String deptCode) {
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
     * 查询全员任职认证信息
     * @param deptCode 部门ID（部门编码），当为"0"时查询2级部门031562下的所有四级部门
     * @param personType 人员类型（0-全员）
     * @return 认证统计信息（包含各部门统计和总计）
     */
    public EmployeeCertStatisticsResponseVO getEmployeeCertStatistics(String deptCode, Integer personType) {
        List<DepartmentInfoVO> targetDepts;
        Integer queryLevel;

        // 特殊处理：当 deptCode 为 "0" 时，查询2级部门031562下的所有四级部门
        if ("0".equals(deptCode)) {
            // 查询2级部门031562下的所有四级部门
            targetDepts = departmentInfoMapper.getLevel4DepartmentsUnderLevel2("031562");
            if (targetDepts == null || targetDepts.isEmpty()) {
                // 如果没有四级部门，返回空统计
                EmployeeCertStatisticsResponseVO response = new EmployeeCertStatisticsResponseVO();
                response.setDepartmentStatistics(new ArrayList<>());
                DepartmentCertStatisticsVO total = new DepartmentCertStatisticsVO();
                total.setDeptCode("总计");
                total.setDeptName("总计");
                total.setTotalCount(0);
                total.setCertifiedCount(0);
                total.setCertRate(BigDecimal.ZERO);
                response.setTotalStatistics(total);
                return response;
            }
            // 查询四级部门的员工，使用 deptLevel=4（因为 EmployeeMapper 中 deptLevel=4 时查询 department5_id）
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
                total.setCertRate(BigDecimal.ZERO);
                response.setTotalStatistics(total);
                return response;
            }

            // 3. 根据当前部门层级，确定查询的部门层级（下一层）
            Integer currentLevel = Integer.parseInt(deptInfo.getDeptLevel());
            queryLevel = currentLevel;
        }

        // 4. 遍历每个部门，分别统计
        List<DepartmentCertStatisticsVO> departmentStats = new ArrayList<>();
        int totalCountSum = 0;
        int certifiedCountSum = 0;

        for (DepartmentInfoVO dept : targetDepts) {
            if (dept.getDeptCode() == null || dept.getDeptCode().trim().isEmpty()) {
                continue;
            }

            // 4.1 查询该部门下的员工工号列表
            List<String> deptIdList = new ArrayList<>();
            deptIdList.add(dept.getDeptCode());
            List<String> employeeNumbers = employeeMapper.getEmployeeNumbersByDeptLevel(queryLevel, deptIdList);

            int deptTotalCount = (employeeNumbers != null) ? employeeNumbers.size() : 0;

            // 4.2 查询该部门已通过认证的员工工号列表
            int deptCertifiedCount = 0;
            if (employeeNumbers != null && !employeeNumbers.isEmpty()) {
                List<String> certifiedNumbers = getCertifiedEmployeeNumbers(employeeNumbers);
                deptCertifiedCount = (certifiedNumbers != null) ? certifiedNumbers.size() : 0;
            }

            // 4.3 计算该部门的认证率
            BigDecimal deptCertRate = BigDecimal.ZERO;
            if (deptTotalCount > 0) {
                BigDecimal total = new BigDecimal(deptTotalCount);
                BigDecimal certified = new BigDecimal(deptCertifiedCount);
                deptCertRate = certified.divide(total, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal(100));
            }

            // 4.4 构建部门统计对象
            DepartmentCertStatisticsVO deptStat = new DepartmentCertStatisticsVO();
            deptStat.setDeptCode(dept.getDeptCode());
            deptStat.setDeptName(dept.getDeptName());
            deptStat.setTotalCount(deptTotalCount);
            deptStat.setCertifiedCount(deptCertifiedCount);
            deptStat.setCertRate(deptCertRate);

            departmentStats.add(deptStat);

            // 4.5 累加总计
            totalCountSum += deptTotalCount;
            certifiedCountSum += deptCertifiedCount;
        }

        // 5. 计算总计的认证率
        BigDecimal totalCertRate = BigDecimal.ZERO;
        if (totalCountSum > 0) {
            BigDecimal total = new BigDecimal(totalCountSum);
            BigDecimal certified = new BigDecimal(certifiedCountSum);
            totalCertRate = certified.divide(total, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(100));
        }

        // 6. 构建总计统计对象
        DepartmentCertStatisticsVO totalStatistics = new DepartmentCertStatisticsVO();
        totalStatistics.setDeptCode("总计");
        totalStatistics.setDeptName("总计");
        totalStatistics.setTotalCount(totalCountSum);
        totalStatistics.setCertifiedCount(certifiedCountSum);
        totalStatistics.setCertRate(totalCertRate);

        // 7. 构建返回结果
        EmployeeCertStatisticsResponseVO response = new EmployeeCertStatisticsResponseVO();
        response.setDepartmentStatistics(departmentStats);
        response.setTotalStatistics(totalStatistics);

        return response;
    }

    /**
     * 按职位类统计部门下不同职位类人数中的认证人数
     * @param deptCode 部门ID（部门编码），当为"0"时查询2级部门031562下的所有四级部门
     * @param personType 人员类型（0-全员）
     * @return 按职位类统计的认证信息
     */
    public CompetenceCategoryCertStatisticsResponseVO getCompetenceCategoryCertStatistics(String deptCode, Integer personType) {
        List<DepartmentInfoVO> targetDepts;
        Integer queryLevel;
        String deptName;

        // 特殊处理：当 deptCode 为 "0" 时，查询2级部门031562下的所有四级部门
        if ("0".equals(deptCode)) {
            // 查询2级部门031562下的所有四级部门（复用已有逻辑）
            targetDepts = departmentInfoMapper.getLevel4DepartmentsUnderLevel2("031562");
            if (targetDepts == null || targetDepts.isEmpty()) {
                // 如果没有四级部门，返回空统计
                CompetenceCategoryCertStatisticsResponseVO response = new CompetenceCategoryCertStatisticsResponseVO();
                response.setDeptCode(deptCode);
                response.setDeptName("所有四级部门");
                response.setCategoryStatistics(new ArrayList<>());
                CompetenceCategoryCertStatisticsVO total = new CompetenceCategoryCertStatisticsVO();
                total.setCompetenceCategory("总计");
                total.setTotalCount(0);
                total.setCertifiedCount(0);
                total.setCertRate(BigDecimal.ZERO);
                response.setTotalStatistics(total);
                return response;
            }
            // 查询四级部门的员工，使用 deptLevel=4（因为 EmployeeMapper 中 deptLevel=4 时查询 department5_id）
            queryLevel = 4;
            deptName = "所有四级部门";
        } else {
            // 1. 查询部门信息
            DepartmentInfoVO deptInfo = departmentInfoMapper.getDepartmentByCode(deptCode);
            if (deptInfo == null) {
                throw new IllegalArgumentException("部门不存在：" + deptCode);
            }

            // 2. 根据部门层级，确定查询的部门层级
            // getEmployeesWithCategoryByDeptLevel方法查询的是下一层级的部门
            // 例如：如果当前部门层级是3，使用deptLevel=3会查询department4_id为部门ID的人员信息
            Integer deptLevel = Integer.parseInt(deptInfo.getDeptLevel());
            queryLevel = deptLevel;
            deptName = deptInfo.getDeptName();
            
            // 只查询当前部门
            targetDepts = new ArrayList<>();
            targetDepts.add(deptInfo);
        }

        // 3. 查询所有目标部门下的成员的工号和职位类
        List<EmployeeWithCategoryVO> allEmployees = new ArrayList<>();
        for (DepartmentInfoVO dept : targetDepts) {
            if (dept.getDeptCode() == null || dept.getDeptCode().trim().isEmpty()) {
                continue;
            }
            List<String> deptIdList = new ArrayList<>();
            deptIdList.add(dept.getDeptCode());
            List<EmployeeWithCategoryVO> employees = employeeMapper.getEmployeesWithCategoryByDeptLevel(queryLevel, deptIdList);
            if (employees != null && !employees.isEmpty()) {
                allEmployees.addAll(employees);
            }
        }

        if (allEmployees.isEmpty()) {
            // 如果没有员工，返回空统计
            CompetenceCategoryCertStatisticsResponseVO response = new CompetenceCategoryCertStatisticsResponseVO();
            response.setDeptCode(deptCode);
            response.setDeptName(deptName);
            response.setCategoryStatistics(new ArrayList<>());
            CompetenceCategoryCertStatisticsVO total = new CompetenceCategoryCertStatisticsVO();
            total.setCompetenceCategory("总计");
            total.setTotalCount(0);
            total.setCertifiedCount(0);
            total.setCertRate(BigDecimal.ZERO);
            response.setTotalStatistics(total);
            return response;
        }

        // 4. 提取所有员工工号
        List<String> employeeNumbers = allEmployees.stream()
                .map(EmployeeWithCategoryVO::getEmployeeNumber)
                .filter(num -> num != null && !num.trim().isEmpty())
                .distinct()
                .collect(Collectors.toList());

        // 5. 查询已通过认证的员工工号列表（复用现有方法）
        List<String> certifiedNumbers = new ArrayList<>();
        if (!employeeNumbers.isEmpty()) {
            certifiedNumbers = getCertifiedEmployeeNumbers(employeeNumbers);
        }

        // 6. 按职位类分组统计
        Map<String, CompetenceCategoryCertStatisticsVO> categoryMap = new HashMap<>();
        int totalCountSum = 0;
        int certifiedCountSum = 0;

        for (EmployeeWithCategoryVO employee : allEmployees) {
            String category = employee.getCompetenceCategory();
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
            String employeeNumber = employee.getEmployeeNumber();
            if (employeeNumber != null && certifiedNumbers.contains(employeeNumber)) {
                if (categoryStat.getCertifiedCount() == null) {
                    categoryStat.setCertifiedCount(0);
                }
                categoryStat.setCertifiedCount(categoryStat.getCertifiedCount() + 1);
                certifiedCountSum++;
            }

            categoryMap.put(category, categoryStat);
        }

        // 7. 计算每个职位类的认证率
        List<CompetenceCategoryCertStatisticsVO> categoryStats = new ArrayList<>();
        for (CompetenceCategoryCertStatisticsVO categoryStat : categoryMap.values()) {
            if (categoryStat.getTotalCount() != null && categoryStat.getTotalCount() > 0) {
                if (categoryStat.getCertifiedCount() == null) {
                    categoryStat.setCertifiedCount(0);
                }
                BigDecimal rate = new BigDecimal(categoryStat.getCertifiedCount())
                        .divide(new BigDecimal(categoryStat.getTotalCount()), 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal(100));
                categoryStat.setCertRate(rate);
            } else {
                categoryStat.setCertRate(BigDecimal.ZERO);
            }
            categoryStats.add(categoryStat);
        }

        // 8. 构建总计统计对象
        CompetenceCategoryCertStatisticsVO totalStatistics = new CompetenceCategoryCertStatisticsVO();
        totalStatistics.setCompetenceCategory("总计");
        totalStatistics.setTotalCount(totalCountSum);
        totalStatistics.setCertifiedCount(certifiedCountSum);
        if (totalCountSum > 0) {
            BigDecimal totalRate = new BigDecimal(certifiedCountSum)
                    .divide(new BigDecimal(totalCountSum), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(100));
            totalStatistics.setCertRate(totalRate);
        } else {
            totalStatistics.setCertRate(BigDecimal.ZERO);
        }

        // 9. 构建返回结果
        CompetenceCategoryCertStatisticsResponseVO response = new CompetenceCategoryCertStatisticsResponseVO();
        response.setDeptCode(deptCode);
        response.setDeptName(deptName);
        response.setCategoryStatistics(categoryStats);
        response.setTotalStatistics(totalStatistics);

        return response;
    }

}

