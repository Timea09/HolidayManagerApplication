import { Injectable } from '@angular/core';
import {ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot, UrlTree} from "@angular/router";
import { Observable } from 'rxjs';
import {CookieService} from "ngx-cookie-service";
import {parseJwt} from "../utils/JWTParser";
import {Location} from "@angular/common";

@Injectable({
  providedIn: 'root'
})
export class AuthguardAdminService implements CanActivate{

  constructor(private cookieService:CookieService,private router:Router) { }

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
    const token = this.cookieService.get("Token");
    const parsedToken = parseJwt(token);
    if(parsedToken["roles"].includes("ADMIN")){
      return true;
    }
    else{
      this.router.navigate(["../admin"])
      return false;
    }
  }
}
