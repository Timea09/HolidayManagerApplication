package com.example.HolidayManager.service;

import com.example.HolidayManager.dto.LoginRequestDto;
import com.example.HolidayManager.dto.LoginResponseDto;

public interface AuthService {

    LoginResponseDto checkUserCredentials(LoginRequestDto loginRequestDto);
}
