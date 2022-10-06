package com.example.HolidayManager.dto;

import com.example.HolidayManager.entity.NotificationStatus;
import com.example.HolidayManager.entity.Status;
import lombok.Data;

@Data
public class NotificationRequestDto {

    private Long notificationId;
    private Long userFrom;
    private Long userTo;
    private Long holidayId;
    private NotificationStatus status;
    private Status requestStatus;
    private String holidayType;

}
