package com.example.HolidayManager.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@NoArgsConstructor
@IdClass(RequestId.class)
@Table(name = "requests")
public class RequestEntity {

    @Id
    @Column(name = "user_id")
    private Long userId;
    @Id
    @Column(name = "holiday_id")
    private Long holidayId;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "extra_info")
    private String extraInfo;

    public RequestEntity(Long userId, Long holidayId, Status status) {

        this.userId = userId;
        this.holidayId = holidayId;
        this.status = status;
    }

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", insertable = false, updatable = false)
    private UserEntity user;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "holiday_id", referencedColumnName = "holiday_id", insertable = false, updatable = false)
    private HolidayEntity holiday;
}
