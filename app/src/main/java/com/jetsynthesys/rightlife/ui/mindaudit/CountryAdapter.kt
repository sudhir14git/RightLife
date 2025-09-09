package com.jetsynthesys.rightlife.ui.mindaudit

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jetsynthesys.rightlife.databinding.RowCountryListItemBinding

class CountryAdapter(
    private val items: List<String>,
    private val onClick: (String,Int) -> Unit
) : RecyclerView.Adapter<CountryAdapter.CountryViewHolder>() {

    inner class CountryViewHolder(val binding: RowCountryListItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountryViewHolder {
        val binding =
            RowCountryListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CountryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CountryViewHolder, position: Int) {
        holder.binding.tvCountryName.text = items[position]
        holder.binding.tvCountryName.setOnClickListener { onClick(items[position],position) }

        if (position % 2 == 0) {
            holder.binding.tvCountryName.setBackgroundColor(Color.parseColor("#FFFFFF")) // white
        } else {
            holder.binding.tvCountryName.setBackgroundColor(Color.parseColor("#F7F7FC")) // light gray
        }
    }

    override fun getItemCount() = items.size
}
