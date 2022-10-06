package com.example.HolidayManager.controller;

import com.example.HolidayManager.dto.*;
import com.example.HolidayManager.entity.Status;
import com.example.HolidayManager.entity.SubstitutePeriodEntity;
import com.example.HolidayManager.entity.UserType;
import com.example.HolidayManager.security.JwtTokenService;
import com.example.HolidayManager.service.*;
import com.example.HolidayManager.util.annotations.AllowEmployee;
import com.example.HolidayManager.util.annotations.AllowTeamLead;
import com.example.HolidayManager.util.annotations.AllowTeamLeadAndEmployee;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/request")
@RequiredArgsConstructor
public class RequestController {

    private final RequestService requestService;

    private final UserService userService;

    private final JwtTokenService jwtTokenService;

    private final SubstituteService substituteService;

    private final TeamService teamService;

    @GetMapping("/get-team-requests-by-status")
    @AllowTeamLead
    public ResponseEntity<List<RequestDto>> getTeamRequests(@RequestHeader("app-auth") String token, @RequestParam("status") String statusAsString) {

        Long userId = jwtTokenService.getUserIdFromToken(token);

        Long teamId = userService.getById(userId).getTeam().getId();

        List<RequestDto> filteredList;

        if (!substituteService.isUserOnHoliday(userId, LocalDate.now().atStartOfDay(), LocalDate.now().atStartOfDay())) {

            if(statusAsString.equals("ALL")) {

                filteredList = requestService.findByTeamId(userId, teamId);
            }
            else {

                Status status;

                switch (statusAsString) {
                    case "APPROVED":

                        status = Status.APPROVED;
                        break;

                    case "DECLINED":

                        status = Status.DECLINED;
                        break;

                    case "PENDING":

                        status = Status.PENDING;
                        break;

                    case "EXTRA_INFO":

                        status = Status.EXTRA_INFO;
                        break;

                    default:

                        throw new IllegalStateException("Unexpected value: " + statusAsString + "; must be of type: APPROVED, DECLINED, PENDING, EXTRA_INFO, or empty string");
                }

                filteredList = requestService.findByTeamIdAndStatus(userId, teamId, status);
            }

            return ResponseEntity.ok(filteredList);
        }
        else {

            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    @GetMapping("/get-requests-by-type")
    @AllowTeamLeadAndEmployee
    public ResponseEntity<List<RequestDto>> getRequests(@RequestHeader("app-auth") String token, @RequestParam String type) {

        Long userId = jwtTokenService.getUserIdFromToken(token);

        ResponseEntity<List<RequestDto>> response;

        switch (type) {

            case "REST": {

                response = ResponseEntity.ok(requestService.findRestHolidaysByUserId(userId));
                break;
            }
            case "SPECIAL": {

                response = ResponseEntity.ok(requestService.findSpecialHolidaysByUserId(userId));
                break;
            }
            case "UNPAID": {

                response = ResponseEntity.ok(requestService.findUnpaidHolidaysByUserId(userId));
                break;
            }
            default: {

                response = ResponseEntity.ok(requestService.findByUserId(userId));
                break;
            }

        }

        return response;
    }

    @PostMapping("/make-new-request")
    @AllowEmployee
    public ResponseEntity<RequestDto> makeRequest(@RequestHeader("app-auth") String token, @RequestBody RequestFormDto requestFormDto) {

        Long userId = jwtTokenService.getUserIdFromToken(token);

        ResponseEntity<RequestDto> response;

        if (requestService.checkForOverlappingRequests(userId, requestFormDto)) {

            response = ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }
        else {

            response = ResponseEntity.ok(requestService.makeRequest(userId, requestFormDto));
        }

        return response;
    }

    @PostMapping("/make-new-team-lead-request")
    @AllowTeamLead
    public ResponseEntity<RequestDto> makeTeamLeadRequest(@RequestHeader("app-auth") String token, @RequestBody TeamLeadRequestFormDto requestFormDto) {

        Long userId = jwtTokenService.getUserIdFromToken(token);

        ResponseEntity<RequestDto> response;

        RequestFormDto partialDto = new RequestFormDto();

        partialDto.setStartDate(requestFormDto.getStartDate());
        partialDto.setEndDate(requestFormDto.getEndDate());

        if (requestService.checkForOverlappingRequests(userId, partialDto)) {

            response = ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }
        else {

            response = ResponseEntity.ok(requestService.makeRequestTeamLead(userId, requestFormDto));
        }

        return response;
    }


    @PutMapping("/update-team-lead-request")
    @AllowTeamLead
    public ResponseEntity<RequestDto> updateTeamLeadRequest(@RequestHeader("app-auth") String token, @RequestBody TeamLeadRequestFormUpdateDto requestFormDto ) {

        Long userId = jwtTokenService.getUserIdFromToken(token);

        ResponseEntity<RequestDto> response;

        RequestFormDto partialDto = new RequestFormDto();

        partialDto.setStartDate(requestFormDto.getStartDate());
        partialDto.setEndDate(requestFormDto.getEndDate());

        if (requestService.checkForOverlappingRequestsExceptForCurrentRequest(userId, partialDto)) {

            response = ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }
        else {

            response = ResponseEntity.ok(requestService.updateTeamLeadRequest(userId, requestFormDto));
        }

        return response;
    }
    @PutMapping("/update-request")
    @AllowTeamLeadAndEmployee
    public ResponseEntity<RequestDto> updateRequest(@RequestHeader("app-auth") String token, @RequestBody RequestFormDto requestFormDto) {

        Long userId = jwtTokenService.getUserIdFromToken(token);
        UserType userType = jwtTokenService.getUserTypeFromToken(token);
        ResponseEntity<RequestDto> response;

        if (requestService.checkForOverlappingRequestsExceptForCurrentRequest(userId, requestFormDto)) {

            response = ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }
        else {

            response = ResponseEntity.ok(requestService.updateRequest(userId, userType, requestFormDto));
        }

        return response;
    }

    @DeleteMapping("/cancel-request/{holidayId}")
    @AllowTeamLeadAndEmployee
    public ResponseEntity<String> cancelRequest(@RequestHeader("app-auth") String token, @PathVariable Long holidayId) {

        Long userId = jwtTokenService.getUserIdFromToken(token);

        requestService.cancelRequest(userId, holidayId);

        return ResponseEntity.status(200).build();
    }

    @DeleteMapping("/cancel-request-for-update/{holidayId}")
    @AllowTeamLead
    public ResponseEntity<String> cancelTeamLeadRequest(@RequestHeader("app-auth") String token, @PathVariable Long holidayId) {

        Long userId = jwtTokenService.getUserIdFromToken(token);

        requestService.cancelTeamLeadRequest(userId, holidayId);

        return ResponseEntity.status(200).build();
    }

    @PutMapping("/set-request-status")
    @AllowTeamLead
    public ResponseEntity<String> setRequestStatus(@RequestHeader("app-auth") String token, @RequestBody SetStatusDto setStatusDto) {

        Long teamLeadId = jwtTokenService.getUserIdFromToken(token);

        requestService.setRequestStatus(setStatusDto, teamLeadId);

        return ResponseEntity.status(200).build();
    }


    @GetMapping("/get-substitute-team-requests-by-status")
    @AllowTeamLead
    public ResponseEntity<List<RequestDto>> getSubstituteTeamRequests(@RequestHeader("app-auth") String token, @RequestParam("status") String statusAsString){

        Long userId = jwtTokenService.getUserIdFromToken(token);
        SubstitutePeriodEntity substitutePeriodEntity = this.substituteService.findUserOnGoingSubstitutePeriod(userId); //  CURENTLY LOGGED USER IS SUBSTITUTE, substitutePeriodEntity is users's period;

        List<RequestDto> filteredList;

        if (substitutePeriodEntity != null) {

            Long toBeSubstitutedTeamLeadId = substitutePeriodEntity.getTeam().getTeamLead().getId();
            Long toBeSubstitutedTeamId = substitutePeriodEntity.getTeam().getId();

            if (statusAsString.equals("ALL")) {

                filteredList = requestService.findByTeamId(toBeSubstitutedTeamLeadId, toBeSubstitutedTeamId);
            }
            else {

                Status status;

                switch (statusAsString) {
                    case "APPROVED":

                        status = Status.APPROVED;
                        break;

                    case "DECLINED":

                        status = Status.DECLINED;
                        break;

                    case "PENDING":

                        status = Status.PENDING;
                        break;

                    case "EXTRA_INFO":

                        status = Status.EXTRA_INFO;
                        break;

                    default:

                        throw new IllegalStateException("Unexpected value: " + statusAsString + "; must be of type: APPROVED, DECLINED, PENDING, EXTRA_INFO, or empty string");
                }

                filteredList = requestService.findByTeamIdAndStatus(toBeSubstitutedTeamLeadId, toBeSubstitutedTeamId, status);
            }

            return ResponseEntity.ok(filteredList);
        }
        else {

            return ResponseEntity.ok(null);
        }
    }


    @GetMapping("/get-substitute-id")
    public ResponseEntity<SubstituteIdDto> findSubstituteId( @RequestHeader("app-auth") String token, @RequestParam Long holidayId)
    {
        Long userId = jwtTokenService.getUserIdFromToken(token);
        Long teamId = this.teamService.findTeamByTeamLeadId(userId);
        return ResponseEntity.ok(this.substituteService.findSubstitute(teamId,holidayId));
    }
}