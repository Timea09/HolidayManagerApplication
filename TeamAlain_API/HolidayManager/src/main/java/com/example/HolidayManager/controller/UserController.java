package com.example.HolidayManager.controller;

import com.example.HolidayManager.dto.*;
import com.example.HolidayManager.security.JwtTokenService;
import com.example.HolidayManager.service.RequestService;
import com.example.HolidayManager.service.SubstituteService;
import com.example.HolidayManager.service.UserService;
import com.example.HolidayManager.util.annotations.AllowAdmin;
import com.example.HolidayManager.util.annotations.AllowEmployee;
import com.example.HolidayManager.util.annotations.AllowTeamLead;
import com.example.HolidayManager.util.annotations.AllowTeamLeadAndEmployee;
import com.example.HolidayManager.util.exceptions.UserNotFoundException;
import com.example.HolidayManager.util.exceptions.UserPasswordNoMatchException;
import com.itextpdf.text.DocumentException;
import lombok.RequiredArgsConstructor;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import java.time.LocalDateTime;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@CrossOrigin
public class UserController {

    private final UserService userService;

    private final SubstituteService substituteService;

    private final RequestService requestService;
    private final JwtTokenService jwtTokenService;

    @AllowAdmin
    @PostMapping("/add")
    public ResponseEntity<UserDto> addUser(@RequestBody UserDto userDto){
        UserDto createdUser = userService.addUser(userDto);
        if(createdUser!=null)
            return new ResponseEntity<>(createdUser, HttpStatus.OK);
        else
            return ResponseEntity.status(409).build(); //conflict email already exists

    }

    @AllowAdmin
    @DeleteMapping("/delete/{userID}")
    public ResponseEntity<UserDto> deleteUser(@PathVariable Long userID){
        UserDto createdUser = userService.deleteUser(userID);
        if(createdUser!=null)
            return new ResponseEntity<>(createdUser, HttpStatus.OK);
        else
            return ResponseEntity.status(409).build(); //user not found
    }

    @AllowAdmin
    @PutMapping("/update")
    public ResponseEntity<UserDto> updateUser(@RequestBody UserDto userDto){
        UserDto updatedUser = userService.updateUser(userDto);
        if(updatedUser!=null)
            return new ResponseEntity<>(updatedUser, HttpStatus.OK);
        else
            return ResponseEntity.status(404).build(); //user not found
    }


    @AllowAdmin
    @GetMapping("/get-all")
    public ResponseEntity<List<UserDto>> getAll() {
        return ResponseEntity.ok(userService.findAll());
    }

    @AllowTeamLeadAndEmployee
    @GetMapping("/get-days-left")
    public ResponseEntity<DaysLeftDto> getDaysLeft(@RequestHeader("app-auth") String token) {

        Long userId = jwtTokenService.getUserIdFromToken(token);

        return ResponseEntity.ok(userService.getDaysLeft(userId));
    }

    @AllowAdmin
    @GetMapping("/get-all-no-team")
    public ResponseEntity<List<UserResponseDto>> getAllUsersWithNoTeam() {
        return ResponseEntity.ok(userService.getAllUsersWithNoTeam());

    }

    @AllowTeamLeadAndEmployee
    @PatchMapping("/change-password")
    public ResponseEntity<String> changeUserPassword(@RequestHeader("app-auth") String token, @RequestBody UserChangePasswordDto userChangePasswordDto){
        Long userId= jwtTokenService.getUserIdFromToken(token);
        try {
            userService.changeUserPassword(userId, userChangePasswordDto);
            return ResponseEntity.status(200).build();
        }
        catch (UserNotFoundException e) {
            return ResponseEntity.status(409).build();
        }
        catch (UserPasswordNoMatchException e) {
            return ResponseEntity.status(400).build();
        }
    }

    @AllowTeamLead
    @GetMapping("/get-all-available-team-leads")
    public ResponseEntity<List<UserDto>> getAllTeamLeads(@RequestHeader("app-auth") String token, @RequestParam String startDate, @RequestParam String endDate) {

        Long userId = jwtTokenService.getUserIdFromToken(token);

        if (!substituteService.isUserAvailable(userId, LocalDateTime.parse(startDate), LocalDateTime.parse(endDate)))
        {
            return ResponseEntity.status(409).build();
        }

        RequestFormDto partialDto = new RequestFormDto();

        partialDto.setStartDate(LocalDateTime.parse(startDate));
        partialDto.setEndDate(LocalDateTime.parse(endDate));

        if (requestService.checkForOverlappingRequests(userId, partialDto))
        {
            return ResponseEntity.status(409).build();
        }

        List<UserDto> result = substituteService.findAllAvailableTeamLeads(userId, LocalDateTime.parse(startDate), LocalDateTime.parse(endDate));

        if (result.isEmpty()) {

            return ResponseEntity.status(406).build();
        }

        return ResponseEntity.ok(result);
    }

    @AllowTeamLead
    @GetMapping("/get-all-available-team-leads-for-update")
    public ResponseEntity<List<UserDto>> getAllTeamLeadsForUpdate(
            @RequestHeader("app-auth") String token, @RequestParam String startDate, @RequestParam String endDate, @RequestParam Long teamId, @RequestParam Long holidayId) {

        Long userId = jwtTokenService.getUserIdFromToken(token);

        if (!substituteService.isUserAvailable(userId, LocalDateTime.parse(startDate), LocalDateTime.parse(endDate)))
        {
            return ResponseEntity.status(409).build();
        }

        RequestFormDto partialDto = new RequestFormDto();

        partialDto.setStartDate(LocalDateTime.parse(startDate));
        partialDto.setEndDate(LocalDateTime.parse(endDate));
        partialDto.setHolidayId(holidayId);

        if (requestService.checkForOverlappingRequestsExceptForCurrentRequest(userId, partialDto))
        {
            return ResponseEntity.status(409).build();
        }

        List<UserDto> result = substituteService.findAllAvailableTeamLeadsForUpdate(userId, LocalDateTime.parse(startDate), LocalDateTime.parse(endDate), teamId);

        if (result.isEmpty()) {

            return ResponseEntity.status(406).build();
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("get-team-id")
    public ResponseEntity<Long> getTeamId(@RequestHeader("app-auth") String token) {
        Long userId = jwtTokenService.getUserIdFromToken(token);
        Long teamId = this.userService.getById(userId).getTeam().getId();
        return ResponseEntity.ok(teamId);
    }

    @GetMapping("/generate-report")
    public ResponseEntity<ReportDto> generatePdfReport(@RequestHeader("app-auth") String token){
        try {
            Long userId = jwtTokenService.getUserIdFromToken(token);
            ReportDto reportDto = userService.generateTeamLeadPdfReport(userId);
            return ResponseEntity.ok(reportDto);
        }
        catch (FileNotFoundException e){
            return ResponseEntity.status(400).build();
        }
    }

    @AllowTeamLeadAndEmployee
    @GetMapping("/complete-document/{holidayId}")
    public ResponseEntity<DocumentDto> completeWordDocument(@RequestHeader("app-auth") String token,@PathVariable Long holidayId){

        try {

            Long userId = jwtTokenService.getUserIdFromToken(token);
            DocumentDto autocompletedDocument = userService.autocompletePdfDocument(userId,holidayId);

            userService.sendSimpleEmail("tudor-alexandru.bal@mhp.com", userId, holidayId);

            return ResponseEntity.ok(autocompletedDocument);
        }
        catch (FileNotFoundException | MessagingException e) {

            e.printStackTrace();
            return ResponseEntity.status(400).build();
        }
        catch (IOException | InvalidFormatException | DocumentException e) {
            throw new RuntimeException(e);
        }
    }
}
