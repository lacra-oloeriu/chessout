package eu.chessout.v2.ui.chat

import androidx.lifecycle.ViewModel
import com.google.common.base.Strings
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import eu.chessout.shared.Locations
import eu.chessout.shared.model.ChatItem
import eu.chessout.v2.util.MyChatFirebaseUtils
import eu.chessout.v2.util.MyFirebaseUtils
import eu.chessout.v2.util.listeners.FirebaseQueryLiveData
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class ChatViewModel : ViewModel() {

    lateinit var currentUserId: String
    lateinit var currentUserDisplayName: String
    lateinit var locationType: ChatItem.LocationType
    lateinit var clubId: String
    lateinit var tournamentId: String
    lateinit var roundId: String
    var postId: String? = null

    lateinit var liveChatItems: FirebaseQueryLiveData<List<ChatItem>>

    fun initializeModel(
        locationType: ChatItem.LocationType,
        clubId: String?,
        tournamentId: String?,
        roundId: String?,
        postId: String?
    ) {
        currentUserId = MyFirebaseUtils.getCurrentUserId()!!
        GlobalScope.launch {
            currentUserDisplayName = MyFirebaseUtils.getUserDisplayName(currentUserId)
        }

        this.locationType = locationType
        if (locationType == ChatItem.LocationType.CLUB_TOURNAMENT_PAIRINGS_AVAILABLE) {
            this.clubId = clubId!!
            this.tournamentId = tournamentId!!
            this.roundId = roundId!!

            liveChatItems = getChatClubTournamentPairingsAvailableLiveData(locationType)
        }

        if (locationType == ChatItem.LocationType.POST) {
            this.postId = postId
            liveChatItems = getChatPostLiveData(locationType)
        }

    }

    private fun getChatPostLiveData(locationType: ChatItem.LocationType): FirebaseQueryLiveData<List<ChatItem>> {
        val converter = getChatConverter()
        val genericChatItem = ChatItem()
        genericChatItem.locationType = locationType
        genericChatItem.postId = this.postId
        val loc = Locations.chatFolder(genericChatItem)
        val db = FirebaseDatabase.getInstance().getReference(loc)
            .limitToLast(1000)
        return FirebaseQueryLiveData(db, converter)
    }

    private fun getChatClubTournamentPairingsAvailableLiveData(locationType: ChatItem.LocationType): FirebaseQueryLiveData<List<ChatItem>> {
        val converter = getChatConverter()
        val genericChatItem = ChatItem.newGenericInstance(
            this.locationType, this.clubId, this.tournamentId,
            this.roundId, this.currentUserId
        )
        val loc = Locations.chatFolder(genericChatItem)
        val db = FirebaseDatabase.getInstance().getReference(loc)
            .limitToLast(1000)
        return FirebaseQueryLiveData(db, converter)
    }

    private fun getChatConverter(): FirebaseQueryLiveData.MyConverter<List<ChatItem>> {
        val converter = object : FirebaseQueryLiveData.MyConverter<List<ChatItem>> {
            override fun getValue(dataSnapshot: DataSnapshot): List<ChatItem> {
                val list = ArrayList<ChatItem>()
                var lastItemUserId: String? = null

                for (item in dataSnapshot.children) {
                    val chat = item.getValue(ChatItem::class.java)

                    // if previous item has the same user id the do not show the
                    // otherUserSection, otherwise do not show
                    chat!!.showOtherUserSection = lastItemUserId != chat.userId

                    lastItemUserId = chat.userId
                    list.add(chat)
                }
                return list
            }
        }
        return converter
    }

    fun persistTextMessage(message: String) {
        if (Strings.isNullOrEmpty(message)) {
            return
        }
        var chat: ChatItem? = null
        if (this.locationType == ChatItem.LocationType.CLUB_TOURNAMENT_PAIRINGS_AVAILABLE) {
            chat = ChatItem.newTextInstance(
                ChatItem.LocationType.CLUB_TOURNAMENT_PAIRINGS_AVAILABLE,
                this.clubId, this.tournamentId, this.roundId, ChatItem.ItemType.TEXT,
                this.currentUserId, this.currentUserDisplayName,
                message
            )
        }
        if (this.locationType == ChatItem.LocationType.POST) {
            chat = ChatItem()
            chat.locationType = locationType
            chat.itemType = ChatItem.ItemType.TEXT
            chat.timeStampCreate = Date().time
            chat.timeStampEdit = Date().time

            chat.postId = this.postId
            chat.userId = this.currentUserId
            chat.userName = this.currentUserDisplayName
            chat.textValue = message
        }


        GlobalScope.launch {
            MyChatFirebaseUtils.persistChat(chat!!)
        }

    }
}