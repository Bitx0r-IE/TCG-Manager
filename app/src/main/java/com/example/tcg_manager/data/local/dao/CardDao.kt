package com.example.tcg_manager.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.tcg_manager.data.local.entity.CardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CardDao {

    @Query("SELECT * FROM cards ORDER BY name COLLATE NOCASE")
    fun observeAll(): Flow<List<CardEntity>>

    @Query("SELECT * FROM cards WHERE source = :source AND externalId = :externalId")
    suspend fun get(source: String, externalId: String): CardEntity?

    @Query("SELECT quantity FROM cards WHERE source = :source AND externalId = :externalId")
    fun observeQuantity(source: String, externalId: String): Flow<Int?>

    @Upsert
    suspend fun upsert(card: CardEntity)

    @Query(
        "UPDATE cards SET quantity = :quantity, updatedAt = :updatedAt " +
            "WHERE source = :source AND externalId = :externalId"
    )
    suspend fun updateQuantity(source: String, externalId: String, quantity: Int, updatedAt: Long)

    @Query("DELETE FROM cards WHERE source = :source AND externalId = :externalId")
    suspend fun delete(source: String, externalId: String)
}
