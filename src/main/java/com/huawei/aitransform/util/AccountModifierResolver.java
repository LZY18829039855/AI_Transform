package com.huawei.aitransform.util;

import com.huawei.aitransform.entity.UserAccountResponseVO;
import com.huawei.aitransform.service.UserConfigService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * 从 Cookie account 解析「修改人工号」写入库字段 Modifier__number / modifierNumber。
 * 优先使用 empNum（去掉首字母后的工号），否则使用 w3Account。
 */
@Component
public class AccountModifierResolver {

    private final UserConfigService userConfigService;

    public AccountModifierResolver(UserConfigService userConfigService) {
        this.userConfigService = userConfigService;
    }

    /**
     * @param accountCookie 可为 null，会再从 request 的 Cookie 中查找 account
     * @return 解析到的操作人工号；无法解析时返回 null
     */
    public String resolveModifierNumber(HttpServletRequest request, String accountCookie) {
        UserAccountResponseVO vo = userConfigService.getUserAccountFromCookie(request, accountCookie);
        if (vo == null) {
            return null;
        }
        if (StringUtils.hasText(vo.getEmpNum())) {
            return vo.getEmpNum().trim();
        }
        if (StringUtils.hasText(vo.getW3Account())) {
            return vo.getW3Account().trim();
        }
        return null;
    }
}
