package com.huawei.aitransform.service;

import com.huawei.aitransform.constant.DepartmentConstants;
import com.huawei.aitransform.entity.CompetenceCategoryCertStatisticsResponseVO;
import com.huawei.aitransform.entity.CompetenceCategoryCertStatisticsVO;
import com.huawei.aitransform.entity.DepartmentCertStatisticsVO;
import com.huawei.aitransform.entity.DepartmentInfoVO;
import com.huawei.aitransform.entity.DepartmentMaturityVO;
import com.huawei.aitransform.entity.EmployeeCertStatisticsResponseVO;
import com.huawei.aitransform.entity.EmployeeDetailVO;
import com.huawei.aitransform.entity.EmployeeDrillDownResponseVO;
import com.huawei.aitransform.entity.EmployeeWithCategoryVO;
import com.huawei.aitransform.entity.ExpertCertStatisticsResponseVO;
import com.huawei.aitransform.entity.ExpertCertStatisticsVO;
import com.huawei.aitransform.entity.MaturityCertStatisticsResponseVO;
import com.huawei.aitransform.entity.MaturityCertStatisticsVO;
import com.huawei.aitransform.mapper.CadreMapper;
import com.huawei.aitransform.mapper.DepartmentInfoMapper;
import com.huawei.aitransform.mapper.DepartmentMaturityMapper;
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

    @Autowired
    private DepartmentMaturityMapper departmentMaturityMapper;

    @Autowired
    private CadreMapper cadreMapper;

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

        // 全员处理流程（personType=0）
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

            // 4.3 查询该部门已获得任职的员工工号列表
            int deptQualifiedCount = 0;
            if (employeeNumbers != null && !employeeNumbers.isEmpty()) {
                // 将带首字母的工号转换为不带首字母的工号（去除第一个字符）
                // 例如：l00123456 -> 00123456
                List<String> employeeNumbersWithoutPrefix = new ArrayList<>();
                for (String empNo : employeeNumbers) {
                    if (empNo != null && empNo.length() > 1) {
                        employeeNumbersWithoutPrefix.add(empNo.substring(1));
                    }
                }
                // 使用去除首字母的工号列表查询任职信息
                List<String> qualifiedNumbers = getQualifiedEmployeeNumbers(employeeNumbersWithoutPrefix);
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
        // 1. 查询部门信息
        DepartmentInfoVO deptInfo = departmentInfoMapper.getDepartmentByCode(deptCode);
        if (deptInfo == null) {
            throw new IllegalArgumentException("部门不存在：" + deptCode);
        }

        // 2. 查询下一级部门列表
        List<DepartmentInfoVO> childDepts = departmentInfoMapper.getChildDepartments(deptCode);
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

        // 3. 遍历每个下级部门，查询所有子部门并统计干部信息
        List<DepartmentCertStatisticsVO> departmentStats = new ArrayList<>();
        int totalCountSum = 0;
        int certifiedCountSum = 0;
        int qualifiedCountSum = 0;

        for (DepartmentInfoVO childDept : childDepts) {
            if (childDept.getDeptCode() == null || childDept.getDeptCode().trim().isEmpty()) {
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

            // 3.4 查询该部门已获得任职的干部工号列表
            int deptQualifiedCount = 0;
            if (cadreEmployeeNumbers != null && !cadreEmployeeNumbers.isEmpty()) {
                // 将带首字母的工号转换为不带首字母的工号（去除第一个字符）
                // 例如：l00123456 -> 00123456
                List<String> employeeNumbersWithoutPrefix = new ArrayList<>();
                for (String empNo : cadreEmployeeNumbers) {
                    if (empNo != null && empNo.length() > 1) {
                        employeeNumbersWithoutPrefix.add(empNo.substring(1));
                    }
                }
                // 使用去除首字母的工号列表查询任职信息
                List<String> qualifiedNumbers = getQualifiedEmployeeNumbers(employeeNumbersWithoutPrefix);
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

        // 全员处理流程（personType=0）
        List<DepartmentInfoVO> targetDepts;
        Integer queryLevel;
        String deptName;

        // 特殊处理：当 deptCode 为 "0" 时，查询云核心网产品线部门下的所有四级部门
        if ("0".equals(deptCode)) {
            // 查询云核心网产品线部门下的所有四级部门（复用已有逻辑）
            targetDepts = departmentInfoMapper.getLevel4DepartmentsUnderLevel2(DepartmentConstants.CLOUD_CORE_NETWORK_DEPT_CODE);
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
                total.setQualifiedCount(0);
                total.setCertRate(BigDecimal.ZERO);
                total.setQualifiedRate(BigDecimal.ZERO);
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
                total.setQualifiedCount(0);
                total.setCertRate(BigDecimal.ZERO);
                total.setQualifiedRate(BigDecimal.ZERO);
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

        // 5.1 查询已获得任职的员工工号列表（去除首字母后查询）
        List<String> qualifiedNumbers = new ArrayList<>();
        if (!employeeNumbers.isEmpty()) {
            // 将带首字母的工号转换为不带首字母的工号（去除第一个字符）
            List<String> employeeNumbersWithoutPrefix = new ArrayList<>();
            for (String empNo : employeeNumbers) {
                if (empNo != null && empNo.length() > 1) {
                    employeeNumbersWithoutPrefix.add(empNo.substring(1));
                }
            }
            // 使用去除首字母的工号列表查询任职信息
            List<String> qualifiedNumbersWithoutPrefix = getQualifiedEmployeeNumbers(employeeNumbersWithoutPrefix);
            // 将返回的工号（不带首字母）转换回带首字母的格式，用于后续匹配
            if (qualifiedNumbersWithoutPrefix != null) {
                // 由于返回的是不带首字母的工号，需要与原始工号匹配
                // 构建一个映射：不带首字母的工号 -> 带首字母的工号
                Map<String, String> prefixMap = new HashMap<>();
                for (String empNo : employeeNumbers) {
                    if (empNo != null && empNo.length() > 1) {
                        prefixMap.put(empNo.substring(1), empNo);
                    }
                }
                // 将查询结果转换回带首字母的工号
                for (String qualifiedNo : qualifiedNumbersWithoutPrefix) {
                    String originalNo = prefixMap.get(qualifiedNo);
                    if (originalNo != null) {
                        qualifiedNumbers.add(originalNo);
                    }
                }
            }
        }

        // 6. 按职位类分组统计
        Map<String, CompetenceCategoryCertStatisticsVO> categoryMap = new HashMap<>();
        int totalCountSum = 0;
        int certifiedCountSum = 0;
        int qualifiedCountSum = 0;

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
        response.setDeptName(deptName);
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

        // 5.1 查询已获得任职的干部工号列表（去除首字母后查询）
        List<String> qualifiedNumbers = new ArrayList<>();
        if (!cadreEmployeeNumbers.isEmpty()) {
            // 将带首字母的工号转换为不带首字母的工号（去除第一个字符）
            List<String> employeeNumbersWithoutPrefix = new ArrayList<>();
            for (String empNo : cadreEmployeeNumbers) {
                if (empNo != null && empNo.length() > 1) {
                    employeeNumbersWithoutPrefix.add(empNo.substring(1));
                }
            }
            // 使用去除首字母的工号列表查询任职信息
            List<String> qualifiedNumbersWithoutPrefix = getQualifiedEmployeeNumbers(employeeNumbersWithoutPrefix);
            // 将返回的工号（不带首字母）转换回带首字母的格式，用于后续匹配
            if (qualifiedNumbersWithoutPrefix != null) {
                // 构建一个映射：不带首字母的工号 -> 带首字母的工号
                Map<String, String> prefixMap = new HashMap<>();
                for (String empNo : cadreEmployeeNumbers) {
                    if (empNo != null && empNo.length() > 1) {
                        prefixMap.put(empNo.substring(1), empNo);
                    }
                }
                // 将查询结果转换回带首字母的工号
                for (String qualifiedNo : qualifiedNumbersWithoutPrefix) {
                    String originalNo = prefixMap.get(qualifiedNo);
                    if (originalNo != null) {
                        qualifiedNumbers.add(originalNo);
                    }
                }
            }
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

        // 6.1 查询已获得任职的员工工号列表（去除首字母后查询）
        List<String> qualifiedNumbers = new ArrayList<>();
        if (!allEmployeeNumbers.isEmpty()) {
            // 将带首字母的工号转换为不带首字母的工号（去除第一个字符）
            List<String> employeeNumbersWithoutPrefix = new ArrayList<>();
            for (String empNo : allEmployeeNumbers) {
                if (empNo != null && empNo.length() > 1) {
                    employeeNumbersWithoutPrefix.add(empNo.substring(1));
                }
            }
            // 使用去除首字母的工号列表查询任职信息
            List<String> qualifiedNumbersWithoutPrefix = getQualifiedEmployeeNumbers(employeeNumbersWithoutPrefix);
            // 将返回的工号（不带首字母）转换回带首字母的格式，用于后续匹配
            if (qualifiedNumbersWithoutPrefix != null) {
                // 由于返回的是不带首字母的工号，需要与原始工号匹配
                // 构建一个映射：不带首字母的工号 -> 带首字母的工号
                Map<String, String> prefixMap = new HashMap<>();
                for (String empNo : allEmployeeNumbers) {
                    if (empNo != null && empNo.length() > 1) {
                        prefixMap.put(empNo.substring(1), empNo);
                    }
                }
                // 将查询结果转换回带首字母的工号
                for (String qualifiedNo : qualifiedNumbersWithoutPrefix) {
                    String originalNo = prefixMap.get(qualifiedNo);
                    if (originalNo != null) {
                        qualifiedNumbers.add(originalNo);
                    }
                }
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
     * @param dataType 数据类型（0：基线，1：任职数据，2：认证数据）
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
            employeeDetails = employeeMapper.getEmployeeCertDetailsByDeptLevel(deptLevel, deptCode);
        } else if (dataType == 1) {
            // 任职数据：暂时返回空列表，等用户提供具体需求
            employeeDetails = new ArrayList<>();
        } else if (dataType == 0) {
            // 基线数据：暂时返回空列表，等用户提供具体需求
            employeeDetails = new ArrayList<>();
        } else {
            throw new IllegalArgumentException("不支持的数据类型：" + dataType);
        }

        // 4. 构建返回结果
        EmployeeDrillDownResponseVO response = new EmployeeDrillDownResponseVO();
        response.setEmployeeDetails(employeeDetails);

        return response;
    }

}

