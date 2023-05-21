package eu.chessout.v2.util

import android.util.Log
import com.fasterxml.jackson.databind.ObjectMapper
import eu.chessout.shared.Constants
import eu.chessout.shared.dao.BasicApiResponse
import eu.chessout.shared.model.MyPayLoad
import eu.chessout.v2.model.BasicApiService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers

class MyBackendUtil {
    companion object {
        fun notifyTheBackendClubPostCreated(clubId: String, postId: String) {
            val myPayLoad = MyPayLoad()
            myPayLoad.event = MyPayLoad.Event.CLUB_POST_CREATED
            myPayLoad.postId = postId
            myPayLoad.clubId = clubId
            myPayLoad.authToken = MyFirebaseUtils.currentUserToken()

            BasicApiService().postCreated(myPayLoad)
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
        }

        fun notifyBackendClubPostDeleted(clubId: String, postId: String) {
            val myPayLoad = MyPayLoad()
            myPayLoad.event = MyPayLoad.Event.CLUB_POST_DELETED
            myPayLoad.postId = postId
            myPayLoad.clubId = clubId
            myPayLoad.authToken = MyFirebaseUtils.currentUserToken()

            val objectMapper = ObjectMapper();
            val stringPayload = objectMapper.writeValueAsString(myPayLoad);
            Log.d(Constants.LOG_TAG, "stringPayload: $stringPayload")

            BasicApiService().postDeleted(myPayLoad)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<BasicApiResponse>() {
                    override fun onSuccess(value: BasicApiResponse?) {
                        Log.d(
                            Constants.LOG_TAG,
                            "Success notifyBackendClubPostDeleted ${value?.message}"
                        )
                    }

                    override fun onError(e: Throwable?) {
                        Log.e(Constants.LOG_TAG, "Error notifyBackendClubPostDeleted ${e?.message}")
                    }

                })
            Log.d(Constants.LOG_TAG, "Finished notifyBackendClubPostDeleted");
        }
    }
}