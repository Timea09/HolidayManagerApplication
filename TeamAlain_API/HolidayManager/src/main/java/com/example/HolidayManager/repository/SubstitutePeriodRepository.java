package com.example.HolidayManager.repository;

import com.example.HolidayManager.entity.SubstitutePeriodEntity;
import com.example.HolidayManager.entity.TeamEntity;
import com.example.HolidayManager.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

public interface SubstitutePeriodRepository extends JpaRepository<SubstitutePeriodEntity, Long> {

    @Query("SELECT DISTINCT s.substitute " +
            "FROM SubstitutePeriodEntity s " +
            "WHERE " +
            "(" +
            "( (:startDate <= s.endDate AND :startDate >= s.startDate) AND (:endDate <= s.endDate AND :endDate >= s.startDate) ) OR " +
            "( (:startDate <= s.endDate AND :startDate >= s.startDate) AND (:endDate >= s.endDate) ) OR " +
            "( (:startDate <= s.startDate) AND (:endDate <= s.endDate AND :endDate >= s.startDate) OR " +
            "( (:startDate <= s.startDate AND :startDate <= s.endDate) AND (:endDate >= s.endDate AND :endDate >= s.startDate) ) )" +
            ")")
    List<UserEntity> findAllUnavailableTeamLeads(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT s " +
            "FROM SubstitutePeriodEntity s " +
            "WHERE " +
            "(" +
            "( (:userId = s.substitute.id AND :currentDate >= s.startDate AND :currentDate <= s.endDate) ) " +
            ")")
    SubstitutePeriodEntity findOnGoingSubstitutePeriod(Long userId, LocalDateTime currentDate);

    @Query("SELECT s " +
            "FROM SubstitutePeriodEntity s " +
            "WHERE " +
            "(" +
            "( (:teamId = s.team.id AND :currentDate >= s.startDate AND :currentDate <= s.endDate) ) " +
            ")")
    SubstitutePeriodEntity findOnGoingSubstitutePeriodTeam(Long teamId, LocalDateTime currentDate);

//find overlapping subs in team

    @Query("SELECT DISTINCT s.substitute " +
            "FROM SubstitutePeriodEntity  s " +
            "WHERE "+
            "(" +
            "( (:startDate <= s.endDate AND :startDate >= s.startDate) AND (:endDate <= s.endDate AND :endDate >= s.startDate) ) OR " +
            "( (:startDate <= s.endDate AND :startDate >= s.startDate) AND (:endDate >= s.endDate) ) OR " +
            "( (:startDate <= s.startDate) AND (:endDate <= s.endDate AND :endDate >= s.startDate) OR " +
            "( (:startDate <= s.startDate AND :startDate <= s.endDate) AND (:endDate >= s.endDate AND :endDate >= s.startDate) ) )" +
            ") AND s.team.id = :teamId "
    )
    UserEntity findOverlappingSubstitute(LocalDateTime startDate, LocalDateTime endDate, Long teamId);

    @Query("SELECT DISTINCT s " +
            "FROM SubstitutePeriodEntity s " +
            "WHERE " +
            "( :startDate = s.startDate AND :endDate = s.endDate AND :substitute =  s.substitute.firstName)"

    )
    SubstitutePeriodEntity findByNameAndStartDateAndEndDate(String substitute,LocalDateTime startDate, LocalDateTime endDate);

    @Transactional
    void deleteSubstitutePeriodEntitiesByStartDateAndEndDateAndTeamId(LocalDateTime startDate, LocalDateTime endDate, Long teamId);

    SubstitutePeriodEntity findSubstitutePeriodEntityByStartDateAndEndDateAndTeamId(LocalDateTime startDate, LocalDateTime endDate, Long teamId);

    List<SubstitutePeriodEntity> findAllBySubstituteId(Long substituteId);
}
