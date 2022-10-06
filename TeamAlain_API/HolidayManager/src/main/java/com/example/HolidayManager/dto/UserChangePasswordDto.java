package com.example.HolidayManager.dto;

import lombok.Data;

@Data
public class UserChangePasswordDto {
    private String oldPassword;
    private String newPassword;
}
