package com.javaevaluation.mapper;

import com.javaevaluation.entity.Teacher;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 教师Mapper接口
 */
@Mapper
public interface TeacherMapper {

    /**
     * 根据用户名查询教师
     */
    @Select("SELECT * FROM teacher WHERE username = #{username}")
    Teacher findByUsername(@Param("username") String username);

    /**
     * 根据ID查询教师
     */
    @Select("SELECT * FROM teacher WHERE id = #{id}")
    Teacher findById(@Param("id") Integer id);

    /**
     * 查询所有教师
     */
    @Select("SELECT * FROM teacher WHERE status = 1 ORDER BY created_at DESC")
    List<Teacher> findAll();

    /**
     * 根据状态查询教师
     */
    @Select("SELECT * FROM teacher WHERE status = #{status} ORDER BY created_at DESC")
    List<Teacher> findByStatus(@Param("status") Integer status);
}