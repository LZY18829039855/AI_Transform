-- AI任职表
CREATE TABLE IF NOT EXISTS `ai_position` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '自增ID',
    `name` VARCHAR(50) NOT NULL COMMENT '姓名',
    `employee_no` VARCHAR(50) NOT NULL COMMENT '工号',
    `has_certificate` TINYINT(1) DEFAULT 0 COMMENT '是否持有证书（0-否，1-是）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_employee_no` (`employee_no`),
    KEY `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI任职表';


