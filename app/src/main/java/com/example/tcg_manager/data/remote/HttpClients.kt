package com.example.tcg_manager.data.remote

import com.example.tcg_manager.util.Json
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

object HttpClients {
    /** Some APIs (e.g. Scryfall) require an identifying User-Agent. */
    private const val USER_AGENT = "TCGManager/1.0"

    fun retrofit(baseUrl: String, extraHeaders: Map<String, String> = emptyMap()): Retrofit {
        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("User-Agent", USER_AGENT)
                    .header("Accept", "application/json")
                    .apply { extraHeaders.forEach { (k, v) -> header(k, v) } }
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(::retryServerErrors)
            .build()
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(Json.gson))
            .build()
    }

    /** Free card APIs sometimes answer 5xx for a moment; retry GETs a couple of times. */
    private fun retryServerErrors(chain: Interceptor.Chain): okhttp3.Response {
        val request = chain.request()
        var response = chain.proceed(request)
        var attempt = 0
        while (request.method == "GET" && response.code in 500..599 && attempt < MAX_RETRIES) {
            response.close()
            Thread.sleep(RETRY_DELAY_MS * (attempt + 1))
            attempt++
            response = chain.proceed(request)
        }
        return response
    }

    private const val MAX_RETRIES = 2
    private const val RETRY_DELAY_MS = 600L
}

/** Thrown when an API answers with an error other than "not found". */
class ApiException(message: String) : IOException(message)

/** Body of a successful response, null on 404 (not found), [ApiException] on other errors. */
fun <T> Response<T>.bodyOrNullIfNotFound(): T? = when {
    isSuccessful -> body()
    code() == 404 -> null
    else -> throw ApiException("HTTP ${code()} from ${raw().request.url.host}")
}
