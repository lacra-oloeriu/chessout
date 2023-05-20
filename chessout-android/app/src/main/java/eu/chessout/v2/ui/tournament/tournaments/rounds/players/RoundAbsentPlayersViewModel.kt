package eu.chessout.v2.ui.tournament.tournaments.rounds.players

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import eu.chessout.shared.model.Player
import eu.chessout.shared.model.Post
import eu.chessout.v2.util.MyBackendUtil
import eu.chessout.v2.util.MyFirebaseUtils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class RoundAbsentPlayersViewModel : ViewModel() {
    lateinit var clubId: String
    lateinit var tournamentId: String
    var roundId: Int = -1
    val liveMissingPlayers = MutableLiveData<List<Player>>()
    var tournamentPlayers: List<Player> = ArrayList<Player>()
    var isAdmin = MutableLiveData<Boolean>(false)
    var myFirebaseUtils = MyFirebaseUtils()

    fun initialize(clubId: String, tournamentId: String, roundId: Int) {
        this.clubId = clubId
        this.tournamentId = tournamentId
        this.roundId = roundId

        GlobalScope.launch {
            isAdmin.postValue(myFirebaseUtils.awaitIsCurrentUserAdmin(clubId))
            tournamentPlayers = myFirebaseUtils.awaitGetTournamentPlayers(clubId, tournamentId)
        }

        initMissingPlayers()
    }

    private fun initMissingPlayers() {
        class PlayersListener : MyFirebaseUtils.PlayersListener {
            override fun listUpdated(players: List<Player>) {
                liveMissingPlayers.value = players
            }
        }
        myFirebaseUtils.getMissingPlayers(
            clubId, tournamentId, roundId, false, PlayersListener()
        )
    }


    fun getPresentPlayers(): List<Player> {
        val map = LinkedHashMap<String, Player>()
        val missingSet = HashSet<String>()
        liveMissingPlayers.value?.map {
            missingSet.add(it.playerKey)
        }
        val presentPlayers = ArrayList<Player>()
        tournamentPlayers.forEach {
            if (!missingSet.contains(it.playerKey)) {
                presentPlayers.add(it)
            }
        }
        return presentPlayers
    }

    fun generateGames() {
        if (roundId <= 0) {
            throw IllegalStateException("Round not allowed to be smaller then 1")
        }
        isAdmin.value = false
        GlobalScope.async {

            myFirebaseUtils.generateGamesForRound(clubId, tournamentId, roundId)


            val post = MyFirebaseUtils.postTournamentPairingsAvailablePost(
                Post.PostType.TOURNAMENT_PAIRINGS_AVAILABLE,
                clubId,
                tournamentId,
                roundId
            )
            MyBackendUtil.notifyTheBackendClubPostCreated(clubId, post.postId)
        }
    }
}