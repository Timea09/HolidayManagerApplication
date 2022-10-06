package com.example.HolidayManager.service;

import com.example.HolidayManager.dto.TeamCreateRequestDto;
import com.example.HolidayManager.dto.TeamCreateResponseDto;
import com.example.HolidayManager.dto.TeamCreateResponseMembersDto;
import com.example.HolidayManager.dto.UserIdDto;
import com.example.HolidayManager.entity.TeamEntity;
import com.example.HolidayManager.entity.UserEntity;
import com.example.HolidayManager.util.exceptions.TeamNameAlreadyExistsException;

import java.util.List;

public interface TeamService {


    TeamCreateResponseMembersDto addTeam(TeamCreateRequestDto teamCreateRequestDto) throws TeamNameAlreadyExistsException;
    TeamCreateResponseMembersDto updateTeam(TeamCreateRequestDto teamCreateRequestDto) throws TeamNameAlreadyExistsException;
    TeamEntity getById(Long teamId);
    List<TeamCreateResponseMembersDto> getTeams();
    void deleteTeamById(Long id);

    Long findTeamByTeamLeadId(Long userId);

}
