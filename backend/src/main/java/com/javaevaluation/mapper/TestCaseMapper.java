package com.javaevaluation.mapper;

import com.javaevaluation.entity.TestCase;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 测试用例Mapper接口
 */
@Mapper
public interface TestCaseMapper {

    /**
     * 根据作业ID查询测试用例
     */
    @Select("SELECT * FROM test_case WHERE homework_id = #{homeworkId} ORDER BY sort_order")
    List<TestCase> findByHomeworkId(Integer homeworkId);

    /**
     * 根据作业ID计算总分
     */
    @Select("SELECT COALESCE(SUM(score), 0) FROM test_case WHERE homework_id = #{homeworkId}")
    int sumScoreByHomeworkId(@Param("homeworkId") Integer homeworkId);
}