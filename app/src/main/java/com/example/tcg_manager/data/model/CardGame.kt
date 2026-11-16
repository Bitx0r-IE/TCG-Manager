package com.example.tcg_manager.data.model

/** Trading card games the app knows how to search. Add a value here when adding a new API. */
enum class CardGame(val displayName: String) {
    MAGIC("Magic: The Gathering"),
    POKEMON("Pokémon TCG");

    override fun toString() = displayName
}
