-- 干部数据表
CREATE TABLE IF NOT EXISTS `cadre_data` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '自增ID',
    `employee_no` VARCHAR(50) NOT NULL COMMENT '工号',
    `appointed_position` VARCHAR(100) DEFAULT NULL COMMENT '任命岗位',
    `position_maturity` VARCHAR(10) DEFAULT NULL COMMENT '岗位成熟度（L1/L2/L3）',
    `cadre_position_type` VARCHAR(50) DEFAULT NULL COMMENT '干部职位类（软件、非软件）',
    `cadre_type` VARCHAR(50) DEFAULT NULL COMMENT '干部类型（商业干部、资源干部、职能干部）',
    `is_grassroots_supervisor` TINYINT(1) DEFAULT 0 COMMENT '基层主管标识（0-否，1-是）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_employee_no` (`employee_no`),
    KEY `idx_position_maturity` (`position_maturity`),
    KEY `idx_cadre_type` (`cadre_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='干部数据表';


