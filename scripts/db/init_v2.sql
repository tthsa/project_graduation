-- ============================================
-- Java Evaluation System - Database Init Script (PostgreSQL)
-- ============================================

-- 删除已存在的表（注意顺序，先删外键表）
DROP TABLE IF EXISTS system_config CASCADE;
DROP TABLE IF EXISTS llm_config CASCADE;
DROP TABLE IF EXISTS notification CASCADE;
DROP TABLE IF EXISTS task CASCADE;
DROP TABLE IF EXISTS evaluation_result CASCADE;
DROP TABLE IF EXISTS submission_history CASCADE;
DROP TABLE IF EXISTS submission_file CASCADE;
DROP TABLE IF EXISTS submission CASCADE;
DROP TABLE IF EXISTS test_case CASCADE;
DROP TABLE IF EXISTS homework CASCADE;
DROP TABLE IF EXISTS course CASCADE;
DROP TABLE IF EXISTS student CASCADE;
DROP TABLE IF EXISTS class CASCADE;
DROP TABLE IF EXISTS teacher CASCADE;
DROP TABLE IF EXISTS admin CASCADE;

-- ============================================
-- 1. 管理员表
-- ============================================
CREATE TABLE admin (
                       id SERIAL PRIMARY KEY,
                       username VARCHAR(50) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       name VARCHAR(50),
                       email VARCHAR(100),
                       create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP
);

COMMENT ON TABLE admin IS '管理员表';
COMMENT ON COLUMN admin.username IS '用户名';
COMMENT ON COLUMN admin.password IS '密码';
COMMENT ON COLUMN admin.name IS '姓名';
COMMENT ON COLUMN admin.email IS '邮箱';
COMMENT ON COLUMN admin.create_time IS '创建时间';
COMMENT ON COLUMN admin.updated_at IS '更新时间';

-- ============================================
-- 2. 教师表
-- ============================================
CREATE TABLE teacher (
                         id SERIAL PRIMARY KEY,
                         username VARCHAR(50) NOT NULL UNIQUE,
                         password VARCHAR(255) NOT NULL,
                         name VARCHAR(50),
                         email VARCHAR(100),
                         status INTEGER DEFAULT 1,
                         create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP
);

COMMENT ON TABLE teacher IS '教师表';
COMMENT ON COLUMN teacher.status IS '状态：1-正常，0-禁用';

-- ============================================
-- 3. 班级表
-- ============================================
CREATE TABLE class (
                       id SERIAL PRIMARY KEY,
                       name VARCHAR(100) NOT NULL,
                       teacher_id INTEGER REFERENCES teacher(id),
                       create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE class IS '班级表';

-- ============================================
-- 4. 学生表
-- ============================================
CREATE TABLE student (
                         id SERIAL PRIMARY KEY,
                         username VARCHAR(50) NOT NULL UNIQUE,
                         student_no VARCHAR(50),
                         password VARCHAR(255) NOT NULL,
                         name VARCHAR(50),
                         email VARCHAR(100),
                         class_id INTEGER REFERENCES class(id),
                         status INTEGER DEFAULT 1,
                         first_login INTEGER DEFAULT 1,
                         create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP
);

COMMENT ON TABLE student IS '学生表';
COMMENT ON COLUMN student.status IS '状态：1-正常，0-禁用';
COMMENT ON COLUMN student.first_login IS '首次登录：1-是，0-否';

-- ============================================
-- 5. 课程表
-- ============================================
CREATE TABLE course (
                        id SERIAL PRIMARY KEY,
                        name VARCHAR(100) NOT NULL,
                        teacher_id INTEGER NOT NULL REFERENCES teacher(id),
                        class_id INTEGER REFERENCES class(id),
                        create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE course IS '课程表';

-- ============================================
-- 6. 作业表
-- ============================================
CREATE TABLE homework (
                          id SERIAL PRIMARY KEY,
                          course_id INTEGER NOT NULL REFERENCES course(id),
                          title VARCHAR(200) NOT NULL,
                          description TEXT,
                          deadline TIMESTAMP,
                          status INTEGER DEFAULT 1,
                          create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP
);

COMMENT ON TABLE homework IS '作业表';
COMMENT ON COLUMN homework.status IS '状态：1-进行中，0-已结束';

-- ============================================
-- 7. 测试用例表
-- ============================================
CREATE TABLE test_case (
                           id SERIAL PRIMARY KEY,
                           homework_id INTEGER NOT NULL REFERENCES homework(id),
                           name VARCHAR(100),
                           input TEXT,
                           expected_output TEXT,
                           is_public INTEGER DEFAULT 0,
                           sort_order INTEGER DEFAULT 0,
                           create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE test_case IS '测试用例表';
COMMENT ON COLUMN test_case.is_public IS '是否公开：1-是，0-否';
COMMENT ON COLUMN test_case.sort_order IS '排序顺序';

-- ============================================
-- 8. 提交记录表
-- ============================================
CREATE TABLE submission (
                            id SERIAL PRIMARY KEY,
                            homework_id INTEGER NOT NULL REFERENCES homework(id),
                            student_id INTEGER NOT NULL REFERENCES student(id),
                            submit_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            status INTEGER DEFAULT 0
);

COMMENT ON TABLE submission IS '提交记录表';
COMMENT ON COLUMN submission.status IS '状态：0-待评测，1-评测中，2-已完成，3-失败';

-- ============================================
-- 9. 提交文件表
-- ============================================
CREATE TABLE submission_file (
                                 id SERIAL PRIMARY KEY,
                                 submission_id INTEGER NOT NULL REFERENCES submission(id),
                                 file_path VARCHAR(500) NOT NULL
);

COMMENT ON TABLE submission_file IS '提交文件表';

-- ============================================
-- 10. 提交历史表
-- ============================================
CREATE TABLE submission_history (
                                    id SERIAL PRIMARY KEY,
                                    submission_id INTEGER NOT NULL REFERENCES submission(id),
                                    submit_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE submission_history IS '提交历史表';

-- ============================================
-- 11. 评测结果表
-- ============================================
CREATE TABLE evaluation_result (
                                   id SERIAL PRIMARY KEY,
                                   submission_id INTEGER NOT NULL REFERENCES submission(id),
                                   test_score INTEGER DEFAULT 0,
                                   llm_score INTEGER DEFAULT 0,
                                   llm_review TEXT,
                                   execution_time BIGINT DEFAULT 0,
                                   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE evaluation_result IS '评测结果表';
COMMENT ON COLUMN evaluation_result.execution_time IS '执行时间(毫秒)';

-- ============================================
-- 12. 任务表
-- ============================================
CREATE TABLE task (
                      id SERIAL PRIMARY KEY,
                      homework_id INTEGER NOT NULL REFERENCES homework(id),
                      name VARCHAR(100),
                      description TEXT,
                      status INTEGER DEFAULT 0,
                      create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE task IS '任务表';
COMMENT ON COLUMN task.status IS '状态：0-待处理，1-处理中，2-已完成';

-- ============================================
-- 13. 通知表
-- ============================================
CREATE TABLE notification (
                              id SERIAL PRIMARY KEY,
                              user_id INTEGER NOT NULL,
                              title VARCHAR(200) NOT NULL,
                              content TEXT,
                              is_read SMALLINT DEFAULT 0,
                              create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE notification IS '通知表';
COMMENT ON COLUMN notification.is_read IS '是否已读：0-未读，1-已读';

-- ============================================
-- 14. LLM配置表
-- ============================================
CREATE TABLE llm_config (
                            id SERIAL PRIMARY KEY,
                            name VARCHAR(100) NOT NULL,
                            api_key VARCHAR(500),
                            api_url VARCHAR(500),
                            model VARCHAR(100),
                            status INTEGER DEFAULT 1,
                            create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE llm_config IS 'LLM配置表';
COMMENT ON COLUMN llm_config.status IS '状态：1-启用，0-禁用';

-- ============================================
-- 15. 系统配置表
-- ============================================
CREATE TABLE system_config (
                               id SERIAL PRIMARY KEY,
                               config_key VARCHAR(100) NOT NULL UNIQUE,
                               config_value TEXT,
                               description VARCHAR(500)
);

COMMENT ON TABLE system_config IS '系统配置表';

-- ============================================
-- 初始数据
-- ============================================

-- 默认管理员 (密码: admin123，需要用 BCrypt 加密)
INSERT INTO admin (username, password, name, email)
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '系统管理员', 'admin@example.com');

-- 默认教师
INSERT INTO teacher (username, password, name, email, status)
VALUES ('teacher', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '测试教师', 'teacher@example.com', 1);

-- 默认班级
INSERT INTO class (name, teacher_id) VALUES ('计算机科学2024级1班', 1);

-- 默认学生
INSERT INTO student (username, student_no, password, name, email, class_id, status, first_login)
VALUES ('student', '2024001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '测试学生', 'student@example.com', 1, 1, 1);

-- 默认LLM配置
INSERT INTO llm_config (name, api_key, api_url, model, status)
VALUES ('DeepSeek', 'your-api-key', 'https://api.deepseek.com/v1/chat/completions', 'deepseek-chat', 1);

-- 系统配置
INSERT INTO system_config (config_key, config_value, description) VALUES
                                                                      ('max_submission_size', '10485760', '最大提交文件大小(字节)'),
                                                                      ('docker_timeout', '30000', 'Docker执行超时时间(毫秒)'),
                                                                      ('max_test_cases', '50', '每个作业最大测试用例数');

-- ============================================
-- 索引（可选，提升查询性能）
-- ============================================
CREATE INDEX idx_student_class_id ON student(class_id);
CREATE INDEX idx_course_teacher_id ON course(teacher_id);
CREATE INDEX idx_course_class_id ON course(class_id);
CREATE INDEX idx_homework_course_id ON homework(course_id);
CREATE INDEX idx_test_case_homework_id ON test_case(homework_id);
CREATE INDEX idx_submission_homework_id ON submission(homework_id);
CREATE INDEX idx_submission_student_id ON submission(student_id);
CREATE INDEX idx_submission_file_submission_id ON submission_file(submission_id);
CREATE INDEX idx_submission_history_submission_id ON submission_history(submission_id);
CREATE INDEX idx_evaluation_result_submission_id ON evaluation_result(submission_id);
CREATE INDEX idx_task_homework_id ON task(homework_id);
CREATE INDEX idx_notification_user_id ON notification(user_id);