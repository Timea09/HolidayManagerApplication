import {Component, EventEmitter, OnInit, Output} from '@angular/core';
import {UserService} from 'src/app/services/user.service';
import {UserRequestFormObject} from "../../shared/data-type/userRequestFormObject";
import {dateObjectToISO8601} from "../../utils/DateConversion";
import {Request} from "../../shared/data-type/request";
import {MatTableDataSource} from "@angular/material/table";
import {MatSnackBar} from "@angular/material/snack-bar";
import {ConfirmationDialogComponent} from "../dialog-boxes/confirmation-dialog/confirmation-dialog.component";
import {MatDialog} from "@angular/material/dialog";

@Component({
  selector: 'app-employee-dashboard',
  templateUrl: './employee-dashboard.component.html',
  styleUrls: ['./employee-dashboard.component.scss']
})
export class EmployeeDashboardComponent implements OnInit {

  daysLeft: Number;
  requestList: MatTableDataSource<Request>;
  requestListTemp: Request[];
  usedRangeDates: Date[][] = [];
  selectedRequest: Request | null = null;
  selectedRequestType: string;

  constructor(private userService: UserService, private _snackBar: MatSnackBar, private dialog : MatDialog) { }



  ngOnInit(): void {

    this.updateDaysLeft();
  }

  updateDaysLeft():void{
    //@ts-ignore
    this.userService.getVacationDays().subscribe((v) => { this.daysLeft = v.daysLeft; });
  }

  onNotify():void{
    this.updateDaysLeft();
    this.setRequestList(this.selectedRequestType);
  }

  setRequestList(type: string) {
    this.selectedRequestType=type;
    this.userService.getHolidayRequestsByType(type).subscribe(
      response => {
        this.requestListTemp = response;
        
        this.requestListTemp.forEach(request => {

          let startDate = new Date(request.startDate);
          let endDate = new Date(request.endDate);

            if(request.status !== 'DECLINED'){

              console.log(startDate);
              console.log(endDate);

              this.usedRangeDates.push([startDate,endDate]);
            }
            else {

              console.log(this.usedRangeDates);

              for (let index = 0; index < this.usedRangeDates.length; index++) {

                if (JSON.stringify(this.usedRangeDates[index][0]) === JSON.stringify(startDate) && 
                    JSON.stringify(this.usedRangeDates[index][1]) === JSON.stringify(endDate)) {

                  this.usedRangeDates.splice(index, 1);
                  break;
                }
              }

              console.log(this.usedRangeDates);

            }
            request.startDate = dateObjectToISO8601(startDate);
            request.endDate = dateObjectToISO8601(endDate);
          }
        );
        this.requestList = new MatTableDataSource<Request>(this.requestListTemp);
      }
    );
  }

  setSelectedRequest(request: Request | null) {

    this.selectedRequest = request;
  }

  makeRequest(requestForm: UserRequestFormObject) {
    this.userService.makeUserRequest(requestForm).subscribe(

      data => {
        let startDate = new Date(data.startDate);
        let endDate = new Date(data.endDate);
        console.log('makeRequest --> ' + data);
        if(data.status !== 'DECLINED'){
          this.usedRangeDates.push([startDate,endDate]);
        }
        data.startDate = dateObjectToISO8601(startDate);
        data.endDate = dateObjectToISO8601(endDate);
        this.requestListTemp.push(data);
        this.requestList.data = this.requestListTemp;
      },
      error => {

        this._snackBar.open("Holiday request is overlapping with another one!", "Dismiss");
      }
    );
  }

  updateRequest(requestForm: UserRequestFormObject) {

    this.userService.updateUserRequest(requestForm).subscribe(

      data => {

        let startDate = new Date(data.startDate);
        let endDate = new Date(data.endDate);
        data.startDate = dateObjectToISO8601(startDate);
        data.endDate = dateObjectToISO8601(endDate);

        this.requestListTemp.forEach((request,index) => {
          if(request.holidayId==data.holidayId && request.userId==data.userId){            
            this.requestListTemp[index] = data;
            this.usedRangeDates[index]=([startDate,endDate]);
          }
        })
        this.requestList.data=this.requestListTemp;
        this.selectedRequest=null;
      },
      error => {

        this._snackBar.open("Holiday request is overlapping with another one!", "Dismiss");
      }

    );
  }

  cancelRequest(event: null) {

    const dialogResponse = this.dialog.open(ConfirmationDialogComponent, {
      width: '250px', data: "Are you sure you want to cancel this request?",
      autoFocus: false
    });
    dialogResponse.afterClosed().subscribe(result => {
      if (result) {
        // @ts-ignore
        this.userService.cancelUserRequest(this.selectedRequest?.holidayId).subscribe(

          data => {

            this.requestListTemp.forEach((request,index)=>{
              if(request.holidayId==this.selectedRequest!.holidayId && request.userId==this.selectedRequest!.userId){
                this.requestListTemp.splice(index,1);
                this.usedRangeDates.splice(index,1);
              }
            })
            this.requestList.data=this.requestListTemp;
            this.selectedRequest=null;
          }
        );
      }
      else { this.selectedRequest = null}
    });


  }

  @Output() onSelectionChange = new EventEmitter<string>();
  requestType: any;
  notifySelectionChanged(type:string) {
    this.requestType=type;
  }
}
