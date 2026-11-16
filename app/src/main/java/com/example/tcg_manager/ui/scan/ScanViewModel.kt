package com.example.tcg_manager.ui.scan

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.tcg_manager.TcgManagerApp
import com.example.tcg_manager.data.model.Card
import com.example.tcg_manager.data.model.CardGame
import com.example.tcg_manager.data.model.SearchType
import com.example.tcg_manager.data.repository.CardSearchRepository
import com.example.tcg_manager.data.repository.SearchOutcome
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Runs searches (camera or manual) and holds the card the user picked.
 * Scoped to the activity so the detail screen can read [selectedCard].
 */
class ScanViewModel(private val repository: CardSearchRepository) : ViewModel() {

    sealed interface State {
        data object Idle : State
        data class Loading(val message: String) : State
        data class Done(val outcome: SearchOutcome) : State
    }

    private val _state = MutableStateFlow<State>(State.Idle)
    val state: StateFlow<State> = _state.asStateFlow()

    private val _selectedCard = MutableStateFlow<Card?>(null)
    val selectedCard: StateFlow<Card?> = _selectedCard.asStateFlow()

    fun search(type: SearchType, query: String, game: CardGame?) =
        run("Searching…") { repository.search(type, query, game) }

    fun searchByImage(image: Bitmap, game: CardGame?) =
        run("Reading card…") { repository.searchByImage(image, game) }

    fun select(card: Card) {
        _selectedCard.value = card
    }

    /** Call once the UI has shown a [State.Done] result so it isn't shown again. */
    fun resultHandled() {
        _state.value = State.Idle
    }

    private fun run(message: String, block: suspend () -> SearchOutcome) {
        if (_state.value is State.Loading) return
        _state.value = State.Loading(message)
        viewModelScope.launch {
            _state.value = try {
                State.Done(block())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                State.Done(SearchOutcome(emptyList(), listOf(e.message ?: e.javaClass.simpleName)))
            }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                ScanViewModel((this[APPLICATION_KEY] as TcgManagerApp).container.cardSearchRepository)
            }
        }
    }
}
