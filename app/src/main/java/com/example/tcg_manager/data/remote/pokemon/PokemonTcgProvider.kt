package com.example.tcg_manager.data.remote.pokemon

import com.example.tcg_manager.data.model.Card
import com.example.tcg_manager.data.model.CardGame
import com.example.tcg_manager.data.model.SearchType
import com.example.tcg_manager.data.remote.CardApiProvider
import com.example.tcg_manager.data.remote.HttpClients
import com.example.tcg_manager.data.remote.bodyOrNullIfNotFound
import com.example.tcg_manager.util.Json
import com.google.gson.JsonObject

/**
 * Pokémon TCG via pokemontcg.io. Supports id, name and description; no image search.
 * Description search covers flavour and ability text (the API errors on attack text).
 *
 * @param apiKey optional key from https://dev.pokemontcg.io for higher rate limits.
 */
class PokemonTcgProvider(
    apiKey: String? = null,
    private val api: PokemonTcgApi = HttpClients
        .retrofit(PokemonTcgApi.BASE_URL, apiKey?.let { mapOf("X-Api-Key" to it) }.orEmpty())
        .create(PokemonTcgApi::class.java),
) : CardApiProvider {

    override val id = "pokemontcg"
    override val displayName = "Pokémon TCG API"
    override val game = CardGame.POKEMON
    override val capabilities = setOf(SearchType.ID, SearchType.NAME, SearchType.DESCRIPTION)

    override suspend fun searchById(id: String): Card? =
        api.cardById(id.trim()).bodyOrNullIfNotFound()
            ?.getAsJsonObject("data")
            ?.let(::toCard)

    override suspend fun searchByName(name: String): List<Card> {
        val clean = sanitize(name)
        val exact = searchList("name:\"$clean\"")
        if (exact.isNotEmpty()) return exact
        // OCR may add or miss characters at the end; retry as a prefix on the first word.
        val firstWord = clean.substringBefore(' ')
        return if (firstWord.length >= 3) searchList("name:$firstWord*") else emptyList()
    }

    override suspend fun searchByDescription(text: String): List<Card> {
        val clean = sanitize(text)
        return searchList("flavorText:\"*$clean*\" OR abilities.text:\"*$clean*\"")
    }

    private suspend fun searchList(query: String): List<Card> =
        api.search(query).bodyOrNullIfNotFound()
            ?.getAsJsonArray("data")
            ?.map { toCard(it.asJsonObject) }
            .orEmpty()

    private fun sanitize(text: String) = text.trim().replace("\"", "").replace("*", "")

    private fun toCard(json: JsonObject): Card {
        val dto = Json.gson.fromJson(json, PokemonCardDto::class.java)

        val stats = linkedMapOf<String, String>()
        dto.hp?.let { stats["HP"] = it }
        dto.types?.takeIf { it.isNotEmpty() }?.let { stats["Type"] = it.joinToString(", ") }
        dto.evolvesFrom?.let { stats["Evolves from"] = it }
        dto.attacks?.forEach { attack ->
            val cost = attack.cost?.takeIf { it.isNotEmpty() }?.joinToString(" ", "[", "]")
            val damage = attack.damage?.takeIf { it.isNotBlank() }?.let { "$it damage" }
            stats["Attack: ${attack.name}"] = listOfNotNull(cost, damage).joinToString(" — ").ifBlank { "—" }
        }
        dto.weaknesses?.takeIf { it.isNotEmpty() }
            ?.let { list -> stats["Weakness"] = list.joinToString { "${it.type} ${it.value}" } }
        dto.resistances?.takeIf { it.isNotEmpty() }
            ?.let { list -> stats["Resistance"] = list.joinToString { "${it.type} ${it.value}" } }
        dto.retreatCost?.let { stats["Retreat cost"] = it.size.toString() }
        dto.regulationMark?.let { stats["Regulation mark"] = it }
        dto.set?.series?.let { stats["Series"] = it }

        val description = buildList {
            dto.abilities?.forEach { add("${it.type ?: "Ability"}: ${it.name}\n${it.text.orEmpty()}") }
            dto.attacks?.forEach { a -> a.text?.takeIf { it.isNotBlank() }?.let { add("${a.name}: $it") } }
            dto.rules?.let { addAll(it) }
        }.joinToString("\n\n").ifBlank { null }

        val prices = dto.cardmarket?.prices.orEmpty()
            .filterKeys { it in PRICE_KEYS }
            .mapNotNull { (k, v) -> v?.let { "Cardmarket $k (EUR)" to "%.2f".format(it) } }
            .toMap()

        return Card(
            source = id,
            externalId = dto.id.orEmpty(),
            game = game,
            name = dto.name.orEmpty(),
            setCode = dto.set?.ptcgoCode ?: dto.set?.id?.uppercase(),
            setName = dto.set?.name,
            collectorNumber = dto.number,
            rarity = dto.rarity,
            typeLine = listOfNotNull(dto.supertype, dto.subtypes?.joinToString(" "))
                .joinToString(" · ").ifBlank { null },
            cost = null,
            description = description,
            flavorText = dto.flavorText,
            artist = dto.artist,
            imageUrl = dto.images?.large ?: dto.images?.small,
            stats = stats,
            prices = prices,
            rawJson = json.toString(),
        )
    }

    private companion object {
        val PRICE_KEYS = setOf("averageSellPrice", "trendPrice", "lowPrice")
    }
}
