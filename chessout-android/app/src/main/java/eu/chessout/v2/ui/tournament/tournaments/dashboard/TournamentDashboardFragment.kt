package eu.chessout.v2.ui.tournament.tournaments.dashboard

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import eu.chessout.v2.R
import kotlinx.android.synthetic.main.tournament_dashboard_fragment.*

class TournamentDashboardFragment : Fragment() {

    private val viewModel: TournamentDashboardViewModel by viewModels()
    private val args: TournamentDashboardFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val mView = inflater.inflate(R.layout.tournament_dashboard_fragment, container, false)
        viewModel.initializeModel(args.tournamentId, args.clubId)
        viewModel.roundNavData.observe(viewLifecycleOwner, Observer<RoundNavData> { roundNavData ->
            tournamentRoundsCard.setOnClickListener { view ->
                val action =
                    TournamentDashboardFragmentDirections
                        .actionTournamentDashboardFragmentToRoundPagerFragment(
                            args.clubId,
                            args.tournamentId,
                            args.totalRounds,
                            roundNavData.navigateToRound
                        )
                view.findNavController().navigate(action)
            }
        })
        return mView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)



        tournamentPlayersCard.setOnClickListener { view ->
            val tournamentKey = args.tournamentId
            val clubKey = args.clubId
            val action =
                TournamentDashboardFragmentDirections
                    .actionTournamentDashboardFragmentToTournamentPlayersFragment(
                        tournamentKey, clubKey
                    )
            Handler().post { view.findNavController().navigate(action) }
        }



        tournamentStandingsCard.setOnClickListener { view ->
            val clubId = args.clubId
            val tournamentId = args.tournamentId
            val totalRounds = args.totalRounds

            val action =
                TournamentDashboardFragmentDirections
                    .actionTournamentDashboardFragmentToStangingsPagerFragment(
                        clubId, tournamentId, totalRounds
                    )
            view.findNavController().navigate(action)
        }
    }

}
