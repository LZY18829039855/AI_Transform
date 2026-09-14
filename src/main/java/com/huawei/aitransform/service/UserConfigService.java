package com.huawei.aitransform.service;

import com.huawei.aitransform.common.PageResult;
import com.huawei.aitransform.constant.DepartmentConstants;
import com.huawei.aitransform.entity.DepartmentInfoVO;
import com.huawei.aitransform.entity.EmployeeTrainingInfoPO;
import com.huawei.aitransform.entity.UserAccountResponseVO;
import com.huawei.aitransform.entity.UserConfigBatchRequestVO;
import com.huawei.aitransform.entity.UserConfigBatchResultVO;
import com.huawei.aitransform.entity.UserConfigDeptMemberVO;
import com.huawei.aitransform.entity.UserConfigManageVO;
import com.huawei.aitransform.entity.UserConfigPermissionResponseVO;
import com.huawei.aitransform.entity.UserConfigVO;
import com.huawei.aitransform.entity.UserPermissionStatusVO;
import com.huawei.aitransform.mapper.DepartmentInfoMapper;
import com.huawei.aitransform.mapper.EmployeeMapper;
import com.huawei.aitransform.mapper.EmployeeTrainingInfoMapper;
import com.huawei.aitransform.mapper.UserConfigMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 用户权限配置服务类
 */
@Service
public class UserConfigService {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 200;

    @Autowired
    private UserConfigMapper userConfigMapper;

    @Autowired
    private EmployeeMapper employeeMapper;

    @Autowired
    private EmployeeTrainingInfoMapper employeeTrainingInfoMapper;

    @Autowired
    private DepartmentInfoMapper departmentInfoMapper;

    /**
     * 查询所有有效用户的权限，将用户分为admin和非admin两组
     * @return 包含admin和非admin工号列表的响应对象
     */
    public UserConfigPermissionResponseVO getUserPermissions() {
        // 查询所有有效用户
        List<UserConfigVO> validUsers = userConfigMapper.selectValidUsers();

        // 分离admin和非admin用户
        List<String> adminAccounts = new ArrayList<>();
        List<String> nonAdminAccounts = new ArrayList<>();

        for (UserConfigVO user : validUsers) {
            if (user.getAccount() == null || user.getAccount().trim().isEmpty()) {
                continue;
            }

            if (parsePermissionFlag(user.getIsAdmin())) {
                adminAccounts.add(user.getAccount());
            } else {
                nonAdminAccounts.add(user.getAccount());
            }
        }

        return new UserConfigPermissionResponseVO(adminAccounts, nonAdminAccounts);
    }

    /**
     * 若工号首字符为英文字母（A-Z 或 a-z），则去掉该首字符，再用于库表匹配。
     */
    private static String normalizeAccountForLookup(String account) {
        if (account == null) {
            return null;
        }
        String trimmed = account.trim();
        if (trimmed.isEmpty()) {
            return trimmed;
        }
        char c = trimmed.charAt(0);
        if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z')) {
            return trimmed.substring(1);
        }
        return trimmed;
    }

    /**
     * 验证指定工号是否为有效用户（须为云核心网产品线成员）
     * @param account 工号（若首字符为英文字母会先去掉再查询，与 cookie/request 中 account 约定一致）
     * @return true表示是有效用户，false表示未获取到工号或不在云核心网产品线
     */
    public boolean isValidUser(String account) {
        return getUserPermissionStatus(account).isMember();
    }

    /**
     * 查询指定工号的权限状态。
     * 普通用户：登录工号须在 t_employee_sync 中属于云核心网产品线（seconddeptcode=031562）；
     * 管理员 / 超级用户仍仅依据 user_config 表配置判定（且同样须先具备普通用户权限）。
     * @param account 工号（若首字符为英文字母会先去掉再查询）
     */
    public UserPermissionStatusVO getUserPermissionStatus(String account) {
        if (account == null || account.trim().isEmpty()) {
            return new UserPermissionStatusVO(false, false);
        }
        String normalized = normalizeAccountForLookup(account);
        if (normalized.isEmpty()) {
            return new UserPermissionStatusVO(false, false);
        }

        // 仅云核心网产品线成员具备普通用户权限
        Long syncId = employeeMapper.findLatestIdBySecondDeptCodeAndEmployeeNumber(
                DepartmentConstants.CLOUD_CORE_NETWORK_DEPT_CODE, normalized);
        if (syncId == null) {
            return new UserPermissionStatusVO(false, false, false);
        }

        UserConfigVO user = userConfigMapper.selectValidUserByAccount(normalized);
        if (user == null) {
            return new UserPermissionStatusVO(true, false, false);
        }
        boolean asAdmin = parsePermissionFlag(user.getIsAdmin());
        boolean canEditCredit = asAdmin && parsePermissionFlag(user.getCanEditCredit());
        return new UserPermissionStatusVO(true, asAdmin, canEditCredit);
    }

    /**
     * 判断权限字段是否表示已授权
     */
    private boolean parsePermissionFlag(String value) {
        if (value == null) {
            return false;
        }
        return value.equals("1")
                || value.equalsIgnoreCase("Y")
                || value.equalsIgnoreCase("true")
                || value.equalsIgnoreCase("yes");
    }

    /**
     * 从cookie中获取用户工号信息
     * @param request HTTP请求对象，用于获取cookie
     * @param accountCookie 从cookie中获取的工号（可选，如果cookie名称为account）
     * @return 包含emp_num和w3_account的用户工号信息，如果未获取到则返回null
     */
    public UserAccountResponseVO getUserAccountFromCookie(HttpServletRequest request, String accountCookie) {
        // 优先使用 @CookieValue 获取的 account cookie
        String account = accountCookie;
        
        // 如果 @CookieValue 没有获取到，尝试从 HttpServletRequest 中获取
        if (account == null || account.trim().isEmpty()) {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    // 只判断cookie名称为account
                    if ("account".equals(cookie.getName())) {
                        account = cookie.getValue();
                        break;
                    }
                }
            }
        }
        
        // 如果未获取到工号，返回null
        if (account == null || account.trim().isEmpty()) {
            return null;
        }
        
        String accountTrimmed = account.trim();
        String empNum = null;
        String w3Account = null;
        
        // 如果工号以字母开头，w3Account是带首字母的，empNum是不带首字母的
        if (accountTrimmed != null && accountTrimmed.length() > 0 && Character.isLetter(accountTrimmed.charAt(0))) {
            w3Account = accountTrimmed;
            empNum = accountTrimmed.substring(1);
        } else {
            // 如果工号不以字母开头，两者相同
            w3Account = accountTrimmed;
            empNum = accountTrimmed;
        }
        
        return new UserAccountResponseVO(empNum, w3Account);
    }

    /**
     * 权限管理分页列表（仅未删除记录；默认按超级用户 > 管理员 > 普通用户排序）
     * @param roleFilter super / admin / member，空表示全部
     */
    public PageResult<UserConfigManageVO> page(String account, String roleFilter, int pageNum, int pageSize) {
        int pn = pageNum < 1 ? 1 : pageNum;
        int ps = pageSize < 1 ? DEFAULT_PAGE_SIZE : pageSize;
        if (ps > MAX_PAGE_SIZE) {
            ps = MAX_PAGE_SIZE;
        }
        String accountFilter = trimToNull(account);
        String normalizedRole = normalizeRoleFilter(roleFilter);
        int offset = (pn - 1) * ps;
        long total = userConfigMapper.countValidByAccount(accountFilter, normalizedRole);
        List<UserConfigVO> rows = userConfigMapper.selectValidPage(accountFilter, normalizedRole, offset, ps);
        return PageResult.of(total, toManageVOs(rows));
    }

    private String normalizeRoleFilter(String roleFilter) {
        String role = trimToNull(roleFilter);
        if (role == null) {
            return null;
        }
        String lower = role.toLowerCase();
        if ("super".equals(lower) || "admin".equals(lower) || "member".equals(lower)) {
            return lower;
        }
        return null;
    }

    /**
     * 按部门分页查询可选成员（含该节点下全部下级人员，数据源 t_employee_training_info）
     */
    public PageResult<UserConfigDeptMemberVO> pageDeptMembers(String deptId, String keyword, int pageNum, int pageSize) {
        int pn = pageNum < 1 ? 1 : pageNum;
        int ps = pageSize < 1 ? DEFAULT_PAGE_SIZE : pageSize;
        if (ps > MAX_PAGE_SIZE) {
            ps = MAX_PAGE_SIZE;
        }
        DepartmentInfoVO dept = resolveDepartment(deptId);
        if (dept == null) {
            return PageResult.of(0, Collections.<UserConfigDeptMemberVO>emptyList());
        }
        String kw = trimToNull(keyword);
        int offset = (pn - 1) * ps;
        Long totalObj = employeeTrainingInfoMapper.countMembersByDeptLevelAndCode(
                dept.getDeptLevel(), dept.getDeptCode(), kw);
        long total = totalObj == null ? 0L : totalObj;
        if (total <= 0) {
            return PageResult.of(0, Collections.<UserConfigDeptMemberVO>emptyList());
        }
        List<EmployeeTrainingInfoPO> rows = employeeTrainingInfoMapper.listMembersByDeptLevelAndCodePaged(
                dept.getDeptLevel(), dept.getDeptCode(), kw, offset, ps);
        if (rows == null || rows.isEmpty()) {
            return PageResult.of(total, Collections.<UserConfigDeptMemberVO>emptyList());
        }

        List<String> accounts = new ArrayList<>();
        for (EmployeeTrainingInfoPO row : rows) {
            if (row != null && StringUtils.hasText(row.getEmployeeNumber())) {
                accounts.add(row.getEmployeeNumber().trim());
            }
        }
        Set<String> configured = new HashSet<>();
        if (!accounts.isEmpty()) {
            List<String> validAccounts = userConfigMapper.selectValidAccountsByAccounts(accounts);
            if (validAccounts != null) {
                for (String acc : validAccounts) {
                    if (StringUtils.hasText(acc)) {
                        configured.add(acc.trim());
                    }
                }
            }
        }

        List<UserConfigDeptMemberVO> result = new ArrayList<>(rows.size());
        for (EmployeeTrainingInfoPO row : rows) {
            if (row == null || !StringUtils.hasText(row.getEmployeeNumber())) {
                continue;
            }
            String account = row.getEmployeeNumber().trim();
            String name = StringUtils.hasText(row.getLastName()) ? row.getLastName().trim() : null;
            result.add(new UserConfigDeptMemberVO(account, name, configured.contains(account)));
        }
        return PageResult.of(total, result);
    }

    /**
     * 批量新增/覆盖权限配置（同一套权限位）；已存在则覆盖更新（含软删恢复）
     */
    @Transactional(rollbackFor = Exception.class)
    public UserConfigBatchResultVO batchUpsert(UserConfigBatchRequestVO body, String operatorAccount) {
        UserConfigBatchResultVO result = new UserConfigBatchResultVO();
        if (body == null || body.getAccounts() == null || body.getAccounts().isEmpty()) {
            throw new IllegalArgumentException("工号列表不能为空");
        }
        boolean asAdmin = body.isAsAdmin();
        boolean canEditCredit = asAdmin && body.isCanEditCredit();

        // 去重并规范化工号，保留提交顺序
        LinkedHashSet<String> normalizedAccounts = new LinkedHashSet<>();
        for (String raw : body.getAccounts()) {
            String normalized = normalizeAccountForLookup(raw);
            if (StringUtils.hasText(normalized)) {
                normalizedAccounts.add(normalized);
            } else {
                result.getItems().add(new UserConfigBatchResultVO.Item(
                        raw == null ? "" : raw.trim(), "failed", "工号不能为空"));
                result.setFailedCount(result.getFailedCount() + 1);
            }
        }
        if (normalizedAccounts.isEmpty()) {
            return result;
        }

        List<String> accountList = new ArrayList<>(normalizedAccounts);
        Map<String, UserConfigVO> existingMap = new HashMap<>();
        List<UserConfigVO> existingRows = userConfigMapper.selectByAccounts(accountList);
        if (existingRows != null) {
            for (UserConfigVO row : existingRows) {
                if (row == null || !StringUtils.hasText(row.getAccount())) {
                    continue;
                }
                String key = row.getAccount().trim();
                // ORDER BY id DESC：保留每个工号最新一条
                if (!existingMap.containsKey(key)) {
                    existingMap.put(key, row);
                }
            }
        }

        for (String account : accountList) {
            try {
                if (isSameAccount(account, operatorAccount)
                        && (!asAdmin || !canEditCredit)) {
                    result.getItems().add(new UserConfigBatchResultVO.Item(
                            account, "failed", "不能降低自己的权限"));
                    result.setFailedCount(result.getFailedCount() + 1);
                    continue;
                }
                UserConfigVO existing = existingMap.get(account);
                if (existing == null) {
                    UserConfigVO record = new UserConfigVO();
                    record.setAccount(account);
                    record.setIsAdmin(toFlag(asAdmin));
                    record.setCanEditCredit(toFlag(canEditCredit));
                    record.setIsDeleted("0");
                    userConfigMapper.insert(record);
                    result.getItems().add(new UserConfigBatchResultVO.Item(account, "created", "新增成功"));
                    result.setCreatedCount(result.getCreatedCount() + 1);
                } else {
                    existing.setAccount(account);
                    existing.setIsAdmin(toFlag(asAdmin));
                    existing.setCanEditCredit(toFlag(canEditCredit));
                    existing.setIsDeleted("0");
                    userConfigMapper.updateById(existing);
                    result.getItems().add(new UserConfigBatchResultVO.Item(account, "updated", "覆盖更新成功"));
                    result.setUpdatedCount(result.getUpdatedCount() + 1);
                }
            } catch (Exception e) {
                result.getItems().add(new UserConfigBatchResultVO.Item(
                        account, "failed", e.getMessage() == null ? "处理失败" : e.getMessage()));
                result.setFailedCount(result.getFailedCount() + 1);
            }
        }
        return result;
    }

    private DepartmentInfoVO resolveDepartment(String deptId) {
        if (!StringUtils.hasText(deptId)) {
            return null;
        }
        String resolvedDeptId = deptId.trim();
        if ("0".equals(resolvedDeptId)) {
            resolvedDeptId = DepartmentConstants.CLOUD_CORE_NETWORK_DEPT_CODE;
        }
        DepartmentInfoVO dept = departmentInfoMapper.getDepartmentByCode(resolvedDeptId);
        if (dept == null || dept.getDeptLevel() == null || dept.getDeptCode() == null) {
            return null;
        }
        return dept;
    }

    /**
     * 新增权限配置；若工号曾被软删则恢复并覆盖权限
     */
    @Transactional(rollbackFor = Exception.class)
    public UserConfigManageVO create(UserConfigManageVO body) {
        String account = requireNormalizedAccount(body == null ? null : body.getAccount());
        boolean asAdmin = body != null && body.isAsAdmin();
        boolean canEditCredit = asAdmin && body != null && body.isCanEditCredit();

        UserConfigVO existing = userConfigMapper.selectByAccount(account);
        if (existing != null && !isDeletedFlag(existing.getIsDeleted())) {
            throw new IllegalArgumentException("该工号已存在权限配置");
        }
        if (existing != null) {
            existing.setAccount(account);
            existing.setIsAdmin(toFlag(asAdmin));
            existing.setCanEditCredit(toFlag(canEditCredit));
            existing.setIsDeleted("0");
            userConfigMapper.updateById(existing);
            return toManageVO(userConfigMapper.selectValidById(existing.getId()), lookupEmployeeName(account));
        }

        UserConfigVO record = new UserConfigVO();
        record.setAccount(account);
        record.setIsAdmin(toFlag(asAdmin));
        record.setCanEditCredit(toFlag(canEditCredit));
        record.setIsDeleted("0");
        userConfigMapper.insert(record);
        return toManageVO(userConfigMapper.selectValidById(record.getId()), lookupEmployeeName(account));
    }

    /**
     * 更新权限配置（工号不可改为已占用工号）；不允许降低自己的权限
     */
    @Transactional(rollbackFor = Exception.class)
    public UserConfigManageVO update(Integer id, UserConfigManageVO body, String operatorAccount) {
        if (id == null) {
            return null;
        }
        UserConfigVO existing = userConfigMapper.selectValidById(id);
        if (existing == null) {
            return null;
        }
        String account = requireNormalizedAccount(body == null ? null : body.getAccount());
        boolean asAdmin = body != null && body.isAsAdmin();
        boolean canEditCredit = asAdmin && body != null && body.isCanEditCredit();

        if (isSameAccount(existing.getAccount(), operatorAccount) && (!asAdmin || !canEditCredit)) {
            throw new IllegalArgumentException("不能降低自己的权限");
        }
        if (!account.equals(existing.getAccount())) {
            UserConfigVO duplicated = userConfigMapper.selectByAccount(account);
            if (duplicated != null && !id.equals(duplicated.getId()) && !isDeletedFlag(duplicated.getIsDeleted())) {
                throw new IllegalArgumentException("该工号已存在权限配置");
            }
        }

        existing.setAccount(account);
        existing.setIsAdmin(toFlag(asAdmin));
        existing.setCanEditCredit(toFlag(canEditCredit));
        existing.setIsDeleted("0");
        userConfigMapper.updateById(existing);
        return toManageVO(userConfigMapper.selectValidById(id), lookupEmployeeName(account));
    }

    /**
     * 软删除；不允许删除自己的配置
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(Integer id, String operatorAccount) {
        if (id == null) {
            return false;
        }
        UserConfigVO existing = userConfigMapper.selectValidById(id);
        if (existing == null) {
            return false;
        }
        if (isSameAccount(existing.getAccount(), operatorAccount)) {
            throw new IllegalArgumentException("不能删除自己的权限配置");
        }
        return userConfigMapper.softDeleteById(id) > 0;
    }

    private List<UserConfigManageVO> toManageVOs(List<UserConfigVO> rows) {
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> accounts = new ArrayList<>();
        for (UserConfigVO row : rows) {
            if (row != null && StringUtils.hasText(row.getAccount())) {
                accounts.add(row.getAccount().trim());
            }
        }
        Map<String, String> nameMap = lookupEmployeeNames(accounts);
        List<UserConfigManageVO> result = new ArrayList<>(rows.size());
        for (UserConfigVO row : rows) {
            if (row == null) {
                continue;
            }
            String name = row.getAccount() == null ? null : nameMap.get(row.getAccount().trim());
            result.add(toManageVO(row, name));
        }
        return result;
    }

    private UserConfigManageVO toManageVO(UserConfigVO user, String employeeName) {
        UserConfigManageVO vo = new UserConfigManageVO();
        if (user == null) {
            return vo;
        }
        vo.setId(user.getId());
        vo.setAccount(user.getAccount());
        vo.setEmployeeName(employeeName);
        boolean asAdmin = parsePermissionFlag(user.getIsAdmin());
        vo.setAsAdmin(asAdmin);
        vo.setCanEditCredit(asAdmin && parsePermissionFlag(user.getCanEditCredit()));
        return vo;
    }

    private Map<String, String> lookupEmployeeNames(List<String> accounts) {
        if (accounts == null || accounts.isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            // 从全员训战表取姓名（覆盖非研发族），避免仅依赖 t_employee（仅研发族+干部）
            List<EmployeeTrainingInfoPO> employees =
                    employeeTrainingInfoMapper.listBasicInfoByEmployeeNumbers(accounts);
            if (employees == null || employees.isEmpty()) {
                return Collections.emptyMap();
            }
            Map<String, String> nameMap = new HashMap<>();
            for (EmployeeTrainingInfoPO employee : employees) {
                if (employee == null || !StringUtils.hasText(employee.getEmployeeNumber())) {
                    continue;
                }
                if (StringUtils.hasText(employee.getLastName())) {
                    nameMap.put(employee.getEmployeeNumber().trim(), employee.getLastName().trim());
                }
            }
            return nameMap;
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    private String lookupEmployeeName(String account) {
        if (!StringUtils.hasText(account)) {
            return null;
        }
        return lookupEmployeeNames(Collections.singletonList(account)).get(account);
    }

    private String requireNormalizedAccount(String account) {
        String normalized = normalizeAccountForLookup(account);
        if (!StringUtils.hasText(normalized)) {
            throw new IllegalArgumentException("工号不能为空");
        }
        return normalized;
    }

    private boolean isSameAccount(String left, String right) {
        String a = normalizeAccountForLookup(left);
        String b = normalizeAccountForLookup(right);
        return StringUtils.hasText(a) && a.equals(b);
    }

    private boolean isDeletedFlag(String value) {
        if (value == null) {
            return false;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return false;
        }
        return !(trimmed.equals("0")
                || trimmed.equalsIgnoreCase("N")
                || trimmed.equalsIgnoreCase("false"));
    }

    private String toFlag(boolean value) {
        return value ? "1" : "0";
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}

