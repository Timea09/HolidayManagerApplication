package com.example.HolidayManager.dto;

import com.example.HolidayManager.entity.UserType;
import lombok.Data;

@Data
public class UserResponseDto {

   final private Long id;
    final private String firstName;
    final private String lastName;
    final  private String email;
    final private String department;
    final private Integer daysLeft;
    final private Long teamId;



}
