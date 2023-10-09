package eu.chessout.shared.model;

import eu.chessout.shared.model.enums.TournamentRegistrationState;

public class TournamentRegistrationRequest {

    public String requestKey;
    public Player player;
    public String multiversXTransaction;
    public TournamentRegistrationState state;
    public String userKey;
}
