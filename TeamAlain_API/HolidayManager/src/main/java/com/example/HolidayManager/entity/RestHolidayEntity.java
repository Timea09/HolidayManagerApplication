package com.example.HolidayManager.entity;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "rest_holidays")
@Data
public class RestHolidayEntity {

    @Id
    @Column(name = "holiday_id")
    private Long holidayId;

    @Column(name = "substitute_name")
    private String substituteName;

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "holiday_id", referencedColumnName = "holiday_id")
    private HolidayEntity holiday;
}
