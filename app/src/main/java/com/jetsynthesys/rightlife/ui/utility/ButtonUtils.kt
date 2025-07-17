package com.jetsynthesys.rightlife.ui.utility

import android.view.View

fun View.disableViewForSeconds() {
    this.isEnabled = false
    postDelayed({ isEnabled = true }, 3000)
}