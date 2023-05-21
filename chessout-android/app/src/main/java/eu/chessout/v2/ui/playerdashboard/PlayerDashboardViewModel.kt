package eu.chessout.v2.ui.playerdashboard

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.UploadTask
import eu.chessout.shared.Constants
import eu.chessout.shared.Locations
import eu.chessout.shared.model.FollowPlayerSettings
import eu.chessout.shared.model.Player
import eu.chessout.v2.util.MyFirebaseUtils
import eu.chessout.v2.util.listeners.FirebaseQueryLiveData
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class PlayerDashboardViewModel : ViewModel() {

    companion object {
        private var showAdminToastValue = true
    }

    private lateinit var clubId: String
    private lateinit var playerId: String
    var userId: String? = null
    val defaultPictureUri = MutableLiveData<String?>()
    val isAdmin = MutableLiveData<Boolean>(false)
    val showArchive = MutableLiveData<Boolean>(false);
    val showActivate = MutableLiveData<Boolean>(false);
    private val myFirebaseUtils = MyFirebaseUtils()
    val player = MutableLiveData<Player?>()
    lateinit var followPlayerSettings: FirebaseQueryLiveData<FollowPlayerSettings>

    fun initModel(clubId: String, playerId: String) {
        this.clubId = clubId
        this.playerId = playerId
        this.userId = MyFirebaseUtils.currentUserId()

        registerPlayerListener()
        GlobalScope.launch {
            val newIsAdmin = myFirebaseUtils.isCurrentUserAdmin(clubId)
            isAdmin.postValue(newIsAdmin)
            if (newIsAdmin) {
                adminRegisterArchiveListener()
            }
        }

        followPlayerSettings = getFollowPlayerSettingsLiveData()
    }

    private fun getFollowPlayerSettingsLiveData(): FirebaseQueryLiveData<FollowPlayerSettings> {
        val converter = object : FirebaseQueryLiveData.MyConverter<FollowPlayerSettings> {
            override fun getValue(dataSnapshot: DataSnapshot): FollowPlayerSettings {
                val followSettingsValue = dataSnapshot.getValue(FollowPlayerSettings::class.java)
                if (null == followSettingsValue) {
                    Log.d(Constants.LOG_TAG, "Null followSettings")
                    return FollowPlayerSettings()
                } else {

                    Log.d(Constants.LOG_TAG, "Valid followSettings")
                    return followSettingsValue
                }
            }
        }
        val loc = Locations.followPlayerSettings(playerId, userId)
        val db = FirebaseDatabase.getInstance().getReference(loc)
        return FirebaseQueryLiveData(db, converter)
    }

    fun setDefaultPicture(uploadTask: UploadTask, pictureName: String) {
        uploadTask.addOnProgressListener { taskSnapshot ->
            val progress = (100.0 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount
            Log.d(Constants.LOG_TAG, "Upload is $progress% done")
        }.addOnPausedListener {
            println("Upload is paused")
        }.addOnCompleteListener {
            persisDefaultPicture(pictureName)
            Log.d(Constants.LOG_TAG, "Upload complete")
        }
    }

    fun setArchived(archived: Boolean) {
        GlobalScope.launch {
            myFirebaseUtils.setPlayerArchiveState(clubId, playerId, archived)
        }
    }

    private fun persisDefaultPicture(pictureName: String) {
        myFirebaseUtils.setDefaultPicture(clubId, playerId, pictureName)
    }

    private fun registerPlayerListener() {
        class PlayerListener : MyFirebaseUtils.PlayerListener {
            override fun valueUpdated(value: Player) {
                player.value = value
                defaultPictureUri.value = value.profilePictureUri
            }
        }
        myFirebaseUtils.registerPlayerListener(false, clubId, playerId, PlayerListener())
    }

    /**
     * It registers player listener responsible only for activate and archive member buttons
     * depending on the state. Should only be called if the user is admin
     */
    private fun adminRegisterArchiveListener() {
        class PlayerListener : MyFirebaseUtils.PlayerListener {
            override fun valueUpdated(value: Player) {
                if (value.isArchived) {
                    showActivate.value = true
                    showArchive.value = false
                } else {
                    showActivate.value = false
                    showArchive.value = true
                }
            }

        }
        myFirebaseUtils.registerPlayerListener(false, clubId, playerId, PlayerListener())
    }

    fun sendNotificationsChanged(checked: Boolean) {

        var settings = FollowPlayerSettings()
        if (null != followPlayerSettings) {
            settings = followPlayerSettings.value!!
        }
        settings.isSendNotificationWhenGameResultIsUpdated = checked
        settings.userId = userId

        MyFirebaseUtils.userUpdateFollowPlayerSettings(playerId, userId, settings)
        MyFirebaseUtils.addToFollowedPlayers(userId!!, playerId, player.value!!)

        val playerValue: Player = player.value!!
        if (playerValue.fideId != null) {
            MyFirebaseUtils.userUpdateFollowFideIdSettings(playerValue.fideId, userId!!, settings);
        }
    }

}
