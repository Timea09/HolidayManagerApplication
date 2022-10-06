package com.example.HolidayManager.controller;

import com.example.HolidayManager.dto.TeamCreateRequestDto;
import com.example.HolidayManager.dto.TeamCreateResponseDto;
import com.example.HolidayManager.dto.TeamCreateResponseMembersDto;
import com.example.HolidayManager.dto.UserDto;
import com.example.HolidayManager.service.TeamService;
import com.example.HolidayManager.service.UserService;
import com.example.HolidayManager.util.annotations.AllowAdmin;
import com.example.HolidayManager.util.exceptions.TeamNameAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/team")
@RequiredArgsConstructor
@CrossOrigin
public class TeamController {

    private final TeamService teamService;


    @AllowAdmin
    @PostMapping("/add")
    public ResponseEntity<TeamCreateResponseMembersDto> addTeam(@RequestBody TeamCreateRequestDto teamCreateRequestDto){
        try{
            TeamCreateResponseMembersDto createdTeam = teamService.addTeam(teamCreateRequestDto);
            return new ResponseEntity<>(createdTeam,HttpStatus.OK);
        }
        catch(TeamNameAlreadyExistsException e) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }

    }

    @AllowAdmin
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteTeam(@PathVariable Long id) {

        teamService.deleteTeamById(id);
        return ResponseEntity.ok("{\"message\": \"Team successfully deleted\"}");
    }
    @GetMapping("/get-teams")
    public ResponseEntity<List<TeamCreateResponseMembersDto>> getTeams()
    {
        return ResponseEntity.ok(teamService.getTeams());
    }

    @AllowAdmin
    @PutMapping("/update")
    public ResponseEntity<TeamCreateResponseMembersDto> updateTeam(@RequestBody TeamCreateRequestDto updatedTeamCreateRequestDto){
        try{
            TeamCreateResponseMembersDto updatedTeam = teamService.updateTeam(updatedTeamCreateRequestDto);
            return new ResponseEntity<>(updatedTeam,HttpStatus.OK);
        }
        catch ( TeamNameAlreadyExistsException e) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
    }


}
