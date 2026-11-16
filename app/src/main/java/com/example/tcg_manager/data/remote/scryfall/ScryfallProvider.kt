package com.example.tcg_manager.data.remote.scryfall

import com.example.tcg_manager.data.model.Card
import com.example.tcg_manager.data.model.CardGame
import com.example.tcg_manager.data.model.SearchType
import com.example.tcg_manager.data.remote.CardApiProvider
import com.example.tcg_manager.data.remote.HttpClients
import com.example.tcg_manager.data.remote.bodyOrNullIfNotFound
import com.example.tcg_manager.util.Json
import com.google.gson.JsonObject

/** Magic: The Gathering via Scryfall. Supports id, name and description; no image search. */
class ScryfallProvider(
    private val api: ScryfallApi =
        HttpClients.retrofit(ScryfallApi.BASE_URL).create(ScryfallApi::class.java),
) : CardApiProvider {

    override val id = "scryfall"
    override val displayName = "Scryfall"
    override val game = CardGame.MAGIC
    override val capabilities = setOf(SearchType.ID, SearchType.NAME, SearchType.DESCRIPTION)

    override suspend fun searchById(id: String): Card? =
        api.cardById(id.trim()).bodyOrNullIfNotFound()?.let(::toCard)

    override suspend fun searchByName(name: String): List<Card> {
        val clean = name.trim()
        // Fuzzy match first: best for OCR text with small mistakes.
        api.cardNamedFuzzy(clean).bodyOrNullIfNotFound()?.let { return listOf(toCard(it)) }
        // Otherwise list every card whose name contains the words.
        return searchList(clean)
    }

    override suspend fun searchByDescription(text: String): List<Card> =
        searchList("o:\"${text.trim().replace("\"", "")}\"")

    private suspend fun searchList(query: String): List<Card> {
        val body = api.search(query).bodyOrNullIfNotFound() ?: return emptyList()
        return body.getAsJsonArray("data")
            ?.map { toCard(it.asJsonObject) }
            .orEmpty()
            .take(MAX_RESULTS)
    }

    private fun toCard(json: JsonObject): Card {
        val dto = Json.gson.fromJson(json, ScryfallCardDto::class.java)
        val faces = dto.cardFaces.orEmpty()
        val front = faces.firstOrNull()

        fun joinFaces(own: String?, pick: (ScryfallCardFaceDto) -> String?): String? =
            own ?: faces.mapNotNull(pick).filter { it.isNotBlank() }
                .takeIf { it.isNotEmpty() }?.joinToString("\n//\n")

        val stats = linkedMapOf<String, String>()
        (dto.power ?: front?.power)?.let { stats["Power"] = it }
        (dto.toughness ?: front?.toughness)?.let { stats["Toughness"] = it }
        (dto.loyalty ?: front?.loyalty)?.let { stats["Loyalty"] = it }
        dto.cmc?.let { stats["Mana value"] = if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() }
        dto.colors?.takeIf { it.isNotEmpty() }?.let { stats["Colors"] = it.joinToString(", ") }
        dto.lang?.let { stats["Language"] = it }
        dto.releasedAt?.let { stats["Released"] = it }

        val prices = dto.prices.orEmpty()
            .mapNotNull { (k, v) -> v?.let { k.uppercase().replace('_', ' ') to it } }
            .toMap()

        return Card(
            source = id,
            externalId = dto.id.orEmpty(),
            game = game,
            name = dto.name.orEmpty(),
            setCode = dto.set?.uppercase(),
            setName = dto.setName,
            collectorNumber = dto.collectorNumber,
            rarity = dto.rarity?.replaceFirstChar { it.uppercase() },
            typeLine = joinFaces(dto.typeLine) { it.typeLine },
            cost = joinFaces(dto.manaCost?.takeIf { it.isNotBlank() }) { it.manaCost },
            description = joinFaces(dto.oracleText) { it.oracleText },
            flavorText = joinFaces(dto.flavorText) { it.flavorText },
            artist = dto.artist,
            imageUrl = (dto.imageUris ?: front?.imageUris)?.let { it.normal ?: it.large ?: it.small },
            stats = stats,
            prices = prices,
            rawJson = json.toString(),
        )
    }

    private companion object {
        const val MAX_RESULTS = 30
    }
}
