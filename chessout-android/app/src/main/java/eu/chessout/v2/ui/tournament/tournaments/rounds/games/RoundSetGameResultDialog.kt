package eu.chessout.v2.ui.tournament.tournaments.rounds.games

import android.app.AlertDialog
import android.app.Dialog
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.DialogFragment
import com.google.firebase.database.FirebaseDatabase
import eu.chessout.shared.Constants
import eu.chessout.shared.dao.BasicApiResponse
import eu.chessout.shared.model.Game
import eu.chessout.shared.model.eventparams.tournament.GameResultUpdatedParams
import eu.chessout.v2.model.BasicApiService
import eu.chessout.v2.util.MyFirebaseUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers

class RoundSetGameResultDialog(
    val clubId: String,
    val tournamentId: String,
    val roundId: Int,
    private val game: Game
) : DialogFragment() {

    private val mWhitePlayer = "1 - 0 : ${game.whitePlayer.name} wins"
    private val mWhiteWinsByForfeit = "1k - 0 : ${game.whitePlayer.name} wins by forfeit"
    private val mBlackPlayer = "0 - 1 : ${game.blackPlayer.name} wins"
    private val mBlackWinsByForfeit = "0 - 1k : ${game.blackPlayer.name} wins by forfeit"
    val mNoPartner = hasNoPartner(game)
    private val mDrawGame = "1/2 - 1/2"
    private val mDoubleForfeit = "0k - 0k : Double forfeit"
    private val mNotDecided = "Still playing"


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        builder.setTitle("Set result for table ${game.tableNumber}")

        builder.setNegativeButton("Cancel") { _, _ -> dismiss() }
        val items = arrayOf(
            mWhitePlayer,
            mDrawGame,
            mBlackPlayer,
            mWhiteWinsByForfeit,
            mBlackWinsByForfeit,
            mDoubleForfeit,
            mNotDecided
        )
        builder.setItems(items) { _, witch ->
            PersistResult(clubId, tournamentId, roundId, game.tableNumber).execute(witch)
        }

        return builder.create()
    }

    private fun hasNoPartner(game: Game): Boolean {
        return null == game.blackPlayer
    }

    class PersistResult(
        val clubId: String,
        val tournamentId: String,
        val roundId: Int,
        private val tableNumber: Int
    ) :
        AsyncTask<Int, Void, Void?>() {
        override fun doInBackground(vararg params: Int?): Void? {

            // set legacy result base on int value
            val paramsVal = params[0]!!

            var result = paramsVal + 1

            //switch = 2 with 3
            if (result == 2) {
                result = 3
            } else if (result == 3) {
                result = 2
            }
            // set not decided (moved last)
            if (paramsVal == 6) {
                result = 0
            }

            // set the rest of the results
            if (paramsVal in 3..5) {
                result = paramsVal + 2
            }

            val resultLoc = Constants.LOCATION_GAME_RESULT
                .replace(Constants.CLUB_KEY, clubId)
                .replace(Constants.TOURNAMENT_KEY, tournamentId)
                .replace(Constants.ROUND_NUMBER, roundId.toString())
                .replace(Constants.TABLE_NUMBER, tableNumber.toString())

            val resultRef =
                FirebaseDatabase.getInstance().getReference(resultLoc)
            resultRef.setValue(result)

            MyFirebaseUtils().computeAndUpdateStandings(clubId, tournamentId, roundId)

            // notify the backend that result was updated
            val gameResultUpdatedParams = GameResultUpdatedParams()
            gameResultUpdatedParams.authToken = MyFirebaseUtils.currentUserToken()
            gameResultUpdatedParams.clubId = clubId
            gameResultUpdatedParams.tournamentId = tournamentId
            gameResultUpdatedParams.roundId = roundId.toString()
            gameResultUpdatedParams.tableId = tableNumber.toString()

            BasicApiService().gameResultUpdated(gameResultUpdatedParams)
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

            return null
        }
    }

}