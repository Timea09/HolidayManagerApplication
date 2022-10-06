import { Component, OnInit } from '@angular/core';
import { UserAdmin } from '../../../shared/data-type/userAdmin'

import { MatTableDataSource } from '@angular/material/table';
import { UserService } from "../../../services/user.service";
import { User } from 'src/app/shared/data-type/User';
import { NonNullableFormBuilder } from '@angular/forms';
import { Validators } from '@angular/forms';
import { MatSnackBar } from "@angular/material/snack-bar";
import { MatDialog } from '@angular/material/dialog';
import { ConfirmationDialogComponent } from '../../dialog-boxes/confirmation-dialog/confirmation-dialog.component';




@Component({
  selector: 'app-user-management',
  templateUrl: './user-management.component.html',
  styleUrls: ['./user-management.component.scss']
})
export class UserManagementComponent implements OnInit {



  users?: UserAdmin[];


  constructor(private formBuilder: NonNullableFormBuilder, private userService: UserService, private snackbar: MatSnackBar, private dialog: MatDialog) { }

  dataSource: UserAdmin[];
  formSource = new UserAdmin();
  user = new UserAdmin();


  userSelected: UserAdmin;
  selected: Boolean;

  columns = [
    {
      columnDef: 'position',
      cell: (element: UserAdmin) => `${element.firstName}` + ' ' + `${element.lastName}`,
    }

  ];

  displayedColumns = this.columns.map(c => c.columnDef);

  ngOnInit(): void {
    this.userService.getAllUsers().subscribe(data => {
      this.users = data
      this.dataSource = data;
      console.log(data);
    });
  }


  createUser(newUser: UserAdmin | null | undefined): void {
    this.userService.createUser(newUser!).subscribe(
      {
        next: (res) => {
          this.users?.push(res)
          this.snackbar.open("Created succesfully", "Ok");
        },
        error: err => {
          if (err.status == 409) {//duplicate email


            this.snackbar.open("Cannot create a new user with an existing email", "Got it!");
          }
        }
      });



  }


  seeUser(user: UserAdmin): void {
    this.userSelected = user;
  }


  findIndexToUpdate(newItem: UserAdmin): number {
    let foundIndex: number;
    // @ts-ignore
    let index = this.users?.indexOf(this.users.find(user => user.id === newItem.id))
    return index!;
  }



  updateUser(user: UserAdmin): void {
    console.log(user);
    this.userService.updateUser(user).subscribe(
      {
        next: (res) => {
          let index: number = this.findIndexToUpdate(res);
          // @ts-ignore
          //this.users[index]== res;
          console.log(index);
          console.log(res)
          // @ts-ignore
          this.users[index] = res;
          //console.log(this.users[index])
          console.log(this.users)
          this.snackbar.open("Updated succesfully", "Ok");
        }
      }
    );


  }
  deleteUser(user: UserAdmin): void {
    //console.log(user.id)

    const dialogResponse = this.dialog.open(ConfirmationDialogComponent, {
      width: '250px', data: "Are you sure you want to delete user" + user.firstName + " " + user.lastName + " ?",
      autoFocus: false
    });

    dialogResponse.afterClosed().subscribe(result => {
      if (result){
        this.userService.deleteUsers(user.id!).subscribe(
          {
            next: (res) => {

              this.users!.splice(this.users!.indexOf(user), 1);
              this.snackbar.open("Deleted succesfully", "Ok");
            },
            error: err => {
              if (err.status == 409) {

                this.snackbar.open("Cannot delete a user who is a team lead", "Got it!");
              }
            }
          }
        );
      }
    });

  }

  cancel() {

    // @ts-ignore
    this.userSelected = null;
  }

}
