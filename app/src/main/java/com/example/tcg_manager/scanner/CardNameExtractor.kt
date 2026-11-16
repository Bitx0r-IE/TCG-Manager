package com.example.tcg_manager.scanner

/**
 * Picks the lines most likely to be the card's name. On almost every TCG the name is
 * printed at the top, so lines are taken top to bottom after removing obvious noise
 * (stage labels, HP values, mana symbols read as digits…).
 */
object CardNameExtractor {

    private val ignoredLines = Regex(
        "^(basic|stage ?[12i]+|trainer|supporter|item|stadium|energy|pok[eé]mon|evolves from.*|v|ex|gx|vmax|vstar)$",
        RegexOption.IGNORE_CASE,
    )
    private val hpSuffix = Regex("\\s*(hp\\s*\\d+|\\d+\\s*hp).*$", RegexOption.IGNORE_CASE)
    private val trailingNoise = Regex("[\\s\\d{}()®©•*@.,:;|/\\\\_~^-]+$")
    private val leadingNoise = Regex("^[\\s\\d{}()®©•*@.,:;|/\\\\_~^-]+")

    /** @param linesTopToBottom OCR lines ordered by vertical position. */
    fun candidates(linesTopToBottom: List<String>, max: Int = 3): List<String> =
        linesTopToBottom.asSequence()
            .map { clean(it) }
            .filter { isPlausibleName(it) }
            .distinctBy { it.lowercase() }
            .take(max)
            .toList()

    private fun clean(line: String): String = line
        .replace(hpSuffix, "")
        .replace(trailingNoise, "")
        .replace(leadingNoise, "")
        .trim()

    private fun isPlausibleName(text: String): Boolean {
        if (text.length !in 2..40) return false
        if (ignoredLines.matches(text)) return false
        val letters = text.count { it.isLetter() }
        return letters >= 2 && letters.toDouble() / text.length >= 0.7
    }
}
