-- 组织成熟度等级表
CREATE TABLE IF NOT EXISTS `organization_maturity` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '自增ID',
    `organization_name` VARCHAR(100) NOT NULL COMMENT '组织名称',
    `maturity_level` VARCHAR(10) DEFAULT NULL COMMENT '成熟度等级（L1/L2/L3）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_organization_name` (`organization_name`),
    KEY `idx_maturity_level` (`maturity_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='组织成熟度等级表';



