package eu.chessout.v2.ui.dashboard02

import android.app.Dialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import eu.chessout.shared.Constants
import eu.chessout.shared.dao.BasicApiResponse
import eu.chessout.shared.model.MyPayLoad
import eu.chessout.shared.model.Post
import eu.chessout.v2.R
import eu.chessout.v2.model.BasicApiService
import eu.chessout.v2.ui.club.ClubCreateDialogFragment
import eu.chessout.v2.util.GlideApp
import eu.chessout.v2.util.MyFirebaseUtils
import eu.chessout.v2.util.SharedPreferencesHelper
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_dashboard02.*

class Dashboard02Fragment : Fragment() {


    private lateinit var viewModel: Dashboard02ViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard02, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val model: Dashboard02ViewModel by viewModels()
        viewModel = model

        create_club_card.setOnClickListener {
            ClubCreateDialogFragment().show(childFragmentManager, "ClubCreateDialogFragment")
        }

        my_clubs_card.setOnClickListener { view ->
            view.findNavController()?.navigate(R.id.navigation_my_clubs_fragment)
        }

        tournaments_card.setOnClickListener { view ->

            val action = Dashboard02FragmentDirections
                .actionNavigationDashboard02ToTournamentsNavigation()
            view.findNavController()?.navigate(action)
        }

        players_card.setOnClickListener { view ->
            run {
                val action =
                    Dashboard02FragmentDirections.actionNavigationDashboard02ToClubPlayersFragment(
                        false
                    )
                view.findNavController()?.navigate(action)
            }
        }
        archivedPlayers.setOnClickListener { view ->
            val action =
                Dashboard02FragmentDirections.actionNavigationDashboard02ToClubPlayersFragment(
                    true
                )
            view.findNavController()?.navigate(action)
        }

        myProfile.setOnClickListener {
            val userId = viewModel.userId.value!!
            val action =
                Dashboard02FragmentDirections.actionNavigationDashboard02ToUserDashboardFragment(
                    userId
                )
            it.findNavController()?.navigate(action)
        }

        myClubCard.setOnClickListener {
            val action =
                Dashboard02FragmentDirections.actionNavigationDashboard02ToClubDashboardFragment(
                    viewModel.myClub.value?.clubId!!
                )
            it.findNavController()?.navigate(action)
        }



        viewModel.myClubCreated.observe(viewLifecycleOwner, Observer { isMyClbCreated ->
            run {
                if (isMyClbCreated) {
                    // no need to create
                    create_club_card.visibility = View.GONE
                    myClubCard.visibility = View.VISIBLE
                } else {
                    // user should be allowed to create
                    create_club_card.visibility = View.VISIBLE
                    myClubCard.visibility = View.GONE
                }
            }
        })

        viewModel.isInDebugMode.observe(viewLifecycleOwner, Observer { isInDebugMode ->
            run {
                if (isInDebugMode) {
                    myDebugCard.visibility = View.VISIBLE

                    class SimpleHelloDialog : DialogFragment() {
                        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
                            AlertDialog.Builder(requireContext())
                                .setMessage("Hello Dialog")
                                .setPositiveButton("OK") { _, _ -> }
                                .create()


                    }

                    myDebugCard.setOnClickListener {

                        var myPayLoad: MyPayLoad = MyPayLoad();

                        myPayLoad.clubId = "-M32TcIPOPg5wdmKgZzd"
                        myPayLoad.tournamentId = "-NKJC4_p3LhpawVi1Vts"

                        BasicApiService().pingBackend(myPayLoad)
                            .subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeWith(object : DisposableSingleObserver<BasicApiResponse>() {
                                override fun onSuccess(value: BasicApiResponse?) {
                                    Log.d(Constants.LOG_TAG, "Success${value?.message}")
                                }

                                override fun onError(e: Throwable?) {
                                    Log.e(Constants.LOG_TAG, "Error ${e?.message}")
                                }

                            })

                        //SimpleHelloDialog().show(childFragmentManager, "some-tag")
                    }

                } else {
                    myDebugCard.visibility = View.GONE
                }
            }
        })

        viewModel.defaultClubExists.observe(viewLifecycleOwner, Observer { defaultClubExists ->
            run {
                if (defaultClubExists) {
                    tournaments_card.visibility = View.VISIBLE
                    players_card.visibility = View.VISIBLE
                } else {
                    tournaments_card.visibility = View.GONE
                    players_card.visibility = View.GONE
                }
            }
        })

        viewModel.showArchivedPlayers.observe(viewLifecycleOwner, Observer {
            if (it) {
                archivedPlayers.visibility = View.VISIBLE
            } else {
                archivedPlayers.visibility = View.GONE
            }
        })

        viewModel.photoUrl.observe(viewLifecycleOwner, Observer {
            if (viewModel.isInternalPhoto) {
                val uri = MyFirebaseUtils().storageReference(it!!)
                GlideApp.with(requireContext())
                    .load(uri)
                    .apply(RequestOptions().circleCrop())
                    .into(myProfileIcon)
            } else {
                val uri = Uri.parse(it)
                GlideApp.with(requireContext())
                    .load(uri)
                    .apply(RequestOptions().circleCrop())
                    .into(myProfileIcon)
            }
        })

        viewModel.myClub.observe(viewLifecycleOwner, Observer {
            it?.picture?.stringUri?.let { path ->
                GlideApp.with(requireContext())
                    .load(getStorageReference(path))
                    .fallback(R.drawable.chess_king_and_rook_v1)
                    .apply(RequestOptions().circleCrop())
                    .into(myClubIcon)
            }
        })

        followedPlayersCard.setOnClickListener { view ->
            val action = Dashboard02FragmentDirections
                .actionNavigationDashboard02ToFollowedPlayersFragment()
            view.findNavController()?.navigate(action)
        }
    }

    private fun getStorageReference(location: String): StorageReference {
        val storage = FirebaseStorage.getInstance()
        return storage.reference.child(location)
    }

    override fun onStart() {
        super.onStart()
        viewModel.initializeData()
        setPostType()
    }

    override fun onResume() {
        super.onResume()
        setPostType()
    }

    override fun onStop() {
        super.onStop()
        viewModel.removeEventListener()
    }


    private fun setPostType() {
        val sharePref = requireActivity().getPreferences(Context.MODE_PRIVATE)
        SharedPreferencesHelper.setPostType(sharePref.edit(), Post.PostType.CLUB_POST)
    }

}
