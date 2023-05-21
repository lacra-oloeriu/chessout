package eu.chessout.v2.ui.dashboard02

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import eu.chessout.shared.Constants
import eu.chessout.shared.model.Club
import eu.chessout.shared.model.DefaultClub
import eu.chessout.shared.model.Picture
import eu.chessout.v2.util.MyFirebaseUtils
import kotlin.concurrent.thread


class Dashboard02ViewModel : ViewModel() {

    companion object {
        private const val LOG_TAG = Constants.LOG_TAG
    }

    private var dataInitialized = false
    private val _text = MutableLiveData<String>().apply {
        value = "This is detail 2 fragment"
    }

    private val _showItem = MutableLiveData<Boolean>().apply {
        value = true
    }

    private val _countValue = MutableLiveData<Int>().apply {
        value = 0;
    }

    private val _myClubCreated = MutableLiveData<Boolean>().apply {
        value = true
    }

    private val _isInDebugMode = MutableLiveData<Boolean>().apply {
        value = Constants.IS_IN_DEBUG_MODE
    }

    var isInternalPhoto = false
    val photoUrl = MutableLiveData<String?>()

    val text: LiveData<String> = _text
    val countValue: LiveData<Int> = _countValue
    val myClubCreated: LiveData<Boolean> = _myClubCreated
    val myClub = MutableLiveData<Club?>()
    val defaultClubExists = MutableLiveData<Boolean>(false)
    val clubId = MutableLiveData("noId")
    val isAdmin = MutableLiveData(false)
    val userId = MutableLiveData<String?>()
    val showArchivedPlayers = MutableLiveData(false)
    val isInDebugMode: LiveData<Boolean> = _isInDebugMode
    var db: DatabaseReference? = null
    var dbListener: ValueEventListener? = null
    private val myFirebaseUtils = MyFirebaseUtils()


    fun initializeCount() {
        thread() {
            while (countValue.value!! < 100) {
                _countValue.postValue(countValue.value!! + 1)
                Thread.sleep(1000)
            }
        }
    }

    fun initializeData() {
        if (!dataInitialized) {
            dataInitialized = true;
            checkClubCreated()
        }

        class DefaultClubListener : MyFirebaseUtils.DefaultClubListener {
            override fun onDefaultClubValue(defaultClub: DefaultClub) {
                defaultClubExists.value = true
            }

            override fun setDbRef(databaseReference: DatabaseReference) {
                //TODO("Not yet implemented")
            }

            override fun setDbListener(valueEventListener: ValueEventListener) {
                //TODO("Not yet implemented")
            }
        }
        this.userId.value = MyFirebaseUtils().getCurrentUserId()
        MyFirebaseUtils().getDefaultClubSingleValueListener(DefaultClubListener())
        registerDefaultClubListener()
        registerUserProfilePhotoListener()
    }

    private fun registerUserProfilePhotoListener() {
        class PictureListener : MyFirebaseUtils.PictureListener {
            override fun valueUpdated(pictureValue: Picture?) {
                if (null == pictureValue) {
                    getDefaultPicture()
                } else {
                    if (pictureValue.isUploadComplete) {
                        isInternalPhoto = true
                        photoUrl.value = pictureValue.stringUri
                    } else {
                        getDefaultPicture()
                    }
                }
            }

            private fun getDefaultPicture() {
                val mFirebaseAuth = FirebaseAuth.getInstance()
                val mFirebaseUser = mFirebaseAuth.currentUser
                if (null != mFirebaseUser) {
                    photoUrl.value = mFirebaseUser.photoUrl.toString()
                }
            }
        }

        MyFirebaseUtils().registerUserDefaultPictureListener(
            false,
            this.userId.value!!,
            PictureListener()
        )
    }

    private fun registerDefaultClubListener() {
        class IsAdminListener : MyFirebaseUtils.IsAdminListener {
            override fun onIsAdmin(isAdminValue: Boolean) {
                isAdmin.value = isAdminValue
                showArchivedPlayers.value = isAdminValue
            }
        }

        class DefaultClubLister : MyFirebaseUtils.DefaultClubListener {
            override fun onDefaultClubValue(defaultClub: DefaultClub) {
                clubId.value = defaultClub.clubKey
                myFirebaseUtils.registerIsAdminListener(
                    true,
                    defaultClub.clubKey,
                    IsAdminListener()
                )
            }

            override fun setDbRef(databaseReference: DatabaseReference) {
                db = databaseReference
            }

            override fun setDbListener(valueEventListener: ValueEventListener) {
                dbListener = valueEventListener
            }
        }
        MyFirebaseUtils().registerDefaultClubListener(DefaultClubLister(), false)
    }

    private fun checkClubCreated() {
        // database references
        val database =
            FirebaseDatabase.getInstance()
        val uid =
            FirebaseAuth.getInstance().currentUser!!.uid

        val myClubLocation: String = Constants.LOCATION_L_MY_CLUB
            .replace(Constants.USER_KEY, uid)
        val myClubRef =
            database.getReference(myClubLocation)

        // listener
        val clubListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                _myClubCreated.value = dataSnapshot.exists()
                if (dataSnapshot.exists() && (null == myClub.value)) {
                    val clubId = dataSnapshot.child("clubKey").getValue(String::class.java)
                    clubId?.let { registerClubListener(clubId) }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // user canceled listening on this node
            }
        }
        myClubRef.addValueEventListener(clubListener)
    }

    fun removeEventListener() {
        if (null != db && null != dbListener) {
            db?.removeEventListener(dbListener!!)
        }
    }

    private fun registerClubListener(clubId: String) {
        class ClubListener : MyFirebaseUtils.ClubListener {
            override fun onClubValue(club: Club) {
                myClub.value = club
            }
        }
        myFirebaseUtils.registerClubListener(true, clubId, ClubListener())
    }

}
