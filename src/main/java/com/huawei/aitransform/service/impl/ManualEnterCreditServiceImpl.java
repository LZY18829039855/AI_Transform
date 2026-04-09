package com.huawei.aitransform.service.impl;

import com.huawei.aitransform.common.PageResult;
import com.huawei.aitransform.entity.ManualEnterCredit;
import com.huawei.aitransform.entity.ManualEnterCreditBatchImportResult;
import com.huawei.aitransform.entity.PersonalCreditNameRow;
import com.huawei.aitransform.mapper.ManualEnterCreditMapper;
import com.huawei.aitransform.service.PersonalCreditService;
import com.huawei.aitransform.service.ManualEnterCreditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

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

    @Autowired
    private PersonalCreditService personalCreditService;

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
    @Transactional(rollbackFor = Exception.class)
    public ManualEnterCredit create(ManualEnterCredit record, String modifierNumber) {
        if (record == null) {
            throw new IllegalArgumentException("记录不能为空");
        }
        if (!StringUtils.hasText(modifierNumber)) {
            throw new IllegalArgumentException("操作人工号不能为空");
        }
        validateEmployeeNumberAndCredits(record.getEmployeeNumber(), record.getCredits());
        applyEmployeeNameFromPersonalCredit(record);
        Date now = new Date();
        if (record.getCreateTime() == null) {
            record.setCreateTime(now);
        }
        if (record.getUpdateTime() == null) {
            record.setUpdateTime(now);
        }
        record.setModifierNumber(modifierNumber.trim());
        manualEnterCreditMapper.insert(record);
        ManualEnterCredit saved = manualEnterCreditMapper.selectById(record.getId());
        personalCreditService.syncPersonalCreditsForEmployees(Collections.singleton(record.getEmployeeNumber()));
        return saved;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
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
        // 工号不允许修改
        if (StringUtils.hasText(record.getEmployeeNumber())) {
            String reqEmp = record.getEmployeeNumber().trim();
            String oldEmp = existing.getEmployeeNumber() == null ? "" : existing.getEmployeeNumber().trim();
            if (!reqEmp.equals(oldEmp)) {
                throw new IllegalArgumentException("工号不允许修改");
            }
        } else {
            record.setEmployeeNumber(existing.getEmployeeNumber());
        }
        validateEmployeeNumberAndCredits(record.getEmployeeNumber(), record.getCredits());
        applyEmployeeNameFromPersonalCredit(record);
        record.setId(id);
        if (record.getCreateTime() == null) {
            record.setCreateTime(existing.getCreateTime());
        }
        record.setUpdateTime(new Date());
        record.setModifierNumber(modifierNumber.trim());
        manualEnterCreditMapper.updateById(record);
        ManualEnterCredit updated = manualEnterCreditMapper.selectById(id);
        personalCreditService.syncPersonalCreditsForEmployees(Collections.singleton(existing.getEmployeeNumber()));
        return updated;
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
        boolean missingRequired = false;
        for (ManualEnterCredit src : rows) {
            if (src == null) {
                missingRequired = true;
                continue;
            }
            if (!StringUtils.hasText(src.getEmployeeNumber())) {
                missingRequired = true;
            }
            if (!StringUtils.hasText(src.getCredits())) {
                missingRequired = true;
            }
        }
        if (missingRequired) {
            throw new IllegalArgumentException("请确认工号或学分信息都填写完整");
        }
        Map<String, String> empToLastName = loadPersonalCreditNamesForImport(rows);
        String mod = modifierNumber.trim();
        Date now = new Date();
        List<ManualEnterCredit> prepared = new ArrayList<>(rows.size());
        for (ManualEnterCredit src : rows) {
            if (src == null) {
                continue;
            }
            ManualEnterCredit row = new ManualEnterCredit();
            String emp = normalizeEmployeeNumber(src.getEmployeeNumber());
            row.setEmployeeNumber(emp);
            row.setEmployeeName(empToLastName.get(emp));
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
            throw new IllegalArgumentException("请确认工号或学分信息都填写完整");
        }
        int n = manualEnterCreditMapper.insertBatch(prepared);
        // 增量刷新个人学分（同一事务内，失败则回滚导入）
        LinkedHashSet<String> affected = new LinkedHashSet<>();
        for (ManualEnterCredit r : prepared) {
            if (r != null && StringUtils.hasText(r.getEmployeeNumber())) {
                affected.add(r.getEmployeeNumber().trim());
            }
        }
        personalCreditService.syncPersonalCreditsForEmployees(affected);
        return new ManualEnterCreditBatchImportResult(n);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(Integer id) {
        if (id == null) {
            return false;
        }
        ManualEnterCredit existing = manualEnterCreditMapper.selectById(id);
        if (existing == null) {
            return false;
        }
        boolean ok = manualEnterCreditMapper.deleteById(id) > 0;
        if (ok && StringUtils.hasText(existing.getEmployeeNumber())) {
            personalCreditService.syncPersonalCreditsForEmployees(Collections.singleton(existing.getEmployeeNumber().trim()));
        }
        return ok;
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

    /**
     * 将工号 trim 后写入实体，并按 t_personal_credit.last_name 覆盖姓名字段；不存在则抛错。
     */
    private void applyEmployeeNameFromPersonalCredit(ManualEnterCredit record) {
        String emp = normalizeEmployeeNumber(record.getEmployeeNumber());
        if (emp == null) {
            throw new IllegalArgumentException("工号不能为空");
        }
        List<PersonalCreditNameRow> found =
                manualEnterCreditMapper.selectPersonalCreditNamesByEmployeeNumbers(Collections.singletonList(emp));
        if (found == null || found.isEmpty()) {
            throw new IllegalArgumentException("请校验以下工号信息是否正确：" + emp);
        }
        PersonalCreditNameRow first = found.get(0);
        String lastName = first.getLastName();
        record.setEmployeeNumber(emp);
        record.setEmployeeName(lastName == null ? "" : lastName.trim());
    }

    /**
     * 批量导入：一次性查出所有涉及工号在 t_personal_credit 中的 last_name；若有工号不存在则整批失败并列出工号。
     */
    private Map<String, String> loadPersonalCreditNamesForImport(List<ManualEnterCredit> rows) {
        LinkedHashSet<String> unique = new LinkedHashSet<>();
        for (ManualEnterCredit src : rows) {
            if (src == null) {
                continue;
            }
            String emp = normalizeEmployeeNumber(src.getEmployeeNumber());
            if (emp != null) {
                unique.add(emp);
            }
        }
        if (unique.isEmpty()) {
            throw new IllegalArgumentException("没有有效工号可校验");
        }
        List<String> numbers = new ArrayList<>(unique);
        List<PersonalCreditNameRow> found =
                manualEnterCreditMapper.selectPersonalCreditNamesByEmployeeNumbers(numbers);
        Map<String, String> empToLastName = new HashMap<>();
        if (found != null) {
            for (PersonalCreditNameRow r : found) {
                if (r == null || !StringUtils.hasText(r.getEmployeeNumber())) {
                    continue;
                }
                String key = r.getEmployeeNumber().trim();
                String ln = r.getLastName() == null ? "" : r.getLastName().trim();
                empToLastName.putIfAbsent(key, ln);
            }
        }
        LinkedHashSet<String> missing = new LinkedHashSet<>();
        for (String emp : unique) {
            if (!empToLastName.containsKey(emp)) {
                missing.add(emp);
            }
        }
        if (!missing.isEmpty()) {
            throw new IllegalArgumentException("请校验以下工号信息是否正确：" + String.join("、", missing));
        }
        return empToLastName;
    }

    private static String normalizeEmployeeNumber(String s) {
        if (!StringUtils.hasText(s)) {
            return null;
        }
        return s.trim();
    }
}
