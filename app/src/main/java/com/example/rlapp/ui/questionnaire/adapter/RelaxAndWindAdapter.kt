package com.example.rlapp.ui.questionnaire.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.rlapp.R
import com.example.rlapp.databinding.ItemRelaxAndWindBinding
import com.example.rlapp.ui.questionnaire.pojo.StressReason

class RelaxAndWindAdapter(
    private val reasons: List<StressReason>,
    private val type: String = "ThinkRight",
    private val onSelectionChanged: (StressReason) -> Unit
) : RecyclerView.Adapter<RelaxAndWindAdapter.ReasonViewHolder>() {

    inner class ReasonViewHolder(private val binding: ItemRelaxAndWindBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(reason: StressReason) {
            binding.tvTitle.text = reason.title
            binding.ivIcon.setImageResource(reason.iconRes)

            if (type == "ThinkRight") {
                val background = if (reason.isSelected)
                    R.drawable.bg_item_selected_think_right
                else
                    R.drawable.bg_food_item
                binding.root.setBackgroundResource(background)
            }

            if (type == "SleepRight") {
                val background = if (reason.isSelected)
                    R.drawable.bg_item_selected_sleep_right
                else
                    R.drawable.bg_food_item
                binding.root.setBackgroundResource(background)
            }

            binding.root.setOnClickListener {
                reason.isSelected = !reason.isSelected
                notifyItemChanged(adapterPosition)
                onSelectionChanged(reason)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReasonViewHolder {
        val binding =
            ItemRelaxAndWindBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReasonViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReasonViewHolder, position: Int) {
        holder.bind(reasons[position])
    }

    override fun getItemCount(): Int = reasons.size
}
