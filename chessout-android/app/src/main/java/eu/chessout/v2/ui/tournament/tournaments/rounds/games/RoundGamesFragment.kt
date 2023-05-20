package eu.chessout.v2.ui.tournament.tournaments.rounds.games

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import eu.chessout.shared.model.Game
import eu.chessout.v2.R
import eu.chessout.v2.util.GlideApp
import kotlinx.android.synthetic.main.round_games_fragment.*

class RoundGamesFragment : Fragment() {

    companion object {
        fun newInstance(clubId: String, tournamentId: String, roundId: Int) =
            RoundGamesFragment().apply {
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
    private val viewModel: RoundGamesViewModel by viewModels()
    lateinit var mView: View
    lateinit var mCompletedGames: TextView
    private val myListAdapter = RoundGamesAdapter(arrayListOf())
    private val listObserver = Observer<List<Game>> { list ->
        list?.let {
            myListAdapter.updateList(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            clubId = requireArguments().getString("clubId")!!
            tournamentId = requireArguments().getString("tournamentId")!!
            roundId = requireArguments().getInt("roundId")!!
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView = inflater.inflate(R.layout.round_games_fragment, container, false)
        mCompletedGames = mView.findViewById(R.id.completedGames)

        viewModel.initialize(clubId, tournamentId, roundId)

        viewModel.filteredList.observe(viewLifecycleOwner, listObserver)
        viewModel.liveGames.observe(viewLifecycleOwner, Observer<List<Game>> {
            mCompletedGames.text = viewModel.formatCompletedText(it)
        })


        viewModel.filter.observe(viewLifecycleOwner, Observer<Filter> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                updateTint(it)
            }
        })

        viewModel.showFilter.observe(viewLifecycleOwner, Observer<Boolean> { showFilter ->
            optionsImage.setOnClickListener {
                viewModel.showFilter.value = !showFilter
            }
            if (showFilter) {
                settingsGroup.visibility = View.VISIBLE
                GlideApp.with(requireContext())
                    .load(R.drawable.ic_more_vert_gray_24px)
                    .into(optionsImage)
            } else {
                settingsGroup.visibility = View.GONE
                updateCurrentOptionImage()
            }
        })

        myListAdapter.setIds(clubId, tournamentId, roundId)
        myListAdapter.setFragmentManger(childFragmentManager)
        myListAdapter.setLifeCycleOwner(viewLifecycleOwner)
        myListAdapter.context = requireContext()
        myListAdapter.setLivePlayersMap(viewModel.livePlayersMap)
        viewModel.isAdmin.observe(viewLifecycleOwner, Observer<Boolean> {
            myListAdapter.isAdmin = it;
        })
        val myRecyclerView = mView.findViewById<RecyclerView>(R.id.my_recycler_view)
        myRecyclerView?.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = myListAdapter
        }

        return mView
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        allCard.setOnClickListener { viewModel.updateFilterValue(Filter.ALL_GAMES) }
        completedCard.setOnClickListener { viewModel.updateFilterValue(Filter.COMPLETED_GAMES) }
        sillPlayingCard.setOnClickListener { viewModel.updateFilterValue(Filter.NOT_DECIDED_GAMES) }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun updateTint(filter: Filter) {
        imageAll.setColorFilter(
            requireContext().getColor(R.color.gray),
            android.graphics.PorterDuff.Mode.SRC_IN
        )
        imageCompleted.setColorFilter(
            requireContext().getColor(R.color.gray),
            android.graphics.PorterDuff.Mode.SRC_IN
        )
        imageStillPlaying.setColorFilter(
            requireContext().getColor(R.color.gray),
            android.graphics.PorterDuff.Mode.SRC_IN
        )

        when (filter) {
            Filter.ALL_GAMES -> imageAll.setColorFilter(
                requireContext().getColor(R.color.colorAccent),
                android.graphics.PorterDuff.Mode.SRC_IN
            )
            Filter.COMPLETED_GAMES -> imageCompleted.setColorFilter(
                requireContext().getColor(R.color.colorAccent),
                android.graphics.PorterDuff.Mode.SRC_IN
            )
            Filter.NOT_DECIDED_GAMES -> imageStillPlaying.setColorFilter(
                requireContext().getColor(R.color.colorAccent),
                android.graphics.PorterDuff.Mode.SRC_IN
            )
        }
    }

    private fun updateCurrentOptionImage() {
        val filter = viewModel.filter.value
        when (filter) {
            Filter.ALL_GAMES -> GlideApp.with(requireContext())
                .load(R.drawable.ic_smile_regular_gray)
                .into(optionsImage)
            Filter.COMPLETED_GAMES -> GlideApp.with(requireContext())
                .load(R.drawable.ic_smile_wink_regular_gray)
                .into(optionsImage)
            Filter.NOT_DECIDED_GAMES -> GlideApp.with(requireContext())
                .load(R.drawable.ic_grin_beam_sweat_regular_gray)
                .into(optionsImage)

        }
    }
}
