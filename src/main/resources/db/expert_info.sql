DROP TABLE IF EXISTS `expert_info`;

CREATE TABLE `expert_info` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `position_code` VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '岗位编号',
  `org_level` INT(11) NULL DEFAULT NULL COMMENT '岗位所属的组织层级',
  `l4_org_code` VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '统计岗位所属的四层组织编码',
  `l4_org_name` VARCHAR(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '统计岗位所属的四层组织名称',
  `professional_field` VARCHAR(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '专业领域',
  `business_field` VARCHAR(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '业务领域',
  `position_name` VARCHAR(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '岗位名称',
  `position_grade` VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '岗位职级',
  `position_region` VARCHAR(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '岗位地域',
  `job_family` VARCHAR(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '职位族',
  `job_category` VARCHAR(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '职位类',
  `job_subcategory` VARCHAR(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '职位子类',
  `ai_maturity` VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'AI成熟度（L1/L2/L3）',
  `on_duty_status` TINYINT(1) NULL DEFAULT NULL COMMENT '在岗情况（0-不在岗，1-在岗）',
  `employee_number` VARCHAR(180) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '在岗人工号',
  `employee_name` VARCHAR(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '在岗人姓名',
  `appointed_position_grade` VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '任命岗位职级',
  `birth_date` DATE NULL DEFAULT NULL COMMENT '出生日期',
  `age` INT(11) NULL DEFAULT NULL COMMENT '年龄',
  `tenure_start_time` DATE NULL DEFAULT NULL COMMENT '任期制开始时间',
  `tenure_duration` INT(11) NULL DEFAULT NULL COMMENT '任期时长（月）',
  `actual_vacancy_time` DATE NULL DEFAULT NULL COMMENT '实际空缺时间',
  `vacancy_duration` INT(11) NULL DEFAULT NULL COMMENT '空缺时长（天）',
  `remarks` TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_position_code` (`position_code`),
  KEY `idx_org_level` (`org_level`),
  KEY `idx_l4_org_code` (`l4_org_code`),
  KEY `idx_employee_number` (`employee_number`),
  KEY `idx_on_duty_status` (`on_duty_status`),
  KEY `idx_ai_maturity` (`ai_maturity`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='专家信息表';




