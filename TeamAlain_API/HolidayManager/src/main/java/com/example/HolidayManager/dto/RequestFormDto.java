package com.example.HolidayManager.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RequestFormDto {

    private Long holidayId;
    private String type;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String substitute;
    private byte[] document;
    private String documentName;
}
