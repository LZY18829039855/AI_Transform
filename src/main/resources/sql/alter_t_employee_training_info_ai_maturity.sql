-- 全员训战表：增加干部/专家岗位 AI 成熟度（与 sync-employee-training-info 同步写入）
-- 部署前在目标库执行一次

ALTER TABLE t_employee_training_info
    ADD COLUMN cadre_position_ai_maturity VARCHAR(64) NULL COMMENT '干部岗位AI成熟度，对应 t_cadre.position_ai_maturity',
    ADD COLUMN expert_position_ai_maturity VARCHAR(64) NULL COMMENT '专家岗位AI成熟度，对应 t_expert.position_ai_maturity';
