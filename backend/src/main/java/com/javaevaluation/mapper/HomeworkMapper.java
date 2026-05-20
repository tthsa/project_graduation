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

    @Select("SELECT COUNT(*) FROM homework")
    int count();

    @Insert("INSERT INTO homework (course_id, title, description, deadline, status, create_time, " +
            "test_weight, llm_weight, grade_a_threshold, grade_b_threshold, grade_c_threshold, llm_dimensions) " +
            "VALUES (#{courseId}, #{title}, #{description}, #{deadline}, #{status}, #{createdAt}, " +
            "COALESCE(#{testWeight}, 70), COALESCE(#{llmWeight}, 30), " +
            "COALESCE(#{gradeAThreshold}, 90), COALESCE(#{gradeBThreshold}, 75), COALESCE(#{gradeCThreshold}, 60), " +
            "#{llmDimensions})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Homework homework);

    @Update("UPDATE homework SET title = #{title}, description = #{description}, " +
            "deadline = #{deadline}, status = #{status}, updated_at = #{updatedAt}, " +
            "test_weight = COALESCE(#{testWeight}, test_weight), " +
            "llm_weight = COALESCE(#{llmWeight}, llm_weight), " +
            "grade_a_threshold = COALESCE(#{gradeAThreshold}, grade_a_threshold), " +
            "grade_b_threshold = COALESCE(#{gradeBThreshold}, grade_b_threshold), " +
            "grade_c_threshold = COALESCE(#{gradeCThreshold}, grade_c_threshold), " +
            "llm_dimensions = #{llmDimensions} " +
            "WHERE id = #{id}")
    int update(Homework homework);

    @Delete("DELETE FROM homework WHERE id = #{id}")
    int delete(Integer id);

    @Select("SELECT COUNT(*) FROM homework h JOIN course c ON h.course_id = c.id WHERE c.teacher_id = #{teacherId}")
    int countByTeacherId(Integer teacherId);
}