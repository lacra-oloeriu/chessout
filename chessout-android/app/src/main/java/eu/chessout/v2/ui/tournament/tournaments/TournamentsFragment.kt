package eu.chessout.v2.ui.tournament.tournaments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import com.firebase.ui.database.FirebaseListAdapter
import com.firebase.ui.database.FirebaseListOptions
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import eu.chessout.shared.Constants
import eu.chessout.shared.model.Tournament
import eu.chessout.v2.R
import eu.chessout.v2.ui.tournament.tournaments.dialogs.TournamentCreateDialogFragment
import kotlinx.android.synthetic.main.tournaments_fragment.*

class TournamentsFragment : Fragment() {

    companion object {
        fun newInstance() = TournamentsFragment()
    }

    private lateinit var viewModel: TournamentsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.tournaments_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val model: TournamentsViewModel by viewModels()
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

        viewModel.initializeModel()

        fab.setOnClickListener { view ->
            val clubKey = viewModel.getClubKey().value!!
            TournamentCreateDialogFragment(clubKey).show(
                childFragmentManager,
                "TournamentCreateDialogFragment"
            )
        }

        viewModel.getClubKey().observe(viewLifecycleOwner, Observer { clubKey ->
            run {
                val adapter = getAdaptor(clubKey)
                list_view_tournaments.adapter = adapter
            }
        })
    }

    private fun getAdaptor(clubKey: String): FirebaseListAdapter<Tournament> {

        //firebase reference
        val locTournaments: String = Constants.LOCATION_TOURNAMENTS
            .replace(Constants.CLUB_KEY, clubKey)
        val mReference =
            FirebaseDatabase.getInstance().getReference(locTournaments)

        val query: Query =
            mReference.orderByChild("reversedDateCreated") //name

        val options = FirebaseListOptions.Builder<Tournament>()
            .setLifecycleOwner(this)
            .setLayout(R.layout.list_item_text)
            .setQuery(query, Tournament::class.java)
            .build()

        val adapter: FirebaseListAdapter<Tournament> =
            object : FirebaseListAdapter<Tournament>(options) {
                override fun populateView(v: View, tournament: Tournament, position: Int) {
                    val textView: TextView =
                        (v.findViewById<View>(R.id.list_item_text_simple_view)) as TextView
                    textView.text = tournament.name
                    textView.setOnClickListener {
                        textView.findNavController().navigate(
                            TournamentsFragmentDirections
                                .actionTournamentsNavigationToTournamentDashboardFragment(
                                    tournament.tournamentId,
                                    tournament.clubId,
                                    tournament.totalRounds
                                )
                        )
                    }
                }
            }
        return adapter
    }
}
