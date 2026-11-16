package com.example.tcg_manager.data.model

/** A card saved in the collection plus how many copies are owned. */
data class CollectionCard(
    val card: Card,
    val quantity: Int,
    val addedAt: Long,
    val updatedAt: Long,
)
