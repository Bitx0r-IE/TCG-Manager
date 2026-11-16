package com.example.tcg_manager.data.local.db

import androidx.room.TypeConverter
import com.example.tcg_manager.data.model.CardGame
import com.example.tcg_manager.util.Json

class Converters {
    @TypeConverter
    fun stringMapToJson(map: Map<String, String>): String = Json.mapToJson(map)

    @TypeConverter
    fun jsonToStringMap(json: String?): Map<String, String> = Json.jsonToMap(json)

    @TypeConverter
    fun gameToString(game: CardGame): String = game.name

    @TypeConverter
    fun stringToGame(value: String): CardGame = CardGame.valueOf(value)
}
