import { UserAdmin } from "./userAdmin";
import {User} from "./User";

export class Team{
    id?: number;
    teamName?: string;
    teamMembers?: User[];
    teamLead?: User;
    clicked : boolean = false;
  }
