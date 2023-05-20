package eu.chessout.v2.ui.club.players

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.*
import eu.chessout.shared.Constants
import eu.chessout.shared.model.DefaultClub
import eu.chessout.shared.model.Player
import eu.chessout.v2.util.MyFirebaseUtils
import java.util.*

class ClubPlayersViewModel : ViewModel() {
    var clubKey = MutableLiveData<String>().apply { value = "" }
    private lateinit var myFirebaseUtils: MyFirebaseUtils
    var isAdmin = MutableLiveData<Boolean>(false)
    val livePlayerList = MutableLiveData<List<Player>>()
    val filteredPlayerList = MutableLiveData<List<Player>>()
    private var archivedPlayers = false

    @RequiresApi(Build.VERSION_CODES.N)
    var filter = MutableLiveData<Optional<String>>(Optional.empty())

    fun getClubKey(): LiveData<String> {
        return clubKey
    }

    fun initializeModel(archivedPlayers: Boolean) {
        this.archivedPlayers = archivedPlayers
        myFirebaseUtils = MyFirebaseUtils()

        class ClubListener : MyFirebaseUtils.DefaultClubListener {
            override fun onDefaultClubValue(defaultClub: DefaultClub) {
                processDefaultClub(defaultClub)
                initializeList()
            }

            override fun setDbRef(databaseReference: DatabaseReference) {
                //TODO("Not yet implemented")
            }

            override fun setDbListener(valueEventListener: ValueEventListener) {
                //TODO("Not yet implemented")
            }
        }
        myFirebaseUtils.getDefaultClubSingleValueListener(ClubListener())
    }

    private fun processDefaultClub(defaultClub: DefaultClub) {
        clubKey.value = defaultClub.clubKey

        class IsAdminListener : MyFirebaseUtils.IsAdminListener {
            override fun onIsAdmin(returnAdmin: Boolean) {
                isAdmin.value = returnAdmin
            }
        }

        myFirebaseUtils.isCurrentUserAdmin(defaultClub.clubKey, IsAdminListener())
    }

    private fun initializeList() {
        val playersLoc: String = Constants.LOCATION_CLUB_PLAYERS
            .replace(Constants.CLUB_KEY, clubKey.value!!)
        val playersRef =
            FirebaseDatabase.getInstance().getReference(playersLoc)
                .orderByChild("archived")
                .equalTo(archivedPlayers)
        val eventListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                // nothing to do on cancel
            }

            @RequiresApi(Build.VERSION_CODES.N)
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val players: ArrayList<Player> = ArrayList()
                for (item in dataSnapshot.children) {
                    val player = item.getValue(Player::class.java)!!
                    players.add(player)
                }
                livePlayerList.value = players
                updateFilteredPlayers(filter.value!!, players)
            }
        }
        playersRef.addValueEventListener(eventListener)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun updateFilterValue(optionalText: Optional<String>) {
        filter.postValue(optionalText)
        updateFilteredPlayers(optionalText, livePlayerList.value!!)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun updateFilteredPlayers(optionalText: Optional<String>, allList: List<Player>) {

        if (!optionalText.isPresent) {
            filteredPlayerList.postValue(allList)
            return;
        } else {
            val filterTextValue = optionalText.get().toLowerCase();
            val filteredList = allList.filter { player ->
                val values: String = player.name.toLowerCase()
                values.contains(filterTextValue)
            }.toList()
            filteredPlayerList.postValue(filteredList);
        }
    }
}
