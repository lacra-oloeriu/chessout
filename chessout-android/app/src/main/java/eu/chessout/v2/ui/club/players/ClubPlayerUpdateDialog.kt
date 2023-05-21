package eu.chessout.v2.ui.club.players

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.firebase.database.FirebaseDatabase
import eu.chessout.shared.Constants
import eu.chessout.shared.model.Player
import eu.chessout.v2.R

class ClubPlayerUpdateDialog(private val player: Player) :
    DialogFragment() {

    private lateinit var mView: View


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)

        val builder = AlertDialog.Builder(requireActivity())
        val inflater = requireActivity().layoutInflater

        mView = inflater.inflate(R.layout.club_player_create_dialog, null)
        builder.setView(mView)

        val name =
            (mView.findViewById<View>(R.id.profileName) as EditText)
        name.setText(player.name)
        val email =
            (mView.findViewById<View>(R.id.email) as EditText)
        email.setText(player.email)
        val eloString =
            (mView.findViewById<View>(R.id.elo) as EditText)
        eloString.setText(player.elo.toString())
        val fideId =
            (mView.findViewById<View>(R.id.fideId) as EditText)
        fideId.setText(player.fideId)
        val thirdPartyId =
            (mView.findViewById<View>(R.id.thirdPartyId) as EditText)
        thirdPartyId.setText(player.thirdPartyKey)

        builder.setNegativeButton("Cancel") { _, _ ->
            dismiss()
        }

        builder.setPositiveButton("Update player") { _, _ ->
            updatePlayer()
        }

        return builder.create()
    }

    private fun updatePlayer() {
        val updatePlayer = buildPlayer()
        if (null != updatePlayer.name) {
            player.name = updatePlayer.name
        }
        if (null != updatePlayer.email) {
            player.email = updatePlayer.email
        }
        if (0 != updatePlayer.elo) {
            player.elo = updatePlayer.elo
        }
        if (null != updatePlayer.thirdPartyKey) {
            player.thirdPartyKey = updatePlayer.thirdPartyKey
        }
        if (null != updatePlayer.fideId) {
            player.fideId = updatePlayer.fideId
        }

        val database =
            FirebaseDatabase.getInstance()

        val playersLoc: String = Constants.LOCATION_CLUB_PLAYERS
            .replace(Constants.CLUB_KEY, player.clubKey) + "/${player.playerKey}"
        val dbRef =
            database.getReference(playersLoc)

        dbRef.setValue(player)
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
        val fideId =
            (mView.findViewById<View>(R.id.fideId) as EditText).text.toString()
        val thirdPartyKey =
            (mView.findViewById<View>(R.id.thirdPartyId) as EditText).text.toString()


        val clubElo = 0
        val player = Player(name, email, player.clubKey, elo, clubElo)
        player.thirdPartyKey = thirdPartyKey
        player.fideId = fideId
        return player
    }
}