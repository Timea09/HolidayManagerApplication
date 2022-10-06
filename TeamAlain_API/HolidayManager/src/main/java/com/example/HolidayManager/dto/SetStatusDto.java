package com.example.HolidayManager.dto;

import com.example.HolidayManager.entity.Status;
import lombok.Data;

@Data
public class SetStatusDto {

    private Long userId;

    private Long holidayId;

    private Status status;

    private String extraInfo;
}
