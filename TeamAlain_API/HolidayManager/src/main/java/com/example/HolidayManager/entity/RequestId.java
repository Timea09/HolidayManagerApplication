package com.example.HolidayManager.entity;

import lombok.Data;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@Data
public class RequestId implements Serializable {

    private Long userId;
    private Long holidayId;
}
