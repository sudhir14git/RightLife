package com.jetsynthesys.rightlife

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast

fun Context.showCustomToast(message: String, isSuccess: Boolean = false) {
    val inflater = LayoutInflater.from(this)
    val layout: View = inflater.inflate(R.layout.custom_toast_new, null)

    // Set message text
    val toastText: TextView = layout.findViewById(R.id.tvMessage)
    toastText.text = message

    // Set icon based on success/failure
    val imgIcon: ImageView = layout.findViewById(R.id.ivIcon)
    imgIcon.setImageResource(
        if (isSuccess) R.drawable.breathing_green_tick
        else R.drawable.close_journal
    )

    // Create and show toast
    Toast(applicationContext).apply {
        duration = Toast.LENGTH_SHORT
        view = layout
        show()
    }
}