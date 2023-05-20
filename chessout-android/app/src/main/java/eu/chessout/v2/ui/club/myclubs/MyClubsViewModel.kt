package eu.chessout.v2.ui.club.myclubs

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import eu.chessout.shared.Constants
import eu.chessout.shared.Constants.CLUB_KEY
import eu.chessout.shared.model.Club
import eu.chessout.shared.model.DefaultClub
import eu.chessout.v2.util.MyFirebaseUtils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MyClubsViewModel : ViewModel() {

    val liveDefaultClubId = MutableLiveData("no-default-club")
    val liveClubList = MutableLiveData<List<Club>>()
    val myFirebaseUtils = MyFirebaseUtils()


    fun initializeModel() {
        class DefaultClubListener : MyFirebaseUtils.DefaultClubListener {
            override fun onDefaultClubValue(defaultClub: DefaultClub) {
                liveDefaultClubId.value = defaultClub.clubKey
            }

            override fun setDbRef(databaseReference: DatabaseReference) {
                //TODO("Not yet implemented")
            }

            override fun setDbListener(valueEventListener: ValueEventListener) {
                //TODO("Not yet implemented")
            }
        }

        MyFirebaseUtils().getDefaultClubListener(DefaultClubListener())
        registerMyClubsListener()
    }

    private fun registerMyClubsListener() {
        val mAuth = FirebaseAuth.getInstance()
        val mUser = mAuth.currentUser!!

        val myClubsLocation: String = Constants.LOCATION_MY_CLUBS
            .replace(Constants.USER_KEY, mUser.uid)
            .replace("/$CLUB_KEY", "") // exclude MY_CLUBS

        val mClubsQuery = FirebaseDatabase.getInstance().reference
            .child(myClubsLocation).orderByKey()

        mClubsQuery.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                //nothing to do user just stopped listening
            }

            override fun onDataChange(p0: DataSnapshot) {
                val iterator = p0.children.iterator()
                val newList = mutableListOf<Club>()
                for (item in iterator) {
                    val club = item.getValue(Club::class.java)
                    club?.let {
                        newList.add(it)
                    }
                }
                liveClubList.value = newList
                newList.forEach { updateMyClubIfNecessary(it) }
            }
        })
    }

    private fun updateMyClubIfNecessary(myClub: Club) {
        GlobalScope.launch {
            val club = MyFirebaseUtils.getClub(myClub.clubId)
            if (myClub != club) {
                val userId = myFirebaseUtils.getCurrentUserId()
                myFirebaseUtils.addToMyClubs(userId!!, club!!.clubId, club)
            }
        }
    }

}
