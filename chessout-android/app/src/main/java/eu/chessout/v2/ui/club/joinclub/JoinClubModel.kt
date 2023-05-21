package eu.chessout.v2.ui.club.joinclub

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import eu.chessout.shared.Constants
import eu.chessout.shared.model.Club

class JoinClubModel() : ViewModel() {

    val liveClubs = MutableLiveData<List<Club>>()


    fun initializeModel() {


        val clubs: ArrayList<Club> = ArrayList()

        val clubsLoc: String = Constants.CLUBS
        val clubsRef =
            FirebaseDatabase.getInstance().getReference(clubsLoc)

        val eventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (item in dataSnapshot.children) {
                    val club = item.getValue(Club::class.java)!!
                    club.clubId = item.key
                    clubs.add(club)
                }
                liveClubs.value = clubs;
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(Constants.LOG_TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }

        clubsRef.addListenerForSingleValueEvent(eventListener)

    }

}