package eu.chessout.v2.ui.club.followedplayers

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import eu.chessout.shared.Constants
import eu.chessout.shared.Locations
import eu.chessout.shared.model.Player
import eu.chessout.v2.util.MyFirebaseUtils

class FollowedPlayersViewModel : ViewModel() {

    lateinit var userId: String
    val livePlayerList = MutableLiveData<List<Player>>()

    fun initializeModel() {
        userId = MyFirebaseUtils.getCurrentUserId()!!
        initLivePlayerList(userId)
    }

    private fun initLivePlayerList(userId: String) {
        val loc: String = Locations.followedPlayersFolder(userId);
        val ref = FirebaseDatabase.getInstance().getReference(loc)
        val eventListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Log.d(Constants.LOG_TAG, "init live players canceled")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val players: MutableList<Player> = mutableListOf()
                for (item in snapshot.children) {
                    val player = item.getValue(Player::class.java)!!
                    players.add(player)
                }
                livePlayerList.value = players
            }

        }
        ref.addValueEventListener(eventListener)
    }
}