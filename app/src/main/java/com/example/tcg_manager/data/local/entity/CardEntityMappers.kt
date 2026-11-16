package com.example.tcg_manager.data.local.entity

import com.example.tcg_manager.data.model.Card
import com.example.tcg_manager.data.model.CollectionCard

fun CardEntity.toCard() = Card(
    source = source,
    externalId = externalId,
    game = game,
    name = name,
    setCode = setCode,
    setName = setName,
    collectorNumber = collectorNumber,
    rarity = rarity,
    typeLine = typeLine,
    cost = cost,
    description = description,
    flavorText = flavorText,
    artist = artist,
    imageUrl = imageUrl,
    stats = stats,
    prices = prices,
    rawJson = rawJson,
)

fun CardEntity.toCollectionCard() = CollectionCard(
    card = toCard(),
    quantity = quantity,
    addedAt = addedAt,
    updatedAt = updatedAt,
)

fun Card.toEntity(quantity: Int, addedAt: Long, updatedAt: Long) = CardEntity(
    source = source,
    externalId = externalId,
    game = game,
    name = name,
    setCode = setCode,
    setName = setName,
    collectorNumber = collectorNumber,
    rarity = rarity,
    typeLine = typeLine,
    cost = cost,
    description = description,
    flavorText = flavorText,
    artist = artist,
    imageUrl = imageUrl,
    stats = stats,
    prices = prices,
    rawJson = rawJson,
    quantity = quantity,
    addedAt = addedAt,
    updatedAt = updatedAt,
)
