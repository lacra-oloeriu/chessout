package eu.chessout.v2.ui.tournament.tournaments.players

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.storage.FirebaseStorage
import eu.chessout.shared.model.Player
import eu.chessout.shared.model.RankedPlayer
import eu.chessout.v2.R
import eu.chessout.v2.util.GlideApp
import eu.chessout.v2.util.listeners.FirebaseQueryLiveData

class TournamentPlayersAdapter(private val playerList: ArrayList<RankedPlayer>) :
    RecyclerView.Adapter<TournamentPlayersAdapter.ItemHolder>() {


    private lateinit var context: Context
    private lateinit var fragmentManager: FragmentManager
    private lateinit var clubId: String
    private lateinit var tournamentId: String
    private lateinit var liveClubPlayersMap: HashMap<String, FirebaseQueryLiveData<Player>>
    private lateinit var lifecycleOwner: LifecycleOwner
    private val storage = FirebaseStorage.getInstance()


    class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textView = itemView.findViewById<TextView>(R.id.list_item_text_simple_view)
        var tournamentOrder = itemView.findViewById<TextView>(R.id.tournamentOrder)
        val profilePicture = itemView.findViewById<ImageView>(R.id.profilePicture)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val itemHolder = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_tournament_player, parent, false)
        return ItemHolder(itemHolder)
    }

    override fun getItemCount(): Int {
        return playerList.size
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        val player: RankedPlayer = playerList[position]
        holder.textView.text = "${player.playerName} (${player.elo})"
        holder.tournamentOrder.text = "${player.tournamentInitialOrder.toString()}."
        setImage(holder.profilePicture, player)
        holder.profilePicture.setOnClickListener { getOnClickListener(player) }
    }

    fun updateList(newList: List<RankedPlayer>) {
        playerList.clear()
        playerList.addAll(newList)
        notifyDataSetChanged()
    }

    fun setContextValues(
        context: Context, fragmentManager: FragmentManager,
        clubId: String, tournamentId: String,
        liveClubPlayersMap: HashMap<String, FirebaseQueryLiveData<Player>>,
        lifecycleOwner: LifecycleOwner
    ) {
        this.context = context
        this.fragmentManager = fragmentManager
        this.clubId = clubId
        this.tournamentId = tournamentId
        this.liveClubPlayersMap = liveClubPlayersMap
        this.lifecycleOwner = lifecycleOwner
    }

    private fun getOnClickListener(rankedPlayer: RankedPlayer) {
        RemovePlayerFromTournamentDialog(clubId, tournamentId, rankedPlayer)
            .show(this.fragmentManager, "RemovePlayerFromTournamentDialog")
    }

    private fun setImage(imageView: ImageView, player: RankedPlayer) {
        liveClubPlayersMap[player.playerKey]?.let { mapLivePlayer ->
            mapLivePlayer.observe(lifecycleOwner, Observer {
                if (null != it.profilePictureUri) {
                    val pictureReference = storage.reference.child(
                        it.profilePictureUri
                    )
                    GlideApp.with(context)
                        .load(pictureReference)
                        .apply(RequestOptions().circleCrop())
                        .into(imageView)
                } else {
                    GlideApp.with(context)
                        .load(R.drawable.ic_user_circle_regular_gray)
                        .into(imageView)
                }
            })
        }
    }

}