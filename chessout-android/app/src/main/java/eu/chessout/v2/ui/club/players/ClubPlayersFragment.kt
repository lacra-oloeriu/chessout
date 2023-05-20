package eu.chessout.v2.ui.club.players

import android.os.Bundle
import android.view.*
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import eu.chessout.shared.model.Player
import eu.chessout.v2.R
import kotlinx.android.synthetic.main.club_players_fragment.*
import java.util.*

class ClubPlayersFragment : Fragment() {

    private val args: ClubPlayersFragmentArgs by navArgs()
    private lateinit var viewModel: ClubPlayersViewModel

    private val myListAdapter = ClubPlayersAdapter(arrayListOf())
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

        val mView = inflater.inflate(R.layout.club_players_fragment, container, false)
        setHasOptionsMenu(true)
        val model: ClubPlayersViewModel by viewModels()
        viewModel = model

        viewModel.isAdmin.observe(viewLifecycleOwner, Observer { isAdmin ->
            run {
                if (isAdmin) {
                    fab.visibility = View.VISIBLE
                } else {
                    fab.visibility = View.GONE
                }
            }
        })
        viewModel.filteredPlayerList.observe(viewLifecycleOwner, myObserver)
        viewModel.clubKey.observe(viewLifecycleOwner, Observer {
            myListAdapter.setClubId(it)
        })
        viewModel.initializeModel(args.archivedPlayers)
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

        fab.setOnClickListener {
            val clubKey = viewModel.getClubKey().value!!
            ClubPlayerCreateDialogFragment(clubKey).show(
                childFragmentManager,
                "ClubPlayerCreateDialogFragment"
            )
        }
        if (args.archivedPlayers) {
            topText.text = "Archived club players"
        } else {
            topText.text = "Club players"
        }

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        inflater.inflate(R.menu.club_players_menu, menu);
        val searchMenu = menu.findItem(R.id.club_player_search)
        val searchView = searchMenu?.actionView as SearchView
        searchView.queryHint = "Search players";
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.updateFilterValue(Optional.ofNullable(newText))

                return true
            }

        })
        super.onCreateOptionsMenu(menu, inflater)
    }
}
