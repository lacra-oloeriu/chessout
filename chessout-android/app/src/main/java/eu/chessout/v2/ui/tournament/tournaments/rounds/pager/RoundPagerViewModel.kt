package eu.chessout.v2.ui.tournament.tournaments.rounds.pager

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.FirebaseDatabase
import eu.chessdata.chesspairing.importexport.Swar
import eu.chessdata.chesspairing.model.*
import eu.chessout.shared.Constants
import eu.chessout.shared.model.Game
import eu.chessout.shared.model.Player
import eu.chessout.shared.model.Post
import eu.chessout.v2.util.MyBackendUtil
import eu.chessout.v2.util.MyFirebaseUtils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class RoundPagerViewModel : ViewModel() {

    companion object {
        class BoolListener(val index: Int, val boolMap: HashMap<Int, Boolean>) :
            MyFirebaseUtils.BoolListener {
            override fun boolValueChanged(newValue: Boolean) {
                boolMap[index] = newValue
            }
        }

        class MyGameListener(
            val index: Int,
            val gameMap: HashMap<Int, List<Game>>
        ) : MyFirebaseUtils.GamesListener {
            override fun listUpdated(games: List<Game>) {
                gameMap[index] = games;
            }

        }

    }

    val position = MutableLiveData<Int>()
    val visibleRoundsCount = MutableLiveData(1)
    val myFirebaseUtils = MyFirebaseUtils()
    lateinit var tournamentId: String
    lateinit var clubId: String
    var totalRounds = -1
    var isAdmin = MutableLiveData(false)
    val hasGamesMap = HashMap<Int, Boolean>()
    val clubPlayers = MutableLiveData<List<Player>>()
    val roundGamesMap = HashMap<Int, List<Game>>()

    fun setPosition(position: Int) {
        this.position.value = position
    }

    fun roundHasGames(index: Int): Boolean {
        return hasGamesMap[index]!!
    }

    fun initializeModel(clubId: String, tournamentId: String, totalRounds: Int, roundId: Int) {
        this.clubId = clubId
        this.tournamentId = tournamentId
        this.totalRounds = totalRounds
        this.visibleRoundsCount.value = roundId

        for (i in 1..totalRounds) {
            hasGamesMap[i] = true
            roundGamesMap[i] = listOf();
            initiateRoundHasGamesListener(i, hasGamesMap)
            initiateRoundGamesListener(i, roundGamesMap)
        }

        class MyListener : MyFirebaseUtils.LongListener {
            override fun valueUpdated(value: Long) {
                visibleRoundsCount.value = value.toInt()
            }
        }
        myFirebaseUtils.registerCompletedRoundsListener(
            false, this.clubId, this.tournamentId, totalRounds.toLong(), MyListener()
        )

        GlobalScope.launch {
            isAdmin.postValue(myFirebaseUtils.awaitIsCurrentUserAdmin(clubId))

        }
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

    fun initiateRoundHasGamesListener(roundId: Int, boolList: HashMap<Int, Boolean>) {

        val listener = BoolListener(roundId, boolList)

        myFirebaseUtils.registerRoundHasGamesListener(
            false,
            clubId,
            tournamentId,
            roundId,
            listener
        )
    }

    fun initiateRoundGamesListener(
        roundId: Int,
        roundGamesMap: HashMap<Int, List<Game>>
    ) {
        val gamesListener = MyGameListener(roundId, roundGamesMap)
        myFirebaseUtils.registerGamesListener(false, clubId, tournamentId, roundId, gamesListener)
    }

    private fun importSwarRound(
        importTournament: ChesspairingTournament?, swar: Swar?
    ) {

        val roundId = position.value!! + 1

        // delete current round
        val gamesLoc = Constants.LOCATION_ROUND_GAMES
            .replace(Constants.CLUB_KEY, clubId)
            .replace(Constants.TOURNAMENT_KEY, tournamentId)
            .replace(Constants.ROUND_NUMBER, roundId.toString())
        FirebaseDatabase.getInstance().getReference(gamesLoc).removeValue()

        // scan imported round
        if (importTournament!!.rounds[roundId - 1] == null) {
            return
        }
        val roundImportRound = importTournament.rounds[roundId - 1]
        val round = ChesspairingRound()
        round.roundNumber = roundId
        roundImportRound.games.forEach { importGame ->
            val game = ChesspairingGame()
            game.tableNumber = importGame.tableNumber
            game.result = importGame.result
            val whitePlayer = getPlayerByThirdPartyKey(importGame.whitePlayer.playerKey)
            if (null != whitePlayer) {
                game.whitePlayer = whitePlayer
            }
            if (importGame.result != ChesspairingResult.BYE) {
                val blackPlayer = getPlayerByThirdPartyKey(importGame.blackPlayer.playerKey)
                if (null != blackPlayer) {
                    game.blackPlayer = blackPlayer
                }
            }
            round.games.add(game)
        }

        // persist the round, create the post, notify the backend
        MyFirebaseUtils().persistNewGames(clubId, tournamentId, round)
        val post = MyFirebaseUtils.postTournamentPairingsAvailablePost(
            Post.PostType.TOURNAMENT_PAIRINGS_AVAILABLE,
            clubId,
            tournamentId,
            roundId
        )
        MyBackendUtil.notifyTheBackendClubPostCreated(clubId, post.postId)
    }

    fun importRoundFromChesspairingTournament(
        importTournament: ChesspairingTournament?, swar: Swar?
    ) {
        GlobalScope.launch {
            importSwarRound(importTournament, swar)
        }
    }

    private fun getPlayerByThirdPartyKey(thirdPartyKey: String): ChesspairingPlayer? {
        val players = clubPlayers.value;
        val player = players?.first { it.thirdPartyKey == thirdPartyKey }
        if (null != player) {
            return MyFirebaseUtils().scanPlayer(player)
        } else {
            return null
        }
    }

}