package com.javaevaluation.controller;

import com.javaevaluation.common.ErrorCode;
import com.javaevaluation.common.Result;
import com.javaevaluation.entity.Homework;
import com.javaevaluation.mapper.HomeworkMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 作业控制器
 */
@RestController
@RequestMapping("/api/homework")
@RequiredArgsConstructor
public class HomeworkController {

    private final HomeworkMapper homeworkMapper;

    /**
     * 获取所有作业列表（学生查看）
     */
    @GetMapping("/list")
    public Result<List<Homework>> list() {
        List<Homework> homeworkList = homeworkMapper.findAll();
        return Result.success(homeworkList);
    }

    /**
     * 获取作业详情
     */
    @GetMapping("/{id}")
    public Result<Homework> getById(@PathVariable Integer id) {
        Homework homework = homeworkMapper.findById(id);
        if (homework == null) {
            return Result.fail(ErrorCode.NOT_FOUND);
        }
        return Result.success(homework);
    }

    /**
     * 创建作业（教师）
     */
    @PostMapping("/create")
    public Result<Homework> create(@Valid @RequestBody Homework homework) {
        homeworkMapper.insert(homework);
        return Result.success(homework);
    }

    /**
     * 更新作业（教师）
     */
    @PutMapping("/{id}")
    public Result<Homework> update(@PathVariable Integer id, @Valid @RequestBody Homework homework) {
        Homework existingHomework = homeworkMapper.findById(id);
        if (existingHomework == null) {
            return Result.fail(ErrorCode.NOT_FOUND);
        }
        homework.setId(id);
        homeworkMapper.update(homework);
        return Result.success(homework);
    }

    /**
     * 删除作业（教师）
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        Homework homework = homeworkMapper.findById(id);
        if (homework == null) {
            return Result.fail(ErrorCode.NOT_FOUND);
        }
        homeworkMapper.delete(id);
        return Result.success(null);
    }
}