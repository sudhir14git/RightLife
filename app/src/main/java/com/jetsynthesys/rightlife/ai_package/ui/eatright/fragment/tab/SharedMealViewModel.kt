package com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment.tab

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jetsynthesys.rightlife.ai_package.model.request.SnapMealLogRequest
import com.jetsynthesys.rightlife.ai_package.ui.eatright.model.MealLogItems
import com.jetsynthesys.rightlife.ai_package.ui.eatright.model.SelectedMealLogList

class SharedMealViewModel : ViewModel() {

    private val _recipeLogAndFrequentlyData = MutableLiveData<List<MealLogItems>>()
    val recipeLogAndFrequentlyData: LiveData<List<MealLogItems>> get() = _recipeLogAndFrequentlyData

    private val _mealLogData = MutableLiveData<List<SelectedMealLogList>>()
    val mealLogData: LiveData<List<SelectedMealLogList>> get() = _mealLogData

    private val _snapMealData = MutableLiveData< List<SnapMealLogRequest>>()
    val snapMealData: LiveData< List<SnapMealLogRequest>> get() = _snapMealData

    fun recipeLogAndFrequentlyLogUpdateMealData(data: List<MealLogItems>) {
        _recipeLogAndFrequentlyData.value = data
    }

    fun mealLogUpdateMealData(data: List<SelectedMealLogList>) {
        _mealLogData.value = data
    }

    fun snapMealLogUpdateMealData(data: List<SnapMealLogRequest>) {
        _snapMealData.value = data
    }
}
