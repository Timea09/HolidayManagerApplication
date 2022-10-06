import { NgModule} from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import {MatTableModule} from '@angular/material/table'
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { LoginPageComponent } from './components/login-page/login-page.component';
import {HTTP_INTERCEPTORS, HttpClientModule} from "@angular/common/http";
import { LandingPageAdminComponent } from './components/landing-page-admin/landing-page-admin.component';
import { UserManagementComponent } from './components/landing-page-admin/user-management/user-management.component';
import { TeamManagementComponent } from './components/landing-page-admin/team-management/team-management.component';
import { AddUserComponent } from './components/landing-page-admin/user-management/add-user/add-user.component';
import { AddTeamComponent } from './components//landing-page-admin/team-management/add-team/add-team.component';
import {AuthentificationInterceptor} from "./services/authentification.interceptor";
import { TeamLeadPageComponent } from './components/team-lead-page/team-lead-page.component';
import {MatButtonToggleModule} from "@angular/material/button-toggle";
import { EmployeeDashboardComponent } from './components/employee-dashboard/employee-dashboard.component';
import { EmployeeHeaderComponent } from './auxiliary-components/employee-header/employee-header.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { EmployeeSaveRequestComponent } from './auxiliary-components/employee-save-request/employee-save-request.component';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MatDatepickerModule} from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatInputModule } from '@angular/material/input';
import { MaterialFileInputModule, NGX_MAT_FILE_INPUT_CONFIG } from 'ngx-material-file-input';
import {MatListModule} from '@angular/material/list';
import { RequestListComponent } from './auxiliary-components/request-list/request-list.component';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { DateAdapter, MAT_DATE_FORMATS, MAT_DATE_LOCALE } from "@angular/material/core";
import { MomentDateAdapter } from '@angular/material-moment-adapter';
import {MatAutocompleteModule} from "@angular/material/autocomplete";
import {ConfirmationDialogComponent} from "./components/dialog-boxes/confirmation-dialog/confirmation-dialog.component";
import {MatDialogModule} from "@angular/material/dialog";
import {ISO8601_FORMAT} from "./utils/DateConversion";
import {MatSortModule} from "@angular/material/sort";
import { NavbarComponent } from './components/navbar/navbar.component';
import { TeamRequestsListComponent } from './auxiliary-components/team-requests-list/team-requests-list.component';
import {TeamRequestViewComponent} from "./components/team-lead-page/team-request-view/team-request-view.component";
import {StompService} from "./services/stomp.service";
import {MatBadgeModule} from "@angular/material/badge";
import {MatMenuModule} from "@angular/material/menu";
import { NotificationDialogComponent } from './components/dialog-boxes/notification-dialog/notification-dialog.component';
import { ChangePasswordPageComponent } from './components/change-password-page/change-password-page.component';
import { TeamLeadSaveRequestComponent } from './auxiliary-components/team-lead-save-request/team-lead-save-request.component';


@NgModule({
  declarations: [
    AppComponent,
    LoginPageComponent,
    LandingPageAdminComponent,
    UserManagementComponent,
    TeamManagementComponent,
    AddUserComponent,
    AddTeamComponent,
    TeamLeadPageComponent,
    EmployeeDashboardComponent,
    EmployeeHeaderComponent,
    EmployeeSaveRequestComponent,
    RequestListComponent,
    TeamManagementComponent,
    AddTeamComponent,
    ConfirmationDialogComponent,
    NavbarComponent,
    TeamRequestsListComponent,
    TeamRequestViewComponent,
    NotificationDialogComponent,
    ChangePasswordPageComponent,
    TeamLeadSaveRequestComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    BrowserAnimationsModule,
    MatFormFieldModule,
    MatInputModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatIconModule,
    HttpClientModule,
    MatTableModule,
    MatDatepickerModule,
    MatSelectModule,
    MatButtonToggleModule,
    MatNativeDateModule,
    MaterialFileInputModule,
    FormsModule,
    MatListModule,
    MatSnackBarModule,
    MatAutocompleteModule,
    MatDialogModule,
    MatSnackBarModule,
    AppRoutingModule,
    BrowserAnimationsModule,
    MatFormFieldModule,
    MatInputModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatIconModule,
    HttpClientModule,
    MatTableModule,
    MatDatepickerModule,
    MatSelectModule,
    MatButtonToggleModule,
    MatNativeDateModule,
    MaterialFileInputModule,
    FormsModule,
    MatSortModule,
    MatBadgeModule,
    MatMenuModule,
  ],
  providers: [
    StompService,
    { provide: HTTP_INTERCEPTORS, useClass: AuthentificationInterceptor,  multi: true},
    { provide: DateAdapter, useClass: MomentDateAdapter, deps: [MAT_DATE_LOCALE] },
    { provide: MAT_DATE_FORMATS, useValue: ISO8601_FORMAT }
  ],
  bootstrap: [AppComponent]
})

export class AppModule { }
