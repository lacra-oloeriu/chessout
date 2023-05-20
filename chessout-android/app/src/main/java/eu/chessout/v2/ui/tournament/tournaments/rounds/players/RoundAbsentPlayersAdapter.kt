package eu.chessout.v2.ui.tournament.tournaments.rounds.players

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import eu.chessout.shared.model.Player
import eu.chessout.v2.R

class RoundAbsentPlayersAdapter(
    var playerList: ArrayList<Player>
) :
    RecyclerView.Adapter<RoundAbsentPlayersAdapter.ItemHolder>() {


    private lateinit var fragmentManager: FragmentManager
    private var isAdmin = false
    private lateinit var clubId: String
    private lateinit var tournamentId: String
    private var roundId: Int = 0

    class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textView = itemView.findViewById<TextView>(R.id.list_item_text_simple_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val itemHolder = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_text, parent, false)
        return ItemHolder(itemHolder)
    }

    override fun getItemCount(): Int {
        return playerList.size
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        val player: Player = playerList[position]
        holder.textView.text = player.name
        holder.textView.setOnClickListener {
            if (isAdmin) {
                RoundRemoveAbsentPlayerDialog(
                    clubId, tournamentId, roundId, player
                )
                    .show(fragmentManager, "RoundRemoveAbsentPlayerDialog")
            }
        }
    }

    fun updateList(newList: List<Player>) {
        playerList.clear()
        playerList.addAll(newList)
        notifyDataSetChanged()
    }

    fun setContextValues(
        fragmentManager: FragmentManager,
        clubId: String, tournamentId: String, roundId: Int
    ) {
        this.fragmentManager = fragmentManager
        this.clubId = clubId
        this.tournamentId = tournamentId
        this.roundId = roundId
    }

    fun setIsAdmin(isAdmin: Boolean) {
        this.isAdmin = isAdmin
    }
}