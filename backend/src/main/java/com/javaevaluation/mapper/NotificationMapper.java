package com.javaevaluation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.javaevaluation.entity.Notification;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {

    @Select("SELECT * FROM notification WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<Notification> findByUserId(Integer userId);

    @Select("SELECT * FROM notification WHERE user_id = #{userId} AND is_read = 0 ORDER BY create_time DESC")
    List<Notification> findUnreadByUserId(Integer userId);

    @Update("UPDATE notification SET is_read = 1 WHERE id = #{id}")
    int markAsRead(Integer id);

    @Update("UPDATE notification SET is_read = 1 WHERE user_id = #{userId}")
    int markAllAsRead(Integer userId);
}