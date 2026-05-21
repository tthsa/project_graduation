package com.javaevaluation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.javaevaluation.entity.SubmissionFile;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 提交文件Mapper接口
 */
@Mapper
public interface SubmissionFileMapper extends BaseMapper<SubmissionFile> {

    /**
     * 根据提交ID查询文件列表
     */
    @Select("SELECT * FROM submission_file WHERE submission_id = #{submissionId} ORDER BY file_order")
    List<SubmissionFile> findBySubmissionId(@Param("submissionId") Integer submissionId);

    /**
     * 删除提交的所有文件
     */
    @Delete("DELETE FROM submission_file WHERE submission_id = #{submissionId}")
    int deleteBySubmissionId(@Param("submissionId") Integer submissionId);

    /**
     * 统计提交的文件数量
     */
    @Select("SELECT COUNT(*) FROM submission_file WHERE submission_id = #{submissionId}")
    int countBySubmissionId(@Param("submissionId") Integer submissionId);
}