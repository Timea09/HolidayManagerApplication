import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {Request} from "../../../shared/data-type/request";
import {FormBuilder, Validators} from "@angular/forms";
import * as FileSaver from "file-saver";
import {RequestStatusObject} from "../../../shared/data-type/requestStatusObject";

@Component({
  selector: 'app-team-request-view',
  templateUrl: './team-request-view.component.html',
  styleUrls: ['./team-request-view.component.scss']
})
export class TeamRequestViewComponent implements OnInit {

  @Input() shownRequest: Request;
  @Output() onRequestStatusSet: EventEmitter<RequestStatusObject> = new EventEmitter<RequestStatusObject>();

  constructor(private formBuilder: FormBuilder) { }

  formGroup = this.formBuilder.group({
    holidayType: [''],
    startDate: [''],
    endDate: [''],
    substitute: [''],
    document: [{ value: undefined, disabled: true }],
    extraInfo: ['']
  })

  ngOnInit(): void {

  }

  setRequestStatus(status: string): void {

    this.onRequestStatusSet.emit(
      {
        userId: this.shownRequest.userId,
        holidayId: this.shownRequest.holidayId,
        status: status,
        extraInfo: (status === "EXTRA_INFO") ? this.formGroup.value.extraInfo! : null
      }
    );
    window.location.reload();
  }

  downloadFile() {
    if (this.shownRequest.document) {
      const byteArray = new Uint8Array(atob(this.shownRequest.document).split('').map(char => char.charCodeAt(0)));
      const file = new Blob([byteArray], {type: 'application/pdf'});
      FileSaver.saveAs(file, this.shownRequest.documentName);
    }
  }
}
