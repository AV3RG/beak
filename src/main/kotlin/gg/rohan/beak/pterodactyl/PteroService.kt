package gg.rohan.beak.pterodactyl

import okhttp3.MultipartBody
import retrofit2.http.*

interface PteroService {

    @POST("/api/client/servers/{server}/power")
    suspend fun changePowerState(@Path("server") server: String, @Body action: PowerState)

    @GET("/api/client/servers/{server}/files/upload")
    suspend fun createUploadUrl(@Path("server") server: String): UploadUrlResponse

    @Multipart
    @POST
    suspend fun uploadFile(@Url url: String, @Query("directory") directory: String?, @Part filePart: MultipartBody.Part)

}