package com.example.tcg_manager.data.remote

import android.graphics.Bitmap
import com.example.tcg_manager.data.model.Card
import com.example.tcg_manager.data.model.CardGame
import com.example.tcg_manager.data.model.SearchType

/**
 * One card API (magicthegathering.io, pokemontcg.io, …).
 *
 * Not every API offers every kind of search, so each provider declares its [capabilities]
 * and only overrides the calls it really supports. Calling an unsupported one throws
 * [UnsupportedSearchException]. Callers should check [supports] first.
 */
interface CardApiProvider {
    /** Stable id stored with saved cards, e.g. "mtgio". Never change it once released. */
    val id: String
    val displayName: String
    val game: CardGame
    val capabilities: Set<SearchType>

    fun supports(type: SearchType): Boolean = type in capabilities

    /** Returns the card with this API id, or null if it doesn't exist. */
    suspend fun searchById(id: String): Card? = unsupported(SearchType.ID)

    suspend fun searchByName(name: String): List<Card> = unsupported(SearchType.NAME)

    suspend fun searchByDescription(text: String): List<Card> = unsupported(SearchType.DESCRIPTION)

    /** Native image search, for APIs that offer it. Others fall back to OCR + name search. */
    suspend fun searchByImage(image: Bitmap): List<Card> = unsupported(SearchType.IMAGE)
}

class UnsupportedSearchException(provider: String, type: SearchType) :
    UnsupportedOperationException("$provider does not support search by ${type.displayName.lowercase()}")

private fun CardApiProvider.unsupported(type: SearchType): Nothing =
    throw UnsupportedSearchException(displayName, type)
