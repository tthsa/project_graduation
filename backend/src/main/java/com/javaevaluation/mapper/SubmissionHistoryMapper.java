package com.javaevaluation.mapper;

import com.javaevaluation.entity.SubmissionHistory;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 提交历史Mapper接口
 */
@Mapper
public interface SubmissionHistoryMapper {

    /**
     * 插入提交历史
     */
    @Insert("INSERT INTO submission_history (submission_id, file_paths, submit_time) " +
            "VALUES (#{submissionId}, #{filePaths}, #{submitTime})")
    int insert(SubmissionHistory history);

    /**
     * 根据ID查询提交历史
     */
    @Select("SELECT * FROM submission_history WHERE id = #{id}")
    SubmissionHistory findById(@Param("id") Integer id);

    /**
     * 根据提交记录ID查询历史
     */
    @Select("SELECT * FROM submission_history WHERE submission_id = #{submissionId} ORDER BY submit_time DESC")
    List<SubmissionHistory> findBySubmissionId(@Param("submissionId") Integer submissionId);
}