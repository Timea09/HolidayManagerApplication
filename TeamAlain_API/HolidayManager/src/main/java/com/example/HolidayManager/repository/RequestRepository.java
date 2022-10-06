package com.example.HolidayManager.repository;

import com.example.HolidayManager.entity.HolidayEntity;
import com.example.HolidayManager.entity.RequestEntity;
import com.example.HolidayManager.entity.UserEntity;
import com.example.HolidayManager.entity.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

public interface RequestRepository extends JpaRepository<RequestEntity, Long> {

    List<RequestEntity> findByUserEmail(String email);

    @Query("SELECT r " +
            "FROM RequestEntity r " +
            "WHERE r.user.team.id = ?1 ")
    List<RequestEntity> findByUserTeamId(Long teamId);

    @Query("SELECT r " +
            "FROM RequestEntity r " +
            "WHERE r.user.id = ?1 ")
    List<RequestEntity> findByUserId(Long userId);

    @Query("SELECT r " +
            "FROM RequestEntity r " +
            "WHERE r.user.id = ?1 AND (r.status = 'APPROVED' OR r.status = 'PENDING')")
    List<RequestEntity> findApprovedOrPendingByUserId(Long userId);

    @Query("SELECT r.holiday " +
            "FROM RequestEntity r " +
            "WHERE r.userId = :userId AND r.status <> 'DECLINED' AND " +
            "(" +
            "( (:startDate <= r.holiday.endDate AND :startDate >= r.holiday.startDate) AND (:endDate <= r.holiday.endDate AND :endDate >= r.holiday.startDate) ) OR " +
            "( (:startDate <= r.holiday.endDate AND :startDate >= r.holiday.startDate) AND (:endDate >= r.holiday.endDate) ) OR " +
            "( (:startDate <= r.holiday.startDate) AND (:endDate <= r.holiday.endDate AND :endDate >= r.holiday.startDate) OR " +
            "( (:startDate <= r.holiday.startDate AND :startDate <= r.holiday.endDate) AND (:endDate >= r.holiday.endDate AND :endDate >= r.holiday.startDate) ) )" +
            ")"
    )
    List<HolidayEntity> findOverlappingRequests(@Param("userId") Long userId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT r.holiday " +
            "FROM RequestEntity r " +
            "WHERE r.userId = :userId AND r.status <> 'DECLINED' AND " +
            "(" +
            "( (:startDate <= r.holiday.endDate AND :startDate >= r.holiday.startDate) AND (:endDate <= r.holiday.endDate AND :endDate >= r.holiday.startDate) ) OR " +
            "( (:startDate <= r.holiday.endDate AND :startDate >= r.holiday.startDate) AND (:endDate >= r.holiday.endDate) ) OR " +
            "( (:startDate <= r.holiday.startDate) AND (:endDate <= r.holiday.endDate AND :endDate >= r.holiday.startDate) OR " +
            "( (:startDate <= r.holiday.startDate AND :startDate <= r.holiday.endDate) AND (:endDate >= r.holiday.endDate AND :endDate >= r.holiday.startDate) ) )" +
            ") AND r.holidayId <> :holidayId"
    )
    List<HolidayEntity> findOverlappingRequestsExceptForCurrentRequest(@Param("userId") Long userId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, @Param("holidayId") Long holidayId);

    RequestEntity findByUserIdAndHolidayId(Long userId, Long holidayId);


    @Query("SELECT r FROM RequestEntity r WHERE r.holidayId = ?1")
    List<RequestEntity> findAllByHolidayId(Long id);

    List<RequestEntity> findByUserIdAndStatusNot(Long userId, Status status);
    void deleteByUserId(Long userId);

    @Query("SELECT DISTINCT r.user " +
            "FROM RequestEntity r " +
            "WHERE r.user.userType = 'TEAM_LEAD' AND " +
            "(" +
            "( (:startDate <= r.holiday.endDate AND :startDate >= r.holiday.startDate) AND (:endDate <= r.holiday.endDate AND :endDate >= r.holiday.startDate) ) OR " +
            "( (:startDate <= r.holiday.endDate AND :startDate >= r.holiday.startDate) AND (:endDate >= r.holiday.endDate) ) OR " +
            "( (:startDate <= r.holiday.startDate) AND (:endDate <= r.holiday.endDate AND :endDate >= r.holiday.startDate) OR " +
            "( (:startDate <= r.holiday.startDate AND :startDate <= r.holiday.endDate) AND (:endDate >= r.holiday.endDate AND :endDate >= r.holiday.startDate) ) )" +
            ")"
    )
    List<UserEntity> findAllTeamLeadsOnHoliday(LocalDateTime startDate, LocalDateTime endDate);
}
