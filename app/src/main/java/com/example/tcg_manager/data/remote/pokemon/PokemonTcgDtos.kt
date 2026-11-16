package com.example.tcg_manager.data.remote.pokemon

/** Subset of a pokemontcg.io card object that the app shows. The full JSON is saved separately. */
data class PokemonCardDto(
    val id: String?,
    val name: String?,
    val supertype: String?,
    val subtypes: List<String>?,
    val hp: String?,
    val types: List<String>?,
    val evolvesFrom: String?,
    val rules: List<String>?,
    val abilities: List<PokemonAbilityDto>?,
    val attacks: List<PokemonAttackDto>?,
    val weaknesses: List<PokemonTypeValueDto>?,
    val resistances: List<PokemonTypeValueDto>?,
    val retreatCost: List<String>?,
    val set: PokemonSetDto?,
    val number: String?,
    val artist: String?,
    val rarity: String?,
    val flavorText: String?,
    val regulationMark: String?,
    val images: PokemonImagesDto?,
    val cardmarket: PokemonMarketDto?,
)

data class PokemonAbilityDto(val name: String?, val text: String?, val type: String?)

data class PokemonAttackDto(
    val name: String?,
    val cost: List<String>?,
    val damage: String?,
    val text: String?,
)

data class PokemonTypeValueDto(val type: String?, val value: String?)

data class PokemonSetDto(val id: String?, val name: String?, val series: String?, val ptcgoCode: String?)

data class PokemonImagesDto(val small: String?, val large: String?)

data class PokemonMarketDto(val prices: Map<String, Double?>?)
