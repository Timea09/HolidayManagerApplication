import { Component, OnInit } from '@angular/core';
import {FormBuilder, FormControl, FormGroup, FormGroupDirective, NgForm, Validators} from "@angular/forms";
import {UserService} from "../../services/user.service";
import {UserCredentials} from "../../shared/data-type/user-credentials";
import {UserChangePassword} from "../../shared/data-type/userChangePassword";
import {parseJwt} from "../../utils/JWTParser";
import {NotificationDialogComponent} from "../dialog-boxes/notification-dialog/notification-dialog.component";
import {ErrorStateMatcher} from "@angular/material/core";
import {MatDialog} from "@angular/material/dialog";
import {Router} from "@angular/router";
import {CookieService} from "ngx-cookie-service";

export class MyErrorStateMatcher implements ErrorStateMatcher {
  isErrorState(control: FormControl | null, form: FormGroupDirective | NgForm | null): boolean {
    // @ts-ignore
    const invalidParent = !!(
      control
      && control.parent
      && control.parent.invalid
      && control.parent.dirty
      && control.parent.hasError('notSame'));
    return (invalidParent);
  }
}

@Component({
  selector: 'app-change-password-page',
  templateUrl: './change-password-page.component.html',
  styleUrls: ['./change-password-page.component.scss']
})
export class ChangePasswordPageComponent implements OnInit {
  old_password_hide: boolean =true;
  new_password_hide: boolean =true;
  confirm_password_hide: boolean =true;

  passwordResetFormGroup = this.formBuilder.group({
      old_password: [""] ,
      new_password: [""],
      confirm_new_password: [""],
    },
    { validators: [this.checkPasswords] })
  matcher = new MyErrorStateMatcher();

  checkPasswords(group: FormGroup) {
    let pass = group.controls['new_password'].value;
    let confirmPass = group.controls['confirm_new_password'].value
    return pass === confirmPass ? null : { notSame: true }
  }


  constructor(private formBuilder: FormBuilder, private userService:UserService,private dialog:MatDialog, private router:Router, private cookieService:CookieService) { }

  ngOnInit(): void {
  }

  clearFormGroup(){
    this.passwordResetFormGroup.reset();
    Object.keys(this.passwordResetFormGroup.controls).forEach(key =>{
      // @ts-ignore
      this.passwordResetFormGroup.controls[key].setErrors(null)
    });
  }

  resetPassword() {
    const valuesFromForm = this.passwordResetFormGroup.value;
    if(!this.passwordResetFormGroup.hasError('notSame') &&
      !this.passwordResetFormGroup.hasError('required', 'old_password') &&
      !this.passwordResetFormGroup.hasError('required', 'new_password')) {
      const resetPasswordBody: UserChangePassword = {
        oldPassword: valuesFromForm.old_password!,
        newPassword: valuesFromForm.new_password!
      };
      this.userService.changePassword(resetPasswordBody).subscribe({
        next: response => {
          this.clearFormGroup()
          const dialogSuccess = this.dialog.open(NotificationDialogComponent, {
            //password changed with success
            width: '250px', data: "Your password has been changed with success!",
            autoFocus: false
          });
          dialogSuccess.afterClosed().subscribe(next => {
            const token = this.cookieService.get("Token");
            const parsedToken = parseJwt(token);
            if(parsedToken["roles"].includes("EMPLOYEE")){
              this.router.navigate(["/employee"]);
            }
            else if(parsedToken["roles"].includes("TEAM_LEAD")){
              this.router.navigate(["/team-lead"]);
            }
          })
        },
        error: err => {
          if(err.status==409){
            this.dialog.open(NotificationDialogComponent, {
              //this happens in case the user is not found in the user database
              //scenario: the admin will delete this user, while the user is connected to the account
              //probability: almost 0
              width: '250px', data: "Your account may not exist anymore!",
              autoFocus: false
            });
          }
          else if(err.status==400){
            this.dialog.open(NotificationDialogComponent, {
              //this happens when the old password submitted from frontend doesn't match the password linked
              //to the user existent in the database
              width: '250px', data: "The old password you entered does not match the password linked to this account!",
              autoFocus: false
            });
          }
        }
      });
    }
  }
}
