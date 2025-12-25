package com.huawei.aitransform.service;

import com.huawei.aitransform.constant.DepartmentConstants;
import com.huawei.aitransform.entity.EntryLevelManager;
import com.huawei.aitransform.entity.PlTmCertStatisticsResponseVO;
import com.huawei.aitransform.entity.PlTmDepartmentStatisticsVO;
import com.huawei.aitransform.mapper.EntryLevelManagerMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
     * 同步有效的PL和TM数据到目标表
     * 
     * @return 同步结果
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> syncEntryLevelManager() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            logger.info("开始同步基层主管（PL/TM）数据");
            
            // 1. 查询所有状态为有效的PL和TM人员
            List<EntryLevelManager> validList = entryLevelManagerMapper.selectValidPlAndTm();
            int totalCount = validList != null ? validList.size() : 0;
            logger.info("查询到状态为有效的PL和TM人员总数：{}", totalCount);
            
            // 2. 查询所有任期结束的PL和TM
            List<EntryLevelManager> expiredList = entryLevelManagerMapper.selectExpiredPlAndTm();
            int expiredCount = expiredList != null ? expiredList.size() : 0;
            logger.info("查询到任期结束的PL和TM人员总数：{}", expiredCount);
            
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
            
            // 过滤出有效的PL和TM数据
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
            logger.info("计算得到当前有效的PL和TM人员总数：{}", validCount);
            
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
            
            logger.info("同步完成，共同步{}条有效PL/TM数据，删除{}条无效数据", syncedCount, deletedCount);
            
            // 构建返回结果
            result.put("success", true);
            result.put("message", String.format("同步成功，共同步%d条有效PL/TM数据，删除%d条无效数据", syncedCount, deletedCount));
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
     * 查询PL、TM任职与认证统计数据
     * 统计研发管理部下各四级部门以及研发管理部整体的PL/TM总人数、通过任职标准的人数及占比、通过认证标准的人数及占比
     * 
     * @return PL/TM任职与认证统计响应数据
     */
    public PlTmCertStatisticsResponseVO getPlTmCertStatistics() {
        logger.info("开始查询PL/TM任职与认证统计数据");
        
        try {
            // 研发管理部部门编码
            String l3DepartmentCode = DepartmentConstants.R_D_MANAGEMENT_DEPT_CODE;
            
            // 1. 查询各四级部门统计数据
            List<PlTmDepartmentStatisticsVO> departmentList = entryLevelManagerMapper.selectPlTmStatisticsByL4Department(l3DepartmentCode);
            if (departmentList == null) {
                departmentList = new ArrayList<>();
            }
            logger.info("查询到{}个四级部门的统计数据", departmentList.size());
            
            // 2. 查询研发管理部汇总数据
            PlTmDepartmentStatisticsVO summary = entryLevelManagerMapper.selectPlTmStatisticsSummary(l3DepartmentCode);
            if (summary == null) {
                // 如果没有数据，创建一个空的汇总对象
                summary = new PlTmDepartmentStatisticsVO();
                summary.setDeptCode(l3DepartmentCode);
                summary.setDeptName("研发管理部");
                summary.setTotalCount(0);
                summary.setQualifiedCount(0);
                summary.setQualifiedRatio(0.0);
                summary.setCertCount(0);
                summary.setCertRatio(0.0);
            }
            logger.info("研发管理部汇总数据：总人数={}, 任职达标人数={}, 认证达标人数={}", 
                summary.getTotalCount(), summary.getQualifiedCount(), summary.getCertCount());
            
            // 3. 构建响应对象
            PlTmCertStatisticsResponseVO response = new PlTmCertStatisticsResponseVO();
            response.setSummary(summary);
            response.setDepartmentList(departmentList);
            
            logger.info("PL/TM任职与认证统计数据查询完成");
            return response;
            
        } catch (Exception e) {
            logger.error("查询PL/TM任职与认证统计数据失败", e);
            throw e;
        }
    }
}

