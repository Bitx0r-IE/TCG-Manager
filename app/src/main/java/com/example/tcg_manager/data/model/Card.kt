package com.example.tcg_manager.data.model

/**
 * One card as returned by any card API, normalised to a common shape.
 *
 * Fields every game has are typed properties. Game-specific stats (power/toughness, HP,
 * attacks, weaknesses…) go into [stats] so new games don't need schema changes.
 * [rawJson] keeps everything the API (or its SDK) returned for the card, so no received data is lost.
 */
data class Card(
    /** Id of the [com.example.tcg_manager.data.remote.CardApiProvider] that returned it, e.g. "mtgio". */
    val source: String,
    /** The card's id inside that API. Together with [source] it uniquely identifies the card. */
    val externalId: String,
    val game: CardGame,
    val name: String,
    val setCode: String? = null,
    val setName: String? = null,
    val collectorNumber: String? = null,
    val rarity: String? = null,
    /** Type line, e.g. "Creature — Goblin" or "Pokémon · Basic". */
    val typeLine: String? = null,
    /** Cost to play, e.g. "{1}{R}" or "Colorless, Colorless". */
    val cost: String? = null,
    /** Rules / ability / attack text. */
    val description: String? = null,
    val flavorText: String? = null,
    val artist: String? = null,
    val imageUrl: String? = null,
    /** Game-specific stats, in display order (label → value). */
    val stats: Map<String, String> = emptyMap(),
    /** Prices reported by the API (label → value), e.g. "USD" → "0.25". */
    val prices: Map<String, String> = emptyMap(),
    /** Complete JSON the API returned for this card. */
    val rawJson: String = "",
) {
    val key: CardKey get() = CardKey(source, externalId)
}

/** Unique identity of a card across all APIs. */
data class CardKey(val source: String, val externalId: String)
