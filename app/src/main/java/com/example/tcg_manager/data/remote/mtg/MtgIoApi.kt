package com.example.tcg_manager.data.remote.mtg

import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Direct calls to https://docs.magicthegathering.io for the searches the Kotlin SDK
 * doesn't expose (rules-text search and lookup by card UUID). Everything else goes
 * through the SDK in [MtgIoProvider].
 */
interface MtgIoApi {

    /** Accepts the card's UUID (`id`) as well as its multiverse id. */
    @GET("cards/{id}")
    suspend fun cardById(@Path("id") id: String): Response<JsonObject>

    /** Cards whose rules text contains [text]. */
    @GET("cards")
    suspend fun searchByText(
        @Query("text") text: String,
        @Query("pageSize") pageSize: Int,
    ): Response<JsonObject>

    companion object {
        const val BASE_URL = "https://api.magicthegathering.io/v1/"
    }
}
