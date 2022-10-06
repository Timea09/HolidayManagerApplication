package com.example.HolidayManager.service;

import com.example.HolidayManager.dto.*;
import com.example.HolidayManager.entity.Status;
import com.example.HolidayManager.entity.UserType;

import java.util.List;

public interface RequestService {

    List<RequestDto> findByUserId(Long id);

    List<RequestDto> findByEmail(String email);

    List<RequestDto> findByTeamId(Long teamLeadId, Long teamId);

    List<RequestDto> findByTeamIdAndStatus(Long teamLeadId, Long teamId, Status status);

    List<RequestDto> findRestHolidaysByUserId(Long id);

    List<RequestDto> findSpecialHolidaysByUserId(Long id);

    List<RequestDto> findUnpaidHolidaysByUserId(Long id);

    RequestDto makeRequest(Long userId, RequestFormDto requestFormDto);

    RequestDto makeRequestTeamLead(Long userId, TeamLeadRequestFormDto requestFormDto);

    RequestDto updateRequest(Long userId, UserType userType, RequestFormDto requestFormDto);

    RequestDto updateTeamLeadRequest(Long userId, TeamLeadRequestFormUpdateDto requestFormDto);

    void cancelRequest(Long userId, Long holidayId);
    void cancelTeamLeadRequest(Long userId, Long holidayId);

    RequestDto setRequestStatus(SetStatusDto setStatusDto, Long teamLeadId);

    boolean checkForOverlappingRequests(Long userId, RequestFormDto requestFormDto);

    boolean checkForOverlappingRequestsExceptForCurrentRequest(Long userId, RequestFormDto requestFormDto);

    void notifyFrontend();
}
