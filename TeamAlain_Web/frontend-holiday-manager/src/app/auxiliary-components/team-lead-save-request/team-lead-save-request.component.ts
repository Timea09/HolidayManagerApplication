import {EventEmitter, SimpleChanges} from '@angular/core';
import { Component, Input, OnInit, Output } from '@angular/core';
import { Validators, FormBuilder } from '@angular/forms';
import { Router } from '@angular/router';
import * as FileSaver from 'file-saver';
import { TeamLeadService } from 'src/app/services/team-lead.service';
import { PeriodObject } from 'src/app/shared/data-type/periodObject';
import { User } from 'src/app/shared/data-type/User';
import { UserRequestFormObject } from 'src/app/shared/data-type/userRequestFormObject';
import { dateObjectToISO8601 } from 'src/app/utils/DateConversion';
import { HolidayType } from '../employee-save-request/employee-save-request.component';
import { Request } from 'src/app/shared/data-type/request';
import { MatSnackBar } from '@angular/material/snack-bar';
import {TeamLeadRequestFormObject} from "../../shared/data-type/teamLeadRequestFormObject";
import {UserService} from "../../services/user.service";
import {TeamLeadRequestFormUpdateObject} from "../../shared/data-type/teamLeadRequestFormUpdateObject";
import {FileInput} from "ngx-material-file-input";


@Component({
  selector: 'app-team-lead-save-request',
  templateUrl: './team-lead-save-request.component.html',
  styleUrls: ['./team-lead-save-request.component.scss']
})
export class TeamLeadSaveRequestComponent implements OnInit {

  @Input() selectedRequest: Request | null = null;
  @Input() requestType:string;
  @Output() onCreateRequest: EventEmitter<TeamLeadRequestFormObject> = new EventEmitter();
  @Output() onUpdateRequest: EventEmitter<TeamLeadRequestFormObject> = new EventEmitter();
  @Output() onCancelRequest: EventEmitter<null> = new EventEmitter();

  selectedHolidayType: string|undefined;
  selectedSubstitute: User;
  teamLeadOptions: User[];

  requestFormGroup = this.formBuilder.group({
    holidayType: ['', Validators.required],
    start_date: ['', Validators.required],
    end_date: ['', Validators.required],
    substitute: [-1, Validators.required],
    document: [null, Validators.required],
  })

  holidayTypes: HolidayType[] = [

    {value: "REST", viewValue: "Rest"},
    {value: "SPECIAL", viewValue: "Special"},
    {value: "UNPAID", viewValue: "Unpaid"}
  ]

  startDate: Date;

  constructor(private formBuilder: FormBuilder, private router: Router, private teamLeadService: TeamLeadService, private snackBar: MatSnackBar, private userService: UserService) {
  }

  ngOnInit(): void {
    this.startDate = new Date();
    this.startDate.setDate(this.startDate.getDate() + 1)
  }


  ngOnChanges(changes:SimpleChanges): void {
    console.log(changes);
    if(changes["requestType"]){
      this.clearFormFields();
    }
    else if(changes["selectedRequest"]){
      this.setFormFields(this.selectedRequest);
    }
  }

  checkMandatoryFields(document: Array<number> | null): boolean {

    const startDate = this.requestFormGroup.value.start_date;
    const endDate = this.requestFormGroup.value.end_date;
    const substitute = this.requestFormGroup.value.substitute;

    return this.selectedHolidayType !== null &&
      startDate !== null && endDate !== null && substitute !== -1 &&
      ((document !== null && this.selectedHolidayType === "SPECIAL") || this.selectedHolidayType !== "SPECIAL");
  }

  int8ArrayToByteArray(buffer: Int8Array | null): Array<number> | null {

    if (buffer !== null) {

      const byteArray = new Array<number>(buffer.length);

      for (let i = 0; i < buffer.length; i++) {

        byteArray[i] = buffer[i];
      }

      return byteArray;
    }

    return null;
  }

  showCancelButtonCondition(): boolean {

    return this.selectedRequest !== null && new Date(this.selectedRequest.startDate).getTime() > Date.now()
  }

  showUpdateButtonCondition = (): boolean => {

    if (this.selectedRequest === null) {

      return false;
    }

    return this.selectedRequest?.status !== 'DECLINED' && new Date(this.selectedRequest.startDate).getTime() > Date.now();
  }

  emitCreatedRequest(buffer: Int8Array | null): void {

    const document = this.int8ArrayToByteArray(buffer);

    if (this.checkMandatoryFields(document)) {

      const myRequest: TeamLeadRequestFormObject = {

        // @ts-ignore
        holidayId: null,
        type: this.selectedHolidayType,
        startDate: dateObjectToISO8601(new Date(this.requestFormGroup.value.start_date!)) + "T00:00:00",
        endDate: dateObjectToISO8601(new Date(this.requestFormGroup.value.end_date!)) + "T00:00:00",
        substituteId: this.requestFormGroup.value.substitute!,
        document: document,
        //@ts-ignore
        documentName: (this.requestFormGroup.controls["document"].value !== null) ? this.requestFormGroup.controls["document"].value.fileNames : null,
      }

      this.onCreateRequest.emit(myRequest);
      this.requestFormGroup.reset();
      Object.keys(this.requestFormGroup.controls).forEach(key => {
        // @ts-ignore
        this.requestFormGroup.controls[key].setErrors(null)
      });
    }
  }

  emitUpdatedRequest(buffer: Int8Array | null): void {

    const document = this.int8ArrayToByteArray(buffer);

    if (this.checkMandatoryFields(document)) {

      const myRequest: TeamLeadRequestFormUpdateObject = {

        // @ts-ignore
        holidayId: this.selectedRequest.holidayId,
        type: this.selectedHolidayType,
        startDate: dateObjectToISO8601(new Date(this.requestFormGroup.value.start_date!)) + "T00:00:00",
        endDate: dateObjectToISO8601(new Date(this.requestFormGroup.value.end_date!)) + "T00:00:00",
        oldStartDate: dateObjectToISO8601(new Date(this.selectedRequest?.startDate!)) + "T00:00:00",
        oldEndDate: dateObjectToISO8601(new Date(this.selectedRequest?.endDate!)) + "T00:00:00",
        substituteId: this.requestFormGroup.value.substitute!,
        document: this.int8ArrayToByteArray(buffer),
        //@ts-ignore
        documentName: this.requestFormGroup.controls["document"].value !== null ? this.requestFormGroup.controls["document"].value.fileNames : null,
      }

      this.onUpdateRequest.emit(myRequest);

      this.requestFormGroup.reset();
      Object.keys(this.requestFormGroup.controls).forEach(key => {
        // @ts-ignore
        this.requestFormGroup.controls[key].setErrors(null)
      });
    }
  }

  createRequest(): void {

    this.selectedRequest = null;

    if (this.selectedHolidayType === "SPECIAL") {

      // @ts-ignore
      this.requestFormGroup.value.document._files[0].arrayBuffer().then(buffer => {

        this.emitCreatedRequest(new Int8Array(buffer));
      });
      //@ts-ignore
      console.log(this.requestFormGroup.controls["document"].value.fileNames);

    } else {

      this.emitCreatedRequest(null);
    }

  }

  updateRequest(): void {

    if (this.selectedRequest !== null) {

      //this.teamLeadService.deleteCurrentSubstitute()

      if (this.selectedHolidayType === "SPECIAL") {

        // @ts-ignore
        this.requestFormGroup.value.document._files[0].arrayBuffer().then(buffer => {

          this.emitUpdatedRequest(new Int8Array(buffer));
        });
      } else {

        this.emitUpdatedRequest(null);
      }
    } else {

      alert("You must select a request to update by clicking on one of the eye buttons from the table!");
    }
  }

  cancelRequest(): void {

    this.selectedRequest = null;
    this.onCancelRequest.emit();
  }

  getUpdateTeamLeads(startDate: string, endDate: string)
  {
    this.userService.getTeamId().subscribe(
      id =>
      {
        this.teamLeadService.getAllAvailableTeamLeadsForUpdate(startDate!, endDate!, id, this.selectedRequest?.holidayId!).subscribe(
          {
            next: (list) =>
            {
              this.teamLeadOptions= list;
              console.log(list)
            },

            error: (err) =>
            {
              if(err.status == 406)
              {
                this.snackBar.open("There are no substitutes available for this period","Dismiss");
              }
              else if (err.status == 409)
              {
                this.snackBar.open("You are substituting for someone else during this period or you have a holiday overlapping with it","Dismiss");
              }

            }
          }
        );
      }
    )
  }
  setFormFields(request: Request | null): void {

    if (request !== null) {

      this.teamLeadService.getSubstituteId(request?.holidayId!).subscribe(
        s =>
        {
            //@ts-ignore
            console.log(s.substituteId);
            this.getUpdateTeamLeads(request.startDate,request.endDate);
            switch (request.type) {

              case "Rest Holiday": {

                this.selectedHolidayType = "REST";

                this.requestFormGroup.patchValue({

                  holidayType: "REST",
                  start_date: request.startDate,
                  end_date: request.endDate,
                  //@ts-ignore
                  substitute: s.substituteId,
                  document: null,
                });

                break;
              }

              case "Special Holiday": {

                this.selectedHolidayType = "SPECIAL";

                // @ts-ignore
                const byteArray = new Uint8Array(atob(request.document).split('').map(char => char.charCodeAt(0)));

                console.log(byteArray);

                const blob = new Blob([byteArray], {type: 'application/octet-stream'});

                // @ts-ignore
                let file = new File([blob], request.documentName);

                let b = new FileInput([file]);

                this.requestFormGroup.patchValue({

                  holidayType: "SPECIAL",
                  start_date: request.startDate,
                  end_date: request.endDate,
                  //@ts-ignore
                  substitute: s.substituteId,
                  //@ts-ignore
                  document: b,
                  extra_info: request.extraInfo
                });
                break;
              }

              case "Unpaid Holiday": {

                this.selectedHolidayType = "UNPAID";

                this.requestFormGroup.patchValue({

                  holidayType: "UNPAID",
                  start_date: request.startDate,
                  end_date: request.endDate,
                  //@ts-ignore
                  substitute: s.substituteId,
                  document: null,
                });

                break;
              }
            }


      }
    );}
    else {

        this.requestFormGroup.reset();
        this.selectedHolidayType = "";
      }

  }

  downloadFile() {

    const byteArray = new Uint8Array(atob(this.selectedRequest?.document).split('').map(char => char.charCodeAt(0)));
    const file = new Blob([byteArray], {type: 'application/octet-stream'});
    FileSaver.saveAs(file, this.selectedRequest?.documentName);
  }

  onInputDate()
  {
    const period : PeriodObject =
    {
      startDate : dateObjectToISO8601(new Date(this.requestFormGroup.value.start_date!)),
      endDate : dateObjectToISO8601(new Date(this.requestFormGroup.value.end_date!))
    }
    console.log(period)
    if( this.selectedRequest !== null )
    {
      this.userService.getTeamId().subscribe(
        id =>
        {
          this.teamLeadService.getAllAvailableTeamLeadsForUpdate(period.startDate!, period.endDate!, id, this.selectedRequest?.holidayId!).subscribe(
            {
              next: (list) =>
              {
                this.teamLeadOptions= list;
                console.log(list)
              },

              error: (err) =>
              {
                if(err.status == 406)
                {
                  this.snackBar.open("There are no substitutes available for this period","Dismiss");
                }
                else if (err.status == 409)
                {
                  this.snackBar.open("You are substituting for someone else during this period or you have a holiday overlapping with it","Dismiss");
                }

              }
            }
          );
        }
      )

    }

    else {
      this.teamLeadService.getAllAvailableTeamLeads(period).subscribe(
        {
          next: (list) =>
          {
            this.teamLeadOptions= list;
            console.log(list)
          },

          error: (err) =>
          {
            if(err.status == 406)
            {
              this.snackBar.open("There are no substitutes available for this period","Dismiss");

            }
            else if (err.status == 409)
            {
              this.snackBar.open("You are substituting for someone else during this period or you have a holiday overlapping with it.","Dismiss");
            }

          }
        }
      );
    }
  }

  checkShowHRButton(): boolean {
    return this.selectedRequest !== null && this.selectedRequest.status === "APPROVED";
  }

  sendToHr() {

    this.userService.sendToHr(this.selectedRequest!.holidayId).subscribe( data => {

      const autocompletedDocument = data["document"];
      const byteArray = new Uint8Array(atob(autocompletedDocument).split('').map(char => char.charCodeAt(0)));

      const file = new Blob([byteArray], {type: 'application/octet-stream'});
      console.log(data)
      FileSaver.saveAs(file, "Autocompleted Document.pdf");

      this.snackBar.open("Document sent to HR!","Dismiss");
    })
  }

  private clearFormFields() {
    this.selectedHolidayType=undefined;
    this.requestFormGroup.reset();
    Object.keys(this.requestFormGroup.controls).forEach(key =>{
      // @ts-ignore
      this.requestFormGroup.controls[key].setErrors(null)
    });
    this.selectedRequest=null;
  }
}
