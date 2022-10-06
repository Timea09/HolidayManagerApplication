package com.example.HolidayManager.repository;

import com.example.HolidayManager.entity.NotificationEntity;

import com.example.HolidayManager.entity.NotificationStatus;
import com.example.HolidayManager.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {

    List<NotificationEntity> findAllByUserToAndStatus(UserEntity userEntity, NotificationStatus status);
    NotificationEntity findByNotificationId(Long notificationId);

}
