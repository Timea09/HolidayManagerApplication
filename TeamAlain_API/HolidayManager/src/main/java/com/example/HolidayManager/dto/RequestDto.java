package com.example.HolidayManager.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RequestDto {

    private Long userId;
    private Long holidayId;
    private String email;
    private String firstName;
    private String lastName;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String type;
    private String status;
    private String substitute;
    private byte[] document;
    private String documentName;
    private String extraInfo;
}
