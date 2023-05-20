package eu.chessout.v2.ui.tournament.tournaments.standings.state

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.storage.FirebaseStorage
import eu.chessout.shared.model.RankedPlayer
import eu.chessout.v2.R
import eu.chessout.v2.util.GlideApp

class StandingRankedPlayerAdapter(var playerList: ArrayList<RankedPlayer>) :
    RecyclerView.Adapter<StandingRankedPlayerAdapter.ItemHolder>() {


    private lateinit var context: Context
    private val storage = FirebaseStorage.getInstance()

    class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var playerName = itemView.findViewById<TextView>(R.id.playerName)
        val profilePicture = itemView.findViewById<ImageView>(R.id.profilePicture)
        val rankNumber = itemView.findViewById<TextView>(R.id.rankNumber)
        val startingNumber = itemView.findViewById<TextView>(R.id.startingNumber)
        val ratingPoints = itemView.findViewById<TextView>(R.id.ratingPoints)
        val tournamentPoints = itemView.findViewById<TextView>(R.id.tournamentPoints)
        val buchholzPoints = itemView.findViewById<TextView>(R.id.buchholzPoints)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val itemHolder = LayoutInflater.from(parent.context)
            .inflate(R.layout.standings_players_item_layout, parent, false)
        return ItemHolder(itemHolder)
    }

    override fun getItemCount(): Int {
        return playerList.size
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        val player: RankedPlayer = playerList[position]
        holder.playerName.text = player.playerName
        player.profilePictureUri?.let {
            val pictureReference = storage.reference.child(it)
            GlideApp.with(context)
                .load(pictureReference)
                .apply(RequestOptions().circleCrop())
                .into(holder.profilePicture)
        }
        holder.rankNumber.text = player.rankNumber.toString()
        holder.startingNumber.text = player.tournamentInitialOrder.toString()
        holder.ratingPoints.text = player.elo.toString()
        holder.tournamentPoints.text = player.points.toString()
        holder.buchholzPoints.text = player.buchholzPoints.toString()
    }

    fun updateList(newList: List<RankedPlayer>) {
        playerList.clear()
        playerList.addAll(newList)
        notifyDataSetChanged()
    }

    fun setContext(context: Context) {
        this.context = context
    }
}