package com.example.HolidayManager.service;

import com.example.HolidayManager.dto.TeamCreateRequestDto;
import com.example.HolidayManager.dto.TeamCreateResponseDto;
import com.example.HolidayManager.dto.TeamCreateResponseMembersDto;
import com.example.HolidayManager.dto.UserIdDto;
import com.example.HolidayManager.entity.SubstitutePeriodEntity;
import com.example.HolidayManager.entity.TeamEntity;
import com.example.HolidayManager.entity.UserEntity;
import com.example.HolidayManager.entity.UserType;
import com.example.HolidayManager.mapper.TeamMapper;
import com.example.HolidayManager.repository.SubstitutePeriodRepository;
import com.example.HolidayManager.repository.TeamRepository;
import com.example.HolidayManager.repository.UserRepository;
import com.example.HolidayManager.util.exceptions.TeamNameAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService{

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    private final SubstitutePeriodRepository substitutePeriodRepository;

    private final TeamMapper teamMapper;

    @Override
    public TeamCreateResponseMembersDto addTeam(TeamCreateRequestDto teamCreateRequestDto) throws TeamNameAlreadyExistsException {
        try {
            TeamEntity teamEntity = teamMapper.dtoToEntity(teamCreateRequestDto);
            //save the team in the database
            TeamEntity createdTeam = teamRepository.save(teamEntity);
            //update team foreign key for each team member
            for (UserEntity userTeamMember : createdTeam.getTeamMembers()) {
                userTeamMember.setTeam(createdTeam);
                userTeamMember.setUserType(UserType.EMPLOYEE);
                userRepository.save(userTeamMember);
            }
            //update team foreign key for team lead
            UserEntity userTeamLead = createdTeam.getTeamLead();
            userTeamLead.setTeam(createdTeam);
            userTeamLead.setUserType(UserType.TEAM_LEAD);
            userRepository.save(userTeamLead);

            return teamMapper.entityTodto(createdTeam);
        }
        catch (Exception e ){
            throw new TeamNameAlreadyExistsException("Team name already exists");
        }
    }

    @Override
    public TeamCreateResponseMembersDto updateTeam(TeamCreateRequestDto teamCreateRequestDto) throws TeamNameAlreadyExistsException {
            TeamEntity teamEntity = teamMapper.dtoToEntity(teamCreateRequestDto);
            TeamEntity tryToFind = teamRepository.findByTeamName(teamEntity.getTeamName());
            if(tryToFind==null || tryToFind.getId().equals(teamEntity.getId())) {
                Optional<TeamEntity> optionalFoundTeamEntity = teamRepository.findById(teamEntity.getId());
                if (optionalFoundTeamEntity.isPresent()) {
                    TeamEntity foundTeamEntity = optionalFoundTeamEntity.get();
                    //unnassign previous team members from team and transform them to employee
                    for (UserEntity exUserTeamMember : foundTeamEntity.getTeamMembers()) {
                        exUserTeamMember.setTeam(null);
                        userRepository.save(exUserTeamMember);
                    }
                    //unnassign previous team leader from team and transform it to employee
                    UserEntity exUserTeamLead = foundTeamEntity.getTeamLead();
                    exUserTeamLead.setTeam(null);
                    exUserTeamLead.setUserType(UserType.EMPLOYEE);
                    userRepository.save(exUserTeamLead);

                    //update the team in the database
                    TeamEntity createdTeam = teamRepository.save(teamEntity);
                    //update team foreign key for each team member
                    for (UserEntity userTeamMember : createdTeam.getTeamMembers()) {
                        userTeamMember.setTeam(createdTeam);
                        userTeamMember.setUserType(UserType.EMPLOYEE);
                        userRepository.save(userTeamMember);
                    }
                    //update team foreign key for team lead
                    UserEntity userTeamLead = createdTeam.getTeamLead();
                    userTeamLead.setTeam(createdTeam);
                    userTeamLead.setUserType(UserType.TEAM_LEAD);
                    userRepository.save(userTeamLead);

                    return teamMapper.entityTodto(createdTeam);

                }
            }
            else{
                throw new TeamNameAlreadyExistsException("Team name already exists");
            }

        return null;
    }

    @Override
    public TeamEntity getById(Long teamId) {
        return teamRepository.getById(teamId);
    }

    @Override
    public void deleteTeamById(Long id) {

        TeamEntity teamEntity = teamRepository.getById(id);

        List<UserEntity> teamMembers = teamEntity.getTeamMembers();
        List<SubstitutePeriodEntity> periods = teamEntity.getSubstitutePeriods();

        teamMembers.forEach(userEntity -> {
            userEntity.setTeam(null);
            userRepository.save(userEntity);
        });

        teamMembers.clear();

        substitutePeriodRepository.findAll().forEach(substitutePeriodEntity -> {
            if(substitutePeriodEntity.getTeam().getId().equals(id)){
                substitutePeriodRepository.delete(substitutePeriodEntity);
            }
        });

        periods.clear();

        UserEntity teamLead = teamEntity.getTeamLead();

        teamLead.setTeam(null);
        teamLead.setUserType(UserType.EMPLOYEE);

        userRepository.save(teamLead);

        teamEntity.setTeamLead(null);

        teamRepository.deleteById(id);
    }


    @Override
    public List<TeamCreateResponseMembersDto> getTeams() {
        List<TeamEntity> team =teamRepository.findAll();
       return teamMapper.entitiesTodtos(team);

    }


    @Override
    public Long findTeamByTeamLeadId(Long userId) {
        TeamEntity team= this.teamRepository.findByTeamLeadId(userId);
        return team.getId();
    }
}
