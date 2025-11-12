DROP TABLE IF EXISTS `t_mes_ct_dept`;

CREATE TABLE `t_mes_ct_dept` (
  `create_by` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建人',
  `coa` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '部门COA编号',
  `parent_dept_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '上级部门编码',
  `dept_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '部门ID',
  `dept_name` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '部门名称',
  `dept_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '部门编码',
  `lastupdate_date` datetime NULL DEFAULT NULL COMMENT '最后更新时间',
  `lastupdate_by` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '最后更新人',
  `virtual` tinyint(1) NULL DEFAULT NULL COMMENT '是否虚拟部门',
  `dept_level` int(11) NULL DEFAULT NULL COMMENT '组织层级',
  `enabled` tinyint(1) NULL DEFAULT NULL COMMENT '是否有效',
  `create_date` datetime NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`dept_id`),
  KEY `idx_dept_code` (`dept_code`),
  KEY `idx_parent_dept_code` (`parent_dept_code`),
  KEY `idx_dept_level` (`dept_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='部门层级信息表';

