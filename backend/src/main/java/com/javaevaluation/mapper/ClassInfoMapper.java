package com.javaevaluation.mapper;

import com.javaevaluation.entity.ClassInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 班级Mapper接口
 */
@Mapper
public interface ClassInfoMapper {

    /**
     * 根据ID查询班级
     */
    @Select("SELECT * FROM class WHERE id = #{id}")
    ClassInfo findById(@Param("id") Integer id);

    /**
     * 根据教师ID查询班级
     */
    @Select("SELECT * FROM class WHERE teacher_id = #{teacherId} AND status = 1 ORDER BY create_time DESC")
    List<ClassInfo> findByTeacherId(@Param("teacherId") Integer teacherId);

    /**
     * 查询所有班级
     */
    @Select("SELECT * FROM class WHERE status = 1 ORDER BY create_time DESC")
    List<ClassInfo> findAll();

    /**
     * 根据名称模糊查询班级
     */
    @Select("SELECT * FROM class WHERE name LIKE CONCAT('%', #{name}, '%') AND status = 1")
    List<ClassInfo> findByNameLike(@Param("name") String name);
}