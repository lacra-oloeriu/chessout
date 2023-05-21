package eu.chessout.v2.ui.club.myclubs

//import com.firebase.ui.database.FirebaseListAdapter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import eu.chessout.shared.Constants
import eu.chessout.v2.R
import eu.chessout.v2.R.layout
import eu.chessout.v2.util.ImageUtil
import kotlinx.android.synthetic.main.my_clubs_fragment.*


class MyClubsFragment : Fragment() {

    private lateinit var mApp: FirebaseApp
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mUser: FirebaseUser

    private val myListAdapter = MyClubsAdapter(arrayListOf())

    private val viewModel: MyClubsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val mView = inflater.inflate(layout.my_clubs_fragment, container, false)

        mApp = FirebaseApp.getInstance()
        mAuth = FirebaseAuth.getInstance()
        mUser = mAuth.currentUser!!

        viewModel.liveClubList.observe(viewLifecycleOwner, Observer {
            myListAdapter.updateList(it)
        })

        viewModel.initializeModel()
        myListAdapter.setContext(requireContext())
        val myRecyclerView = mView.findViewById<RecyclerView>(R.id.myRecyclerView)
        myRecyclerView?.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = myListAdapter
        }
        return mView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // configure fab
        fab.setOnClickListener { view ->
            if (ImageUtil.askForCameraPermissions(requireActivity())) {
                val qrDirection = MyClubsFragmentDirections
                    .actionNavigationMyClubsFragmentToJoinClubByQrCodeFragment()
                view.findNavController()?.navigate(qrDirection)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d(
            Constants.LOG_TAG, "Request code = $requestCode"
        )
    }
}
