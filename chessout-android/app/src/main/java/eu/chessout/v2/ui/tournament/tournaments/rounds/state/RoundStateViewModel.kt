package eu.chessout.v2.ui.tournament.tournaments.rounds.state

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import eu.chessout.v2.util.MyFirebaseUtils

class RoundStateViewModel : ViewModel() {
    lateinit var tournamentId: String
    lateinit var clubId: String
    var roundId: Int = -1
    var hasGames = MutableLiveData(false)
    var myFirebaseUtils = MyFirebaseUtils()

    fun initialize(clubId: String, tournamentId: String, roundId: Int) {
        this.clubId = clubId
        this.tournamentId = tournamentId
        this.roundId = roundId

        initHasGames()
    }

    private fun initHasGames() {
        class MyListener : MyFirebaseUtils.BoolListener {
            override fun boolValueChanged(newValue: Boolean) {
                hasGames.value = newValue
            }
        }
        myFirebaseUtils.observeRoundHasGames(
            false, clubId, tournamentId, roundId, MyListener()
        )
    }
}