package eu.chessout.v2.ui.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import eu.chessout.shared.model.Post
import eu.chessout.v2.R
import eu.chessout.v2.util.SharedPreferencesHelper

class HomeFragment : Fragment() {

    private val homeViewModel: HomeViewModel by viewModels()
    private val listAdapter = PostsAdapter(arrayListOf())
    private val listObserver = Observer<List<Post>> {
        listAdapter.updateList(it)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val mView = inflater.inflate(R.layout.fragment_home, container, false)

        val sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE)
        val clubId = SharedPreferencesHelper.getDefaultClub(sharedPref)
        clubId?.let {
            if (clubId != SharedPreferencesHelper.NO_DEFAULT_CLUB) {
                homeViewModel.initializeModel()
                homeViewModel.livePostList.observe(viewLifecycleOwner, listObserver)
                listAdapter.setLifeCycleOwner(viewLifecycleOwner)
                listAdapter.setContext(requireContext())
                listAdapter.setLikesMap(homeViewModel.likesMap)
                listAdapter.setChatItemsMap(homeViewModel.chatItemsMap)
                listAdapter.setUserLikesMap(homeViewModel.userLikedMap)
                listAdapter.setTournamentTotalPlayersMap(homeViewModel.tournamentTotalPlayers)
                listAdapter.setGamesCompletedMap(homeViewModel.gamesCompletedMap)
            }
        }

        val recycler = mView.findViewById<RecyclerView>(R.id.postsRecycler)
        recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = listAdapter
        }
        return mView
    }

    override fun onStart() {
        super.onStart()
        setPostType()
    }

    override fun onResume() {
        super.onResume()
        setPostType()
    }

    private fun setPostType() {
        val sharePref = requireActivity().getPreferences(Context.MODE_PRIVATE)
        SharedPreferencesHelper.setPostType(sharePref.edit(), Post.PostType.USER_POST)
    }
}