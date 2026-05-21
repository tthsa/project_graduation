package com.javaevaluation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.javaevaluation.entity.Submission;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 提交记录Mapper接口
 */
@Mapper
public interface SubmissionMapper extends BaseMapper<Submission> {

    /**
     * 仅更新状态(不动 submit_time)
     */
    @Update("UPDATE submission SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Integer id, @Param("status") Integer status);

    /**
     * 根据作业ID和学生ID查询提交记录
     */
    @Select("SELECT * FROM submission WHERE homework_id = #{homeworkId} AND student_id = #{studentId}")
    Submission findByHomeworkIdAndStudentId(@Param("homeworkId") Integer homeworkId,
                                            @Param("studentId") Integer studentId);

    /**
     * 根据作业ID查询所有提交记录
     */
    @Select("SELECT * FROM submission WHERE homework_id = #{homeworkId} ORDER BY submit_time DESC")
    List<Submission> findByHomeworkId(@Param("homeworkId") Integer homeworkId);

    /**
     * 根据学生ID查询提交记录
     */
    @Select("SELECT * FROM submission WHERE student_id = #{studentId} ORDER BY submit_time DESC")
    List<Submission> findByStudentId(@Param("studentId") Integer studentId);

    /**
     * 根据作业ID统计提交数量
     */
    @Select("SELECT COUNT(*) FROM submission WHERE homework_id = #{homeworkId}")
    int countByHomeworkId(@Param("homeworkId") Integer homeworkId);

    /**
     * 统计某教师作业下、状态小于 status 的提交数(待评测/评测中)
     */
    @Select("SELECT COUNT(*) FROM submission s " +
            "JOIN homework h ON s.homework_id = h.id " +
            "JOIN course c ON h.course_id = c.id " +
            "WHERE c.teacher_id = #{teacherId} AND s.status < #{status}")
    int countPendingByTeacherId(@Param("teacherId") Integer teacherId,
                                @Param("status") Integer status);

    /**
     * 统计某教师作业下、指定状态的提交数(已完成)
     */
    @Select("SELECT COUNT(*) FROM submission s " +
            "JOIN homework h ON s.homework_id = h.id " +
            "JOIN course c ON h.course_id = c.id " +
            "WHERE c.teacher_id = #{teacherId} AND s.status = #{status}")
    int countByTeacherIdAndStatus(@Param("teacherId") Integer teacherId,
                                  @Param("status") Integer status);
}