package gg.rohan.beak.retrofit

import gg.rohan.beak.pterodactyl.PteroService
import retrofit2.Retrofit

object RetrofitClient {

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://pterodactyl.rohan.gg/api/client")
        .build()
    val pterodactylService: PteroService = retrofit
        .create(PteroService::class.java)

}