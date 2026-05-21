package com.javaevaluation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.javaevaluation.entity.SubmissionHistory;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SubmissionHistoryMapper extends BaseMapper<SubmissionHistory> {

    @Select("SELECT * FROM submission_history WHERE submission_id = #{submissionId}")
    List<SubmissionHistory> findBySubmissionId(Integer submissionId);
}