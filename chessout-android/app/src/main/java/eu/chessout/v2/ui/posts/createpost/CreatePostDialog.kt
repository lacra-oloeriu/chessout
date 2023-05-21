package eu.chessout.v2.ui.posts.createpost

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import eu.chessout.shared.Constants
import eu.chessout.shared.dao.BasicApiResponse
import eu.chessout.shared.model.Club
import eu.chessout.shared.model.MyPayLoad
import eu.chessout.shared.model.Post
import eu.chessout.v2.R
import eu.chessout.v2.model.BasicApiService
import eu.chessout.v2.util.GlideApp
import eu.chessout.v2.util.ImageUtil
import eu.chessout.v2.util.MyFirebaseUtils
import eu.chessout.v2.util.SharedPreferencesHelper
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class CreatePostDialog(
    val imageUri: Uri?
) : DialogFragment() {
    private lateinit var mView: View
    private lateinit var sharedPref: SharedPreferences

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE)
        mView = layoutInflater.inflate(R.layout.create_post_dialog, null)
        val builder = AlertDialog.Builder(requireActivity())
        builder.setView(mView)
        showImage()
        configureFab()
        return builder.create()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (null != imageUri) {
            showImage()
        }
    }

    private fun configureFab() {
        val view = mView.findViewById<FloatingActionButton>(R.id.fab)
        view.setOnClickListener {
            val activity = requireActivity()
            dismiss()
            GlobalScope.launch {
                persistPost(activity)
            }
        }
    }

    private fun showImage() {
        val imageView = mView.findViewById<ImageView>(R.id.postImage)
        GlideApp.with(requireContext())
            .load(imageUri!!)
            .into(imageView)
    }

    private fun persistPost(activity: Activity) {
        // persist post in club stream (any post has to be part of a club)
        val post = MyFirebaseUtils().createAndPersistPost(buildPost(activity))
        // persist post in user stream
        val userId = MyFirebaseUtils.currentUserId()
        MyFirebaseUtils.persistPostInUserStream(userId, post)

        var index = 0;
        post.pictures.forEach {
            if (null != imageUri) {
                val localUri = imageUri!!
                MyFirebaseUtils().uploadPicture(
                    it, // picture
                    post,
                    index++, // index of picture in post
                    localUri,
                    activity.contentResolver // content resolver
                )
            }
        }

        Log.d(Constants.LOG_TAG, "Post persisted ${post.postId}")

        notifyTheBackend(post.clubId, post.postId)
    }

    private fun notifyTheBackend(clubId: String, postId: String) {
        val myPayLoad = MyPayLoad()
        myPayLoad.event = MyPayLoad.Event.CLUB_POST_CREATED
        myPayLoad.postId = postId
        myPayLoad.clubId = clubId
        myPayLoad.authToken = MyFirebaseUtils.currentUserToken()

        BasicApiService().postCreated(myPayLoad)
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : DisposableSingleObserver<BasicApiResponse>() {
                override fun onSuccess(value: BasicApiResponse?) {
                    Log.d(Constants.LOG_TAG, "Success${value?.message}")
                }

                override fun onError(e: Throwable?) {
                    Log.e(Constants.LOG_TAG, "Error ${e?.message}")
                }

            })
    }

    private fun buildPost(activity: Activity): Post {

        val message = (mView.findViewById<EditText>(R.id.messageText)).text.toString()
        val postType = SharedPreferencesHelper.getPostType(sharedPref)

        val clubId = SharedPreferencesHelper.getDefaultClub(sharedPref)

        val post = Post()
        post.postType = postType
        post.clubId = clubId
        post.userId = MyFirebaseUtils().getCurrentUserId()

        imageUri?.let {
            var extension = ""
            val cursor = activity.contentResolver.query(
                it!!, null, null, null, null
            )
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    val displayName =
                        cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                    extension = ImageUtil.getExtension(displayName)
                    Log.d(Constants.LOG_TAG, "Display name: $displayName")
                }
            } finally {
                cursor!!.close();
            }

            val picture = MyFirebaseUtils().createAndPersistPicture(clubId!!, extension)
            post.pictures = listOf(picture)
        }

        post.message = message
        post.setTimeStamps()
        post.userName = MyFirebaseUtils().getUserDisplayName(post.userId)
        post.userPictureUrl = MyFirebaseUtils().getUserProfilePictureUri(post.userId)

        val club: Club = MyFirebaseUtils().getClubPublicInfo(clubId)
        post.clubPictureUrl = club?.picture?.stringUri
        post.clubShortName = club?.shortName

        return post
    }


}