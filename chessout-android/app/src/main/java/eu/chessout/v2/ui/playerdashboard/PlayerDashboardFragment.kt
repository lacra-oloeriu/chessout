package eu.chessout.v2.ui.playerdashboard

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storageMetadata
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import eu.chessout.shared.Constants
import eu.chessout.v2.R
import eu.chessout.v2.ui.club.players.ClubPlayerUpdateDialog
import eu.chessout.v2.util.GlideApp
import kotlinx.android.synthetic.main.player_dashboard_fragment.*
import java.io.File
import java.util.*

class PlayerDashboardFragment : Fragment() {

    companion object {
        private const val PERMISSION_REQUEST_EXTERNAL_STORAGE_READ_CODE = 1000
        private const val PERMISSION_REQUEST_EXTERNAL_STORAGE_WRITE_CODE = 1001
        private const val IMAGE_PICK_PLAYER_CODE = 1002
        fun newInstance() = PlayerDashboardFragment()
    }

    lateinit var clubId: String
    lateinit var playerId: String
    private val viewModel: PlayerDashboardViewModel by viewModels()
    private val storage = FirebaseStorage.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            this.clubId = requireArguments().getString("clubId")!!
            this.playerId = requireArguments().getString("playerId")!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.player_dashboard_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        playerRing.visibility = View.GONE

        updateProfilePicture.setOnClickListener {
            if (askForPermissions()) {
                pickImageFromGallery()
            } else {
                Toast.makeText(
                    requireContext(), "Permission denied by rationale",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        updateInfo.setOnClickListener {
            ClubPlayerUpdateDialog(viewModel.player!!.value!!).show(
                childFragmentManager,
                "ClubPlayerUpdateDialog"
            )
        }

        archivePlayer.setOnClickListener {
            viewModel.setArchived(true)
        }
        activatePlayer.setOnClickListener {
            viewModel.setArchived(false)
        }

        viewModel.defaultPictureUri.observe(viewLifecycleOwner, Observer { stringUri ->
            if (null == stringUri) {
                playerRing.visibility = View.VISIBLE
                playerProfilePicture.visibility = View.VISIBLE
                GlideApp.with(requireContext())
                    .load(R.drawable.ic_user_alt_solid_gray)
                    .apply(RequestOptions().circleCrop())
                    .into(playerProfilePicture)
            }
            stringUri?.let {
                playerProfilePicture.visibility = View.VISIBLE

                val storageReference = storage.reference.child(stringUri)
                playerRing.visibility = View.VISIBLE
                GlideApp.with(requireContext())
                    .load(storageReference)
                    .apply(RequestOptions().circleCrop())
                    .placeholder(R.drawable.ic_user_alt_solid_gray)
                    .into(playerProfilePicture)
            }
        })

        viewModel.isAdmin.observe(viewLifecycleOwner, Observer {
            if (it) {
                updateProfilePicture.visibility = View.VISIBLE
                updateInfo.visibility = View.VISIBLE
            } else {
                updateProfilePicture.visibility = View.GONE
                updateInfo.visibility = View.GONE
            }
        })
        viewModel.showArchive.observe(viewLifecycleOwner, Observer {
            if (it) {
                archivePlayer.visibility = View.VISIBLE
            } else {
                archivePlayer.visibility = View.GONE
            }
        })
        viewModel.showActivate.observe(viewLifecycleOwner, Observer {
            if (it) {
                activatePlayer.visibility = View.VISIBLE
            } else {
                activatePlayer.visibility = View.GONE
            }
        })


        viewModel.initModel(this.clubId, this.playerId)

        viewModel.player.observe(viewLifecycleOwner, Observer { player ->
            player?.let {
                name.text = player.name
                elo.text = "(${player.elo})"
                if (player.isArchived) {
                    activeState.text = "Archived"
                } else {
                    activeState.text = "Active"
                }
            }
        })

        viewModel.followPlayerSettings.observe(viewLifecycleOwner, Observer {
            sendNotificationsSwitch.isChecked = it.isSendNotificationWhenGameResultIsUpdated
        })

        sendNotificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.sendNotificationsChanged(isChecked)
        }
    }

    private fun isPermissionsAllowed(permissionKey: String): Boolean {
        val permission = ContextCompat.checkSelfPermission(
            requireContext(), permissionKey
        )
        return permission == PackageManager.PERMISSION_GRANTED
    }


    private fun askForPermissions(): Boolean {
        if (!isPermissionsAllowed(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    requireActivity(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            ) {
                Log.d(Constants.LOG_TAG, "Permissions not ok READ")
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requireActivity().requestPermissions(
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        PERMISSION_REQUEST_EXTERNAL_STORAGE_READ_CODE
                    )
                }
            }
            return false
        }

        if (!isPermissionsAllowed(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    requireActivity(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            ) {
                Log.d(Constants.LOG_TAG, "Permissions not ok WRITE")
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requireActivity().requestPermissions(
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        PERMISSION_REQUEST_EXTERNAL_STORAGE_WRITE_CODE
                    )
                }
            }
            return false
        }

        return true
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        val mimeType = arrayOf("image/jpeg", "image/png", "image/jpg")
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeType)
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        startActivityForResult(intent, IMAGE_PICK_PLAYER_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            IMAGE_PICK_PLAYER_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    data?.data?.let { uri ->
                        launchImageCrop(uri)
                    }
                } else {
                    Log.e(Constants.LOG_TAG, "Image selection error: Not able to select image")
                }
            }
            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {

                val result = CropImage.getActivityResult(data)
                if (resultCode == Activity.RESULT_OK) {
                    val imageUri = result.uri!!

                    // second version to locate file
                    val fileName = getFileName(imageUri)
                    Log.d(Constants.LOG_TAG, "File name = $fileName")
                    val croppedFile = File(requireContext().cacheDir, fileName)

                    val generatedName = UUID.randomUUID().toString() + "." + getExtension(fileName);
                    val locProfilePicture = Constants.LOCATION_PLAYER_MEDIA_PROFILE_PICTURE
                        .replace(Constants.CLUB_KEY, clubId)
                        .replace(Constants.PLAYER_KEY, playerId) + "/" + generatedName;

                    val storageReference = storage.reference.child(locProfilePicture)
                    //val stream = FileInputStream(File(imageUri.toString()))

                    val metadata = storageMetadata {
                        contentType = getContentType(fileName)
                    }
                    val uploadTask = storageReference.putFile(croppedFile.toUri(), metadata)
                    viewModel.setDefaultPicture(uploadTask, generatedName)

                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Log.e(Constants.LOG_TAG, "Crop error: ${result.error}")
                }
            }
        }
    }

    private fun launchImageCrop(uri: Uri) {
        CropImage.activity(uri)
            .setGuidelines(CropImageView.Guidelines.ON)
            .setAspectRatio(1080, 1080)
            .setCropShape(CropImageView.CropShape.RECTANGLE)
            .start(requireContext(), this)
    }

    private fun getContentType(displayName: String): String {
        val items = displayName.split(".")
        val extension = items[1]
        if (extension == "jpg") {
            return "image/jpeg"
        }
        throw IllegalStateException("Not supported content type")
    }

    private fun getExtension(displayName: String): String {
        val items = displayName.split(".")
        return items[1]
    }

    private fun getFileName(uri: Uri): String {
        val stringUri = uri.toString();
        val items = stringUri.split("/")
        val size = items.size
        return items[size - 1]
    }
}
