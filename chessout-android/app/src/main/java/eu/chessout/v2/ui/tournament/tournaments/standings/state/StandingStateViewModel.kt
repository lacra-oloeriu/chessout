package eu.chessout.v2.ui.tournament.tournaments.standings.state

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import eu.chessout.shared.model.RankedPlayer
import eu.chessout.v2.util.MyFirebaseUtils

class StandingStateViewModel : ViewModel() {
    lateinit var tournamentId: String
    lateinit var clubId: String
    var roundId: Int = -1
    val hasStandings = MutableLiveData(false)
    val liveRankedPlayers = MutableLiveData<List<RankedPlayer>>()
    val myFirebaseUtils = MyFirebaseUtils()

    fun initModel(clubId: String, tournamentId: String, roundId: Int) {
        this.tournamentId = tournamentId
        this.clubId = clubId
        this.roundId = roundId

        initHasStandings()
        initRankedPlayers()
    }

    private fun initHasStandings() {
        class MyListener : MyFirebaseUtils.BoolListener {
            override fun boolValueChanged(newValue: Boolean) {
                hasStandings.value = newValue
            }
        }
        myFirebaseUtils.registerRoundHasStandingsListener(
            false, false, clubId, tournamentId, roundId, MyListener()
        )
    }

    private fun initRankedPlayers() {
        class RankedPlayersListener : MyFirebaseUtils.RankedPlayerListener {
            override fun listUpdated(players: List<RankedPlayer>) {
                liveRankedPlayers.value = players
            }

        }

        myFirebaseUtils.registerRoundStandingsListener(
            false,
            clubId,
            tournamentId,
            roundId,
            RankedPlayersListener()
        )
    }
}
