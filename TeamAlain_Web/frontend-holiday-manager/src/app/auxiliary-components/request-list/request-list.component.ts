import {Component, EventEmitter, Input, OnChanges, OnInit, Output, ViewChild} from '@angular/core';
import { Request } from '../../shared/data-type/request';
import {UserService} from "../../services/user.service";
import {dateObjectToISO8601} from "../../utils/DateConversion";
import {LiveAnnouncer} from "@angular/cdk/a11y";
import {MatTableDataSource} from "@angular/material/table";
import {MatSort, Sort} from "@angular/material/sort";
import { saveAs } from 'file-saver';
import * as FileSaver from "file-saver";


@Component({
  selector: 'app-request-list',
  templateUrl: './request-list.component.html',
  styleUrls: ['./request-list.component.scss']
})
export class RequestListComponent implements OnInit, OnChanges {

  private _requestList: MatTableDataSource<Request>;

  public toggleView: boolean = false;

  get requestList(): MatTableDataSource<Request> {

    return this._requestList;
  }

  @Input()
  set requestList(requestList: MatTableDataSource<Request>) {

      this._requestList = requestList;
      this._requestList.sort = this.sort;
  }

  @ViewChild(MatSort) sort: MatSort;

  private _selectedRequest: Request | null = null;

  get selectedRequest(): Request | null {

    return this._selectedRequest;
  }

  @Input()
  set selectedRequest(request: Request | null) {

    this._selectedRequest = request;
  }

  @Output() onRequestListTypeSelectionChange = new EventEmitter<string>();

  @Output() onShowButtonPressed = new EventEmitter<Request|null>();

  selectedType: string;
  defaultOption = 'ALL';

  @Input()
  columnsToDisplay: Array<string>;

  constructor(private _liveAnnouncer: LiveAnnouncer) { }

  ngOnInit(): void {

    this.onRequestListTypeSelectionChange.emit(this.defaultOption);
  }

  ngOnChanges(): void {

  }

  requestIsCurrentlySelected(request: Request): boolean {

      return JSON.stringify(request) === JSON.stringify(this._selectedRequest);
  }

  selectRow(request: Request) {

    let index = this._requestList.data.indexOf(request);

    document.getElementById("request-table")?.children.item(1)?.children[index].classList.add("selected-request");
  }

  deselectRow() {

    let rows = document.getElementById("request-table")?.children.item(1)?.children;

    // @ts-ignore
    for (let i = 0; i < rows.length; i++) {

      // @ts-ignore
      rows[i].classList.remove("selected-request");
      // rows[i].children
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

  changeType(type: string) {

    this.selectedType = type;
    this.onRequestListTypeSelectionChange.emit(type);
    this._selectedRequest=null;
  }

  toggleShowRequest(request: Request) {

    if (!this.toggleView || this.selectedRequest !== request || (this.toggleView && this.selectedRequest !== request)) {

      this.onShowButtonPressed.emit(request);

      this.deselectRow();

      this.selectRow(request);

      this.toggleView = true;
    }

    if (this.toggleView && this.selectedRequest === request) {

      this.onShowButtonPressed.emit(null);

      this.deselectRow()

      this.toggleView = false;
    }

  }

}
