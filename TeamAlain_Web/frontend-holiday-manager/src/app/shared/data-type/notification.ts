export class Notification {

  public notificationId: number;
  public holidayId: number;
  public message: string;

  public equals(n: Notification): boolean {

    return this.notificationId === n.notificationId;
  }
}
