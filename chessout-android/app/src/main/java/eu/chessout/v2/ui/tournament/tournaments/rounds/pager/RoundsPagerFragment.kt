package eu.chessout.v2.ui.tournament.tournaments.rounds.pager

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import eu.chessdata.chesspairing.importexport.Swar
import eu.chessout.shared.Constants
import eu.chessout.shared.model.Game
import eu.chessout.v2.R
import eu.chessout.v2.ui.tournament.tournaments.rounds.players.RoundAddAbsentPlayersDialog
import eu.chessout.v2.ui.tournament.tournaments.rounds.players.RoundDeleteGamesDialog
import eu.chessout.v2.ui.tournament.tournaments.rounds.state.RoundStateFragment
import kotlinx.android.synthetic.main.rounds_pager_fragment.roundsPager
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date


const val ROUNDS_PAGER_OPEN_FILE = 1007
const val OPEN_DIRECTORY_TO_EXPORT_SWAR_FILE = 1008

class RoundsPagerFragment : Fragment() {


    private val args: RoundsPagerFragmentArgs by navArgs()
    private lateinit var mView: View
    lateinit var viewModel: RoundPagerViewModel
    val stateFragments = HashMap<Int, RoundStateFragment>()
    lateinit var pagerAdapter: ScreenSlidePagerAdapter
    private val myObserver = Observer<Int> {
        pagerAdapter.updateCount(it)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView = inflater.inflate(R.layout.rounds_pager_fragment, container, false)
        setHasOptionsMenu(true)
        val model: RoundPagerViewModel by viewModels()
        viewModel = model
        viewModel.visibleRoundsCount.observe(viewLifecycleOwner, myObserver)
        viewModel.initializeModel(args.clubId, args.tournamentId, args.totalRounds, args.roundId)
        return mView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        pagerAdapter = ScreenSlidePagerAdapter(
            this.requireActivity(),
            viewModel.visibleRoundsCount.value!!.toInt()
        )
        roundsPager.adapter = pagerAdapter
        Handler().postDelayed({
            roundsPager.setCurrentItem(args.roundId, true)
        }, 100)
        roundsPager.setPageTransformer(ZoomOutPageTransformer())
        roundsPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                viewModel.setPosition(position)
            }
        })

    }

    inner class ScreenSlidePagerAdapter(fa: FragmentActivity, private var countValue: Int) :
        FragmentStateAdapter(fa) {

        fun updateCount(newCount: Int) {
            this.countValue = newCount
            Handler().post {
                notifyDataSetChanged()
            }
        }

        override fun getItemCount(): Int = countValue

        override fun createFragment(position: Int): Fragment {
            val roundId = position + 1;
            val stateFragment = RoundStateFragment().apply {
                arguments = Bundle().apply {
                    putString(RoundStateFragment.CLUB_ID, args.clubId)
                    putString(RoundStateFragment.TOURNAMENT_ID, args.tournamentId)
                    putInt(RoundStateFragment.ROUND_ID, roundId)
                }
            }
            stateFragments[position] = stateFragment
            return stateFragment
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.round_absent_players_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.addAbsentPlayers -> {
                if (!viewModel.isAdmin.value!!) {
                    Toast.makeText(
                        requireContext(), "Only admins can add absent players", Toast.LENGTH_SHORT
                    ).show()
                    return true
                }
                if (viewModel.roundHasGames(viewModel.position.value!! + 1)) {
                    Toast.makeText(
                        requireContext(),
                        "Not allowed to register absent players anymore",
                        Toast.LENGTH_SHORT
                    ).show()
                    return true
                }

                val position = viewModel.position.value!!
                Log.d(Constants.LOG_TAG, "Present players for $position")
                val players = stateFragments[viewModel.position.value!!]!!
                    .getPresentPlayers()
                val roundId = viewModel.position.value!! + 1
                RoundAddAbsentPlayersDialog(
                    args.clubId,
                    args.tournamentId,
                    roundId,
                    players
                ).show(childFragmentManager, "RoundAddAbsentPlayersDialog")
                return true

            }
            R.id.importRoundGamesFromSwar -> {
                if (!viewModel.isAdmin.value!!) {
                    Toast.makeText(
                        requireContext(), "Only admins can do this", Toast.LENGTH_SHORT
                    ).show()
                    return true
                } else {
                    openFile()
                }


                return true
            }
            R.id.deleteGames -> {
                if (!viewModel.isAdmin.value!!) {
                    Toast.makeText(
                        requireContext(), "Only admins can do this", Toast.LENGTH_SHORT
                    ).show()
                } else {
                    RoundDeleteGamesDialog(
                        args.clubId,
                        args.tournamentId,
                        args.roundId
                    ).show(childFragmentManager, "RoundDeleteGamesDialog")
                }
                return true
            }
            R.id.exportRoundResultsToSwar -> {
                exportRoundResultsToSwar()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun exportRoundResultsToSwar() {

        val roundValue = 1 + viewModel.position.value!!

        val gameList = viewModel.roundGamesMap[roundValue]

        val sb = java.lang.StringBuilder()
        sb.append("[RESULTATS]\n[ROUND] $roundValue\n\n")

        val date = Date();
        val dateFormatter = SimpleDateFormat("dd/MM/YYYY")
        val formattedDate = dateFormatter.format(date);
        val tournamentName = viewModel.mutableTournamentName.value;
        sb.append("//Tournament: $tournamentName\n")
        sb.append("//Date: $formattedDate\n\n")

        gameList?.forEach { game ->
            run {
                if (game.result != 0 || game.result != 4) {
                    val swarResult = convertGameResultToSwarCsvResult(game)
                    swarResult?.let {
                        sb.append("${game.actualNumber};$swarResult\n")
                    }
                }
            }
        }
        val exportBytes = sb.toString();
        val downloads =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val fileName = "ChessoutToSwarExport$tournamentName-Round-$roundValue.csv";
        File(downloads, fileName).writeText(exportBytes);
        Toast.makeText(
            requireContext(),
            "$fileName complete. Open downloads folder to view the file",
            Toast.LENGTH_SHORT
        ).show()
    }

    fun openFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"

            // Optionally, specify a URI for the file that should appear in the
            // system file picker when it loads.
            //putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)
        }

        startActivityForResult(intent, ROUNDS_PAGER_OPEN_FILE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //super.onActivityResult(requestCode, resultCode, data)
        Log.d(Constants.LOG_TAG, "Request code: $resultCode")
        when (requestCode) {
            ROUNDS_PAGER_OPEN_FILE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val uri = data?.data
                    Log.d(Constants.LOG_TAG, "Uri: ${uri.toString()}")
                    val fileDescriptor = requireContext().contentResolver.openFileDescriptor(
                        uri!!,
                        "r"
                    )?.fileDescriptor
                    val fileInputStream = FileInputStream(fileDescriptor);

                    val reader = BufferedReader(fileInputStream.reader())
                    var content: String = ""
                    reader.use { reader ->
                        content = reader.readText()
                    }

                    val swar = Swar.newInstance();
                    val importTournament = swar.buildFromString(content);
                    viewModel.importRoundFromChesspairingTournament(importTournament, swar)
                }
            }
            OPEN_DIRECTORY_TO_EXPORT_SWAR_FILE -> {
                val uri = data?.data
                Log.d(Constants.LOG_TAG, "Uri: ${uri.toString()}")
                val gameList = viewModel.roundGamesMap[args.roundId]

                val sb = java.lang.StringBuilder()
                gameList?.forEach { game ->
                    run {

                        sb.append("${game.actualNumber}, result=${game.result}")
                    }
                }
                var filePath = this.context?.filesDir?.path + "/myFile.txt";
                uri
                var fileOutputStream = FileOutputStream(uri.toString() + "/myFile.txt");
                fileOutputStream.write(sb.toString().toByteArray())
                Log.d(Constants.LOG_TAG, "End of debug")
            }
        }
    }

    fun convertGameResultToSwarCsvResult(game: Game): String? {
        /**
         * 0 still not decided
         * 1 white player wins
         * 2 black player wins
         * 3 draw game
         * 4 bye
         * 5 white wins by forfeit
         * 6 black wins by forfeit
         * 7 double forfeit
         */
        when (game.result) {
            // 0 -> return "" //not decided
            1 -> return "1;0" //white player wins
            2 -> return "0;1" //black player wins
            3 -> return "5;5" //draw game
            4 -> return null //bye and swar does not support by
            5 -> return "1ff;0" // white wins by forfeit
            6 -> return "0;1ff" // black wins by forfeit
            7 -> return "5ff;5ff" // double forfeit
        }
        throw java.lang.IllegalStateException("Not supported result ${game.result}")
    }


}