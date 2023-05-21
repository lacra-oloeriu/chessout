package eu.chessout.v2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.*
import com.google.firebase.messaging.FirebaseMessaging
import eu.chessout.shared.Constants
import eu.chessout.shared.model.Device
import eu.chessout.shared.model.User
import kotlinx.android.synthetic.main.activity_sign_in.*

private val TAG = "MainActivity"


//https://github.com/firebase/quickstart-android/blob/master/auth/app/src/main/java/com/google/firebase/quickstart/auth/kotlin/GoogleSignInActivity.kt
//old video: https://youtu.be/SXlidHy-Tb8
class SignInActivity : AppCompatActivity(), View.OnClickListener {

    // <declare_auth>
    private lateinit var auth: FirebaseAuth
    // </declare_auth>

    private lateinit var googleSignInClient: GoogleSignInClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContentView(R.layout.activity_sign_in)


        // Button listeners
        signInButton.setOnClickListener(this)
        signOutButton.setOnClickListener(this)
        disconnectButton.setOnClickListener(this)

        // [START config_signin]
        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        // [END config_signin]

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // [START initialize_auth]
        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        // [END initialize_auth]


        var timeToLogOut = savedInstanceState?.getBoolean("timeToLogOut")
        timeToLogOut?.let {
            signOut()
        }
    }


    // [START on_start_check_user]
    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        Log.d(TAG, "onStart -> $currentUser")

        var timeToLogOut = this.intent.extras?.getBoolean("timeToLogOut")
        timeToLogOut?.let {
            if (timeToLogOut) {
                signOut()
            }
        }
    }
    // [END on_start_check_user]

    // [START onactivityresult]
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
                // [START_EXCLUDE]
                updateUI(null)
                // [END_EXCLUDE]
            }
        }
    }
    // [END onactivityresult]


    // [START auth_with_google]
    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.id!!)
        // [START_EXCLUDE silent]
        // [END_EXCLUDE]

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    updateUI(user)
                    setUserInFirebaseHelper()
                    startActivity(Intent(this, MainActivity::class.java))
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Snackbar.make(main_layout, "Authentication Failed.", Snackbar.LENGTH_SHORT)
                        .show()
                    updateUI(null)
                }

                // [START_EXCLUDE]
                // [END_EXCLUDE]
            }
    }
    // [END auth_with_google]

    private fun setUserInFirebaseHelper() {
        val app = FirebaseApp.getInstance()
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        val auth = FirebaseAuth.getInstance()
        val firebaseUser = auth.currentUser
        val username = firebaseUser!!.displayName
        val email = firebaseUser.email
        val uid = firebaseUser.uid

        val userLocation: DatabaseReference = database.getReference(Constants.USERS).child(uid)

        userLocation.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) { //only set value if user does not exist
                if (dataSnapshot.getValue() == null) {
                    val timeStampJoined =
                        HashMap<String, Any>()
                    timeStampJoined[Constants.FIREBASE_PROPERTY_TIMESTAMP] = ServerValue.TIMESTAMP
                    val user = User(username, email)
                    userLocation.setValue(user)
                }
                registerDeviceInFirebase()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(
                    TAG,
                    "DatabaseError: " + databaseError.getMessage()
                )
            }
        })
    }

    @Suppress("DEPRECATION")
    private fun registerDeviceInFirebase() {
        val userKey = FirebaseAuth.getInstance().currentUser!!.uid
        //val deviceKey: String = FirebaseInstanceId.getInstance().token!!
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            // Log and toast
            val msg = "Token value = $token";
            Log.d(TAG, msg)
            Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
            val deviceKey = token;
            //Log.d(Constants.LOG_TAG, "Device token = $deviceKey")
            val deviceLoc: String = Constants.LOCATION_MY_DEVICE
                .replace(Constants.USER_KEY, userKey)
                .replace(Constants.DEVICE_KEY, deviceKey)
            val deviceRef = FirebaseDatabase.getInstance().getReference(deviceLoc)
            deviceRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) { //only set value if value device not registered yet
                    if (dataSnapshot.value == null) {
                        val device = Device(deviceKey, Device.DeviceType.ANDROID)
                        deviceRef.setValue(device)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e(
                        TAG,
                        "DatabaseError: " + databaseError.message
                    )
                }
            })
        })
    }


    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            status.text = user.email
            detail.text = user.uid

            signInButton.visibility = View.GONE
        } else {
            detail.text = null

            signInButton.visibility = View.VISIBLE
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.signInButton -> signIn()
            R.id.signOutButton -> signOut()
            R.id.disconnectButton -> revokeAccess()
        }
    }

    // [START signin]
    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }
    // [END signin]

    private fun signOut() {
        // Firebase sign out
        auth.signOut()

        // Google sign out
        googleSignInClient.signOut().addOnCompleteListener(this) {
            updateUI(null)
        }
    }

    private fun revokeAccess() {
        // Firebase sign out
        auth.signOut()

        // Google revoke access
        googleSignInClient.revokeAccess().addOnCompleteListener(this) {
            updateUI(null)
        }
    }

    companion object {
        private const val TAG = "myDebugTag"
        private const val RC_SIGN_IN = 9001
    }
}
