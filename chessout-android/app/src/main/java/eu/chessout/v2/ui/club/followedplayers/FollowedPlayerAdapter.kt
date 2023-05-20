package eu.chessout.v2.ui.club.followedplayers

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.storage.FirebaseStorage
import eu.chessout.shared.model.Player
import eu.chessout.v2.R
import eu.chessout.v2.util.GlideApp

class FollowedPlayerAdapter(var playerList: MutableList<Player>) :
    RecyclerView.Adapter<FollowedPlayerAdapter.ItemHolder>() {

    private val storage = FirebaseStorage.getInstance()
    private lateinit var context: Context
    private lateinit var fragmentManager: FragmentManager

    class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textView = itemView.findViewById<TextView>(R.id.list_item_text_simple_view)
        val profilePicture = itemView.findViewById<ImageView>(R.id.profilePicture)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val itemHolder = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_club_player, parent, false)
        return ItemHolder(itemHolder)
    }

    override fun getItemCount(): Int {
        return playerList.size
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        val player: Player = playerList[position]
        val onClickListener = View.OnClickListener {
            val action =
                FollowedPlayersFragmentDirections
                    .actionFollowedPlayersFragmentToPlayerDashboardFragment(
                        player.clubKey, player.playerKey
                    )
            holder.textView.findNavController().navigate(action)
        }

        holder.textView.text = player.name
        holder.textView.setOnClickListener(onClickListener)
        holder.profilePicture.setOnClickListener(onClickListener)

        player.profilePictureUri?.let {
            val pictureReference = storage.reference.child(it)
            GlideApp.with(context)
                .load(pictureReference)
                .apply(RequestOptions().circleCrop())
                .into(holder.profilePicture)
        }
    }

    fun updateList(newList: List<Player>) {
        playerList.clear()
        playerList.addAll(newList)
        notifyDataSetChanged()
    }

    fun setFragmentManager(fragmentManager: FragmentManager) {
        this.fragmentManager = fragmentManager
    }

    fun setContext(context: Context) {
        this.context = context
    }

}