package com.example.tcg_manager.data.repository

import android.graphics.Bitmap
import com.example.tcg_manager.data.model.Card
import com.example.tcg_manager.data.model.CardGame
import com.example.tcg_manager.data.model.SearchType
import com.example.tcg_manager.data.remote.CardApiProvider
import com.example.tcg_manager.data.remote.CardApiRegistry
import com.example.tcg_manager.scanner.CardOcrResult
import com.example.tcg_manager.scanner.CardTextRecognizer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope

/** Result of a search across one or more APIs. Errors from individual APIs don't hide other results. */
data class SearchOutcome(
    val cards: List<Card>,
    val errors: List<String> = emptyList(),
    /** Set for image searches: what ML Kit read from the photo. */
    val ocr: CardOcrResult? = null,
)

/**
 * Sends searches to the right APIs.
 *
 * - With a game selected, only that game's APIs are asked; with none (auto), every API
 *   supporting the search type is asked in parallel.
 * - APIs without native image search fall back to ML Kit OCR + name search.
 */
class CardSearchRepository(
    private val registry: CardApiRegistry,
    private val recognizer: CardTextRecognizer,
) {

    suspend fun search(type: SearchType, query: String, game: CardGame?): SearchOutcome {
        require(type != SearchType.IMAGE) { "Use searchByImage for image searches" }
        if (query.isBlank()) return SearchOutcome(emptyList(), listOf("Enter something to search"))

        val providers = registry.supporting(type, game)
        if (providers.isEmpty()) {
            val target = game?.displayName ?: "any game"
            return SearchOutcome(emptyList(), listOf("No API for $target supports search by ${type.displayName.lowercase()}"))
        }
        return queryAll(providers) { provider ->
            when (type) {
                SearchType.ID -> listOfNotNull(provider.searchById(query))
                SearchType.NAME -> provider.searchByName(query)
                SearchType.DESCRIPTION -> provider.searchByDescription(query)
                SearchType.IMAGE -> error("unreachable")
            }
        }
    }

    suspend fun searchByImage(image: Bitmap, gameHint: CardGame?): SearchOutcome {
        val ocr = recognizer.recognize(image)
        val game = gameHint ?: ocr.detectedGame
        val providers = game?.let(registry::forGame) ?: registry.providers

        val outcome = queryAll(providers) { provider ->
            when {
                provider.supports(SearchType.IMAGE) -> provider.searchByImage(image)
                provider.supports(SearchType.NAME) -> searchByCandidateNames(provider, ocr.nameCandidates)
                else -> emptyList()
            }
        }
        val extraErrors = if (ocr.nameCandidates.isEmpty() && providers.none { it.supports(SearchType.IMAGE) }) {
            listOf("Couldn't read the card name. Try again with better light and the card filling the frame.")
        } else emptyList()
        return outcome.copy(ocr = ocr, errors = outcome.errors + extraErrors)
    }

    /** Tries each OCR name candidate in order until one returns results. */
    private suspend fun searchByCandidateNames(provider: CardApiProvider, candidates: List<String>): List<Card> {
        for (name in candidates) {
            val cards = provider.searchByName(name)
            if (cards.isNotEmpty()) return cards
        }
        return emptyList()
    }

    private suspend fun queryAll(
        providers: List<CardApiProvider>,
        call: suspend (CardApiProvider) -> List<Card>,
    ): SearchOutcome = supervisorScope {
        val results = providers.map { provider ->
            async {
                try {
                    Result.success(call(provider))
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Result.failure(Exception("${provider.displayName}: ${e.message ?: e.javaClass.simpleName}", e))
                }
            }
        }.awaitAll()
        SearchOutcome(
            cards = results.flatMap { it.getOrDefault(emptyList()) }.distinctBy { it.key },
            errors = results.mapNotNull { it.exceptionOrNull()?.message },
        )
    }
}
