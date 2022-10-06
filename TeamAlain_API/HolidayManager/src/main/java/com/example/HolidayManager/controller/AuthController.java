package com.example.HolidayManager.controller;

import com.example.HolidayManager.dto.TokenDto;
import com.example.HolidayManager.security.JwtTokenService;
import com.example.HolidayManager.dto.LoginRequestDto;
import com.example.HolidayManager.dto.LoginResponseDto;
import com.example.HolidayManager.service.AuthService;
import com.example.HolidayManager.service.UserService;
import com.example.HolidayManager.service.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@RestController
@CrossOrigin
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    private final JwtTokenService jwtTokenService;

    private final UserServiceImpl userService;

    @PostMapping("/login")
    private ResponseEntity<TokenDto> loginUser(@RequestBody LoginRequestDto loginRequestDto) {


        LoginResponseDto loginResponseDto = authService.checkUserCredentials(loginRequestDto);

        if (loginResponseDto != null) {
            String jwt = jwtTokenService.createJwtToken(loginResponseDto.getId(), Collections.singleton(loginResponseDto.getUserType()));
            TokenDto token = new TokenDto();
            token.setToken(jwt);
            return ResponseEntity.ok(token);
        }

        return ResponseEntity.status(401).build();
    }
}
