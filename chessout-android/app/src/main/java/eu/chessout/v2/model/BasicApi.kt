package eu.chessout.v2.model

import eu.chessout.shared.dao.BasicApiResponse
import eu.chessout.shared.model.MyPayLoad
import eu.chessout.shared.model.eventparams.tournament.GameResultUpdatedParams
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.PUT

interface BasicApi {

    @PUT("api/gameResultUpdated")
    fun gameResultUpdated(@Body gameResultUpdatedParams: GameResultUpdatedParams): Single<BasicApiResponse>

    @PUT("api/postCreated")
    fun postCreated(@Body myPayLoad: MyPayLoad): Single<BasicApiResponse>

    @PUT("api/postDeleted")
    fun postDeleted(@Body myPayLoad: MyPayLoad): Single<BasicApiResponse>

    @PUT("api/clubPostPictureUploadComplete")
    fun clubPostPictureUploadComplete(@Body myPayLoad: MyPayLoad): Single<BasicApiResponse>

    @PUT("api/pingServer")
    fun pingBackend(@Body myPayLoad: MyPayLoad): Single<BasicApiResponse>
}