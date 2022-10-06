package com.example.HolidayManager.dto;

import com.example.HolidayManager.entity.NotificationStatus;
import lombok.Data;

@Data
public class NotificationResponseDto {

    private Long notificationId;
    private Long userFrom;
    private Long userTo;
    private Long holidayId;
    private NotificationStatus status;
    private String message;

}
