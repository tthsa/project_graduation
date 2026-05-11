package com.javaevaluation.controller;

import com.javaevaluation.common.ErrorCode;
import com.javaevaluation.common.Result;
import com.javaevaluation.entity.Course;
import com.javaevaluation.mapper.CourseMapper;
import com.javaevaluation.security.JwtUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 课程控制器（教师管理自己的课程）
 */
@RestController
@RequestMapping("/api/teacher/course")
@RequiredArgsConstructor
public class CourseController {

    private final CourseMapper courseMapper;
    private final JwtUtils jwtUtils;

    /**
     * 获取当前教师的课程列表
     */
    @GetMapping("/list")
    public Result<List<Course>> list(@RequestHeader("Authorization") String authHeader) {
        Integer teacherId = currentTeacherId(authHeader);
        if (teacherId == null) {
            return Result.fail(ErrorCode.TOKEN_INVALID);
        }
        List<Course> courses = courseMapper.findByTeacherId(teacherId);
        return Result.success(courses);
    }

    /**
     * 获取课程详情
     */
    @GetMapping("/{id}")
    public Result<Course> getById(@PathVariable Integer id,
                                  @RequestHeader("Authorization") String authHeader) {
        Integer teacherId = currentTeacherId(authHeader);
        if (teacherId == null) {
            return Result.fail(ErrorCode.TOKEN_INVALID);
        }
        Course course = courseMapper.findById(id);
        if (course == null) {
            return Result.fail(ErrorCode.NOT_FOUND);
        }
        if (!teacherId.equals(course.getTeacherId())) {
            return Result.fail(ErrorCode.FORBIDDEN);
        }
        return Result.success(course);
    }

    /**
     * 创建课程
     */
    @PostMapping("/create")
    public Result<Course> create(@Valid @RequestBody Course course,
                                 @RequestHeader("Authorization") String authHeader) {
        Integer teacherId = currentTeacherId(authHeader);
        if (teacherId == null) {
            return Result.fail(ErrorCode.TOKEN_INVALID);
        }
        course.setTeacherId(teacherId);
        courseMapper.insert(course);
        return Result.success(course);
    }

    /**
     * 更新课程
     */
    @PutMapping("/{id}")
    public Result<Course> update(@PathVariable Integer id,
                                 @Valid @RequestBody Course course,
                                 @RequestHeader("Authorization") String authHeader) {
        Integer teacherId = currentTeacherId(authHeader);
        if (teacherId == null) {
            return Result.fail(ErrorCode.TOKEN_INVALID);
        }
        Course existing = courseMapper.findById(id);
        if (existing == null) {
            return Result.fail(ErrorCode.NOT_FOUND);
        }
        if (!teacherId.equals(existing.getTeacherId())) {
            return Result.fail(ErrorCode.FORBIDDEN);
        }
        course.setId(id);
        course.setTeacherId(teacherId);
        courseMapper.update(course);
        return Result.success(course);
    }

    /**
     * 删除课程
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id,
                               @RequestHeader("Authorization") String authHeader) {
        Integer teacherId = currentTeacherId(authHeader);
        if (teacherId == null) {
            return Result.fail(ErrorCode.TOKEN_INVALID);
        }
        Course existing = courseMapper.findById(id);
        if (existing == null) {
            return Result.fail(ErrorCode.NOT_FOUND);
        }
        if (!teacherId.equals(existing.getTeacherId())) {
            return Result.fail(ErrorCode.FORBIDDEN);
        }
        courseMapper.delete(id);
        return Result.success(null);
    }

    private Integer currentTeacherId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        String token = authHeader.substring(7);
        if (!jwtUtils.validateToken(token)) {
            return null;
        }
        return jwtUtils.getUserIdFromToken(token);
    }
}
