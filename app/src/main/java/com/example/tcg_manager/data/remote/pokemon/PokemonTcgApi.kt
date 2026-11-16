package com.example.tcg_manager.data.remote.pokemon

import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/** https://docs.pokemontcg.io. Works without an API key (lower rate limit). */
interface PokemonTcgApi {

    @GET("cards/{id}")
    suspend fun cardById(@Path("id") id: String): Response<JsonObject>

    /** Lucene-like query, e.g. `name:"pikachu"`. */
    @GET("cards")
    suspend fun search(
        @Query("q") query: String,
        @Query("pageSize") pageSize: Int = 30,
        @Query("orderBy") orderBy: String = "-set.releaseDate",
    ): Response<JsonObject>

    companion object {
        const val BASE_URL = "https://api.pokemontcg.io/v2/"
    }
}
