package com.javaevaluation.mapper;

import com.javaevaluation.entity.SubmissionHistory;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SubmissionHistoryMapper {

    @Select("SELECT * FROM submission_history WHERE submission_id = #{submissionId}")
    List<SubmissionHistory> findBySubmissionId(Integer submissionId);

    @Insert("INSERT INTO submission_history (submission_id, submit_time) VALUES (#{submissionId}, #{submitTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SubmissionHistory history);
}