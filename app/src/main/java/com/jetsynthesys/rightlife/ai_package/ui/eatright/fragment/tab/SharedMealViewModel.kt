package com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment.tab

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jetsynthesys.rightlife.ai_package.model.request.SnapMealLogRequest
import com.jetsynthesys.rightlife.ai_package.ui.eatright.model.MealLogItems

class SharedMealViewModel : ViewModel() {
    private val _mealData = MutableLiveData<List<MealLogItems>>()
    val mealData: LiveData<List<MealLogItems>> get() = _mealData

    private val _snapMealData = MutableLiveData<SnapMealLogRequest>()
    val snapMealData: LiveData<SnapMealLogRequest> get() = _snapMealData

    fun mealLogUpdateMealData(data: List<MealLogItems>) {
        _mealData.value = data
    }

    fun snapMealLogUpdateMealData(data: SnapMealLogRequest) {
        _snapMealData.value = data
    }
}
