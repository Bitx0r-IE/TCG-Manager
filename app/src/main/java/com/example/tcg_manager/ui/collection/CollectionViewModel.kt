package com.example.tcg_manager.ui.collection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.tcg_manager.TcgManagerApp
import com.example.tcg_manager.data.model.Card
import com.example.tcg_manager.data.model.CardKey
import com.example.tcg_manager.data.model.CollectionCard
import com.example.tcg_manager.data.repository.CollectionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CollectionViewModel(private val repository: CollectionRepository) : ViewModel() {

    val cards: StateFlow<List<CollectionCard>?> = repository.observeCollection()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun quantity(key: CardKey): Flow<Int> = repository.observeQuantity(key)

    fun add(card: Card, amount: Int) {
        viewModelScope.launch { repository.add(card, amount) }
    }

    fun setQuantity(key: CardKey, quantity: Int) {
        viewModelScope.launch { repository.setQuantity(key, quantity) }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                CollectionViewModel((this[APPLICATION_KEY] as TcgManagerApp).container.collectionRepository)
            }
        }
    }
}
