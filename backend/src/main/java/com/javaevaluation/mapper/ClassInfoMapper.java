package com.javaevaluation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.javaevaluation.entity.ClassInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 班级Mapper接口
 */
@Mapper
public interface ClassInfoMapper extends BaseMapper<ClassInfo> {

    /**
     * 根据教师ID查询班级
     */
    @Select("SELECT * FROM class_info WHERE teacher_id = #{teacherId} AND status = 1 ORDER BY create_time DESC")
    List<ClassInfo> findByTeacherId(@Param("teacherId") Integer teacherId);

    /**
     * 根据名称模糊查询班级
     */
    @Select("SELECT * FROM class_info WHERE name LIKE CONCAT('%', #{name}, '%') AND status = 1")
    List<ClassInfo> findByNameLike(@Param("name") String name);
}