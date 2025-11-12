-- 全员基础信息表
CREATE TABLE IF NOT EXISTS `employee_info` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '自增ID',
    `name` VARCHAR(50) NOT NULL COMMENT '姓名',
    `employee_no` VARCHAR(50) NOT NULL COMMENT '工号',
    `department` VARCHAR(100) DEFAULT NULL COMMENT '部门',
    `position_type` VARCHAR(50) DEFAULT NULL COMMENT '职位类（软件类、系统类、研究类等）',
    `job_level` VARCHAR(50) DEFAULT NULL COMMENT '任职级别',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_employee_no` (`employee_no`),
    KEY `idx_department` (`department`),
    KEY `idx_position_type` (`position_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='全员基础信息表';

