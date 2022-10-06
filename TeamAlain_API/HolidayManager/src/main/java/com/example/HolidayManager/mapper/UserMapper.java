package com.example.HolidayManager.mapper;

import com.example.HolidayManager.dto.LoginResponseDto;
import com.example.HolidayManager.dto.UserDto;
import com.example.HolidayManager.dto.UserIdDto;
import com.example.HolidayManager.dto.UserResponseDto;
import com.example.HolidayManager.entity.UserEntity;
import com.example.HolidayManager.service.TeamService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    LoginResponseDto entityToResponseDto(UserEntity userEntity);

    @Mapping(target = "teamId", source = "team.id")
    UserDto entityToDto(UserEntity userEntity);

    UserEntity dtoToEntity(UserDto userDto);

    UserIdDto dtoToEntity(UserEntity userEntity);

    List<UserDto> entitiesToDtos(List<UserEntity> userEntities);

    List<UserResponseDto> responseEntitiesToDtos(List<UserEntity> userEntities);

    List<UserEntity> dtosToEntities(List<UserDto> userEntities);
    List<UserEntity> ResponseDtosToEntities(List<UserResponseDto> userEntities);


}
