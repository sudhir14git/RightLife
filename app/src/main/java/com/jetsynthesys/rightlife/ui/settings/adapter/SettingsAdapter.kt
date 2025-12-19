package com.jetsynthesys.rightlife.ui.settings.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jetsynthesys.rightlife.databinding.RowSettingsBinding
import com.jetsynthesys.rightlife.ui.settings.pojo.SettingItem

class SettingsAdapter(
    private val items: List<SettingItem>,
    private val itemDesc: List<String>? = null,
    private val onItemClick: (SettingItem) -> Unit
) : RecyclerView.Adapter<SettingsAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: RowSettingsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SettingItem) {
            binding.settingTitle.text = item.title
            if (itemDesc?.isNotEmpty() == true) {
                binding.tvDesc.visibility = View.VISIBLE
                binding.tvDesc.text = itemDesc[bindingAdapterPosition]
                if ("RightLife Pro Plans".equals(
                        itemDesc[bindingAdapterPosition],
                        ignoreCase = true
                    )
                )
                    binding.llPlansDesc.visibility = View.VISIBLE
            } else {
                binding.tvDesc.visibility = View.GONE
                binding.llPlansDesc.visibility = View.GONE
            }
            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RowSettingsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}
