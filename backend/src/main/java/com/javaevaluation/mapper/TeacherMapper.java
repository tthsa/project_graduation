package com.javaevaluation.mapper;

import com.javaevaluation.entity.Teacher;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TeacherMapper {

    @Select("SELECT * FROM teacher WHERE id = #{id}")
    Teacher findById(Integer id);

    @Select("SELECT * FROM teacher WHERE teacher_no = #{teacherNo}")
    Teacher findByTeacherNo(String teacherNo);

    @Select("SELECT * FROM teacher")
    List<Teacher> findAll();

    @Insert("INSERT INTO teacher (teacher_no, password, name, email, phone, status, create_time) " +
            "VALUES (#{teacherNo}, #{password}, #{name}, #{email}, #{phone}, #{status}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Teacher teacher);

    @Update("UPDATE teacher SET name = #{name}, email = #{email}, phone = #{phone}, " +
            "status = #{status}, updated_at = #{updatedAt} WHERE id = #{id}")
    int update(Teacher teacher);

    @Update("UPDATE teacher SET password = #{password} WHERE id = #{id}")
    int updatePassword(@Param("id") Integer id, @Param("password") String password);

    @Delete("DELETE FROM teacher WHERE id = #{id}")
    int delete(Integer id);
}