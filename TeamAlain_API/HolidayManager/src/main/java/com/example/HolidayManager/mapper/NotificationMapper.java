package com.example.HolidayManager.mapper;

import com.example.HolidayManager.dto.NotificationRequestDto;
import com.example.HolidayManager.dto.NotificationResponseDto;
import com.example.HolidayManager.entity.NotificationEntity;

import java.util.List;


public interface NotificationMapper {

    NotificationResponseDto entityToDto(NotificationEntity notificationEntity);

    List<NotificationResponseDto> entitiesToDtos(List<NotificationEntity> notificationEntities);

    NotificationEntity dtoToEntity(NotificationRequestDto dto);
    NotificationEntity dtoToEntity(NotificationResponseDto dto);
}
