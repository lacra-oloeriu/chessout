package eu.chessout.v2.ui.club.joinclubbyqrcode

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import eu.chessout.shared.Constants
import eu.chessout.shared.model.Club
import eu.chessout.shared.model.ClubSettings
import eu.chessout.shared.model.DefaultClub
import eu.chessout.v2.util.MyFirebaseUtils
import eu.chessout.v2.util.SharedPreferencesHelper
import kotlinx.coroutines.GlobalScope
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

class JoinClubByQrCodeViewModel : ViewModel() {
    val clubLocated = MutableLiveData(false)
    val working = AtomicBoolean(false)

    lateinit var prefEditor: SharedPreferences.Editor

    fun initModel(prefEditor: SharedPreferences.Editor) {
        this.prefEditor = prefEditor
    }

    fun checkClub(clubId: String) {
        GlobalScope.let {
            if (working.compareAndSet(false, true)) {
                val club = MyFirebaseUtils.getClub(clubId)
                if (null == club) {
                    working.set(false)
                } else {
                    joinAndSetDefaultClub(club)
                    showPosts(club, true)
                    clubLocated.postValue(true)
                }
            } else {
                Log.d(Constants.LOG_TAG, "Skip check $clubId ${Date().time}")
            }
        }
    }

    private fun joinAndSetDefaultClub(club: Club) {
        val database =
            FirebaseDatabase.getInstance()
        val auth =
            FirebaseAuth.getInstance()
        val firebaseUser = auth.currentUser
        val userId = firebaseUser!!.uid

        val firebaseUtils = MyFirebaseUtils()
        firebaseUtils.addToMyClubs(userId, club.clubId, club)

        val defaultClub = DefaultClub(club.clubId, club.name)
        firebaseUtils.setDefaultClub(defaultClub)
        SharedPreferencesHelper.setDefaultClub(prefEditor, defaultClub.clubKey)
    }

    private fun showPosts(club: Club, showValue: Boolean) {
        val userId = MyFirebaseUtils.currentUserId()
        val setting = ClubSettings()
        setting.isShowPosts = showValue
        MyFirebaseUtils.userUpdateClubSettings(club.clubId, userId, setting)
    }

}
