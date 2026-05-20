package com.javaevaluation.controller;

import com.javaevaluation.common.Result;
import com.javaevaluation.mapper.CourseMapper;
import com.javaevaluation.mapper.HomeworkMapper;
import com.javaevaluation.mapper.StudentMapper;
import com.javaevaluation.mapper.TeacherMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/stats")
@RequiredArgsConstructor
public class AdminStatsController {

    private final TeacherMapper teacherMapper;
    private final StudentMapper studentMapper;
    private final CourseMapper courseMapper;
    private final HomeworkMapper homeworkMapper;

    @GetMapping("/overview")
    public Result<Map<String, Integer>> overview() {
        Map<String, Integer> stats = new LinkedHashMap<>();
        stats.put("teacherCount", teacherMapper.count());
        stats.put("studentCount", studentMapper.count());
        stats.put("courseCount", courseMapper.count());
        stats.put("homeworkCount", homeworkMapper.count());
        return Result.success(stats);
    }
}
