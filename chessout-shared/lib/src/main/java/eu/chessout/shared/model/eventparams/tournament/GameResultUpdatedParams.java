package eu.chessout.shared.model.eventparams.tournament;

public class GameResultUpdatedParams {

    public String authToken;

    public String clubId;
    public String tournamentId;
    public String roundId;
    public String tableId;

    public String whitePlayerId;
    public String whitePlayerName;

    public String blackPlayerId;
    public String blackPlayerName;

    public String chesspairingResultString;

}
