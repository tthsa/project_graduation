package com.javaevaluation.mapper;

import com.javaevaluation.entity.Student;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 学生Mapper接口
 */
@Mapper
public interface StudentMapper {

    /**
     * 根据学号查询学生
     */
    @Select("SELECT * FROM student WHERE student_no = #{studentNo}")
    Student findByStudentNo(@Param("studentNo") String studentNo);

    /**
     * 根据ID查询学生
     */
    @Select("SELECT * FROM student WHERE id = #{id}")
    Student findById(@Param("id") Integer id);

    /**
     * 根据班级ID查询学生
     */
    @Select("SELECT * FROM student WHERE class_id = #{classId} AND status = 1 ORDER BY student_no")
    List<Student> findByClassId(@Param("classId") Integer classId);

    /**
     * 查询所有学生
     */
    @Select("SELECT * FROM student WHERE status = 1 ORDER BY class_id, student_no")
    List<Student> findAll();

    /**
     * 根据班级ID统计学生数量
     */
    @Select("SELECT COUNT(*) FROM student WHERE class_id = #{classId} AND status = 1")
    int countByClassId(@Param("classId") Integer classId);
}