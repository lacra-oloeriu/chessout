package eu.chessout.v2.ui.tournament.tournaments.addplayers

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import eu.chessout.shared.Constants
import eu.chessout.shared.model.Player
import eu.chessout.v2.R
import eu.chessout.v2.util.MyFirebaseUtils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

class TournamentAddPlayersDialog(
    val clubId: String,
    val tournamentId: String,
    val players: List<Player>
) :
    DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)
        val builder = AlertDialog.Builder(requireActivity())
        builder.setTitle("Select player to add")

        val nameArray: Array<String> = players.map { p -> p.name }.toTypedArray()
        builder.setItems(nameArray, DialogInterface.OnClickListener { _, witch ->
            addPlayer(players[witch])
        })

        builder.setNegativeButton("Cancel") { _, _ ->
            dismiss()
        }

        return builder.create()
    }

    internal class PlayerAdapter(context: Context?, players: List<Player?>?) :
        ArrayAdapter<Player?>(context!!, 0, players!!) {
        override fun getView(
            position: Int,
            convertView: View?,
            parent: ViewGroup
        ): View {
            var convertView = convertView
            val player = getItem(position)
            if (convertView == null) {
                convertView = LayoutInflater.from(context)
                    .inflate(R.layout.list_item_text, parent, false)
            }
            val listItem =
                convertView!!.findViewById<View>(R.id.list_item_text_simple_view) as TextView
            listItem.text = player!!.name
            return convertView
        }
    }

    private fun addPlayer(player: Player) {
        val tournamentPlayerLoc: String = Constants.LOCATION_TOURNAMENT_PLAYERS
            .replace(Constants.CLUB_KEY, clubId)
            .replace(Constants.TOURNAMENT_KEY, tournamentId) +
                "/" + player.playerKey
        val playerRef =
            FirebaseDatabase.getInstance()
                .getReference(tournamentPlayerLoc)
        playerRef.addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.value == null) {
                    playerRef.setValue(player)
                    GlobalScope.async {
                        MyFirebaseUtils().refreshTournamentInitialOrder(clubId, tournamentId)
                    }
                }
                dialog?.dismiss()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(tag, "Firebase error: " + databaseError.message)
            }
        })
    }
}