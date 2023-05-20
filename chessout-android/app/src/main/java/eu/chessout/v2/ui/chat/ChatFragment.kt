package eu.chessout.v2.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import eu.chessout.shared.model.ChatItem
import eu.chessout.v2.R
import kotlinx.android.synthetic.main.fragment_chat.*

class ChatFragment : Fragment() {

    private val args: ChatFragmentArgs by navArgs()
    private val chatViewModel: ChatViewModel by viewModels()
    private val chatAdapter = ChatAdapter(arrayListOf())

    private lateinit var mLayoutManager: LinearLayoutManager;
    private lateinit var recycler: RecyclerView
    private val chatObserver = Observer<List<ChatItem>> {
        chatAdapter.updateList(it)
        mLayoutManager.smoothScrollToPosition(recycler, null, it.size)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val mView = inflater.inflate(R.layout.fragment_chat, container, false)
        val locationType = ChatItem.LocationType.valueOf(args.type)
        chatViewModel.initializeModel(
            locationType, args.clubId, args.tournamentId, args.roundId,
            args.postId
        )
        chatAdapter.initLateItems(chatViewModel.currentUserId)
        chatViewModel.liveChatItems.observe(viewLifecycleOwner, chatObserver)

        recycler = mView.findViewById<RecyclerView>(R.id.chatRecycler)
        recycler.apply {
            mLayoutManager = LinearLayoutManager(context)
            mLayoutManager.stackFromEnd = true

            layoutManager = mLayoutManager
            adapter = chatAdapter
        }

        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sendView.setOnClickListener {
            val message = chatText.text.toString()
            chatText.text.clear()
            chatViewModel.persistTextMessage(message)
        }
    }
}