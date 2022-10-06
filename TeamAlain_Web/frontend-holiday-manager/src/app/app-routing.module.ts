import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import {LoginPageComponent} from "./components/login-page/login-page.component";
import { LandingPageAdminComponent } from './components/landing-page-admin/landing-page-admin.component';
import { TeamManagementComponent } from './components/landing-page-admin/team-management/team-management.component';
import { UserManagementComponent } from './components/landing-page-admin/user-management/user-management.component';
import {AuthguardService} from "./authguards/authguard.service";
import {AuthguardAdminService} from "./authguards/authguard-admin.service";
import { TeamLeadPageComponent } from './components/team-lead-page/team-lead-page.component';
import {EmployeeDashboardComponent} from "./components/employee-dashboard/employee-dashboard.component";
import {AuthguardTeamLeadService} from "./authguards/authguard-team-lead.service";
import {AuthguardEmployeeService} from "./authguards/authguard-employee.service";
import {ChangePasswordPageComponent} from "./components/change-password-page/change-password-page.component";


const routes: Routes = [
  {path:"login",component:LoginPageComponent},
  {path: 'admin', component: LandingPageAdminComponent, canActivate: [AuthguardService,AuthguardAdminService]},
  {path: 'user-management', component: UserManagementComponent, canActivate: [AuthguardService,AuthguardAdminService]},
  {path: 'team-management', component: TeamManagementComponent, canActivate: [AuthguardService,AuthguardAdminService]},
  {path:"team-lead", component:TeamLeadPageComponent, canActivate: [AuthguardService,AuthguardTeamLeadService]},
  {path: 'employee', component: EmployeeDashboardComponent, canActivate: [AuthguardService,AuthguardEmployeeService]},
  {path: 'change-password', component: ChangePasswordPageComponent, canActivate:[AuthguardService]},

  ////////////////////////////////////////////////////////////////////////////////
  //always last, in case of any invalid url, the user will be redirected to /login
  {path: "**",redirectTo:'login'},
]


@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})

export class AppRoutingModule { }
