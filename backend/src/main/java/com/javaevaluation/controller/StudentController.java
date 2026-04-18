package com.javaevaluation.controller;

import com.javaevaluation.common.ErrorCode;
import com.javaevaluation.common.Result;
import com.javaevaluation.entity.Student;
import com.javaevaluation.mapper.StudentMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学生控制器（教师管理班级学生）
 */
@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class StudentController {

    private final StudentMapper studentMapper;

    /**
     * 获取所有学生列表
     */
    @GetMapping("/list")
    public Result<List<Student>> list() {
        List<Student> students = studentMapper.findAll();
        return Result.success(students);
    }

    /**
     * 根据班级ID获取学生列表
     */
    @GetMapping("/class/{classId}")
    public Result<List<Student>> listByClassId(@PathVariable Integer classId) {
        List<Student> students = studentMapper.findByClassId(classId);
        return Result.success(students);
    }

    /**
     * 获取学生详情
     */
    @GetMapping("/{id}")
    public Result<Student> getById(@PathVariable Integer id) {
        Student student = studentMapper.findById(id);
        if (student == null) {
            return Result.fail(ErrorCode.NOT_FOUND);
        }
        return Result.success(student);
    }

    /**
     * 添加学生（教师）
     */
    @PostMapping("/create")
    public Result<Student> create(@Valid @RequestBody Student student) {
        // 检查学号是否已存在
        Student existingStudent = studentMapper.findByStudentNo(student.getStudentNo());
        if (existingStudent != null) {
            return Result.fail(ErrorCode.BAD_REQUEST, "学号已存在");
        }
        studentMapper.insert(student);
        return Result.success(student);
    }

    /**
     * 更新学生信息（教师）
     */
    @PutMapping("/{id}")
    public Result<Student> update(@PathVariable Integer id, @Valid @RequestBody Student student) {
        Student existingStudent = studentMapper.findById(id);
        if (existingStudent == null) {
            return Result.fail(ErrorCode.NOT_FOUND);
        }
        student.setId(id);
        studentMapper.update(student);
        return Result.success(student);
    }

    /**
     * 删除学生（教师）
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        Student student = studentMapper.findById(id);
        if (student == null) {
            return Result.fail(ErrorCode.NOT_FOUND);
        }
        studentMapper.delete(id);
        return Result.success(null);
    }
}