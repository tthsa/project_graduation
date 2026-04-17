package com.javaevaluation.mapper;

import com.javaevaluation.entity.SubmissionFile;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 提交文件Mapper接口
 */
@Mapper
public interface SubmissionFileMapper {

    /**
     * 插入文件记录
     */
    @Insert("INSERT INTO submission_file (submission_id, file_path) " +
            "VALUES (#{submissionId}, #{filePath})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SubmissionFile file);

    /**
     * 根据提交ID查询文件列表
     */
    @Select("SELECT * FROM submission_file WHERE submission_id = #{submissionId}")
    List<SubmissionFile> findBySubmissionId(@Param("submissionId") Integer submissionId);

    /**
     * 根据ID查询文件
     */
    @Select("SELECT * FROM submission_file WHERE id = #{id}")
    SubmissionFile findById(@Param("id") Integer id);

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

    /**
     * 批量插入文件记录
     */
    int batchInsert(@Param("files") List<SubmissionFile> files);
}