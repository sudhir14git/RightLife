package com.jetsynthesys.rightlife.ui.mindaudit

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jetsynthesys.rightlife.databinding.RowHelplineBinding

class HelpLineAdapter(
    private val context: Context,
    private val items: List<Organization>,
    private val isForState: Boolean = false
) : RecyclerView.Adapter<HelpLineAdapter.CountryViewHolder>() {

    inner class CountryViewHolder(val binding: RowHelplineBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountryViewHolder {
        val binding =
            RowHelplineBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CountryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CountryViewHolder, position: Int) {
        val item = items[position]
        with(holder.binding) {
            tvTiming.text = item.hours
            tvOrganizationName.text = item.name
            val phoneNumbers = item.hotline?.split(",")
            if (phoneNumbers?.size == 1) {
                tvPhoneNumber1.text = item.hotline
                rlPhoneNumber2.visibility = View.GONE
            } else {
                tvPhoneNumber1.text = phoneNumbers?.get(0) ?: ""
                tvPhoneNumber2.text = phoneNumbers?.get(1) ?: ""
            }

            if (isForState) {
                rlHelpLine.setBackgroundColor(Color.parseColor(if (position % 2 == 0) "#FFA8A5" else "#FEB4B2"))
            }

            rlPhoneNumber1.setOnClickListener {
                if (tvPhoneNumber1.text.isNullOrEmpty()) return@setOnClickListener
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:${tvPhoneNumber1.text}")
                }
                context.startActivity(intent)
            }
        }
    }

    override fun getItemCount() = items.size
}
