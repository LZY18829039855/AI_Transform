package com.huawei.aitransform.service;

import com.huawei.aitransform.constant.DepartmentConstants;
import com.huawei.aitransform.entity.DepartmentStatisticsVO;
import com.huawei.aitransform.entity.EntryLevelManager;
import com.huawei.aitransform.entity.PlTmCertStatisticsResponseVO;
import com.huawei.aitransform.entity.PlTmDepartmentStatisticsVO;
import com.huawei.aitransform.entity.StatisticsDataVO;
import com.huawei.aitransform.mapper.EntryLevelManagerMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 基层主管数据同步服务类
 */
@Service
public class EntryLevelManagerService {

    private static final Logger logger = LoggerFactory.getLogger(EntryLevelManagerService.class);

    @Autowired
    private EntryLevelManagerMapper entryLevelManagerMapper;

    @Autowired
    private ExpertCertStatisticsService expertCertStatisticsService;

    /**
     * 同步有效的PL、TM和项目经理数据到目标表
     * 
     * @return 同步结果
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> syncEntryLevelManager() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            logger.info("开始同步基层主管（PL/TM/项目经理）数据");
            
            // 1. 查询所有状态为有效的PL、TM和项目经理人员
            List<EntryLevelManager> validList = entryLevelManagerMapper.selectValidPlAndTm();
            int totalCount = validList != null ? validList.size() : 0;
            logger.info("查询到状态为有效的PL、TM和项目经理人员总数：{}", totalCount);
            
            // 2. 查询所有任期结束的PL、TM和项目经理
            List<EntryLevelManager> expiredList = entryLevelManagerMapper.selectExpiredPlAndTm();
            int expiredCount = expiredList != null ? expiredList.size() : 0;
            logger.info("查询到任期结束的PL、TM和项目经理人员总数：{}", expiredCount);
            
            // 3. 根据employee_number剔除任期结束的数据
            final Set<String> expiredEmployeeNumbers;
            if (expiredList != null && !expiredList.isEmpty()) {
                expiredEmployeeNumbers = expiredList.stream()
                    .map(EntryLevelManager::getEmployeeNumber)
                    .filter(empNum -> empNum != null && !empNum.isEmpty())
                    .collect(Collectors.toSet());
            } else {
                expiredEmployeeNumbers = java.util.Collections.emptySet();
            }
            
            // 过滤出有效的PL、TM和项目经理数据
            List<EntryLevelManager> finalValidList = null;
            if (validList != null && !validList.isEmpty()) {
                if (!expiredEmployeeNumbers.isEmpty()) {
                    finalValidList = validList.stream()
                        .filter(item -> item.getEmployeeNumber() != null 
                            && !expiredEmployeeNumbers.contains(item.getEmployeeNumber()))
                        .collect(Collectors.toList());
                } else {
                    finalValidList = validList;
                }
            }
            
            int validCount = finalValidList != null ? finalValidList.size() : 0;
            logger.info("计算得到当前有效的PL、TM和项目经理人员总数：{}", validCount);
            
            // 4. 查询目标表中所有已存在的employee_number，用于后续删除不在有效列表中的数据
            List<String> allExistingEmployeeNumbers = entryLevelManagerMapper.selectAllEmployeeNumbers();
            Set<String> existingEmployeeNumberSet = allExistingEmployeeNumbers != null 
                ? new java.util.HashSet<>(allExistingEmployeeNumbers) 
                : new java.util.HashSet<>();
            logger.info("目标表中已存在的employee_number总数：{}", existingEmployeeNumberSet.size());
            
            // 5. 计算需要删除的数据（目标表中存在，但不在新的有效数据列表中）
            Set<String> validEmployeeNumbers = finalValidList != null && !finalValidList.isEmpty()
                ? finalValidList.stream()
                    .map(EntryLevelManager::getEmployeeNumber)
                    .filter(empNum -> empNum != null && !empNum.isEmpty())
                    .collect(Collectors.toSet())
                : java.util.Collections.emptySet();
            
            Set<String> toDeleteEmployeeNumbers = existingEmployeeNumberSet.stream()
                .filter(empNum -> !validEmployeeNumbers.contains(empNum))
                .collect(Collectors.toSet());
            
            int deletedCount = 0;
            if (!toDeleteEmployeeNumbers.isEmpty()) {
                logger.info("需要删除的数据：{}条（目标表中存在但不在新的有效数据列表中）", toDeleteEmployeeNumbers.size());
                // 批量删除
                List<String> toDeleteList = new java.util.ArrayList<>(toDeleteEmployeeNumbers);
                int batchSize = 1000;
                for (int i = 0; i < toDeleteList.size(); i += batchSize) {
                    int end = Math.min(i + batchSize, toDeleteList.size());
                    List<String> batch = toDeleteList.subList(i, end);
                    int batchResult = entryLevelManagerMapper.batchDeleteByEmployeeNumbers(batch);
                    deletedCount += batchResult;
                    logger.info("批量删除第{}批数据，共{}条", (i / batchSize + 1), batch.size());
                }
                logger.info("删除完成，共删除{}条数据", deletedCount);
            }
            
            // 6. 根据employee_number批量插入或更新到目标表
            // 注意：如果employee_number字段有唯一索引，可以直接使用ON DUPLICATE KEY UPDATE
            // 如果没有唯一索引，需要先查询已存在的记录，然后分别插入和更新
            int syncedCount = 0;
            if (finalValidList != null && !finalValidList.isEmpty()) {
                // 收集所有需要检查的employee_number
                Set<String> employeeNumbers = finalValidList.stream()
                    .map(EntryLevelManager::getEmployeeNumber)
                    .filter(empNum -> empNum != null && !empNum.isEmpty())
                    .collect(Collectors.toSet());
                
                // 查询目标表中已存在的employee_number
                Set<String> existingEmployeeNumbers = new java.util.HashSet<>();
                for (String empNum : employeeNumbers) {
                    EntryLevelManager existing = entryLevelManagerMapper.selectByEmployeeNumber(empNum);
                    if (existing != null) {
                        existingEmployeeNumbers.add(empNum);
                    }
                }
                
                // 分离需要插入和更新的数据
                List<EntryLevelManager> toInsert = finalValidList.stream()
                    .filter(item -> item.getEmployeeNumber() != null 
                        && !existingEmployeeNumbers.contains(item.getEmployeeNumber()))
                    .collect(Collectors.toList());
                
                List<EntryLevelManager> toUpdate = finalValidList.stream()
                    .filter(item -> item.getEmployeeNumber() != null 
                        && existingEmployeeNumbers.contains(item.getEmployeeNumber()))
                    .collect(Collectors.toList());
                
                logger.info("需要插入的数据：{}条，需要更新的数据：{}条", toInsert.size(), toUpdate.size());
                
                // 7. 批量查询认证达标和任职达标的员工工号
                List<String> employeeNumberList = new ArrayList<>(employeeNumbers);
                Set<String> certifiedEmployeeNumbers = new HashSet<>();
                Set<String> qualifiedEmployeeNumbers = new HashSet<>();
                
                if (!employeeNumberList.isEmpty()) {
                    // 查询通过专业级认证的员工（认证达标）
                    List<String> certifiedList = expertCertStatisticsService.getCertifiedEmployeeNumbers(employeeNumberList);
                    if (certifiedList != null) {
                        certifiedEmployeeNumbers.addAll(certifiedList);
                    }
                    logger.info("查询到通过专业级认证的员工数量：{}", certifiedEmployeeNumbers.size());
                    
                    // 查询获得3级及以上AI任职的员工（任职达标）
                    List<String> qualifiedList = entryLevelManagerMapper.selectQualifiedEmployeeNumbersLevel3Plus(employeeNumberList);
                    if (qualifiedList != null) {
                        qualifiedEmployeeNumbers.addAll(qualifiedList);
                    }
                    logger.info("查询到获得3级及以上AI任职的员工数量：{}", qualifiedEmployeeNumbers.size());
                }
                
                // 8. 为每个员工设置认证达标和任职达标字段
                for (EntryLevelManager item : toInsert) {
                    if (item.getEmployeeNumber() != null) {
                        // 设置认证达标：通过专业级认证为1，否则为0
                        item.setIsCertStandard(certifiedEmployeeNumbers.contains(item.getEmployeeNumber()) ? 1 : 0);
                        // 设置任职达标：获得3级及以上AI任职为1，否则为0
                        item.setIsQualificationsStandard(qualifiedEmployeeNumbers.contains(item.getEmployeeNumber()) ? 1 : 0);
                    } else {
                        item.setIsCertStandard(0);
                        item.setIsQualificationsStandard(0);
                    }
                }
                
                for (EntryLevelManager item : toUpdate) {
                    if (item.getEmployeeNumber() != null) {
                        // 设置认证达标：通过专业级认证为1，否则为0
                        item.setIsCertStandard(certifiedEmployeeNumbers.contains(item.getEmployeeNumber()) ? 1 : 0);
                        // 设置任职达标：获得3级及以上AI任职为1，否则为0
                        item.setIsQualificationsStandard(qualifiedEmployeeNumbers.contains(item.getEmployeeNumber()) ? 1 : 0);
                    } else {
                        item.setIsCertStandard(0);
                        item.setIsQualificationsStandard(0);
                    }
                }
                
                // 批量插入
                if (!toInsert.isEmpty()) {
                    int batchSize = 1000;
                    for (int i = 0; i < toInsert.size(); i += batchSize) {
                        int end = Math.min(i + batchSize, toInsert.size());
                        List<EntryLevelManager> batch = toInsert.subList(i, end);
                        for (EntryLevelManager item : batch) {
                            entryLevelManagerMapper.insert(item);
                            syncedCount++;
                        }
                        logger.info("批量插入第{}批数据，共{}条", (i / batchSize + 1), batch.size());
                    }
                }
                
                // 批量更新（根据employee_number）
                if (!toUpdate.isEmpty()) {
                    for (EntryLevelManager item : toUpdate) {
                        int updateResult = entryLevelManagerMapper.updateByEmployeeNumber(item);
                        if (updateResult > 0) {
                            syncedCount++;
                        }
                    }
                    logger.info("批量更新完成，共更新{}条数据", toUpdate.size());
                }
            }
            
            logger.info("同步完成，共同步{}条有效PL/TM/项目经理数据，删除{}条无效数据", syncedCount, deletedCount);
            
            // 构建返回结果
            result.put("success", true);
            result.put("message", String.format("同步成功，共同步%d条有效PL/TM/项目经理数据，删除%d条无效数据", syncedCount, deletedCount));
            result.put("totalCount", totalCount);
            result.put("expiredCount", expiredCount);
            result.put("validCount", validCount);
            result.put("syncedCount", syncedCount);
            result.put("deletedCount", deletedCount);
            
            return result;
            
        } catch (Exception e) {
            logger.error("同步基层主管数据失败", e);
            result.put("success", false);
            result.put("message", "同步失败：" + e.getMessage());
            throw e;
        }
    }

    /**
     * 查询PL、TM、PM（项目经理）任职与认证统计数据
     * 按部门维度返回，每个部门包含PL/TM和PM两套统计数据
     * PL和TM合并统计，PM单独统计
     * 
     * @return PL/TM/PM任职与认证统计响应数据
     */
    public PlTmCertStatisticsResponseVO getPlTmCertStatistics() {
        logger.info("开始查询PL/TM/PM任职与认证统计数据");
        
        try {
            // 研发管理部部门编码
            String l3DepartmentCode = DepartmentConstants.R_D_MANAGEMENT_DEPT_CODE;
            
            // 1. 查询各四级部门PL/TM统计数据
            List<PlTmDepartmentStatisticsVO> plTmDepartmentList = entryLevelManagerMapper.selectPlTmStatisticsByL4Department(l3DepartmentCode);
            if (plTmDepartmentList == null) {
                plTmDepartmentList = new ArrayList<>();
            }
            logger.info("查询到{}个四级部门的PL/TM统计数据", plTmDepartmentList.size());
            
            // 2. 查询各四级部门PM统计数据
            List<PlTmDepartmentStatisticsVO> pmDepartmentList = entryLevelManagerMapper.selectPmStatisticsByL4Department(l3DepartmentCode);
            if (pmDepartmentList == null) {
                pmDepartmentList = new ArrayList<>();
            }
            logger.info("查询到{}个四级部门的PM统计数据", pmDepartmentList.size());
            
            // 3. 合并部门列表（去重），构建部门Map，key为部门编码
            Map<String, DepartmentStatisticsVO> departmentMap = new LinkedHashMap<>();
            
            // 添加PL/TM的部门数据
            for (PlTmDepartmentStatisticsVO plTmDept : plTmDepartmentList) {
                String deptCode = plTmDept.getDeptCode();
                DepartmentStatisticsVO deptStat = departmentMap.get(deptCode);
                if (deptStat == null) {
                    deptStat = new DepartmentStatisticsVO(deptCode, plTmDept.getDeptName());
                    departmentMap.put(deptCode, deptStat);
                }
                // 设置PL/TM统计数据
                StatisticsDataVO plTmData = new StatisticsDataVO(
                    plTmDept.getTotalCount(),
                    plTmDept.getQualifiedCount(),
                    plTmDept.getQualifiedRatio(),
                    plTmDept.getCertCount(),
                    plTmDept.getCertRatio()
                );
                deptStat.setPlTm(plTmData);
            }
            
            // 添加PM的部门数据
            for (PlTmDepartmentStatisticsVO pmDept : pmDepartmentList) {
                String deptCode = pmDept.getDeptCode();
                DepartmentStatisticsVO deptStat = departmentMap.get(deptCode);
                if (deptStat == null) {
                    deptStat = new DepartmentStatisticsVO(deptCode, pmDept.getDeptName());
                    departmentMap.put(deptCode, deptStat);
                }
                // 设置PM统计数据
                StatisticsDataVO pmData = new StatisticsDataVO(
                    pmDept.getTotalCount(),
                    pmDept.getQualifiedCount(),
                    pmDept.getQualifiedRatio(),
                    pmDept.getCertCount(),
                    pmDept.getCertRatio()
                );
                deptStat.setPm(pmData);
            }
            
            // 对于只有PL/TM或只有PM的部门，补充空数据
            for (DepartmentStatisticsVO deptStat : departmentMap.values()) {
                if (deptStat.getPlTm() == null) {
                    deptStat.setPlTm(new StatisticsDataVO(0, 0, 0.0, 0, 0.0));
                }
                if (deptStat.getPm() == null) {
                    deptStat.setPm(new StatisticsDataVO(0, 0, 0.0, 0, 0.0));
                }
            }
            
            // 转换为列表
            List<DepartmentStatisticsVO> departmentList = new ArrayList<>(departmentMap.values());
            
            // 按照指定顺序排序
            List<String> sortOrder = java.util.Arrays.asList(
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

            departmentList.sort((o1, o2) -> {
                String name1 = o1.getDeptName() != null ? o1.getDeptName().trim() : "";
                String name2 = o2.getDeptName() != null ? o2.getDeptName().trim() : "";
                
                int index1 = sortOrder.indexOf(name1);
                int index2 = sortOrder.indexOf(name2);
                
                // 如果都不在列表中，保持原顺序（这里简化处理，都排在最后）
                if (index1 == -1) index1 = Integer.MAX_VALUE;
                if (index2 == -1) index2 = Integer.MAX_VALUE;
                
                return Integer.compare(index1, index2);
            });

            logger.info("合并后共有{}个四级部门", departmentList.size());
            
            // 4. 查询研发管理部PL/TM汇总数据
            PlTmDepartmentStatisticsVO plTmSummary = entryLevelManagerMapper.selectPlTmStatisticsSummary(l3DepartmentCode);
            StatisticsDataVO plTmSummaryData;
            if (plTmSummary == null) {
                plTmSummaryData = new StatisticsDataVO(0, 0, 0.0, 0, 0.0);
            } else {
                plTmSummaryData = new StatisticsDataVO(
                    plTmSummary.getTotalCount(),
                    plTmSummary.getQualifiedCount(),
                    plTmSummary.getQualifiedRatio(),
                    plTmSummary.getCertCount(),
                    plTmSummary.getCertRatio()
                );
            }
            logger.info("研发管理部PL/TM汇总数据：总人数={}, 任职达标人数={}, 认证达标人数={}", 
                plTmSummaryData.getTotalCount(), plTmSummaryData.getQualifiedCount(), plTmSummaryData.getCertCount());
            
            // 5. 查询研发管理部PM汇总数据
            PlTmDepartmentStatisticsVO pmSummary = entryLevelManagerMapper.selectPmStatisticsSummary(l3DepartmentCode);
            StatisticsDataVO pmSummaryData;
            if (pmSummary == null) {
                pmSummaryData = new StatisticsDataVO(0, 0, 0.0, 0, 0.0);
            } else {
                pmSummaryData = new StatisticsDataVO(
                    pmSummary.getTotalCount(),
                    pmSummary.getQualifiedCount(),
                    pmSummary.getQualifiedRatio(),
                    pmSummary.getCertCount(),
                    pmSummary.getCertRatio()
                );
            }
            logger.info("研发管理部PM汇总数据：总人数={}, 任职达标人数={}, 认证达标人数={}", 
                pmSummaryData.getTotalCount(), pmSummaryData.getQualifiedCount(), pmSummaryData.getCertCount());
            
            // 6. 构建汇总数据，从查询结果中获取部门名称
            // 优先使用 plTmSummary 的部门名称，如果为空则使用 pmSummary 的部门名称，都为空则使用默认值
            String summaryDeptName = "云核心网研发管理部"; // 默认值
            if (plTmSummary != null && plTmSummary.getDeptName() != null && !plTmSummary.getDeptName().trim().isEmpty()) {
                summaryDeptName = plTmSummary.getDeptName();
            } else if (pmSummary != null && pmSummary.getDeptName() != null && !pmSummary.getDeptName().trim().isEmpty()) {
                summaryDeptName = pmSummary.getDeptName();
            }
            DepartmentStatisticsVO summary = new DepartmentStatisticsVO(l3DepartmentCode, summaryDeptName);
            summary.setPlTm(plTmSummaryData);
            summary.setPm(pmSummaryData);
            
            // 7. 构建响应对象
            PlTmCertStatisticsResponseVO response = new PlTmCertStatisticsResponseVO();
            response.setSummary(summary);
            response.setDepartmentList(departmentList);
            
            logger.info("PL/TM/PM任职与认证统计数据查询完成");
            return response;
            
        } catch (Exception e) {
            logger.error("查询PL/TM/PM任职与认证统计数据失败", e);
            throw e;
        }
    }
}

