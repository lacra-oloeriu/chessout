package eu.chessout.v2.ui.userdashboard

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import eu.chessout.shared.Constants
import eu.chessout.shared.model.Picture
import eu.chessout.v2.util.MyFirebaseUtils

class UserDashboardViewModel : ViewModel() {
    // TODO: Implement the ViewModel
    lateinit var userId: String
    var currentUserId: String? = null
    val sameUser = MutableLiveData(false)
    val isUploaded = MutableLiveData(false)
    val pictureUrl = MutableLiveData<String?>()
    val displayName = MutableLiveData("My profile")
    val displayEmail = MutableLiveData("My email")

    fun initModel(userId: String) {
        this.userId = userId
        this.currentUserId = MyFirebaseUtils().getCurrentUserId()
        registerPictureListener()
        registerDisplayNameListener()
        registerUserEmailListener()
        sameUser.value = userId == currentUserId
    }

    private fun registerDisplayNameListener() {
        class StringListener : MyFirebaseUtils.StringListener {
            override fun valueUpdated(value: String?) {
                value?.let {
                    displayName.value = value
                }
            }
        }
        MyFirebaseUtils.registerUserDisplayNameListener(
            false, userId,
            StringListener()
        )
    }

    private fun registerUserEmailListener() {
        class StringListener : MyFirebaseUtils.StringListener {
            override fun valueUpdated(value: String?) {
                value?.let {
                    Log.d(Constants.LOG_TAG, "Email value=$value")
                    displayEmail.value = value
                }
            }
        }
        MyFirebaseUtils.registerUserEmailListener(
            false, userId,
            StringListener()
        )
    }

    private fun registerPictureListener() {
        class PictureListener : MyFirebaseUtils.PictureListener {
            override fun valueUpdated(value: Picture?) {
                if (null != value) {
                    isUploaded.value = value.isUploadComplete
                    pictureUrl.value = value.stringUri
                } else {
                    getDefaultPicture()
                }
            }

            private fun getDefaultPicture() {
                val mFirebaseAuth = FirebaseAuth.getInstance()
                val mFirebaseUser = mFirebaseAuth.currentUser
                if (null != mFirebaseUser) {
                    pictureUrl.value = mFirebaseUser.photoUrl.toString()
                }
            }
        }
        MyFirebaseUtils().registerUserDefaultPictureListener(
            false, userId, PictureListener()
        )
    }
}
