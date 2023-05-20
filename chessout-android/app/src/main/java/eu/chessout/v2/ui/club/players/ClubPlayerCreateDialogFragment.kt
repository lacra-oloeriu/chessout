package eu.chessout.v2.ui.club.players

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import eu.chessout.shared.model.Player
import eu.chessout.v2.R
import eu.chessout.v2.util.MyFirebaseUtils

class ClubPlayerCreateDialogFragment(var mClubKey: String) : DialogFragment() {

    private lateinit var mView: View

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)

        val builder = AlertDialog.Builder(requireActivity())
        val inflater = requireActivity().layoutInflater

        mView = inflater.inflate(R.layout.club_player_create_dialog, null)
        builder.setView(mView)

        builder.setNegativeButton("Cancel") { _, _ ->
            dismiss()
        }

        builder.setPositiveButton("Create player") { _, _ ->
            val player = buildPlayer();
            MyFirebaseUtils.persistPlayer(player)
        }

        return builder.create()
    }


    private fun buildPlayer(): Player {
        val name =
            (mView.findViewById<View>(R.id.profileName) as EditText).text
                .toString()
        val email =
            (mView.findViewById<View>(R.id.email) as EditText).text.toString()
        val eloString =
            (mView.findViewById<View>(R.id.elo) as EditText).text.toString()
        val elo = Integer.valueOf(eloString)

        val clubElo = 0
        return Player(name, email, mClubKey, elo, clubElo)
    }
}