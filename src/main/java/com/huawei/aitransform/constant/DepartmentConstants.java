package com.huawei.aitransform.constant;

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
     * 私有构造函数，防止实例化
     */
    private DepartmentConstants() {
        throw new UnsupportedOperationException("常量类不允许实例化");
    }
}

