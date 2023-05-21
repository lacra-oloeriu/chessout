package eu.chessout.v2

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import eu.chessout.shared.Constants
import eu.chessout.shared.model.DefaultClub
import eu.chessout.shared.model.Post
import eu.chessout.v2.ui.club.myclubs.MyClubsFragmentDirections
import eu.chessout.v2.ui.posts.createpost.CreatePostDialog
import eu.chessout.v2.util.ImageUtil
import eu.chessout.v2.util.MyFirebaseUtils
import eu.chessout.v2.util.SharedPreferencesHelper

class MainActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private var firebaseUser: FirebaseUser? = null
    private lateinit var navController: NavController
    private lateinit var bottomNavView: BottomNavigationView
    private lateinit var sharedPref: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        checkLoggedIn()

        bottomNavView = findViewById(R.id.bottom_nav_view)

        navController = findNavController(R.id.nav_host_fragment)
        NavigationUI.setupActionBarWithNavController(this, navController)

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard02, R.id.navigation_notifications
            )
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        bottomNavView.setupWithNavController(navController)

        if (null == savedInstanceState) {
            // bottomNavView.selectedItemId = R.id.navigation_home
        }

        sharedPref = getPreferences(Context.MODE_PRIVATE)
    }


    override fun onStart() {
        super.onStart()

        val listener = getDefaultClubListener()
        MyFirebaseUtils().registerDefaultClubListener(listener, true)
        setCustomPostListener()


    }

    /**
     * It checks the user is logged in and if not it will start the SignInActivity
     */
    private fun checkLoggedIn() {
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = firebaseAuth.currentUser
        if (null == firebaseUser) {
            startActivity(Intent(this, SignInActivity::class.java))
        }
    }


    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp()
    }


    fun showBottomNav() {
        bottomNavView.visibility = View.VISIBLE
    }

    fun hideBottomNav() {
        bottomNavView.visibility = View.GONE
    }

    private fun setCustomPostListener() {
        val postMenuItem = bottomNavView.findViewById<View>(R.id.navigation_newPost)


        postMenuItem?.setOnClickListener {
            initCreatePost()
        }
    }

    private fun getDefaultClubListener(): MyFirebaseUtils.DefaultClubListener {
        class DefaultClubListener : MyFirebaseUtils.DefaultClubListener {
            override fun onDefaultClubValue(defaultClub: DefaultClub) {
                SharedPreferencesHelper.setDefaultClub(sharedPref.edit(), defaultClub.clubKey)
            }

            override fun setDbRef(databaseReference: DatabaseReference) {
                // nothing to implement
            }

            override fun setDbListener(valueEventListener: ValueEventListener) {
                // nothing to implement
            }
        }

        return DefaultClubListener()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        when (requestCode) {
            ImageUtil.IMAGE_PICK_CODE_FROM_ACTIVITY -> {
                if (resultCode == Activity.RESULT_OK) {
                    intent?.data.let {
                        Log.d(Constants.LOG_TAG, "Image uri: $it")
                        val clubId = SharedPreferencesHelper.getDefaultClub(sharedPref)
                        val dialog = CreatePostDialog(it)
                        dialog.show(
                            supportFragmentManager,
                            "CreateClubPost"
                        )
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            ImageUtil.PERMISSION_REQUEST_SELECT_AND_CROP_IMAGE_FROM_MAIN_ACTIVITY -> {
                initCreatePost()
            }
            ImageUtil.PERMISSION_REQUEST_CAMERA_CODE -> {
                val qrDirection = MyClubsFragmentDirections
                    .actionNavigationMyClubsFragmentToJoinClubByQrCodeFragment()
                navController.navigate(qrDirection)
            }
        }
    }

    private fun initCreatePost() {
        val clubId = SharedPreferencesHelper.getDefaultClub(sharedPref)
        val postType = SharedPreferencesHelper.getPostType(sharedPref)
        if (SharedPreferencesHelper.NO_DEFAULT_CLUB == clubId) {
            Toast.makeText(
                applicationContext, "No default club selected. " +
                        "Use the Dashboard to join a club", Toast.LENGTH_SHORT
            )
                .show()
        } else {
            if (ImageUtil.askForPermissions(this)) {
                postType?.let {
                    when (it) {
                        Post.PostType.CLUB_POST -> ImageUtil.pickImageFromGallery(
                            this,
                            ImageUtil.IMAGE_PICK_CODE_FROM_ACTIVITY
                        )
                        Post.PostType.USER_POST -> ImageUtil.pickImageFromGallery(
                            this,
                            ImageUtil.IMAGE_PICK_CODE_FROM_ACTIVITY
                        )
                    }
                }
            }
        }
    }
/*
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // FCM SDK (and your app) can post notifications.
        } else {
            // TODO: Inform user that that your app will not show notifications.
        }
    }

    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // TODO: display an educational UI explaining to the user the features that will be enabled
                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                //       If the user selects "No thanks," allow the user to continue without notifications.
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }*/

}
