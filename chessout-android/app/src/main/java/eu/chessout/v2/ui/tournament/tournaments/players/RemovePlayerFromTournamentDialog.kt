package eu.chessout.v2.ui.tournament.tournaments.players

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.firebase.database.FirebaseDatabase
import eu.chessout.shared.Constants
import eu.chessout.shared.model.RankedPlayer
import eu.chessout.v2.util.MyFirebaseUtils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

class RemovePlayerFromTournamentDialog(
    private val clubId: String,
    private val tournamentId: String,
    private val rankedPlayer: RankedPlayer
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        builder.setMessage("Remove ${rankedPlayer.playerName} from the tournament?")
        builder.setNegativeButton("Cancel") { _, _ -> dismiss() }
        builder.setPositiveButton("Remove") { _, _ -> removePlayer() }
        return builder.create()
    }

    private fun removePlayer() {
        Toast.makeText(
            requireContext(), "Time to remove ${rankedPlayer.playerName}",
            Toast.LENGTH_SHORT
        ).show()

        val tournamentPlayerLoc: String = Constants.LOCATION_TOURNAMENT_PLAYERS
            .replace(Constants.CLUB_KEY, clubId)
            .replace(Constants.TOURNAMENT_KEY, tournamentId) +
                "/" + rankedPlayer.playerKey
        GlobalScope.async {
            FirebaseDatabase.getInstance()
                .getReference(tournamentPlayerLoc).removeValue();
            MyFirebaseUtils().refreshTournamentInitialOrder(clubId, tournamentId)
        }
    }
}