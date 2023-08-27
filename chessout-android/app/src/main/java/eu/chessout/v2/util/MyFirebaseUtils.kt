package eu.chessout.v2.util

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storageMetadata
import eu.chessdata.chesspairing.algoritms.fideswissduch.Algorithm
import eu.chessdata.chesspairing.algoritms.javafo.JavafoWrapp
import eu.chessdata.chesspairing.model.*
import eu.chessout.shared.Constants
import eu.chessout.shared.Locations
import eu.chessout.shared.dao.BasicApiResponse
import eu.chessout.shared.model.*
import eu.chessout.v2.model.BasicApiService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.CountDownLatch
import kotlin.collections.set

class MyFirebaseUtils {

    companion object {

        fun registerUserDisplayNameListener(
            singleValueEvent: Boolean,
            userId: String,
            stringListener: StringListener
        ) {
            val valueEventListener = object : ValueEventListener {
                override fun onCancelled(e: DatabaseError) {
                    throw IllegalStateException("Problems with database: ${e.message}")
                }

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val displayName = dataSnapshot.getValue(String::class.java)
                        if (displayName != null) {
                            stringListener.valueUpdated(displayName)
                            return
                        }
                    }
                    stringListener.valueUpdated(null)
                }
            }
            val location = Locations.userDisplayName(userId)
            val db = getDatabaseReference(location)
            if (singleValueEvent) {
                db.addListenerForSingleValueEvent(valueEventListener)
            } else {
                db.addValueEventListener(valueEventListener)
            }
        }

        fun registerUserEmailListener(
            singleValueEvent: Boolean,
            userId: String,
            stringListener: StringListener
        ) {
            val valueEventListener = object : ValueEventListener {
                override fun onCancelled(e: DatabaseError) {
                    throw IllegalStateException("Problems with database: ${e.message}")
                }

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val displayName = dataSnapshot.getValue(String::class.java)
                        if (displayName != null) {
                            stringListener.valueUpdated(displayName)
                            return
                        }
                    }
                    stringListener.valueUpdated(null)
                }
            }
            val location = Locations.userEmailValue(userId)
            val db = getDatabaseReference(location)
            if (singleValueEvent) {
                db.addListenerForSingleValueEvent(valueEventListener)
            } else {
                db.addValueEventListener(valueEventListener)
            }
        }


        fun getUserDisplayName(
            userId: String
        ): String {
            var displayName = "no-display-name"
            val latch = CountDownLatch(1)

            class MyStringListener : MyFirebaseUtils.StringListener {
                override fun valueUpdated(value: String?) {
                    try {
                        value?.let {
                            displayName = value
                            latch.countDown()
                        }
                    } catch (e: IllegalStateException) {
                        latch.countDown()
                    }
                }
            }
            registerUserDisplayNameListener(
                true, userId, MyStringListener()
            )
            latch.await()

            return displayName
        }

        fun registerTournamentListener(
            singleValueEvent: Boolean,
            clubId: String, tournamentId: String,
            tournamentListener: TournamentListener
        ) {
            val ValueEventListener = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    throw IllegalStateException("Database error: ${p0.message}")
                }

                override fun onDataChange(p0: DataSnapshot) {
                    val tournament = p0.getValue(Tournament::class.java)
                    if (null != tournament) {
                        tournamentListener.onTournamentValue(tournament)
                    }
                }

            }

            val location = Locations.tournament(clubId, tournamentId)

        }

        fun getClub(clubId: String): Club? {
            var club: Club? = null
            val latch = CountDownLatch(1)
            val valueEventListener = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    throw IllegalStateException("Database error: ${p0.message}")
                }

                override fun onDataChange(p0: DataSnapshot) {
                    club = p0.getValue(Club::class.java)
                    latch.countDown()
                }
            }

            val clubLocation = Locations.club(clubId)
            val db = MyFirebaseUtils().getDatabaseReference(clubLocation)
            db.addListenerForSingleValueEvent(valueEventListener)
            latch.await()
            return club
        }

        fun getDatabaseReference(absolutePath: String): DatabaseReference {
            return FirebaseDatabase.getInstance().getReference(absolutePath)
        }

        fun currentUserId(): String {
            return FirebaseAuth.getInstance().currentUser!!.uid
        }


        fun currentUserToken(): String? {
            var idToken: String? = null
            val firebaseUser = FirebaseAuth.getInstance().currentUser
            val latch = CountDownLatch(1)
            firebaseUser?.getIdToken(true)
                ?.addOnCompleteListener { task -> //TaskOnCompleteListener<GetTokenResult>
                    if (task.isSuccessful) {
                        idToken = task.result?.token
                    }
                    latch.countDown()
                }
            latch.await()
            return idToken
        }

        fun userUpdateClubSettings(clubId: String, userId: String?, settings: ClubSettings) {
            val loc = Locations.clubSettings(clubId, userId)
            val db = FirebaseDatabase.getInstance().getReference(loc)
            db.setValue(settings)
        }

        fun userUpdateFollowPlayerSettings(
            playerId: String,
            userId: String?,
            settings: FollowPlayerSettings
        ) {
            val loc = Locations.followPlayerSettings(playerId, userId)
            val db = FirebaseDatabase.getInstance().getReference(loc)
            db.setValue(settings)
        }

        fun userUpdateFollowFideIdSettings(
            fideId: String,
            userId: String,
            settings: FollowPlayerSettings
        ) {
            val loc = Locations.followFideIdSettings(fideId, userId)
            val db = FirebaseDatabase.getInstance().getReference(loc)
            db.setValue(settings)
        }

        fun persistPostInUserStream(userId: String, post: Post) {
            val loc = Locations.userStreamPost(userId, post.postId)
            val ref = FirebaseDatabase.getInstance().getReference(loc)
            ref.setValue(post)
            updateUserStreamPostReverseOrder(userId, post)
        }

        private fun updateUserStreamPostReverseOrder(userId: String, post: Post) {
            val loc = Locations.userStreamPost(userId, post.postId)
            val db = FirebaseDatabase.getInstance().getReference(loc)
            val valueEventListener = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    Log.e(Constants.LOG_TAG, "updateUserStreamPostReverseOrder ${p0.message}")
                }

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val post: Post? = dataSnapshot.getValue(Post::class.java)
                    if (post != null) {
                        val timeStamp: Long = post.dateCreatedStamp()
                        val reversedDateCreated = 0 - timeStamp
                        db.child("reversedDateCreated").setValue(reversedDateCreated)
                    }
                }

            }
            db.addListenerForSingleValueEvent(valueEventListener)
        }

        private fun setPictureUploadCompleteInUserStreamPost(
            userId: String,
            postId: String,
            index: Int
        ) {
            val loc = Locations.pictureInUserStreamPostUploadComplete(userId, postId, index)
            val db = FirebaseDatabase.getInstance().getReference(loc)
            db.setValue(true)
        }

        private fun notifyTheBackendClubPostPictureUploadComplete(
            clubId: String,
            postId: String,
            pictureIndex: Int
        ) {
            val myPayLoad = MyPayLoad()
            myPayLoad.event = MyPayLoad.Event.CLUB_POST_PICTURE_UPLOAD_COMPLETE
            myPayLoad.postId = postId
            myPayLoad.clubId = clubId
            myPayLoad.pictureIndex = pictureIndex
            myPayLoad.authToken = MyFirebaseUtils.currentUserToken()

            BasicApiService().clubPostPictureUploadComplete(myPayLoad)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<BasicApiResponse>() {
                    override fun onSuccess(value: BasicApiResponse?) {
                        Log.d(Constants.LOG_TAG, "Success${value?.message}")
                    }

                    override fun onError(e: Throwable?) {
                        Log.e(Constants.LOG_TAG, "Error ${e?.message}")
                    }

                })
        }

        fun getCurrentUserId(): String? {
            return FirebaseAuth.getInstance().currentUser?.uid
        }

        fun addToFollowedPlayers(userId: String, playerId: String, player: Player) {
            val db = FirebaseDatabase.getInstance()
            val loc = Locations.followedPlayer(userId, playerId)
            val ref = db.getReference(loc)
            ref.setValue(player)
        }

        fun persistPlayer(player: Player): Player {
            val database =
                FirebaseDatabase.getInstance()

            val playersLoc: String = Constants.LOCATION_CLUB_PLAYERS
                .replace(Constants.CLUB_KEY, player.clubKey)
            val playersRef =
                database.getReference(playersLoc)
            val playerRef = playersRef.push()
            val playerKey = playerRef.key
            player.playerKey = playerKey
            playerRef.setValue(player)
            return player
        }

        fun postTournamentPairingsAvailablePost(
            postType: Post.PostType,
            clubId: String,
            tournamentId: String,
            roundId: Int
        ): Post {
            val currentUserId = MyFirebaseUtils.currentUserId()
            val post = buildTournamentPairingsAvailablePost(
                postType,
                currentUserId,
                clubId,
                tournamentId,
                roundId
            )
            val finalPost = MyFirebaseUtils().createAndPersistPost(post)
            persistPostInUserStream(currentUserId, finalPost);
            return finalPost
        }

        private fun buildTournamentPairingsAvailablePost(
            postType: Post.PostType,
            userId: String?,
            clubId: String,
            tournamentId: String,
            roundId: Int
        ): Post {

            val post = Post()
            post.setTimeStamps()
            post.postType = postType
            post.userId = userId
            post.clubId = clubId
            post.tournamentId = tournamentId
            post.roundId = roundId

            var realUserId = userId;
            if (realUserId == null) {
                realUserId = ""
            }

            post.userPictureUrl = MyFirebaseUtils().getUserProfilePictureUri(realUserId)
            val club: Club = MyFirebaseUtils().getClubPublicInfo(clubId)
            post.clubPictureUrl = club.picture?.stringUri
            post.clubShortName = club.shortName

            return post
        }


    }

    fun setDefaultClub(defaultClub: DefaultClub) {
        val database =
            FirebaseDatabase.getInstance()
        val uid =
            FirebaseAuth.getInstance().currentUser!!.uid

        val defaultClubLocation: String = Constants.LOCATION_DEFAULT_CLUB
            .replace(Constants.USER_KEY, uid)
        val managedClubRef =
            database.getReference(defaultClubLocation)
        managedClubRef.setValue(defaultClub)
    }

    interface DefaultClubListener {
        fun onDefaultClubValue(defaultClub: DefaultClub)
        fun setDbRef(databaseReference: DatabaseReference)
        fun setDbListener(valueEventListener: ValueEventListener)
    }

    interface ClubListener {
        fun onClubValue(club: Club)
    }

    interface TournamentListener {
        fun onTournamentValue(tournament: Tournament)
    }

    interface IsAdminListener {
        fun onIsAdmin(isAdmin: Boolean)
    }

    interface BoolListener {
        fun boolValueChanged(newValue: Boolean)
    }

    interface StringListener {
        fun valueUpdated(value: String?)
    }

    interface PlayersListener {
        fun listUpdated(players: List<Player>)
    }

    interface RankedPlayerListener {
        fun listUpdated(players: List<RankedPlayer>)
    }

    interface PostsListener {
        fun listUpdated(posts: List<Post>)
    }

    interface GamesListener {
        fun listUpdated(games: List<Game>)
    }

    interface LongListener {
        fun valueUpdated(value: Long)
    }

    interface PictureListener {
        fun valueUpdated(value: Picture?)
    }

    interface PlayerListener {
        fun valueUpdated(value: Player)
    }


    @Deprecated(message = "Use registerDefaultClubListener instead")
    fun getDefaultClubSingleValueListener(
        listener: DefaultClubListener
    ) {
        val database =
            FirebaseDatabase.getInstance()
        val uid =
            FirebaseAuth.getInstance().currentUser!!.uid
        val defaultClubLocation = Constants.LOCATION_DEFAULT_CLUB
            .replace(Constants.USER_KEY, uid)
        val defaultClubRef =
            database.getReference(defaultClubLocation)
        defaultClubRef.addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val defaultClub = dataSnapshot.getValue(DefaultClub::class.java)
                if (null != defaultClub) {
                    listener.onDefaultClubValue(defaultClub!!)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }


    @Deprecated(message = "Use registerDefaultClubListener instead")
    fun getDefaultClubListener(
        listener: DefaultClubListener
    ) {
        val database =
            FirebaseDatabase.getInstance()
        val uid =
            FirebaseAuth.getInstance().currentUser!!.uid
        val defaultClubLocation = Constants.LOCATION_DEFAULT_CLUB
            .replace(Constants.USER_KEY, uid)
        val defaultClubRef =
            database.getReference(defaultClubLocation)
        defaultClubRef.addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val defaultClub = dataSnapshot.getValue(DefaultClub::class.java)
                if (null != defaultClub) {
                    listener.onDefaultClubValue(defaultClub!!)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    fun registerDefaultClubListener(listener: DefaultClubListener, singleValueEvent: Boolean) {
        val database =
            FirebaseDatabase.getInstance()
        val uid =
            FirebaseAuth.getInstance().currentUser?.uid ?: return
        val defaultClubLocation = Constants.LOCATION_DEFAULT_CLUB
            .replace(Constants.USER_KEY, uid)
        val dbRef =
            database.getReference(defaultClubLocation)
        listener.setDbRef(dbRef)

        val valueEventListener = object : ValueEventListener {
            override fun onCancelled(databaseError: DatabaseError) {
                //nothing to implement
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val defaultClub = dataSnapshot.getValue(DefaultClub::class.java)
                if (null != defaultClub) {
                    listener.onDefaultClubValue(defaultClub!!)
                }
            }
        }
        listener.setDbListener(valueEventListener)

        if (singleValueEvent) {
            dbRef.addListenerForSingleValueEvent(valueEventListener)
        } else {
            dbRef.addValueEventListener(valueEventListener)
        }
    }

    fun setMyClub(myClub: DefaultClub) {
        val database =
            FirebaseDatabase.getInstance()
        val uid =
            FirebaseAuth.getInstance().currentUser!!.uid

        val defaultClubLocation: String = Constants.LOCATION_L_MY_CLUB
            .replace(Constants.USER_KEY, uid)
        val managedClubRef =
            database.getReference(defaultClubLocation)
        managedClubRef.setValue(myClub)
    }

    fun setManager(
        displayName: String,
        email: String,
        clubId: String,
        uid: String
    ) {
        //set manager
        val database = FirebaseDatabase.getInstance()

        val managersLocation: String = Constants.LOCATION_CLUB_MANAGERS
            .replace(Constants.CLUB_KEY, clubId!!)
            .replace(Constants.MANAGER_KEY, uid)
        val clubManager = User(displayName, email)
        val managersRef =
            database.getReference(managersLocation)
        managersRef.setValue(clubManager)
    }

    fun addToMyClubs(uid: String, clubId: String, club: Club) {
        val database = FirebaseDatabase.getInstance()
        val myClubLocation: String = Constants.LOCATION_MY_CLUBS
            .replace(Constants.USER_KEY, uid)
            .replace(Constants.CLUB_KEY, clubId)
        val myClubsRef =
            database.getReference(myClubLocation)
        myClubsRef.setValue(club)
    }

    fun updateTournamentReversedOrder(
        clubKey: String?,
        tournamentKey: String
    ) {
        val database =
            FirebaseDatabase.getInstance()
        val tournamentLocation = Constants.LOCATION_TOURNAMENTS
            .replace(Constants.CLUB_KEY, clubKey!!) + "/" + tournamentKey
        val tournamentRef =
            database.getReference(tournamentLocation)
        tournamentRef.addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val tournament: Tournament? = dataSnapshot.getValue(Tournament::class.java)
                if (tournament != null) {
                    val timeStamp: Long = tournament.dateCreatedGetLong()
                    val reversedDateCreated = 0 - timeStamp
                    tournamentRef.child("reversedDateCreated").setValue(reversedDateCreated)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    @Deprecated("Replaced by registerIsAdminListener(eventType,clubId,listener)")
    fun isCurrentUserAdmin(clubKey: String?, isAdminListener: IsAdminListener) {
        val managers =
            arrayOf(false) //first boolean holds the result
        val uid =
            FirebaseAuth.getInstance().currentUser!!.uid
        val clubManagerLoc = Constants.LOCATION_CLUB_MANAGERS
            .replace(Constants.CLUB_KEY, clubKey!!)
            .replace(Constants.MANAGER_KEY, uid)
        val managerRef =
            FirebaseDatabase.getInstance().getReference(clubManagerLoc)
        managerRef.addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.value != null) {
                    isAdminListener.onIsAdmin(true)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                isAdminListener.onIsAdmin(false)
            }
        })
    }

    fun registerIsAdminListener(
        singleValueEvent: Boolean,
        clubId: String,
        isAdminListener: IsAdminListener
    ) {

        val uid =
            FirebaseAuth.getInstance().currentUser!!.uid
        val clubManagerLoc = Constants.LOCATION_CLUB_MANAGERS
            .replace(Constants.CLUB_KEY, clubId)
            .replace(Constants.MANAGER_KEY, uid)
        val dbRef =
            FirebaseDatabase.getInstance().getReference(clubManagerLoc)

        val valueEventListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                isAdminListener.onIsAdmin(false)
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.value != null) {
                    isAdminListener.onIsAdmin(true)
                } else {
                    isAdminListener.onIsAdmin(false)
                }
            }
        }

        if (singleValueEvent) {
            dbRef.addListenerForSingleValueEvent(valueEventListener)
        } else {
            dbRef.addValueEventListener(valueEventListener)
        }
    }

    fun isCurrentUserAdmin(clubKey: String): Boolean {
        val managers =
            arrayOf(false) //first boolean holds the result
        val uid =
            FirebaseAuth.getInstance().currentUser!!.uid
        val clubManagerLoc = Constants.LOCATION_CLUB_MANAGERS
            .replace(Constants.CLUB_KEY, clubKey!!)
            .replace(Constants.MANAGER_KEY, uid)
        val managerRef =
            FirebaseDatabase.getInstance().getReference(clubManagerLoc)
        val latch = CountDownLatch(1)
        val adminItem = arrayListOf(false)
        managerRef.addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.value != null) {
                    adminItem[0] = true
                }
                latch.countDown()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                latch.countDown()
            }
        })
        latch.await()
        return adminItem[0]
    }

    fun awaitIsCurrentUserAdmin(clubKey: String?): Boolean {
        val managers =
            arrayOf(false) //first boolean holds the result
        val uid =
            FirebaseAuth.getInstance().currentUser!!.uid
        val clubManagerLoc = Constants.LOCATION_CLUB_MANAGERS
            .replace(Constants.CLUB_KEY, clubKey!!)
            .replace(Constants.MANAGER_KEY, uid)
        val managerRef =
            FirebaseDatabase.getInstance().getReference(clubManagerLoc)
        val isAdmin = arrayOf(false)
        val latch = CountDownLatch(1)
        val valueEventListener: ValueEventListener = object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.value != null) {
                    isAdmin[0] = true
                }
                latch.countDown()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                latch.countDown()
            }
        }
        managerRef.addListenerForSingleValueEvent(valueEventListener)
        latch.await()
        return isAdmin[0]
    }

    private fun getClubPlayersRefString(clubId: String): String {
        return Constants.LOCATION_CLUB_PLAYERS
            .replace(Constants.CLUB_KEY, clubId)
    }

    fun registerClubPlayersListener(
        clubId: String,
        singleValueEvent: Boolean,
        playersListener: PlayersListener
    ) {
        val clubPlayersLoc = getClubPlayersRefString(clubId)
        val clubPlayersRef =
            FirebaseDatabase.getInstance().getReference(clubPlayersLoc)
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val players = ArrayList<Player>()
                for (item in dataSnapshot.children) {
                    val player = item.getValue(Player::class.java)
                    players.add(player!!)
                }
                playersListener.listUpdated(players)
            }

            override fun onCancelled(p0: DatabaseError) {
                // nothing to do
            }
        }
        if (singleValueEvent) {
            clubPlayersRef.addListenerForSingleValueEvent(valueEventListener)
        } else {
            clubPlayersRef.addValueEventListener(valueEventListener)
        }
    }

    fun registerTournamentPlayersListener(
        clubId: String,
        tournamentId: String,
        singleValueEvent: Boolean,
        playersListener: PlayersListener
    ) {
        val playersLoc = Constants.LOCATION_TOURNAMENT_PLAYERS
            .replace(Constants.CLUB_KEY, clubId)
            .replace(Constants.TOURNAMENT_KEY, tournamentId)
        val mReference =
            FirebaseDatabase.getInstance().getReference(playersLoc)

        val valueEventListener: ValueEventListener =
            object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val tournamentPlayers = ArrayList<Player>()
                    for (item in dataSnapshot.children) {
                        val rankedPlayer = item.getValue(Player::class.java)
                        tournamentPlayers.add(rankedPlayer!!)
                    }
                    playersListener.listUpdated(tournamentPlayers)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                }
            }
        if (singleValueEvent) {
            mReference.addListenerForSingleValueEvent(valueEventListener)
        } else {
            mReference.addValueEventListener(valueEventListener)
        }
    }

    fun getMissingPlayers(
        clubId: String,
        tournamentId: String,
        roundId: Int,
        singleValueEvent: Boolean,
        playersListener: PlayersListener
    ) {
        val roundPlayersLoc = Constants.LOCATION_ROUND_ABSENT_PLAYERS
            .replace(Constants.CLUB_KEY, clubId)
            .replace(Constants.TOURNAMENT_KEY, tournamentId)
            .replace(Constants.ROUND_NUMBER, roundId.toString())
        val mReference = FirebaseDatabase.getInstance()
            .getReference(roundPlayersLoc)

        val valueEventListener: ValueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val absentList = ArrayList<Player>()
                for (item in dataSnapshot.children) {
                    val player = item.getValue(Player::class.java)
                    absentList.add(player!!)
                }
                playersListener.listUpdated(absentList)
            }

            override fun onCancelled(p0: DatabaseError) {
                // nothing to implement
            }
        }
        if (singleValueEvent) {
            mReference.addListenerForSingleValueEvent(valueEventListener)
        } else {
            mReference.addValueEventListener(valueEventListener)
        }
    }

    fun awaitGetTournamentPlayers(clubId: String, tournamentId: String): List<Player> {
        val playersLoc = Constants.LOCATION_TOURNAMENT_PLAYERS
            .replace(Constants.CLUB_KEY, clubId)
            .replace(Constants.TOURNAMENT_KEY, tournamentId)
        val mReference =
            FirebaseDatabase.getInstance().getReference(playersLoc)
        val players = ArrayList<Player>()
        val latch = CountDownLatch(1)
        val valueEventListener: ValueEventListener = object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                for (item in p0.children) {
                    val player = item.getValue(Player::class.java)
                    players.add(player!!)
                }
                latch.countDown()
            }

            override fun onCancelled(p0: DatabaseError) {
                latch.countDown()
            }
        }
        mReference.addListenerForSingleValueEvent(valueEventListener)
        latch.await()

        return players
    }


    fun refreshTournamentInitialOrder(clubId: String, tournamentId: String) {
        if (!isCurrentUserAdmin(clubId)) {
            return
        }

        val tournament: ChesspairingTournament = buildChessPairingTournament(
            clubId, tournamentId
        )

        val playersList = getTournamentPlayers(clubId, tournamentId)
        val clubPlayersMap = HashMap<String, Player>()
        playersList.forEach {
            clubPlayersMap[it.playerKey] = it
        }

        val clubKey = clubId
        val tournamentKey = tournamentId

        // initial order
        var initialRankedPlayers: List<RankedPlayer> =
            getTournamentInitialOrder(clubId, tournamentKey)
        val playerMap: MutableMap<String, RankedPlayer> =
            LinkedHashMap()
        tournament.players.forEach {
            val rankedPlayer = RankedPlayer(
                it, tournamentKey, clubKey,
                clubPlayersMap[it.playerKey]!!.profilePictureUri
            )
            playerMap[it.playerKey] = rankedPlayer
        }
        initialRankedPlayers.forEach {
            // replace with RankedPlayer only if already in the map
            if (playerMap.containsKey(it.playerKey)) {
                playerMap[it.playerKey] = it
            }
        }
        val rankedPlayer = mutableListOf<RankedPlayer>()
        for ((key, value) in playerMap) {
            rankedPlayer.add(value)
        }

        val comparator = Comparator<RankedPlayer> { player, to ->
            var playerRank = 0;
            player.elo?.let { playerRank = it }
            var toRank = 0;
            to.elo?.let { toRank = it }
            -1 * (playerRank - toRank)
        }

        val orderedList = rankedPlayer.sortedWith(comparator)
        var rank = 1

        // update player.tournamentInitialOrder value
        if (initialRankedPlayers.isNotEmpty()) {
            for (player in orderedList) {
                player.tournamentInitialOrder = rank++
                playerMap[player.playerKey] = player
            }
        }

        //delete initial order
        val initialOrder = Constants.LOCATION_TOURNAMENT_PLAYER_INITIAL_ORDER
            .replace(Constants.CLUB_KEY, clubId)
            .replace(Constants.TOURNAMENT_KEY, tournamentKey)
            .replace("/" + Constants.PLAYER_KEY, "")
        val initialOrderReference =
            FirebaseDatabase.getInstance()
                .getReference(initialOrder)
        initialOrderReference.removeValue()

        //set new order

        for (rankedPlayer in playerMap.values) {
            val tournamentOrderLocation =
                Constants.LOCATION_TOURNAMENT_PLAYER_INITIAL_ORDER
                    .replace(Constants.CLUB_KEY, clubId)
                    .replace(Constants.TOURNAMENT_KEY, tournamentKey)
                    .replace(Constants.PLAYER_KEY, rankedPlayer.playerKey)
            val updatedOrderReference =
                FirebaseDatabase.getInstance()
                    .getReference(tournamentOrderLocation)
            updatedOrderReference.setValue(rankedPlayer)
        }

        Log.d(
            Constants.LOG_TAG,
            "End of eu.chessdata.utils.MyFirebaseUtils.refreshTournamentInitialOrder"
        )

        Log.d(Constants.LOG_TAG, "Refresh initial order clubId=$clubId tournamentId=$tournamentId")
    }


    /**
     * it collects the entire required data from firebase in order to build the current state
     * of the tournament
     *
     * @param clubKey
     * @param tournamentKey
     * @return
     */
    fun buildChessPairingTournament(
        clubKey: String,
        tournamentKey: String
    ): ChesspairingTournament {
        val chesspairingTournament = ChesspairingTournament()
        val latch1 = CountDownLatch(1)
        //get the general description
        val tournamentLoc = Constants.LOCATION_TOURNAMENT
            .replace(Constants.CLUB_KEY, clubKey!!)
            .replace(Constants.TOURNAMENT_KEY, tournamentKey!!)
        val tournamentRef =
            FirebaseDatabase.getInstance().getReference(tournamentLoc)
        tournamentRef.addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val tournament = dataSnapshot.getValue(Tournament::class.java)
                if (tournament != null) {
                    chesspairingTournament.name = tournament.name
                    chesspairingTournament.description = tournament.description
                    chesspairingTournament.totalRounds = tournament.totalRounds
                    chesspairingTournament.totalRounds = tournament.totalRounds
                }
                tournamentRef.removeEventListener(this)
                latch1.countDown()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(Constants.LOG_TAG, databaseError.message)
                latch1.countDown()
            }
        })
        try {
            latch1.await()
        } catch (e: InterruptedException) {
            Log.e(Constants.LOG_TAG, "tournamentDetailsError: " + e.message)
        }

        //populate players
        val players: List<Player> =
            getTournamentPlayers(clubKey, tournamentKey)

        //initial order
        val rankedPlayers: List<RankedPlayer> =
            getTournamentInitialOrder(clubKey, tournamentKey)
        val initialOrder: MutableMap<String, Int> =
            HashMap()
        var weHaveInitialOrder = false
        if (rankedPlayers.size > 0) {
            weHaveInitialOrder = true
            var i = 1
            for (player in rankedPlayers) {
                initialOrder[player.getPlayerKey()] = i++
            }
        }
        val chesspairingPlayers: MutableList<ChesspairingPlayer> =
            java.util.ArrayList()
        var i = 0
        var k = -1
        for (player in players) {
            i++ //set the player order as the natural one collected from firebase
            val chesspairingPlayer: ChesspairingPlayer = scanPlayer(player)
            if (weHaveInitialOrder) {
                if (initialOrder.containsKey(chesspairingPlayer.playerKey)) {
                    chesspairingPlayer.initialOrderId = initialOrder[chesspairingPlayer.playerKey]!!
                } else {
                    k++
                    val order = k + initialOrder.size
                    chesspairingPlayer.initialOrderId = k + initialOrder.size
                }
            } else {
                chesspairingPlayer.initialOrderId = i
            }
            chesspairingPlayers.add(chesspairingPlayer)
        }
        val comparator =
            Comparator { p1: ChesspairingPlayer, p2: ChesspairingPlayer -> p1.initialOrderId - p2.initialOrderId }
        chesspairingPlayers.sortWith(comparator)
        chesspairingTournament.players = chesspairingPlayers

        //populate the rounds
        chesspairingTournament.rounds = getTournamentRounds(clubKey, tournamentKey)
        return chesspairingTournament
    }

    fun getTournamentInitialOrder(clubId: String, tournamentKey: String): List<RankedPlayer> {
        val playerList: ArrayList<RankedPlayer> =
            java.util.ArrayList<RankedPlayer>()
        val latch = CountDownLatch(1)
        val playersLoc = Constants.LOCATION_TOURNAMENT_INITIAL_ORDER
            .replace(Constants.CLUB_KEY, clubId)
            .replace(Constants.TOURNAMENT_KEY, tournamentKey!!)
        val playersRef =
            FirebaseDatabase.getInstance().getReference(playersLoc)
        playersRef.addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val it: Iterator<DataSnapshot> =
                    dataSnapshot.children.iterator()
                while (it.hasNext()) {
                    val player: RankedPlayer? = it.next().getValue(RankedPlayer::class.java)
                    playerList.add(player!!)
                }
                latch.countDown()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(Constants.LOG_TAG, databaseError.message)
                latch.countDown()
            }
        })
        try {
            latch.await()
        } catch (e: InterruptedException) {
            // nothing to do
        }
        val comparator: Comparator<RankedPlayer> =
            Comparator<RankedPlayer> { pa, pb -> pa.getTournamentInitialOrder() - pb.getTournamentInitialOrder() }
        playerList.sortWith(comparator)
        return playerList
    }


    fun observeTournamentInitialOrder(
        singleValueEvent: Boolean,
        clubId: String,
        tournamentKey: String,
        rankedPlayerListener: RankedPlayerListener
    ) {
        val playerList: ArrayList<RankedPlayer> =
            java.util.ArrayList<RankedPlayer>()
        val playersLoc = Constants.LOCATION_TOURNAMENT_INITIAL_ORDER
            .replace(Constants.CLUB_KEY, clubId)
            .replace(Constants.TOURNAMENT_KEY, tournamentKey!!)
        val playersRef =
            FirebaseDatabase.getInstance().getReference(playersLoc)

        val valueEventListener = object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                playerList.clear()
                val it: Iterator<DataSnapshot> =
                    dataSnapshot.children.iterator()
                while (it.hasNext()) {
                    val player: RankedPlayer? = it.next().getValue(RankedPlayer::class.java)
                    playerList.add(player!!)
                }
                val comparator: Comparator<RankedPlayer> =
                    Comparator<RankedPlayer> { pa, pb -> pa.getTournamentInitialOrder() - pb.getTournamentInitialOrder() }
                playerList.sortWith(comparator)
                rankedPlayerListener.listUpdated(playerList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(Constants.LOG_TAG, databaseError.message)
            }
        }
        if (singleValueEvent) {
            playersRef.addListenerForSingleValueEvent(valueEventListener)
        } else {
            playersRef.addValueEventListener(valueEventListener)
        }
    }

    private fun buildGamesRef(
        clubId: String,
        tournamentKey: String,
        roundId: Int
    ): DatabaseReference {
        val gamesLoc = Constants.LOCATION_ROUND_GAMES
            .replace(Constants.CLUB_KEY, clubId)
            .replace(Constants.TOURNAMENT_KEY, tournamentKey)
            .replace(Constants.ROUND_NUMBER, roundId.toString())
        return FirebaseDatabase.getInstance().getReference(gamesLoc)
    }

    private fun buildStandingsRef(
        clubId: String,
        tournamentId: String,
        roundId: Int,
        categoryName: String
    ): DatabaseReference {
        val standingsRef = Constants.LOCATION_STANDINGS
            .replace(Constants.CLUB_KEY, clubId)
            .replace(Constants.TOURNAMENT_KEY, tournamentId)
            .replace(Constants.ROUND_NUMBER, roundId.toString())
            .replace(Constants.CATEGORY_NAME, categoryName)
            .replace("/" + Constants.STANDING_NUMBER, "")
        return FirebaseDatabase.getInstance().getReference(standingsRef)
    }

    fun observeRoundHasGames(
        singleValueEvent: Boolean,
        clubId: String,
        tournamentKey: String,
        roundId: Int,
        boolListener: BoolListener
    ) {
        val valueEventListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                boolListener.boolValueChanged(false)
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.value != null) {
                    boolListener.boolValueChanged(true)
                } else {
                    boolListener.boolValueChanged(false)
                }
            }
        }
        val gamesRef = buildGamesRef(clubId, tournamentKey, roundId)
        if (singleValueEvent) {
            gamesRef.addListenerForSingleValueEvent(valueEventListener)
        } else {
            gamesRef.addValueEventListener(valueEventListener)
        }
    }

    fun getTournamentPlayers(clubId: String, tournamentKey: String?): List<Player> {
        val playerList: MutableList<Player> = java.util.ArrayList()
        val latch = CountDownLatch(1)
        val playersLoc = Constants.LOCATION_TOURNAMENT_PLAYERS
            .replace(Constants.CLUB_KEY, clubId)
            .replace(Constants.TOURNAMENT_KEY, tournamentKey!!)
        val playersRef =
            FirebaseDatabase.getInstance().getReference(playersLoc)
        playersRef.addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val it: Iterator<DataSnapshot> =
                    dataSnapshot.children.iterator()
                while (it.hasNext()) {
                    val player = it.next().getValue(Player::class.java)
                    playerList.add(player!!)
                }
                latch.countDown()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(Constants.LOG_TAG, databaseError.message)
                latch.countDown()
            }
        })
        try {
            latch.await()
        } catch (e: InterruptedException) {
            Log.e(Constants.LOG_TAG, "getTournamentPlayers: " + e.message)
        }
        return playerList
    }


    fun getTournamentRounds(clubId: String, tournamentKey: String?): List<ChesspairingRound>? {
        val chesspairingRounds: MutableList<ChesspairingRound> =
            java.util.ArrayList()
        val latch = CountDownLatch(1)
        val sectionNotRequired =
            "/" + Constants.ROUND_NUMBER + "/" + Constants.ROUND_ABSENT_PLAYERS
        //get the rounds data
        val roundsLoc = Constants.LOCATION_ROUND_ABSENT_PLAYERS
            .replace(Constants.CLUB_KEY, clubId)
            .replace(sectionNotRequired, "")
            .replace(Constants.TOURNAMENT_KEY, tournamentKey!!)
        val roundsRef =
            FirebaseDatabase.getInstance().getReference(roundsLoc)
        roundsRef.addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val roundIterator: Iterator<DataSnapshot> =
                    dataSnapshot.children.iterator()
                while (roundIterator.hasNext()) {
                    val snapshot =
                        roundIterator.next()
                    val chesspairingRound = ChesspairingRound()
                    //get the round number
                    val roundNumber = snapshot.key
                    chesspairingRound.roundNumber = Integer.valueOf(roundNumber!!)
                    val absentPlayers: MutableList<Player?> =
                        java.util.ArrayList()
                    val games: List<Game> = java.util.ArrayList<Game>()

                    //get the absentPlayers
                    if (snapshot.hasChild(Constants.ROUND_ABSENT_PLAYERS)) {
                        val playersSnapshot =
                            snapshot.child(Constants.ROUND_ABSENT_PLAYERS)
                        val playersIterator: Iterator<DataSnapshot> =
                            playersSnapshot.children.iterator()
                        while (playersIterator.hasNext()) {
                            val player = playersIterator.next().getValue(Player::class.java)
                            absentPlayers.add(player)
                        }
                    }
                    //List<ChesspairingPlayer> absentChesspairingPlayers = new ArrayList<ChesspairingPlayer>();
                    for (player in absentPlayers) {
                        val chesspairingPlayer: ChesspairingPlayer =
                            scanPlayer(player!!)
                        chesspairingPlayer.isPresent = false
                        chesspairingRound.addAbsentPlayer(chesspairingPlayer)
                    }
                    Log.i(Constants.LOG_TAG, "Time to decode game")
                    //get the games
                    if (snapshot.hasChild(Constants.GAMES)) {
                        val gamesSnapshot =
                            snapshot.child(Constants.GAMES)
                        val gamesIterator: Iterator<DataSnapshot> =
                            gamesSnapshot.children.iterator()
                        val chesspairingGames: MutableList<ChesspairingGame> =
                            java.util.ArrayList()
                        val presentPlayers: MutableList<ChesspairingPlayer> =
                            java.util.ArrayList()
                        while (gamesIterator.hasNext()) {
                            val game: Game? = gamesIterator.next().getValue(Game::class.java)
                            if (game!!.whitePlayer == null) {
                                continue
                            }
                            val chesspairingGame: ChesspairingGame =
                                scanGame(game!!)
                            chesspairingGames.add(chesspairingGame)
                            if (chesspairingGame.whitePlayer != null) {
                                presentPlayers.add(chesspairingGame.whitePlayer)
                            }
                            if (chesspairingGame.blackPlayer != null) {
                                presentPlayers.add(chesspairingGame.blackPlayer)
                            }
                        }
                        chesspairingRound.games = chesspairingGames
                        chesspairingRound.presentPlayers = presentPlayers
                    }

                    //add the round
                    chesspairingRounds.add(chesspairingRound)
                }
                latch.countDown()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                latch.countDown()
            }
        })
        //wait for the thread to finish computation
        try {
            latch.await()
        } catch (e: InterruptedException) {
            Log.e(Constants.LOG_TAG, "tournamentDetailsError: " + e.message)
        }
        return chesspairingRounds
    }

    /**
     * Converts Player onto ChesspairingPlayer
     * @param player
     * @return
     */
    fun scanPlayer(player: Player): ChesspairingPlayer {
        val chesspairingPlayer = ChesspairingPlayer()
        chesspairingPlayer.name = player.name
        val elo: Int
        elo = if (player.elo !== 0) {
            player.elo
        } else {
            player.clubElo
        }
        chesspairingPlayer.elo = elo
        chesspairingPlayer.playerKey = player.playerKey
        return chesspairingPlayer
    }

    fun scanGame(game: Game): ChesspairingGame {
        val chesspairingGame = ChesspairingGame()
        chesspairingGame.tableNumber = game.getActualNumber()
        chesspairingGame.whitePlayer =
            scanPlayer(game.getWhitePlayer())
        if (game.getBlackPlayer() != null) {
            chesspairingGame.blackPlayer =
                scanPlayer(game.getBlackPlayer())
        }
        val result: ChesspairingResult =
            convertResult(game.getResult())
        chesspairingGame.result = result
        return chesspairingGame
    }

    fun convertResult(result: Int): ChesspairingResult {
        when (result) {
            0 -> return ChesspairingResult.NOT_DECIDED
            1 -> return ChesspairingResult.WHITE_WINS
            2 -> return ChesspairingResult.BLACK_WINS
            3 -> return ChesspairingResult.DRAW_GAME
            4 -> return ChesspairingResult.BYE
            5 -> return ChesspairingResult.WHITE_WINS_BY_FORFEIT
            6 -> return ChesspairingResult.BLACK_WINS_BY_FORFEIT
            7 -> return ChesspairingResult.DOUBLE_FORFEIT
            8 -> return ChesspairingResult.DRAW_REFEREE_DECISION
        }
        throw IllegalStateException("New result type. please convert: $result")
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    fun generateGamesForRound(
        clubKey: String,
        tournamentKey: String,
        roundId: Int
    ) {
        if (!isCurrentUserAdmin(clubKey)) {
            return
        }

        var tournament: ChesspairingTournament =
            buildChessPairingTournament(clubKey, tournamentKey)
        val algorithm: Algorithm = JavafoWrapp()

        // generate next round
        // we should catch an error and build a report that can be debugged
        tournament = algorithm.generateNextRound(tournament)
        val rounds = tournament.rounds
        val round = rounds[rounds.size - 1]
        persistNewGames(clubKey, tournamentKey, round)
        Log.d(
            Constants.LOG_TAG,
            "persistNewGames has bean initiated"
        )
    }

    fun persistNewGames(
        clubKey: String,
        tournamentKey: String,
        round: ChesspairingRound
    ) {
        val firstTableNumber: Int = getFirstTableNumber(clubKey, tournamentKey)
        val tempList: List<Player> =
            getTournamentPlayers(clubKey, tournamentKey)
        val playerMap: MutableMap<String, Player> =
            HashMap()
        for (player in tempList) {
            playerMap[player.playerKey] = player
        }

        //copy the games data
        val chesspairingGames = round.games
        val games: MutableList<Game> = java.util.ArrayList()
        for (chesspairingGame in chesspairingGames) {
            val game = Game()
            game.tableNumber = chesspairingGame.tableNumber
            game.actualNumber = chesspairingGame.tableNumber + firstTableNumber - 1
            game.whitePlayer = playerMap[chesspairingGame.whitePlayer.playerKey]
            if (chesspairingGame.blackPlayer != null) {
                //white player ad black player are present
                game.blackPlayer = playerMap[chesspairingGame.blackPlayer.playerKey]
            } else {
                game.result = 4
            }
            game.result = convertChesspairingResultToIntGameResult(chesspairingGame.result)
            games.add(game)

        }
        val roundNumber = round.roundNumber.toString()
        val gamesLoc = Constants.LOCATION_ROUND_GAMES
            .replace(Constants.CLUB_KEY, clubKey)
            .replace(Constants.TOURNAMENT_KEY, tournamentKey!!)
            .replace(Constants.ROUND_NUMBER, roundNumber)
        val allGamesRef =
            FirebaseDatabase.getInstance().getReference(gamesLoc)

        for (gameItem in games) {
            val gameRef =
                allGamesRef.ref.child(java.lang.String.valueOf(gameItem.tableNumber))
            gameRef.setValue(gameItem)
        }
    }

    /**
     * Please see Game class documentation for extra info
     */
    fun convertChesspairingResultToIntGameResult(chesspairingResult: ChesspairingResult): Int {
        return when (chesspairingResult) {
            ChesspairingResult.NOT_DECIDED -> 0
            ChesspairingResult.WHITE_WINS -> 1
            ChesspairingResult.BLACK_WINS -> 2
            ChesspairingResult.DRAW_GAME -> 3
            ChesspairingResult.BYE -> 4
            ChesspairingResult.WHITE_WINS_BY_FORFEIT -> 5
            ChesspairingResult.BLACK_WINS_BY_FORFEIT -> 6
            ChesspairingResult.DOUBLE_FORFEIT -> 7
            ChesspairingResult.DRAW_REFEREE_DECISION -> 8
        }
    }

    private fun getFirstTableNumber(
        clubKey: String,
        tournamentKey: String
    ): Int {
        val numbers = intArrayOf(1) //first number holds the result
        val latch = CountDownLatch(1)
        val tournamentLoc = Constants.LOCATION_TOURNAMENTS
            .replace(Constants.CLUB_KEY, clubKey)
            .replace(Constants.TOURNAMENT_KEY, tournamentKey)
        val tournamentRef =
            FirebaseDatabase.getInstance().getReference(tournamentLoc)
        tournamentRef.addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.value != null) {
                    val tournament = dataSnapshot.getValue(Tournament::class.java)
                    numbers[0] = tournament!!.firstTableNumber
                }
                latch.countDown()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(
                    Constants.LOG_TAG,
                    "getFirstTableNumber: " + databaseError.message
                )
                latch.countDown()
            }
        })
        try {
            latch.await()
        } catch (e: InterruptedException) {
            Log.e(Constants.LOG_TAG, "getFirstTableNumber " + e.message)
        }
        return numbers[0]
    }


    fun registerGamesListener(
        singleValueEvent: Boolean,
        clubId: String,
        tournamentId: String,
        roundId: Int,
        gamesListener: GamesListener
    ) {
        val valueEventListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                gamesListener.listUpdated(ArrayList())
            }

            override fun onDataChange(p0: DataSnapshot) {
                val newList = ArrayList<Game>()
                for (item in p0.children) {
                    val game = item.getValue(Game::class.java)
                    newList.add(game!!)
                }
                gamesListener.listUpdated(newList)
            }

        }
        val gamesReference = buildGamesRef(clubId, tournamentId, roundId)
        if (singleValueEvent) {
            gamesReference.addListenerForSingleValueEvent(valueEventListener)
        } else {
            gamesReference.addValueEventListener(valueEventListener)
        }
    }


    fun registerCompletedRoundsListener(
        singleValueEvent: Boolean,
        clubId: String,
        tournamentId: String,
        totalRounds: Long,
        longListener: LongListener
    ) {
        val sectionNotRequired =
            "/" + Constants.ROUND_NUMBER + "/" + Constants.ROUND_ABSENT_PLAYERS
        val roundsLoc = Constants.LOCATION_ROUND_ABSENT_PLAYERS
            .replace(Constants.CLUB_KEY, clubId)
            .replace(sectionNotRequired, "")
            .replace(Constants.TOURNAMENT_KEY, tournamentId)
        val roundsRef =
            FirebaseDatabase.getInstance().getReference(roundsLoc)

        val valueEventListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                // nothing to implement
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var roundsWithData = dataSnapshot.childrenCount
                if (roundsWithData == 0L) {
                    roundsWithData = 1
                }
                if (roundsWithData == totalRounds) {
                    longListener.valueUpdated(totalRounds)
                    return
                }


                if (!dataSnapshot.hasChild(roundsWithData.toString())) {
                    longListener.valueUpdated(roundsWithData)
                    return
                }

                // get the last round
                val vipRound = dataSnapshot.child(roundsWithData.toString())
                if (!vipRound.hasChild(Constants.GAMES)) {
                    return
                }
                val vipGames =
                    vipRound.child(Constants.GAMES)

                if (roundsWithData < totalRounds) {
                    var incrementRoundsWithData = true
                    /**
                     * iterate over games and if any results are 0 (not decided)
                     * then break the loop and set
                     * incrementRoundsWithData = false;
                     */
                    for (item in vipGames.children) {
                        val game = item.getValue(Game::class.java)
                        if (game!!.result === 0) {
                            incrementRoundsWithData = false
                            break
                        }
                    }
                    if (incrementRoundsWithData) {
                        longListener.valueUpdated(roundsWithData + 1)
                    } else {
                        longListener.valueUpdated(roundsWithData)
                    }
                }
            }
        }
        if (singleValueEvent) {
            roundsRef.addListenerForSingleValueEvent(valueEventListener)
        } else {
            roundsRef.addValueEventListener(valueEventListener)
        }
    }

    private fun getProfilePictureRefString(clubId: String, playerId: String): String {
        return Constants.LOCATION_PLAYER_MEDIA_PROFILE_PICTURE
            .replace(Constants.CLUB_KEY, clubId)
            .replace(Constants.PLAYER_KEY, playerId)
    }

    private fun getProfilePictureRef(clubId: String, playerId: String): DatabaseReference {
        val refString = getProfilePictureRefString(clubId, playerId)
        return FirebaseDatabase.getInstance().getReference(refString)
    }

    private fun getClubPlayerRefString(clubId: String, playerId: String): String {
        return Constants.LOCATION_CLUB_PLAYER
            .replace(Constants.CLUB_KEY, clubId)
            .replace(Constants.PLAYER_KEY, playerId)
    }

    private fun getDatabaseReference(absolutePath: String): DatabaseReference {
        return FirebaseDatabase.getInstance().getReference(absolutePath)
    }

    fun setDefaultPicture(clubId: String, playerId: String, pictureName: String): Picture {
        val profilePictureRefString = getProfilePictureRefString(clubId, playerId)
        val pictureUri = "$profilePictureRefString/$pictureName";
        val picture = Picture("defaultPicture", pictureUri)

        val defaultPictureRef = getProfilePictureRef(clubId, playerId)
        defaultPictureRef.setValue(picture)

        //update only the picture field
        val pictureFieldRefString = "${getClubPlayerRefString(clubId, playerId)}/profilePictureUri"
        val pictureFieldRef = getDatabaseReference(pictureFieldRefString)
        pictureFieldRef.setValue(pictureUri)

        return picture
    }


    fun setPlayerArchiveState(clubId: String, playerId: String, archiveState: Boolean) {
        val archiveRef = Locations.clubPlayerArchiveState(clubId, playerId)
        val db = getDatabaseReference(archiveRef)
        db.setValue(archiveState)
    }

    fun registerPlayerListener(
        singleValueEvent: Boolean,
        clubId: String,
        playerId: String,
        playerListener: PlayerListener
    ) {
        val valueEventListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                // nothing to implement
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.value != null) {
                    val player = dataSnapshot.getValue(Player::class.java)
                    playerListener.valueUpdated(player!!)
                }
            }
        }
        val playerRef = getClubPlayerRefString(clubId, playerId)
        val db = getDatabaseReference(playerRef)
        if (singleValueEvent) {
            db.addListenerForSingleValueEvent(valueEventListener)
        } else {
            db.addValueEventListener(valueEventListener)
        }
    }

    fun registerRoundHasGamesListener(
        singleValueEvent: Boolean,
        clubId: String,
        tournamentId: String,
        roundId: Int,
        boolListener: BoolListener
    ) {
        val valueEventListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                boolListener.boolValueChanged(false)
            }

            override fun onDataChange(p0: DataSnapshot) {
                val newList = ArrayList<Game>()
                for (item in p0.children) {
                    val game = item.getValue(Game::class.java)
                    newList.add(game!!)
                }
                if (newList.size > 0) {
                    boolListener.boolValueChanged(true)
                } else {
                    boolListener.boolValueChanged(false)
                }
            }

        }
        val gamesReference = buildGamesRef(clubId, tournamentId, roundId)
        if (singleValueEvent) {
            gamesReference.addListenerForSingleValueEvent(valueEventListener)
        } else {
            gamesReference.addValueEventListener(valueEventListener)
        }
    }

    fun registerRoundHasStandingsListener(
        singleValueEvent: Boolean,
        clubId: String,
        tournamentId: String,
        roundId: Int,
        boolListener: BoolListener
    ) {

        val valueEventListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                boolListener.boolValueChanged(false)
            }

            override fun onDataChange(p0: DataSnapshot) {
                val newList = ArrayList<RankedPlayer>()
                for (item in p0.children) {
                    val player = item.getValue(RankedPlayer::class.java)
                    newList.add(player!!)
                }
                if (newList.size > 0) {
                    boolListener.boolValueChanged(true)
                } else {
                    boolListener.boolValueChanged(false)
                }
            }
        }

        val standingsReference = buildStandingsRef(
            clubId, tournamentId, roundId,
            Constants.CATEGORY_DEFAULT
        )

        if (singleValueEvent) {
            standingsReference.addListenerForSingleValueEvent(valueEventListener)
        } else {
            standingsReference.addValueEventListener(valueEventListener)
        }
    }


    fun getClubPlayer(clubId: String, playerId: String): Player {
        val loc = getClubPlayerRefString(clubId, playerId);
        val ref = getDatabaseReference(loc)
        val data = ArrayList<Player>()

        val latch = CountDownLatch(1)
        val valueEventListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                latch.countDown()
            }

            override fun onDataChange(p0: DataSnapshot) {
                val player = p0.getValue(Player::class.java)
                data.add(player!!)
                latch.countDown()
            }
        }
        ref.addListenerForSingleValueEvent(valueEventListener)
        latch.await()
        return data[0]
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

    private fun getAllStandingsRef(clubId: String, tournamentId: String): String {

        val sectionNotRequired =
            "/${Constants.ROUND_NUMBER}/${Constants.STANDINGS}/${Constants.CATEGORY_NAME}" +
                    "/${Constants.STANDING_NUMBER}"

        return Constants.LOCATION_STANDINGS
            .replace(Constants.CLUB_KEY, clubId)
            .replace(Constants.TOURNAMENT_KEY, tournamentId)
            .replace(sectionNotRequired, "")

    }


    /**
     * Checks if all the games are played in the respective round and then computes and
     * updates the standings
     */
    fun computeAndUpdateStandings(clubId: String, tournamentId: String, roundId: Int) {
        if (!isCurrentUserAdmin(clubId)) {
            return
        }
        val tournament: ChesspairingTournament = buildChessPairingTournament(
            clubId, tournamentId
        )
        val vipRound = tournament.getRoundByRoundNumber(roundId)
        if (!vipRound.allGamesHaveBeanPlayed()) {
            return
        }
        val standings = tournament.computeStandings(roundId)

        val rankedPlayers = ArrayList<RankedPlayer>()
        var i = 0;

        standings.forEach {
            i++

            val clubPlayer = getClubPlayer(clubId, it.playerKey)
            val tournamentPlayer = tournament.getPlayer(it.playerKey)

            val player = RankedPlayer()
            player.playerKey = it.playerKey
            player.tournamentKey = tournamentId
            player.clubKey = clubId
            player.rankNumber = i
            player.tournamentInitialOrder = tournamentPlayer.initialOrderId
            player.buchholzPoints = tournament.computeBuchholzPoints(roundId, it.playerKey)
            player.points = tournament.computePoints(roundId, it.playerKey)
            player.elo = tournamentPlayer.elo
            player.playerName = it.name
            player.profilePictureUri = clubPlayer.profilePictureUri

            rankedPlayers.add(player)
        }

        rankedPlayers.forEach {
            val location = getStandingsRef(clubId, tournamentId, roundId, it.rankNumber)
            val standingRef = getDatabaseReference(location)
            standingRef.setValue(it)
        }
    }


    fun registerRoundsWithStandingsListener(
        singleValueEvent: Boolean,
        clubId: String,
        tournamentId: String,
        totalRounds: Long,
        longListener: LongListener
    ) {

        val valueEventListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                // nothing to implement
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var roundsWithStandings = dataSnapshot.childrenCount


                for (i in 1..totalRounds) {

                    val childRef =
                        "/${i.toString()}/${Constants.STANDINGS}/${Constants.CATEGORY_DEFAULT}"
                    if (dataSnapshot.hasChild(childRef)) {
                        roundsWithStandings = i
                    } else {
                        break
                    }
                }
                if (roundsWithStandings + 1 <= totalRounds) {
                    roundsWithStandings++
                }
                longListener.valueUpdated(roundsWithStandings)
            }

        }

        val ref = getAllStandingsRef(clubId, tournamentId)
        val dbRef = getDatabaseReference(ref)
        if (singleValueEvent) {
            dbRef.addListenerForSingleValueEvent(valueEventListener)
        } else {
            dbRef.addValueEventListener(valueEventListener)
        }

    }

    fun registerRoundHasStandingsListener(
        singleValueEvent: Boolean,
        ignoredParam: Boolean,
        clubId: String,
        tournamentId: String,
        roundId: Int,
        boolListener: BoolListener
    ) {
        val valueEventListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                boolListener.boolValueChanged(false)
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.value != null) {
                    boolListener.boolValueChanged(true)
                }
            }
        }

        val myRef = Constants.LOCATION_STANDINGS
            .replace(Constants.CLUB_KEY, clubId)
            .replace(Constants.TOURNAMENT_KEY, tournamentId)
            .replace(Constants.ROUND_NUMBER, roundId.toString())
            .replace(Constants.CATEGORY_NAME, Constants.CATEGORY_DEFAULT)
            .replace("/${Constants.STANDING_NUMBER}", "")

        val dbRef = getDatabaseReference(myRef)
        if (singleValueEvent) {
            dbRef.addListenerForSingleValueEvent(valueEventListener)
        } else {
            dbRef.addValueEventListener(valueEventListener)
        }
    }

    fun registerRoundStandingsListener(
        singleValueEvent: Boolean,
        clubId: String,
        tournamentId: String,
        roundId: Int,
        rankedPlayerListener: RankedPlayerListener
    ) {
        val playerList = ArrayList<RankedPlayer>()
        val valueEventListener = object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                playerList.clear()
                val it: Iterator<DataSnapshot> =
                    dataSnapshot.children.iterator()
                while (it.hasNext()) {
                    val player: RankedPlayer? = it.next().getValue(RankedPlayer::class.java)
                    playerList.add(player!!)
                }
                rankedPlayerListener.listUpdated(playerList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(Constants.LOG_TAG, databaseError.message)
            }
        }

        val myRef = Constants.LOCATION_STANDINGS
            .replace(Constants.CLUB_KEY, clubId)
            .replace(Constants.TOURNAMENT_KEY, tournamentId)
            .replace(Constants.ROUND_NUMBER, roundId.toString())
            .replace(Constants.CATEGORY_NAME, Constants.CATEGORY_DEFAULT)
            .replace("/${Constants.STANDING_NUMBER}", "")
        val dbRef = getDatabaseReference(myRef)
        if (singleValueEvent) {
            dbRef.addListenerForSingleValueEvent(valueEventListener)
        } else {
            dbRef.addValueEventListener(valueEventListener)
        }
    }

    fun createAndPersistPicture(clubId: String, extention: String): Picture {
        val pictureFolder = Locations.clubPictureFolder(clubId)
        val pictureRef = FirebaseDatabase.getInstance().getReference(pictureFolder).push()


        val pictureId = pictureRef.key
        var pictureName = "$pictureId"
        if (extention.isNotBlank()) {
            pictureName = "$pictureName.$extention"
        }
        val pictureUri = Locations.clubPictureUri(clubId, pictureId, pictureName)
        val pictureLocation = Locations.clubPictureLocation(clubId, pictureId)

        val picture = Picture(pictureId, pictureUri, pictureLocation)
        pictureRef.setValue(picture)
        return picture
    }

    fun createAndPersistPost(post: Post): Post {
        if (null == post.postType) {
            throw UnsupportedOperationException("Post type must not be null: ${post.postType}")
        }

        val postFolder: String = Locations.clubPostFolder(post.clubId)
        val postRef = FirebaseDatabase.getInstance().getReference(postFolder).push()
        post.postId = postRef.key
        postRef.setValue(post)
        updatePostReverseOrder(post)
        return post

    }

    fun deletePost(clubId: String, postId: String) {
        GlobalScope.async {
            Log.d(Constants.LOG_TAG, "Initiate DeletePost clubId=${clubId}, postId=${postId}")
            MyBackendUtil.notifyBackendClubPostDeleted(clubId, postId);
        }


    }

    fun registerPostsListener(
        singleValueEvent: Boolean,
        clubId: String,
        postsListener: PostsListener
    ) {
        val valueEventListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Log.e(Constants.LOG_TAG, p0.message)
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val posts = ArrayList<Post>()
                for (item in dataSnapshot.children) {
                    val post = item.getValue(Post::class.java)
                    posts.add(post!!)
                }
                postsListener.listUpdated(posts)
            }
        }

        val postsLocation = Locations.clubPostFolder(clubId)
        val db = FirebaseDatabase.getInstance().getReference(postsLocation)
            .orderByChild("reversedDateCreated")
            .limitToFirst(500)
        if (singleValueEvent) {
            db.addListenerForSingleValueEvent(valueEventListener)
        } else {
            db.addValueEventListener(valueEventListener)
        }
    }

    private fun updatePostReverseOrder(post: Post) {
        val postRef = Locations.clubPostLocation(post.clubId, post.postId)
        val db = FirebaseDatabase.getInstance().getReference(postRef)

        val valueEventListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Log.e(Constants.LOG_TAG, p0.message)
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val post: Post? = dataSnapshot.getValue(Post::class.java)
                if (post != null) {
                    val timeStamp: Long = post.dateCreatedStamp()
                    val reversedDateCreated = 0 - timeStamp
                    db.child("reversedDateCreated").setValue(reversedDateCreated)
                }
            }
        }

        db.addListenerForSingleValueEvent(valueEventListener)
    }

    private fun getContentType(displayName: String): String {
        val items = displayName.split(".")
        val extension = items[1].toLowerCase(Locale.getDefault())
        when (extension) {
            "jpg" -> return "image/jpeg"
            "png" -> return "image/png"
        }
        throw IllegalStateException("Not supported content type: $extension")
    }

    /**
     * It uploads a picture from the local uri to a bucket
     *
     * @SuppressLint was generating intellij errors for line
     * cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
     */
    @SuppressLint("Range")
    fun uploadPicture(
        picture: Picture, post: Post,
        index: Int, localUri: Uri, contentResolver: ContentResolver
    ) {
        val userId = MyFirebaseUtils.currentUserId()
        val storage = FirebaseStorage.getInstance()
        val cursor = contentResolver.query(
            localUri, null, null, null, null
        )
        cursor?.let {
            if (cursor.moveToFirst()) {
                val displayName =
                    cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                val metadata = storageMetadata {
                    contentType = getContentType(displayName)
                }
                cursor.close()
                val storageRef = storage.reference.child(picture.stringUri)
                val uploadTask = storageRef.putFile(localUri, metadata)
                uploadTask.addOnCompleteListener {
                    setPictureUploadComplete(post, index)
                    setPictureUploadCompleteInUserStreamPost(userId, post.postId, index)
                    GlobalScope.launch {
                        notifyTheBackendClubPostPictureUploadComplete(
                            post.clubId,
                            post.postId,
                            index
                        )
                    }
                }
            }
        }
    }


    fun storageReference(path: String): StorageReference {
        val storage = FirebaseStorage.getInstance()
        return storage.reference.child(path)
    }

    private fun setPictureUploadComplete(post: Post, index: Int) {
        val booleanLoc = Locations.pictureInPostLocation(post, index)
        val db = FirebaseDatabase.getInstance().getReference(booleanLoc)
        db.setValue(true)
    }

    fun registerUserDefaultPictureListener(
        singleValueEvent: Boolean,
        userId: String,
        pictureListener: PictureListener
    ) {
        val valueEventListener = object : ValueEventListener {
            override fun onCancelled(e: DatabaseError) {
                throw IllegalStateException("Problems with database: ${e.message}")
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val picture = dataSnapshot.getValue(Picture::class.java)
                    if (picture != null) {
                        pictureListener.valueUpdated(picture)
                    } else {
                        pictureListener.valueUpdated(null)
                    }
                } else {
                    pictureListener.valueUpdated(null)
                }
            }
        }
        val userProfilePictureLocation = Locations.userProfilePicture(userId)
        val db = getDatabaseReference(userProfilePictureLocation)
        if (singleValueEvent) {
            db.addListenerForSingleValueEvent(valueEventListener)
        } else {
            db.addValueEventListener(valueEventListener)
        }
    }


    fun getCurrentUserId(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }

    fun getDefaultCurrentUserPictureUrl(): String? {
        val mFirebaseAuth = FirebaseAuth.getInstance()
        val mFirebaseUser = mFirebaseAuth.currentUser
        if (null != mFirebaseUser) {
            return mFirebaseUser.photoUrl.toString()
        } else {
            return null
        }
    }

    fun getDefaultCurrentUserDisplayName(): String? {
        return FirebaseAuth.getInstance().currentUser?.displayName
    }

    fun setUserDisplayName(userId: String?, displayName: String?) {
        val location = Locations.userDisplayName(userId)
        val db = getDatabaseReference(location)
        db.setValue(displayName)
    }

    fun getUserDisplayName(userId: String): String {
        var displayName = ""
        val latch = CountDownLatch(1)
        val valueEventListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                throw IllegalStateException("Database problems: ${p0.message}")
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    displayName = dataSnapshot.getValue(String::class.java)!!
                }
                latch.countDown()
            }
        }
        val location = Locations.userDisplayName(userId)
        val db = getDatabaseReference(location)
        db.addListenerForSingleValueEvent(valueEventListener)
        latch.await()
        return displayName
    }

    fun getUserProfilePictureUri(userId: String): String? {
        var pictureUri = ""
        val latch = CountDownLatch(1)
        val valueEventListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                throw IllegalStateException("Database problems: ${p0.message}")
                latch.countDown()
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    pictureUri = dataSnapshot.getValue(String::class.java)!!
                }
                latch.countDown()
            }
        }
        val pictureLocation = Locations.userProfilePicture(userId)
        val pictureUriLocation = "$pictureLocation/stringUri"
        val db = getDatabaseReference(pictureUriLocation)
        db.addListenerForSingleValueEvent(valueEventListener)
        latch.await()
        return pictureUri
    }

    fun setUserProfilePicture(userId: String?, picture: Picture) {
        val location = Locations.userProfilePicture(userId)
        val db = getDatabaseReference(location)
        db.setValue(picture)
    }

    fun persistNewUserDefaultPicture(
        userId: String,
        extension: String,
        metadata: StorageMetadata,
        localUri: Uri
    ) {

        val pictureFolder = Locations.userProfilePictureFolder(userId)
        val pictureRef = getDatabaseReference(pictureFolder).push()

        val picture = Picture()
        picture.pictureId = pictureRef.key
        picture.location = Locations.userProfilePictureItem(userId, picture.pictureId)

        val storage = FirebaseStorage.getInstance()
        val fileName = picture.pictureId + ".$extension"
        val driveUri = picture.location + "/$fileName"
        picture.stringUri = driveUri
        pictureRef.setValue(picture)

        val driveRef = storage.reference.child(picture.stringUri)

        val uploadTask = driveRef.putFile(localUri, metadata)
        uploadTask.addOnCompleteListener {
            val defaultLoc = Locations.userProfilePicture(userId)
            val db = getDatabaseReference(defaultLoc)
            picture.isUploadComplete = true
            db.setValue(picture)
        }
    }

    fun registerClubListener(
        singleValueEvent: Boolean,
        clubId: String,
        clubListener: ClubListener
    ) {
        val valueEventListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                throw IllegalStateException("Database error: ${p0.message}")
            }

            override fun onDataChange(p0: DataSnapshot) {
                val club = p0.getValue(Club::class.java)
                if (null != club) {
                    clubListener.onClubValue(club)
                }
            }
        }

        val clubLocation = Locations.club(clubId)
        val db = getDatabaseReference(clubLocation)
        if (singleValueEvent) {
            db.addListenerForSingleValueEvent(valueEventListener)
        } else {
            db.addValueEventListener(valueEventListener)
        }
    }

    fun getClubPublicInfo(clubId: String?): Club {
        var club: Club? = null
        val latch = CountDownLatch(1)
        val valueEventListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                throw IllegalStateException("Database error: ${p0.message}")
            }

            override fun onDataChange(p0: DataSnapshot) {
                val clubValue = p0.getValue(Club::class.java)
                if (null != clubValue) {
                    club = clubValue
                }
                latch.countDown()
            }
        }
        val location = Locations.club(clubId)
        val db = getDatabaseReference(location)
        db.addListenerForSingleValueEvent(valueEventListener)
        latch.await()
        return club!!
    }

    fun persistClubDefaultPicture(
        clubId: String, extension: String, metadata: StorageMetadata, localUri: Uri
    ) {
        val pictureFolder = Locations.clubPictureFolder(clubId);
        val pictureRef = getDatabaseReference(pictureFolder).push()

        val picture = Picture()
        picture.pictureId = pictureRef.key
        picture.location = Locations.clubPictureLocation(clubId, picture.pictureId)

        val storage = FirebaseStorage.getInstance()
        val fileName = picture.pictureId + ".$extension"
        val driveUri = picture.location + "/$fileName"
        picture.stringUri = driveUri
        pictureRef.setValue(picture)

        val driveRef = storage.reference.child(picture.stringUri)
        val uploadTask = driveRef.putFile(localUri, metadata)
        uploadTask.addOnCompleteListener {
            val defaultLoc: String = Locations.clubProfilePicture(clubId)
            val db = getDatabaseReference(defaultLoc)
            picture.isUploadComplete = true
            db.setValue(picture)
        }
    }

    fun updateClub(club: Club) {
        val clubLocation = Locations.club(club.clubId)
        val db = getDatabaseReference(clubLocation)
        db.setValue(club)
    }

    fun processLikeRequest(post: Post) {

        val userInfo = UserInfo()
        userInfo.userId = getCurrentUserId()
        userInfo.userName = post.userName
        userInfo.pictureUri = post.userPictureUrl

        val likes: List<UserInfo> = getLikes(post)
        val byUserLikes = likes.filter {
            it.userId == userInfo.userId
        }

        val likeLoc = Locations.like(userInfo.userId, post.clubId, post.postId)
        val db = getDatabaseReference(likeLoc)
        if (byUserLikes.isNotEmpty()) {
            db.removeValue()
        } else {
            //create new like
            db.setValue(userInfo)
        }
    }

    private fun getLikes(post: Post): List<UserInfo> {
        val likes = ArrayList<UserInfo>()
        val latch = CountDownLatch(1)
        val valueEventListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                throw IllegalStateException("Database problems: ${p0.message}")
            }

            override fun onDataChange(p0: DataSnapshot) {
                for (item in p0.children) {
                    val info = item.getValue(UserInfo::class.java)
                    likes.add(info!!)
                }
                latch.countDown()
            }
        }
        val loc = Locations.likeFolder(post.clubId, post.postId)
        val db = getDatabaseReference(loc)
        db.addListenerForSingleValueEvent(valueEventListener)
        latch.await()
        return likes;
    }

    fun registerLikesCountListener(
        singleValueEvent: Boolean,
        valueEventListener: ValueEventListener,
        clubId: String,
        postId: String
    ) {
        val likesFolder = Locations.likeFolder(clubId, postId)
        val db = getDatabaseReference(likesFolder)
        if (singleValueEvent) {
            db.addListenerForSingleValueEvent(valueEventListener)
        } else {
            db.addValueEventListener(valueEventListener)
        }
    }

    fun registerTournamentListener(
        singleValueEvent: Boolean,
        clubId: String,
        tournamentId: String,
        tournamentListener: TournamentListener
    ) {
        val location = Locations.tournament(clubId, tournamentId)
        val db = getDatabaseReference(location)
        val valueEventListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                throw java.lang.IllegalStateException("Database problems: ${p0.message}")
            }

            override fun onDataChange(p0: DataSnapshot) {
                val tournament = p0.getValue(Tournament::class.java)
                if (null != tournament) {
                    tournamentListener.onTournamentValue(tournament)
                }
            }

        }
        if (singleValueEvent) {
            db.addListenerForSingleValueEvent(valueEventListener)
        } else {
            db.addValueEventListener(valueEventListener)
        }
    }


}