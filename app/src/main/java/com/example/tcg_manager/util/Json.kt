package com.example.tcg_manager.util

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/** Shared Gson instance and small JSON helpers used by the database and the API layer. */
object Json {
    val gson: Gson = Gson()

    private val stringMapType = object : TypeToken<LinkedHashMap<String, String>>() {}.type

    fun mapToJson(map: Map<String, String>): String = gson.toJson(map)

    fun jsonToMap(json: String?): Map<String, String> =
        if (json.isNullOrBlank()) emptyMap() else gson.fromJson(json, stringMapType)
}
