package eu.chessout.v2.ui.tournament.tournaments.rounds.players

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.firebase.database.FirebaseDatabase
import eu.chessout.shared.Constants
import eu.chessout.shared.model.Player

class RoundRemoveAbsentPlayerDialog(
    val clubId: String,
    val tournamentId: String,
    val roundId: Int,
    val player: Player
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)
        val builder = AlertDialog.Builder(requireActivity())
        builder.setTitle("Remove ${player.name} from the absent list?")
        builder.setNegativeButton("Cancel") { _, _ -> dismiss() }
        builder.setPositiveButton("Yes") { _, _ -> removeAbsentPlayer() }
        return builder.create()
    }

    private fun removeAbsentPlayer() {
        val playerLoc = Constants.LOCATION_ROUND_ABSENT_PLAYERS
            .replace(Constants.CLUB_KEY, clubId)
            .replace(Constants.TOURNAMENT_KEY, tournamentId)
            .replace(Constants.ROUND_NUMBER, roundId.toString()) + "/" + player.playerKey
        val playerRef =
            FirebaseDatabase.getInstance().getReference(playerLoc)
        playerRef.removeValue()
        dismiss()
    }
}