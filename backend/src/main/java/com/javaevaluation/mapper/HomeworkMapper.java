package com.javaevaluation.mapper;

import com.javaevaluation.entity.Homework;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface HomeworkMapper {

    @Select("SELECT * FROM homework WHERE id = #{id}")
    Homework findById(Integer id);

    @Select("SELECT * FROM homework WHERE course_id = #{courseId}")
    List<Homework> findByCourseId(Integer courseId);

    @Select("SELECT * FROM homework")
    List<Homework> findAll();

    @Insert("INSERT INTO homework (course_id, title, description, deadline, status, create_time) " +
            "VALUES (#{courseId}, #{title}, #{description}, #{deadline}, #{status}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Homework homework);

    @Update("UPDATE homework SET title = #{title}, description = #{description}, " +
            "deadline = #{deadline}, status = #{status}, updated_at = #{updatedAt} WHERE id = #{id}")
    int update(Homework homework);

    @Delete("DELETE FROM homework WHERE id = #{id}")
    int delete(Integer id);
}