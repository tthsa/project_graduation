package com.javaevaluation.mapper;

import com.javaevaluation.entity.Notification;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface NotificationMapper {

    @Select("SELECT * FROM notification WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<Notification> findByUserId(Integer userId);

    @Select("SELECT * FROM notification WHERE user_id = #{userId} AND is_read = 0 ORDER BY create_time DESC")
    List<Notification> findUnreadByUserId(Integer userId);

    @Insert("INSERT INTO notification (user_id, title, content, is_read, create_time) " +
            "VALUES (#{userId}, #{title}, #{content}, 0, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Notification notification);

    @Update("UPDATE notification SET is_read = 1 WHERE id = #{id}")
    int markAsRead(Integer id);

    @Update("UPDATE notification SET is_read = 1 WHERE user_id = #{userId}")
    int markAllAsRead(Integer userId);

    @Delete("DELETE FROM notification WHERE id = #{id}")
    int delete(Integer id);
}