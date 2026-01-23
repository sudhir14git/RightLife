package com.jetsynthesys.rightlife.ui

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Window
import com.jetsynthesys.rightlife.R

object AppLoader {

    private var dialog: Dialog? = null

    fun show(activity: Activity) {
        if (activity.isFinishing || activity.isDestroyed) return
        if (dialog?.isShowing == true) return

        dialog = Dialog(activity).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setCancelable(false)
            setContentView(R.layout.dialiog_loader_layout)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
        }
    }

    fun hide(activity: Activity?) {
        if (activity == null) return
        if (activity.isFinishing || activity.isDestroyed) return

        activity.runOnUiThread {
            try {
                dialog?.let {
                    if (it.isShowing) {
                        it.dismiss()
                    }
                }
                dialog = null
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
