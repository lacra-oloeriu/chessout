package eu.chessout.v2.ui.club.clubdashboard

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storageMetadata
import com.theartofdev.edmodo.cropper.CropImage
import eu.chessout.shared.model.Club
import eu.chessout.v2.R
import eu.chessout.v2.util.GlideApp
import eu.chessout.v2.util.ImageUtil
import eu.chessout.v2.util.MyFirebaseUtils
import kotlinx.android.synthetic.main.club_dashboard_fragment.*
import java.io.File

class ClubDashboardFragment : Fragment() {

    private val viewModel: ClubDashboardViewModel by viewModels()
    private val args: ClubDashboardFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val mView = inflater.inflate(R.layout.club_dashboard_fragment, container, false)
        viewModel.initModel(args.clubId)
        return mView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        GlideApp.with(requireContext())
            .load(R.drawable.chess_king_and_rook_v1)
            .apply(RequestOptions().circleCrop())
            .into(clubPicture)

        viewModel.club.observe(viewLifecycleOwner, Observer {
            updateUi(it)
        })
        viewModel.isAdmin.observe(viewLifecycleOwner, Observer {
            if (it) {
                updateClubPicture.visibility = View.VISIBLE
                updateInfo.visibility = View.VISIBLE
            } else {
                updateClubPicture.visibility = View.GONE
                updateInfo.visibility = View.GONE
            }
        })
        viewModel.isDefaultClub.observe(viewLifecycleOwner, Observer {
            if (it) {
                defaultClubHeaderCard.visibility = View.VISIBLE
                makeDefaultCard.visibility = View.GONE
            } else {
                defaultClubHeaderCard.visibility = View.GONE
                makeDefaultCard.visibility = View.VISIBLE
            }
        })

        updateClubPicture.setOnClickListener {
            if (ImageUtil.askForPermissions(requireActivity())) {
                pickImageFromGallery()
            }
        }

        updateInfo.setOnClickListener {
            ClubUpdateDialog(viewModel.club!!.value!!).show(
                childFragmentManager,
                "ClubUpdateDialog"
            )
        }

        followersCard.setOnClickListener {
            val action = ClubDashboardFragmentDirections
                .actionClubDashboardFragmentToClubFollowersFragment(viewModel.clubId)
            it.findNavController().navigate(action)
        }

        qrCard.setOnClickListener {
            val dialogView: View = layoutInflater.inflate(R.layout.qr_dialog, null)
            ClubQrDialog(dialogView, viewModel.clubId).show(
                childFragmentManager,
                "ClubQrDialog"
            )
        }

        makeDefaultCard.setOnClickListener {
            val prefEditor = requireActivity().getPreferences(Context.MODE_PRIVATE).edit()
            viewModel.setCurrentAsDefaultClub(prefEditor)
            Toast.makeText(
                requireActivity().baseContext,
                "Club: ${viewModel.club.value?.shortName} Is now the default club",
                Toast.LENGTH_SHORT
            ).show()
        }

        viewModel.clubSettings.observe(viewLifecycleOwner, Observer {
            if (showPostsSwitch.isChecked != it.isShowPosts) {
                showPostsSwitch.isChecked = it.isShowPosts
            }
        })

        showPostsSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.showPostsChanged(isChecked)
        }
    }

    private fun getStorageReference(location: String): StorageReference {
        val storage = FirebaseStorage.getInstance()
        return storage.reference.child(location)
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        val mimeType = arrayOf("image/jpeg", "image/png", "image/jpg")
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeType)
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        startActivityForResult(intent, ImageUtil.IMAGE_PICK_CODE_FROM_CLUB_DASHBOARD)
    }

    private fun updateUi(club: Club?) {
        club?.picture?.stringUri?.let { path ->
            GlideApp.with(requireContext())
                .load(getStorageReference(path))
                .fallback(R.drawable.chess_king_and_rook_v1)
                .apply(RequestOptions().circleCrop())
                .into(clubPicture)
        }

        var name = ""
        club?.name?.let { string -> name = string }
        clubName.text = name

        var shortName = ""
        club?.shortName?.let { value -> shortName = value }
        clubShortName.text = shortName

        var country = ""
        club?.country?.let { value -> country = value }
        clubCountry.text = country

        var city = ""
        club?.city?.let { value -> city = value }
        clubCity.text = city

        var email = ""
        club?.email?.let { value -> email = value }
        clubEmail.text = email

        var description = ""
        club?.description?.let { value -> description = value }
        clubDescription.text = description
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            ImageUtil.IMAGE_PICK_CODE_FROM_CLUB_DASHBOARD -> {
                if (resultCode == Activity.RESULT_OK) {
                    data?.data.let {
                        it?.let {
                            ImageUtil.launchImageCrop(it, requireContext(), this)
                        }
                    }
                }
            }
            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val result = CropImage.getActivityResult(data)
                    persistClubNewProfileImage(result)
                }
            }
        }
    }

    private fun getFileNameFromCropUri(uri: Uri): String {
        val stringUri = uri.toString();
        val items = stringUri.split("/")
        val size = items.size
        return items[size - 1]
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

    private fun persistClubNewProfileImage(activityResult: CropImage.ActivityResult) {
        val cropUri = activityResult.uri!!
        val initialName = getFileNameFromCropUri(cropUri)
        val croppedFile = File(requireContext().cacheDir, initialName)

        val extension = getExtension(initialName)
        val metadata = storageMetadata {
            contentType = getContentType(initialName)
        }
        val localUri = croppedFile.toUri()
        viewModel.userId?.let {
            MyFirebaseUtils().persistClubDefaultPicture(
                viewModel.clubId,
                extension,
                metadata,
                localUri
            )
        }
    }
}
