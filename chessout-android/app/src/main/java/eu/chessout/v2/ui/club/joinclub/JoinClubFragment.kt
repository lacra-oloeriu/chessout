package eu.chessout.v2.ui.club.joinclub

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import eu.chessout.shared.model.Club
import eu.chessout.v2.MainActivity
import eu.chessout.v2.R
import kotlinx.android.synthetic.main.fragment_join_club.*

class JoinClubFragment() : Fragment() {

    private lateinit var mView: View
    private lateinit var joinClubModel: JoinClubModel
    private val myListAdapter = JoinClubAdapter(arrayListOf())

    private val myObserver = Observer<List<Club>> { list ->
        list?.let {
            my_recycler_view.visibility = View.VISIBLE
            myListAdapter.updateArrayList(it)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        mView = inflater.inflate(R.layout.fragment_join_club, container, false)

        val model: JoinClubModel by viewModels()
        joinClubModel = model
        joinClubModel.liveClubs.observe(viewLifecycleOwner, myObserver)
        joinClubModel.initializeModel()

        val myRecyclerView = mView.findViewById<RecyclerView>(R.id.my_recycler_view)
        myRecyclerView?.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = myListAdapter
        }

        return mView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        fab.setOnClickListener { view ->
            view.findNavController().navigate(
                JoinClubFragmentDirections
                    .actionNavigationJoinClubFragmentToNavigationMyClubsFragment()
            )
        }
    }

    override fun onStart() {
        super.onStart()
        val activity = activity as MainActivity
        activity.hideBottomNav()
    }

    override fun onStop() {
        super.onStop()
        val activity = activity as MainActivity
        activity.showBottomNav()
    }
}