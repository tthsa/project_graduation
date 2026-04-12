package com.javaevaluation.controller;

import com.javaevaluation.common.ErrorCode;
import com.javaevaluation.common.Result;
import com.javaevaluation.dto.LoginRequest;
import com.javaevaluation.dto.LoginResponse;
import com.javaevaluation.entity.Admin;
import com.javaevaluation.entity.Student;
import com.javaevaluation.entity.Teacher;
import com.javaevaluation.mapper.AdminMapper;
import com.javaevaluation.mapper.StudentMapper;
import com.javaevaluation.mapper.TeacherMapper;
import com.javaevaluation.security.JwtUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AdminMapper adminMapper;
    private final TeacherMapper teacherMapper;
    private final StudentMapper studentMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        Integer userId;
        String username;
        String name;

        switch (request.getUserType()) {
            case "admin":
                Admin admin = adminMapper.findByUsername(request.getUsername());
                if (admin == null) {
                    return Result.fail(ErrorCode.LOGIN_FAILED);
                }
                if (!passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
                    return Result.fail(ErrorCode.LOGIN_FAILED);
                }
                userId = admin.getId();
                username = admin.getUsername();
                name = "管理员";
                break;

            case "teacher":
                Teacher teacher = teacherMapper.findByUsername(request.getUsername());
                if (teacher == null) {
                    return Result.fail(ErrorCode.LOGIN_FAILED);
                }
                if (!passwordEncoder.matches(request.getPassword(), teacher.getPassword())) {
                    return Result.fail(ErrorCode.LOGIN_FAILED);
                }
                userId = teacher.getId();
                username = teacher.getUsername();
                name = teacher.getName();
                break;

            case "student":
                Student student = studentMapper.findByStudentNo(request.getUsername());
                if (student == null) {
                    return Result.fail(ErrorCode.LOGIN_FAILED);
                }
                if (!passwordEncoder.matches(request.getPassword(), student.getPassword())) {
                    return Result.fail(ErrorCode.LOGIN_FAILED);
                }
                userId = student.getId();
                username = student.getStudentNo();
                name = student.getName();
                break;

            default:
                return Result.fail(ErrorCode.USER_TYPE_ERROR);
        }

        // 生成Token
        String token = jwtUtils.generateToken(userId, username, request.getUserType());

        LoginResponse response = LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(jwtExpiration / 1000)
                .userId(userId)
                .username(username)
                .userType(request.getUserType())
                .name(name)
                .build();

        return Result.success(response);
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    public Result<?> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");

        if (!jwtUtils.validateToken(token)) {
            return Result.fail(ErrorCode.TOKEN_INVALID);
        }

        Integer userId = jwtUtils.getUserIdFromToken(token);
        String userType = jwtUtils.getUserTypeFromToken(token);

        switch (userType) {
            case "admin":
                Admin admin = adminMapper.findById(userId);
                if (admin != null) {
                    return Result.success(admin);
                }
                break;
            case "teacher":
                Teacher teacher = teacherMapper.findById(userId);
                if (teacher != null) {
                    return Result.success(teacher);
                }
                break;
            case "student":
                Student student = studentMapper.findById(userId);
                if (student != null) {
                    return Result.success(student);
                }
                break;
        }

        return Result.fail(ErrorCode.USER_NOT_FOUND);
    }
}