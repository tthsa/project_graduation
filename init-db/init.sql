--
-- PostgreSQL database dump
--

-- Dumped from database version 15.18
-- Dumped by pg_dump version 15.18

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';
SET default_table_access_method = heap;

CREATE TABLE public.admin (
    id integer NOT NULL,
    username character varying(50) NOT NULL,
    password character varying(255) NOT NULL,
    name character varying(50),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone
);

COMMENT ON TABLE public.admin IS '管理员表';
COMMENT ON COLUMN public.admin.username IS '用户名';
COMMENT ON COLUMN public.admin.password IS '密码';
COMMENT ON COLUMN public.admin.name IS '姓名';
COMMENT ON COLUMN public.admin.create_time IS '创建时间';
COMMENT ON COLUMN public.admin.updated_at IS '更新时间';

CREATE SEQUENCE public.admin_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.admin_id_seq OWNED BY public.admin.id;

CREATE TABLE public.class_info (
    id integer NOT NULL,
    name character varying(100) NOT NULL,
    teacher_id integer,
    description text,
    status integer DEFAULT 1,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone
);

COMMENT ON TABLE public.class_info IS '班级表';
COMMENT ON COLUMN public.class_info.name IS '班级名称';
COMMENT ON COLUMN public.class_info.teacher_id IS '班主任ID';
COMMENT ON COLUMN public.class_info.description IS '班级描述';
COMMENT ON COLUMN public.class_info.status IS '状态：1-正常，0-禁用';
COMMENT ON COLUMN public.class_info.create_time IS '创建时间';
COMMENT ON COLUMN public.class_info.updated_at IS '更新时间';

CREATE SEQUENCE public.class_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.class_id_seq OWNED BY public.class_info.id;

CREATE TABLE public.course (
    id integer NOT NULL,
    name character varying(100) NOT NULL,
    teacher_id integer NOT NULL,
    class_id integer,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone
);

COMMENT ON TABLE public.course IS '课程表';
COMMENT ON COLUMN public.course.name IS '课程名称';
COMMENT ON COLUMN public.course.teacher_id IS '教师ID';
COMMENT ON COLUMN public.course.class_id IS '班级ID';
COMMENT ON COLUMN public.course.create_time IS '创建时间';

CREATE SEQUENCE public.course_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.course_id_seq OWNED BY public.course.id;

CREATE TABLE public.evaluation_result (
    id integer NOT NULL,
    submission_id integer NOT NULL,
    test_score integer DEFAULT 0,
    llm_score integer DEFAULT 0,
    llm_review text,
    execution_time bigint DEFAULT 0,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    final_score integer,
    grade character varying(2) DEFAULT NULL::character varying,
    llm_dimension_scores text
);

COMMENT ON TABLE public.evaluation_result IS '评测结果表';
COMMENT ON COLUMN public.evaluation_result.submission_id IS '提交ID';
COMMENT ON COLUMN public.evaluation_result.test_score IS '测试得分';
COMMENT ON COLUMN public.evaluation_result.llm_score IS 'LLM评分';
COMMENT ON COLUMN public.evaluation_result.llm_review IS 'LLM评审意见';
COMMENT ON COLUMN public.evaluation_result.execution_time IS '执行时间(毫秒)';
COMMENT ON COLUMN public.evaluation_result.created_at IS '创建时间';
COMMENT ON COLUMN public.evaluation_result.final_score IS '综合分 (0-100), NULL=该提交在加列前评的, 未填';
COMMENT ON COLUMN public.evaluation_result.grade IS '等级 A/B/C/D, 由 final_score + homework 阈值映射, NULL=同上';
COMMENT ON COLUMN public.evaluation_result.llm_dimension_scores IS '各 LLM 维度分 JSON, 形如 [{"name":"代码质量","score":8}], NULL=LLM 失败或单维度模式';

CREATE SEQUENCE public.evaluation_result_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.evaluation_result_id_seq OWNED BY public.evaluation_result.id;

CREATE TABLE public.evaluation_task (
    id integer NOT NULL,
    homework_id integer NOT NULL,
    name character varying(100),
    description text,
    status integer DEFAULT 0,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE public.evaluation_task IS '任务表';
COMMENT ON COLUMN public.evaluation_task.homework_id IS '作业ID';
COMMENT ON COLUMN public.evaluation_task.name IS '任务名称';
COMMENT ON COLUMN public.evaluation_task.description IS '任务描述';
COMMENT ON COLUMN public.evaluation_task.status IS '状态：0-待处理，1-处理中，2-已完成';
COMMENT ON COLUMN public.evaluation_task.created_at IS '创建时间';

CREATE TABLE public.homework (
    id integer NOT NULL,
    course_id integer NOT NULL,
    title character varying(200) NOT NULL,
    description text,
    deadline timestamp without time zone,
    status integer DEFAULT 1,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone,
    test_weight integer DEFAULT 70,
    llm_weight integer DEFAULT 30,
    grade_a_threshold integer DEFAULT 90,
    grade_b_threshold integer DEFAULT 75,
    grade_c_threshold integer DEFAULT 60,
    llm_dimensions text
);

COMMENT ON TABLE public.homework IS '作业表';
COMMENT ON COLUMN public.homework.course_id IS '课程ID';
COMMENT ON COLUMN public.homework.title IS '作业标题';
COMMENT ON COLUMN public.homework.description IS '作业描述';
COMMENT ON COLUMN public.homework.deadline IS '截止时间';
COMMENT ON COLUMN public.homework.status IS '状态：1-进行中，0-已结束';
COMMENT ON COLUMN public.homework.create_time IS '创建时间';
COMMENT ON COLUMN public.homework.updated_at IS '更新时间';
COMMENT ON COLUMN public.homework.test_weight IS '测试得分权重 (0-100), 与 llm_weight 之和必须=100';
COMMENT ON COLUMN public.homework.llm_weight IS 'LLM 评分权重 (0-100), 与 test_weight 之和必须=100';
COMMENT ON COLUMN public.homework.grade_a_threshold IS 'A 等级阈值, final_score>=此值为 A';
COMMENT ON COLUMN public.homework.grade_b_threshold IS 'B 等级阈值, A>B>C';
COMMENT ON COLUMN public.homework.grade_c_threshold IS 'C 等级阈值, 低于此值为 D';
COMMENT ON COLUMN public.homework.llm_dimensions IS 'LLM 评分维度 JSON, NULL=默认单维度。形如 [{"name":"代码质量","weight":50},{"name":"可读性","weight":50}]';

CREATE SEQUENCE public.homework_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.homework_id_seq OWNED BY public.homework.id;

CREATE TABLE public.llm_config (
    id integer NOT NULL,
    name character varying(100) NOT NULL,
    api_key character varying(500),
    api_url character varying(500),
    model character varying(100),
    status integer DEFAULT 1,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE public.llm_config IS 'LLM配置表';
COMMENT ON COLUMN public.llm_config.name IS '配置名称';
COMMENT ON COLUMN public.llm_config.api_key IS 'API密钥';
COMMENT ON COLUMN public.llm_config.api_url IS 'API地址';
COMMENT ON COLUMN public.llm_config.model IS '模型名称';
COMMENT ON COLUMN public.llm_config.status IS '状态：1-启用，0-禁用';
COMMENT ON COLUMN public.llm_config.create_time IS '创建时间';

CREATE SEQUENCE public.llm_config_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.llm_config_id_seq OWNED BY public.llm_config.id;

CREATE TABLE public.notification (
    id integer NOT NULL,
    user_id integer NOT NULL,
    title character varying(200) NOT NULL,
    content text,
    is_read smallint DEFAULT 0,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE public.notification IS '通知表';
COMMENT ON COLUMN public.notification.user_id IS '用户ID';
COMMENT ON COLUMN public.notification.title IS '标题';
COMMENT ON COLUMN public.notification.content IS '内容';
COMMENT ON COLUMN public.notification.is_read IS '是否已读：0-未读，1-已读';
COMMENT ON COLUMN public.notification.create_time IS '创建时间';

CREATE SEQUENCE public.notification_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.notification_id_seq OWNED BY public.notification.id;

CREATE TABLE public.student (
    id integer NOT NULL,
    student_no character varying(50) NOT NULL,
    password character varying(255) NOT NULL,
    name character varying(50),
    email character varying(100),
    class_id integer,
    status integer DEFAULT 1,
    first_login integer DEFAULT 1,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone
);

COMMENT ON TABLE public.student IS '学生表';
COMMENT ON COLUMN public.student.student_no IS '学号（登录账号）';
COMMENT ON COLUMN public.student.password IS '密码';
COMMENT ON COLUMN public.student.name IS '姓名';
COMMENT ON COLUMN public.student.email IS '邮箱';
COMMENT ON COLUMN public.student.class_id IS '班级ID';
COMMENT ON COLUMN public.student.status IS '状态：1-正常，0-禁用';
COMMENT ON COLUMN public.student.first_login IS '首次登录：1-是，0-否';
COMMENT ON COLUMN public.student.create_time IS '创建时间';
COMMENT ON COLUMN public.student.updated_at IS '更新时间';

CREATE SEQUENCE public.student_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.student_id_seq OWNED BY public.student.id;

CREATE TABLE public.submission (
    id integer NOT NULL,
    homework_id integer NOT NULL,
    student_id integer NOT NULL,
    submit_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    status integer DEFAULT 0
);

COMMENT ON TABLE public.submission IS '提交记录表';
COMMENT ON COLUMN public.submission.homework_id IS '作业ID';
COMMENT ON COLUMN public.submission.student_id IS '学生ID';
COMMENT ON COLUMN public.submission.submit_time IS '提交时间';
COMMENT ON COLUMN public.submission.status IS '状态：0-待评测，1-评测中，2-已完成，3-失败';

CREATE SEQUENCE public.submission_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.submission_id_seq OWNED BY public.submission.id;

CREATE TABLE public.submission_file (
    id integer NOT NULL,
    submission_id integer NOT NULL,
    file_name character varying(200) NOT NULL,
    file_content text NOT NULL,
    file_order integer DEFAULT 0,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);

CREATE SEQUENCE public.submission_file_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.submission_file_id_seq OWNED BY public.submission_file.id;

CREATE TABLE public.submission_history (
    id integer NOT NULL,
    submission_id integer NOT NULL,
    submit_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE public.submission_history IS '提交历史表';
COMMENT ON COLUMN public.submission_history.submission_id IS '提交ID';
COMMENT ON COLUMN public.submission_history.submit_time IS '提交时间';

CREATE SEQUENCE public.submission_history_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.submission_history_id_seq OWNED BY public.submission_history.id;

CREATE TABLE public.system_config (
    id integer NOT NULL,
    config_key character varying(100) NOT NULL,
    config_value text,
    description character varying(500),
    updated_at timestamp without time zone
);

COMMENT ON TABLE public.system_config IS '系统配置表';
COMMENT ON COLUMN public.system_config.config_key IS '配置键';
COMMENT ON COLUMN public.system_config.config_value IS '配置值';
COMMENT ON COLUMN public.system_config.description IS '配置描述';
COMMENT ON COLUMN public.system_config.updated_at IS '更新时间';

CREATE SEQUENCE public.system_config_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.system_config_id_seq OWNED BY public.system_config.id;

CREATE SEQUENCE public.task_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.task_id_seq OWNED BY public.evaluation_task.id;

CREATE TABLE public.teacher (
    id integer NOT NULL,
    teacher_no character varying(50) NOT NULL,
    password character varying(255) NOT NULL,
    name character varying(50),
    email character varying(100),
    phone character varying(20),
    status integer DEFAULT 1,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone
);

COMMENT ON TABLE public.teacher IS '教师表';
COMMENT ON COLUMN public.teacher.teacher_no IS '工号（登录账号）';
COMMENT ON COLUMN public.teacher.password IS '密码';
COMMENT ON COLUMN public.teacher.name IS '姓名';
COMMENT ON COLUMN public.teacher.email IS '邮箱';
COMMENT ON COLUMN public.teacher.phone IS '联系电话';
COMMENT ON COLUMN public.teacher.status IS '状态：1-正常，0-禁用';
COMMENT ON COLUMN public.teacher.create_time IS '创建时间';
COMMENT ON COLUMN public.teacher.updated_at IS '更新时间';

CREATE SEQUENCE public.teacher_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.teacher_id_seq OWNED BY public.teacher.id;

CREATE TABLE public.test_case (
    id integer NOT NULL,
    homework_id integer NOT NULL,
    name character varying(100),
    input text,
    expected_output text,
    is_public integer DEFAULT 0,
    sort_order integer DEFAULT 0,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE public.test_case IS '测试用例表';
COMMENT ON COLUMN public.test_case.homework_id IS '作业ID';
COMMENT ON COLUMN public.test_case.name IS '测试用例名称';
COMMENT ON COLUMN public.test_case.input IS '输入数据';
COMMENT ON COLUMN public.test_case.expected_output IS '预期输出';
COMMENT ON COLUMN public.test_case.is_public IS '是否公开：1-是，0-否';
COMMENT ON COLUMN public.test_case.sort_order IS '排序顺序';
COMMENT ON COLUMN public.test_case.create_time IS '创建时间';

CREATE SEQUENCE public.test_case_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.test_case_id_seq OWNED BY public.test_case.id;

ALTER TABLE ONLY public.admin ALTER COLUMN id SET DEFAULT nextval('public.admin_id_seq'::regclass);
ALTER TABLE ONLY public.class_info ALTER COLUMN id SET DEFAULT nextval('public.class_id_seq'::regclass);
ALTER TABLE ONLY public.course ALTER COLUMN id SET DEFAULT nextval('public.course_id_seq'::regclass);
ALTER TABLE ONLY public.evaluation_result ALTER COLUMN id SET DEFAULT nextval('public.evaluation_result_id_seq'::regclass);
ALTER TABLE ONLY public.evaluation_task ALTER COLUMN id SET DEFAULT nextval('public.task_id_seq'::regclass);
ALTER TABLE ONLY public.homework ALTER COLUMN id SET DEFAULT nextval('public.homework_id_seq'::regclass);
ALTER TABLE ONLY public.llm_config ALTER COLUMN id SET DEFAULT nextval('public.llm_config_id_seq'::regclass);
ALTER TABLE ONLY public.notification ALTER COLUMN id SET DEFAULT nextval('public.notification_id_seq'::regclass);
ALTER TABLE ONLY public.student ALTER COLUMN id SET DEFAULT nextval('public.student_id_seq'::regclass);
ALTER TABLE ONLY public.submission ALTER COLUMN id SET DEFAULT nextval('public.submission_id_seq'::regclass);
ALTER TABLE ONLY public.submission_file ALTER COLUMN id SET DEFAULT nextval('public.submission_file_id_seq'::regclass);
ALTER TABLE ONLY public.submission_history ALTER COLUMN id SET DEFAULT nextval('public.submission_history_id_seq'::regclass);
ALTER TABLE ONLY public.system_config ALTER COLUMN id SET DEFAULT nextval('public.system_config_id_seq'::regclass);
ALTER TABLE ONLY public.teacher ALTER COLUMN id SET DEFAULT nextval('public.teacher_id_seq'::regclass);
ALTER TABLE ONLY public.test_case ALTER COLUMN id SET DEFAULT nextval('public.test_case_id_seq'::regclass);

ALTER TABLE ONLY public.admin ADD CONSTRAINT admin_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.admin ADD CONSTRAINT admin_username_key UNIQUE (username);
ALTER TABLE ONLY public.class_info ADD CONSTRAINT class_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.course ADD CONSTRAINT course_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.evaluation_result ADD CONSTRAINT evaluation_result_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.homework ADD CONSTRAINT homework_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.llm_config ADD CONSTRAINT llm_config_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.notification ADD CONSTRAINT notification_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.student ADD CONSTRAINT student_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.student ADD CONSTRAINT student_student_no_key UNIQUE (student_no);
ALTER TABLE ONLY public.submission_file ADD CONSTRAINT submission_file_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.submission_history ADD CONSTRAINT submission_history_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.submission ADD CONSTRAINT submission_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.system_config ADD CONSTRAINT system_config_config_key_key UNIQUE (config_key);
ALTER TABLE ONLY public.system_config ADD CONSTRAINT system_config_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.evaluation_task ADD CONSTRAINT task_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.teacher ADD CONSTRAINT teacher_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.teacher ADD CONSTRAINT teacher_teacher_no_key UNIQUE (teacher_no);
ALTER TABLE ONLY public.test_case ADD CONSTRAINT test_case_pkey PRIMARY KEY (id);

CREATE INDEX idx_course_class_id ON public.course USING btree (class_id);
CREATE INDEX idx_course_teacher_id ON public.course USING btree (teacher_id);
CREATE INDEX idx_evaluation_result_submission_id ON public.evaluation_result USING btree (submission_id);
CREATE INDEX idx_homework_course_id ON public.homework USING btree (course_id);
CREATE INDEX idx_notification_user_id ON public.notification USING btree (user_id);
CREATE INDEX idx_student_class_id ON public.student USING btree (class_id);
CREATE INDEX idx_student_no ON public.student USING btree (student_no);
CREATE INDEX idx_submission_file_submission_id ON public.submission_file USING btree (submission_id);
CREATE INDEX idx_submission_history_submission_id ON public.submission_history USING btree (submission_id);
CREATE INDEX idx_submission_homework_id ON public.submission USING btree (homework_id);
CREATE INDEX idx_submission_student_id ON public.submission USING btree (student_id);
CREATE INDEX idx_task_homework_id ON public.evaluation_task USING btree (homework_id);
CREATE INDEX idx_teacher_no ON public.teacher USING btree (teacher_no);
CREATE INDEX idx_test_case_homework_id ON public.test_case USING btree (homework_id);

ALTER TABLE ONLY public.class_info ADD CONSTRAINT class_teacher_id_fkey FOREIGN KEY (teacher_id) REFERENCES public.teacher(id);
ALTER TABLE ONLY public.course ADD CONSTRAINT course_class_id_fkey FOREIGN KEY (class_id) REFERENCES public.class_info(id);
ALTER TABLE ONLY public.course ADD CONSTRAINT course_teacher_id_fkey FOREIGN KEY (teacher_id) REFERENCES public.teacher(id);
ALTER TABLE ONLY public.evaluation_result ADD CONSTRAINT evaluation_result_submission_id_fkey FOREIGN KEY (submission_id) REFERENCES public.submission(id);
ALTER TABLE ONLY public.homework ADD CONSTRAINT homework_course_id_fkey FOREIGN KEY (course_id) REFERENCES public.course(id);
ALTER TABLE ONLY public.student ADD CONSTRAINT student_class_id_fkey FOREIGN KEY (class_id) REFERENCES public.class_info(id);
ALTER TABLE ONLY public.submission_file ADD CONSTRAINT submission_file_submission_id_fkey FOREIGN KEY (submission_id) REFERENCES public.submission(id) ON DELETE CASCADE;
ALTER TABLE ONLY public.submission_history ADD CONSTRAINT submission_history_submission_id_fkey FOREIGN KEY (submission_id) REFERENCES public.submission(id);
ALTER TABLE ONLY public.submission ADD CONSTRAINT submission_homework_id_fkey FOREIGN KEY (homework_id) REFERENCES public.homework(id);
ALTER TABLE ONLY public.submission ADD CONSTRAINT submission_student_id_fkey FOREIGN KEY (student_id) REFERENCES public.student(id);
ALTER TABLE ONLY public.evaluation_task ADD CONSTRAINT task_homework_id_fkey FOREIGN KEY (homework_id) REFERENCES public.homework(id);
ALTER TABLE ONLY public.test_case ADD CONSTRAINT test_case_homework_id_fkey FOREIGN KEY (homework_id) REFERENCES public.homework(id);
