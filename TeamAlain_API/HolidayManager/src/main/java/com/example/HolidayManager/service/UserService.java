package com.example.HolidayManager.service;

import com.amazonaws.services.simpleemail.model.SendEmailResult;
import com.amazonaws.services.simpleemail.model.SendRawEmailResult;
import com.example.HolidayManager.dto.*;
import com.example.HolidayManager.entity.UserEntity;
import com.example.HolidayManager.util.exceptions.UserNotFoundException;
import com.example.HolidayManager.util.exceptions.UserPasswordNoMatchException;
import com.itextpdf.text.DocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import javax.mail.MessagingException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

public interface UserService {

    List<UserDto> findAll();

    UserEntity getById(Long id);

    UserEntity getById(UserIdDto userIdDto);

    UserDto addUser(UserDto userDto);

    UserDto deleteUser(Long userID);

    UserDto updateUser(UserDto userDto);

    DaysLeftDto getDaysLeft(Long userId);

    List<UserResponseDto> getAllUsersWithNoTeam();

    void changeUserPassword(Long userId, UserChangePasswordDto userChangePasswordDto) throws UserPasswordNoMatchException, UserNotFoundException;

    ReportDto generateTeamLeadPdfReport(Long userId) throws FileNotFoundException;

    DocumentDto autocompletePdfDocument(Long userId, Long holidayId) throws DocumentException, IOException, InvalidFormatException;

    SendRawEmailResult sendSimpleEmail(String invitedUserEmail, Long userId, Long holidayId) throws MessagingException, DocumentException, IOException, InvalidFormatException;
}
