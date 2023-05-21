package eu.chessout.v2.ui.club.clubdashboard

import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import eu.chessout.shared.Locations
import eu.chessout.shared.model.Club
import eu.chessout.shared.model.ClubSettings
import eu.chessout.shared.model.DefaultClub
import eu.chessout.v2.util.MyFirebaseUtils
import eu.chessout.v2.util.SharedPreferencesHelper
import eu.chessout.v2.util.listeners.FirebaseQueryLiveData
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ClubDashboardViewModel : ViewModel() {
    lateinit var clubId: String
    var userId: String? = null
    val isAdmin = MutableLiveData(false)
    val club = MutableLiveData<Club?>()
    val myFirebaseUtils = MyFirebaseUtils()
    val isDefaultClub = MutableLiveData(false)
    lateinit var clubSettings: FirebaseQueryLiveData<ClubSettings>

    fun initModel(clubId: String) {
        this.clubId = clubId
        this.userId = MyFirebaseUtils.currentUserId()
        GlobalScope.launch {
            isAdmin.postValue(myFirebaseUtils.isCurrentUserAdmin(clubId))
        }
        registerClubListener()
        myFirebaseUtils.registerDefaultClubListener(defaultClubListener, true)
        clubSettings = getClubSettingsLiveData()
    }

    private fun getClubSettingsLiveData(): FirebaseQueryLiveData<ClubSettings> {
        val converter = object : FirebaseQueryLiveData.MyConverter<ClubSettings> {
            override fun getValue(dataSnapshot: DataSnapshot): ClubSettings {
                val settingsValue = dataSnapshot.getValue(ClubSettings::class.java)
                if (null == settingsValue) {
                    return ClubSettings()
                } else {
                    return settingsValue
                }
            }
        }
        val loc = Locations.clubSettings(clubId, userId)
        val db = FirebaseDatabase.getInstance().getReference(loc)
        return FirebaseQueryLiveData(db, converter)
    }

    private fun registerClubListener() {
        class ClubListener : MyFirebaseUtils.ClubListener {
            override fun onClubValue(clubValue: Club) {
                club.value = clubValue
            }
        }
        myFirebaseUtils.registerClubListener(false, clubId, ClubListener())
    }

    fun setCurrentAsDefaultClub(prefEditor: SharedPreferences.Editor) {
        val defaultClub = DefaultClub(club.value?.clubId, club.value?.name)
        MyFirebaseUtils().setDefaultClub(defaultClub)
        SharedPreferencesHelper.setDefaultClub(prefEditor, defaultClub.clubKey)
        isDefaultClub.value = true
    }

    val defaultClubListener = object : MyFirebaseUtils.DefaultClubListener {
        override fun onDefaultClubValue(defaultClub: DefaultClub) {
            if (clubId == defaultClub.clubKey) {
                isDefaultClub.value = true
            }
        }

        override fun setDbRef(databaseReference: DatabaseReference) {
            // nothing to do
        }

        override fun setDbListener(valueEventListener: ValueEventListener) {
            // nothing to do
        }
    }

    fun showPostsChanged(checked: Boolean) {
        var settings = ClubSettings()
        if (null != clubSettings) {
            settings = clubSettings.value!!
        }
        settings.isShowPosts = checked
        MyFirebaseUtils.userUpdateClubSettings(clubId, userId, settings)
    }
}
