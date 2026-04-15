package com.javaevaluation.mapper;

import com.javaevaluation.entity.Submission;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 提交记录Mapper接口
 */
@Mapper
public interface SubmissionMapper {

    /**
     * 插入提交记录
     */
    @Insert("INSERT INTO submission (homework_id, student_id, submit_time, status) " +
            "VALUES (#{homeworkId}, #{studentId}, #{submitTime}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Submission submission);

    /**
     * 更新提交记录
     */
    @Update("UPDATE submission SET submit_time = #{submitTime}, status = #{status} WHERE id = #{id}")
    int update(Submission submission);

    /**
     * 根据ID查询提交记录
     */
    @Select("SELECT * FROM submission WHERE id = #{id}")
    Submission findById(@Param("id") Integer id);

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
}