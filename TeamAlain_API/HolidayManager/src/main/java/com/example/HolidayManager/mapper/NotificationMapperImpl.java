package com.example.HolidayManager.mapper;


import com.example.HolidayManager.dto.NotificationRequestDto;
import com.example.HolidayManager.dto.NotificationResponseDto;
import com.example.HolidayManager.entity.HolidayEntity;
import com.example.HolidayManager.entity.NotificationEntity;
import com.example.HolidayManager.entity.UserEntity;
import com.example.HolidayManager.repository.HolidayRepository;
import com.example.HolidayManager.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class NotificationMapperImpl implements NotificationMapper{


    private UserRepository userRepository;
    private HolidayRepository holidayRepository;

    public NotificationMapperImpl(HolidayRepository holidayRepository, UserRepository userRepository) {
        this.holidayRepository = holidayRepository;
        this.userRepository = userRepository;
    }

    @Override
    public NotificationResponseDto entityToDto(NotificationEntity notificationEntity) {
        if( notificationEntity == null) {
            return null;
        }
        NotificationResponseDto dto = new NotificationResponseDto();
        dto.setNotificationId(notificationEntity.getNotificationId());
        dto.setUserFrom(notificationEntity.getUserFrom().getId());
        dto.setUserTo(notificationEntity.getUserTo().getId());

        if (notificationEntity.getHoliday() != null) {
            dto.setHolidayId(notificationEntity.getHoliday().getId());
        }
        else {
            dto.setHolidayId(null);
        }

        dto.setStatus(notificationEntity.getStatus());
        dto.setMessage(notificationEntity.getMessage());

        return dto;
    }

    @Override
    public List<NotificationResponseDto> entitiesToDtos(List<NotificationEntity> notificationEntities) {
        if (notificationEntities.isEmpty()) {
            return null;
        }
        List<NotificationResponseDto> dtos= new ArrayList<>();
        for (NotificationEntity entity: notificationEntities) {
            dtos.add(entityToDto(entity));
        }
        return dtos;
    }

    @Override
    public NotificationEntity dtoToEntity(NotificationRequestDto dto) {

       if (dto == null) {
           return null;
       }
        NotificationEntity entity = new NotificationEntity();
        entity.setNotificationId(dto.getNotificationId());
        entity.setStatus(dto.getStatus());
        entity.setMessage("");


        Optional<HolidayEntity> optionalRequest = holidayRepository.findById(dto.getHolidayId());
        optionalRequest.ifPresent(entity::setHoliday);

        Optional<UserEntity> optionalUserToEntity = userRepository.findById(dto.getUserTo());
        //System.out.println(optionalUserToEntity);
        optionalUserToEntity.ifPresent(entity::setUserTo);



        Optional<UserEntity> optionalUserFromEntity = userRepository.findById(dto.getUserFrom());
        if (optionalUserFromEntity.isPresent()){
            entity.setUserFrom(optionalUserFromEntity.get());
        }

        return entity;
    }

    @Override
    public NotificationEntity dtoToEntity(NotificationResponseDto dto) {

        if (dto == null) {
            return null;
        }
        NotificationEntity entity = new NotificationEntity();
        entity.setNotificationId(dto.getNotificationId());
        entity.setStatus(dto.getStatus());
        entity.setMessage(dto.getMessage());


        if (dto.getHolidayId() != null) {
            Optional<HolidayEntity> optionalRequest = holidayRepository.findById(dto.getHolidayId());
            optionalRequest.ifPresent(entity::setHoliday);
        }

        Optional<UserEntity> optionalUserToEntity = userRepository.findById(dto.getUserTo());

        optionalUserToEntity.ifPresent(entity::setUserTo);

        Optional<UserEntity> optionalUserFromEntity = userRepository.findById(dto.getUserFrom());
        if (optionalUserFromEntity.isPresent()){
            entity.setUserFrom(optionalUserFromEntity.get());
        }

        return entity;
    }
}
