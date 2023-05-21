package eu.chessout.v2.ui.userdashboard

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.storage.ktx.storageMetadata
import com.theartofdev.edmodo.cropper.CropImage
import eu.chessout.shared.Constants
import eu.chessout.v2.R
import eu.chessout.v2.util.GlideApp
import eu.chessout.v2.util.ImageUtil
import eu.chessout.v2.util.MyFirebaseUtils
import eu.chessout.v2.util.SharedPreferencesHelper
import kotlinx.android.synthetic.main.user_dashboard_fragment.*
import java.io.File

class UserDashboardFragment : Fragment() {

    companion object {
        fun newInstance() = UserDashboardFragment()
    }

    private val viewModel: UserDashboardViewModel by viewModels()
    private val args: UserDashboardFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.user_dashboard_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.initModel(args.userId)
        viewModel.pictureUrl.observe(viewLifecycleOwner, Observer {
            if (viewModel.isUploaded.value!!) {
                val uri = MyFirebaseUtils().storageReference(it!!)
                GlideApp.with(requireContext())
                    .load(uri)
                    .apply(RequestOptions().circleCrop())
                    .into(playerProfilePicture)
            } else {
                val uri = Uri.parse(it)
                GlideApp.with(requireContext())
                    .load(uri)
                    .apply(RequestOptions().circleCrop())
                    .into(playerProfilePicture)
            }
        })

        viewModel.sameUser.observe(viewLifecycleOwner, Observer { sameUser ->
            if (sameUser) {
                updateProfilePicture.visibility = View.VISIBLE
                updateInfo.visibility = View.GONE
                signOutCard.visibility = View.VISIBLE
            } else {
                updateProfilePicture.visibility = View.GONE
                updateInfo.visibility = View.GONE
                signOutCard.visibility = View.GONE
            }
        })
        viewModel.displayName.observe(viewLifecycleOwner, Observer {
            it?.let {
                name.text = it
            }
        })
        viewModel.displayEmail.observe(viewLifecycleOwner, Observer {
            it?.let {
                email.text = it
            }
        })

        configClickListeners()
    }

    private fun configClickListeners() {

        updateProfilePicture.setOnClickListener {
            if (ImageUtil.askForPermissions(requireActivity())) {
                pickImageFromGallery()
            }
        }

        signOutCard.setOnClickListener {
            val editor = requireActivity().getPreferences(Context.MODE_PRIVATE).edit()
            SharedPreferencesHelper.setDefaultClub(editor, SharedPreferencesHelper.NO_DEFAULT_CLUB)
            val argsBundle = bundleOf("timeToLogOut" to true)
            view?.findNavController()?.navigate(R.id.signInActivity, argsBundle)
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        val mimeType = arrayOf("image/jpeg", "image/png", "image/jpg")
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeType)
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        startActivityForResult(intent, ImageUtil.IMAGE_PICK_CODE_FROM_USER_DASHBOARD)
    }

    private fun getFileNameFromCropUri(uri: Uri): String {
        val stringUri = uri.toString();
        val items = stringUri.split("/")
        val size = items.size
        return items[size - 1]
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        Log.d(Constants.LOG_TAG, "Request code: $requestCode")
        when (requestCode) {
            ImageUtil.IMAGE_PICK_CODE_FROM_USER_DASHBOARD -> {
                if (resultCode == Activity.RESULT_OK) {
                    intent?.data?.let {
                        ImageUtil.launchImageCrop(it, requireContext(), this)
                    }
                }
            }
            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val result = CropImage.getActivityResult(intent)
                    val cropUri = result.uri!!
                    val initialName = getFileNameFromCropUri(cropUri)
                    val croppedFile = File(requireContext().cacheDir, initialName)
                    val userId = viewModel.currentUserId!!

                    val extension = getExtension(initialName)
                    val metadata = storageMetadata {
                        contentType = getContentType(initialName)
                    }
                    val localUri = croppedFile.toUri()

                    MyFirebaseUtils().persistNewUserDefaultPicture(
                        userId,
                        extension,
                        metadata,
                        localUri
                    )
                }
            }
        }
    }

    private fun getExtension(displayName: String): String {
        val items = displayName.split(".")
        return items[1]
    }

    private fun getContentType(displayName: String): String {
        val items = displayName.split(".")
        val extension = items[1]
        if (extension == "jpg") {
            return "image/jpeg"
        }
        throw IllegalStateException("Not supported content type")
    }
}
