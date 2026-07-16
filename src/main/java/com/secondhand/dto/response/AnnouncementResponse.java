package com.secondhand.dto.response;

import com.secondhand.entity.Announcement;
import lombok.Data;

@Data
public class AnnouncementResponse {
    private Long announcementId;
    private String title;
    private String content;
    private String createTime;

    public static AnnouncementResponse fromEntity(Announcement announcement) {
        AnnouncementResponse response = new AnnouncementResponse();
        response.setAnnouncementId(announcement.getAnnouncementId());
        response.setTitle(announcement.getTitle());
        response.setContent(announcement.getContent());
        response.setCreateTime(announcement.getCreateTime() != null ? announcement.getCreateTime().toString() : null);
        return response;
    }
}