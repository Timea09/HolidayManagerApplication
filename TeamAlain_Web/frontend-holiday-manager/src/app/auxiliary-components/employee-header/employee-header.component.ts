import { Component, Input, OnInit } from '@angular/core';

@Component({
  selector: 'app-employee-header',
  templateUrl: './employee-header.component.html',
  styleUrls: ['./employee-header.component.scss']
})
export class EmployeeHeaderComponent implements OnInit {

  @Input() title: string;
  @Input() daysLeft: Number;

  constructor() { }

  ngOnInit(): void {
  }

}
