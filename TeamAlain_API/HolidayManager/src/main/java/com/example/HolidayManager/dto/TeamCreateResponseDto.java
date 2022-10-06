package com.example.HolidayManager.dto;

import lombok.Data;

@Data
public class TeamCreateResponseDto {

    private Long id;

    private String teamName;

    private UserDto teamLead;
}
