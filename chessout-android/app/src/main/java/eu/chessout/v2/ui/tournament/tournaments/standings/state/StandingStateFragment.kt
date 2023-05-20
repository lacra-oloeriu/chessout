package eu.chessout.v2.ui.tournament.tournaments.standings.state

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import eu.chessout.shared.model.RankedPlayer
import eu.chessout.v2.R
import kotlinx.android.synthetic.main.standing_state_fragment.*

class StandingStateFragment() : Fragment() {

    companion object {

        fun newInstance(
            clubId: String,
            tournamentId: String,
            roundId: Int
        ) = StandingStateFragment().apply {
            arguments = Bundle(3).apply {
                putString("clubId", clubId)
                putString("tournamentId", tournamentId)
                putInt("roundId", roundId)
            }
        }
    }

    private val viewModel: StandingStateViewModel by viewModels()
    private lateinit var clubId: String
    private lateinit var tournamentId: String
    private var roundId = -1
    private val myListAdapter = StandingRankedPlayerAdapter(arrayListOf())
    private val hasStandingsObserver = Observer<Boolean> { hasStandings ->
        if (hasStandings) {
            showStandings()
        } else {
            showNoStandingsMessage()
        }
    }
    private val myObserver = Observer<List<RankedPlayer>> { list ->
        list?.let {
            myListAdapter.updateList(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val mView = inflater.inflate(R.layout.standing_state_fragment, container, false)

        arguments?.let {
            clubId = it.getString("clubId")!!
            tournamentId = it.getString("tournamentId")!!
            roundId = it.getInt("roundId")!!
        }

        viewModel.liveRankedPlayers.observe(viewLifecycleOwner, myObserver)
        viewModel.hasStandings.observe(viewLifecycleOwner, hasStandingsObserver)
        myListAdapter.setContext(requireContext())
        viewModel.initModel(clubId, tournamentId, roundId)
        val myRecyclerView = mView.findViewById<RecyclerView>(R.id.my_recycler_view)
        myRecyclerView?.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = myListAdapter
        }

        return mView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        roundNumber.text = "Round number ${roundId.toString()}"
    }

    private fun showNoStandingsMessage() {

        my_recycler_view.visibility = View.GONE;
    }

    private fun showStandings() {
        my_recycler_view.visibility = View.VISIBLE;
    }
}
