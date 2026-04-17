package com.javaevaluation.mapper;

import com.javaevaluation.entity.TestCase;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 测试用例Mapper接口
 */
@Mapper
public interface TestCaseMapper {

    /**
     * 插入测试用例
     */
    @Insert("INSERT INTO test_case (homework_id, name, input, expected_output, is_public, sort_order, create_time) " +
            "VALUES (#{homeworkId}, #{name}, #{input}, #{expectedOutput}, #{isPublic}, #{sortOrder}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(TestCase testCase);

    /**
     * 根据作业ID查询测试用例
     */
    @Select("SELECT * FROM test_case WHERE homework_id = #{homeworkId} ORDER BY sort_order")
    List<TestCase> findByHomeworkId(@Param("homeworkId") Integer homeworkId);

    /**
     * 根据作业ID查询公开测试用例
     */
    @Select("SELECT * FROM test_case WHERE homework_id = #{homeworkId} AND is_public = 1 ORDER BY sort_order")
    List<TestCase> findPublicByHomeworkId(@Param("homeworkId") Integer homeworkId);

    /**
     * 根据ID查询测试用例
     */
    @Select("SELECT * FROM test_case WHERE id = #{id}")
    TestCase findById(@Param("id") Integer id);

    /**
     * 更新测试用例
     */
    @Update("UPDATE test_case SET name = #{name}, input = #{input}, expected_output = #{expectedOutput}, " +
            "is_public = #{isPublic}, sort_order = #{sortOrder} WHERE id = #{id}")
    int update(TestCase testCase);

    /**
     * 删除测试用例
     */
    @Delete("DELETE FROM test_case WHERE id = #{id}")
    int delete(@Param("id") Integer id);

    /**
     * 根据作业ID删除所有测试用例
     */
    @Delete("DELETE FROM test_case WHERE homework_id = #{homeworkId}")
    int deleteByHomeworkId(@Param("homeworkId") Integer homeworkId);

    /**
     * 统计作业的测试用例数量
     */
    @Select("SELECT COUNT(*) FROM test_case WHERE homework_id = #{homeworkId}")
    int countByHomeworkId(@Param("homeworkId") Integer homeworkId);
}