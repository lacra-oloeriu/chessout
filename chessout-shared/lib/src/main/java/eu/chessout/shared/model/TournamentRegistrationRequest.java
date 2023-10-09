package eu.chessout.shared.model;

import eu.chessout.shared.model.enums.TournamentRegistrationState;

public class TournamentRegistrationRequest {

    public String key;
    public Player player;
    public String multiversXTransaction;
    public TournamentRegistrationState state;
    public String userKey;
}
