package com.example.HolidayManager.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TeamLeadRequestFormDto {

    private Long holidayId;
    private String type;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Long substituteId;
    private byte[] document;
    private String documentName;
}
