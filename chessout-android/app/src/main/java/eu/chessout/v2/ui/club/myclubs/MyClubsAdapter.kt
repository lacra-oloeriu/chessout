package eu.chessout.v2.ui.club.myclubs

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import eu.chessout.shared.model.Club
import eu.chessout.shared.model.DefaultClub
import eu.chessout.v2.R
import eu.chessout.v2.util.GlideApp
import eu.chessout.v2.util.MyFirebaseUtils

class MyClubsAdapter(private val clubList: ArrayList<Club>) :
    RecyclerView.Adapter<MyClubsAdapter.ItemHolder>() {

    private lateinit var context: Context
    private val storage = FirebaseStorage.getInstance()
    private val myFirebaseUtils = MyFirebaseUtils()

    class ItemHolder(view: View) : RecyclerView.ViewHolder(view) {
        val allItems = view
        val clubImage = view.findViewById<ImageView>(R.id.clubImage)
        val clubShortName = view.findViewById<TextView>(R.id.clubShortName)
    }


    fun updateList(newList: List<Club>) {
        clubList.clear()
        clubList.addAll(newList)
        notifyDataSetChanged()
    }

    fun setContext(context: Context) {
        this.context = context
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val view = LayoutInflater.from((parent.context))
            .inflate(R.layout.list_item_club, parent, false)
        return ItemHolder(view)
    }

    override fun getItemCount(): Int {
        return clubList.size
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        val club = clubList[position]
        holder.clubShortName.text = club.shortName
        holder.allItems.setOnClickListener {
            val clubDirection = MyClubsFragmentDirections
                .actionNavigationMyClubsFragmentToClubDashboardFragment(club.clubId)
            it.findNavController().navigate(clubDirection)
        }
        GlideApp.with(context)
            .load(R.drawable.chess_king_and_rook_v1)
            .apply(RequestOptions().circleCrop())
            .into(holder.clubImage)
        club.picture?.stringUri?.let { path ->
            GlideApp.with(context)
                .load(getStorageReference(path))
                .fallback(R.drawable.chess_king_and_rook_v1)
                .apply(RequestOptions().circleCrop())
                .into(holder.clubImage)
        }
        registerIsDefaultClubListener(holder.clubShortName, club)
    }

    private fun getStorageReference(location: String): StorageReference {
        return storage.reference.child(location)
    }

    private fun registerIsDefaultClubListener(textView: TextView, club: Club) {
        val listener = object : MyFirebaseUtils.DefaultClubListener {
            override fun onDefaultClubValue(defaultClub: DefaultClub) {
                if (defaultClub.clubKey == club.clubId) {
                    textView.text = "${club.shortName} ( default )"
                } else {
                    textView.text = club.shortName
                }
            }

            override fun setDbRef(databaseReference: DatabaseReference) {
                // nothing to implement
            }

            override fun setDbListener(valueEventListener: ValueEventListener) {
                // nothing to implement
            }

        }
        myFirebaseUtils.registerDefaultClubListener(listener, true)
    }
}