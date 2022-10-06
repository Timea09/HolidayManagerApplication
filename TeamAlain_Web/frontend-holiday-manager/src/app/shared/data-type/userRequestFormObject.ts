export class UserRequestFormObject {

  public holidayId: number | null;
  public type: string = "";
  public startDate: string = "";
  public endDate: string = "";
  public substitute?: string;
  public document?: any;
  public documentName : string = "";
}
