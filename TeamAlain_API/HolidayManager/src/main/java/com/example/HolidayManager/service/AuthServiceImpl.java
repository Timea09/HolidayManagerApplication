package com.example.HolidayManager.service;

import com.example.HolidayManager.dto.LoginRequestDto;
import com.example.HolidayManager.dto.LoginResponseDto;
import com.example.HolidayManager.entity.UserEntity;
import com.example.HolidayManager.mapper.UserMapper;
import com.example.HolidayManager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    private final PasswordEncoder encoder;


    @Override
    public LoginResponseDto checkUserCredentials(LoginRequestDto loginRequestDto) {
        UserEntity userEntity = userRepository.findByEmail(loginRequestDto.getEmail());

        if (userEntity != null && encoder.matches(loginRequestDto.getPassword(),userEntity.getPassword())) {
            return userMapper.entityToResponseDto(userEntity);
        }
        return null;
    }
}
