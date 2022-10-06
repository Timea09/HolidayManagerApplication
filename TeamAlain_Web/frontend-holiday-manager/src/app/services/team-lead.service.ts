import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {RequestStatusObject} from "../shared/data-type/requestStatusObject";
import {Request} from "../shared/data-type/request";
import { TeamLeadRequestFormObject } from '../shared/data-type/teamLeadRequestFormObject';
import { PeriodObject } from '../shared/data-type/periodObject';
import { User } from '../shared/data-type/User';
import {TeamLeadRequestFormUpdateObject} from "../shared/data-type/teamLeadRequestFormUpdateObject";

const GET_TEAM_REQUESTS = "http://localhost:8090/request/get-team-requests-by-status?status=";
const GET_SUBSTITUTE_TEAM_REQUESTS = "http://localhost:8090/request/get-substitute-team-requests-by-status?status=";
const SET_TEAM_REQUEST_STATUS = "http://localhost:8090/request/set-request-status";
const GET_AVAILABLE_TEAM_LEADS="http://localhost:8090/user/get-all-available-team-leads";
const GET_AVAILABLE_TEAM_LEADS_UPDATE="http://localhost:8090/user/get-all-available-team-leads-for-update";
const POST_TEAM_LEAD_REQUEST = "http://localhost:8090/request/make-new-team-lead-request";
const UPDATE_TEAM_LEAD_REQUEST = "http://localhost:8090/request/update-team-lead-request"
const DELETE_TEAM_LEAD_REQUEST = "http://localhost:8090/request/cancel-request-for-update/";
const GET_SUBSTITUTE_ID= "http://localhost:8090/request/get-substitute-id";
@Injectable({
  providedIn: 'root'
})
export class TeamLeadService {

  constructor(private httpClient:HttpClient) {

  }

  public getTeamHolidayRequestsByStatus(status: string): Observable<Request[]>{

    return this.httpClient.get<Request[]>(GET_TEAM_REQUESTS + status);
  }

  public getSubstituteTeamHolidayRequestsByStatus(status: string): Observable<Request[]>{

    return this.httpClient.get<Request[]>(GET_SUBSTITUTE_TEAM_REQUESTS + status);
  }

  public setTeamRequestStatus(requestStatusObject: RequestStatusObject): Observable<JSON>{

    return this.httpClient.put<JSON>(SET_TEAM_REQUEST_STATUS, requestStatusObject);
  }

  public getAllAvailableTeamLeads(periodObject: PeriodObject): Observable<User[]> {

    let endpoint = GET_AVAILABLE_TEAM_LEADS+"?startDate="+periodObject.startDate+"T00:00:00&endDate="+periodObject.endDate+"T00:00:00";
    return this.httpClient.get<User[]>(endpoint)
  }

  public getAllAvailableTeamLeadsForUpdate(startDate: string, endDate: string, teamId: number, holidayId: number): Observable<User[]> {

    let endpoint = GET_AVAILABLE_TEAM_LEADS_UPDATE+"?startDate="+startDate+"T00:00:00&endDate="+endDate+"T00:00:00"+"&teamId="+teamId+"&holidayId="+holidayId;
    return this.httpClient.get<User[]>(endpoint);
  }

  public makeNewTeamLeadRequest(teamLeadRequestFormObject: TeamLeadRequestFormObject): Observable<Request>{

    return this.httpClient.post<Request>(POST_TEAM_LEAD_REQUEST, teamLeadRequestFormObject);
  }

  public updateTeamLeadRequest(teamLeadRequestFormUpdateObject: TeamLeadRequestFormUpdateObject): Observable<Request>
  {
    return this.httpClient.put<Request>(UPDATE_TEAM_LEAD_REQUEST, teamLeadRequestFormUpdateObject);
  }
  public cancelTeamLeadRequest(holidayId: number): Observable<string> {

    return this.httpClient.delete<string>(DELETE_TEAM_LEAD_REQUEST + holidayId);
  }

  public getSubstituteId(holidayId: number): Observable<JSON>
  {
    return this.httpClient.get<JSON>(GET_SUBSTITUTE_ID+"?holidayId="+holidayId);
  }
}
