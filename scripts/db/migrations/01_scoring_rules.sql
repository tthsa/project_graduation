-- Migration: 01_scoring_rules
-- Date: 2026-05-12
-- Adds teacher-configurable scoring rules per homework + computed final_score/grade per submission.

BEGIN;

-- homework: 评分配置(粒度=每个作业)
ALTER TABLE homework ADD COLUMN IF NOT EXISTS test_weight       INTEGER DEFAULT 70;
ALTER TABLE homework ADD COLUMN IF NOT EXISTS llm_weight        INTEGER DEFAULT 30;
ALTER TABLE homework ADD COLUMN IF NOT EXISTS grade_a_threshold INTEGER DEFAULT 90;
ALTER TABLE homework ADD COLUMN IF NOT EXISTS grade_b_threshold INTEGER DEFAULT 75;
ALTER TABLE homework ADD COLUMN IF NOT EXISTS grade_c_threshold INTEGER DEFAULT 60;
ALTER TABLE homework ADD COLUMN IF NOT EXISTS llm_dimensions    TEXT    DEFAULT NULL;

COMMENT ON COLUMN homework.test_weight       IS '测试得分权重 (0-100), 与 llm_weight 之和必须=100';
COMMENT ON COLUMN homework.llm_weight        IS 'LLM 评分权重 (0-100), 与 test_weight 之和必须=100';
COMMENT ON COLUMN homework.grade_a_threshold IS 'A 等级阈值, final_score>=此值为 A';
COMMENT ON COLUMN homework.grade_b_threshold IS 'B 等级阈值, A>B>C';
COMMENT ON COLUMN homework.grade_c_threshold IS 'C 等级阈值, 低于此值为 D';
COMMENT ON COLUMN homework.llm_dimensions    IS 'LLM 评分维度 JSON, NULL=默认单维度。形如 [{"name":"代码质量","weight":50},{"name":"可读性","weight":50}]';

-- evaluation_result: 综合分 + 等级 + 各维度分
ALTER TABLE evaluation_result ADD COLUMN IF NOT EXISTS final_score          INTEGER     DEFAULT NULL;
ALTER TABLE evaluation_result ADD COLUMN IF NOT EXISTS grade                VARCHAR(2)  DEFAULT NULL;
ALTER TABLE evaluation_result ADD COLUMN IF NOT EXISTS llm_dimension_scores TEXT        DEFAULT NULL;

COMMENT ON COLUMN evaluation_result.final_score          IS '综合分 (0-100), NULL=该提交在加列前评的, 未填';
COMMENT ON COLUMN evaluation_result.grade                IS '等级 A/B/C/D, 由 final_score + homework 阈值映射, NULL=同上';
COMMENT ON COLUMN evaluation_result.llm_dimension_scores IS '各 LLM 维度分 JSON, 形如 [{"name":"代码质量","score":8}], NULL=LLM 失败或单维度模式';

COMMIT;

-- 验证: 所有新列都已加上
\d homework
\d evaluation_result
