package com.example.HolidayManager.service;

import com.example.HolidayManager.dto.NotificationRequestDto;
import com.example.HolidayManager.dto.NotificationResponseDto;
import com.example.HolidayManager.entity.RequestEntity;


import java.util.List;

public interface NotificationService {

    List<NotificationResponseDto> findAll();

    List<NotificationResponseDto> findByUserTo(Long id);

    NotificationResponseDto add(NotificationRequestDto dto);
    NotificationResponseDto add(NotificationResponseDto dto);

    List<NotificationResponseDto> markAsRead(Long notificationId);


    //NotificationResponseDto createNotification(RequestEntity request);

}
