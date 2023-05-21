package eu.chessout.shared.model;

import eu.chessdata.chesspairing.model.ChesspairingResult;

/**
 * Created by Bogdan Oloeriu on 6/19/2016.
 */
public class Game {
    /**
     * it allays starts from 1
     */
    private int tableNumber;
    /**
     * represents the table number that it is printed on the desc.
     * It usually is computed from tableNumber + a constant
     */
    private int actualNumber;
    private Player whitePlayer;
    private Player blackPlayer;
    /**
     * 0 still not decided
     * 1 white player wins
     * 2 black player wins
     * 3 draw game
     * 4 bye
     * 5 white wins by forfeit
     * 6 black wins by forfeit
     * 7 double forfeit
     */
    private int result = 0;
    private ChesspairingResult theResult = ChesspairingResult.NOT_DECIDED;

    public void copyValues(Game game) {
        this.tableNumber = game.getTableNumber();
        this.actualNumber = game.getActualNumber();
        this.whitePlayer = game.getWhitePlayer();
        if (game.getBlackPlayer() != null) {
            this.blackPlayer = game.getBlackPlayer();
        }
        this.result = game.getResult();
    }

    public Game() {
    }

    public int getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(int tableNumber) {
        this.tableNumber = tableNumber;
    }

    public int getActualNumber() {
        return actualNumber;
    }

    public void setActualNumber(int actualNumber) {
        this.actualNumber = actualNumber;
    }

    public Player getWhitePlayer() {
        return whitePlayer;
    }

    public void setWhitePlayer(Player whitePlayer) {
        this.whitePlayer = whitePlayer;
    }

    public Player getBlackPlayer() {
        return blackPlayer;
    }

    public void setBlackPlayer(Player blackPlayer) {
        this.blackPlayer = blackPlayer;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public ChesspairingResult getTheResult() {
        return theResult;
    }

    public void setTheResult(ChesspairingResult theResult) {
        this.theResult = theResult;
    }
}
