package com.example.tcg_manager.data.remote.scryfall

import com.google.gson.annotations.SerializedName

/** Subset of a Scryfall card object that the app shows. The full JSON is saved separately. */
data class ScryfallCardDto(
    val id: String?,
    val name: String?,
    val lang: String?,
    val set: String?,
    @SerializedName("set_name") val setName: String?,
    @SerializedName("collector_number") val collectorNumber: String?,
    val rarity: String?,
    @SerializedName("type_line") val typeLine: String?,
    @SerializedName("mana_cost") val manaCost: String?,
    val cmc: Double?,
    @SerializedName("oracle_text") val oracleText: String?,
    @SerializedName("flavor_text") val flavorText: String?,
    val power: String?,
    val toughness: String?,
    val loyalty: String?,
    val colors: List<String>?,
    val artist: String?,
    @SerializedName("released_at") val releasedAt: String?,
    @SerializedName("image_uris") val imageUris: ScryfallImageUris?,
    @SerializedName("card_faces") val cardFaces: List<ScryfallCardFaceDto>?,
    val prices: Map<String, String?>?,
)

/** Double-faced / split cards keep most data per face. */
data class ScryfallCardFaceDto(
    val name: String?,
    @SerializedName("mana_cost") val manaCost: String?,
    @SerializedName("type_line") val typeLine: String?,
    @SerializedName("oracle_text") val oracleText: String?,
    @SerializedName("flavor_text") val flavorText: String?,
    val power: String?,
    val toughness: String?,
    val loyalty: String?,
    @SerializedName("image_uris") val imageUris: ScryfallImageUris?,
)

data class ScryfallImageUris(val small: String?, val normal: String?, val large: String?)
