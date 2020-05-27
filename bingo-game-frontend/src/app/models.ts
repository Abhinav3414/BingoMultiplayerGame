export class PlayerResponse {
  name: string;
  email: string;
  id = 'id';

  constructor(theName: string, theEmail: string) {
    this.name = theName;
    this.email = theEmail;
  }
}
