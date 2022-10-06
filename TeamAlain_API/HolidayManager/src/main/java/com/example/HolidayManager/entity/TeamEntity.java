package com.example.HolidayManager.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "teams")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TeamEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "team_id")
    private Long id;

    @Column(name = "team_name")
    private String teamName;

    @OneToMany(mappedBy = "team")
    private List<UserEntity> teamMembers;

    @OneToMany(mappedBy = "substitute", cascade = CascadeType.ALL)
    private List<SubstitutePeriodEntity> substitutePeriods;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "team_lead_id", referencedColumnName = "user_id")
    private UserEntity teamLead;
}
