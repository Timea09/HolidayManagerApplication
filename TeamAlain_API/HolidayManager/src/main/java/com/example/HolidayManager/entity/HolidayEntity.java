package com.example.HolidayManager.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Table(name = "holidays")
public class HolidayEntity {

    @Id
    @Column(name = "holiday_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "holiday", fetch = FetchType.LAZY)
    private RestHolidayEntity restHoliday;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "holiday", fetch = FetchType.LAZY)
    private SpecialHolidayEntity specialHoliday;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "holiday", fetch = FetchType.LAZY)
    private UnpaidHolidayEntity unpaidHoliday;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "holiday", fetch = FetchType.LAZY)
    private List<NotificationEntity> notifications;
}
