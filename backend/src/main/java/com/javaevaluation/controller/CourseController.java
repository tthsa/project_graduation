package com.javaevaluation.controller;

import com.javaevaluation.common.ErrorCode;
import com.javaevaluation.common.Result;
import com.javaevaluation.entity.Course;
import com.javaevaluation.mapper.CourseMapper;
import com.javaevaluation.utils.JwtUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teacher/course")
@RequiredArgsConstructor
public class CourseController {

    private final CourseMapper courseMapper;
    private final JwtUtils jwtUtils;

    @GetMapping("/list")
    public Result<List<Course>> list(@RequestHeader("Authorization") String authHeader) {
        Integer teacherId = jwtUtils.getUserIdFromHeader(authHeader);
        if (teacherId == null) {
            return Result.fail(ErrorCode.TOKEN_INVALID);
        }
        List<Course> courses = courseMapper.findByTeacherId(teacherId);
        return Result.success(courses);
    }

    @GetMapping("/{id}")
    public Result<Course> getById(@PathVariable Integer id,
                                  @RequestHeader("Authorization") String authHeader) {
        Integer teacherId = jwtUtils.getUserIdFromHeader(authHeader);
        if (teacherId == null) {
            return Result.fail(ErrorCode.TOKEN_INVALID);
        }
        Course course = courseMapper.selectById(id);
        if (course == null) {
            return Result.fail(ErrorCode.NOT_FOUND);
        }
        if (!teacherId.equals(course.getTeacherId())) {
            return Result.fail(ErrorCode.FORBIDDEN);
        }
        return Result.success(course);
    }

    @PostMapping("/create")
    public Result<Course> create(@Valid @RequestBody Course course,
                                 @RequestHeader("Authorization") String authHeader) {
        Integer teacherId = jwtUtils.getUserIdFromHeader(authHeader);
        if (teacherId == null) {
            return Result.fail(ErrorCode.TOKEN_INVALID);
        }
        course.setTeacherId(teacherId);
        courseMapper.insert(course);
        return Result.success(course);
    }

    @PutMapping("/{id}")
    public Result<Course> update(@PathVariable Integer id,
                                 @Valid @RequestBody Course course,
                                 @RequestHeader("Authorization") String authHeader) {
        Integer teacherId = jwtUtils.getUserIdFromHeader(authHeader);
        if (teacherId == null) {
            return Result.fail(ErrorCode.TOKEN_INVALID);
        }
        Course existing = courseMapper.selectById(id);
        if (existing == null) {
            return Result.fail(ErrorCode.NOT_FOUND);
        }
        if (!teacherId.equals(existing.getTeacherId())) {
            return Result.fail(ErrorCode.FORBIDDEN);
        }
        course.setId(id);
        course.setTeacherId(teacherId);
        courseMapper.updateById(course);
        return Result.success(course);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id,
                               @RequestHeader("Authorization") String authHeader) {
        Integer teacherId = jwtUtils.getUserIdFromHeader(authHeader);
        if (teacherId == null) {
            return Result.fail(ErrorCode.TOKEN_INVALID);
        }
        Course existing = courseMapper.selectById(id);
        if (existing == null) {
            return Result.fail(ErrorCode.NOT_FOUND);
        }
        if (!teacherId.equals(existing.getTeacherId())) {
            return Result.fail(ErrorCode.FORBIDDEN);
        }
        courseMapper.deleteById(id);
        return Result.success(null);
    }
}
