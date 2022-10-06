package com.example.HolidayManager.mapper;

import com.example.HolidayManager.dto.RequestDto;
import com.example.HolidayManager.entity.RequestEntity;
import com.example.HolidayManager.entity.SubstitutePeriodEntity;

public interface RequestMapper {

    RequestDto entityToDto(RequestEntity requestEntity);

    //TeamLeadRequestDto entityToDto(RequestEntity requestEntity, SubstitutePeriodEntity substitutePeriodEntity);
}
