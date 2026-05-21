package com.javaevaluation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.javaevaluation.entity.Homework;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface HomeworkMapper extends BaseMapper<Homework> {

    @Select("SELECT * FROM homework WHERE course_id = #{courseId}")
    List<Homework> findByCourseId(Integer courseId);

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

    @Select("SELECT COUNT(*) FROM homework h JOIN course c ON h.course_id = c.id WHERE c.teacher_id = #{teacherId}")
    int countByTeacherId(Integer teacherId);
}