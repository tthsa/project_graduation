package com.javaevaluation.mapper;

import com.javaevaluation.entity.EvaluationResult;
import org.apache.ibatis.annotations.*;

@Mapper
public interface EvaluationResultMapper {

    @Select("SELECT * FROM evaluation_result WHERE id = #{id}")
    EvaluationResult findById(Integer id);

    @Select("SELECT * FROM evaluation_result WHERE submission_id = #{submissionId} ORDER BY created_at DESC LIMIT 1")
    EvaluationResult findBySubmissionId(Integer submissionId);

    @Insert("INSERT INTO evaluation_result (submission_id, test_score, llm_score, llm_review, execution_time, created_at) " +
            "VALUES (#{submissionId}, #{testScore}, #{llmScore}, #{llmReview}, #{executionTime}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(EvaluationResult result);
}