package com.example.tcg_manager.data.repository

import androidx.room.withTransaction
import com.example.tcg_manager.data.local.db.AppDatabase
import com.example.tcg_manager.data.local.entity.toCollectionCard
import com.example.tcg_manager.data.local.entity.toEntity
import com.example.tcg_manager.data.model.Card
import com.example.tcg_manager.data.model.CardKey
import com.example.tcg_manager.data.model.CollectionCard
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** The user's collection: saved cards with all their API data and owned quantity. */
class CollectionRepository(private val db: AppDatabase) {

    private val dao = db.cardDao()

    fun observeCollection(): Flow<List<CollectionCard>> =
        dao.observeAll().map { list -> list.map { it.toCollectionCard() } }

    /** Copies owned of [key]; 0 when the card isn't in the collection. */
    fun observeQuantity(key: CardKey): Flow<Int> =
        dao.observeQuantity(key.source, key.externalId).map { it ?: 0 }

    /**
     * Adds [amount] copies. If the card is already saved, its data is refreshed with the
     * latest API response and the quantity is increased.
     */
    suspend fun add(card: Card, amount: Int = 1) {
        require(amount > 0) { "amount must be positive" }
        val now = System.currentTimeMillis()
        db.withTransaction {
            val existing = dao.get(card.source, card.externalId)
            dao.upsert(
                card.toEntity(
                    quantity = (existing?.quantity ?: 0) + amount,
                    addedAt = existing?.addedAt ?: now,
                    updatedAt = now,
                )
            )
        }
    }

    /** Sets the owned quantity; 0 or less removes the card from the collection. */
    suspend fun setQuantity(key: CardKey, quantity: Int) {
        if (quantity <= 0) dao.delete(key.source, key.externalId)
        else dao.updateQuantity(key.source, key.externalId, quantity, System.currentTimeMillis())
    }

    suspend fun remove(key: CardKey) = dao.delete(key.source, key.externalId)
}
