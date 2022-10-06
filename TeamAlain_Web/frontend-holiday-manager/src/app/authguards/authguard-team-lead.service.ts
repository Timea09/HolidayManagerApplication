import { Injectable } from '@angular/core';
import {ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot, UrlTree} from "@angular/router";
import {Observable} from "rxjs";
import {parseJwt} from "../utils/JWTParser";
import {CookieService} from "ngx-cookie-service";
import {Location} from "@angular/common";

@Injectable({
  providedIn: 'root'
})
export class AuthguardTeamLeadService implements CanActivate{

  constructor(private cookieService:CookieService, private router: Router) { }

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
    const token = this.cookieService.get("Token");
    const parsedToken = parseJwt(token);
    if(parsedToken["roles"].includes("TEAM_LEAD")){
      return true;
    }
    else{
      this.router.navigate(["../team-lead"])
      return false;
    }
  }
}
