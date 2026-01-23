package com.jetsynthesys.rightlife.ai_package.ui.eatright

import android.R.color.transparent
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import com.jetsynthesys.rightlife.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class MealSaveQuitBottomSheet : BottomSheetDialogFragment() {

    private var listener: OnMealSaveQuitListener? = null
    private var loadingOverlay : FrameLayout? = null

    interface OnMealSaveQuitListener {
        fun onMealSaveQuit(mealData: String)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = when {
            context is OnMealSaveQuitListener -> context
            parentFragment is OnMealSaveQuitListener -> parentFragment as OnMealSaveQuitListener
            else -> throw ClassCastException("$context must implement OnMealSaveQuitListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_delete_meal_bottom_sheet, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val dialog = BottomSheetDialog(requireContext(), R.style.LoggedBottomSheetDialogTheme)
        dialog.setContentView(R.layout.fragment_frequently_logged)
        dialog.window?.setBackgroundDrawableResource(transparent)
        val deleteTitle = view.findViewById<TextView>(R.id.deleteTitle)
        val deleteConfirmTv = view.findViewById<TextView>(R.id.deleteConfirmTv)
        val deleteBtnLayout = view.findViewById<ConstraintLayout>(R.id.deleteBtnLayout)
        val yesBtn = view.findViewById<LinearLayoutCompat>(R.id.yesBtn)
        val noBtn = view.findViewById<LinearLayoutCompat>(R.id.noBtn)
        deleteTitle.text = "Are You Sure You Want To Quit?"
        deleteConfirmTv.text = "Your changes were not saved. You can still continue."

        noBtn.setOnClickListener {
            dismiss()
        }

        yesBtn.setOnClickListener {
            listener?.onMealSaveQuit("")
            dismiss()
        }
    }

    companion object {
        const val TAG = "LoggedBottomSheet"
        @JvmStatic
        fun newInstance() = MealSaveQuitBottomSheet().apply {
            arguments = Bundle().apply {
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }
}

