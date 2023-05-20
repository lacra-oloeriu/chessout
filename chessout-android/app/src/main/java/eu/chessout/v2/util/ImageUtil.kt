package eu.chessout.v2.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import eu.chessout.shared.Constants
import java.util.*

class ImageUtil {
    companion object {
        const val PERMISSION_REQUEST_EXTERNAL_STORAGE_READ_CODE = 1000
        const val PERMISSION_REQUEST_EXTERNAL_STORAGE_WRITE_CODE = 1001
        const val PERMISSION_REQUEST_CAMERA_CODE = 1006
        const val IMAGE_PICK_CODE_FROM_ACTIVITY = 1003
        const val PERMISSION_REQUEST_SELECT_AND_CROP_IMAGE_FROM_MAIN_ACTIVITY = 1007
        const val IMAGE_PICK_CODE_FROM_USER_DASHBOARD = 1004
        const val IMAGE_PICK_CODE_FROM_CLUB_DASHBOARD = 1005
        //const val PICK_PDF_FILE = 1006

        fun pickImageFromGallery(activity: Activity, requestCode: Int) {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"
            val mimeType = arrayOf("image/jpeg", "image/png", "image/jpg")
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeType)
            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            activity.startActivityForResult(intent, requestCode)
        }

        private fun isPermissionsAllowed(permissionKey: String, context: Context): Boolean {
            val permission = ContextCompat.checkSelfPermission(
                context, permissionKey
            )
            return permission == PackageManager.PERMISSION_GRANTED
        }

        private fun isSelectImageAllowed(context: Context): Boolean {
            val readExternalStorage = ContextCompat.checkSelfPermission(
                context, Manifest.permission.READ_EXTERNAL_STORAGE
            )
            val writeExternalStorage = ContextCompat.checkSelfPermission(
                context, Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            // return false if not all image relate permissions are granted
            return !(readExternalStorage != PackageManager.PERMISSION_GRANTED ||
                    writeExternalStorage != PackageManager.PERMISSION_GRANTED)
        }

        fun askForPermissions(activity: Activity): Boolean {

            if (!isSelectImageAllowed(activity)) {
                val problemsWithRead = ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
                val problemsWitchWrite = ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                if (problemsWithRead || problemsWitchWrite) {
                    Log.d(Constants.LOG_TAG, "Permissions not ok READ")
                    return false
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        activity.requestPermissions(
                            arrayOf(
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                            ),
                            PERMISSION_REQUEST_SELECT_AND_CROP_IMAGE_FROM_MAIN_ACTIVITY
                        )
                        return isSelectImageAllowed(activity)
                    }
                }
            }
            return true
        }

        /**
         * It gets the extension from an uri
         */
        fun getExtension(displayName: String): String {
            val items = displayName.split(".")
            return items[1]
        }


        /**
         * It get the name from an Uri
         */
        private fun getFileNameFromUri(uri: Uri): String {
            val stringUri = uri.toString();
            val items = stringUri.split("/")
            val size = items.size
            return items[size - 1]
        }

        /**
         * It uses the extension from the uri and generates a new name for it
         */
        fun generatePictureName(uri: Uri): String {
            val fileName = getFileNameFromUri(uri)
            val generatedName = UUID.randomUUID().toString() + "." + getExtension(fileName)
            return generatedName
        }

        fun launchImageCrop(uri: Uri, context: Context, fragment: Fragment) {
            CropImage.activity(uri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1080, 1080)
                .setCropShape(CropImageView.CropShape.RECTANGLE)
                .start(context, fragment)
        }

        fun askForCameraPermissions(requireActivity: FragmentActivity): Boolean {
            if (!isPermissionsAllowed(Manifest.permission.CAMERA, requireActivity)) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        requireActivity,
                        Manifest.permission.CAMERA
                    )
                ) {
                    Log.d(Constants.LOG_TAG, "Camera permissions not ok")
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requireActivity.requestPermissions(
                            arrayOf(Manifest.permission.CAMERA),
                            PERMISSION_REQUEST_CAMERA_CODE
                        )
                    }
                }
                return false
            }
            return true
        }
    }
}