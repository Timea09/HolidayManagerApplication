import { Component, OnInit } from '@angular/core';
import { Output,Input } from '@angular/core';
import { EventEmitter } from '@angular/core';
import { UserAdmin } from '../../../../shared/data-type/userAdmin';
import { Validators } from '@angular/forms';
import { FormBuilder } from '@angular/forms';
import { FormControl } from '@angular/forms';
import { UserManagementComponent } from '../user-management.component';
import { UserId } from 'src/app/shared/data-type/userId';
import { SimpleChanges } from '@angular/core';
import { threadId } from 'worker_threads';
import { User } from 'src/app/shared/data-type/User';
import { MatSnackBar } from '@angular/material/snack-bar';
import { TypeofExpr } from '@angular/compiler';
import { userInfo } from 'os';
import { NgForm } from '@angular/forms';
@Component({
  selector: 'app-add-user',
  templateUrl: './add-user.component.html',
  styleUrls: ['./add-user.component.scss']
})


export class AddUserComponent implements OnInit {

  hide=true;
  roles=["Tester","Developer"];
  types=["Employee","Team_lead"];

  @Output() clickCreate= new EventEmitter<UserAdmin>();
  @Output() clickDelete= new EventEmitter<number>();
  @Output() clickCancel= new EventEmitter<null>();
  @Input() userInfo: UserAdmin;
  @Input() userSelected:Boolean= false;

  initialValues: any;
  userFormGroup = this.formBuilder.group(
    {
      id: [0,Validators.required],

      firstName: ['',Validators.required],
      lastName: ['',Validators.required],
      email: ['',Validators.required],
      password: ['',Validators.required],
      department: ['',Validators.required],
      role: [''],
      daysLeft: [26, Validators.required]

    }
  )




  constructor(private formBuilder: FormBuilder, private userManagement: UserManagementComponent, private snackBar: MatSnackBar) { }


  ngOnChanges(changes:SimpleChanges) {

    if (changes['userInfo'] && this.userInfo != undefined){
      this.userSelected=true;
      this.userFormGroup.reset();

      this.userFormGroup.patchValue(
        {
          // @ts-ignore
          firstName: this.userInfo.firstName,
          lastName: this.userInfo.lastName,
          email: this.userInfo.email,
          password: "",
          department: this.userInfo.department,
          role: this.userInfo.role![0] + this.userInfo.role!.substring(1).toLowerCase(),
          daysLeft: this.userInfo.daysLeft

        }
      );
      this.userFormGroup.controls["password"].setErrors(null)

    }
  }

  clearFormErrors()
  {
    this.userFormGroup.reset();
    Object.keys(this.userFormGroup.controls).forEach(key =>{
      // @ts-ignore
      this.userFormGroup.controls[key].setErrors(null)
    });
  }
  onCancel()
  {
    this.userFormGroup.reset();
    this.clearFormErrors()
    this.userSelected=false;
    console.log("reset")
    this.clickCancel.emit();
    //this.userFormGroup.setValue(this.initialValues);

  }

  onUpdate()
  {

    const valuesFromForm = this.userFormGroup.value;
    let errors= this.validateForm();

    const updatedUser: UserAdmin=
    {
      firstName: valuesFromForm.firstName!,
      lastName: valuesFromForm.lastName!,
      email: valuesFromForm.email!,
      department: valuesFromForm.department!,
      role: valuesFromForm.role!.toUpperCase(),
      daysLeft:valuesFromForm.daysLeft!,
      teamId: this.userInfo.teamId,
      id: this.userInfo.id,
      password: valuesFromForm.password!,
      userType: this.userInfo.userType
    }
    //this.userFormGroup.get('password')?.setErrors(null);
    if(valuesFromForm.password=="")
    {

      updatedUser.password=this.userInfo.password;
    }
    else updatedUser.password!=valuesFromForm.password;
    this.userFormGroup.controls["password"].setErrors(null)
    //console.log(valuesFromForm.password)

    this.userManagement.updateUser(updatedUser);
    this.clearFormErrors();
    this.userSelected = false;

    //this.userFormGroup.reset();

  }

  validateForm()
  {
    const valuesFromForm = this.userFormGroup.value;
    let errors : string = "";

    if ( (valuesFromForm.firstName=="" || valuesFromForm.firstName==null) ||
          (valuesFromForm.lastName=="" || valuesFromForm.lastName==null) ||
          (valuesFromForm.email=="" || valuesFromForm.email==null) ||
          (valuesFromForm.password=="" || valuesFromForm.password==null) ||
          (valuesFromForm.department=="" || valuesFromForm.department==null) ||
          // (valuesFromForm.role=="" || valuesFromForm.role==null) ||
          (valuesFromForm.daysLeft==0 || valuesFromForm.daysLeft==null)
    ) {
      errors += "All fields need to be completed\n";
    }
    return errors;

  }
  createUser(): void
  {

    const valuesFromForm= this.userFormGroup.value;
    let errors= this.validateForm();


        const newUser: UserAdmin=
      {
        firstName: valuesFromForm.firstName!,
        lastName: valuesFromForm.lastName!,
        email: valuesFromForm.email!,
        password: valuesFromForm.password!,
        department: valuesFromForm.department!,
        role: valuesFromForm.role?.toUpperCase(),
        daysLeft:valuesFromForm.daysLeft!

        // userType: valuesFromForm.type?.toUpperCase
      };
      console.log(newUser)

      if(errors=="")
      {

        console.log("ok");
        this.userManagement.createUser(newUser!);
        this.userFormGroup.reset();
        this.clearFormErrors();
      }

    else
    {

      console.log("not");
      this.snackBar.open(errors,"Got it!");
    }


  }

  ngOnInit(): void {
   this.initialValues=this.userFormGroup.value;

  }



}
