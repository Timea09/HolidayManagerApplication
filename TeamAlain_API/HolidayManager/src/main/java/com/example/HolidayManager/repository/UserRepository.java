package com.example.HolidayManager.repository;

import com.example.HolidayManager.dto.UserDto;
import com.example.HolidayManager.entity.UserEntity;
import com.example.HolidayManager.entity.UserType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    UserEntity findByEmail(String username);

    List<UserEntity> findAllByUserType(UserType userType);


    List<UserEntity> findByTeamIdIsNullAndUserTypeNot(UserType userType);

}
