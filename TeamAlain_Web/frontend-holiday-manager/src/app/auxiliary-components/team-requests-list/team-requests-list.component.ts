import {Component, EventEmitter, Input, OnInit, Output, ViewChild} from '@angular/core';
import {MatTableDataSource} from "@angular/material/table";
import {Request} from "../../shared/data-type/request";
import {MatSort, Sort} from "@angular/material/sort";
import {LiveAnnouncer} from "@angular/cdk/a11y";

@Component({
  selector: 'app-team-requests-list',
  templateUrl: './team-requests-list.component.html',
  styleUrls: ['./team-requests-list.component.scss']
})
export class TeamRequestsListComponent implements OnInit {

  private _externalRequestList: Request[] = [];
  private _teamRequestList: MatTableDataSource<Request> = new MatTableDataSource<Request>([]);

  get teamRequestList(): MatTableDataSource<Request> {

    return this._teamRequestList;
  }

  @Input()
  set teamRequestList(requestList: MatTableDataSource<Request>) {

    this._teamRequestList = requestList;
    this._externalRequestList = requestList.data;

    this.changeTypeFilter(this.selectedType);
    this._teamRequestList.sort = this.sort;
  }

  @ViewChild(MatSort) sort: MatSort;

  @Output() onTeamRequestListStatusSelectionChange = new EventEmitter<string>();

  @Output() onShowTeamRequestButtonPressed = new EventEmitter<Request|null>();

  selectedTeamRequest: Request | null = null;

  selectedType: string;
  selectedStatus: string;

  defaultTypeOption = 'ALL';
  defaultStatusOption = 'PENDING';

  columnsToDisplay: Array<string> = ['firstName', 'lastName', 'startDate', 'endDate', 'type', 'show'];
  toggleViewTeamRequest: boolean = false;

  @Input()
  tableId: string;

  constructor(private _liveAnnouncer: LiveAnnouncer) { }

  ngOnInit(): void {

    this.selectedType = this.defaultTypeOption;
    this.onTeamRequestListStatusSelectionChange.emit(this.defaultStatusOption);
  }

  requestIsCurrentlySelected(request: Request): boolean {

    return JSON.stringify(request) === JSON.stringify(this.selectedTeamRequest);
  }

  selectRow(request: Request) {

    let index = this._teamRequestList.data.indexOf(request);

    document.getElementById(this.tableId)?.children.item(1)?.children[index].classList.add("selected-request");
  }

  deselectRow() {

    let rows = document.getElementById(this.tableId)?.children.item(1)?.children;

    // @ts-ignore
    for (let i = 0; i < rows.length; i++) {

      // @ts-ignore
      rows[i].classList.remove("selected-request");
    }
  }

  announceSortChange(sortState: Sort) {

    if (sortState.direction) {

      this._liveAnnouncer.announce(`Sorted by ${sortState.active} ${sortState.direction}`);
    }
    else {

      this._liveAnnouncer.announce(`Sorting Cleared.`);
    }
  }

  changeTypeFilter(type: string) {

    this.selectedType = type;

    this.selectedTeamRequest = null;

    this.onShowTeamRequestButtonPressed.emit(null);

    this.deselectRow()

    this.toggleViewTeamRequest = false;

    if (type !== 'ALL') {

      this._teamRequestList.data = this._externalRequestList.filter(request => request.type === type);
    }
    else {

      this._teamRequestList.data = this._externalRequestList;
    }
  }

  changeStatusFilter(status: string) {

    this.selectedStatus = status;
    this.onTeamRequestListStatusSelectionChange.emit(status);
  }

  toggleShowTeamRequest(request: Request) {

    if (!this.toggleViewTeamRequest || (this.selectedTeamRequest && !this.requestIsCurrentlySelected(request)) ) {

      this.onShowTeamRequestButtonPressed.emit(request);

      this.selectedTeamRequest = request;

      this.deselectRow();

      this.selectRow(request);

      this.toggleViewTeamRequest = true;
    }

    else if (this.toggleViewTeamRequest && this.requestIsCurrentlySelected(request)) {

      this.selectedTeamRequest = null;

      this.onShowTeamRequestButtonPressed.emit(null);

      this.deselectRow()

      this.toggleViewTeamRequest = false;
    }

  }

}
