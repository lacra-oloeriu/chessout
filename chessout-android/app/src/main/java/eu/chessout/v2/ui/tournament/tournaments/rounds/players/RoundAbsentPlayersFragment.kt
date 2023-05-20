package eu.chessout.v2.ui.tournament.tournaments.rounds.players

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import eu.chessout.shared.Constants
import eu.chessout.shared.model.Player
import eu.chessout.v2.R
import kotlinx.android.synthetic.main.round_absent_players_fragment.*

class RoundAbsentPlayersFragment : Fragment() {

    companion object {
        fun newInstance(clubId: String, tournamentId: String, roundId: Int) =
            RoundAbsentPlayersFragment().apply {
                arguments = Bundle().apply {
                    putString("clubId", clubId)
                    putString("tournamentId", tournamentId)
                    putInt("roundId", roundId)
                }
            }
    }

    lateinit var clubId: String
    lateinit var tournamentId: String
    var roundId: Int = -1
    lateinit var mView: View
    private val viewModel: RoundAbsentPlayersViewModel by viewModels()
    private val myListAdapter = RoundAbsentPlayersAdapter(arrayListOf())
    private val myObserver = Observer<List<Player>> { list ->
        list?.let {
            my_recycler_view.visibility = View.VISIBLE
            myListAdapter.updateList(it)
        }
    }
    lateinit var mMenu: Menu

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            clubId = requireArguments().getString("clubId")!!
            tournamentId = requireArguments().getString("tournamentId")!!
            roundId = requireArguments().getInt("roundId")!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView = inflater.inflate(R.layout.round_absent_players_fragment, container, false)

        myListAdapter.setContextValues(childFragmentManager, clubId, tournamentId, roundId)
        viewModel.liveMissingPlayers.observe(viewLifecycleOwner, myObserver)
        viewModel.isAdmin.observe(viewLifecycleOwner, Observer { admin ->
            myListAdapter.setIsAdmin(admin)
            if (admin) {
                fab.visibility = View.VISIBLE
            } else {
                fab.visibility = View.GONE
            }
        })
        viewModel.initialize(clubId, tournamentId, roundId)

        val myRecyclerView = mView.findViewById<RecyclerView>(R.id.my_recycler_view)
        myRecyclerView?.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = myListAdapter
        }

        return mView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        fab.setOnClickListener {
            Log.d(
                Constants.LOG_TAG,
                "Total tournament players = ${viewModel.tournamentPlayers.size}"
            )
        }
        mView.onFocusChangeListener = View.OnFocusChangeListener { _, isFocused ->

            val menuItem = mMenu.findItem(R.id.addAbsentPlayers)
            menuItem.isVisible = isFocused
        }
        fab.setOnClickListener { viewModel.generateGames() }
    }

    fun getPresentPlayers(): List<Player> {
        return viewModel.getPresentPlayers()
    }
}