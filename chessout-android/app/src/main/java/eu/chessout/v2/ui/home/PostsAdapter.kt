package eu.chessout.v2.ui.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.request.RequestOptions
import eu.chessout.shared.model.ChatItem
import eu.chessout.shared.model.Post
import eu.chessout.shared.model.Post.PostType
import eu.chessout.shared.model.Tournament
import eu.chessout.v2.R
import eu.chessout.v2.util.GlideApp
import eu.chessout.v2.util.MyFirebaseUtils
import eu.chessout.v2.util.listeners.FirebaseQueryLiveData
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class PostsAdapter(var posts: ArrayList<Post>) :
    RecyclerView.Adapter<PostsAdapter.ItemHolder>() {


    companion object {
        private const val REGULAR_POST = 100
        private const val TOURNAMENT_CREATED = 101
        private const val TOURNAMENT_PAIRINGS_AVAILABLE = 102
    }

    private lateinit var context: Context
    private lateinit var likesMap: HashMap<String, FirebaseQueryLiveData<Long>>
    private lateinit var chatItemsMap: HashMap<String, FirebaseQueryLiveData<Long>>
    private lateinit var userLikesMap: HashMap<String, FirebaseQueryLiveData<Boolean>>
    private lateinit var tournamentTotalPlayersMap: HashMap<String, FirebaseQueryLiveData<Long>>
    private lateinit var gamesCompletedMap: HashMap<String, FirebaseQueryLiveData<HomeViewModel.CompletedGames>>
    private lateinit var lifecycleOwner: LifecycleOwner
    private val myFirebaseUtils = MyFirebaseUtils()

    class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val avatarImage = itemView.findViewById<ImageView>(R.id.avatarImage)
        val avatarText = itemView.findViewById<TextView>(R.id.avatarText)
        val postText = itemView.findViewById<TextView>(R.id.postText)
        val postPicture = itemView.findViewById<ImageView>(R.id.postImage)
        val likesImage = itemView.findViewById<ImageView>(R.id.likesImage)
        val likesText = itemView.findViewById<TextView>(R.id.likesText)
        val commentsText = itemView.findViewById<TextView>(R.id.commentsText)
        val commentsImage = itemView.findViewById<ImageView>(R.id.commentsImage)
        val optionsImage = itemView.findViewById<ImageView>(R.id.optionsImage)

        //val tournamentName
        val tournamentName = itemView.findViewById<TextView>(R.id.tournamentName)
        val tournamentLocation = itemView.findViewById<TextView>(R.id.tournamentLocation)
        val playersCount = itemView.findViewById<TextView>(R.id.playersCount)
        val postTitle = itemView.findViewById<TextView>(R.id.postTitle)
        val labelPlayers = itemView.findViewById<TextView>(R.id.labelPlayers)
        val playersSection = itemView.findViewById<ConstraintLayout>(R.id.playersSection)


        val funMessages = listOf(
            "Functionality not implemented yet",
            "We understand that chat is important and we are working hard to bring this feature to you",
            "Please wait a bit until the next release and we promise that chat will be available",
            "No chat available yet",
            "Keep on clicking",
            "Clicking is fun"
        )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val view: View = when (viewType) {
            REGULAR_POST -> {
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.post_item, parent, false)
            }
            TOURNAMENT_CREATED -> {
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.post_tournament_created_item, parent, false)
            }
            TOURNAMENT_PAIRINGS_AVAILABLE -> {
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.post_tournament_created_item, parent, false)
            }
            else -> throw NotImplementedError("Not supported type: $viewType")
        }

        return ItemHolder(view)
    }

    override fun getItemViewType(position: Int): Int {
        val post = posts[position]
        return when (post.postType) {
            PostType.CLUB_POST -> REGULAR_POST
            PostType.USER_POST -> REGULAR_POST
            PostType.TOURNAMENT_CREATED -> TOURNAMENT_CREATED
            PostType.TOURNAMENT_PAIRINGS_AVAILABLE -> TOURNAMENT_PAIRINGS_AVAILABLE
            else -> throw NotImplementedError("Not supported type: ${post.postType}")
        }
    }

    override fun getItemCount(): Int {
        return posts.size
    }


    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        val post: Post = posts[position]

        when (post.postType) {
            PostType.USER_POST -> bindUserPost(holder, post)
            PostType.CLUB_POST -> bindUserPost(holder, post)
            PostType.TOURNAMENT_CREATED -> {
                bindUserPost(holder, post)
                bindTournamentCreatedPost(holder, post)
            }
            PostType.TOURNAMENT_PAIRINGS_AVAILABLE -> {
                bindUserPost(holder, post)
                bindPairingsAvailablePost(holder, post)
            }
            else -> throw NotImplementedError("Not supported type: ${post.postType}")
        }
    }


    fun updateList(newList: List<Post>) {
        posts.clear()
        posts.addAll(newList)
        notifyDataSetChanged()
    }

    public fun setContext(context: Context) {
        this.context = context
    }

    public fun setLifeCycleOwner(lifecycleOwner: LifecycleOwner) {
        this.lifecycleOwner = lifecycleOwner
    }

    private fun registerLikesCountListener(textView: TextView, clubId: String, postId: String) {
        this.likesMap?.get(postId)?.let {
            it.observe(lifecycleOwner, Observer { count ->
                updateLikeTest(textView, count)
            })
        }
    }

    private fun registerChatCountListener(textView: TextView, postId: String) {
        this.chatItemsMap?.get(postId)?.let {
            it.observe(lifecycleOwner, Observer { count ->
                updateChatCountText(textView, count)
            })
        }
    }


    private fun registerUserLikedThisPostListener(
        imageView: ImageView,
        clubId: String,
        postId: String
    ) {
        this.userLikesMap[postId]?.let {
            it.observe(lifecycleOwner, Observer { userLikesPost -> //boolean live data
                updateLikeImage(imageView, userLikesPost)
            })
        }
    }

    private fun updateLikeImage(imageView: ImageView, userLikesPost: Boolean) {
        if (userLikesPost) {
            GlideApp.with(context)
                .load(R.drawable.ic_heart_solid)
                .into(imageView)
        } else {
            GlideApp.with(context)
                .load(R.drawable.ic_heart_regular)
                .into(imageView)
        }
    }

    private fun createPopUpMenu(imageView: View, clubId: String, postId: String) {
        val popupMenu = PopupMenu(context, imageView)
        popupMenu.inflate(R.menu.post_options_menu)
        popupMenu.setOnMenuItemClickListener { item ->
            item?.let {
                when (item.itemId) {
                    R.id.postItemDelete -> myFirebaseUtils.deletePost(clubId, postId)
                }
            }
            true
        }
        popupMenu.show()
    }

    private fun bindUserPost(holder: ItemHolder, post: Post) {

        holder.postText?.text = post.message

        // avatar image
        when (post.postType) {
            Post.PostType.USER_POST -> {
                holder.avatarText.text = post.userName
                post.userPictureUrl?.let {
                    GlideApp.with(context)
                        .load(myFirebaseUtils.storageReference(it))
                        .fallback(R.drawable.ic_user_circle_regular_themed)
                        .apply(RequestOptions().circleCrop())
                        .into(holder.avatarImage)
                }
                holder.avatarImage.setOnClickListener {
                    val action = HomeFragmentDirections.actionNavigationHomeToUserDashboardFragment(
                        post.userId
                    )
                    it.findNavController()?.navigate(action)
                }
            }
            Post.PostType.CLUB_POST -> {
                holder.avatarText.text = post.clubShortName
                post.clubPictureUrl?.let {
                    GlideApp.with(context)
                        .load(myFirebaseUtils.storageReference(it))
                        .fallback(R.drawable.chess_king_and_rook_v1)
                        .apply(RequestOptions().circleCrop())
                        .into(holder.avatarImage)
                }
            }
        }

        // post picture
        post.pictures?.let {
            val firstPicture = post.pictures.first()
            holder.postPicture?.let {
                if (firstPicture.isUploadComplete) {
                    holder.postPicture.visibility = View.VISIBLE
                    val pictureReference =
                        MyFirebaseUtils().storageReference(firstPicture.stringUri)
                    GlideApp.with(context)
                        .load(pictureReference)
                        .into(holder.postPicture)
                } else {
                    holder.postPicture.visibility = View.GONE
                }
            }
        }



        holder.likesImage.setOnClickListener {
            localOnlyLikeUpdate(holder, post)
            GlobalScope.launch {
                myFirebaseUtils.processLikeRequest(post)
            }
        }

        registerLikesCountListener(holder.likesText, post.clubId, post.postId)
        registerUserLikedThisPostListener(holder.likesImage, post.clubId, post.postId)
        registerChatCountListener(holder.commentsText, post.postId)

        // and we can also navigate the the chat screen
        val chatDirection = HomeFragmentDirections
            .actionNavigationHomeToChatFragment(
                ChatItem.LocationType.POST.toString(),
                post.clubId,
                null,
                null,
                post.postId
            )
        holder.commentsImage.setOnClickListener {
            it.findNavController()?.navigate(chatDirection)
        }



        holder.optionsImage.setOnClickListener {
            createPopUpMenu(it, post.clubId, post.postId)
        }
    }

    /**
     * This function is meant to update local state of the adapter after the user incremented
     * or decremented the like count
     */
    private fun localOnlyLikeUpdate(
        holder: ItemHolder,
        post: Post
    ) {
        if (userLikesMap[post.postId]!!.value!!) {
            val newCount: Long = likesMap[post.postId]!!.value!! - 1
            updateLikeTest(holder.likesText, newCount)
            updateLikeImage(holder.likesImage, false)
        } else {
            val newCount: Long = likesMap[post.postId]!!.value!! + 1
            updateLikeTest(holder.likesText, newCount)
            updateLikeImage(holder.likesImage, true)
        }
    }

    private fun updateLikeTest(textView: TextView, count: Long) {
        if (count == 1L) {
            textView.text = "$count like"
        } else {
            textView.text = "$count likes"
        }
    }

    private fun updateChatCountText(textView: TextView, count: Long) {
        if (count == 1L) {
            textView.text = "$count comment"
        } else {
            textView.text = "$count comments"
        }
    }


    private fun bindTournamentCreatedPost(holder: PostsAdapter.ItemHolder, post: Post) {
        holder.avatarText.text = post.clubShortName
        post.clubPictureUrl?.let {
            GlideApp.with(context)
                .load(myFirebaseUtils.storageReference(it))
                .fallback(R.drawable.chess_king_and_rook_v1)
                .apply(RequestOptions().circleCrop())
                .into(holder.avatarImage)
        }

        // tournament name end location
        val tournamentListener = object : MyFirebaseUtils.TournamentListener {
            override fun onTournamentValue(tournament: Tournament) {
                tournament?.let {
                    holder.tournamentName.text = tournament.name
                    holder.tournamentLocation.text = tournament.location
                    val tournamentDirection = HomeFragmentDirections
                        .actionNavigationHomeToTournamentDashboardFragment(
                            post.tournamentId,
                            post.clubId,
                            tournament.totalRounds
                        )
                    holder.tournamentName.setOnClickListener {
                        it.findNavController()?.navigate(tournamentDirection)
                    }
                    holder.postTitle.setOnClickListener {
                        it.findNavController()?.navigate(tournamentDirection)
                    }
                }
            }
        }
        myFirebaseUtils.registerTournamentListener(
            true,
            post.clubId,
            post.tournamentId,
            tournamentListener
        )

        // tournament players listener
        this.tournamentTotalPlayersMap?.get(post.postId)?.let {
            it.observe(lifecycleOwner, Observer { count ->
                if (count == 1L) {
                    holder.playersCount.text = "1 player"
                } else {
                    holder.playersCount.text = "$count players"
                }
            })
        }
    }

    private fun bindPairingsAvailablePost(holder: PostsAdapter.ItemHolder, post: Post) {
        holder.avatarText.text = post.clubShortName
        post.clubPictureUrl?.let {
            GlideApp.with(context)
                .load(myFirebaseUtils.storageReference(it))
                .fallback(R.drawable.chess_king_and_rook_v1)
                .apply(RequestOptions().circleCrop())
                .into(holder.avatarImage)
        }

        // tournament name end location
        val tournamentListener = object : MyFirebaseUtils.TournamentListener {
            override fun onTournamentValue(tournament: Tournament) {
                tournament?.let {
                    holder.tournamentName.text = tournament.name
                    holder.tournamentLocation.text = tournament.location

                    holder.tournamentName.setOnClickListener {
                        val destination = HomeFragmentDirections
                            .actionNavigationHomeToTournamentDashboardFragment(
                                post.tournamentId,
                                post.clubId,
                                tournament.totalRounds
                            )
                        it.findNavController()?.navigate(destination)
                    }

                    // we have total count so w can also navigate to specific round
                    val roundPageDirection = HomeFragmentDirections
                        .actionNavigationHomeToRoundPagerFragment(
                            post.clubId,
                            post.tournamentId,
                            tournament.totalRounds,
                            post.roundId
                        )
                    holder.postTitle.setOnClickListener {
                        it.findNavController()?.navigate(roundPageDirection)
                    }
                    holder.playersSection.setOnClickListener {
                        it.findNavController()?.navigate(roundPageDirection)
                    }

                    // and we can also navigate the the chat screen
                    val chatDirection = HomeFragmentDirections
                        .actionNavigationHomeToChatFragment(
                            ChatItem.LocationType.POST.toString(),
                            post.clubId,
                            post.tournamentId,
                            post.roundId.toString(),
                            post.postId
                        )
                    holder.commentsImage.setOnClickListener {
                        it.findNavController()?.navigate(chatDirection)
                    }
                }
            }
        }
        myFirebaseUtils.registerTournamentListener(
            true,
            post.clubId,
            post.tournamentId,
            tournamentListener
        )

        // round status section
        holder.labelPlayers.text = "Completed games"
        holder.postTitle.text = "Round ${post.roundId} pairings available"

        this.gamesCompletedMap?.get(post.postId)?.let {
            it.observe(lifecycleOwner, Observer { completedGames ->
                val message = "( ${completedGames.completed} / ${completedGames.total} )"
                holder.playersCount.text = message
            })
        }

    }

    fun setLikesMap(likesMap: HashMap<String, FirebaseQueryLiveData<Long>>) {
        this.likesMap = likesMap
    }

    fun setUserLikesMap(userLikesMap: HashMap<String, FirebaseQueryLiveData<Boolean>>) {
        this.userLikesMap = userLikesMap
    }

    fun setChatItemsMap(chatItemsMap: HashMap<String, FirebaseQueryLiveData<Long>>) {
        this.chatItemsMap = chatItemsMap
    }

    fun setTournamentTotalPlayersMap(tournamentTotalPlayersMap: HashMap<String, FirebaseQueryLiveData<Long>>) {
        this.tournamentTotalPlayersMap = tournamentTotalPlayersMap
    }

    fun setGamesCompletedMap(gamesCompletedMap: HashMap<String, FirebaseQueryLiveData<HomeViewModel.CompletedGames>>) {
        this.gamesCompletedMap = gamesCompletedMap
    }
}