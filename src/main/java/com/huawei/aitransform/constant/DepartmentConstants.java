package com.huawei.aitransform.constant;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 部门常量类
 * 用于存储部门相关的常量，避免在代码中出现魔鬼数字
 */
public class DepartmentConstants {

    /**
     * 云核心网产品线部门ID（2级部门）
     */
    public static final String CLOUD_CORE_NETWORK_DEPT_CODE = "031562";

    /**
     * 研发管理部部门ID
     */
    public static final String R_D_MANAGEMENT_DEPT_CODE = "030681";

    /**
     * 部门课程完成率接口：当入参为 0 或云核心网二级部门时，仅统计以下四级部门（按此顺序返回）
     */
    public static final List<String> COMPLETION_RATE_LEVEL4_DEPT_CODES = Collections.unmodifiableList(Arrays.asList(
            "047375", "047374", "043539", "041852", "038460", "030699", "038462", "038461", "047376"
    ));

    /**
     * 私有构造函数，防止实例化
     */
    private DepartmentConstants() {
        throw new UnsupportedOperationException("常量类不允许实例化");
    }
}

