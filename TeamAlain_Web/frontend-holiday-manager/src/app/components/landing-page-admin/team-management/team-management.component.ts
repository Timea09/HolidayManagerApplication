import { leadingComment } from '@angular/compiler';
import {Component, ElementRef, EventEmitter, OnInit, Output, ViewChild} from '@angular/core';
import { TeamService } from 'src/app/services/team.service';
import { TeamRequest } from '../../../shared/data-type/teamRequest'
import {Team} from '../../../shared/data-type/team';
import {MatDialog} from "@angular/material/dialog";
import {ConfirmationDialogComponent} from "../../dialog-boxes/confirmation-dialog/confirmation-dialog.component";
import {User} from "../../../shared/data-type/User";
import {UserService} from "../../../services/user.service";
import {MatSnackBar} from "@angular/material/snack-bar";




@Component({
  selector: 'app-team-management',
  templateUrl: './team-management.component.html',
  styleUrls: ['./team-management.component.scss']
})
export class TeamManagementComponent implements OnInit {

  @ViewChild('toggleBtn') toggleBtn: ElementRef;

  teams: Team[] = [];
  teamSelected:Team;
  eventUsersWithNoTeamChanged:boolean=false;

  constructor(private teamService: TeamService,private userService:UserService, private dialog : MatDialog, private snackbar : MatSnackBar) {
  }


  ngOnInit(): void {

    this.teamService.getAllTeams().subscribe(data=>
    {
      this.teams= data;
      console.log(data)
    });

  }

  createTeam(newTeam: TeamRequest) :void
  {
    if(newTeam.id==undefined) {
      this.teamService.createTeam(newTeam).subscribe({next: responseTeam => {
        console.log(responseTeam.teamName+"@@@@@@@@@@@@@@222"+responseTeam.teamMembers)
          this.teams?.push(responseTeam);
        },
        error: err =>{
          if(err.status==409){//team name already exists 409--conflict
            this.eventUsersWithNoTeamChanged=!this.eventUsersWithNoTeamChanged

            // alert("Team name already in use");
            this.snackbar.open("Team name already in use!", "Got it!");
          }
        }
      });
    }

    else{
      this.teamService.updateTeam(newTeam).subscribe({
        next: responseTeam => {

          let index = 0;
          for (let i = 0; i < this.teams!.length; i++) {
            if (this.teams![i].id == responseTeam.id) {
              index = i;
            }
          }

          this.teams?.splice(index, 1);
          this.teams!.push(responseTeam);
        },

        error: err => {
          if (err.status == 409) {//team name already exists 409--conflict
            this.eventUsersWithNoTeamChanged=!this.eventUsersWithNoTeamChanged
            // alert("Team name already in use");
            this.snackbar.open("Team name already in use!", "Got it!");
          }
        }
      });
    }

  }



  deleteTeam(team: Team) : void
  {
    const dialogResponse = this.dialog.open(ConfirmationDialogComponent, {
      width: '250px', data: "Are you sure you want to delete team " + team.teamName+ " ?",
      autoFocus: false
    });
    dialogResponse.afterClosed().subscribe(result => {
      if (result) {
        this.teamService.deleteTeam(team.id!).subscribe(data=>{
          // @ts-ignore
          this.teams.splice(this.teams?.indexOf(team), 1);
          this.eventUsersWithNoTeamChanged=!this.eventUsersWithNoTeamChanged

        });
      }
    });
  }

  seeTeam(team : Team) {

    team.clicked = !team.clicked;
    //make clicked team unique
    if (team.clicked) {

      this.teams?.forEach((t)=>{
        if ( t.id != team.id) {
          t.clicked = false;
        }
      })
      //obiect nou ==> ngOnChanges detecteaza o schimbare
      this.teamSelected = {
        id: team.id,
        teamName: team.teamName,
        teamMembers: team.teamMembers,
        teamLead : team.teamLead,
        clicked: team.clicked
      }
    }
    else{
      this.teamSelected=new Team();
    }
  }

}
