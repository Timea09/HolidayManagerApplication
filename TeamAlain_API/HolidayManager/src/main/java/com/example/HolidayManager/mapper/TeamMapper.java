package com.example.HolidayManager.mapper;

import com.example.HolidayManager.dto.TeamCreateRequestDto;
import com.example.HolidayManager.dto.TeamCreateResponseDto;
import com.example.HolidayManager.dto.TeamCreateResponseMembersDto;
import com.example.HolidayManager.entity.TeamEntity;
import com.example.HolidayManager.service.UserService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {UserMapper.class, UserService.class})
public interface TeamMapper {

    TeamCreateResponseDto entityToDto(TeamEntity teamEntity);
    TeamCreateResponseMembersDto entityTodto(TeamEntity teamEntity);

    List<TeamCreateResponseMembersDto> entitiesTodtos(List<TeamEntity> teamEntities);

    List<TeamCreateResponseDto> entitiesToDtos(List<TeamEntity> teamEntities);

    TeamEntity dtoToEntity(TeamCreateResponseDto teamDto);

    //se va cauta o metoda in UserService care mapeaza un Long intr un obiect de tip UserEntity (metoda getById)
    @Mapping(target = "teamLead",source = "teamLeadId")
    @Mapping(target = "teamMembers",source = "teamMembersId")
    TeamEntity dtoToEntity(TeamCreateRequestDto teamCreateRequestDto);



}
