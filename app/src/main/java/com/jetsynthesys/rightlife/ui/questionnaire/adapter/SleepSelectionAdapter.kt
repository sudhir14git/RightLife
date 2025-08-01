package com.jetsynthesys.rightlife.ui.questionnaire.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.databinding.ItemSleepOptionBinding
import com.jetsynthesys.rightlife.ui.questionnaire.pojo.SleepOption
import com.jetsynthesys.rightlife.ui.utility.disableViewForSeconds

class SleepSelectionAdapter(
    private val options: List<SleepOption>,
    private val onItemSelected: (SleepOption) -> Unit
) : RecyclerView.Adapter<SleepSelectionAdapter.SleepViewHolder>() {

    private var selectedPosition = RecyclerView.NO_POSITION

    inner class SleepViewHolder(val binding: ItemSleepOptionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(option: SleepOption, isSelected: Boolean) {
            binding.imageView.setImageResource(option.imageRes)
            binding.titleTextView.text = option.title
            binding.subtitleTextView.text = option.subtitle

            // Change background based on selection
            if (isSelected) {
                binding.root.setBackgroundResource(R.drawable.bg_item_selected_sleep_right)
            } else {
                binding.root.setBackgroundResource(R.drawable.bg_food_item)
            }

            binding.itemContainer.setOnClickListener {
                binding.itemContainer.disableViewForSeconds()
                val previousPosition = selectedPosition
                selectedPosition = adapterPosition
                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)
                onItemSelected(option)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SleepViewHolder {
        val binding = ItemSleepOptionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SleepViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SleepViewHolder, position: Int) {
        holder.bind(options[position], position == selectedPosition)
    }

    override fun getItemCount(): Int = options.size
}
