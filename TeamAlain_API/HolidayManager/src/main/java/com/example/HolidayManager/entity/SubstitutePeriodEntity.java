package com.example.HolidayManager.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "substitute_periods")
@Data
public class SubstitutePeriodEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "period_id")
    private Long periodId;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @ManyToOne
    @JoinColumn(name = "substitute_id")
    private UserEntity substitute;

    @ManyToOne
    @JoinColumn(name = "team_id")
    private TeamEntity team;
}
