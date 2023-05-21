package eu.chessout.v2.ui.club.clubfollowers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import eu.chessout.v2.R

class ClubFollowersFragment : Fragment() {

    companion object {
        fun newInstance() = ClubFollowersFragment()
    }

    private val viewModel: ClubFollowersViewModel by viewModels()
    private val args: ClubFollowersFragmentArgs by navArgs()
    private val myListAdapter = ClubFollowersAdapter(arrayListOf())
    private val myObserver = Observer<List<String>> { list ->
        list?.let {
            myListAdapter.updateList(list)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val mView = inflater.inflate(R.layout.club_followers_fragment, container, false)

        viewModel.initializeModel(args.clubId)
        viewModel.liveUserIdList.observe(viewLifecycleOwner, myObserver)
        myListAdapter.setContextValues(
            requireContext(),
            childFragmentManager,
            args.clubId,
            viewModel.liveUserMapInfo,
            viewLifecycleOwner
        )
        val myRecyclerView = mView.findViewById<RecyclerView>(R.id.my_followers_recycler_view)
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