package eu.chessout.v2.ui.tournament.tournaments.players

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import eu.chessdata.chesspairing.model.ChesspairingPlayer
import eu.chessdata.chesspairing.model.ChesspairingTournament
import eu.chessout.shared.Constants
import eu.chessout.shared.Constants.LOG_TAG
import eu.chessout.shared.Locations
import eu.chessout.shared.model.Player
import eu.chessout.shared.model.RankedPlayer
import eu.chessout.v2.util.MyFirebaseUtils
import eu.chessout.v2.util.listeners.FirebaseQueryLiveData
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class TournamentPlayersViewModel : ViewModel() {
    private var clubId = MutableLiveData<String>().apply { value = "" }
    private var tournamentId = MutableLiveData<String>().apply { value = "" }
    var isAdmin = MutableLiveData(false)
    val livePlayerList = MutableLiveData<List<Player>>()

    // players listed by the fragment
    val liveRankedPlayer = MutableLiveData<List<RankedPlayer>>()

    // the map that holds live players from the club (not tournament)
    val liveClubPlayersMap = HashMap<String, FirebaseQueryLiveData<Player>>()

    // all club players
    val liveClubPlayerList = MutableLiveData<List<Player>>()

    // players not registered in tournament
    val notSubscribedPlayers = MutableLiveData<List<Player>>()

    private lateinit var myFirebaseUtils: MyFirebaseUtils

    fun initializeModel(clubId: String, tournamentId: String) {
        this.clubId.value = clubId
        this.tournamentId.value = tournamentId
        myFirebaseUtils = MyFirebaseUtils()

        initIsAdmin()
        initRankedPlayers()
        initPlayers()
        Log.d(LOG_TAG, "$LOG_TAG clubId=${clubId}, tournamentId=${tournamentId}")
    }

    private fun initIsAdmin() {
        class IsAdminListener : MyFirebaseUtils.IsAdminListener {
            override fun onIsAdmin(returnAdmin: Boolean) {
                isAdmin.value = returnAdmin
            }
        }
        myFirebaseUtils.isCurrentUserAdmin(clubId.value, IsAdminListener())
    }

    private fun initRankedPlayers() {
        class RankedPlayersListener : MyFirebaseUtils.RankedPlayerListener {
            override fun listUpdated(players: List<RankedPlayer>) {
                liveRankedPlayer.value = players
                players.forEach {
                    if (!liveClubPlayersMap.containsKey(it.playerKey)) {
                        liveClubPlayersMap[it.playerKey] = getClubPlayer(it.playerKey)
                    }
                }
            }
        }
        myFirebaseUtils.observeTournamentInitialOrder(
            false,
            clubId.value!!,
            tournamentId.value!!,
            RankedPlayersListener()
        )
    }

    private fun getClubPlayer(playerKey: String?): FirebaseQueryLiveData<Player> {
        val converter = object : FirebaseQueryLiveData.MyConverter<Player> {
            override fun getValue(dataSnapshot: DataSnapshot): Player {
                val player = dataSnapshot.getValue(Player::class.java)
                return player!!
            }
        }
        val loc = Locations.clubPlayer(clubId.value, playerKey)
        val db = FirebaseDatabase.getInstance().getReference(loc)
        return FirebaseQueryLiveData(db, converter)
    }

    fun getClubId(): String {
        return clubId!!.value!!
    }

    fun getTournamentId(): String {
        return tournamentId!!.value!!
    }

    fun getMissingPlayers(): List<Player> {
        return notSubscribedPlayers.value!!
    }

    private fun initPlayers() {
        class PlayersListener : MyFirebaseUtils.PlayersListener {
            override fun listUpdated(players: List<Player>) {
                livePlayerList.value = players
                initClubPlayersList()
            }
        }
        myFirebaseUtils.registerTournamentPlayersListener(
            clubId.value!!, tournamentId.value!!, false, PlayersListener()
        )
    }

    private fun initClubPlayersList() {
        class PlayersListener : MyFirebaseUtils.PlayersListener {
            override fun listUpdated(players: List<Player>) {
                liveClubPlayerList.value = players
                updateMissingPlayers()
            }
        }
        myFirebaseUtils.registerClubPlayersListener(clubId.value!!, true, PlayersListener())
    }

    private fun updateMissingPlayers() {
        val allClubPlayers: List<Player> = liveClubPlayerList.value!!
        notSubscribedPlayers.value = allClubPlayers.filter {
            !it.isArchived
        }.filter {
            !livePlayerList.value!!.contains(it)
        }
    }

    fun generateAndGetSWarTournament(): String {
        val sb: StringBuffer = StringBuffer()
        sb.append(
            "" +
                    "[TOURNAMENT];;;;;;;;\n" +
                    "1;Chessout export;;;;;;;\n" +
                    "[PLAYER];;;;;;;;\n"
        )
        livePlayerList.value!!.forEach {
            sb.append(it.thirdPartyKey + ";" + it.name + ";;;;0;;;;\n")
        }
        return sb.toString()
    }


    fun getClubPlayerByThirdPartiId(thirdPartyId: String): Player? {
        return liveClubPlayerList.value?.firstOrNull() { player ->
            player.thirdPartyKey == thirdPartyId
        }
    }

    /**
     * Import thom chesspairingTournament and matches the players by playerKey
     */
    fun importFromChesspairingTournament(chesspairingTournament: ChesspairingTournament) {

        val players: MutableList<Player> = mutableListOf()
        val importPlayers = chesspairingTournament.players
        importPlayers.forEach {
            val player = getClubPlayerByThirdPartiId(it.playerKey)
            if (null != player) {
                players.add(player)
                player.isArchived = false;
                val playersLoc: String = Constants.LOCATION_CLUB_PLAYERS
                    .replace(Constants.CLUB_KEY, player.clubKey) + "/${player.playerKey}"
                val database =
                    FirebaseDatabase.getInstance()
                val dbRef =
                    database.getReference(playersLoc)
                dbRef.setValue(player)
            } else {
                val newPlayerInfo = buildPlayer(it)
                val newPlayer = MyFirebaseUtils.persistPlayer(newPlayerInfo)
                players.add(newPlayer)
            }
        }
        GlobalScope.launch {
            replaceInitialList(players, chesspairingTournament)
        }
    }

    private fun buildPlayer(importedPlayer: ChesspairingPlayer): Player {
        val player = Player(
            importedPlayer.name,
            null,
            clubId.value,
            importedPlayer.elo,
            importedPlayer.elo
        )
        player.thirdPartyKey = importedPlayer.playerKey
        return player
    }

    fun replaceInitialList(players: List<Player>, importedTournament: ChesspairingTournament) {
        // delete current players
        val allPlayersLoc: String = Constants.LOCATION_TOURNAMENT_PLAYERS
            .replace(Constants.CLUB_KEY, clubId.value!!)
            .replace(Constants.TOURNAMENT_KEY, tournamentId.value!!)
        val allPlayersRef = FirebaseDatabase.getInstance().getReference(allPlayersLoc)
        allPlayersRef.removeValue()

        // add all players to the tournament
        players.forEach {
            val tournamentPlayerLoc: String = Constants.LOCATION_TOURNAMENT_PLAYERS
                .replace(Constants.CLUB_KEY, clubId.value!!)
                .replace(Constants.TOURNAMENT_KEY, tournamentId.value!!) +
                    "/" + it.playerKey
            val playerRef = FirebaseDatabase.getInstance().getReference(tournamentPlayerLoc)
            playerRef.setValue(it)
        }

        // delete initial order
        val initialOrder = Constants.LOCATION_TOURNAMENT_PLAYER_INITIAL_ORDER
            .replace(Constants.CLUB_KEY, clubId.value!!)
            .replace(Constants.TOURNAMENT_KEY, tournamentId.value!!)
            .replace("/" + Constants.PLAYER_KEY, "")
        val initialOrderReference =
            FirebaseDatabase.getInstance()
                .getReference(initialOrder)
        initialOrderReference.removeValue()

        //set new order
        val tournament: ChesspairingTournament = MyFirebaseUtils().buildChessPairingTournament(
            clubId.value!!, tournamentId.value!!
        )

        val rankedPlayers: MutableList<RankedPlayer> = mutableListOf()

        players.forEach {
            val newInitialOrder = importedTournament.getPlayerById(it.thirdPartyKey).rank
            val chesspairingPlayer = tournament.getPlayerById(it.playerKey);
            chesspairingPlayer.initialOrderId = newInitialOrder

            val ranked = RankedPlayer(
                chesspairingPlayer,
                tournamentId.value!!, clubId.value!!, it.profilePictureUri
            )
            rankedPlayers.add(ranked)
        }

        // persist new order
        rankedPlayers.forEach { rankedPlayer ->
            val tournamentOrderLocation =
                Constants.LOCATION_TOURNAMENT_PLAYER_INITIAL_ORDER
                    .replace(Constants.CLUB_KEY, clubId.value!!)
                    .replace(Constants.TOURNAMENT_KEY, tournamentId.value!!)
                    .replace(Constants.PLAYER_KEY, rankedPlayer.playerKey)
            val updatedOrderReference =
                FirebaseDatabase.getInstance()
                    .getReference(tournamentOrderLocation)
            updatedOrderReference.setValue(rankedPlayer)
        }
    }
}
