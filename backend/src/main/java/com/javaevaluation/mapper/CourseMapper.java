package com.javaevaluation.mapper;

import com.javaevaluation.entity.Course;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CourseMapper {

    @Select("SELECT * FROM course WHERE id = #{id}")
    Course findById(Integer id);

    @Select("SELECT * FROM course WHERE teacher_id = #{teacherId} ORDER BY id DESC")
    List<Course> findByTeacherId(Integer teacherId);

    @Select("SELECT * FROM course ORDER BY id DESC")
    List<Course> findAll();

    @Insert("INSERT INTO course (name, teacher_id, class_id, create_time) " +
            "VALUES (#{name}, #{teacherId}, #{classId}, CURRENT_TIMESTAMP)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Course course);

    @Update("UPDATE course SET name = #{name}, class_id = #{classId} WHERE id = #{id}")
    int update(Course course);

    @Delete("DELETE FROM course WHERE id = #{id}")
    int delete(Integer id);

    @Select("SELECT COUNT(*) FROM course WHERE teacher_id = #{teacherId}")
    int countByTeacherId(Integer teacherId);
}
