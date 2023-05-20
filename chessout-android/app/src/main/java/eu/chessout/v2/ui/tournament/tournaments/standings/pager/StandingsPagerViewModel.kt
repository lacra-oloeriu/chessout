package eu.chessout.v2.ui.tournament.tournaments.standings.pager

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.FirebaseDatabase
import eu.chessdata.chesspairing.importexport.Swar
import eu.chessdata.chesspairing.model.ChesspairingPlayer
import eu.chessdata.chesspairing.model.ChesspairingStanding
import eu.chessdata.chesspairing.model.ChesspairingTournament
import eu.chessout.shared.Constants
import eu.chessout.shared.model.Player
import eu.chessout.shared.model.RankedPlayer
import eu.chessout.v2.util.MyFirebaseUtils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class StandingsPagerViewModel : ViewModel() {


    class BoolListener(private val index: Int, private val boolMap: HashMap<Int, Boolean>) :
        MyFirebaseUtils.BoolListener {
        override fun boolValueChanged(newValue: Boolean) {
            boolMap[index] = newValue
        }
    }

    var isAdmin = MutableLiveData(false)
    val clubPlayers = MutableLiveData<List<Player>>()
    val position = MutableLiveData<Int>()
    val visibleRoundsCount = MutableLiveData(1)
    val myFirebaseUtils = MyFirebaseUtils()
    lateinit var tournamentId: String
    lateinit var clubId: String
    var totalRounds = -1
    private val hasResultsMap = HashMap<Int, Boolean>()

    fun setPosition(position: Int) {
        this.position.value = position
    }

    fun roundHasResults(index: Int): Boolean {
        return hasResultsMap[index]!!
    }

    fun initModel(clubId: String, tournamentId: String, totalRounds: Int) {
        this.clubId = clubId
        this.tournamentId = tournamentId
        this.totalRounds = totalRounds

        GlobalScope.launch {
            isAdmin.postValue(myFirebaseUtils.awaitIsCurrentUserAdmin(clubId))
        }

        for (i in 1..totalRounds) {
            hasResultsMap[i] = false
            initiateRoundHasStandingsListener(i, hasResultsMap)
        }

        class MyListener : MyFirebaseUtils.LongListener {
            override fun valueUpdated(value: Long) {
                visibleRoundsCount.value = value.toInt()
            }
        }

        myFirebaseUtils.registerRoundsWithStandingsListener(
            false, this.clubId, this.tournamentId, totalRounds.toLong(), MyListener()
        )
        initClubPlayersList()
    }

    private fun initClubPlayersList() {
        class PlayersListener : MyFirebaseUtils.PlayersListener {
            override fun listUpdated(players: List<Player>) {
                clubPlayers.value = players
            }
        }
        myFirebaseUtils.registerClubPlayersListener(clubId, true, PlayersListener())
    }

    private fun initiateRoundHasStandingsListener(roundId: Int, boolList: HashMap<Int, Boolean>) {
        val listener = BoolListener(roundId, boolList)
        myFirebaseUtils.registerRoundHasStandingsListener(
            false,
            clubId,
            tournamentId,
            roundId,
            listener
        )
    }

    fun importStandingsFromChesspairingTournament(
        importTournament: ChesspairingTournament?,
        swar: Swar?
    ) {
        GlobalScope.launch {
            importSwarStandings(importTournament, swar)
        }
    }

    private fun importSwarStandings(importTournament: ChesspairingTournament?, swar: Swar?) {

        val roundId = position.value!! + 1

        // delete current standings
        val suffix = Constants.STANDING_NUMBER
        val locStandings = Constants.LOCATION_STANDINGS
            .replace(Constants.CLUB_KEY, clubId)
            .replace(Constants.TOURNAMENT_KEY, tournamentId)
            .replace(Constants.ROUND_NUMBER, roundId.toString())
            .replace(Constants.CATEGORY_NAME, Constants.CATEGORY_DEFAULT)
            .replace(suffix, "")
        FirebaseDatabase.getInstance().getReference(locStandings).removeValue()

        // build ranked list
        val rankedPlayers: MutableList<RankedPlayer> = mutableListOf()
        importTournament?.players?.forEach { importPlayer ->
            val clubPlayer = getClubPlayer(importPlayer.playerKey)
            val standing = getStandings(importTournament, importPlayer.playerKey)
            clubPlayer?.run {

                val player = RankedPlayer()
                player.playerKey = clubPlayer.playerKey
                player.tournamentKey = tournamentId
                player.clubKey = clubId
                player.rankNumber = importPlayer.rank
                player.tournamentInitialOrder = importPlayer.initialOrderId
                player.buchholzPoints = standing.bucholtz
                player.points = standing.points
                player.elo = clubPlayer.elo
                player.playerName = clubPlayer.name
                player.profilePictureUri = clubPlayer.profilePictureUri

                rankedPlayers.add(player)
            }
        }

        // persist list
        rankedPlayers.forEach {
            val location = getStandingsRef(clubId, tournamentId, roundId, it.rankNumber)
            val standingRef = FirebaseDatabase.getInstance().getReference(location)
            standingRef.setValue(it)
        }
    }

    private fun getChessoutPlayerByThirdPartyKey(thirdPartyKey: String): ChesspairingPlayer? {
        val players = clubPlayers.value;
        val player = players?.first { it.thirdPartyKey == thirdPartyKey }
        if (null != player) {
            return MyFirebaseUtils().scanPlayer(player)
        } else {
            return null
        }
    }

    private fun getStandings(
        tournament: ChesspairingTournament,
        playerKey: String
    ): ChesspairingStanding {
        return tournament.standings.first { it.player.playerKey == playerKey }
    }

    private fun getClubPlayer(thirdPartyKey: String): Player? {
        val players = clubPlayers.value;
        return players?.first { it.thirdPartyKey == thirdPartyKey }
    }

    private fun getStandingsRef(
        clubId: String,
        tournamentId: String,
        roundId: Int,
        standingId: Int
    ): String {
        return Constants.LOCATION_STANDINGS
            .replace(Constants.CLUB_KEY, clubId)
            .replace(Constants.TOURNAMENT_KEY, tournamentId)
            .replace(Constants.ROUND_NUMBER, roundId.toString())
            .replace(Constants.CATEGORY_NAME, Constants.CATEGORY_DEFAULT)
            .replace(Constants.STANDING_NUMBER, standingId.toString())
    }

}
