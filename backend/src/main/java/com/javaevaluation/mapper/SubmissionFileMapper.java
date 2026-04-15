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
    @Insert("INSERT INTO submission_file (submission_id, file_path, file_name, file_size, upload_time) " +
            "VALUES (#{submissionId}, #{filePath}, #{fileName}, #{fileSize}, #{uploadTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SubmissionFile file);

    /**
     * 批量插入文件记录
     */
    @Insert("<script>" +
            "INSERT INTO submission_file (submission_id, file_path, file_name, file_size, upload_time) VALUES " +
            "<foreach collection='files' item='file' separator=','>" +
            "(#{file.submissionId}, #{file.filePath}, #{file.fileName}, #{file.fileSize}, #{file.uploadTime})" +
            "</foreach>" +
            "</script>")
    int batchInsert(@Param("files") List<SubmissionFile> files);

    /**
     * 根据提交ID查询文件列表
     */
    @Select("SELECT * FROM submission_file WHERE submission_id = #{submissionId} ORDER BY upload_time")
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
     * 统计提交的文件总大小
     */
    @Select("SELECT COALESCE(SUM(file_size), 0) FROM submission_file WHERE submission_id = #{submissionId}")
    Long sumFileSizeBySubmissionId(@Param("submissionId") Integer submissionId);
}