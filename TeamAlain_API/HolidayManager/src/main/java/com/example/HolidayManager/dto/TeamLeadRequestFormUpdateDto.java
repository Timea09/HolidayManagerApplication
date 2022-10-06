package com.example.HolidayManager.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TeamLeadRequestFormUpdateDto {
    private Long holidayId;
    private String type;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime oldStartDate;
    private LocalDateTime oldEndDate;
    private Long substituteId;
    private byte[] document;
    private String documentName;
}
