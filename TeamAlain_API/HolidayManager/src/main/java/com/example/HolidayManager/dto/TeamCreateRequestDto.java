package com.example.HolidayManager.dto;

import lombok.Data;

import java.util.List;

@Data
public class TeamCreateRequestDto {

    private Long id;

    private String teamName;

    private Long teamLeadId;

    private List<UserIdDto> teamMembersId;



}
