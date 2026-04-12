package com.javaevaluation.mapper;

import com.javaevaluation.entity.Homework;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 作业Mapper接口
 */
@Mapper
public interface HomeworkMapper {

    /**
     * 根据ID查询作业
     */
    @Select("SELECT * FROM homework WHERE id = #{id}")
    Homework findById(@Param("id") Integer id);

    /**
     * 根据班级ID查询作业
     */
    @Select("SELECT * FROM homework WHERE class_id = #{classId} AND status = 1 ORDER BY deadline DESC")
    List<Homework> findByClassId(@Param("classId") Integer classId);

    /**
     * 根据教师ID查询作业
     */
    @Select("SELECT * FROM homework WHERE teacher_id = #{teacherId} AND status = 1 ORDER BY created_at DESC")
    List<Homework> findByTeacherId(@Param("teacherId") Integer teacherId);

    /**
     * 查询所有作业
     */
    @Select("SELECT * FROM homework WHERE status = 1 ORDER BY created_at DESC")
    List<Homework> findAll();
}