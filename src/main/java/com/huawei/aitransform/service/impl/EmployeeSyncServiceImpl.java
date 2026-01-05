package com.huawei.aitransform.service.impl;

import com.huawei.aitransform.entity.EmployeePO;
import com.huawei.aitransform.entity.EmployeeSyncDataVO;
import com.huawei.aitransform.mapper.CadreMapper;
import com.huawei.aitransform.mapper.EmployeeMapper;
import com.huawei.aitransform.service.EmployeeSyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.Objects;

@Service
public class EmployeeSyncServiceImpl implements EmployeeSyncService {

    @Autowired
    private EmployeeMapper employeeMapper;

    @Autowired
    private CadreMapper cadreMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> syncEmployeeData(String periodId) {
        if (periodId == null || periodId.trim().isEmpty()) {
            throw new IllegalArgumentException("Period ID cannot be empty");
        }

        // 1. 获取研发族数据 (t_employee_sync + 达标计算)
        List<EmployeeSyncDataVO> rndList = employeeMapper.getEmployeeSyncData(periodId);
        
        // 2. 获取干部数据
        // 2.1 查询所有干部的工号
        List<String> cadreEmployeeNumbers = new ArrayList<>();
        try {
            cadreEmployeeNumbers = cadreMapper.getAllCadreEmployeeNumbers();
        } catch (Exception e) {
            // 如果查询干部工号失败，记录日志但不影响研发族数据同步
            System.err.println("Failed to query cadre employee numbers: " + e.getMessage());
        }
        
        // 2.2 如果干部工号列表不为空，查询干部数据
        List<EmployeeSyncDataVO> cadreList = new ArrayList<>();
        if (cadreEmployeeNumbers != null && !cadreEmployeeNumbers.isEmpty()) {
            try {
                cadreList = employeeMapper.getEmployeeSyncDataByEmployeeNumbers(periodId, cadreEmployeeNumbers);
            } catch (Exception e) {
                // 如果查询干部数据失败，记录日志但不影响研发族数据同步
                System.err.println("Failed to query cadre data: " + e.getMessage());
            }
        }
        
        // 3. 合并数据（去重，以工号为唯一标识，干部数据优先覆盖研发族数据）
        Map<String, EmployeeSyncDataVO> sourceMap = new HashMap<>();
        
        // 3.1 将研发族数据转换为 Map（工号 -> 数据）
        if (rndList != null) {
            for (EmployeeSyncDataVO vo : rndList) {
                if (vo.getEmployeeNumber() != null) {
                    sourceMap.put(vo.getEmployeeNumber(), vo);
                }
            }
        }
        
        // 3.2 将干部数据合并到 Map 中（如果工号已存在，干部数据优先覆盖）
        if (cadreList != null) {
            for (EmployeeSyncDataVO vo : cadreList) {
                if (vo.getEmployeeNumber() != null) {
                    sourceMap.put(vo.getEmployeeNumber(), vo);
                }
            }
        }
        
        // 3.3 转换为 List
        List<EmployeeSyncDataVO> sourceList = new ArrayList<>(sourceMap.values());
        
        // 校验合并后的数据量，如果小于2000条则认为数据异常，中止同步
        if (sourceList == null || sourceList.size() < 2000) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("periodId", periodId);
            errorResult.put("success", false);
            errorResult.put("message", "Source data count is too low (" + (sourceList == null ? 0 : sourceList.size()) + "), sync aborted.");
            errorResult.put("rndCount", rndList != null ? rndList.size() : 0);
            errorResult.put("cadreCount", cadreList != null ? cadreList.size() : 0);
            errorResult.put("cadreEmployeeNumberCount", cadreEmployeeNumbers != null ? cadreEmployeeNumbers.size() : 0);
            return errorResult;
        }

        // 4. 获取目标数据 (t_employee 全量)
        List<EmployeePO> targetList = employeeMapper.getAllEmployees();
        Map<String, EmployeePO> targetMap = targetList.stream()
                .collect(Collectors.toMap(EmployeePO::getEmployeeNumber, Function.identity(), (k1, k2) -> k1));

        int insertCount = 0;
        int updateCount = 0;
        int deleteCount = 0;
        int ignoreCount = 0;
        
        List<EmployeePO> insertList = new ArrayList<>();
        List<EmployeePO> updateList = new ArrayList<>();
        List<String> deleteList = new ArrayList<>();

        // 5. 遍历源数据，判断新增或更新
        for (EmployeeSyncDataVO sourceVO : sourceList) {
            EmployeePO targetPO = targetMap.get(sourceVO.getEmployeeNumber());
            
            if (targetPO == null) {
                // 新增
                EmployeePO newPO = convertToPO(sourceVO);
                insertList.add(newPO);
            } else {
                // 判断是否需要更新 (忽略 periodId 和 updatedTime)
                if (isDifferent(sourceVO, targetPO)) {
                    EmployeePO updatePO = convertToPO(sourceVO);
                    updateList.add(updatePO);
                } else {
                    ignoreCount++;
                }
                // 从目标Map中移除已处理的，剩下的就是需要删除的
                targetMap.remove(sourceVO.getEmployeeNumber());
            }
        }
        
        // 6. 处理删除 (目标Map中剩余的)
        deleteList.addAll(targetMap.keySet());
        
        // 7. 批量执行数据库操作
        // 批量插入
        if (!insertList.isEmpty()) {
            // 分批处理，每批1000条
            int batchSize = 1000;
            for (int i = 0; i < insertList.size(); i += batchSize) {
                int end = Math.min(i + batchSize, insertList.size());
                List<EmployeePO> subList = insertList.subList(i, end);
                employeeMapper.batchInsertEmployees(subList);
            }
            insertCount = insertList.size();
        }
        
        // 批量更新
        if (!updateList.isEmpty()) {
            // 分批处理，每批1000条
            int batchSize = 1000;
            for (int i = 0; i < updateList.size(); i += batchSize) {
                int end = Math.min(i + batchSize, updateList.size());
                List<EmployeePO> subList = updateList.subList(i, end);
                employeeMapper.batchUpdateEmployees(subList);
            }
            updateCount = updateList.size();
        }
        
        // 批量删除
        if (!deleteList.isEmpty()) {
            // 分批处理，每批1000条
            int batchSize = 1000;
            for (int i = 0; i < deleteList.size(); i += batchSize) {
                int end = Math.min(i + batchSize, deleteList.size());
                List<String> subList = deleteList.subList(i, end);
                employeeMapper.batchDeleteEmployees(subList);
            }
            deleteCount = deleteList.size();
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Sync completed successfully");
        result.put("periodId", periodId);
        result.put("rndCount", rndList != null ? rndList.size() : 0);
        result.put("cadreCount", cadreList != null ? cadreList.size() : 0);
        result.put("cadreEmployeeNumberCount", cadreEmployeeNumbers != null ? cadreEmployeeNumbers.size() : 0);
        result.put("totalSource", sourceList.size());
        result.put("insertCount", insertCount);
        result.put("updateCount", updateCount);
        result.put("deleteCount", deleteCount);
        result.put("ignoreCount", ignoreCount);
        
        return result;
    }

    private EmployeePO convertToPO(EmployeeSyncDataVO vo) {
        EmployeePO po = new EmployeePO();
        po.setEmployeeNumber(vo.getEmployeeNumber());
        po.setLastName(vo.getLastName());
        po.setFirstdeptcode(vo.getFirstdeptcode());
        po.setSeconddeptcode(vo.getSeconddeptcode());
        po.setThirddeptcode(vo.getThirddeptcode());
        po.setFourthdeptcode(vo.getFourthdeptcode());
        po.setFifthdeptcode(vo.getFifthdeptcode());
        po.setSixthdeptcode(vo.getSixthdeptcode());
        po.setLowestDeptId(vo.getLowestDeptNumber()); // 注意映射：VO的lowestDeptNumber -> PO的lowestDeptId
        po.setFirstdept(vo.getFirstdept());
        po.setSeconddept(vo.getSeconddept());
        po.setThirddept(vo.getThirddept());
        po.setFourthdept(vo.getFourthdept());
        po.setFifthdept(vo.getFifthdept());
        po.setSixthdept(vo.getSixthdept());
        po.setLowestDept(vo.getLowestDept());
        po.setJobType(vo.getJobType());
        po.setJobCategory(vo.getJobCategory());
        po.setJobSubcategory(vo.getJobSubcategory());
        po.setPeriodId(vo.getPeriodId());
        po.setIsQualificationsStandard(vo.getIsQualificationsStandard());
        po.setIsCertStandard(vo.getIsCertStandard());
        po.setCertTitle(vo.getCertTitle());
        po.setIsPassedSubject2(vo.getIsPassedSubject2());
        po.setCompetenceFamilyCn(vo.getCompetenceFamilyCn());
        po.setCompetenceCategoryCn(vo.getCompetenceCategoryCn());
        po.setCompetenceSubcategoryCn(vo.getCompetenceSubcategoryCn());
        po.setDirectionCnName(vo.getDirectionCnName());
        po.setCompetenceRatingCn(vo.getCompetenceRatingCn());
        po.setCompetenceGradeCn(vo.getCompetenceGradeCn());
        po.setCompetenceFrom(vo.getCompetenceFrom());
        po.setCompetenceTo(vo.getCompetenceTo());
        return po;
    }

    private boolean isDifferent(EmployeeSyncDataVO source, EmployeePO target) {
        // 比较除 periodId 和 updatedTime 外的所有字段
        if (!Objects.equals(source.getLastName(), target.getLastName())) return true;
        if (!Objects.equals(source.getFirstdeptcode(), target.getFirstdeptcode())) return true;
        if (!Objects.equals(source.getSeconddeptcode(), target.getSeconddeptcode())) return true;
        if (!Objects.equals(source.getThirddeptcode(), target.getThirddeptcode())) return true;
        if (!Objects.equals(source.getFourthdeptcode(), target.getFourthdeptcode())) return true;
        if (!Objects.equals(source.getFifthdeptcode(), target.getFifthdeptcode())) return true;
        if (!Objects.equals(source.getSixthdeptcode(), target.getSixthdeptcode())) return true;
        if (!Objects.equals(source.getLowestDeptNumber(), target.getLowestDeptId())) return true; // 注意字段名差异
        if (!Objects.equals(source.getFirstdept(), target.getFirstdept())) return true;
        if (!Objects.equals(source.getSeconddept(), target.getSeconddept())) return true;
        if (!Objects.equals(source.getThirddept(), target.getThirddept())) return true;
        if (!Objects.equals(source.getFourthdept(), target.getFourthdept())) return true;
        if (!Objects.equals(source.getFifthdept(), target.getFifthdept())) return true;
        if (!Objects.equals(source.getSixthdept(), target.getSixthdept())) return true;
        if (!Objects.equals(source.getLowestDept(), target.getLowestDept())) return true;
        if (!Objects.equals(source.getJobType(), target.getJobType())) return true;
        if (!Objects.equals(source.getJobCategory(), target.getJobCategory())) return true;
        if (!Objects.equals(source.getJobSubcategory(), target.getJobSubcategory())) return true;
        // 注意：isQualificationsStandard 和 isCertStandard 可能为null，需要处理
        if (!Objects.equals(source.getIsQualificationsStandard(), target.getIsQualificationsStandard())) return true;
        if (!Objects.equals(source.getIsCertStandard(), target.getIsCertStandard())) return true;

        if (!Objects.equals(source.getCertTitle(), target.getCertTitle())) return true;
        if (!Objects.equals(source.getIsPassedSubject2(), target.getIsPassedSubject2())) return true;
        if (!Objects.equals(source.getCompetenceFamilyCn(), target.getCompetenceFamilyCn())) return true;
        if (!Objects.equals(source.getCompetenceCategoryCn(), target.getCompetenceCategoryCn())) return true;
        if (!Objects.equals(source.getCompetenceSubcategoryCn(), target.getCompetenceSubcategoryCn())) return true;
        if (!Objects.equals(source.getDirectionCnName(), target.getDirectionCnName())) return true;
        if (!Objects.equals(source.getCompetenceRatingCn(), target.getCompetenceRatingCn())) return true;
        if (!Objects.equals(source.getCompetenceGradeCn(), target.getCompetenceGradeCn())) return true;
        if (!Objects.equals(source.getCompetenceFrom(), target.getCompetenceFrom())) return true;
        if (!Objects.equals(source.getCompetenceTo(), target.getCompetenceTo())) return true;
        
        return false;
    }
}
