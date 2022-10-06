import { Injectable } from '@angular/core';
import {HttpEvent, HttpHandler, HttpHeaders, HttpInterceptor, HttpRequest} from "@angular/common/http";
import { Observable } from 'rxjs';
import {CookieService} from "ngx-cookie-service";

@Injectable({
  providedIn: 'root'
})
export class AuthentificationInterceptor implements HttpInterceptor{

  constructor(private cookieService:CookieService) { }

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    if(!req.url.includes("/auth/")) {
     // console.log("Headers are set");
      const jwt = this.cookieService.get("Token");
      const headers: any = {"app-auth": jwt}
      req = req.clone({setHeaders: headers});
    }
    return next.handle(req);

  }
}
