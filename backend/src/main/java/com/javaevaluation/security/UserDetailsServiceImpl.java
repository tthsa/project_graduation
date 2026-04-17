package com.javaevaluation.security;

import com.javaevaluation.entity.Admin;
import com.javaevaluation.entity.Student;
import com.javaevaluation.entity.Teacher;
import com.javaevaluation.mapper.AdminMapper;
import com.javaevaluation.mapper.StudentMapper;
import com.javaevaluation.mapper.TeacherMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * 用户详情服务实现
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final AdminMapper adminMapper;
    private final TeacherMapper teacherMapper;
    private final StudentMapper studentMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 用户名格式: userType:username (如 admin:admin, teacher:zhangsan, student:2024001)
        String[] parts = username.split(":", 2);
        if (parts.length != 2) {
            throw new UsernameNotFoundException("用户名格式错误，应为 userType:username");
        }

        String userType = parts[0];
        String name = parts[1];

        switch (userType) {
            case "admin":
                Admin admin = adminMapper.findByUsername(name);
                if (admin != null) {
                    return User.builder()
                            .username("admin:" + admin.getUsername())
                            .password(admin.getPassword())
                            .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")))
                            .build();
                }
                break;

            case "teacher":
                Teacher teacher = teacherMapper.findByTeacherNo(name);
                if (teacher != null) {
                    return User.builder()
                            .username("teacher:" + teacher.getTeacherNo())
                            .password(teacher.getPassword())
                            .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_TEACHER")))
                            .build();
                }
                break;

            case "student":
                Student student = studentMapper.findByStudentNo(name);
                if (student != null) {
                    return User.builder()
                            .username("student:" + student.getStudentNo())
                            .password(student.getPassword())
                            .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_STUDENT")))
                            .build();
                }
                break;

            default:
                throw new UsernameNotFoundException("未知的用户类型: " + userType);
        }

        throw new UsernameNotFoundException("用户不存在: " + username);
    }
}