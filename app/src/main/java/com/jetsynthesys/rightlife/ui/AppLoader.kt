package com.jetsynthesys.rightlife.ui

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Window
import com.jetsynthesys.rightlife.R

object AppLoader {

    private var dialog: Dialog? = null

    fun show(context: Context) {
        if (dialog?.isShowing == true) return

        dialog = Dialog(context).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setCancelable(false)
            setContentView(R.layout.dialiog_loader_layout)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
        }
    }

    fun hide() {
        dialog?.let {
            if (it.isShowing) it.dismiss()
        }
        dialog = null
    }
}
