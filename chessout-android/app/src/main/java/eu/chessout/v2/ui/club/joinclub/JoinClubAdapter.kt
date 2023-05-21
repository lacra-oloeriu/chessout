package eu.chessout.v2.ui.club.joinclub

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import eu.chessout.shared.model.Club
import eu.chessout.shared.model.DefaultClub
import eu.chessout.v2.R
import eu.chessout.v2.util.MyFirebaseUtils

class JoinClubAdapter(
    var clubList: ArrayList<Club>
) : RecyclerView.Adapter<JoinClubAdapter.ItemHolder>() {

    fun updateArrayList(newArrayList: List<Club>) {
        clubList.clear()
        clubList.addAll(newArrayList)
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val itemHolder = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_text, parent, false)
        return ItemHolder(itemHolder)
    }

    override fun getItemCount(): Int {
        return clubList.size
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        val club: Club = clubList[position]
        holder.textView.text = club.name



        holder.textView.setOnClickListener {
            joinAndSetDefaultClub(club)
            holder.textView.findNavController()?.navigate(
                JoinClubFragmentDirections
                    .actionNavigationJoinClubFragmentToNavigationMyClubsFragment()
            )
        }
    }

    class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textView = itemView.findViewById<TextView>(R.id.list_item_text_simple_view)
    }

    private fun joinAndSetDefaultClub(club: Club) {

        val auth =
            FirebaseAuth.getInstance()
        val firebaseUser = auth.currentUser
        val uid = firebaseUser!!.uid

        val firebaseUtils = MyFirebaseUtils()
        firebaseUtils.addToMyClubs(uid, club.clubId, club)

        val defaultClub = DefaultClub(club.clubId, club.name)
        firebaseUtils.setDefaultClub(defaultClub)
    }

}
