package com.example.HolidayManager.service;

import com.example.HolidayManager.dto.*;
import com.example.HolidayManager.entity.*;
import com.example.HolidayManager.mapper.RequestMapper;
import com.example.HolidayManager.mapper.UserMapper;
import com.example.HolidayManager.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {

    private final UserRepository userRepository;

    private final RequestRepository requestRepository;

    private final TeamRepository teamRepository;

    private final SubstitutePeriodRepository substitutePeriodRepository;

    private final HolidayRepository holidayRepository;

    private final RestHolidayRepository restHolidayRepository;

    private final SpecialHolidayRepository specialHolidayRepository;

    private final UnpaidHolidayRepository unpaidHolidayRepository;

    private final RequestMapper requestMapper;

    private final UserMapper userMapper;

    private final NotificationService notificationService;

    private final WebSocketService webSocketService;


    @Override
    @Transactional
    public List<RequestDto> findByUserId(Long id) {

        List<RequestEntity> requests = requestRepository.findByUserId(id);

        return requests.stream()
                .map(requestMapper::entityToDto)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    @Transactional
    public List<RequestDto> findByEmail(String email) {

        List<RequestEntity> requests = requestRepository.findByUserEmail(email);

        return requests.stream().map(requestMapper::entityToDto).collect(java.util.stream.Collectors.toList());
    }

    @Override
    @Transactional
    public List<RequestDto> findByTeamId(Long teamLeadId, Long teamId) {

        List<RequestEntity> requests = requestRepository.findByUserTeamId(teamId);

        return requests.stream()
                .map(requestMapper::entityToDto)
                .filter( r -> !r.getUserId().equals(teamLeadId))
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    @Transactional
    public List<RequestDto> findByTeamIdAndStatus(Long teamLeadId, Long teamId, Status status) {

        List<RequestEntity> requests = requestRepository.findByUserTeamId(teamId);

        return requests.stream()
                .map(requestMapper::entityToDto)
                .filter( r -> r.getStatus().equals(status.toString()) && !r.getUserId().equals(teamLeadId))
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    @Transactional
    public List<RequestDto> findRestHolidaysByUserId(Long id) {

        return findByUserId(id).stream().filter(request -> request.getType().equals("Rest Holiday")).collect(java.util.stream.Collectors.toList());
    }

    @Override
    @Transactional
    public List<RequestDto> findSpecialHolidaysByUserId(Long id) {

        findByUserId(id).stream().filter(request -> request.getType().equals("Special Holiday")).collect(java.util.stream.Collectors.toList())
                .forEach(System.out::println);
    return findByUserId(id).stream().filter(request -> request.getType().equals("Special Holiday")).collect(java.util.stream.Collectors.toList());
    }

    @Override
    @Transactional
    public List<RequestDto> findUnpaidHolidaysByUserId(Long id) {

        return findByUserId(id).stream().filter(request -> request.getType().equals("Unpaid Holiday")).collect(java.util.stream.Collectors.toList());
    }

    @Override
    @Transactional
    public boolean checkForOverlappingRequests(Long userId, RequestFormDto requestFormDto) {

        LocalDateTime startDate = requestFormDto.getStartDate();
        LocalDateTime endDate = requestFormDto.getEndDate();

        List<HolidayEntity> overlappingHolidays = requestRepository.findOverlappingRequests(userId, startDate, endDate);

        return !overlappingHolidays.isEmpty();
    }

    @Override
    @Transactional
    public boolean checkForOverlappingRequestsExceptForCurrentRequest(Long userId, RequestFormDto requestFormDto) {

        LocalDateTime startDate = requestFormDto.getStartDate();
        LocalDateTime endDate = requestFormDto.getEndDate();
        Long requestId = requestFormDto.getHolidayId();

        List<HolidayEntity> overlappingHolidays = requestRepository.findOverlappingRequestsExceptForCurrentRequest(userId, startDate, endDate, requestId);

        return !overlappingHolidays.isEmpty();
    }


    /**
     * This method is used to make a request for any holiday type.
     * Makes a new RequestEntity to be linked to the UserEntity with the given id, sets the status to "PENDING".
     * Makes a new HolidayEntity to be linked to the RequestEntity and sets the start and end date.
     * Makes a new Rest/Special/UnpaidHolidayEntity to be linked to the HolidayEntity and sets the substitute name and document where applicable.
     *
     * @param userId - The id of the user who makes the request.
     * @param requestFormDto - The request form data. This should contain the following:
     *                       type, startDate, endDate, extraInfo,
     *                       substitute (exclusively for type == "REST" || type == "SPECIAL"),
     *                       document (exclusively for type == "SPECIAL").
     *
     * @return A dto of the newly added request.
     */
    @Override
    @Transactional
    public RequestDto makeRequest(Long userId, RequestFormDto requestFormDto) {

        String type = requestFormDto.getType();
        RequestEntity request = new RequestEntity();

        request.setUserId(userId);
        request.setStatus(Status.PENDING);

        request.setUser(userRepository.getById(userId));

        HolidayEntity holiday = new HolidayEntity();

        holiday.setStartDate(requestFormDto.getStartDate());
        holiday.setEndDate(requestFormDto.getEndDate());

        HolidayEntity holidayEntity = holidayRepository.save(holiday);

        switch (type) {

            case "REST": {

                RestHolidayEntity restHoliday = new RestHolidayEntity();

                restHoliday.setHolidayId(holidayEntity.getId());
                restHoliday.setSubstituteName(requestFormDto.getSubstitute());
                restHoliday.setHoliday(holidayEntity);

                restHoliday = restHolidayRepository.save(restHoliday);

                holiday.setRestHoliday(restHoliday);

                break;
            }

            case "SPECIAL": {

                SpecialHolidayEntity specialHoliday = new SpecialHolidayEntity();

                specialHoliday.setHolidayId(holidayEntity.getId());
                specialHoliday.setSubstituteName(requestFormDto.getSubstitute());
                specialHoliday.setDocument(requestFormDto.getDocument());
                specialHoliday.setDocumentName(requestFormDto.getDocumentName());
                specialHoliday.setHoliday(holidayEntity);

                specialHoliday = specialHolidayRepository.save(specialHoliday);

                holiday.setSpecialHoliday(specialHoliday);

                break;
            }
            case "UNPAID": {

                UnpaidHolidayEntity unpaidHoliday = new UnpaidHolidayEntity();

                unpaidHoliday.setHolidayId(holidayEntity.getId());
                unpaidHoliday.setHoliday(holidayEntity);

                unpaidHoliday = unpaidHolidayRepository.save(unpaidHoliday);

                holiday.setUnpaidHoliday(unpaidHoliday);

                break;
            }
        }

        holidayRepository.save(holiday);

        request.setHolidayId(holidayEntity.getId());
        request.setHoliday(holiday);

        //user-from= current user
        //user-to= current user.teamId.teamLead.id
        //status= UNREAD
        //holiday id= din request

        NotificationRequestDto notificationDto = new NotificationRequestDto();
        notificationDto.setUserFrom(request.getUser().getId());

        SubstitutePeriodEntity substitutePeriod = substitutePeriodRepository.findOnGoingSubstitutePeriodTeam(request.getUser().getTeam().getId(), LocalDate.now().atStartOfDay());

        UserEntity teamLead;

        if (substitutePeriod != null) {

            teamLead = substitutePeriod.getSubstitute();
        }
        else {

            teamLead = request.getUser().getTeam().getTeamLead();
        }

        notificationDto.setUserTo(teamLead.getId());
        notificationDto.setStatus(NotificationStatus.UNREAD);
        notificationDto.setHolidayId(request.getHolidayId());
        notificationDto.setHolidayType(getHolidayType(request.getHoliday()));
        notificationDto.setRequestStatus(request.getStatus());
        notificationService.add(notificationDto);

        notifyFrontend();

        return requestMapper.entityToDto(requestRepository.save(request));
    }

    @Override
    @Transactional
    public RequestDto makeRequestTeamLead(Long userId, TeamLeadRequestFormDto requestFormDto) {

        String type = requestFormDto.getType();
        RequestEntity request = new RequestEntity();

        request.setUserId(userId);
        request.setStatus(Status.APPROVED);

        request.setUser(userRepository.getById(userId));

        HolidayEntity holiday = new HolidayEntity();

        holiday.setStartDate(requestFormDto.getStartDate());
        holiday.setEndDate(requestFormDto.getEndDate());

        HolidayEntity holidayEntity = holidayRepository.save(holiday);



        UserEntity substitute = userRepository.getById(requestFormDto.getSubstituteId());

        switch (type) {

            case "REST": {

                RestHolidayEntity restHoliday = new RestHolidayEntity();

                restHoliday.setHolidayId(holidayEntity.getId());
                restHoliday.setSubstituteName(substitute.getFirstName() + " " + substitute.getLastName());
                restHoliday.setHoliday(holidayEntity);

                restHoliday = restHolidayRepository.save(restHoliday);

                holiday.setRestHoliday(restHoliday);

                break;
            }

            case "SPECIAL": {

                SpecialHolidayEntity specialHoliday = new SpecialHolidayEntity();

                specialHoliday.setHolidayId(holidayEntity.getId());
                specialHoliday.setSubstituteName(substitute.getFirstName() + " " + substitute.getLastName());
                specialHoliday.setDocument(requestFormDto.getDocument());
                specialHoliday.setDocumentName(requestFormDto.getDocumentName());
                specialHoliday.setHoliday(holidayEntity);

                specialHoliday = specialHolidayRepository.save(specialHoliday);

                holiday.setSpecialHoliday(specialHoliday);

                break;
            }
            case "UNPAID": {

                UnpaidHolidayEntity unpaidHoliday = new UnpaidHolidayEntity();

                unpaidHoliday.setHolidayId(holidayEntity.getId());
                unpaidHoliday.setHoliday(holidayEntity);

                unpaidHoliday = unpaidHolidayRepository.save(unpaidHoliday);

                holiday.setUnpaidHoliday(unpaidHoliday);

                break;
            }
        }
        HolidayEntity hEntity= holidayRepository.save(holiday);
        System.out.println(hEntity.getId());

        request.setHolidayId(holidayEntity.getId());
        request.setHoliday(holiday);

        SubstitutePeriodEntity substitutePeriod = new SubstitutePeriodEntity();
        substitutePeriod.setSubstitute(substitute);
        substitutePeriod.setStartDate(requestFormDto.getStartDate());
        substitutePeriod.setEndDate(requestFormDto.getEndDate());
        TeamEntity team = this.userRepository.findById(userId).get().getTeam();
        substitutePeriod.setTeam(team);


        substitutePeriodRepository.save(substitutePeriod);

        return requestMapper.entityToDto(requestRepository.save(request));
    }

    private String getHolidayType(HolidayEntity holiday) {

        String type;

        if (holiday.getRestHoliday() != null) {

            type = "REST";
        }
        else if (holiday.getSpecialHoliday() != null) {

            type = "SPECIAL";
        }
        else {

            type = "UNPAID";
        }

        return type;
    }

    /**
     * This method is used to update the holiday details from a request of a user with the given id.
     * The existing RequestEntity is identified by the received user id and holiday id. Its extra info is updated.
     * The linked HolidayEntity will be updated with the new start and end date.
     * The method will determine whether the new holiday type is the same as the existing type.
     * If so, the linked Rest/Special/UnpaidHolidayEntity will be updated with the new substitute name and document where applicable.
     * If not, the linked Rest/Special/UnpaidHolidayEntity will be deleted,
     * and a new one will be created with the newly received substitute name and document where applicable.
     *
     * @param userId - The id of the user who makes the request.
     * @param requestFormDto - The request form data. This should contain the following:
     *      *                holidayId, type, startDate, endDate, extraInfo,
     *      *                substitute (exclusively for type == "REST" || type == "SPECIAL"),
     *      *                document (exclusively for type == "SPECIAL").
     *
     * @return A dto of the updated request.
     */
    @Override
    @Transactional
    public RequestDto updateRequest(Long userId, UserType userType, RequestFormDto requestFormDto) {

        String newType = requestFormDto.getType();
        LocalDateTime startDate = requestFormDto.getStartDate();
        LocalDateTime endDate = requestFormDto.getEndDate();

        RequestEntity request = requestRepository.findByUserIdAndHolidayId(userId, requestFormDto.getHolidayId());

        request.setStatus(Status.PENDING);


        HolidayEntity modifiedHoliday = holidayRepository.getById(requestFormDto.getHolidayId());

        modifiedHoliday.setStartDate(startDate);
        modifiedHoliday.setEndDate(endDate);

        String oldType = getHolidayType(modifiedHoliday);

        if (Objects.equals(newType, oldType)) {

            switch (newType) {

                case "REST": {

                    RestHolidayEntity restHoliday = modifiedHoliday.getRestHoliday();
                    restHoliday.setSubstituteName(requestFormDto.getSubstitute());
                    restHolidayRepository.save(restHoliday);

                    modifiedHoliday.setRestHoliday(restHoliday);
                    break;
                }
                case "SPECIAL": {

                    SpecialHolidayEntity specialHoliday = modifiedHoliday.getSpecialHoliday();
                    specialHoliday.setSubstituteName(requestFormDto.getSubstitute());
                    specialHoliday.setDocument(requestFormDto.getDocument());
                    specialHoliday.setDocumentName(requestFormDto.getDocumentName());
                    specialHolidayRepository.save(specialHoliday);

                    modifiedHoliday.setSpecialHoliday(specialHoliday);
                    break;
                }
                case "UNPAID": {

                    UnpaidHolidayEntity unpaidHoliday = modifiedHoliday.getUnpaidHoliday();
                    unpaidHolidayRepository.save(unpaidHoliday);

                    modifiedHoliday.setUnpaidHoliday(unpaidHoliday);
                    break;
                }
            }
        }
        else {

            switch (oldType) {

                case "REST": {

                    RestHolidayEntity restHoliday = modifiedHoliday.getRestHoliday();

                    modifiedHoliday.setRestHoliday(null);
                    restHolidayRepository.delete(restHoliday);
                    break;
                }
                case "SPECIAL": {

                    SpecialHolidayEntity specialHoliday = modifiedHoliday.getSpecialHoliday();

                    modifiedHoliday.setSpecialHoliday(null);
                    specialHolidayRepository.delete(specialHoliday);
                    break;
                }
                case "UNPAID": {

                    UnpaidHolidayEntity unpaidHoliday = modifiedHoliday.getUnpaidHoliday();

                    modifiedHoliday.setUnpaidHoliday(null);
                    unpaidHolidayRepository.delete(unpaidHoliday);
                    break;
                }
            }

            switch (newType) {

                case "REST": {

                    RestHolidayEntity restHoliday = new RestHolidayEntity();
                    restHoliday.setHolidayId(modifiedHoliday.getId());
                    restHoliday.setSubstituteName(requestFormDto.getSubstitute());
                    restHoliday.setHoliday(modifiedHoliday);

                    modifiedHoliday.setRestHoliday(restHoliday);
                    restHolidayRepository.save(restHoliday);
                    break;
                }
                case "SPECIAL": {

                    SpecialHolidayEntity specialHoliday = new SpecialHolidayEntity();
                    specialHoliday.setHolidayId(modifiedHoliday.getId());
                    specialHoliday.setSubstituteName(requestFormDto.getSubstitute());
                    specialHoliday.setDocument(requestFormDto.getDocument());
                    specialHoliday.setHoliday(modifiedHoliday);

                    modifiedHoliday.setSpecialHoliday(specialHoliday);
                    specialHolidayRepository.save(specialHoliday);
                    break;
                }
                case "UNPAID": {

                    UnpaidHolidayEntity unpaidHoliday = new UnpaidHolidayEntity();
                    unpaidHoliday.setHolidayId(modifiedHoliday.getId());
                    unpaidHoliday.setHoliday(modifiedHoliday);

                    modifiedHoliday.setUnpaidHoliday(unpaidHoliday);
                    unpaidHolidayRepository.save(unpaidHoliday);
                    break;
                }
            }
        }

        holidayRepository.save(modifiedHoliday);

        request.setHoliday(modifiedHoliday);


        if(!request.getUser().getTeam().getTeamLead().getId().equals(request.getUser().getId())) {
            NotificationResponseDto notificationDto = new NotificationResponseDto();
            notificationDto.setMessage(request.getUser().getFirstName() + " " + request.getUser().getLastName() + " has updated their " + getHolidayType(request.getHoliday()).toLowerCase() + " request");
            notificationDto.setStatus(NotificationStatus.UNREAD);
            notificationDto.setUserFrom(request.getUser().getId());

            SubstitutePeriodEntity substitutePeriod = substitutePeriodRepository.findOnGoingSubstitutePeriodTeam(request.getUser().getTeam().getId(), LocalDate.now().atStartOfDay());

            UserEntity teamLead;

            if (substitutePeriod != null) {

                teamLead = substitutePeriod.getSubstitute();
            }
            else {

                teamLead = request.getUser().getTeam().getTeamLead();
            }

            notificationDto.setUserTo(teamLead.getId());
            notificationDto.setHolidayId(request.getHolidayId());

            System.out.println("----------> onUpdate from member: " + notificationDto);
            this.notificationService.add(notificationDto);
            notifyFrontend();
        }


        return requestMapper.entityToDto(requestRepository.save(request));
    }

    @Override
    @Transactional
    public RequestDto updateTeamLeadRequest(Long userId, TeamLeadRequestFormUpdateDto requestFormDto) {

        String newType = requestFormDto.getType();

        LocalDateTime startDate = requestFormDto.getStartDate();
        LocalDateTime endDate = requestFormDto.getEndDate();

        UserEntity substitute = userRepository.findById(requestFormDto.getSubstituteId()).get();

        String substituteName = substitute.getFirstName() + " " + substitute.getLastName();

        RequestEntity request = requestRepository.findByUserIdAndHolidayId(userId, requestFormDto.getHolidayId());

        HolidayEntity modifiedHoliday = holidayRepository.getById(requestFormDto.getHolidayId()); // get modified holiday date

        modifiedHoliday.setStartDate(startDate);
        modifiedHoliday.setEndDate(endDate);

        String oldType = getHolidayType(modifiedHoliday);

        if (Objects.equals(newType, oldType)) { //compare new and old holidays

            switch (newType) {

                case "REST": {

                    RestHolidayEntity restHoliday = modifiedHoliday.getRestHoliday();
                    restHoliday.setSubstituteName(substituteName);
                    restHolidayRepository.save(restHoliday);

                    modifiedHoliday.setRestHoliday(restHoliday);
                    break;
                }
                case "SPECIAL": {

                    SpecialHolidayEntity specialHoliday = modifiedHoliday.getSpecialHoliday();
                    specialHoliday.setSubstituteName(substituteName);
                    specialHoliday.setDocument(requestFormDto.getDocument());
                    specialHoliday.setDocumentName(requestFormDto.getDocumentName());
                    specialHolidayRepository.save(specialHoliday);

                    modifiedHoliday.setSpecialHoliday(specialHoliday);
                    break;
                }
                case "UNPAID": {

                    UnpaidHolidayEntity unpaidHoliday = modifiedHoliday.getUnpaidHoliday();
                    unpaidHolidayRepository.save(unpaidHoliday);

                    modifiedHoliday.setUnpaidHoliday(unpaidHoliday);
                    break;
                }
            }
        }
        else {

            switch (oldType) {

                case "REST": {

                    RestHolidayEntity restHoliday = modifiedHoliday.getRestHoliday();

                    modifiedHoliday.setRestHoliday(null);
                    restHolidayRepository.delete(restHoliday);
                    break;
                }
                case "SPECIAL": {

                    SpecialHolidayEntity specialHoliday = modifiedHoliday.getSpecialHoliday();

                    modifiedHoliday.setSpecialHoliday(null);
                    specialHolidayRepository.delete(specialHoliday);
                    break;
                }
                case "UNPAID": {

                    UnpaidHolidayEntity unpaidHoliday = modifiedHoliday.getUnpaidHoliday();

                    modifiedHoliday.setUnpaidHoliday(null);
                    unpaidHolidayRepository.delete(unpaidHoliday);
                    break;
                }
            }

            switch (newType) {

                case "REST": {

                    RestHolidayEntity restHoliday = new RestHolidayEntity();
                    restHoliday.setHolidayId(modifiedHoliday.getId());
                    restHoliday.setSubstituteName(substituteName);
                    restHoliday.setHoliday(modifiedHoliday);

                    modifiedHoliday.setRestHoliday(restHoliday);
                    restHolidayRepository.save(restHoliday);
                    break;
                }
                case "SPECIAL": {

                    SpecialHolidayEntity specialHoliday = new SpecialHolidayEntity();
                    specialHoliday.setHolidayId(modifiedHoliday.getId());

                    specialHoliday.setSubstituteName(substituteName);
                    specialHoliday.setDocument(requestFormDto.getDocument());
                    specialHoliday.setHoliday(modifiedHoliday);

                    modifiedHoliday.setSpecialHoliday(specialHoliday);
                    specialHolidayRepository.save(specialHoliday);
                    break;
                }
                case "UNPAID": {

                    UnpaidHolidayEntity unpaidHoliday = new UnpaidHolidayEntity();
                    unpaidHoliday.setHolidayId(modifiedHoliday.getId());
                    unpaidHoliday.setHoliday(modifiedHoliday);

                    modifiedHoliday.setUnpaidHoliday(unpaidHoliday);
                    unpaidHolidayRepository.save(unpaidHoliday);
                    break;
                }
            }
        }

        holidayRepository.save(modifiedHoliday);
        request.setHoliday(modifiedHoliday);

        TeamEntity team = this.userRepository.findById(userId).get().getTeam();

        System.out.println(team.getId());
        this.substitutePeriodRepository.deleteSubstitutePeriodEntitiesByStartDateAndEndDateAndTeamId(
                requestFormDto.getOldStartDate(),requestFormDto.getOldEndDate(),team.getId());


        UserEntity userSubstituteEntity = this.userRepository.findById(requestFormDto.getSubstituteId()).get();
        SubstitutePeriodEntity substitutePeriod = new SubstitutePeriodEntity();
        substitutePeriod.setSubstitute(userSubstituteEntity);
        substitutePeriod.setStartDate(requestFormDto.getStartDate());
        substitutePeriod.setEndDate(requestFormDto.getEndDate());

        substitutePeriod.setTeam(team);

        substitutePeriodRepository.save(substitutePeriod);
        return requestMapper.entityToDto(requestRepository.save(request));
    }

    @Override
    @Transactional
    public void cancelRequest(Long userId, Long holidayId) {

        RequestEntity request = requestRepository.findByUserIdAndHolidayId(userId, holidayId);

        RequestDto dto = this.requestMapper.entityToDto(request);

        if(!request.getUser().getTeam().getTeamLead().getId().equals(request.getUser().getId())) {

            NotificationResponseDto notificationDto = new NotificationResponseDto();

            notificationDto.setMessage(request.getUser().getFirstName() + " " + request.getUser().getLastName() + " has cancelled their " + dto.getType().toLowerCase() + " request");

            notificationDto.setStatus(NotificationStatus.UNREAD);

            notificationDto.setUserFrom(request.getUser().getId());

            SubstitutePeriodEntity substitutePeriod = substitutePeriodRepository.findOnGoingSubstitutePeriodTeam(request.getUser().getTeam().getId(), LocalDate.now().atStartOfDay());

            UserEntity teamLead;

            if (substitutePeriod != null) {

                teamLead = substitutePeriod.getSubstitute();
            }
            else {

                teamLead = request.getUser().getTeam().getTeamLead();
            }

            notificationDto.setUserTo(teamLead.getId());

            notificationService.add(notificationDto);

            notifyFrontend();
        }

        requestRepository.delete(request);
    }

    @Override
    @Transactional
    public void cancelTeamLeadRequest(Long userId, Long holidayId) {

        System.out.println(this.userRepository.findById(userId).get().getTeam().getId());
        System.out.println(this.holidayRepository.findById(holidayId).get().getStartDate());
        System.out.println(this.holidayRepository.findById(holidayId).get().getEndDate());

        Long teamId = this.userRepository.findById(userId).get().getTeam().getId();
        HolidayEntity holiday = this.holidayRepository.findById(holidayId).get();

        RequestEntity request = requestRepository.findByUserIdAndHolidayId(userId, holidayId);

        RequestDto dto = this.requestMapper.entityToDto(request);


        this.substitutePeriodRepository.deleteSubstitutePeriodEntitiesByStartDateAndEndDateAndTeamId(holiday.getStartDate(),holiday.getEndDate(),teamId);

        requestRepository.delete(request);
    }
    @Override
    @Transactional
    public RequestDto setRequestStatus(SetStatusDto setStatusDto, Long teamLeadId) {

        RequestEntity request = requestRepository.findByUserIdAndHolidayId(setStatusDto.getUserId(), setStatusDto.getHolidayId());

        request.setStatus(setStatusDto.getStatus());
        request.setExtraInfo(setStatusDto.getExtraInfo());

        NotificationRequestDto notificationDto = new NotificationRequestDto();

        notificationDto.setStatus(NotificationStatus.UNREAD);
        notificationDto.setRequestStatus(setStatusDto.getStatus());
        notificationDto.setHolidayId(request.getHolidayId());
        notificationDto.setUserFrom(teamLeadId);
        notificationDto.setUserTo(request.getUser().getId());
        notificationDto.setHolidayType(getHolidayType(request.getHoliday()));

        notificationService.add(notificationDto);

        notifyFrontend();

        return requestMapper.entityToDto(requestRepository.save(request));
    }

    @Override
    public void notifyFrontend(){

    	webSocketService.sendMessage("notification");
    }

}
