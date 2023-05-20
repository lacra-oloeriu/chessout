package eu.chessout.v2.ui.tournament.tournaments.rounds.players

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.firebase.database.FirebaseDatabase
import eu.chessout.shared.Constants
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class RoundDeleteGamesDialog(
    val clubId: String,
    val tournamentId: String,
    val roundId: Int
) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)
        val builder = AlertDialog.Builder(requireActivity())
        builder.setTitle("Are you sure that you want to delete current games from this round?")
        builder.setNegativeButton("Cancel") { _, _ -> dismiss() }
        builder.setPositiveButton("Delete games") { _, _ ->
            deleteGames()
            dismiss()
        }
        return builder.create()
    }

    private fun deleteGames() {
        GlobalScope.launch {
            // delete current round
            val gamesLoc = Constants.LOCATION_ROUND_GAMES
                .replace(Constants.CLUB_KEY, clubId)
                .replace(Constants.TOURNAMENT_KEY, tournamentId)
                .replace(Constants.ROUND_NUMBER, roundId.toString())
            FirebaseDatabase.getInstance().getReference(gamesLoc).removeValue()
        }
    }
}