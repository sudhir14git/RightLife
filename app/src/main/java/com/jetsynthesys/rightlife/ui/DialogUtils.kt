package com.jetsynthesys.rightlife.ui

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.os.Looper
import android.text.Html
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.databinding.DialogChecklistQuestionsBinding
import com.jetsynthesys.rightlife.databinding.DialogJournalCommonBinding
import com.jetsynthesys.rightlife.databinding.DialogPlaylistCreatedBinding
import com.jetsynthesys.rightlife.databinding.DialogSwitchAccountBinding
import com.jetsynthesys.rightlife.databinding.DisclaimerCommonDialogBinding

object DialogUtils {

    fun showJournalCommonDialog(context: Context, header: String, htmlText: String) {
        val dialog = Dialog(context)
        val binding = DialogJournalCommonBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(binding.root)
        dialog.setCancelable(true)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val window = dialog.window
        // Set the dim amount
        val layoutParams = window!!.attributes
        layoutParams.dimAmount = 0.7f // Adjust the dim amount (0.0 - 1.0)
        window.attributes = layoutParams

        binding.tvTitle.text = header
        binding.tvDescription.text = Html.fromHtml(htmlText, Html.FROM_HTML_MODE_LEGACY)

        // Handle close button click
        binding.btnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    fun showCheckListQuestionCommonDialog(
        context: Context,
        header: String = "Finish Checklist to Unlock"
    ) {
        val dialog = Dialog(context)
        val binding = DialogChecklistQuestionsBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(binding.root)
        dialog.setCancelable(true)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val window = dialog.window
        // Set the dim amount
        val layoutParams = window!!.attributes
        layoutParams.dimAmount = 0.7f // Adjust the dim amount (0.0 - 1.0)
        window.attributes = layoutParams

        binding.titleText.text = header

        // Handle close button click
        binding.btnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    fun showSuccessDialog(
        activity: Activity,
        message: String,
        desc: String = ""
    ) {
        // Inflate the view binding for the dialog layout
        val inflater = LayoutInflater.from(activity)
        val binding = DialogPlaylistCreatedBinding.inflate(inflater)

        // Create the dialog and set the root view
        val dialog = Dialog(activity)
        dialog.setContentView(binding.root)
        dialog.setCancelable(true)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Dim background
        val window = dialog.window
        val layoutParams = window?.attributes
        layoutParams?.dimAmount = 0.7f
        window?.attributes = layoutParams

        // Set full width
        val displayMetrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
        val width = displayMetrics.widthPixels
        layoutParams?.width = width

        // Set message using view binding
        binding.tvDialogPlaylistCreated.text = message

        if (desc.isNotEmpty()) {
            binding.tvDialogDescription.visibility = View.VISIBLE
            binding.tvDialogDescription.text = desc
        }

        dialog.show()

        Handler(Looper.getMainLooper()).postDelayed({
            dialog.dismiss()
            activity.finish()
        }, 2000)
    }


    fun showSwitchAccountDialog(context: Context, header: String, htmlText: String) {
        val dialog = Dialog(context)
        val binding = DialogSwitchAccountBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(binding.root)
        dialog.setCancelable(true)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val window = dialog.window
        // Set the dim amount
        val layoutParams = window!!.attributes
        layoutParams.dimAmount = 0.7f // Adjust the dim amount (0.0 - 1.0)
        window.attributes = layoutParams

        /*binding.tvTitle.text = header
        binding.tvDescription.text = Html.fromHtml(htmlText, Html.FROM_HTML_MODE_LEGACY)*/

        // Handle close button click
        binding.btnOk.setOnClickListener {
            dialog.dismiss()
        }
        binding.btnSwitchAccount.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    fun showCommonBottomSheetDialog(
        context: Context,
        description: String,
        header: String = "Disclaimer",
        btnText: String = "Okay",
        onOkayClick: (() -> Unit)? = null,
        onCloseClick: (() -> Unit)? = null
    ) {
        val bottomSheetDialog = BottomSheetDialog(context)
        // Inflate the BottomSheet layout
        val binding = DisclaimerCommonDialogBinding.inflate(LayoutInflater.from(context))
        val bottomSheetView = binding.root
        bottomSheetDialog.setContentView(bottomSheetView)

        // Set up the animation
        val bottomSheetLayout = bottomSheetView.findViewById<LinearLayout>(R.id.design_bottom_sheet)
        if (bottomSheetLayout != null) {
            val slideUpAnimation: Animation =
                AnimationUtils.loadAnimation(context, R.anim.bottom_sheet_slide_up)
            bottomSheetLayout.animation = slideUpAnimation
        }

        bottomSheetDialog.setCancelable(false)
        bottomSheetDialog.setCanceledOnTouchOutside(false)

        // ✅ Set dim background manually for safety
        bottomSheetDialog.window?.let { window ->
            val layoutParams = window.attributes
            layoutParams.dimAmount = 0.7f // 0.0 to 1.0
            window.attributes = layoutParams
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        binding.tvTitle.text = header
        binding.tvDescription.text = description
        binding.btnOkay.text = btnText

        binding.btnOkay.setOnClickListener {
            bottomSheetDialog.dismiss()
            onOkayClick?.invoke()
        }

        binding.btnClose.setOnClickListener {
            bottomSheetDialog.dismiss()
            onCloseClick?.invoke()
        }

        bottomSheetDialog.show()
    }


}