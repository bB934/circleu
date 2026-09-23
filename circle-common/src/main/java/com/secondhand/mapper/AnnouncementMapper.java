package com.secondhand.mapper;

import com.secondhand.entity.Announcement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface AnnouncementMapper {
    Announcement findById(@Param("announcementId") Long announcementId);
    List<Announcement> findByStatus(@Param("status") Integer status);
    List<Announcement> findAll();
    int insert(Announcement announcement);
    int update(Announcement announcement);
    int deleteById(@Param("announcementId") Long announcementId);
}