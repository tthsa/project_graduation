package com.javaevaluation.config;

import com.javaevaluation.entity.*;
import com.javaevaluation.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 数据初始化器：应用启动时检查并插入基础测试数据
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final AdminMapper adminMapper;
    private final TeacherMapper teacherMapper;
    private final StudentMapper studentMapper;
    private final ClassInfoMapper classInfoMapper;
    private final CourseMapper courseMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        initAdmin();
        initTeacher();
        initClassInfo();
        initStudent();
        initCourse();
    }

    private void initAdmin() {
        if (adminMapper.selectCount(null) == 0L) {
            Admin admin = new Admin();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("123456"));
            admin.setName("管理员");
            adminMapper.insert(admin);
            log.info("初始化管理员: admin");
        }
    }

    private void initTeacher() {
        if (teacherMapper.selectCount(null) == 0L) {
            Teacher teacher = new Teacher();
            teacher.setTeacherNo("T001");
            teacher.setPassword(passwordEncoder.encode("123456"));
            teacher.setName("测试教师");
            teacher.setEmail("teacher@test.com");
            teacher.setStatus(1);
            teacherMapper.insert(teacher);
            log.info("初始化教师: T001");
        }
    }

    private void initClassInfo() {
        if (classInfoMapper.selectCount(null) == 0L) {
            Teacher teacher = teacherMapper.findByTeacherNo("T001");
            if (teacher == null) {
                log.warn("无法初始化班级：教师 T001 不存在");
                return;
            }

            ClassInfo classInfo = new ClassInfo();
            classInfo.setName("软件工程1班");
            classInfo.setTeacherId(teacher.getId());
            classInfo.setDescription("E2E测试用班级");
            classInfo.setStatus(1);
            classInfoMapper.insert(classInfo);
            log.info("初始化班级: id={}", classInfo.getId());
        }
    }

    private void initStudent() {
        if (studentMapper.selectCount(null) == 0L) {
            ClassInfo classInfo = classInfoMapper.selectList(null).stream().findFirst().orElse(null);
            if (classInfo == null) {
                log.warn("无法初始化学生：班级不存在");
                return;
            }

            Student student = new Student();
            student.setStudentNo("2024001");
            student.setPassword(passwordEncoder.encode("123456"));
            student.setName("测试学生");
            student.setEmail("student@test.com");
            student.setClassId(classInfo.getId());
            student.setStatus(1);
            student.setFirstLogin(1);
            studentMapper.insert(student);
            log.info("初始化学生: 2024001");
        }
    }

    private void initCourse() {
        if (courseMapper.selectCount(null) == 0L) {
            Teacher teacher = teacherMapper.findByTeacherNo("T001");
            ClassInfo classInfo = classInfoMapper.selectList(null).stream().findFirst().orElse(null);
            if (teacher == null || classInfo == null) {
                log.warn("无法初始化课程：教师或班级不存在");
                return;
            }

            Course course = new Course();
            course.setName("Java程序设计");
            course.setTeacherId(teacher.getId());
            course.setClassId(classInfo.getId());
            courseMapper.insert(course);
            log.info("初始化课程: Java程序设计");
        }
    }
}
