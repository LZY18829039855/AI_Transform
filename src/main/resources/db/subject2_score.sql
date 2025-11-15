-- 科目二成绩表
CREATE TABLE IF NOT EXISTS `subject2_score` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '自增ID',
    `name` VARCHAR(50) NOT NULL COMMENT '姓名',
    `employee_no` VARCHAR(50) NOT NULL COMMENT '工号',
    `is_passed` TINYINT(1) DEFAULT 0 COMMENT '科目二是否通过（0-未通过，1-通过）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_employee_no` (`employee_no`),
    KEY `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='科目二成绩表';




