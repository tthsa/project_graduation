package com.javaevaluation.controller;

import com.javaevaluation.common.ErrorCode;
import com.javaevaluation.common.Result;
import com.javaevaluation.mapper.CourseMapper;
import com.javaevaluation.mapper.HomeworkMapper;
import com.javaevaluation.mapper.SubmissionMapper;
import com.javaevaluation.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 教师统计接口
 */
@RestController
@RequestMapping("/api/teacher/stats")
@RequiredArgsConstructor
public class TeacherStatsController {

    private final CourseMapper courseMapper;
    private final HomeworkMapper homeworkMapper;
    private final SubmissionMapper submissionMapper;
    private final JwtUtils jwtUtils;

    /**
     * 当前教师首页 4 张统计卡数据
     * - courseCount:    我的课程数
     * - homeworkCount:  我的作业数
     * - pendingCount:   待评测提交数(status < 2)
     * - completedCount: 已完成评测数(status = 2)
     */
    @GetMapping("/overview")
    public Result<Map<String, Integer>> overview(@RequestHeader("Authorization") String authHeader) {
        Integer teacherId = currentTeacherId(authHeader);
        if (teacherId == null) {
            return Result.fail(ErrorCode.TOKEN_INVALID);
        }

        Map<String, Integer> stats = new LinkedHashMap<>();
        stats.put("courseCount", courseMapper.countByTeacherId(teacherId));
        stats.put("homeworkCount", homeworkMapper.countByTeacherId(teacherId));
        stats.put("pendingCount", submissionMapper.countPendingByTeacherId(teacherId, 2));
        stats.put("completedCount", submissionMapper.countByTeacherIdAndStatus(teacherId, 2));
        return Result.success(stats);
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
