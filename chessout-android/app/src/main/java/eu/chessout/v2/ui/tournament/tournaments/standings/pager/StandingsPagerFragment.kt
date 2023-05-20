package eu.chessout.v2.ui.tournament.tournaments.standings.pager

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import eu.chessdata.chesspairing.importexport.Swar
import eu.chessout.v2.R
import eu.chessout.v2.ui.tournament.tournaments.rounds.pager.ZoomOutPageTransformer
import eu.chessout.v2.ui.tournament.tournaments.standings.state.StandingStateFragment
import kotlinx.android.synthetic.main.standings_pager_fragment.*
import java.io.BufferedReader
import java.io.FileInputStream

const val STANDINGS_PAGER_OPEN_FILE = 1008

class StandingsPagerFragment : Fragment() {

    companion object {
        fun newInstance() = StandingsPagerFragment()
    }

    private val viewModel: StandingsPagerViewModel by viewModels()
    private val args: StandingsPagerFragmentArgs by navArgs()
    private lateinit var mView: View
    val stateFragments = HashMap<Int, StandingStateFragment>()

    lateinit var pagerAdapter: ScreenSlidePagerAdapter
    private val myObserver = Observer<Int> {
        pagerAdapter.updateCount(it)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView = inflater.inflate(R.layout.standings_pager_fragment, container, false)
        setHasOptionsMenu(true)
        viewModel.visibleRoundsCount.observe(viewLifecycleOwner, myObserver)
        viewModel.initModel(args.clubId, args.tournamentId, args.totalRounds)
        return mView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        pagerAdapter = ScreenSlidePagerAdapter(
            this.requireActivity(),
            viewModel.visibleRoundsCount.value!!.toInt()
        )
        standingsPager.adapter = pagerAdapter
        standingsPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                viewModel.setPosition(position)
            }
        })
        standingsPager.setPageTransformer(ZoomOutPageTransformer())
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.standings_option_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.importStandingsFromSwar -> {
                if (!viewModel.isAdmin.value!!) {
                    Toast.makeText(
                        requireContext(), "Only admins can do this"
                        , Toast.LENGTH_SHORT
                    ).show()
                    return true
                } else {
                    openFile()
                }
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun openFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"

            // Optionally, specify a URI for the file that should appear in the
            // system file picker when it loads.
            //putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)
        }

        startActivityForResult(intent, STANDINGS_PAGER_OPEN_FILE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            STANDINGS_PAGER_OPEN_FILE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val uri = data?.data
                    val fileDescriptor = requireContext().contentResolver.openFileDescriptor(
                        uri!!,
                        "r"
                    )?.fileDescriptor
                    val fileInputStream = FileInputStream(fileDescriptor);

                    val reader = BufferedReader(fileInputStream.reader())
                    var content = ""
                    reader.use { reader ->
                        content = reader.readText()
                    }

                    val swar = Swar.newInstance();
                    val importTournament = swar.buildFromString(content);
                    viewModel.importStandingsFromChesspairingTournament(importTournament, swar)
                }
            }
        }
    }

    inner class ScreenSlidePagerAdapter(fa: FragmentActivity, private var countValue: Int) :
        FragmentStateAdapter(fa) {

        fun updateCount(newCount: Int) {
            this.countValue = newCount
            notifyDataSetChanged()
        }

        override fun getItemCount(): Int = countValue

        override fun createFragment(position: Int): Fragment {
            val roundId = position + 1;
            val stateFragment = StandingStateFragment.newInstance(
                args.clubId, args.tournamentId, roundId
            )
            stateFragments[position] = stateFragment
            return stateFragment
        }
    }

}
