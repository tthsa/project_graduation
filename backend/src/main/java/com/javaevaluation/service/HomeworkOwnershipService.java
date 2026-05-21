package com.javaevaluation.service;

import com.javaevaluation.entity.Course;
import com.javaevaluation.entity.Homework;
import com.javaevaluation.entity.TestCase;
import com.javaevaluation.mapper.CourseMapper;
import com.javaevaluation.mapper.HomeworkMapper;
import com.javaevaluation.mapper.TestCaseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 教师对作业/测试用例的归属校验
 * 链路:homework → course → course.teacher_id
 */
@Service
@RequiredArgsConstructor
public class HomeworkOwnershipService {

    private final HomeworkMapper homeworkMapper;
    private final CourseMapper courseMapper;
    private final TestCaseMapper testCaseMapper;

    public boolean isHomeworkOwnedBy(Integer homeworkId, Integer teacherId) {
        if (homeworkId == null || teacherId == null) {
            return false;
        }
        Homework homework = homeworkMapper.selectById(homeworkId);
        if (homework == null || homework.getCourseId() == null) {
            return false;
        }
        Course course = courseMapper.selectById(homework.getCourseId());
        return course != null && teacherId.equals(course.getTeacherId());
    }

    public boolean isTestCaseOwnedBy(Integer testCaseId, Integer teacherId) {
        if (testCaseId == null || teacherId == null) {
            return false;
        }
        TestCase testCase = testCaseMapper.selectById(testCaseId);
        if (testCase == null) {
            return false;
        }
        return isHomeworkOwnedBy(testCase.getHomeworkId(), teacherId);
    }
}
