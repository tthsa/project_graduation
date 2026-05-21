package com.javaevaluation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.javaevaluation.entity.Course;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CourseMapper extends BaseMapper<Course> {

    @Select("SELECT * FROM course WHERE teacher_id = #{teacherId} ORDER BY id DESC")
    List<Course> findByTeacherId(Integer teacherId);

    @Update("UPDATE course SET name = #{name}, class_id = #{classId} WHERE id = #{id}")
    int update(Course course);

    @Select("SELECT COUNT(*) FROM course WHERE teacher_id = #{teacherId}")
    int countByTeacherId(Integer teacherId);
}
