package com.jetsynthesys.rightlife.ui.challenge

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.WindowManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.jetsynthesys.rightlife.databinding.ActivityChallengeRewardBinding
import com.jetsynthesys.rightlife.databinding.BottomSheetChallengeBinding
import com.jetsynthesys.rightlife.databinding.BottomsheetScoreInfoBinding
import com.jetsynthesys.rightlife.databinding.BottomsheetTaskInfoBinding

object ChallengeBottomSheetHelper {
    fun showChallengeInfoBottomSheet(activity: Activity) {

        val bottomSheetDialog = BottomSheetDialog(activity)
        val binding = ActivityChallengeRewardBinding.inflate(activity.layoutInflater)
        bottomSheetDialog.setContentView(binding.root)

        bottomSheetDialog.setOnShowListener { dialog ->

            val bottomSheet =
                (dialog as BottomSheetDialog)
                    .findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)

            bottomSheet?.let {
                val behavior = BottomSheetBehavior.from(it)

                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.isHideable = false
                behavior.skipCollapsed = true
                behavior.isDraggable = false   // 🔥 prevents swipe down
            }
        }

        // Dim background
        bottomSheetDialog.window?.apply {
            setDimAmount(0.7f)
            addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        binding.btnClose.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    fun showScoreInfoBottomSheet(activity: Activity) {

        val bottomSheetDialog = BottomSheetDialog(activity)
        val binding = BottomsheetScoreInfoBinding.inflate(activity.layoutInflater)
        bottomSheetDialog.setContentView(binding.root)

        bottomSheetDialog.setOnShowListener { dialog ->

            val bottomSheet =
                (dialog as BottomSheetDialog)
                    .findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)

            bottomSheet?.let {
                val behavior = BottomSheetBehavior.from(it)

                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.isHideable = false
                behavior.skipCollapsed = true
                //behavior.isDraggable = false   // 🔥 prevents swipe down
            }
        }

        // Dim background
        bottomSheetDialog.window?.apply {
            setDimAmount(0.7f)
            addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        binding.btnClose.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    fun showTaskInfoBottomSheet(activity: Activity) {

        val bottomSheetDialog = BottomSheetDialog(activity)
        val binding = BottomsheetTaskInfoBinding.inflate(activity.layoutInflater)
        bottomSheetDialog.setContentView(binding.root)

        bottomSheetDialog.setOnShowListener { dialog ->

            val bottomSheet =
                (dialog as BottomSheetDialog)
                    .findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)

            bottomSheet?.let {
                val behavior = BottomSheetBehavior.from(it)

                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.isHideable = false
                behavior.skipCollapsed = true
                //behavior.isDraggable = false   // 🔥 prevents swipe down
            }
        }

        // Dim background
        bottomSheetDialog.window?.apply {
            setDimAmount(0.7f)
            addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        binding.btnClose.setOnClickListener {
            bottomSheetDialog.dismiss()
        }
        bottomSheetDialog.show()
    }
}