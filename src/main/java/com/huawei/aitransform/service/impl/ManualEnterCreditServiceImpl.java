package com.huawei.aitransform.service.impl;

import com.huawei.aitransform.common.PageResult;
import com.huawei.aitransform.entity.ManualEnterCredit;
import com.huawei.aitransform.entity.ManualEnterCreditBatchImportResult;
import com.huawei.aitransform.mapper.ManualEnterCreditMapper;
import com.huawei.aitransform.service.ManualEnterCreditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 手动录入学分服务实现
 */
@Service
public class ManualEnterCreditServiceImpl implements ManualEnterCreditService {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 200;
    private static final int MAX_BATCH_IMPORT_SIZE = 500;

    @Autowired
    private ManualEnterCreditMapper manualEnterCreditMapper;

    @Override
    public PageResult<ManualEnterCredit> page(String employeeNumber, String employeeName, int pageNum, int pageSize) {
        int pn = pageNum < 1 ? 1 : pageNum;
        int ps = pageSize < 1 ? DEFAULT_PAGE_SIZE : pageSize;
        if (ps > MAX_PAGE_SIZE) {
            ps = MAX_PAGE_SIZE;
        }
        int offset = (pn - 1) * ps;
        long total = manualEnterCreditMapper.countByCondition(trimToNull(employeeNumber), trimToNull(employeeName));
        List<ManualEnterCredit> rows = manualEnterCreditMapper.selectPage(
                trimToNull(employeeNumber), trimToNull(employeeName), offset, ps);
        return PageResult.of(total, rows);
    }

    @Override
    public ManualEnterCredit getById(Integer id) {
        if (id == null) {
            return null;
        }
        return manualEnterCreditMapper.selectById(id);
    }

    @Override
    public ManualEnterCredit create(ManualEnterCredit record, String modifierNumber) {
        if (record == null) {
            throw new IllegalArgumentException("记录不能为空");
        }
        if (!StringUtils.hasText(modifierNumber)) {
            throw new IllegalArgumentException("操作人工号不能为空");
        }
        validateEmployeeNumberAndCredits(record.getEmployeeNumber(), record.getCredits());
        Date now = new Date();
        if (record.getCreateTime() == null) {
            record.setCreateTime(now);
        }
        if (record.getUpdateTime() == null) {
            record.setUpdateTime(now);
        }
        record.setModifierNumber(modifierNumber.trim());
        manualEnterCreditMapper.insert(record);
        return manualEnterCreditMapper.selectById(record.getId());
    }

    @Override
    public ManualEnterCredit update(Integer id, ManualEnterCredit record, String modifierNumber) {
        if (id == null) {
            throw new IllegalArgumentException("id 不能为空");
        }
        if (record == null) {
            throw new IllegalArgumentException("记录不能为空");
        }
        if (!StringUtils.hasText(modifierNumber)) {
            throw new IllegalArgumentException("操作人工号不能为空");
        }
        ManualEnterCredit existing = manualEnterCreditMapper.selectById(id);
        if (existing == null) {
            return null;
        }
        validateEmployeeNumberAndCredits(record.getEmployeeNumber(), record.getCredits());
        record.setId(id);
        if (record.getCreateTime() == null) {
            record.setCreateTime(existing.getCreateTime());
        }
        record.setUpdateTime(new Date());
        record.setModifierNumber(modifierNumber.trim());
        manualEnterCreditMapper.updateById(record);
        return manualEnterCreditMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ManualEnterCreditBatchImportResult batchImport(List<ManualEnterCredit> rows, String modifierNumber) {
        if (rows == null || rows.isEmpty()) {
            throw new IllegalArgumentException("导入数据不能为空");
        }
        if (!StringUtils.hasText(modifierNumber)) {
            throw new IllegalArgumentException("操作人工号不能为空");
        }
        if (rows.size() > MAX_BATCH_IMPORT_SIZE) {
            throw new IllegalArgumentException("单次最多导入 " + MAX_BATCH_IMPORT_SIZE + " 条");
        }
        List<String> validationErrors = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            ManualEnterCredit src = rows.get(i);
            if (src == null) {
                validationErrors.add("第" + (i + 1) + "条：数据为空");
                continue;
            }
            if (!StringUtils.hasText(src.getEmployeeNumber())) {
                validationErrors.add("第" + (i + 1) + "条：工号不能为空");
            }
            if (!StringUtils.hasText(src.getCredits())) {
                validationErrors.add("第" + (i + 1) + "条：获得学分不能为空");
            }
        }
        if (!validationErrors.isEmpty()) {
            throw new IllegalArgumentException(validationErrors.stream().distinct().collect(Collectors.joining("；")));
        }
        String mod = modifierNumber.trim();
        Date now = new Date();
        List<ManualEnterCredit> prepared = new ArrayList<>(rows.size());
        for (ManualEnterCredit src : rows) {
            if (src == null) {
                continue;
            }
            ManualEnterCredit row = new ManualEnterCredit();
            row.setEmployeeNumber(src.getEmployeeNumber());
            row.setEmployeeName(src.getEmployeeName());
            row.setCreditType(src.getCreditType());
            row.setActivityName(src.getActivityName());
            row.setActivityDate(src.getActivityDate());
            row.setCredits(src.getCredits());
            row.setDescription(src.getDescription());
            row.setAttachmentUrl(src.getAttachmentUrl());
            row.setCreateTime(now);
            row.setUpdateTime(now);
            row.setModifierNumber(mod);
            prepared.add(row);
        }
        if (prepared.isEmpty()) {
            throw new IllegalArgumentException("没有有效行可导入");
        }
        int n = manualEnterCreditMapper.insertBatch(prepared);
        return new ManualEnterCreditBatchImportResult(n);
    }

    @Override
    public boolean delete(Integer id) {
        if (id == null) {
            return false;
        }
        return manualEnterCreditMapper.deleteById(id) > 0;
    }

    private static String trimToNull(String s) {
        if (!StringUtils.hasText(s)) {
            return null;
        }
        return s.trim();
    }

    /**
     * 单次新增/修改时的工号、学分校验（与批量导入规则一致）
     */
    private static void validateEmployeeNumberAndCredits(String employeeNumber, String credits) {
        if (!StringUtils.hasText(employeeNumber)) {
            throw new IllegalArgumentException("工号不能为空");
        }
        if (!StringUtils.hasText(credits)) {
            throw new IllegalArgumentException("获得学分不能为空");
        }
    }
}
