package eu.chessout.v2.util

import com.google.firebase.database.FirebaseDatabase
import eu.chessout.shared.Locations
import eu.chessout.shared.model.ChatItem

class MyChatFirebaseUtils {
    companion object {
        fun persistChat(chat: ChatItem) {
            val chatFolder = Locations.chatFolder(chat)
            val chatRef = FirebaseDatabase.getInstance().getReference(chatFolder).push()
            chat.chatId = chatRef.key
            val valueRef = chatRef.setValue(chat)
        }
    }
}