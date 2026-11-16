package com.example.tcg_manager.data.remote.scryfall

import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/** https://scryfall.com/docs/api. Responses are kept as raw JSON so everything can be stored. */
interface ScryfallApi {

    @GET("cards/{id}")
    suspend fun cardById(@Path("id") id: String): Response<JsonObject>

    /** Fuzzy name match: tolerant of OCR typos, returns the single best card. */
    @GET("cards/named")
    suspend fun cardNamedFuzzy(@Query("fuzzy") name: String): Response<JsonObject>

    /** Full Scryfall search syntax, e.g. `o:"draw a card"`. */
    @GET("cards/search")
    suspend fun search(@Query("q") query: String): Response<JsonObject>

    companion object {
        const val BASE_URL = "https://api.scryfall.com/"
    }
}
