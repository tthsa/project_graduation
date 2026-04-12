package com.javaevaluation.mapper;

import com.javaevaluation.entity.SystemConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 系统配置Mapper接口
 */
@Mapper
public interface SystemConfigMapper {

    /**
     * 根据ID查询配置
     */
    @Select("SELECT * FROM system_config WHERE id = #{id}")
    SystemConfig findById(@Param("id") Integer id);

    /**
     * 根据配置键查询配置
     */
    @Select("SELECT * FROM system_config WHERE config_key = #{configKey}")
    SystemConfig findByConfigKey(@Param("configKey") String configKey);

    /**
     * 查询所有配置
     */
    @Select("SELECT * FROM system_config ORDER BY config_key")
    List<SystemConfig> findAll();
}