package com.jetsynthesys.rightlife.ai_package.ui.eatright.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SearchIngredientsViewModel : ViewModel() {

    // Search text
    private val _searchQuery = MutableLiveData<String>()
    val searchQuery: LiveData<String> get() = _searchQuery
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }
}
