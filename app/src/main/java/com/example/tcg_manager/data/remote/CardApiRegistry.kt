package com.example.tcg_manager.data.remote

import com.example.tcg_manager.data.model.CardGame
import com.example.tcg_manager.data.model.SearchType

/** All available card APIs. Register a new provider here to make the app search it. */
class CardApiRegistry(val providers: List<CardApiProvider>) {

    fun forGame(game: CardGame): List<CardApiProvider> = providers.filter { it.game == game }

    /** Providers for [game] (or all games if null) that support [type]. */
    fun supporting(type: SearchType, game: CardGame? = null): List<CardApiProvider> =
        providers.filter { (game == null || it.game == game) && it.supports(type) }

    fun byId(id: String): CardApiProvider? = providers.firstOrNull { it.id == id }
}
