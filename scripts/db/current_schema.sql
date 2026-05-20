--
-- PostgreSQL database dump
--

\restrict JUStb66b72c2mMVqoZWZLyBJkss0Q4iQJoQMIYcXQXMFcMquaZrf9ZxK3IPmL4D

-- Dumped from database version 15.17
-- Dumped by pg_dump version 15.17

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

--
-- Name: public; Type: SCHEMA; Schema: -; Owner: -
--

-- *not* creating schema, since initdb creates it


--
-- Name: SCHEMA public; Type: COMMENT; Schema: -; Owner: -
--

COMMENT ON SCHEMA public IS '';


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: admin; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.admin (
    id integer NOT NULL,
    username character varying(50) NOT NULL,
    password character varying(255) NOT NULL,
    name character varying(50),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone
);


--
-- Name: TABLE admin; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.admin IS '管理员表';


--
-- Name: COLUMN admin.username; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.admin.username IS '用户名';


--
-- Name: COLUMN admin.password; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.admin.password IS '密码';


--
-- Name: COLUMN admin.name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.admin.name IS '姓名';


--
-- Name: COLUMN admin.create_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.admin.create_time IS '创建时间';


--
-- Name: COLUMN admin.updated_at; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.admin.updated_at IS '更新时间';


--
-- Name: admin_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.admin_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: admin_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.admin_id_seq OWNED BY public.admin.id;


--
-- Name: class; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.class (
    id integer NOT NULL,
    name character varying(100) NOT NULL,
    teacher_id integer,
    description text,
    status integer DEFAULT 1,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone
);


--
-- Name: TABLE class; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.class IS '班级表';


--
-- Name: COLUMN class.name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.class.name IS '班级名称';


--
-- Name: COLUMN class.teacher_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.class.teacher_id IS '班主任ID';


--
-- Name: COLUMN class.description; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.class.description IS '班级描述';


--
-- Name: COLUMN class.status; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.class.status IS '状态：1-正常，0-禁用';


--
-- Name: COLUMN class.create_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.class.create_time IS '创建时间';


--
-- Name: COLUMN class.updated_at; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.class.updated_at IS '更新时间';


--
-- Name: class_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.class_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: class_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.class_id_seq OWNED BY public.class.id;


--
-- Name: course; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.course (
    id integer NOT NULL,
    name character varying(100) NOT NULL,
    teacher_id integer NOT NULL,
    class_id integer,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: TABLE course; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.course IS '课程表';


--
-- Name: COLUMN course.name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.course.name IS '课程名称';


--
-- Name: COLUMN course.teacher_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.course.teacher_id IS '教师ID';


--
-- Name: COLUMN course.class_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.course.class_id IS '班级ID';


--
-- Name: COLUMN course.create_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.course.create_time IS '创建时间';


--
-- Name: course_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.course_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: course_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.course_id_seq OWNED BY public.course.id;


--
-- Name: evaluation_result; Type: TABLE; Schema: public; Owner: -
--

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


--
-- Name: TABLE evaluation_result; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.evaluation_result IS '评测结果表';


--
-- Name: COLUMN evaluation_result.submission_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.evaluation_result.submission_id IS '提交ID';


--
-- Name: COLUMN evaluation_result.test_score; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.evaluation_result.test_score IS '测试得分';


--
-- Name: COLUMN evaluation_result.llm_score; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.evaluation_result.llm_score IS 'LLM评分';


--
-- Name: COLUMN evaluation_result.llm_review; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.evaluation_result.llm_review IS 'LLM评审意见';


--
-- Name: COLUMN evaluation_result.execution_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.evaluation_result.execution_time IS '执行时间(毫秒)';


--
-- Name: COLUMN evaluation_result.created_at; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.evaluation_result.created_at IS '创建时间';


--
-- Name: COLUMN evaluation_result.final_score; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.evaluation_result.final_score IS '综合分 (0-100), NULL=该提交在加列前评的, 未填';


--
-- Name: COLUMN evaluation_result.grade; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.evaluation_result.grade IS '等级 A/B/C/D, 由 final_score + homework 阈值映射, NULL=同上';


--
-- Name: COLUMN evaluation_result.llm_dimension_scores; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.evaluation_result.llm_dimension_scores IS '各 LLM 维度分 JSON, 形如 [{"name":"代码质量","score":8}], NULL=LLM 失败或单维度模式';


--
-- Name: evaluation_result_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.evaluation_result_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: evaluation_result_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.evaluation_result_id_seq OWNED BY public.evaluation_result.id;


--
-- Name: evaluation_task; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.evaluation_task (
    id integer NOT NULL,
    homework_id integer NOT NULL,
    name character varying(100),
    description text,
    status integer DEFAULT 0,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: TABLE evaluation_task; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.evaluation_task IS '任务表';


--
-- Name: COLUMN evaluation_task.homework_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.evaluation_task.homework_id IS '作业ID';


--
-- Name: COLUMN evaluation_task.name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.evaluation_task.name IS '任务名称';


--
-- Name: COLUMN evaluation_task.description; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.evaluation_task.description IS '任务描述';


--
-- Name: COLUMN evaluation_task.status; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.evaluation_task.status IS '状态：0-待处理，1-处理中，2-已完成';


--
-- Name: COLUMN evaluation_task.created_at; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.evaluation_task.created_at IS '创建时间';


--
-- Name: homework; Type: TABLE; Schema: public; Owner: -
--

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


--
-- Name: TABLE homework; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.homework IS '作业表';


--
-- Name: COLUMN homework.course_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.homework.course_id IS '课程ID';


--
-- Name: COLUMN homework.title; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.homework.title IS '作业标题';


--
-- Name: COLUMN homework.description; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.homework.description IS '作业描述';


--
-- Name: COLUMN homework.deadline; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.homework.deadline IS '截止时间';


--
-- Name: COLUMN homework.status; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.homework.status IS '状态：1-进行中，0-已结束';


--
-- Name: COLUMN homework.create_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.homework.create_time IS '创建时间';


--
-- Name: COLUMN homework.updated_at; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.homework.updated_at IS '更新时间';


--
-- Name: COLUMN homework.test_weight; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.homework.test_weight IS '测试得分权重 (0-100), 与 llm_weight 之和必须=100';


--
-- Name: COLUMN homework.llm_weight; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.homework.llm_weight IS 'LLM 评分权重 (0-100), 与 test_weight 之和必须=100';


--
-- Name: COLUMN homework.grade_a_threshold; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.homework.grade_a_threshold IS 'A 等级阈值, final_score>=此值为 A';


--
-- Name: COLUMN homework.grade_b_threshold; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.homework.grade_b_threshold IS 'B 等级阈值, A>B>C';


--
-- Name: COLUMN homework.grade_c_threshold; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.homework.grade_c_threshold IS 'C 等级阈值, 低于此值为 D';


--
-- Name: COLUMN homework.llm_dimensions; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.homework.llm_dimensions IS 'LLM 评分维度 JSON, NULL=默认单维度。形如 [{"name":"代码质量","weight":50},{"name":"可读性","weight":50}]';


--
-- Name: homework_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.homework_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: homework_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.homework_id_seq OWNED BY public.homework.id;


--
-- Name: llm_config; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.llm_config (
    id integer NOT NULL,
    name character varying(100) NOT NULL,
    api_key character varying(500),
    api_url character varying(500),
    model character varying(100),
    status integer DEFAULT 1,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: TABLE llm_config; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.llm_config IS 'LLM配置表';


--
-- Name: COLUMN llm_config.name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.llm_config.name IS '配置名称';


--
-- Name: COLUMN llm_config.api_key; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.llm_config.api_key IS 'API密钥';


--
-- Name: COLUMN llm_config.api_url; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.llm_config.api_url IS 'API地址';


--
-- Name: COLUMN llm_config.model; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.llm_config.model IS '模型名称';


--
-- Name: COLUMN llm_config.status; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.llm_config.status IS '状态：1-启用，0-禁用';


--
-- Name: COLUMN llm_config.create_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.llm_config.create_time IS '创建时间';


--
-- Name: llm_config_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.llm_config_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: llm_config_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.llm_config_id_seq OWNED BY public.llm_config.id;


--
-- Name: notification; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.notification (
    id integer NOT NULL,
    user_id integer NOT NULL,
    title character varying(200) NOT NULL,
    content text,
    is_read smallint DEFAULT 0,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: TABLE notification; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.notification IS '通知表';


--
-- Name: COLUMN notification.user_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.notification.user_id IS '用户ID';


--
-- Name: COLUMN notification.title; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.notification.title IS '标题';


--
-- Name: COLUMN notification.content; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.notification.content IS '内容';


--
-- Name: COLUMN notification.is_read; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.notification.is_read IS '是否已读：0-未读，1-已读';


--
-- Name: COLUMN notification.create_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.notification.create_time IS '创建时间';


--
-- Name: notification_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.notification_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: notification_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.notification_id_seq OWNED BY public.notification.id;


--
-- Name: student; Type: TABLE; Schema: public; Owner: -
--

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


--
-- Name: TABLE student; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.student IS '学生表';


--
-- Name: COLUMN student.student_no; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.student.student_no IS '学号（登录账号）';


--
-- Name: COLUMN student.password; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.student.password IS '密码';


--
-- Name: COLUMN student.name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.student.name IS '姓名';


--
-- Name: COLUMN student.email; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.student.email IS '邮箱';


--
-- Name: COLUMN student.class_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.student.class_id IS '班级ID';


--
-- Name: COLUMN student.status; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.student.status IS '状态：1-正常，0-禁用';


--
-- Name: COLUMN student.first_login; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.student.first_login IS '首次登录：1-是，0-否';


--
-- Name: COLUMN student.create_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.student.create_time IS '创建时间';


--
-- Name: COLUMN student.updated_at; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.student.updated_at IS '更新时间';


--
-- Name: student_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.student_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: student_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.student_id_seq OWNED BY public.student.id;


--
-- Name: submission; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.submission (
    id integer NOT NULL,
    homework_id integer NOT NULL,
    student_id integer NOT NULL,
    submit_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    status integer DEFAULT 0
);


--
-- Name: TABLE submission; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.submission IS '提交记录表';


--
-- Name: COLUMN submission.homework_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.submission.homework_id IS '作业ID';


--
-- Name: COLUMN submission.student_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.submission.student_id IS '学生ID';


--
-- Name: COLUMN submission.submit_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.submission.submit_time IS '提交时间';


--
-- Name: COLUMN submission.status; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.submission.status IS '状态：0-待评测，1-评测中，2-已完成，3-失败';


--
-- Name: submission_file; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.submission_file (
    id integer NOT NULL,
    submission_id integer NOT NULL,
    file_name character varying(200) NOT NULL,
    file_content text NOT NULL,
    file_order integer DEFAULT 0,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: submission_file_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.submission_file_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: submission_file_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.submission_file_id_seq OWNED BY public.submission_file.id;


--
-- Name: submission_history; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.submission_history (
    id integer NOT NULL,
    submission_id integer NOT NULL,
    submit_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: TABLE submission_history; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.submission_history IS '提交历史表';


--
-- Name: COLUMN submission_history.submission_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.submission_history.submission_id IS '提交ID';


--
-- Name: COLUMN submission_history.submit_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.submission_history.submit_time IS '提交时间';


--
-- Name: submission_history_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.submission_history_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: submission_history_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.submission_history_id_seq OWNED BY public.submission_history.id;


--
-- Name: submission_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.submission_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: submission_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.submission_id_seq OWNED BY public.submission.id;


--
-- Name: system_config; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.system_config (
    id integer NOT NULL,
    config_key character varying(100) NOT NULL,
    config_value text,
    description character varying(500),
    updated_at timestamp without time zone
);


--
-- Name: TABLE system_config; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.system_config IS '系统配置表';


--
-- Name: COLUMN system_config.config_key; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.system_config.config_key IS '配置键';


--
-- Name: COLUMN system_config.config_value; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.system_config.config_value IS '配置值';


--
-- Name: COLUMN system_config.description; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.system_config.description IS '配置描述';


--
-- Name: COLUMN system_config.updated_at; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.system_config.updated_at IS '更新时间';


--
-- Name: system_config_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.system_config_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: system_config_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.system_config_id_seq OWNED BY public.system_config.id;


--
-- Name: task_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.task_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: task_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.task_id_seq OWNED BY public.evaluation_task.id;


--
-- Name: teacher; Type: TABLE; Schema: public; Owner: -
--

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


--
-- Name: TABLE teacher; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.teacher IS '教师表';


--
-- Name: COLUMN teacher.teacher_no; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.teacher.teacher_no IS '工号（登录账号）';


--
-- Name: COLUMN teacher.password; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.teacher.password IS '密码';


--
-- Name: COLUMN teacher.name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.teacher.name IS '姓名';


--
-- Name: COLUMN teacher.email; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.teacher.email IS '邮箱';


--
-- Name: COLUMN teacher.phone; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.teacher.phone IS '联系电话';


--
-- Name: COLUMN teacher.status; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.teacher.status IS '状态：1-正常，0-禁用';


--
-- Name: COLUMN teacher.create_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.teacher.create_time IS '创建时间';


--
-- Name: COLUMN teacher.updated_at; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.teacher.updated_at IS '更新时间';


--
-- Name: teacher_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.teacher_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: teacher_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.teacher_id_seq OWNED BY public.teacher.id;


--
-- Name: test_case; Type: TABLE; Schema: public; Owner: -
--

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


--
-- Name: TABLE test_case; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.test_case IS '测试用例表';


--
-- Name: COLUMN test_case.homework_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.test_case.homework_id IS '作业ID';


--
-- Name: COLUMN test_case.name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.test_case.name IS '测试用例名称';


--
-- Name: COLUMN test_case.input; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.test_case.input IS '输入数据';


--
-- Name: COLUMN test_case.expected_output; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.test_case.expected_output IS '预期输出';


--
-- Name: COLUMN test_case.is_public; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.test_case.is_public IS '是否公开：1-是，0-否';


--
-- Name: COLUMN test_case.sort_order; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.test_case.sort_order IS '排序顺序';


--
-- Name: COLUMN test_case.create_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.test_case.create_time IS '创建时间';


--
-- Name: test_case_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.test_case_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: test_case_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.test_case_id_seq OWNED BY public.test_case.id;


--
-- Name: admin id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.admin ALTER COLUMN id SET DEFAULT nextval('public.admin_id_seq'::regclass);


--
-- Name: class id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.class ALTER COLUMN id SET DEFAULT nextval('public.class_id_seq'::regclass);


--
-- Name: course id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.course ALTER COLUMN id SET DEFAULT nextval('public.course_id_seq'::regclass);


--
-- Name: evaluation_result id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.evaluation_result ALTER COLUMN id SET DEFAULT nextval('public.evaluation_result_id_seq'::regclass);


--
-- Name: evaluation_task id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.evaluation_task ALTER COLUMN id SET DEFAULT nextval('public.task_id_seq'::regclass);


--
-- Name: homework id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.homework ALTER COLUMN id SET DEFAULT nextval('public.homework_id_seq'::regclass);


--
-- Name: llm_config id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.llm_config ALTER COLUMN id SET DEFAULT nextval('public.llm_config_id_seq'::regclass);


--
-- Name: notification id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.notification ALTER COLUMN id SET DEFAULT nextval('public.notification_id_seq'::regclass);


--
-- Name: student id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.student ALTER COLUMN id SET DEFAULT nextval('public.student_id_seq'::regclass);


--
-- Name: submission id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.submission ALTER COLUMN id SET DEFAULT nextval('public.submission_id_seq'::regclass);


--
-- Name: submission_file id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.submission_file ALTER COLUMN id SET DEFAULT nextval('public.submission_file_id_seq'::regclass);


--
-- Name: submission_history id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.submission_history ALTER COLUMN id SET DEFAULT nextval('public.submission_history_id_seq'::regclass);


--
-- Name: system_config id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.system_config ALTER COLUMN id SET DEFAULT nextval('public.system_config_id_seq'::regclass);


--
-- Name: teacher id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.teacher ALTER COLUMN id SET DEFAULT nextval('public.teacher_id_seq'::regclass);


--
-- Name: test_case id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.test_case ALTER COLUMN id SET DEFAULT nextval('public.test_case_id_seq'::regclass);


--
-- Name: admin admin_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.admin
    ADD CONSTRAINT admin_pkey PRIMARY KEY (id);


--
-- Name: admin admin_username_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.admin
    ADD CONSTRAINT admin_username_key UNIQUE (username);


--
-- Name: class class_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.class
    ADD CONSTRAINT class_pkey PRIMARY KEY (id);


--
-- Name: course course_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.course
    ADD CONSTRAINT course_pkey PRIMARY KEY (id);


--
-- Name: evaluation_result evaluation_result_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.evaluation_result
    ADD CONSTRAINT evaluation_result_pkey PRIMARY KEY (id);


--
-- Name: homework homework_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.homework
    ADD CONSTRAINT homework_pkey PRIMARY KEY (id);


--
-- Name: llm_config llm_config_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.llm_config
    ADD CONSTRAINT llm_config_pkey PRIMARY KEY (id);


--
-- Name: notification notification_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.notification
    ADD CONSTRAINT notification_pkey PRIMARY KEY (id);


--
-- Name: student student_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.student
    ADD CONSTRAINT student_pkey PRIMARY KEY (id);


--
-- Name: student student_student_no_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.student
    ADD CONSTRAINT student_student_no_key UNIQUE (student_no);


--
-- Name: submission_file submission_file_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.submission_file
    ADD CONSTRAINT submission_file_pkey PRIMARY KEY (id);


--
-- Name: submission_history submission_history_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.submission_history
    ADD CONSTRAINT submission_history_pkey PRIMARY KEY (id);


--
-- Name: submission submission_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.submission
    ADD CONSTRAINT submission_pkey PRIMARY KEY (id);


--
-- Name: system_config system_config_config_key_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.system_config
    ADD CONSTRAINT system_config_config_key_key UNIQUE (config_key);


--
-- Name: system_config system_config_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.system_config
    ADD CONSTRAINT system_config_pkey PRIMARY KEY (id);


--
-- Name: evaluation_task task_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.evaluation_task
    ADD CONSTRAINT task_pkey PRIMARY KEY (id);


--
-- Name: teacher teacher_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.teacher
    ADD CONSTRAINT teacher_pkey PRIMARY KEY (id);


--
-- Name: teacher teacher_teacher_no_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.teacher
    ADD CONSTRAINT teacher_teacher_no_key UNIQUE (teacher_no);


--
-- Name: test_case test_case_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.test_case
    ADD CONSTRAINT test_case_pkey PRIMARY KEY (id);


--
-- Name: idx_course_class_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_course_class_id ON public.course USING btree (class_id);


--
-- Name: idx_course_teacher_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_course_teacher_id ON public.course USING btree (teacher_id);


--
-- Name: idx_evaluation_result_submission_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_evaluation_result_submission_id ON public.evaluation_result USING btree (submission_id);


--
-- Name: idx_homework_course_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_homework_course_id ON public.homework USING btree (course_id);


--
-- Name: idx_notification_user_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_notification_user_id ON public.notification USING btree (user_id);


--
-- Name: idx_student_class_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_student_class_id ON public.student USING btree (class_id);


--
-- Name: idx_student_no; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_student_no ON public.student USING btree (student_no);


--
-- Name: idx_submission_file_submission_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_submission_file_submission_id ON public.submission_file USING btree (submission_id);


--
-- Name: idx_submission_history_submission_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_submission_history_submission_id ON public.submission_history USING btree (submission_id);


--
-- Name: idx_submission_homework_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_submission_homework_id ON public.submission USING btree (homework_id);


--
-- Name: idx_submission_student_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_submission_student_id ON public.submission USING btree (student_id);


--
-- Name: idx_task_homework_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_task_homework_id ON public.evaluation_task USING btree (homework_id);


--
-- Name: idx_teacher_no; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_teacher_no ON public.teacher USING btree (teacher_no);


--
-- Name: idx_test_case_homework_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_test_case_homework_id ON public.test_case USING btree (homework_id);


--
-- Name: class class_teacher_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.class
    ADD CONSTRAINT class_teacher_id_fkey FOREIGN KEY (teacher_id) REFERENCES public.teacher(id);


--
-- Name: course course_class_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.course
    ADD CONSTRAINT course_class_id_fkey FOREIGN KEY (class_id) REFERENCES public.class(id);


--
-- Name: course course_teacher_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.course
    ADD CONSTRAINT course_teacher_id_fkey FOREIGN KEY (teacher_id) REFERENCES public.teacher(id);


--
-- Name: evaluation_result evaluation_result_submission_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.evaluation_result
    ADD CONSTRAINT evaluation_result_submission_id_fkey FOREIGN KEY (submission_id) REFERENCES public.submission(id);


--
-- Name: homework homework_course_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.homework
    ADD CONSTRAINT homework_course_id_fkey FOREIGN KEY (course_id) REFERENCES public.course(id);


--
-- Name: student student_class_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.student
    ADD CONSTRAINT student_class_id_fkey FOREIGN KEY (class_id) REFERENCES public.class(id);


--
-- Name: submission_file submission_file_submission_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.submission_file
    ADD CONSTRAINT submission_file_submission_id_fkey FOREIGN KEY (submission_id) REFERENCES public.submission(id) ON DELETE CASCADE;


--
-- Name: submission_history submission_history_submission_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.submission_history
    ADD CONSTRAINT submission_history_submission_id_fkey FOREIGN KEY (submission_id) REFERENCES public.submission(id);


--
-- Name: submission submission_homework_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.submission
    ADD CONSTRAINT submission_homework_id_fkey FOREIGN KEY (homework_id) REFERENCES public.homework(id);


--
-- Name: submission submission_student_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.submission
    ADD CONSTRAINT submission_student_id_fkey FOREIGN KEY (student_id) REFERENCES public.student(id);


--
-- Name: evaluation_task task_homework_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.evaluation_task
    ADD CONSTRAINT task_homework_id_fkey FOREIGN KEY (homework_id) REFERENCES public.homework(id);


--
-- Name: test_case test_case_homework_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.test_case
    ADD CONSTRAINT test_case_homework_id_fkey FOREIGN KEY (homework_id) REFERENCES public.homework(id);


--
-- PostgreSQL database dump complete
--

\unrestrict JUStb66b72c2mMVqoZWZLyBJkss0Q4iQJoQMIYcXQXMFcMquaZrf9ZxK3IPmL4D

