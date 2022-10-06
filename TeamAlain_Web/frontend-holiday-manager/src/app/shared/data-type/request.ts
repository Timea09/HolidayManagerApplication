export class Request {

  public userId: number;
  public holidayId: number;
  public email: string = "";
  public firstName: string = "";
  public lastName: string = "";
  public startDate: string;
  public endDate: string;
  public type: string = "";
  public status: string = "";
  public substitute?: string;
  public document?: any;
  public documentName : string = "";
  public extraInfo: string = "";
}

