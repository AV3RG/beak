package gg.rohan.beak.pterodactyl

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

interface PteroService {

    @POST("/servers/{server}/power")
    suspend fun changePowerState(@Path("server") server: String, @Body action: PowerState)

    @GET("/servers/{server}/files/upload")
    suspend fun createUploadUrl(@Path("server") server: String): String

    @POST
    suspend fun uploadFile(@Url url: String, @Query("directory") directory: String?, @Body file: ByteArray)

}