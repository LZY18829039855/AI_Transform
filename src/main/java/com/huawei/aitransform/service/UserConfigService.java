package com.huawei.aitransform.service;

import com.huawei.aitransform.entity.UserConfigPermissionResponseVO;
import com.huawei.aitransform.entity.UserConfigVO;
import com.huawei.aitransform.mapper.UserConfigMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}

