package com.example.HolidayManager.service;

import com.example.HolidayManager.dto.SubstituteIdDto;
import com.example.HolidayManager.dto.UserDto;
import com.example.HolidayManager.entity.SubstitutePeriodEntity;

import java.time.LocalDateTime;
import java.util.List;

public interface SubstituteService {

    List<UserDto> findAllAvailableTeamLeads(Long userId, LocalDateTime startDate, LocalDateTime endDate);

    List<UserDto> findAllAvailableTeamLeadsForUpdate(Long userId, LocalDateTime startDate, LocalDateTime endDate, Long teamId);
    
    boolean isUserAvailable(Long userId, LocalDateTime startDate, LocalDateTime endDate);

    boolean isUserSubstitute(Long userId);

    SubstitutePeriodEntity findUserOnGoingSubstitutePeriod(Long userId);

    boolean isUserOnHoliday(Long userId, LocalDateTime startDate, LocalDateTime endDate);

    SubstituteIdDto findSubstitute(Long teamId, Long holidayId);
}
