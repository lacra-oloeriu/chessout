package eu.chessout.v2.ui.tournament.tournaments.dashboard

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import eu.chessout.shared.model.Tournament
import eu.chessout.v2.util.MyFirebaseUtils

data class RoundNavData(val totalRounds: Int, val navigateToRound: Int)

class TournamentDashboardViewModel : ViewModel() {
    lateinit var tournamentId: String
    lateinit var clubId: String
    val myFirebaseUtils = MyFirebaseUtils()
    val roundNavData = MutableLiveData<RoundNavData>(RoundNavData(1, 1))

    fun initializeModel(tournamentId: String, clubId: String) {
        this.tournamentId = tournamentId
        this.clubId = clubId

        Log.d("chessout.v2", "DEBUG"+this.clubId+"/"+this.tournamentId)
        myFirebaseUtils.registerTournamentListener(true, clubId, tournamentId,
            object : MyFirebaseUtils.TournamentListener {
                override fun onTournamentValue(tournament: Tournament) {
                    myFirebaseUtils.registerCompletedRoundsListener(
                        true, clubId, tournamentId, tournament.totalRounds.toLong(),
                        object : MyFirebaseUtils.LongListener {
                            override fun valueUpdated(value: Long) {
                                val totalRounds = tournament.totalRounds
                                val navigateToRound = value.toInt()
                                roundNavData.value = RoundNavData(totalRounds, navigateToRound)
                            }
                        }
                    )
                }
            })
    }
}
