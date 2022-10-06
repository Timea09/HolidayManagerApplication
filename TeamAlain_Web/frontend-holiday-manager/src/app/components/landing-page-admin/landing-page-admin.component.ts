import { Component, OnInit } from '@angular/core';
import {Router} from "@angular/router";
import {UserService} from "../../services/user.service";

@Component({
  selector: 'app-landing-page-admin',
  templateUrl: './landing-page-admin.component.html',
  styleUrls: ['./landing-page-admin.component.scss']
})
export class LandingPageAdminComponent implements OnInit {

  constructor(private router:Router) { }

  ngOnInit(): void {
  }


  navigateToUserManagement() {
    this.router.navigate(['user-management']);

  }
}
