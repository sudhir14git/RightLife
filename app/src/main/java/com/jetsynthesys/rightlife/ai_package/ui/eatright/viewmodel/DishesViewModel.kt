package com.jetsynthesys.rightlife.ai_package.ui.eatright.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DishesViewModel : ViewModel() {

    // Search text
    private val _searchQuery = MutableLiveData<String>()
    val searchQuery: LiveData<String> get() = _searchQuery
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // Selected Meal Type
    private val _selectedMealType = MutableLiveData<String?>()
    val selectedMealType: LiveData<String?> get() = _selectedMealType
    fun setSelectedMealType(type: String?) {
        _selectedMealType.value = type
    }

    // Selected Food Type
    private val _selectedFoodType = MutableLiveData<String?>()
    val selectedFoodType: LiveData<String?> get() = _selectedFoodType
    fun setSelectedFoodType(type: String?) {
        _selectedFoodType.value = type
    }

    // Selected Cuisine
    private val _selectedCuisine = MutableLiveData<String?>()
    val selectedCuisine: LiveData<String?> get() = _selectedCuisine
    fun setSelectedCuisine(type: String?) {
        _selectedCuisine.value = type
    }

    // In DishesViewModel
    private val _selectedTabIndex = MutableLiveData<Int?>()
    val selectedTabIndex: LiveData<Int?> get() = _selectedTabIndex
    fun setSelectedTabIndex(index: Int?) {
        _selectedTabIndex.value = index
    }
}
