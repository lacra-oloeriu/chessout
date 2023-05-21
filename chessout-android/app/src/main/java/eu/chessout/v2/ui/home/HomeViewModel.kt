package eu.chessout.v2.ui.home

import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import eu.chessout.shared.Locations
import eu.chessout.shared.model.*
import eu.chessout.v2.util.MyFirebaseUtils
import eu.chessout.v2.util.listeners.FirebaseQueryLiveData

class HomeViewModel : ViewModel() {
    data class CompletedGames(val total: Int, val completed: Int)

    lateinit var livePostList: FirebaseQueryLiveData<List<Post>>
    val likesMap = HashMap<String, FirebaseQueryLiveData<Long>>()
    val userLikedMap = HashMap<String, FirebaseQueryLiveData<Boolean>>()
    val chatItemsMap = HashMap<String, FirebaseQueryLiveData<Long>>()
    val tournamentTotalPlayers = HashMap<String, FirebaseQueryLiveData<Long>>()
    val gamesCompletedMap = HashMap<String, FirebaseQueryLiveData<CompletedGames>>()

    //private lateinit var clubId: String
    private lateinit var myFirebaseUtils: MyFirebaseUtils
    private val userId = MyFirebaseUtils.currentUserId()

    fun initializeModel() {
        myFirebaseUtils = MyFirebaseUtils()

        registerCurrentUserListener()

        livePostList = getMyQueryLiveData(userId)
    }

    private fun getMyQueryLiveData(userId: String): FirebaseQueryLiveData<List<Post>> {
        val converter = object : FirebaseQueryLiveData.MyConverter<List<Post>> {
            override fun getValue(dataSnapshot: DataSnapshot): List<Post> {
                val list = ArrayList<Post>()
                for (item in dataSnapshot.children) {
                    val post = item.getValue(Post::class.java)
                    list.add(post!!)
                    if (!likesMap.containsKey(post.postId)) {
                        likesMap[post.postId] = getLikesCountLiveData(post.clubId, post.postId)
                    }
                    if (!userLikedMap.containsKey(post.postId)) {
                        userLikedMap[post.postId] = getUserLikesLiveData(
                            post.clubId, post.postId, userId
                        )
                    }
                    if (!chatItemsMap.containsKey(post.postId)) {
                        chatItemsMap[post.postId] = getChatItemsCountLiveData(
                            post.postId
                        )
                    }
                    if (post.postType == Post.PostType.TOURNAMENT_CREATED) {
                        tournamentTotalPlayers[post.postId] =
                            getTotalPlayersCountLiveData(post.clubId, post.tournamentId)
                    }
                    if (post.postType == Post.PostType.TOURNAMENT_PAIRINGS_AVAILABLE) {
                        gamesCompletedMap[post.postId] =
                            getCompletedGamesLiveData(post.clubId, post.tournamentId, post.roundId)
                    }
                }
                return list
            }
        }
        val loc = Locations.userStreamPostFolder(userId)
        val db = FirebaseDatabase.getInstance().getReference(loc)
            .orderByChild("reversedDateCreated")
            .limitToFirst(500)
        return FirebaseQueryLiveData(db, converter)
    }

    private fun getCompletedGamesLiveData(clubId: String, tournamentId: String, roundId: Int)
            : FirebaseQueryLiveData<CompletedGames> {
        val loc = Locations.tournamentGames(clubId, tournamentId, roundId.toString())
        val db = FirebaseDatabase.getInstance().getReference(loc)
        val converter = object : FirebaseQueryLiveData.MyConverter<CompletedGames> {
            override fun getValue(dataSnapshot: DataSnapshot): CompletedGames {
                val games = ArrayList<Game>()
                val notDecided = 0 // we store in database the result as ints
                // val bye = 4 // we store in database the results as ints
                for (item in dataSnapshot.children) {
                    val game = item.getValue(Game::class.java)
                    games.add(game!!)
                }

                val playedGames = games.filter {
                    it.result != notDecided
                }
                return CompletedGames(games.size, playedGames.size)
            }
        }
        return FirebaseQueryLiveData(db, converter)
    }

    private fun getTotalPlayersCountLiveData(
        clubId: String,
        tournamentId: String
    ): FirebaseQueryLiveData<Long> {
        val converter = object : FirebaseQueryLiveData.MyConverter<Long> {
            override fun getValue(dataSnapshot: DataSnapshot): Long {
                return dataSnapshot.childrenCount
            }
        }
        val loc = Locations.tournamentPlayers(clubId, tournamentId)
        val db = FirebaseDatabase.getInstance().getReference(loc)
        return FirebaseQueryLiveData(db, converter)
    }

    private fun getLikesCountLiveData(clubId: String, postId: String): FirebaseQueryLiveData<Long> {
        val converter = object : FirebaseQueryLiveData.MyConverter<Long> {
            override fun getValue(p0: DataSnapshot): Long {
                return p0.childrenCount
            }
        }
        val likesFolder = Locations.likeFolder(clubId, postId)
        val db = FirebaseDatabase.getInstance().getReference(likesFolder)
        return FirebaseQueryLiveData(db, converter)
    }

    private fun getChatItemsCountLiveData(postId: String): FirebaseQueryLiveData<Long> {
        val converter = object : FirebaseQueryLiveData.MyConverter<Long> {
            override fun getValue(p0: DataSnapshot): Long {
                return p0.childrenCount
            }
        }
        val chatItem = ChatItem()
        chatItem.locationType = ChatItem.LocationType.POST
        chatItem.postId = postId
        val likesFolder = Locations.chatFolder(chatItem)
        val db = FirebaseDatabase.getInstance().getReference(likesFolder)
        return FirebaseQueryLiveData(db, converter)
    }

    /**
     * It decides if the current user actually liked a specific post
     */
    private fun getUserLikesLiveData(
        clubId: String,
        postId: String,
        userId: String
    ): FirebaseQueryLiveData<Boolean> {
        val converter = object : FirebaseQueryLiveData.MyConverter<Boolean> {
            override fun getValue(p0: DataSnapshot): Boolean {
                val it: Iterator<DataSnapshot> = p0.children.iterator()
                for (item in it) {
                    val info = item.getValue(UserInfo::class.java)
                    if (userId == info?.userId) {
                        return true
                    }
                }
                return false
            }
        }

        val likesFolder = Locations.likeFolder(clubId, postId)
        val db = FirebaseDatabase.getInstance().getReference(likesFolder)
        return FirebaseQueryLiveData(db, converter)
    }


    private fun registerCurrentUserListener() {

        class StringListener : MyFirebaseUtils.StringListener {
            override fun valueUpdated(value: String?) {
                if (null == value) {
                    updateName()
                }
            }
        }

        val currentUserId = MyFirebaseUtils().getCurrentUserId()
        currentUserId?.let {
            MyFirebaseUtils.registerUserDisplayNameListener(
                false, currentUserId,
                StringListener()
            )
        }

    }

    private fun updateName() {
        val currentUserId = MyFirebaseUtils().getCurrentUserId()
        val currentUserDisplayName = MyFirebaseUtils().getDefaultCurrentUserDisplayName()
        MyFirebaseUtils().setUserDisplayName(currentUserId, currentUserDisplayName)
        val pictureUrl = MyFirebaseUtils().getDefaultCurrentUserPictureUrl()
        val picture = Picture()
        picture.pictureId = "noId"
        picture.stringUri = pictureUrl
        picture.location = "noLocation"
        MyFirebaseUtils().setUserProfilePicture(currentUserId, picture)
    }
}