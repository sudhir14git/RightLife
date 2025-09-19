package com.jetsynthesys.rightlife.ui.mindaudit

import android.app.Dialog
import android.os.Bundle
import android.view.ViewGroup
import android.view.Window
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.jetsynthesys.rightlife.R

class CountryListDialog(
    private val countries: List<String>,
    private val name: String,
    private val onSelect: (String, Int) -> Unit
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext(), R.style.FullScreenDialog)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_country_list)

        val recyclerView = dialog.findViewById<RecyclerView>(R.id.rvCountryList)
        val btnOk = dialog.findViewById<Button>(R.id.btnImFine)
        val ivClose = dialog.findViewById<ImageView>(R.id.ivClose)
        val tvTitle = dialog.findViewById<TextView>(R.id.tvTitle)

        tvTitle.text = name

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = CountryAdapter(countries) { country, position ->
            onSelect(country, position)
            dismiss()
        }

        btnOk.setOnClickListener { dismiss() }
        ivClose.setOnClickListener { dismiss() }

        return dialog
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            // Make dialog fill the screen
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

            // ✅ Status bar color
            statusBarColor = ContextCompat.getColor(context, R.color.dialog_status_bar)

            // ✅ Control status bar icon color (light or dark)
            dialog?.window?.decorView?.let { decorView ->
                ViewCompat.getWindowInsetsController(decorView)?.isAppearanceLightStatusBars = false
                // false = light icons, true = dark icons
            }
        }
    }
}
