package com.javaevaluation.mapper;

import com.javaevaluation.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 通知Mapper接口
 */
@Mapper
public interface NotificationMapper {

    /**
     * 根据ID查询通知
     */
    @Select("SELECT * FROM notification WHERE id = #{id}")
    Notification findById(@Param("id") Integer id);

    /**
     * 根据用户ID查询通知
     */
    @Select("SELECT * FROM notification WHERE user_id = #{userId} AND user_type = #{userType} ORDER BY created_at DESC")
    List<Notification> findByUserIdAndUserType(@Param("userId") Integer userId, @Param("userType") String userType);

    /**
     * 查询未读通知
     */
    @Select("SELECT * FROM notification WHERE user_id = #{userId} AND user_type = #{userType} AND is_read = 0 ORDER BY created_at DESC")
    List<Notification> findUnread(@Param("userId") Integer userId, @Param("userType") String userType);

    /**
     * 统计未读通知数量
     */
    @Select("SELECT COUNT(*) FROM notification WHERE user_id = #{userId} AND user_type = #{userType} AND is_read = 0")
    int countUnread(@Param("userId") Integer userId, @Param("userType") String userType);
}