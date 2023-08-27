package eu.chessout.v2.ui.tournament.tournaments.rounds.games

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.storage.FirebaseStorage
import eu.chessout.shared.Constants
import eu.chessout.shared.model.Game
import eu.chessout.shared.model.Player
import eu.chessout.v2.R
import eu.chessout.v2.util.GlideApp
import eu.chessout.v2.util.listeners.FirebaseQueryLiveData

class RoundGamesAdapter(val gameList: MutableList<Game>) :
    RecyclerView.Adapter<RoundGamesAdapter.ItemHolder>() {

    private lateinit var clubId: String
    private lateinit var tournamentId: String
    private var roundId = -1
    private lateinit var fragmentManager: FragmentManager
    lateinit var context: Context
    private lateinit var livePlayersMap: HashMap<String, FirebaseQueryLiveData<Player>>
    private lateinit var lifecycleOwner: LifecycleOwner

    var isAdmin = false
    private val storage = FirebaseStorage.getInstance()

    class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tableNumber: TextView = itemView.findViewById(R.id.tableNumber)
        var gameResult: TextView = itemView.findViewById(R.id.gameResult)
        var whitePlayerPicture: ImageView = itemView.findViewById(R.id.whitePlayerPicture)
        var blackPlayerPicture: ImageView = itemView.findViewById(R.id.blackPlayerPicture)
        var whitePlayerName: TextView = itemView.findViewById(R.id.whitePlayerName)
        val blackPlayerName: TextView = itemView.findViewById(R.id.blackPlayerName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val itemHolder = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_round_game_v2, parent, false)
        return ItemHolder(itemHolder)
    }

    override fun getItemCount(): Int {
        return gameList.size
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        val model: Game = gameList[position]
        //set table number
        holder.tableNumber.text = "${model.actualNumber.toString()}. "

        val whitePlayer = model.whitePlayer
        val blackPlayer = model.blackPlayer
        holder.gameResult.text = formatResult(model.result)

        holder.whitePlayerName.text = whitePlayer.name
        if (blackPlayer != null) {
            holder.blackPlayerName.text = blackPlayer.name
        } else {
            holder.blackPlayerName.text = ""
        }



        setClickListeners(holder, model)
        setImages(holder, model)
    }

    private fun getOnClickListener(model: Game): View.OnClickListener {
        return View.OnClickListener {
            if (isAdmin && (null != model.blackPlayer)) {
                if (model.result == 0) {
                    RoundSetGameResultDialog(clubId, tournamentId, roundId, model).show(
                        this.fragmentManager,
                        "RoundSetGameResultDialog"
                    )
                } else {
                    Toast.makeText(
                        this.context,
                        "Use long click if you intend to update results",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Log.d(Constants.LOG_TAG, "You are not an admin")
            }
        }
    }

    private fun getOnLongClickListener(model: Game): View.OnLongClickListener {
        return View.OnLongClickListener {
            if (isAdmin && (null != model.blackPlayer)) {
                RoundSetGameResultDialog(clubId, tournamentId, roundId, model).show(
                    this.fragmentManager,
                    "RoundSetGameResultDialog"
                )
            }
            true
        }
    }

    private fun setClickListeners(holder: ItemHolder, game: Game) {
        val viewItems = ArrayList<View>()
        viewItems.add(holder.tableNumber)
        viewItems.add(holder.gameResult)
        viewItems.add(holder.whitePlayerPicture)
        viewItems.add(holder.blackPlayerPicture)
        val onClickListener = getOnClickListener(game)
        val onLongClickList = getOnLongClickListener(game)

        viewItems.forEach {
            it.setOnClickListener(onClickListener)
            it.setOnLongClickListener(onLongClickList)
        }
    }

    private fun setImages(holder: ItemHolder, game: Game) {
        setImage(holder.whitePlayerPicture, game.whitePlayer)
        if (null != game.blackPlayer) {
            holder.blackPlayerPicture.visibility = View.VISIBLE
            setImage(holder.blackPlayerPicture, game.blackPlayer)
        } else {
            holder.blackPlayerPicture.visibility = View.GONE
        }
    }

    private fun setImage(imageVew: ImageView, player: Player) {
        livePlayersMap[player.playerKey]?.let { mapLivePlayer ->

            mapLivePlayer.observe(lifecycleOwner, Observer {
                if (null != it.profilePictureUri) {
                    val pictureReference = storage.reference.child(
                        it.profilePictureUri
                    )
                    GlideApp.with(context)
                        .load(pictureReference)
                        .apply(RequestOptions().circleCrop())
                        .into(imageVew)

                } else {
                    GlideApp.with(context)
                        .load(R.drawable.ic_user_circle_regular_gray)
                        .into(imageVew)
                }
            })
        }
    }

    private fun formatResult(result: Int): String? {
        var format: String? = null
        when (result) {
            0 -> format = "---"
            1 -> format = "1-0"
            2 -> format = "0-1"
            3 -> format = "1/2-1/2"
            4 -> format = "1(Bye)"
            5 -> format = "1ff-0"
            6 -> format = "0-1ff"
            7 -> format = "0ff-0ff"
            8 -> format = "1/2ff-1/2ff"
        }
        return format
    }

    fun updateList(newList: List<Game>) {
        gameList.clear()
        gameList.addAll(newList)
        notifyDataSetChanged()
    }

    fun setIds(clubId: String, tournamentId: String, roundId: Int) {
        this.clubId = clubId
        this.tournamentId = tournamentId
        this.roundId = roundId
    }

    fun setFragmentManger(fragmentManager: FragmentManager) {
        this.fragmentManager = fragmentManager
    }

    fun setLivePlayersMap(livePlayersMap: HashMap<String, FirebaseQueryLiveData<Player>>) {
        this.livePlayersMap = livePlayersMap
    }

    public fun setLifeCycleOwner(lifecycleOwner: LifecycleOwner) {
        this.lifecycleOwner = lifecycleOwner
    }

}