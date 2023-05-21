package eu.chessout.v2.ui.tournament.tournaments.rounds.games

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import eu.chessout.shared.Locations
import eu.chessout.shared.model.Game
import eu.chessout.shared.model.Player
import eu.chessout.v2.util.MyFirebaseUtils
import eu.chessout.v2.util.listeners.FirebaseQueryLiveData
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

enum class Filter {
    ALL_GAMES, COMPLETED_GAMES, NOT_DECIDED_GAMES
}

class RoundGamesViewModel : ViewModel() {
    lateinit var clubId: String
    lateinit var tournamentId: String

    lateinit var liveGames: FirebaseQueryLiveData<List<Game>>
    val allGames = MutableLiveData<List<Game>>()
    val filteredList = MutableLiveData<List<Game>>()
    val livePlayersMap = HashMap<String, FirebaseQueryLiveData<Player>>()
    var filter = MutableLiveData<Filter>(Filter.ALL_GAMES)

    val showFilter = MutableLiveData<Boolean>(false)

    var roundId: Int = -1
    var myFirebaseUtils = MyFirebaseUtils()

    var isAdmin = MutableLiveData<Boolean>(false)

    fun initialize(clubId: String, tournamentId: String, roundId: Int) {
        this.clubId = clubId
        this.tournamentId = tournamentId
        this.roundId = roundId

        liveGames = getLiveGames(clubId, tournamentId, roundId)

        GlobalScope.launch {
            isAdmin.postValue(myFirebaseUtils.isCurrentUserAdmin(clubId))
        }
    }

    private fun getLiveGames(clubId: String, tournamentId: String, roundId: Int):
            FirebaseQueryLiveData<List<Game>> {
        val converter = object : FirebaseQueryLiveData.MyConverter<List<Game>> {
            override fun getValue(dataSnapshot: DataSnapshot): List<Game> {
                val list: MutableList<Game> = mutableListOf()
                for (item in dataSnapshot.children) {
                    val game = item.getValue(Game::class.java)
                    game?.let {
                        if (null != game.whitePlayer) {

                            list.add(game)
                            val whitePlayerKey = game.whitePlayer.playerKey
                            var blackPlayerKey: String? = null
                            // game result stored in database as integers. value 4 is buy
                            if (game.result != 4 && game.blackPlayer != null) {
                                blackPlayerKey = game.blackPlayer.playerKey
                            }

                            if (!livePlayersMap.containsKey(whitePlayerKey)) {
                                livePlayersMap[whitePlayerKey] = getLivePlayer(whitePlayerKey)
                            }
                            blackPlayerKey?.let {
                                if (!livePlayersMap.containsKey(blackPlayerKey)) {
                                    livePlayersMap[blackPlayerKey] = getLivePlayer(blackPlayerKey)
                                }
                            }
                        }
                    }
                }
                allGames.value = list
                updateFilteredGames(filter.value!!, list)
                return list
            }
        }
        val loc = Locations.gamesFolder(clubId, tournamentId, roundId)
        val db = FirebaseDatabase.getInstance().getReference(loc)
        return FirebaseQueryLiveData(db, converter)
    }

    fun updateFilterValue(newFilter: Filter) {
        filter.postValue(newFilter)
        updateFilteredGames(newFilter, allGames.value!!)
    }

    private fun updateFilteredGames(filter: Filter, list: List<Game>) {
        val newList: List<Game> =
            when (filter) {
                Filter.ALL_GAMES -> list
                Filter.NOT_DECIDED_GAMES -> list.filter {
                    it.result == 0
                }.toList()
                Filter.COMPLETED_GAMES -> list.filter {
                    it.result != 0
                }.toList()
            }
        filteredList.postValue(newList)
    }

    fun formatCompletedText(gameList: List<Game>): String {
        val totalGames = gameList.size
        val completedGames = getCompletedGamesCount(gameList)
        return "Round progress: $completedGames / $totalGames"
    }

    private fun getCompletedGamesCount(gameList: List<Game>): Int {
        val completedGamesList = gameList.filter {
            it.result != 0
        }.toList()
        return completedGamesList.size
    }

    private fun getLivePlayer(playerId: String?): FirebaseQueryLiveData<Player> {
        val converter = object : FirebaseQueryLiveData.MyConverter<Player> {
            override fun getValue(dataSnapshot: DataSnapshot): Player {
                val player = dataSnapshot.getValue(Player::class.java)
                return player!!
            }
        }
        val loc = Locations.clubPlayer(clubId, playerId)
        val db = FirebaseDatabase.getInstance().getReference(loc)
        return FirebaseQueryLiveData(db, converter)
    }


}
