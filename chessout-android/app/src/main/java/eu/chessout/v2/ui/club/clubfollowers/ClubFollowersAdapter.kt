package eu.chessout.v2.ui.club.clubfollowers

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.storage.FirebaseStorage
import eu.chessout.shared.model.UserInfo
import eu.chessout.v2.R
import eu.chessout.v2.util.GlideApp
import eu.chessout.v2.util.listeners.FirebaseQueryLiveData

class ClubFollowersAdapter(private val userIdList: ArrayList<String>) :
    RecyclerView.Adapter<ClubFollowersAdapter.ItemHolder>() {

    private lateinit var context: Context
    private lateinit var fragmentManager: FragmentManager
    private lateinit var clubId: String
    private lateinit var lifecycleOwner: LifecycleOwner
    private val storage = FirebaseStorage.getInstance()

    private lateinit var liveUserInfoMap: MutableMap<String, FirebaseQueryLiveData<UserInfo>>


    class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textView = itemView.findViewById<TextView>(R.id.list_item_text_simple_view)
        var tournamentOrder = itemView.findViewById<TextView>(R.id.tournamentOrder)
        val profilePicture = itemView.findViewById<ImageView>(R.id.profilePicture)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val itemHolder = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_tournament_player, parent, false)
        return ItemHolder(itemHolder)
    }

    override fun getItemCount(): Int {
        return userIdList.size
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        val userId: String = userIdList[position]
        liveUserInfoMap[userId]?.let {
            it.observe(lifecycleOwner, Observer {
                holder.textView.text = it.userName
                holder.tournamentOrder.text = "${position + 1} ."
                setImage(holder.profilePicture, it)
            })
        }
        if (!liveUserInfoMap.containsKey(userId)) {
            holder.textView.text = "No name: $userId"
        }
    }


    private fun setImage(imageView: ImageView, userInfo: UserInfo) {
        GlideApp.with(context)
            .load(R.drawable.ic_user_circle_regular_gray)
            .into(imageView)
        liveUserInfoMap[userInfo.userId]?.let { mapLivePlayer ->
            mapLivePlayer.observe(lifecycleOwner, Observer {
                if (null != it.pictureUri) {
                    val pictureReference = storage.reference.child(
                        it.pictureUri
                    )
                    pictureReference.downloadUrl.addOnSuccessListener {
                        GlideApp.with(context)
                            .load(pictureReference)
                            .fallback(R.drawable.ic_user_circle_regular_gray)
                            .apply(RequestOptions().circleCrop())
                            .into(imageView)
                    }
                }
            })
        }
    }

    fun setContextValues(
        context: Context, fragmentManager: FragmentManager,
        clubId: String,
        liveUserInfoMap: MutableMap<String, FirebaseQueryLiveData<UserInfo>>,
        lifecycleOwner: LifecycleOwner
    ) {
        this.context = context
        this.fragmentManager = fragmentManager
        this.clubId = clubId
        this.liveUserInfoMap = liveUserInfoMap
        this.lifecycleOwner = lifecycleOwner
    }

    fun updateList(newList: List<String>) {
        userIdList.clear()
        userIdList.addAll(newList)
        notifyDataSetChanged()
    }

}