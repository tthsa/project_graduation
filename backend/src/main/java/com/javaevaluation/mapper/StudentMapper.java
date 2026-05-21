package com.javaevaluation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.javaevaluation.entity.Student;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface StudentMapper extends BaseMapper<Student> {

    @Select("SELECT * FROM student WHERE student_no = #{studentNo}")
    Student findByStudentNo(String studentNo);

    @Select("SELECT * FROM student WHERE class_id = #{classId}")
    List<Student> findByClassId(Integer classId);

    @Update("UPDATE student SET name = #{name}, email = #{email}, class_id = #{classId}, " +
            "status = #{status}, updated_at = #{updatedAt} WHERE id = #{id}")
    int update(Student student);

    @Update("UPDATE student SET password = #{password} WHERE id = #{id}")
    int updatePassword(@Param("id") Integer id, @Param("password") String password);

    @Update("UPDATE student SET first_login = 0 WHERE id = #{id}")
    int markFirstLoginDone(Integer id);
}