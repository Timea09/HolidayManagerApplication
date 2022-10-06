package com.example.HolidayManager.entity;

import lombok.Data;
import lombok.ToString;
import org.springframework.boot.autoconfigure.cassandra.CassandraProperties;

import javax.persistence.*;

@Data
@Entity
@Table(name="notifications")

public class NotificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long notificationId;

    @ManyToOne
    @JoinColumn(name = "from_id", referencedColumnName = "user_id")
    @ToString.Exclude
    private UserEntity userFrom;

    @ManyToOne
    @JoinColumn(name = "to_id", referencedColumnName = "user_id")
    @ToString.Exclude
    private UserEntity userTo;


    //actually holidayId that will be connected to the requests table
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "request_id", referencedColumnName = "holiday_id")
    @ToString.Exclude
    private HolidayEntity holiday;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private NotificationStatus status;

    @Column(name = "message")
    private String message;




}
