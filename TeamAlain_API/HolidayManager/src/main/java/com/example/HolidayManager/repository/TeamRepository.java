package com.example.HolidayManager.repository;

import com.example.HolidayManager.entity.TeamEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamRepository extends JpaRepository<TeamEntity, Long> {

    @Query("SELECT t " +
            "FROM TeamEntity t LEFT JOIN UserEntity u ON t.teamLead.id = u.id " +
            "WHERE u.id = ?1")
    TeamEntity findByTeamLeadId(Long teamLeadId);

    TeamEntity findByTeamName(String teamName);

    void deleteById(Long id);
}
