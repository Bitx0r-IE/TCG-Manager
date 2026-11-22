package com.example.tcg_manager.data.remote.mtg

import com.example.tcg_manager.data.model.Card
import com.example.tcg_manager.data.model.CardGame
import com.example.tcg_manager.data.model.SearchType
import com.example.tcg_manager.data.remote.CardApiProvider
import com.example.tcg_manager.data.remote.HttpClients
import com.example.tcg_manager.data.remote.bodyOrNullIfNotFound
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializer
import io.magicthegathering.kotlinsdk.api.MtgCardApiClient
import io.magicthegathering.kotlinsdk.api.deserializer.MtgCardDeserializer
import io.magicthegathering.kotlinsdk.model.card.MtgCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.joda.time.DateTime

/**
 * Magic: The Gathering via magicthegathering.io, using the official mtg-sdk-kotlin.
 *
 * | Search      | How                                                        |
 * |-------------|------------------------------------------------------------|
 * | ID          | SDK `getCard` for multiverse ids; direct call for UUIDs     |
 * | Name        | SDK exact name, then partial name                          |
 * | Description | direct `cards?text=` call (not in the SDK)                 |
 * | Image       | not supported → OCR + name fallback                        |
 *
 * The SDK makes blocking calls, so they run on [Dispatchers.IO].
 */
class MtgIoProvider(
    private val api: MtgIoApi =
        HttpClients.retrofit(MtgIoApi.BASE_URL).create(MtgIoApi::class.java),
) : CardApiProvider {

    override val id = "mtgio"
    override val displayName = "magicthegathering.io"
    override val game = CardGame.MAGIC
    override val capabilities = setOf(SearchType.ID, SearchType.NAME, SearchType.DESCRIPTION)

    override suspend fun searchById(id: String): Card? = withContext(Dispatchers.IO) {
        val clean = id.trim()
        val multiverseId = clean.toIntOrNull()
        if (multiverseId != null) {
            MtgCardApiClient.getCard(multiverseId).bodyOrNullIfNotFound()?.let { toCard(it) }
        } else {
            api.cardById(clean).bodyOrNullIfNotFound()?.getAsJsonObject("card")?.let(::parseRaw)
        }
    }

    override suspend fun searchByName(name: String): List<Card> = withContext(Dispatchers.IO) {
        val clean = name.trim().replace("\"", "")
        val exact = MtgCardApiClient.getCardsByExactName(clean, MAX_RESULTS, FIRST_PAGE)
            .bodyOrNullIfNotFound().orEmpty()
        val cards = exact.ifEmpty {
            MtgCardApiClient.getCardsByPartialName(clean, MAX_RESULTS, FIRST_PAGE)
                .bodyOrNullIfNotFound().orEmpty()
        }
        cards.map { toCard(it) }
    }

    override suspend fun searchByDescription(text: String): List<Card> = withContext(Dispatchers.IO) {
        api.searchByText(text.trim(), MAX_RESULTS).bodyOrNullIfNotFound()
            ?.getAsJsonArray("cards")
            ?.map { parseRaw(it.asJsonObject) }
            .orEmpty()
    }

    /** Parses a card from a direct API call with the SDK's own deserializer; keeps the full raw JSON. */
    private fun parseRaw(json: JsonObject): Card =
        toCard(sdkGson.fromJson(json, MtgCard::class.java), rawJson = json.toString())

    private fun toCard(card: MtgCard, rawJson: String = outGson.toJson(card)): Card {
        // The SDK declares some fields non-null, but the API omits them (e.g. lands have no
        // manaCost). Read every field through orNull() so a missing value can't crash the app.
        val stats = linkedMapOf<String, String>()
        orNull(card.power)?.let { stats["Power"] = it }
        orNull(card.toughness)?.let { stats["Toughness"] = it }
        orNull(card.loyalty)?.let { stats["Loyalty"] = it }
        orNull(card.cmc)?.let { stats["Mana value"] = it.toString() }
        orNull(card.colors)?.takeIf { it.isNotEmpty() }?.let { stats["Colors"] = it.joinToString(", ") }
        orNull(card.colorIdentity)?.takeIf { it.isNotEmpty() }
            ?.let { stats["Color identity"] = it.joinToString(", ") }
        orNull(card.names)?.takeIf { it.size > 1 }?.let { stats["Card faces"] = it.joinToString(" // ") }
        orNull(card.layout)?.let { stats["Layout"] = it }
        orNull(card.multiverseid)?.let { stats["Multiverse ID"] = it.toString() }
        orNull(card.legalities)
            ?.filter { orNull(it.legality).equals("Legal", ignoreCase = true) }
            ?.mapNotNull { orNull(it.format) }
            ?.takeIf { it.isNotEmpty() }
            ?.let { stats["Legal in"] = it.joinToString(", ") }
        orNull(card.rulings)?.takeIf { it.isNotEmpty() }?.let { stats["Rulings"] = it.size.toString() }

        val externalId = orNull(card.id) ?: orNull(card.multiverseid)?.toString().orEmpty()
        return Card(
            source = id,
            externalId = externalId,
            game = game,
            name = orNull(card.name).orEmpty(),
            setCode = orNull(card.set),
            setName = orNull(card.setName),
            collectorNumber = orNull(card.number),
            rarity = orNull(card.rarity),
            typeLine = orNull(card.type),
            cost = orNull(card.manaCost)?.takeIf { it.isNotBlank() },
            description = orNull(card.text),
            // The SDK model has no flavour field; it's only kept when the raw API JSON is available.
            flavorText = flavorFrom(rawJson),
            artist = orNull(card.artist),
            imageUrl = orNull(card.imageUrl)?.replaceFirst("http://", "https://"),
            stats = stats,
            prices = emptyMap(), // magicthegathering.io doesn't provide prices
            rawJson = rawJson,
        )
    }

    private fun flavorFrom(rawJson: String): String? = runCatching {
        outGson.fromJson(rawJson, JsonObject::class.java)?.get("flavor")?.takeIf { it.isJsonPrimitive }?.asString
    }.getOrNull()

    /** Widens a (wrongly) non-null SDK value to nullable so null checks aren't optimised away. */
    private fun <T> orNull(value: T): T? = value

    private companion object {
        const val MAX_RESULTS = 30
        // The API treats page 0 and 1 the same; 1 is the documented first page.
        const val FIRST_PAGE = 1

        /** Same card parsing the SDK uses (handles ruling dates). */
        val sdkGson: Gson = GsonBuilder()
            .registerTypeAdapter(MtgCard::class.java, MtgCardDeserializer())
            .create()

        /** For saving SDK cards as JSON: Joda dates written as ISO strings. */
        val outGson: Gson = GsonBuilder()
            .registerTypeAdapter(
                DateTime::class.java,
                JsonSerializer<DateTime> { src, _, _ -> JsonPrimitive(src.toString()) },
            )
            .create()
    }
}
