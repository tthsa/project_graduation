package com.javaevaluation.controller;

import com.javaevaluation.common.ErrorCode;
import com.javaevaluation.common.Result;
import com.javaevaluation.entity.Teacher;
import com.javaevaluation.mapper.TeacherMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 教师控制器（管理员管理教师）
 */
@RestController
@RequestMapping("/api/teacher")
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherMapper teacherMapper;

    /**
     * 获取所有教师列表
     */
    @GetMapping("/list")
    public Result<List<Teacher>> list() {
        List<Teacher> teachers = teacherMapper.findAll();
        return Result.success(teachers);
    }

    /**
     * 获取教师详情
     */
    @GetMapping("/{id}")
    public Result<Teacher> getById(@PathVariable Integer id) {
        Teacher teacher = teacherMapper.findById(id);
        if (teacher == null) {
            return Result.fail(ErrorCode.NOT_FOUND);
        }
        return Result.success(teacher);
    }

    /**
     * 添加教师（管理员）
     */
    @PostMapping("/create")
    public Result<Teacher> create(@Valid @RequestBody Teacher teacher) {
        // 检查工号是否已存在
        Teacher existingTeacher = teacherMapper.findByTeacherNo(teacher.getTeacherNo());
        if (existingTeacher != null) {
            return Result.fail(ErrorCode.BAD_REQUEST, "工号已存在");
        }
        teacherMapper.insert(teacher);
        return Result.success(teacher);
    }

    /**
     * 更新教师信息（管理员）
     */
    @PutMapping("/{id}")
    public Result<Teacher> update(@PathVariable Integer id, @Valid @RequestBody Teacher teacher) {
        Teacher existingTeacher = teacherMapper.findById(id);
        if (existingTeacher == null) {
            return Result.fail(ErrorCode.NOT_FOUND);
        }
        teacher.setId(id);
        teacherMapper.update(teacher);
        return Result.success(teacher);
    }

    /**
     * 删除教师（管理员）
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        Teacher teacher = teacherMapper.findById(id);
        if (teacher == null) {
            return Result.fail(ErrorCode.NOT_FOUND);
        }
        teacherMapper.delete(id);
        return Result.success(null);
    }
}