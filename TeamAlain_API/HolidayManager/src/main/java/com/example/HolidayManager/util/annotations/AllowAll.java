package com.example.HolidayManager.util.annotations;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasAuthority(T(com.example.HolidayManager.entity.UserType).TEAM_LEAD)" +
        "or hasAuthority(T(com.example.HolidayManager.entity.UserType).ADMIN)" +
        "or hasAuthority(T(com.example.HolidayManager.entity.UserType).EMPLOYEE)")
@Target(ElementType.METHOD)
public @interface AllowAll {
}
