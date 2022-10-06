package com.example.HolidayManager.service;

import com.example.HolidayManager.dto.SubstituteIdDto;
import com.example.HolidayManager.dto.UserDto;
import com.example.HolidayManager.entity.*;
import com.example.HolidayManager.mapper.UserMapper;
import com.example.HolidayManager.repository.HolidayRepository;
import com.example.HolidayManager.repository.RequestRepository;
import com.example.HolidayManager.repository.SubstitutePeriodRepository;
import com.example.HolidayManager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubstituteServiceImpl implements SubstituteService {

    private final UserRepository userRepository;
    private final RequestRepository requestRepository;

    private final SubstitutePeriodRepository substitutePeriodRepository;

    private final UserMapper userMapper;

    private final HolidayRepository holidayRepository;
    @Override
    public List<UserDto> findAllAvailableTeamLeads(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
//team id -> team lead -> indAllUnavailableTeamLeads (end,start, team lead id)
        List<UserEntity> teamLeads =
                userRepository.findAllByUserType(UserType.TEAM_LEAD)
                        .stream().filter(u -> !Objects.equals(u.getId(), userId)).collect(Collectors.toList());

        List<UserEntity> teamLeadsOnHoliday = requestRepository.findAllTeamLeadsOnHoliday(startDate, endDate);

        List<UserEntity> unavailableTeamLeads = substitutePeriodRepository.findAllUnavailableTeamLeads(startDate, endDate);

        List<UserEntity> availableTeamLeads =
                teamLeads.stream()
                        .filter(u -> !teamLeadsOnHoliday.contains(u) && !unavailableTeamLeads.contains(u))
                        .collect(Collectors.toList());

        return userMapper.entitiesToDtos(availableTeamLeads);
    }

    @Override
    public List<UserDto> findAllAvailableTeamLeadsForUpdate(Long userId, LocalDateTime startDate, LocalDateTime endDate, Long teamId) {
//team id -> team lead -> indAllUnavailableTeamLeads (end,start, team lead id)
        List<UserEntity> teamLeads =
                userRepository.findAllByUserType(UserType.TEAM_LEAD)
                        .stream().filter(u -> !Objects.equals(u.getId(), userId)).collect(Collectors.toList());

        List<UserEntity> teamLeadsOnHoliday = requestRepository.findAllTeamLeadsOnHoliday(startDate, endDate);

        List<UserEntity> unavailableTeamLeads = substitutePeriodRepository.findAllUnavailableTeamLeads(startDate, endDate);

        List<UserEntity> availableTeamLeads =
                teamLeads.stream()
                        .filter(u -> !teamLeadsOnHoliday.contains(u) && !unavailableTeamLeads.contains(u))
                        .collect(Collectors.toList());

        UserEntity currentSubstitute = substitutePeriodRepository.findOverlappingSubstitute(startDate,endDate,teamId);

        if (currentSubstitute != null) {

            availableTeamLeads.add(currentSubstitute);
        }

        return userMapper.entitiesToDtos(availableTeamLeads);
    }

    @Override
    public boolean isUserAvailable(Long userId, LocalDateTime startDate, LocalDateTime endDate) {

        List<UserEntity> unavailableTeamLeads = substitutePeriodRepository.findAllUnavailableTeamLeads(startDate, endDate);

        return unavailableTeamLeads.stream().noneMatch(u -> u.getId().equals(userId));
    }

    @Override
    public boolean isUserSubstitute(Long userId) {

        LocalDateTime startDate = LocalDate.now().atStartOfDay();

        return !this.isUserAvailable(userId, startDate, startDate);
    }

    @Override
    public SubstitutePeriodEntity findUserOnGoingSubstitutePeriod(Long userId) {

//        if (this.substitutePeriodRepository.findAllBySubstituteId(userId).isEmpty()) {
//
//            return null;
//        }
//        else {
//
//            LocalDateTime currentDate = LocalDate.now().atStartOfDay();
//
//            SubstitutePeriodEntity period = this.substitutePeriodRepository.findOnGoingSubstitutePeriod(userId, currentDate);
//
//            return Objects.requireNonNullElseGet(period, SubstitutePeriodEntity::new);
//        }

        LocalDateTime currentDate = LocalDate.now().atStartOfDay();

        return this.substitutePeriodRepository.findOnGoingSubstitutePeriod(userId, currentDate);
    }

    @Override
    public boolean isUserOnHoliday(Long userId, LocalDateTime startDate, LocalDateTime endDate) {

        List<UserEntity> teamLeadsOnHoliday = requestRepository.findAllTeamLeadsOnHoliday(startDate, endDate);

        return teamLeadsOnHoliday.stream().anyMatch(u -> u.getId().equals(userId));
    }

    @Override
    public SubstituteIdDto findSubstitute(Long teamId, Long holidayId) {
        HolidayEntity holiday= this.holidayRepository.findById(holidayId).get();
        LocalDateTime startDate = holiday.getStartDate();
        LocalDateTime endDate= holiday.getEndDate();
        SubstitutePeriodEntity periodEntity = this.substitutePeriodRepository.findSubstitutePeriodEntityByStartDateAndEndDateAndTeamId(startDate,endDate,teamId);
        return new SubstituteIdDto(periodEntity.getSubstitute().getId());
    }
}
