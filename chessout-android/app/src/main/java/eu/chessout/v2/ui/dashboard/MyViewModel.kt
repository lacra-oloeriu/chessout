package eu.chessout.v2.ui.dashboard

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import eu.chessout.shared.Constants
import eu.chessout.v2.R
import eu.chessout.v2.model.BasicApiService

class MyViewModel() : ViewModel() {

    val TAG = Constants.LOG_TAG;
    val dashboardModelList = MutableLiveData<List<DashboardModel>>()

    private val api = BasicApiService()

    fun refresh() {
        getInitialList()
    }

    fun getInitialList() {
        var items: ArrayList<DashboardModel> = ArrayList()
        /*items.add(DashboardModel(R.drawable.chess_king_v1, "King"))
        items.add(DashboardModel(R.drawable.chess_queen_v1, "Queen"))
        items.add(DashboardModel(R.drawable.chess_bishop_v1, "Bishop"))
        items.add(DashboardModel(R.drawable.chess_knight_v1, "Knight"))
        items.add(DashboardModel(R.drawable.chess_rook_v1, "Rook"))
        items.add(DashboardModel(R.drawable.chess_pawn_v1, "Pawn"))
        items.add(DashboardModel(R.drawable.chess_king_and_rook_v1, "King and rook"))
*/
        items.add(DashboardModel(R.drawable.ic_sign_out_alt_solid, "Sign out"))
        items.add(DashboardModel(R.drawable.ic_paper_plane_regular, "Ping backend"))

        dashboardModelList.value = items
    }


}