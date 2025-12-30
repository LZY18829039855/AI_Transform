package com.huawei.aitransform.service.impl;

import com.huawei.aitransform.entity.EmployeePO;
import com.huawei.aitransform.entity.EmployeeSyncDataVO;
import com.huawei.aitransform.mapper.EmployeeMapper;
import com.huawei.aitransform.service.EmployeeSyncService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> syncEmployeeData(String periodId) {
        if (periodId == null || periodId.trim().isEmpty()) {
            throw new IllegalArgumentException("Period ID cannot be empty");
        }

        // 1. 获取源数据 (t_employee_sync + 达标计算)
        List<EmployeeSyncDataVO> sourceList = employeeMapper.getEmployeeSyncData(periodId);
        Map<String, EmployeeSyncDataVO> sourceMap = sourceList.stream()
                .collect(Collectors.toMap(EmployeeSyncDataVO::getEmployeeNumber, Function.identity(), (k1, k2) -> k1));

        // 2. 获取目标数据 (t_employee 全量)
        List<EmployeePO> targetList = employeeMapper.getAllEmployees();
        Map<String, EmployeePO> targetMap = targetList.stream()
                .collect(Collectors.toMap(EmployeePO::getEmployeeNumber, Function.identity(), (k1, k2) -> k1));

        int insertCount = 0;
        int updateCount = 0;
        int deleteCount = 0;
        int ignoreCount = 0;

        String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        // 3. 遍历源数据，判断新增或更新
        for (EmployeeSyncDataVO sourceVO : sourceList) {
            EmployeePO targetPO = targetMap.get(sourceVO.getEmployeeNumber());
            
            if (targetPO == null) {
                // 新增
                EmployeePO newPO = convertToPO(sourceVO);
                newPO.setUpdatedTime(currentTime);
                employeeMapper.insertEmployee(newPO);
                insertCount++;
            } else {
                // 判断是否需要更新 (忽略 periodId 和 updatedTime)
                if (isDifferent(sourceVO, targetPO)) {
                    EmployeePO updatePO = convertToPO(sourceVO);
                    updatePO.setUpdatedTime(currentTime);
                    employeeMapper.updateEmployee(updatePO);
                    updateCount++;
                } else {
                    ignoreCount++;
                }
                // 从目标Map中移除已处理的，剩下的就是需要删除的
                targetMap.remove(sourceVO.getEmployeeNumber());
            }
        }

        // 4. 处理删除 (目标Map中剩余的)
        for (String employeeNumber : targetMap.keySet()) {
            employeeMapper.deleteEmployee(employeeNumber);
            deleteCount++;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("periodId", periodId);
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
        
        return false;
    }
}
