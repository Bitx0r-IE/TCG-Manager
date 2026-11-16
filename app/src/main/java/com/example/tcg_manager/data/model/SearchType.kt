package com.example.tcg_manager.data.model

/** The kinds of search an API may (or may not) support. */
enum class SearchType(val displayName: String) {
    ID("ID"),
    NAME("Name"),
    DESCRIPTION("Description"),
    IMAGE("Image");

    override fun toString() = displayName
}
