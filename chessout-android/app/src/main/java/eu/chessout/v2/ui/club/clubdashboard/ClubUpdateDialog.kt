package eu.chessout.v2.ui.club.clubdashboard

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import eu.chessout.shared.model.Club
import eu.chessout.v2.R
import eu.chessout.v2.util.MyFirebaseUtils

class ClubUpdateDialog(private val club: Club) : DialogFragment() {
    private lateinit var mView: View
    private lateinit var nameView: EditText
    private lateinit var shortNameView: EditText
    private lateinit var emailView: EditText
    private lateinit var countryView: EditText
    private lateinit var cityView: EditText
    private lateinit var homePageView: EditText
    private lateinit var descriptionView: EditText


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)
        val builder = AlertDialog.Builder(requireActivity())
        val inflater = requireActivity().layoutInflater

        mView = inflater.inflate(R.layout.club_create_dialog, null)
        setViewItems()
        builder.setView(mView)


        builder.setNegativeButton("Cancel") { _, _ -> dismiss() }
        builder.setPositiveButton("Update club") { _, _ -> updateClub() }

        return builder.create()
    }

    private fun setViewItems() {
        nameView = mView.findViewById(R.id.clubName)
        shortNameView = mView.findViewById(R.id.shortName)
        emailView = mView.findViewById(R.id.email)
        countryView = mView.findViewById(R.id.country)
        cityView = mView.findViewById(R.id.city)
        homePageView = mView.findViewById(R.id.homePage)
        homePageView.visibility = View.GONE
        descriptionView = mView.findViewById(R.id.clubDescription)

        nameView.setText(club.name)
        shortNameView.setText(club.shortName)
        emailView.setText(club.email)
        countryView.setText(club.country)
        cityView.setText(club.city)
        homePageView.setText(club.homePage)
        descriptionView.setText(club.description)
    }

    private fun updateClub() {
        club.name = nameView.text.toString()
        club.shortName = shortNameView.text.toString()
        club.email = emailView.text.toString()
        club.country = countryView.text.toString()
        club.city = cityView.text.toString()
        club.homePage = homePageView.text.toString()
        club.description = descriptionView.text.toString()

        MyFirebaseUtils().updateClub(club)

        dismiss()
    }
}