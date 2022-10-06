import {Component, EventEmitter, OnDestroy, OnInit, Output} from '@angular/core';
import {Router} from "@angular/router";
import {ConfirmationDialogComponent} from "../dialog-boxes/confirmation-dialog/confirmation-dialog.component";
import {MatDialog} from "@angular/material/dialog";
import { UserService } from 'src/app/services/user.service';
import {StompService} from "../../services/stomp.service";
import {Notification} from "../../shared/data-type/notification";

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.scss']
})
export class NavbarComponent implements OnInit, OnDestroy {

  userEmail: string
  notifications: Notification[] = [];
  hasBeenPressed: boolean = false;

  @Output() onNewNotification: EventEmitter<null> = new EventEmitter<null>();

  constructor(private dialog: MatDialog, private router:Router, private stompService: StompService, public userService: UserService) {}

  ngOnInit(): void {

    this.userEmail = localStorage.getItem("email")!;

    if (this.router.url !== '/admin' && this.router.url !== '/team-management' &&
      this.router.url !== '/user-management' ) {

      this.userService.getUserNotifications().subscribe(notifications => {

        if (notifications !== null) {
          this.notifications = notifications;
        }

      });

      this.stompService.subscribe("/topic/notification", () => {

        this.hasBeenPressed=false;
        this.userService.getUserNotifications().subscribe(notifications => {

          notifications.forEach(new_n => {

            if (this.notifications.find(n => n.notificationId===new_n.notificationId) === undefined) {
              this.notifications.push(new_n)

            }
          });
          //

          console.log(this.notifications);
          // this.notifications = notifications;
          this.onNewNotification.emit(null);
        });
      });
    }
  }

  goBack(){
    if(this.router.url==="/team-management"){
      this.router.navigate(["admin"])
    }
    else if(this.router.url=="/user-management"){
      this.router.navigate(["admin"])
    }
    else if(this.router.url=="/change-password" && localStorage.getItem("role")=="EMPLOYEE"){
      this.router.navigate(["employee"])
    }
    else if(this.router.url=="/change-password" && localStorage.getItem("role")=="TEAM_LEAD"){
      this.router.navigate(["team-lead"])
    }
  }

  signOut() {
    const dialogResponse = this.dialog.open(ConfirmationDialogComponent, {
      width: '250px', data: "Are you sure you want to sign out?",
      autoFocus: false
    });
    dialogResponse.afterClosed().subscribe(result => {
      if (result) {
        document.cookie = "Token = ;";
        this.router.navigate(["login"]);
      }
    });

  }


  goChangePassword() {
    this.router.navigate(["change-password"])
  }

  checkShowBack():boolean{
    return this.router.url!=='/employee' && this.router.url!=='/team-lead' && this.router.url!=='/admin';
  }

  checkShowChangePassword():boolean{
    return this.router.url!=='/admin' && this.router.url!=='/team-management' &&
      this.router.url!=='/user-management' && this.router.url!=='/change-password'  ;
  }

  checkShowNotifications():boolean{

    return this.router.url !== '/admin' && this.router.url !== '/team-management' &&
      this.router.url !== '/user-management' && this.router.url !== '/change-password';
  }

  toggleBadgeButton(){
    if(!this.hasBeenPressed){
      this.hasBeenPressed=true;
    }
  }

  ngOnDestroy(): void {
    if (this.router.url !== '/admin' && this.router.url !== '/team-management' &&
      this.router.url !== '/user-management' && this.router.url !== '/login') {
      this.stompService.unsubscribe();
    }
  }

  onReadNotifications() {

    this.userService.readNotifications().subscribe(
      res=>
      {

      }
    );
    this.toggleBadgeButton()

  }
}
