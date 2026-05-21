package com.javaevaluation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.javaevaluation.entity.Teacher;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TeacherMapper extends BaseMapper<Teacher> {

    @Select("SELECT * FROM teacher WHERE teacher_no = #{teacherNo}")
    Teacher findByTeacherNo(String teacherNo);

    @Update("UPDATE teacher SET name = #{name}, email = #{email}, phone = #{phone}, " +
            "status = #{status}, updated_at = #{updatedAt} WHERE id = #{id}")
    int update(Teacher teacher);

    @Update("UPDATE teacher SET password = #{password} WHERE id = #{id}")
    int updatePassword(@Param("id") Integer id, @Param("password") String password);
}