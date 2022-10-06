package com.example.HolidayManager.controller;

import com.example.HolidayManager.dto.NotificationRequestDto;
import com.example.HolidayManager.dto.NotificationResponseDto;
import com.example.HolidayManager.security.JwtTokenService;
import com.example.HolidayManager.service.NotificationService;
import com.example.HolidayManager.util.annotations.AllowAll;
import com.example.HolidayManager.util.annotations.AllowTeamLeadAndEmployee;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/notification")
@RequiredArgsConstructor
public class NotificationController {

private final NotificationService notificationService;
private final JwtTokenService jwtTokenService;



    @AllowTeamLeadAndEmployee
    @GetMapping("/get-notifications")
    public ResponseEntity<List<NotificationResponseDto>> getAllMine(@RequestHeader("app-auth") String token){

        Long userId = jwtTokenService.getUserIdFromToken(token);
        return ResponseEntity.ok(notificationService.findByUserTo(userId));}


    @AllowTeamLeadAndEmployee
    @PutMapping("/read-notifications")
    public ResponseEntity<String> markAsRead(@RequestHeader("app-auth") String token)
    {
        Long userId = jwtTokenService.getUserIdFromToken(token);
        notificationService.markAsRead(userId);

        return ResponseEntity.ok("");
    }
}
