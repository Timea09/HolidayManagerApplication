package com.example.HolidayManager.entity;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(name = "unpaid_holidays")
public class UnpaidHolidayEntity {

    @Id
    @Column(name = "holiday_id")
    private Long holidayId;

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "holiday_id", referencedColumnName = "holiday_id")
    private HolidayEntity holiday;
}
