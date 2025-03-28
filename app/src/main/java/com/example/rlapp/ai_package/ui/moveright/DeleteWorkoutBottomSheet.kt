package com.example.rlapp.ai_package.ui.moveright

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.rlapp.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class DeleteWorkoutBottomSheet: BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_edit_workout_bottom_sheet, container, false)
    }
}