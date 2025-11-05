package com.jetsynthesys.rightlife.ai_package.ui.sleepright

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment

private fun Fragment.safeShowDialog(
    dialog: DialogFragment,
    tag: String
) {
    if (!isAdded) return
    if (parentFragmentManager.isStateSaved) return
    dialog.show(parentFragmentManager, tag)
}
