package eu.chessout.v2.ui.tournament.tournaments.players

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import eu.chessdata.chesspairing.importexport.Swar
import eu.chessout.shared.Constants
import eu.chessout.shared.model.RankedPlayer
import eu.chessout.v2.R
import eu.chessout.v2.ui.tournament.tournaments.addplayers.TournamentAddPlayersDialog
import kotlinx.android.synthetic.main.tournament_players_fragment.*
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.util.*

const val PICK_JSON_FILE = 1006

class TournamentPlayersFragment() : Fragment() {

    private val viewModel: TournamentPlayersViewModel by viewModels()
    private val args: TournamentPlayersFragmentArgs by navArgs()
    private val myListAdapter = TournamentPlayersAdapter(arrayListOf())
    private val myObserver = Observer<List<RankedPlayer>> { list ->
        list?.let {
            my_recycler_view.visibility = View.VISIBLE
            myListAdapter.updateList(it)
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val mView = inflater.inflate(R.layout.tournament_players_fragment, container, false)
        setHasOptionsMenu(true)

        viewModel.isAdmin.observe(viewLifecycleOwner, Observer { isAdmin ->
            if (isAdmin) {
                fab.visibility = View.VISIBLE
            } else {
                fab.visibility = View.GONE
            }
        })
        viewModel.liveRankedPlayer.observe(viewLifecycleOwner, myObserver)
        viewModel.initializeModel(args.clubId, args.tournamentId)
        myListAdapter.setContextValues(
            requireContext(),
            childFragmentManager,
            args.clubId,
            args.tournamentId,
            viewModel.liveClubPlayersMap,
            viewLifecycleOwner
        )
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
            TournamentAddPlayersDialog(
                viewModel.getClubId(),
                viewModel.getTournamentId(),
                viewModel.getMissingPlayers()
            ).show(childFragmentManager, "TournamentAddPlayersFragment")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.tournament_players_menu, menu)
        val exportPlayersToSwarCsv = menu.findItem(R.id.exportPlayersToSwarCsv);
        if (null != exportPlayersToSwarCsv) {
            exportPlayersToSwarCsv.isVisible = false
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.exportPlayersToSwarCsv -> {
                if (!viewModel.isAdmin.value!!) {
                    Toast.makeText(
                        requireContext(), "Sorry. Only club admins can do this",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val content = viewModel.generateAndGetSWarTournament()

                    //save to file
                    val fileName = "exportFile" + Date().getTime() + ".csv"
                    val out = requireContext().openFileOutput(fileName, Context.MODE_PRIVATE)
                    out.write(content.toByteArray())
                    out.close()

                    //exporting
                    val file = File(requireContext().filesDir, fileName)
                    val path = FileProvider.getUriForFile(
                        requireContext(),
                        "eu.chessout.v2.fileprovider",
                        file
                    )
                    val fileIntent = Intent(Intent.ACTION_SEND)
                    fileIntent.type = "text/csv";
                    val time = Date().time
                    fileIntent.putExtra(Intent.EXTRA_SUBJECT, "ChessoutExport$time.csv")
                    fileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    fileIntent.putExtra(Intent.EXTRA_STREAM, path)
                    startActivity(Intent.createChooser(fileIntent, "Export tournament data"))

                }
                return true;
            }
            R.id.importPlayersFromSwarJson -> {
                if (!viewModel.isAdmin.value!!) {
                    Toast.makeText(
                        requireContext(), "Sorry. Only club admins can do this",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    openFile()
                }
                return true;
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

        startActivityForResult(intent, PICK_JSON_FILE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //super.onActivityResult(requestCode, resultCode, data)
        Log.d(Constants.LOG_TAG, "Request code: $resultCode")
        when (requestCode) {
            PICK_JSON_FILE -> {
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
                    viewModel.importFromChesspairingTournament(importTournament)
                }
            }
        }
    }
}
