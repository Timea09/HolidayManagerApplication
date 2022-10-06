package com.example.HolidayManager.dto;

import com.example.HolidayManager.entity.UserEntity;
import lombok.Data;

import java.util.List;

@Data
public class TeamCreateResponseMembersDto {

    private Long id;

    private String teamName;

    private UserDto teamLead;

    private List<UserDto> teamMembers;
    //private UserDto[] members;


    public TeamCreateResponseMembersDto(Long id, String teamName, UserEntity teamLead, List<UserDto> teamMembers) {

        this.id = id;
        this.teamName = teamName;
        this.teamLead =
                new UserDto(
                        teamLead.getId(),
                        teamLead.getFirstName(),
                        teamLead.getLastName(),
                        teamLead.getEmail(),
                        teamLead.getPassword(),
                        teamLead.getUserType(),
                        teamLead.getDepartment(),
                        teamLead.getDaysLeft(),
                        id,
                        teamLead.getRole()

                );

        //System.out.println(getTeamLead());
        //if(teamMembers.contains(getTeamLead())) System.out.println("true");

        this.teamMembers = teamMembers;
        //this.teamLead= getTeamLead();
       // this.teamMembers.remove(teamMembers);

    }


}
