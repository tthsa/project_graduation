package com.javaevaluation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.javaevaluation.entity.TestCase;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 测试用例Mapper接口
 */
@Mapper
public interface TestCaseMapper extends BaseMapper<TestCase> {

    /**
     * 根据作业ID查询测试用例
     */
    @Select("SELECT * FROM test_case WHERE homework_id = #{homeworkId} ORDER BY sort_order")
    List<TestCase> findByHomeworkId(@Param("homeworkId") Integer homeworkId);

    /**
     * 根据ID查询（覆盖 BaseMapperExt 默认方法，避免 selectById 异常）
     */
    @Select("SELECT * FROM test_case WHERE id = #{id}")
    TestCase findById(Integer id);

    /**
     * 根据作业ID查询公开测试用例
     */
    @Select("SELECT * FROM test_case WHERE homework_id = #{homeworkId} AND is_public = 1 ORDER BY sort_order")
    List<TestCase> findPublicByHomeworkId(@Param("homeworkId") Integer homeworkId);

    /**
     * 更新测试用例
     */
    @Update("UPDATE test_case SET name = #{name}, input = #{input}, expected_output = #{expectedOutput}, " +
            "is_public = #{isPublic}, sort_order = #{sortOrder} WHERE id = #{id}")
    int update(TestCase testCase);

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