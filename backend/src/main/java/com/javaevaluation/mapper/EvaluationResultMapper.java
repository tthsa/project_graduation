package com.javaevaluation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.javaevaluation.entity.EvaluationResult;
import org.apache.ibatis.annotations.*;

@Mapper
public interface EvaluationResultMapper extends BaseMapper<EvaluationResult> {

    @Select("SELECT * FROM evaluation_result WHERE submission_id = #{submissionId} ORDER BY created_at DESC LIMIT 1")
    EvaluationResult findBySubmissionId(Integer submissionId);
}