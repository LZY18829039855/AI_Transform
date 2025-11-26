-- 专家数据表
CREATE TABLE IF NOT EXISTS `expert_data` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '自增ID',
    `name` VARCHAR(50) NOT NULL COMMENT '姓名',
    `employee_no` VARCHAR(50) NOT NULL COMMENT '工号',
    `appointed_position` VARCHAR(100) DEFAULT NULL COMMENT '任命岗位',
    `position_maturity` VARCHAR(10) DEFAULT NULL COMMENT '岗位成熟度（L1/L2/L3）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_employee_no` (`employee_no`),
    KEY `idx_name` (`name`),
    KEY `idx_position_maturity` (`position_maturity`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='专家数据表';








