package com.jetsynthesys.rightlife.ui.NewSleepSounds

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jetsynthesys.rightlife.databinding.ItemSleepCategoryBinding
import com.jetsynthesys.rightlife.ui.NewSleepSounds.newsleepmodel.SleepCategory
import androidx.core.content.ContextCompat
import com.jetsynthesys.rightlife.R


class SleepCategoryAdapter(
    private val categoryList: List<SleepCategory>,
    private val onCategorySelected: (SleepCategory) -> Unit
) : RecyclerView.Adapter<SleepCategoryAdapter.CategoryViewHolder>() {

    private var selectedPosition = 1

    inner class CategoryViewHolder(val binding: ItemSleepCategoryBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemSleepCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categoryList[position]
        holder.binding.textCategory.text = category.title

        if (selectedPosition == position) {
            holder.binding.cardCategory.setBackgroundResource(R.drawable.roundedcornerbluebordersolid) // Selected background
            holder.binding.textCategory.setTextColor(ContextCompat.getColor(holder.binding.root.context, R.color.white))
        } else {
            holder.binding.cardCategory.setBackgroundResource(R.drawable.roundedcornerblueborder) // Selected background
            holder.binding.textCategory.setTextColor(ContextCompat.getColor(holder.binding.root.context, R.color.text_color_app))
        }

        // Handle click
        holder.binding.root.setOnClickListener {
            val previousPosition = selectedPosition
            selectedPosition = position
            notifyItemChanged(previousPosition)
            notifyItemChanged(position)

            // Notify Activity/Fragment
            onCategorySelected.invoke(category)
        }
    }

    fun updateSelectedPosition(newPosition: Int) {
        val previousPosition = selectedPosition
        selectedPosition = newPosition
        notifyDataSetChanged()
    }
    override fun getItemCount(): Int = categoryList.size
}
