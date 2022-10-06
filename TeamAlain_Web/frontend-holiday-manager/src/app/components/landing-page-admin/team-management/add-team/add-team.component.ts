import {
  ChangeDetectionStrategy,
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnInit,
  Output,
  SimpleChanges, ViewChild
} from '@angular/core';
import {FormBuilder, FormControl, Validators} from '@angular/forms';
import { Team } from '../../../../shared/data-type/team';
import { TeamRequest } from '../../../../shared/data-type/teamRequest';
import {User} from "../../../../shared/data-type/User";
import {map, Observable, startWith} from "rxjs";
import {UserService} from "../../../../services/user.service";
import {ConfirmationDialogComponent} from "../../../dialog-boxes/confirmation-dialog/confirmation-dialog.component";
import {MatDialog} from "@angular/material/dialog";
import {MatSnackBar} from "@angular/material/snack-bar";


@Component({
  selector: 'app-add-team',
  templateUrl: './add-team.component.html',
  styleUrls: ['./add-team.component.scss'],
})
export class AddTeamComponent implements OnInit, OnChanges {


  @Output() clickCreate= new EventEmitter<TeamRequest>();
  @Input() teamInfo:Team|undefined;
  @Input() eventUsersWithNoTeamChanged:boolean =false;

  usersWithNoTeam: User[] = [];
  teamFormGroup = this.formBuilder.group(
    {
      teamName : '',
      teamLeadId: ['',Validators.required],
      teamMembersId: ['',Validators.required],


    }
  )

  //teamMembers: User[] = [];
  myControl = new FormControl<string | User>('');
  teamMembers: User[] = [];
  filteredUsersWithNoTeam: Observable<User[]>;
  selectedTeamLeader: User|null;
  teamSelected : boolean = false;

  t : Team = new Team();

  constructor(private formBuilder: FormBuilder,private userService:UserService, private dialog : MatDialog, private snackbar : MatSnackBar) { }

  ngOnInit(): void {
    this.userService.getAllUsersWithNoTeam().subscribe(data => {
      this.usersWithNoTeam = data;
      this.initializeFilteredList()
      this.teamInfo = new Team()
      this.teamInfo.clicked=false;
    })

  }


  ngOnChanges(changes:SimpleChanges) {
    console.log(changes)
    if(changes['eventUsersWithNoTeamChanged']){
      //usersWithNoTeam list will be updated and fetch new data from server
      this.userService.getAllUsersWithNoTeam().subscribe(data=>{
        this.usersWithNoTeam=data;
        this.initializeFilteredList();
      })
    }
    if (changes['teamInfo'] && this.teamInfo != null && this.teamInfo.clicked){

      this.teamSelected = true;
      this.teamFormGroup.patchValue(
        {
          // @ts-ignore
          teamName: this.teamInfo.teamName,

        }
      );

      this.teamMembers.splice(0);
      // @ts-ignore
      this.teamInfo.teamMembers.forEach(val => this.teamMembers.push(Object.assign({}, val)));

      let index = 0;
      for ( let i = 0; i < this.teamMembers.length; i++) {
        if ( this.teamMembers[i].id == this.teamInfo.teamLead!.id) {
          index = i;
        }
      }

      this.selectedTeamLeader = this.teamMembers[index];
    }

    else{
      this.teamSelected = false;
      this.teamFormGroup.reset();
      this.teamMembers.splice(0);
      this.selectedTeamLeader = null;
    }
  }

  initializeFilteredList(){
    this.myControl.reset();
    this.filteredUsersWithNoTeam = this.myControl.valueChanges.pipe(
      startWith(''),
      map(value => {
        const name = typeof value === 'string' ? value : value?.firstName+" "+value?.lastName;
        return name ? this._filter(name as string) : this.usersWithNoTeam.slice();
      }),
    );
  }


  addMemberToList(user: User) {
    this.usersWithNoTeam.splice(this.usersWithNoTeam.indexOf(user),1);
    this.teamMembers.push(user);
    this.initializeFilteredList()
  }

  removeFromMemberList(member:User) {
    const dialogResponse = this.dialog.open(ConfirmationDialogComponent, {
      width: '250px', data: "Are you sure you want to remove " + member.firstName+" "+member.lastName+ " from the current team?",
      autoFocus: false

    });
    dialogResponse.afterClosed().subscribe(result => {
      if (result) {
        this.teamMembers.splice(this.teamMembers.indexOf(member),1);
        this.usersWithNoTeam.push(member);
        if(member==this.selectedTeamLeader){
          this.selectedTeamLeader=null;
        }
        this.initializeFilteredList()
      }
    });

  }



  displayFn(user: User): string {
    return user && user.firstName ? user.firstName+" "+user.lastName : '';
  }

  private _filter(name: string): User[] {
    const filterValue = name.toLowerCase();
    // @ts-ignore
    return this.usersWithNoTeam.filter(option => {
      const fullName = option.firstName?.toLowerCase()+" "+option.lastName?.toLowerCase();
      const reversedFullName = option.lastName?.toLowerCase()+" "+option.firstName?.toLowerCase();
      if(fullName.startsWith(filterValue) || reversedFullName.startsWith(filterValue)){
        return true;
      }
    });
  }

  updateOrCreateTeam(): void
  {
    const valuesFromForm= this.teamFormGroup.value;
    const teamMembersId = this.teamMembers.map(member => {return {"id":member.id}});

    const errors = this.validateForm();
    if (errors == "")
    {

      const newTeam=
        {
          id : this.teamInfo!.id,
          teamName: valuesFromForm.teamName,
          teamLeadId: this.selectedTeamLeader!.id,
          teamMembersId: teamMembersId,
        };
      // @ts-ignore
      this.clickCreate.emit(newTeam);
      this.teamFormGroup.reset();
      Object.keys(this.teamFormGroup.controls).forEach(key =>{
        // @ts-ignore
        this.teamFormGroup.controls[key].setErrors(null)
      });
      this.teamMembers.splice(0);
    }

    else {
      this.snackbar.open(errors,"Dismiss", {
        panelClass: ['success-snackbar'] });

    }
  }

  validateForm( ) {
    let errors : string = "";
    if ( this.selectedTeamLeader == null) {
      errors += "A team leader must be selected!\n"
    }
    if ( this.teamMembers.length == 0) {
      errors += "Team must contain at least one member!\n"
    }

    //@ts-ignore
    if ( this.teamFormGroup.value.teamName == null || this.teamFormGroup.value.teamName.trim() == "") {
      errors += "Team must have a name!"
    }

    return errors;
  }


}
