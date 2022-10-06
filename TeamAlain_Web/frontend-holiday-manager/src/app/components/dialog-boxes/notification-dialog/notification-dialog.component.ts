import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from "@angular/material/dialog";

@Component({
  selector: 'app-notification-dialog',
  templateUrl: './notification-dialog.component.html',
  styleUrls: ['./notification-dialog.component.scss']
})
export class NotificationDialogComponent implements OnInit {

  constructor(public dialog : MatDialog, public dialogRef : MatDialogRef<NotificationDialogComponent>,
              @Inject(MAT_DIALOG_DATA) public notificationMessage : string) { }

  ngOnInit(): void {
  }

}
