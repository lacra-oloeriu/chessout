package eu.chessout.v2.ui.club.clubfollowers

import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import eu.chessout.shared.Constants
import eu.chessout.shared.Locations
import eu.chessout.shared.model.UserInfo
import eu.chessout.v2.util.listeners.FirebaseQueryLiveData

class ClubFollowersViewModel : ViewModel() {

    private lateinit var clubId: String
    lateinit var liveUserIdList: FirebaseQueryLiveData<List<String>>

    val liveUserMapInfo: MutableMap<String, FirebaseQueryLiveData<UserInfo>> = mutableMapOf();


    fun initializeModel(clubId: String) {
        this.clubId = clubId

        this.liveUserIdList = getLiveUserInfoList(this.clubId)
    }


    private fun getLiveUserInfoList(clubId: String): FirebaseQueryLiveData<List<String>> {
        val converter = object : FirebaseQueryLiveData.MyConverter<List<String>> {
            override fun getValue(dataSnapshot: DataSnapshot): List<String> {
                val list = ArrayList<String>()
                for (item in dataSnapshot.children) {
                    item.key?.let {
                        list.add(it)
                        if (!liveUserMapInfo.containsKey(it)) {
                            liveUserMapInfo[it] = getUserInfo(it)
                        }
                    }
                }
                return list
            }
        }
        val loc = Locations.clubUsersSettings(clubId)
        val db = FirebaseDatabase.getInstance().getReference(loc)
        return FirebaseQueryLiveData(db, converter)
    }

    private fun getUserInfo(userId: String): FirebaseQueryLiveData<UserInfo> {
        val converter = object : FirebaseQueryLiveData.MyConverter<UserInfo> {
            override fun getValue(dataSnapshot: DataSnapshot): UserInfo {
                val userInfo = UserInfo()
                userInfo.userId = userId
                if (dataSnapshot.hasChild(Constants.DISPLAY_NAME)) {
                    userInfo.userName = dataSnapshot.child(Constants.DISPLAY_NAME).value.toString()
                }
                if (dataSnapshot.hasChild("${Constants.PROFILE_PICTURE}/stringUri")) {
                    userInfo.pictureUri =
                        dataSnapshot.child("${Constants.PROFILE_PICTURE}/stringUri")
                            .value.toString()
                }
                return userInfo
            }
        }
        val loc = Locations.userPublicInfo(userId)
        val db = FirebaseDatabase.getInstance().getReference(loc)
        return FirebaseQueryLiveData(db, converter)
    }
}