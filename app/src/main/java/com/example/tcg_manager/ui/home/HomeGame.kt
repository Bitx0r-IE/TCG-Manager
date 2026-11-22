package com.example.tcg_manager.ui.home

import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import com.example.tcg_manager.R

/**
 * The trading card games shown as widgets on the main screen.
 * Kept separate from CardGame because Yu-Gi-Oh! has no API provider yet.
 */
enum class HomeGame(
    @StringRes val title: Int,
    val initials: String,
    @ColorRes val color: Int,
) {
    MAGIC(R.string.game_magic, "MTG", R.color.game_magic),
    POKEMON(R.string.game_pokemon, "PKM", R.color.game_pokemon),
    YUGIOH(R.string.game_yugioh, "YGO", R.color.game_yugioh),
}
