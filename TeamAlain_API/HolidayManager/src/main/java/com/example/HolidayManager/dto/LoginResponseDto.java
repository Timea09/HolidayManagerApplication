package com.example.HolidayManager.dto;

import com.example.HolidayManager.entity.UserType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponseDto {

    private  Long id;
    private  UserType userType;
}
