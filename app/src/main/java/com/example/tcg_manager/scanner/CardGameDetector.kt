package com.example.tcg_manager.scanner

import com.example.tcg_manager.data.model.CardGame

/**
 * Guesses which game a card belongs to from the text printed on it.
 * Each game has words that only (or mostly) appear on its cards; the highest score wins.
 * Add an entry to [keywords] when adding a new game.
 */
object CardGameDetector {

    private val keywords: Map<CardGame, List<Pair<Regex, Int>>> = mapOf(
        CardGame.MAGIC to listOf(
            "wizards of the coast" to 5,
            "\\b(creature|instant|sorcery|enchantment|planeswalker)\\b" to 3,
            "\\blegendary\\b" to 2,
            "\\b(artifact|land|aura|equipment)\\b" to 1,
            "\\b(flying|trample|haste|deathtouch|lifelink|vigilance|flash)\\b" to 2,
            "\\btap\\b|\\{t\\}" to 1,
        ),
        CardGame.POKEMON to listOf(
            "pok[eé]mon" to 4,
            "nintendo|game freak|creatures" to 4,
            "\\bweakness\\b" to 3,
            "\\bresistance\\b" to 2,
            "\\bretreat\\b" to 3,
            "\\b\\d{2,3}\\s?hp\\b|\\bhp\\s?\\d{2,3}\\b" to 3,
            "\\b(basic|stage ?[12]|trainer|supporter)\\b" to 2,
            "\\bevolves from\\b" to 3,
        ),
    ).mapValues { (_, list) -> list.map { (pattern, weight) -> Regex(pattern, RegexOption.IGNORE_CASE) to weight } }

    /** Returns the most likely game, or null when the text gives no clear signal. */
    fun detect(text: String): CardGame? {
        val scores = keywords.mapValues { (_, rules) ->
            rules.sumOf { (regex, weight) -> if (regex.containsMatchIn(text)) weight else 0 }
        }
        val best = scores.maxByOrNull { it.value } ?: return null
        val tie = scores.count { it.value == best.value } > 1
        return if (best.value >= MIN_SCORE && !tie) best.key else null
    }

    private const val MIN_SCORE = 3
}
