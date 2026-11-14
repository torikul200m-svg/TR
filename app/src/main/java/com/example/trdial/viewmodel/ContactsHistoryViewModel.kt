package com.example.trdial.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.trdial.data.local.CallHistoryEntity
import com.example.trdial.data.local.ContactEntity
import com.example.trdial.data.repository.CallHistoryRepository
import com.example.trdial.data.repository.ContactRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ContactsHistoryViewModel(
    private val contactRepository: ContactRepository,
    private val callHistoryRepository: CallHistoryRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val contacts: StateFlow<List<ContactEntity>> = _searchQuery
        .flatMapLatest { query -> contactRepository.observeContacts(query) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favorites: StateFlow<List<ContactEntity>> = contactRepository.observeFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentCalls: StateFlow<List<CallHistoryEntity>> = callHistoryRepository.observeRecent(5)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCalls: StateFlow<List<CallHistoryEntity>> = callHistoryRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun saveContact(contact: ContactEntity) {
        if (contact.name.isBlank()) return
        viewModelScope.launch {
            contactRepository.saveContact(contact)
        }
    }

    fun deleteContact(contact: ContactEntity) {
        viewModelScope.launch {
            contactRepository.deleteContact(contact)
        }
    }

    fun toggleFavorite(contact: ContactEntity) {
        viewModelScope.launch {
            contactRepository.setFavorite(contact.id, !contact.isFavorite)
        }
    }

    fun deleteHistoryEntry(id: Long) {
        viewModelScope.launch {
            callHistoryRepository.deleteEntry(id)
        }
    }

    fun clearCallHistory() {
        viewModelScope.launch {
            callHistoryRepository.clearAll()
        }
    }

    class Factory(
        private val contactRepository: ContactRepository,
        private val callHistoryRepository: CallHistoryRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ContactsHistoryViewModel(contactRepository, callHistoryRepository) as T
    }
}
