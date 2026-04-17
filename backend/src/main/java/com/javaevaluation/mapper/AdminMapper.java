package com.javaevaluation.mapper;

import com.javaevaluation.entity.Admin;
import org.apache.ibatis.annotations.*;

@Mapper
public interface AdminMapper {

    @Select("SELECT * FROM admin WHERE id = #{id}")
    Admin findById(Integer id);

    @Select("SELECT * FROM admin WHERE username = #{username}")
    Admin findByUsername(String username);

    @Insert("INSERT INTO admin (username, password, name, create_time) " +
            "VALUES (#{username}, #{password}, #{name}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Admin admin);

    @Update("UPDATE admin SET name = #{name}, updated_at = #{updatedAt} WHERE id = #{id}")
    int update(Admin admin);

    @Update("UPDATE admin SET password = #{password} WHERE id = #{id}")
    int updatePassword(@Param("id") Integer id, @Param("password") String password);
}