package com.huawei.aitransform.service;

import com.huawei.aitransform.entity.CadreDepartmentRefreshVO;
import com.huawei.aitransform.entity.DepartmentInfoVO;
import com.huawei.aitransform.mapper.CadreMapper;
import com.huawei.aitransform.mapper.DepartmentInfoMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 干部部门信息刷新服务类
 */
@Service
public class CadreDepartmentRefreshService {

    private static final Logger logger = LoggerFactory.getLogger(CadreDepartmentRefreshService.class);

    @Autowired
    private CadreMapper cadreMapper;

    @Autowired
    private DepartmentInfoMapper departmentInfoMapper;

    /**
     * 刷新所有干部的部门信息
     * 从最小部门ID开始向上查询父级部门，直到二级部门，并更新到干部表中
     * 
     * @return 刷新结果
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> refreshCadreDepartmentInfo() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            logger.info("开始刷新干部部门信息");
            
            // 1. 查询所有干部及其最小部门ID
            List<CadreDepartmentRefreshVO> cadreList = cadreMapper.getAllCadreWithMiniDepartment();
            int totalCount = cadreList != null ? cadreList.size() : 0;
            logger.info("查询到干部总数：{}", totalCount);
            
            if (cadreList == null || cadreList.isEmpty()) {
                result.put("success", true);
                result.put("message", "未查询到需要刷新的干部数据");
                result.put("totalCount", 0);
                result.put("updatedCount", 0);
                result.put("failedCount", 0);
                return result;
            }
            
            // 2. 对每个干部，从最小部门开始向上查询父级部门
            List<CadreDepartmentRefreshVO> updateList = new ArrayList<>();
            int successCount = 0;
            int failedCount = 0;
            
            for (CadreDepartmentRefreshVO cadre : cadreList) {
                try {
                    // 从最小部门开始向上查询
                    String currentDeptCode = cadre.getMiniDepartnameId();
                    if (currentDeptCode == null || currentDeptCode.isEmpty()) {
                        failedCount++;
                        logger.warn("干部 {} 的最小部门ID为空，跳过", cadre.getAccount());
                        continue;
                    }
                    
                    // 查询部门层级信息
                    Map<String, String> deptMap = getDepartmentHierarchy(currentDeptCode);
                    
                    // 设置各级部门编码
                    cadre.setL2DepartmentCode(deptMap.get("l2"));
                    cadre.setL3DepartmentCode(deptMap.get("l3"));
                    cadre.setL4DepartmentCode(deptMap.get("l4"));
                    cadre.setL5DepartmentCode(deptMap.get("l5"));
                    
                    updateList.add(cadre);
                    successCount++;
                    
                } catch (Exception e) {
                    failedCount++;
                    logger.error("处理干部 {} 的部门信息时发生异常：{}", cadre.getAccount(), e.getMessage(), e);
                }
            }
            
            logger.info("成功处理 {} 条干部数据，失败 {} 条", successCount, failedCount);
            
            // 3. 批量更新干部表
            int updatedCount = 0;
            if (!updateList.isEmpty()) {
                // 分批更新，每批1000条，使用批量更新SQL
                int batchSize = 1000;
                for (int i = 0; i < updateList.size(); i += batchSize) {
                    int endIndex = Math.min(i + batchSize, updateList.size());
                    List<CadreDepartmentRefreshVO> batchList = new ArrayList<>(updateList.subList(i, endIndex));
                    try {
                        int batchUpdated = cadreMapper.batchUpdateCadreDepartmentCodes(batchList);
                        updatedCount += batchUpdated;
                        logger.info("批量更新第 {} 批，更新 {} 条记录", (i / batchSize + 1), batchUpdated);
                    } catch (Exception e) {
                        // 如果批量更新失败，尝试单个更新
                        logger.warn("批量更新第 {} 批失败，改为单个更新：{}", (i / batchSize + 1), e.getMessage());
                        for (CadreDepartmentRefreshVO cadre : batchList) {
                            try {
                                int singleUpdated = cadreMapper.updateCadreDepartmentCodes(cadre);
                                updatedCount += singleUpdated;
                            } catch (Exception ex) {
                                logger.error("更新干部 {} 的部门信息失败：{}", cadre.getAccount(), ex.getMessage());
                            }
                        }
                    }
                }
            }
            
            result.put("success", true);
            result.put("message", String.format("刷新完成，共处理 %d 条干部数据，成功 %d 条，失败 %d 条，更新 %d 条", 
                totalCount, successCount, failedCount, updatedCount));
            result.put("totalCount", totalCount);
            result.put("successCount", successCount);
            result.put("failedCount", failedCount);
            result.put("updatedCount", updatedCount);
            
            logger.info("干部部门信息刷新完成，共处理 {} 条，成功 {} 条，失败 {} 条，更新 {} 条", 
                totalCount, successCount, failedCount, updatedCount);
            
        } catch (Exception e) {
            logger.error("刷新干部部门信息时发生异常：{}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "刷新失败：" + e.getMessage());
            throw e;
        }
        
        return result;
    }
    
    /**
     * 从指定部门编码开始向上查询，直到二级部门，返回各级部门编码
     * 
     * @param startDeptCode 起始部门编码（最小部门ID）
     * @return 包含l2、l3、l4、l5部门编码的Map
     */
    private Map<String, String> getDepartmentHierarchy(String startDeptCode) {
        Map<String, String> deptMap = new HashMap<>();
        deptMap.put("l2", null);
        deptMap.put("l3", null);
        deptMap.put("l4", null);
        deptMap.put("l5", null);
        
        String currentDeptCode = startDeptCode;
        int maxLevels = 10; // 防止无限循环
        int level = 0;
        
        while (currentDeptCode != null && !currentDeptCode.isEmpty() && level < maxLevels) {
            // 查询当前部门信息
            DepartmentInfoVO deptInfo = departmentInfoMapper.getDepartmentByCode(currentDeptCode);
            
            if (deptInfo == null) {
                logger.warn("未找到部门编码为 {} 的部门信息", currentDeptCode);
                break;
            }
            
            String deptLevel = deptInfo.getDeptLevel();
            
            // 根据部门级别设置对应的字段
            if ("2".equals(deptLevel)) {
                deptMap.put("l2", deptInfo.getDeptCode());
                // 找到二级部门，停止向上查询
                break;
            } else if ("3".equals(deptLevel)) {
                deptMap.put("l3", deptInfo.getDeptCode());
            } else if ("4".equals(deptLevel)) {
                deptMap.put("l4", deptInfo.getDeptCode());
            } else if ("5".equals(deptLevel)) {
                deptMap.put("l5", deptInfo.getDeptCode());
            }
            
            // 继续向上查询父级部门
            String parentDeptCode = deptInfo.getParentDeptCode();
            if (parentDeptCode == null || parentDeptCode.isEmpty()) {
                // 没有父级部门，停止查询
                break;
            }
            
            currentDeptCode = parentDeptCode;
            level++;
        }
        
        return deptMap;
    }
}

