package com.javaevaluation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.javaevaluation.entity.Admin;
import org.apache.ibatis.annotations.*;

@Mapper
public interface AdminMapper extends BaseMapper<Admin> {

    @Select("SELECT * FROM admin WHERE username = #{username}")
    Admin findByUsername(String username);

    @Update("UPDATE admin SET name = #{name}, updated_at = #{updatedAt} WHERE id = #{id}")
    int update(Admin admin);

    @Update("UPDATE admin SET password = #{password} WHERE id = #{id}")
    int updatePassword(@Param("id") Integer id, @Param("password") String password);
}