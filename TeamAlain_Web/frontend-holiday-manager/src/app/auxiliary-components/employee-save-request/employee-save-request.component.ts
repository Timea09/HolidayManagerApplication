import {
  Component,
  ElementRef,
  EventEmitter,
  Input,
  OnChanges,
  OnInit,
  Output,
  QueryList, SimpleChanges,
  ViewChild,
  ViewChildren
} from '@angular/core';
import { FormGroup, FormControl, FormBuilder, Validators } from '@angular/forms';
import { UserRequestFormObject } from '../../shared/data-type/userRequestFormObject'
import { Request } from 'src/app/shared/data-type/request';
import { dateObjectToISO8601 } from "../../utils/DateConversion";
import { Router } from "@angular/router";
import * as FileSaver from "file-saver";
import { UserService } from "../../services/user.service";
import { MatTableDataSource } from "@angular/material/table";
import { FileInput } from "ngx-material-file-input";
import { start } from 'repl';
import {MatSnackBar} from "@angular/material/snack-bar";

export interface HolidayType {
  value: string;
  viewValue: string;
}

@Component({
  selector: 'app-employee-save-request',
  templateUrl: './employee-save-request.component.html',
  styleUrls: ['./employee-save-request.component.scss']
})
export class EmployeeSaveRequestComponent implements OnInit, OnChanges {

  @Input() selectedRequest: Request | null = null;
  @Output() onCreateRequest: EventEmitter<UserRequestFormObject> = new EventEmitter();
  @Output() onUpdateRequest: EventEmitter<UserRequestFormObject> = new EventEmitter();
  @Output() onCancelRequest: EventEmitter<null> = new EventEmitter();

  @ViewChild('datePicker') datePicker: ElementRef;

  private _usedRangeDates: Date[][];

  get usedRangeDates(): Date[][] {
    return this._usedRangeDates;
  }

  @Input()
  set usedRangeDates(usedRangeDates: Date[][]) {
    this._usedRangeDates = usedRangeDates;
  }

  @Input() selectionChanged:string;

  @Input() requestType:string;

  selectedHolidayType: string|undefined;

  requestFormGroup = this.formBuilder.group({
    holidayType: ['', Validators.required],
    start_date: ['', Validators.required],
    end_date: ['', Validators.required],
    substitute: ['', Validators.required],
    document: [null, Validators.required],
    extra_info: ['']
  })

  holidayTypes: HolidayType[] = [

    { value: "REST", viewValue: "Rest" },
    { value: "SPECIAL", viewValue: "Special" },
    { value: "UNPAID", viewValue: "Unpaid" }
  ]

  startDate: Date;

  constructor(private formBuilder: FormBuilder, private router: Router, private snackBar: MatSnackBar, private userService: UserService) { }

  ngOnInit(): void {
    this.startDate = new Date();
    this.startDate.setDate(this.startDate.getDate() + 1)
  }

  ngOnChanges(changes:SimpleChanges): void {
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
      startDate !== null && endDate !== null &&
      ((substitute !== null && (this.selectedHolidayType === "REST" || this.selectedHolidayType === "SPECIAL")) || this.selectedHolidayType === "UNPAID") &&
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

  showUpdateButtonCondition = (): boolean => {

    if (this.selectedRequest === null) {
      return false;
    }

    return this.selectedRequest?.status !== 'DECLINED';
  }

  emitCreatedRequest(buffer: Int8Array | null): void {

    const document = this.int8ArrayToByteArray(buffer);

    if (this.checkMandatoryFields(document)) {

      const myRequest: UserRequestFormObject = {

        // @ts-ignore
        holidayId: null,
        // @ts-ignore
        type: this.selectedHolidayType,
        startDate: dateObjectToISO8601(new Date(this.requestFormGroup.value.start_date!)) + "T00:00:00",
        endDate: dateObjectToISO8601(new Date(this.requestFormGroup.value.end_date!)) + "T00:00:00",
        substitute: this.requestFormGroup.value.substitute!,
        document: document,
        // @ts-ignore
        documentName: (this.requestFormGroup.controls["document"].value !== null) ? this.requestFormGroup.controls["document"].value.fileNames : null,
      }

      //clear form
      this.onCreateRequest.emit(myRequest);
      this.requestFormGroup.reset();
      Object.keys(this.requestFormGroup.controls).forEach(key => {
        // @ts-ignore
        this.requestFormGroup.controls[key].setErrors(null)
      });
    }

    this.requestFormGroup.reset();
    Object.keys(this.requestFormGroup.controls).forEach(key => {
      // @ts-ignore
      this.requestFormGroup.controls[key].setErrors(null)
    });
  }

  emitUpdatedRequest(buffer: Int8Array | null): void {

    const document = this.int8ArrayToByteArray(buffer);

    if (this.checkMandatoryFields(document)) {

      const myRequest: UserRequestFormObject = {

        // @ts-ignore
        holidayId: this.selectedRequest.holidayId,
        // @ts-ignore
        type: this.selectedHolidayType,
        startDate: dateObjectToISO8601(new Date(this.requestFormGroup.value.start_date!)) + "T00:00:00",
        endDate: dateObjectToISO8601(new Date(this.requestFormGroup.value.end_date!)) + "T00:00:00",
        substitute: this.requestFormGroup.value.substitute!,
        document: this.int8ArrayToByteArray(buffer),
        //@ts-ignore
        documentName: this.requestFormGroup.controls["document"].value !== null ? this.requestFormGroup.controls["document"].value.fileNames : null,
      }

      this.onUpdateRequest.emit(myRequest);
    }

    this.requestFormGroup.reset();
    Object.keys(this.requestFormGroup.controls).forEach(key => {
      // @ts-ignore
      this.requestFormGroup.controls[key].setErrors(null)
    });
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

    }
    else {

      this.emitCreatedRequest(null);
    }

  }

  updateRequest(): void {

    if (this.selectedRequest !== null) {

      if (this.selectedHolidayType === "SPECIAL") {

        // @ts-ignore
        console.log(this.requestFormGroup.value.document._files)

        // @ts-ignore
        this.requestFormGroup.value.document._files[0].arrayBuffer().then(buffer => {

          this.emitUpdatedRequest(new Int8Array(buffer));
        });

      }
      else {

        this.emitUpdatedRequest(null);
      }
    }
    else {

      alert("You must select a request to update by clicking on one of the eye buttons from the table!");
    }
  }

  cancelRequest(): void {

    this.selectedRequest = null;
    this.onCancelRequest.emit();
  }

  setFormFields(request: Request | null): void {

    //@ts ignore
    if (request !== null) {

      switch (request.type) {

        case "Rest Holiday": {

            this.selectedHolidayType = "REST";

            this.requestFormGroup.patchValue({

              holidayType: "REST",
              start_date: request.startDate,
              end_date: request.endDate,
              substitute: request.substitute,
              document: null,
              extra_info: request.extraInfo
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
            substitute: request.substitute,
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
            substitute: null,
            document: null,
            extra_info: request.extraInfo
          });

          break;
        }
      }
    }
    else {

      this.requestFormGroup.reset();
      this.selectedHolidayType = "";
    }
  }

  checkShowHRButton(): boolean {
    return this.selectedRequest !== null && this.selectedRequest.status === "APPROVED";
  }

  downloadFile() {

    const byteArray = new Uint8Array(atob(this.selectedRequest?.document).split('').map(char => char.charCodeAt(0)));
    const file = new Blob([byteArray], { type: 'application/octet-stream' });
    FileSaver.saveAs(file, this.selectedRequest?.documentName);
  }

  sendToHr() {
    this.userService.sendToHr(this.selectedRequest!.holidayId).subscribe(data => {

      const autocompletedDocument = data["document"];
      const byteArray = new Uint8Array(atob(autocompletedDocument).split('').map(char => char.charCodeAt(0)));

      const file = new Blob([byteArray], { type: 'application/octet-stream' });
      FileSaver.saveAs(file, "Autocompleted Document.pdf");

      this.snackBar.open("Document sent to HR!", "Dismiss")
    })
  }


  filterForPickedDays = (d: Date): boolean => {
    if (this.selectedRequest != null) {
      const startDate = new Date(this.selectedRequest!.startDate);
      startDate.setHours(0);
      const endDate = new Date(this.selectedRequest!.endDate);
      endDate.setHours(0);

      console.log(this.usedRangeDates);

      return !this.usedRangeDates.find(el => {
        return JSON.stringify(el[0]) !== JSON.stringify(startDate) &&
        JSON.stringify(el[1]) !== JSON.stringify(endDate) && d >= el[0] && d <= el[1]
      })
    }
    else {
      return !this.usedRangeDates.find(el => {
        return d >= el[0] && d <= el[1]
      })
    }

  }


  private clearFormFields() {
    this.selectedHolidayType=undefined;
    this.requestFormGroup.reset();
    Object.keys(this.requestFormGroup.controls).forEach(key =>{
      // @ts-ignore
      this.requestFormGroup.controls[key].setErrors(null)
    });
    this.selectedRequest=null
  }
}
