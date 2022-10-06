package com.example.HolidayManager.entity;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "special_holidays")
@Data
public class SpecialHolidayEntity {

    @Id
    @Column(name = "holiday_id")
    private Long holidayId;

    @Column(name = "substitute_name")
    private String substituteName;

    @Lob
    @Column(name = "document")
    private byte[] document;

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "holiday_id", referencedColumnName = "holiday_id")
    private HolidayEntity holiday;

    @Column(name = "document_name")
    private String documentName;
}
