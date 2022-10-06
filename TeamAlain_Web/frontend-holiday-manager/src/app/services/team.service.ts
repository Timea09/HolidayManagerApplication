import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Team } from '../shared/data-type/team';
import { Observable } from 'rxjs';
import { TeamRequest} from '../shared/data-type/teamRequest';

const GET_ALL = 'http://localhost:8090/team/get-teams'
const ADD_TEAM = 'http://localhost:8090/team/add'
const DELETE_TEAM ='http://localhost:8090/team/delete/'
const UPDATE_TEAM ='http://localhost:8090/team/update'

@Injectable({
  providedIn: 'root'
})
export class TeamService {

  constructor(private httpClient:HttpClient) { }

  public getAllTeams(): Observable<Team[]>
  {
    return this.httpClient.get<Team[]>(GET_ALL);
  }

  public createTeam(team: TeamRequest): Observable<Team>
  {
    console.log(team);
    return this.httpClient.post<Team>(ADD_TEAM,team);
  }

  public deleteTeam(id : number): Observable<any> {
        console.log("deleted that bitchh");
        return this.httpClient.delete(DELETE_TEAM + id)
}

  public updateTeam(team: TeamRequest): Observable<Team>
  {
    console.log(team);
    return this.httpClient.put<Team>(UPDATE_TEAM,team);
  }


}
