package com.example.tcg_manager

import android.content.Context
import com.example.tcg_manager.data.local.db.AppDatabase
import com.example.tcg_manager.data.remote.CardApiRegistry
import com.example.tcg_manager.data.remote.mtg.MtgIoProvider
import com.example.tcg_manager.data.remote.pokemon.PokemonTcgProvider
import com.example.tcg_manager.data.repository.CardSearchRepository
import com.example.tcg_manager.data.repository.CollectionRepository
import com.example.tcg_manager.scanner.CardTextRecognizer

/** Creates the app's long-lived objects once. Access it through [TcgManagerApp.container]. */
class AppContainer(context: Context) {

    private val database by lazy { AppDatabase.create(context) }

    /** Every card API the app can search. Add new providers to this list. */
    val apiRegistry by lazy {
        CardApiRegistry(
            listOf(
                MtgIoProvider(),
                PokemonTcgProvider(),
            )
        )
    }

    val textRecognizer by lazy { CardTextRecognizer() }

    val cardSearchRepository by lazy { CardSearchRepository(apiRegistry, textRecognizer) }

    val collectionRepository by lazy { CollectionRepository(database) }
}
