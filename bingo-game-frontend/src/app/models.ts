export class PlayerResponse {
  name: string;
  email: string;
  id = 'id';

  constructor(theName: string, theEmail: string) {
    this.name = theName;
    this.email = theEmail;
  }
}

export class GameSetupAttributesResponse {
  gameId: string;
  boardType: string;
  slips: number;
  emailSlips: boolean;
  gameName: string;
  joinGameViaLink: boolean;

  constructor(gameId: string, boardType: string, slips: number, emailSlips: boolean, gameName: string, joinGameViaLink: boolean) {
    this.gameId = gameId;
    this.boardType = boardType;
    this.slips = slips;
    this.emailSlips = emailSlips;
    this.gameName = gameName;
    this.joinGameViaLink = joinGameViaLink;
  }
}
