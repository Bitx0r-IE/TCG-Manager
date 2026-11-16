package com.example.tcg_manager.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import com.example.tcg_manager.data.model.CardGame

/**
 * A card in the user's collection. Stores every field received from the API
 * (including the full raw JSON) plus how many copies the user owns.
 */
@Entity(
    tableName = "cards",
    primaryKeys = ["source", "externalId"],
    indices = [Index("name"), Index("game")],
)
data class CardEntity(
    val source: String,
    val externalId: String,
    val game: CardGame,
    val name: String,
    val setCode: String?,
    val setName: String?,
    val collectorNumber: String?,
    val rarity: String?,
    val typeLine: String?,
    val cost: String?,
    val description: String?,
    val flavorText: String?,
    val artist: String?,
    val imageUrl: String?,
    val stats: Map<String, String>,
    val prices: Map<String, String>,
    val rawJson: String,
    val quantity: Int,
    val addedAt: Long,
    val updatedAt: Long,
)
