import { Injectable } from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {Observable} from "rxjs";
import {UserCredentials} from "../shared/data-type/user-credentials";
import {UserAdmin} from "../shared/data-type/userAdmin";
import {Request} from "../shared/data-type/request";
import {UserRequestFormObject} from "../shared/data-type/userRequestFormObject";
import { User } from '../shared/data-type/User';
import { Notification } from "../shared/data-type/notification";
import {UserChangePassword} from "../shared/data-type/userChangePassword";
const LOGIN = "http://localhost:8090/auth/login";
const GET_VACATION_DAYS = "http://localhost:8090/user/get-days-left";
const ADD_USER = 'http://localhost:8090/user/add'
const GET_REQUESTS = "http://localhost:8090/request/get-requests-by-type?type=";
const POST_USER_REQUEST = "http://localhost:8090/request/make-new-request";
const PUT_USER_REQUEST = "http://localhost:8090/request/update-request";
const DELETE_USER_REQUEST = "http://localhost:8090/request/cancel-request/";
const GET_ALL = 'http://localhost:8090/user/get-all'
const DELETE_USER= 'http://localhost:8090/user/delete'
const UPDATE_USER='http://localhost:8090/user/update'
const GET_ALL_WITH_NO_TEAM= 'http://localhost:8090/user/get-all-no-team';
const GET_USER_NOTIFICATIONS = 'http://localhost:8090/notification/get-notifications';
const READ_NOTIFICATIONS = 'http://localhost:8090/notification/read-notifications';
const RESET_PASSWORD = 'http://localhost:8090/user/change-password';
const DOWNLOAD_REPORT = 'http://localhost:8090/user/generate-report';
const GET_TEAM_ID = 'http://localhost:8090/user/get-team-id';
const SEND_TO_HR = 'http://localhost:8090/user/complete-document/';
//const UPLOAD_TO_CLOUD = 'https://gentle-mountain-44660.herokuapp.com/https://9ausqv1gij.execute-api.us-east-1.amazonaws.com/internship-test/pdf'


@Injectable({
  providedIn: 'root'
})
export class UserService {

  constructor(private httpClient: HttpClient) {
  }

  public loginUser(userCredentials: UserCredentials): Observable<any> {
    const headers = new HttpHeaders().set('Content-Type', 'text/plain; charset=utf-8');

    localStorage.setItem("email", userCredentials.email)

    return this.httpClient.post<any>(LOGIN, userCredentials);
  }


  public getVacationDays(): Observable<JSON>{

    return this.httpClient.get<JSON>(GET_VACATION_DAYS);
  }


  public getTeamId(): Observable<number>
  {
    return this.httpClient.get<number>(GET_TEAM_ID);
  }
  public getHolidayRequestsByType(type: string): Observable<Request[]> {

    return this.httpClient.get<Request[]>(GET_REQUESTS + type);
  }

  public createUser(user: UserAdmin): Observable<UserAdmin>
  {
    console.log(user);
    return this.httpClient.post<UserAdmin>(ADD_USER,user);
  }


  public updateUser(user: UserAdmin): Observable<UserAdmin>
  {

    return this.httpClient.put<UserAdmin>(UPDATE_USER,user);
  }


  public makeUserRequest(requestFormObject: UserRequestFormObject): Observable<Request> {
    return this.httpClient.post<Request>(POST_USER_REQUEST, requestFormObject);
  }

  public updateUserRequest(requestFormObject: UserRequestFormObject): Observable<Request> {

    return this.httpClient.put<Request>(PUT_USER_REQUEST, requestFormObject);
  }

  public cancelUserRequest(holidayId: number): Observable<string> {

    return this.httpClient.delete<string>(DELETE_USER_REQUEST + holidayId);
  }

  public getAllUsers(): Observable<UserAdmin[]> {
    return this.httpClient.get<UserAdmin[]>(GET_ALL);
  }

  public deleteUsers(id: Number): Observable<UserAdmin> {

    //console.log(id);
    let endpoints="/"+id;
    //console.log(DELETE_USER+endpoints);
    return this.httpClient.delete<UserAdmin>(DELETE_USER+endpoints);
  }

  public getAllUsersWithNoTeam() {

    return this.httpClient.get<User[]>(GET_ALL_WITH_NO_TEAM);
  }

  public getUserNotifications(): Observable<Notification[]> {

    return this.httpClient.get<Notification[]>(GET_USER_NOTIFICATIONS);
  }

  public readNotifications(): Observable<string> {

    console.log(READ_NOTIFICATIONS);

    return this.httpClient.put<string>(READ_NOTIFICATIONS, null);
  }

  public changePassword(resetPasswordBody: UserChangePassword): Observable<any>{
    return this.httpClient.patch<UserChangePassword>(RESET_PASSWORD,resetPasswordBody)
  }

  public downloadReport(): Observable<any> {
    return this.httpClient.get(DOWNLOAD_REPORT)
  }

  public sendToHr(holidayId: number): Observable<any> {
    return this.httpClient.get(SEND_TO_HR+holidayId);
  }

  // public uploadToCloud(document:any): Observable<any> {
  //   console.log("Uploading to cloud...")
  //   console.log(document)
  //   return this.httpClient.post(UPLOAD_TO_CLOUD,document)
  // }
}
