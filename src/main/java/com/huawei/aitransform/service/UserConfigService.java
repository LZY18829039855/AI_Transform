package com.huawei.aitransform.service;

import com.huawei.aitransform.entity.UserAccountResponseVO;
import com.huawei.aitransform.entity.UserConfigPermissionResponseVO;
import com.huawei.aitransform.entity.UserConfigVO;
import com.huawei.aitransform.mapper.UserConfigMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户权限配置服务类
 */
@Service
public class UserConfigService {

    @Autowired
    private UserConfigMapper userConfigMapper;

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

            // 判断是否为管理员（is_admin为"1"、"Y"、"true"等视为管理员）
            String isAdmin = user.getIsAdmin();
            if (isAdmin != null && (isAdmin.equals("1") || isAdmin.equalsIgnoreCase("Y") 
                    || isAdmin.equalsIgnoreCase("true") || isAdmin.equalsIgnoreCase("yes"))) {
                adminAccounts.add(user.getAccount());
            } else {
                nonAdminAccounts.add(user.getAccount());
            }
        }

        return new UserConfigPermissionResponseVO(adminAccounts, nonAdminAccounts);
    }

    /**
     * 验证指定工号是否为有效用户
     * @param account 工号
     * @return true表示是有效用户，false表示不是有效用户或不存在
     */
    public boolean isValidUser(String account) {
        if (account == null || account.trim().isEmpty()) {
            return false;
        }
        
        UserConfigVO user = userConfigMapper.selectValidUserByAccount(account.trim());
        return user != null;
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
}

