import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {MatTableDataSource} from "@angular/material/table";
import {Request} from "../../shared/data-type/request";
import {UserService} from "../../services/user.service";
import {MatSnackBar} from "@angular/material/snack-bar";
import {MatDialog} from "@angular/material/dialog";
import {dateObjectToISO8601} from "../../utils/DateConversion";
import {UserRequestFormObject} from "../../shared/data-type/userRequestFormObject";
import {TeamLeadService} from "../../services/team-lead.service";
import {RequestStatusObject} from "../../shared/data-type/requestStatusObject";
import {ConfirmationDialogComponent} from "../dialog-boxes/confirmation-dialog/confirmation-dialog.component";

import * as FileSaver from "file-saver";

import {TeamLeadRequestFormObject} from "../../shared/data-type/teamLeadRequestFormObject";
import {TeamLeadRequestFormUpdateObject} from "../../shared/data-type/teamLeadRequestFormUpdateObject";


@Component({
  selector: 'app-team-lead-page',
  templateUrl: './team-lead-page.component.html',
  styleUrls: ['./team-lead-page.component.scss']
})
export class TeamLeadPageComponent implements OnInit {

  daysLeft: number;
  userRequestList: MatTableDataSource<Request>;
  userRequestListTemp: Request[];

  selectedUserRequest: Request | null = null;

  teamRequestList: MatTableDataSource<Request>;
  substituteTeamRequestList: MatTableDataSource<Request>;
  selectedTeamRequest: Request | null = null;
  selectedSubstituteTeamRequest: Request | null = null;

  rawRequestList: any;

  statusFilter: string;
  substituteTeamStatusFilter: string;

  constructor(private userService: UserService, private teamLeadService: TeamLeadService, private _snackBar: MatSnackBar, private _dialog: MatDialog) { }

  ngOnInit(): void {

    //@ts-ignore
    this.userService.getVacationDays().subscribe((v) => { this.daysLeft = v.daysLeft; });
    this.userRequestList = new MatTableDataSource<Request>();
    this.teamRequestList = new MatTableDataSource<Request>();
    this.substituteTeamRequestList = new MatTableDataSource<Request>();
  }

  loadLists(){

    this.setTeamRequestList(this.statusFilter);
    this.setSubstituteTeamRequestList(this.substituteTeamStatusFilter);
  }

  setUserRequestList(type: string) {

    this.userService.getHolidayRequestsByType(type).subscribe(
      response => {
        this.userRequestListTemp=response;
        this.userRequestListTemp.forEach(request => {
            let startDate = new Date(request.startDate);
            let endDate = new Date(request.endDate);

            request.startDate = dateObjectToISO8601(startDate);

            request.endDate = dateObjectToISO8601(endDate);
          }
        );
        this.userRequestList.data = this.userRequestListTemp;
      }
    );
  }

  setTeamRequestList(status: string) {

    this.statusFilter = status;
    this.teamLeadService.getTeamHolidayRequestsByStatus(status).subscribe(
      response => {

        response.forEach(request => {
            let startDate = new Date(request.startDate);
            let endDate = new Date(request.endDate);

            request.startDate = dateObjectToISO8601(startDate);

            request.endDate = dateObjectToISO8601(endDate);
          }
        );
        this.teamRequestList = new MatTableDataSource<Request>(response);
      }
    );
  }

  setSubstituteTeamRequestList(status: string) {

    this.substituteTeamStatusFilter = status;
    this.teamLeadService.getSubstituteTeamHolidayRequestsByStatus(status).subscribe(
      response => {

        this.rawRequestList = response;

        if (this.rawRequestList !== null) {

          response.forEach(request => {
            let startDate = new Date(request.startDate);
            let endDate = new Date(request.endDate);

            request.startDate = dateObjectToISO8601(startDate);

            request.endDate = dateObjectToISO8601(endDate);
          }
        );
        this.substituteTeamRequestList = new MatTableDataSource<Request>(response);
        }
      }
    );
  }

  setSelectedUserRequest(request: Request | null) {

    this.selectedUserRequest = request;
  }

  setSelectedTeamRequest(request: Request | null) {

    this.selectedTeamRequest = request;
  }

  setSelectedSubstituteTeamRequest(request: Request | null) {

    this.selectedSubstituteTeamRequest = request;
  }

  makeRequest(requestForm: TeamLeadRequestFormObject) {

    this.teamLeadService.makeNewTeamLeadRequest(requestForm).subscribe(

      data => {
        let startDate = new Date(data.startDate);
        let endDate = new Date(data.endDate);
        data.startDate = dateObjectToISO8601(startDate);
        data.endDate = dateObjectToISO8601(endDate);
        this.userRequestListTemp.push(data);
        this.userRequestList.data = this.userRequestListTemp;
      },
      error => {

        this._snackBar.open("Holiday request is overlapping with another one!", "Dismiss");
      }
    );
  }



  updateRequest(requestForm: TeamLeadRequestFormUpdateObject) {

    this.teamLeadService.updateTeamLeadRequest(requestForm).subscribe(

      data => {
        let startDate = new Date(data.startDate);
        let endDate = new Date(data.endDate);
        data.startDate = dateObjectToISO8601(startDate);
        data.endDate = dateObjectToISO8601(endDate);

        this.userRequestListTemp.forEach((request,index) => {
          if(request.holidayId==data.holidayId && request.userId==data.userId){
            this.userRequestListTemp[index] = data;
          }
        })
        this.userRequestList.data=this.userRequestListTemp;
        this.selectedUserRequest=null;
      },
      error => {

        this._snackBar.open("Holiday request is overlapping with another one!", "Dismiss");
      }

    );
  }

  cancelRequest(event: null) {

    const dialogResponse = this._dialog.open(ConfirmationDialogComponent, {
      width: '250px', data: "Are you sure you want to cancel this request?",
      autoFocus: false
    });
    dialogResponse.afterClosed().subscribe(
      result => {
        if(result) {
          // @ts-ignore

          this.teamLeadService.cancelTeamLeadRequest(this.selectedUserRequest?.holidayId).subscribe(

            data => {

              this.userRequestListTemp.forEach((request,index)=>{
                if(request.holidayId==this.selectedUserRequest!.holidayId && request.userId==this.selectedUserRequest!.userId){
                  this.userRequestListTemp.splice(index,1);
                }
              })
              this.userRequestList.data=this.userRequestListTemp;
              this.selectedUserRequest=null;
            }
          );
        }
        else {
          this.selectedUserRequest = null;
        }
    }
    )

  }

  setTeamRequestStatus(requestStatusObject: RequestStatusObject) {

    if (this.selectedTeamRequest !== null) {

      this.teamLeadService.setTeamRequestStatus(requestStatusObject).subscribe(

        data => {

          console.log(data);
          this.setTeamRequestList("ALL");
        }
      );
    }
    else {

      this._snackBar.open("No request selected!", "Dismiss");
    }

  }

  setSubstituteTeamRequestStatus(requestStatusObject: RequestStatusObject) {

    if (this.selectedSubstituteTeamRequest !== null) {

      this.teamLeadService.setTeamRequestStatus(requestStatusObject).subscribe(

        data => {

          console.log(data);
          this.setTeamRequestList("ALL");
        }
      );
    }
    else {

      this._snackBar.open("No request selected!", "Dismiss");
    }

  }

  downloadReport() {
    this.userService.downloadReport().subscribe(data => {
      const generatedReport = data["report"];
      const byteArray = new Uint8Array(atob(generatedReport).split('').map(char => char.charCodeAt(0)));
      const file = new Blob([byteArray], {type: 'application/pdf'});
      FileSaver.saveAs(file, "Team Holiday Report");
    })
  }

  @Output() onSelectionChange = new EventEmitter<string>();
  requestType: any;
  notifySelectionChanged(type:string) {
    this.requestType=type;
  }
}

