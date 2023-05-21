package eu.chessout.v2.ui.tournament.tournaments.rounds.state

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import eu.chessout.shared.model.Player
import eu.chessout.v2.R
import eu.chessout.v2.ui.tournament.tournaments.rounds.games.RoundGamesFragment
import eu.chessout.v2.ui.tournament.tournaments.rounds.players.RoundAbsentPlayersFragment
import kotlinx.android.synthetic.main.round_state_fragment.*

class RoundStateFragment : Fragment() {

    companion object {
        const val CLUB_ID = "clubId"
        const val TOURNAMENT_ID = "tournamentId"
        const val ROUND_ID = "roundId"
    }


    lateinit var mView: View
    private lateinit var absentPlayersFragment: RoundAbsentPlayersFragment
    private lateinit var gamesFragment: RoundGamesFragment
    private val viewModel: RoundStateViewModel by viewModels()
    private val myObserver = Observer<Boolean> { hasGames ->

        if (hasGames) {
            showGames()
        } else {
            showAbsentPlayers()
        }


    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView = inflater.inflate(R.layout.round_state_fragment, container, false)

        absentPlayersFragment = RoundAbsentPlayersFragment.newInstance(
            viewModel.clubId, viewModel.tournamentId, viewModel.roundId
        )
        gamesFragment = RoundGamesFragment.newInstance(
            viewModel.clubId,
            viewModel.tournamentId,
            viewModel.roundId
        );

        return mView
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        arguments?.let {
            decodeBundle(it)
        }
    }

    private fun decodeBundle(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            lateinit var clubId: String
            lateinit var tournamentId: String
            val roundId: Int

            it.getString(CLUB_ID)?.let { clubId = it }
            it.getString(TOURNAMENT_ID)?.let { tournamentId = it }
            roundId = it.getInt(ROUND_ID)

            viewModel.initialize(clubId, tournamentId, roundId)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        textView.text = "Round number ${viewModel.roundId}"


        viewModel.hasGames.observe(viewLifecycleOwner, myObserver)
    }

    fun getPresentPlayers(): List<Player> {
        return absentPlayersFragment.getPresentPlayers()
    }

    private fun showAbsentPlayers() {
        val transaction = childFragmentManager.beginTransaction();
        transaction.replace(R.id.stateContainerView, absentPlayersFragment)
        transaction.commit()
    }

    private fun showGames() {
        val transaction = childFragmentManager.beginTransaction();
        transaction.replace(R.id.stateContainerView, gamesFragment)
        transaction.commit()
    }
}