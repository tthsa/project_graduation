package com.javaevaluation.mapper;

import com.javaevaluation.entity.EvaluationResult;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 评测结果Mapper接口
 */
@Mapper
public interface EvaluationResultMapper {

    /**
     * 插入评测结果
     */
    @Insert("INSERT INTO evaluation_result (task_id, submission_id, student_id, compile_status, " +
            "test_passed, test_total, test_score, llm_score, total_score, llm_review, error_message, created_at) " +
            "VALUES (#{taskId}, #{submissionId}, #{studentId}, #{compileStatus}, " +
            "#{testPassed}, #{testTotal}, #{testScore}, #{llmScore}, #{totalScore}, #{llmReview}, #{errorMessage}, #{createdAt})")
    int insert(EvaluationResult result);

    /**
     * 根据ID查询评测结果
     */
    @Select("SELECT * FROM evaluation_result WHERE id = #{id}")
    EvaluationResult findById(@Param("id") Integer id);

    /**
     * 根据任务ID查询评测结果
     */
    @Select("SELECT * FROM evaluation_result WHERE task_id = #{taskId} ORDER BY total_score DESC")
    List<EvaluationResult> findByTaskId(@Param("taskId") Integer taskId);

    /**
     * 根据学生ID查询评测结果
     */
    @Select("SELECT * FROM evaluation_result WHERE student_id = #{studentId} ORDER BY created_at DESC")
    List<EvaluationResult> findByStudentId(@Param("studentId") Integer studentId);

    /**
     * 根据任务ID和学生ID查询评测结果
     */
    @Select("SELECT * FROM evaluation_result WHERE task_id = #{taskId} AND student_id = #{studentId}")
    EvaluationResult findByTaskIdAndStudentId(@Param("taskId") Integer taskId,
                                              @Param("studentId") Integer studentId);

    /**
     * 根据任务ID统计平均分
     */
    @Select("SELECT COALESCE(AVG(total_score), 0) FROM evaluation_result WHERE task_id = #{taskId}")
    double avgScoreByTaskId(@Param("taskId") Integer taskId);
}