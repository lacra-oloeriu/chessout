package eu.chessout.v2.ui.club.followedplayers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import eu.chessout.shared.model.Player
import eu.chessout.v2.R
import kotlinx.android.synthetic.main.followed_players_fragment.*

class FollowedPlayersFragment : Fragment() {

    companion object {
        fun newInstance() = FollowedPlayersFragment()
    }

    private lateinit var viewModel: FollowedPlayersViewModel
    private val myListAdapter = FollowedPlayerAdapter(mutableListOf())
    private val myObserver = Observer<List<Player>> { list ->
        list?.let {
            my_recycler_view.visibility = View.VISIBLE
            myListAdapter.updateList(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val mView = inflater.inflate(R.layout.followed_players_fragment, container, false);

        val model: FollowedPlayersViewModel by viewModels()
        viewModel = model

        viewModel.livePlayerList.observe(viewLifecycleOwner, myObserver)

        viewModel.initializeModel()

        myListAdapter.setFragmentManager(childFragmentManager)
        myListAdapter.setContext(requireContext())

        val myRecyclerView = mView.findViewById<RecyclerView>(R.id.my_recycler_view)
        myRecyclerView?.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = myListAdapter
        }

        return mView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

    }

}