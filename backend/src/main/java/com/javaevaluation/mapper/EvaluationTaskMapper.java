package com.javaevaluation.mapper;

import com.javaevaluation.entity.EvaluationTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 评测任务Mapper接口
 */
@Mapper
public interface EvaluationTaskMapper {

    /**
     * 根据ID查询评测任务
     */
    @Select("SELECT * FROM evaluation_task WHERE id = #{id}")
    EvaluationTask findById(@Param("id") Integer id);

    /**
     * 根据作业ID查询评测任务
     */
    @Select("SELECT * FROM evaluation_task WHERE homework_id = #{homeworkId} ORDER BY created_at DESC")
    List<EvaluationTask> findByHomeworkId(@Param("homeworkId") Integer homeworkId);

    /**
     * 根据状态查询评测任务
     */
    @Select("SELECT * FROM evaluation_task WHERE status = #{status} ORDER BY created_at")
    List<EvaluationTask> findByStatus(@Param("status") String status);

    /**
     * 查询最新的评测任务
     */
    @Select("SELECT * FROM evaluation_task WHERE homework_id = #{homeworkId} ORDER BY created_at DESC LIMIT 1")
    EvaluationTask findLatestByHomeworkId(@Param("homeworkId") Integer homeworkId);
}