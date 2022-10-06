package com.example.HolidayManager.mapper;

import com.example.HolidayManager.dto.RequestDto;
import com.example.HolidayManager.entity.RequestEntity;
import com.example.HolidayManager.entity.SubstitutePeriodEntity;
import org.springframework.stereotype.Component;

@Component
public class RequestMapperImpl implements RequestMapper {

    @Override
    public RequestDto entityToDto(RequestEntity requestEntity) {

        RequestDto requestDto = new RequestDto();

        requestDto.setUserId(requestEntity.getUser().getId());
        requestDto.setHolidayId(requestEntity.getHoliday().getId());
        requestDto.setEmail(requestEntity.getUser().getEmail());
        requestDto.setFirstName(requestEntity.getUser().getFirstName());
        requestDto.setLastName(requestEntity.getUser().getLastName());
        requestDto.setStartDate(requestEntity.getHoliday().getStartDate());
        requestDto.setEndDate(requestEntity.getHoliday().getEndDate());

        String type;

        if (requestEntity.getHoliday().getRestHoliday() != null) {

            type = "Rest Holiday";
            requestDto.setSubstitute(requestEntity.getHoliday().getRestHoliday().getSubstituteName());
        }
        else if (requestEntity.getHoliday().getSpecialHoliday() != null) {

            type = "Special Holiday";
            requestDto.setSubstitute(requestEntity.getHoliday().getSpecialHoliday().getSubstituteName());
            requestDto.setDocument(requestEntity.getHoliday().getSpecialHoliday().getDocument());
            requestDto.setDocumentName(requestEntity.getHoliday().getSpecialHoliday().getDocumentName());

        } else {
            type = "Unpaid Holiday";
        }

        requestDto.setType(type);
        requestDto.setStatus(requestEntity.getStatus().toString());
        requestDto.setExtraInfo(requestEntity.getExtraInfo());

        return requestDto;
    }

//    @Override
//    public TeamLeadRequestDto entityToDto(RequestEntity requestEntity, SubstitutePeriodEntity substitutePeriodEntity) {
//
//        TeamLeadRequestDto teamLeadRequestDto = new TeamLeadRequestDto();
//
//        teamLeadRequestDto.setUserId(requestEntity.getUser().getId());
//        teamLeadRequestDto.setHolidayId(requestEntity.getHoliday().getId());
//        teamLeadRequestDto.setEmail(requestEntity.getUser().getEmail());
//        teamLeadRequestDto.setFirstName(requestEntity.getUser().getFirstName());
//        teamLeadRequestDto.setLastName(requestEntity.getUser().getLastName());
//        teamLeadRequestDto.setStartDate(requestEntity.getHoliday().getStartDate());
//        teamLeadRequestDto.setEndDate(requestEntity.getHoliday().getEndDate());
//
//        String type;
//
//        if (requestEntity.getHoliday().getRestHoliday() != null) {
//
//            type = "Rest Holiday";
//            teamLeadRequestDto.setSubstituteId(substitutePeriodEntity.getSubstitute().getId());
//            teamLeadRequestDto.setSubstituteName(substitutePeriodEntity.getSubstitute().getFirstName() + " " + substitutePeriodEntity.getSubstitute().getLastName());
//        }
//        else if (requestEntity.getHoliday().getSpecialHoliday() != null) {
//
//            type = "Special Holiday";
//            teamLeadRequestDto.setSubstituteId(substitutePeriodEntity.getSubstitute().getId());
//            teamLeadRequestDto.setSubstituteName(substitutePeriodEntity.getSubstitute().getFirstName() + " " + substitutePeriodEntity.getSubstitute().getLastName());
//            teamLeadRequestDto.setDocument(requestEntity.getHoliday().getSpecialHoliday().getDocument());
//            teamLeadRequestDto.setDocumentName(requestEntity.getHoliday().getSpecialHoliday().getDocumentName());
//
//        } else {
//            type = "Unpaid Holiday";
//        }
//
//        teamLeadRequestDto.setType(type);
//        teamLeadRequestDto.setStatus(requestEntity.getStatus().toString());
//        teamLeadRequestDto.setExtraInfo(requestEntity.getExtraInfo());
//
//        return teamLeadRequestDto;
//    }
}
