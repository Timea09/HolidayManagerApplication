import {Component, EventEmitter, OnInit, Output} from '@angular/core';
import {FormBuilder, Validators} from "@angular/forms";
import {UserService} from "../../services/user.service";
import {UserCredentials} from "../../shared/data-type/user-credentials";
import {Router} from "@angular/router";
import {parseJwt} from "../../utils/JWTParser";
import {MatSnackBar} from '@angular/material/snack-bar';
import {ConfirmationDialogComponent} from "../dialog-boxes/confirmation-dialog/confirmation-dialog.component";
import {MatDialog} from "@angular/material/dialog";
import {NotificationDialogComponent} from "../dialog-boxes/notification-dialog/notification-dialog.component";

@Component({
  selector: 'app-login-page',
  templateUrl: './login-page.component.html',
  styleUrls: ['./login-page.component.scss']
})
export class LoginPageComponent implements OnInit {

  @Output() clickCreate = new EventEmitter<JSON>();
  userCredentialsFormGroup = this.formBuilder.group({
    email: ["",[Validators.required, Validators.email]] ,
    password: ["",[Validators.required]],
  })
  public hide:boolean = true;

  constructor(
    private formBuilder: FormBuilder,
    private router: Router,
    private userService: UserService,
    private _snackBar: MatSnackBar,
    private dialog:MatDialog,
    ) {}

  ngOnInit(): void {
  }


  getEmailErrorMessage() {
    const email = this.userCredentialsFormGroup.controls["email"];
    if (email.hasError('required')) {
      return 'E-Mail cannot be empty!';
    }
    return email.hasError('email') ? 'E-Mail is not valid!' : '';
  }


  getPasswordErrorMessage() {
    return this.userCredentialsFormGroup.controls["password"].hasError('required') ? 'Password cannot be empty!' : '';
  }

  public loginUser() {
    const valuesFromForm = this.userCredentialsFormGroup.value;
    const userCredentials: UserCredentials = {
      email: valuesFromForm.email!,
      password: valuesFromForm.password!,
    };
    // @ts-ignore
    if (this.getPasswordErrorMessage() == "" && this.getEmailErrorMessage() == "") {
      this.userService.loginUser(userCredentials).subscribe({
        next: response => {
          document.cookie = "Token = " + response['token'] + "; path=/";
          const parsedJWT = parseJwt(response['token']);
          localStorage.setItem("role",parsedJWT['roles'])

          if (parsedJWT['roles'].includes("ADMIN"))
            this.router.navigate(['../admin']);
          else if (parsedJWT.roles.includes("TEAM_LEAD"))
            this.router.navigate(['../team-lead']);
          else if (parsedJWT.roles.includes("EMPLOYEE"))
            this.router.navigate(['../employee']);
          },
        error: err => {

          this.dialog.open(NotificationDialogComponent, {
            width: '250px', data: "There is no account matching your credentials!",
            autoFocus: false
          });
        }
      });
    }
  }

}
