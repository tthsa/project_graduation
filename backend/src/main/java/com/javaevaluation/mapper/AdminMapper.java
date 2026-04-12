package com.javaevaluation.mapper;

import com.javaevaluation.entity.Admin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 管理员Mapper接口
 */
@Mapper
public interface AdminMapper {

    /**
     * 根据用户名查询管理员
     */
    @Select("SELECT * FROM admin WHERE username = #{username}")
    Admin findByUsername(@Param("username") String username);

    /**
     * 根据ID查询管理员
     */
    @Select("SELECT * FROM admin WHERE id = #{id}")
    Admin findById(@Param("id") Integer id);
}