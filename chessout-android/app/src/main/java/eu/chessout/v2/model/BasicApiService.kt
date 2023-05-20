package eu.chessout.v2.model

import com.fasterxml.jackson.databind.ObjectMapper
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import eu.chessout.shared.Constants
import eu.chessout.shared.dao.BasicApiResponse
import eu.chessout.shared.model.MyPayLoad
import eu.chessout.shared.model.eventparams.tournament.GameResultUpdatedParams
import io.reactivex.Single
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class BasicApiService {

    private val baseUrl = Constants.API_URL

    private val api = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()
        .create(BasicApi::class.java)

    fun gameResultUpdated(gameResultUpdatedParams: GameResultUpdatedParams): Single<BasicApiResponse> {
        val objectMapper = ObjectMapper();
        val stringValue = objectMapper.writeValueAsString(gameResultUpdatedParams);
        return api.gameResultUpdated(gameResultUpdatedParams)
    }

    fun postCreated(myPayLoad: MyPayLoad): Single<BasicApiResponse> {
        return api.postCreated(myPayLoad)
    }

    fun postDeleted(myPayLoad: MyPayLoad): Single<BasicApiResponse> {
        return api.postDeleted(myPayLoad)
    }

    fun clubPostPictureUploadComplete(myPayLoad: MyPayLoad): Single<BasicApiResponse> {
        return api.clubPostPictureUploadComplete(myPayLoad)
    }

    fun pingBackend(myPayLoad: MyPayLoad): Single<BasicApiResponse> {
        return api.pingBackend(myPayLoad);
    }
}