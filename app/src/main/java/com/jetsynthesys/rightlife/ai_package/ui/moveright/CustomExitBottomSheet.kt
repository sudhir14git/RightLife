package com.jetsynthesys.rightlife.ai_package.ui.moveright

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.LinearLayoutCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.jetsynthesys.rightlife.R




class ExitConfirmationBottomSheet(
    private val onYesClicked: () -> Unit
) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: android.os.Bundle?
    ): View {
        val view = inflater.inflate(R.layout.bottom_sheet_exit_routine, container, false)

        val btnYes = view.findViewById<LinearLayoutCompat>(R.id.yes_btn_bottom_sheet)
        val btnNo = view.findViewById<LinearLayoutCompat>(R.id.layout_btn_log_meal)

        btnYes.setOnClickListener {
            onYesClicked()
            dismiss()
        }

        btnNo.setOnClickListener {
            dismiss()
        }

        return view
    }
}
