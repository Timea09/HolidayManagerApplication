export function dateObjectToISO8601(dateObject: Date) : string {

  return dateObject.getFullYear()
    + "-"
    + (dateObject.getMonth() + 1).toString().padStart(2, "0")
    + "-"
    + dateObject.getDate().toString().padStart(2, "0");
}

export const ISO8601_FORMAT = {
  parse: {
    dateInput: 'YYYY-MM-DD', // this is how your date will be parsed from Input
  },
  display: {
    dateInput: 'YYYY-MM-DD', // this is how your date will get displayed on the Input
    monthYearLabel: 'MMMM YYYY',
    dateA11yLabel: 'LL',
    monthYearA11yLabel: 'MMMM YYYY'
  }
};
