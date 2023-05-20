package eu.chessout.v2.ui.chat

import android.text.Spannable
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import eu.chessout.shared.model.ChatItem
import eu.chessout.v2.R
import java.util.*

class ChatAdapter(private var chatItems: ArrayList<ChatItem>) :
    RecyclerView.Adapter<ChatAdapter.ItemHolder>() {

    companion object {
        private const val USER_ITEM = 100
        private const val CURRENT_USER_ITEM = 101
    }

    private lateinit var currentUserId: String


    class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val chatSimpleLine: TextView = itemView.findViewById(R.id.chatSimpleLine)

        // specific other user
        val otherUserName: TextView? = itemView.findViewById(R.id.otherUserName)
    }

    override fun getItemViewType(position: Int): Int {
        val chatItem = chatItems[position]
        return if (chatItem.userId == currentUserId) {
            CURRENT_USER_ITEM
        } else {
            USER_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val view: View = when (viewType) {
            USER_ITEM -> {
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.chat_otheruser_item, parent, false)
            }
            CURRENT_USER_ITEM -> {
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.chat_currentuser_item, parent, false)
            }
            else -> throw NotImplementedError("Not supported type: $viewType")
        }
        return ItemHolder(view)
    }

    override fun getItemCount(): Int {
        return chatItems.size
    }

    private fun getFormatedType(chatItem: ChatItem): String {
        val calendar = Calendar.getInstance()
        calendar.time = Date(chatItem.timeStampCreate)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val intMinutes = calendar.get(Calendar.MINUTE)
        var stringMinutes = "$intMinutes"
        if (intMinutes < 10) {
            stringMinutes = "0$intMinutes"
        }
        val time = "$hour:$stringMinutes"
        return time
    }

    private fun getMessageSpannableString(chatItem: ChatItem, time: String): SpannableString {
        val displayText = "${chatItem.textValue} ( $time )"
        val spannableString = SpannableString(displayText)
        spannableString.setSpan(
            RelativeSizeSpan(0.6F),
            chatItem.textValue.length + 1,
            displayText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return spannableString
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {

        val chatItem: ChatItem = chatItems[position]
        val time = getFormatedType(chatItem)
        val spannableString = getMessageSpannableString(chatItem, time)

        if (chatItem.itemType == ChatItem.ItemType.TEXT) {
            holder.chatSimpleLine.text = spannableString
        }

        if (currentUserId != chatItem.userId) {
            if (chatItem.showOtherUserSection) {
                holder.otherUserName?.text = chatItem.userName
                holder.otherUserName?.visibility = View.VISIBLE
            } else {
                holder.otherUserName?.visibility = View.GONE
            }
        }

    }

    fun updateList(newList: List<ChatItem>) {
        chatItems.clear()
        chatItems.addAll(newList)
        notifyDataSetChanged()
    }

    public fun initLateItems(currentUserId: String) {
        this.currentUserId = currentUserId;
    }
}