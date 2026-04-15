-- =====================================================
-- Java作业评测系统 - 数据库初始化脚本
-- =====================================================

-- 创建数据库（如果不存在）
-- CREATE DATABASE java_evaluation;

-- =====================================================
-- 1. 用户相关表
-- =====================================================

-- 管理员表
CREATE TABLE IF NOT EXISTS admin (
                                     id SERIAL PRIMARY KEY,
                                     username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(100),
    email VARCHAR(100),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- 教师表
CREATE TABLE IF NOT EXISTS teacher (
                                       id SERIAL PRIMARY KEY,
                                       username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- 学生表
CREATE TABLE IF NOT EXISTS student (
                                       id SERIAL PRIMARY KEY,
                                       username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    class_id INTEGER,
    student_no VARCHAR(50),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- 班级表
CREATE TABLE IF NOT EXISTS class_info (
                                          id SERIAL PRIMARY KEY,
                                          name VARCHAR(100) NOT NULL,
    teacher_id INTEGER REFERENCES teacher(id),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- =====================================================
-- 2. 作业相关表
-- =====================================================

-- 作业表
CREATE TABLE IF NOT EXISTS homework (
                                        id SERIAL PRIMARY KEY,
                                        title VARCHAR(200) NOT NULL,
    description TEXT,
    teacher_id INTEGER REFERENCES teacher(id),
    class_id INTEGER REFERENCES class_info(id),
    deadline TIMESTAMP,
    status INTEGER DEFAULT 0,  -- 0=草稿, 1=已发布, 2=已结束
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- 测试用例表
CREATE TABLE IF NOT EXISTS test_case (
                                         id SERIAL PRIMARY KEY,
                                         homework_id INTEGER REFERENCES homework(id),
    input TEXT,
    expected_output TEXT NOT NULL,
    score INTEGER DEFAULT 10,
    is_public BOOLEAN DEFAULT FALSE,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- =====================================================
-- 3. 提交相关表
-- =====================================================

-- 提交记录表
CREATE TABLE IF NOT EXISTS submission (
                                          id SERIAL PRIMARY KEY,
                                          homework_id INTEGER REFERENCES homework(id),
    student_id INTEGER REFERENCES student(id),
    submit_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status INTEGER DEFAULT 0  -- 0=待评测, 1=评测中, 2=完成, 3=失败
    );

-- 提交文件表（新增）
CREATE TABLE IF NOT EXISTS submission_file (
                                               id SERIAL PRIMARY KEY,
                                               submission_id INTEGER NOT NULL REFERENCES submission(id) ON DELETE CASCADE,
    file_path VARCHAR(500) NOT NULL,
    file_name VARCHAR(200) NOT NULL,
    file_size BIGINT DEFAULT 0,
    upload_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- 提交历史表
CREATE TABLE IF NOT EXISTS submission_history (
                                                  id SERIAL PRIMARY KEY,
                                                  submission_id INTEGER REFERENCES submission(id),
    submit_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- =====================================================
-- 4. 评测相关表
-- =====================================================

-- 评测任务表
CREATE TABLE IF NOT EXISTS evaluation_task (
                                               id SERIAL PRIMARY KEY,
                                               submission_id INTEGER REFERENCES submission(id),
    status INTEGER DEFAULT 0,  -- 0=待处理, 1=处理中, 2=完成, 3=失败
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    complete_time TIMESTAMP
    );

-- 评测结果表
CREATE TABLE IF NOT EXISTS evaluation_result (
                                                 id SERIAL PRIMARY KEY,
                                                 submission_id INTEGER REFERENCES submission(id),
    test_case_id INTEGER REFERENCES test_case(id),
    passed BOOLEAN DEFAULT FALSE,
    actual_output TEXT,
    error_message TEXT,
    execution_time BIGINT,  -- 执行时间（毫秒）
    score INTEGER DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- =====================================================
-- 5. 系统相关表
-- =====================================================

-- 通知表
CREATE TABLE IF NOT EXISTS notification (
                                            id SERIAL PRIMARY KEY,
                                            user_id INTEGER NOT NULL,
                                            user_type VARCHAR(20) NOT NULL,  -- student/teacher/admin
    title VARCHAR(200) NOT NULL,
    content TEXT,
    is_read BOOLEAN DEFAULT FALSE,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- 系统配置表
CREATE TABLE IF NOT EXISTS system_config (
                                             id SERIAL PRIMARY KEY,
                                             config_key VARCHAR(100) NOT NULL UNIQUE,
    config_value TEXT,
    description VARCHAR(500),
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- =====================================================
-- 6. 索引
-- =====================================================

-- 提交文件表索引
CREATE INDEX IF NOT EXISTS idx_submission_file_submission_id ON submission_file(submission_id);
CREATE INDEX IF NOT EXISTS idx_submission_file_file_name ON submission_file(file_name);

-- 提交记录索引
CREATE INDEX IF NOT EXISTS idx_submission_homework_id ON submission(homework_id);
CREATE INDEX IF NOT EXISTS idx_submission_student_id ON submission(student_id);
CREATE INDEX IF NOT EXISTS idx_submission_status ON submission(status);

-- 提交历史索引
CREATE INDEX IF NOT EXISTS idx_submission_history_submission_id ON submission_history(submission_id);

-- 评测结果索引
CREATE INDEX IF NOT EXISTS idx_evaluation_result_submission_id ON evaluation_result(submission_id);
CREATE INDEX IF NOT EXISTS idx_evaluation_result_test_case_id ON evaluation_result(test_case_id);

-- =====================================================
-- 7. 初始数据
-- =====================================================

-- 插入默认管理员
INSERT INTO admin (username, password, name, email)
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '系统管理员', 'admin@example.com')
    ON CONFLICT (username) DO NOTHING;

-- 插入系统配置
INSERT INTO system_config (config_key, config_value, description) VALUES
                                                                      ('max_file_size', '10485760', '最大文件大小（字节）'),
                                                                      ('max_file_count', '10', '最大文件数量'),
                                                                      ('execution_timeout', '30000', '代码执行超时时间（毫秒）')
    ON CONFLICT (config_key) DO NOTHING;