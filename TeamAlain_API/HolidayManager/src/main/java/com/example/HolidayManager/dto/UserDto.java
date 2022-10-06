package com.example.HolidayManager.dto;

import com.example.HolidayManager.entity.Role;
import com.example.HolidayManager.entity.UserType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserDto {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private UserType userType;
    private String department;
    private Integer daysLeft;

    private Long teamId;
    private Role role;

    public UserDto()
    {

    }

}
