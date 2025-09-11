package com.jetsynthesys.rightlife.ai_package.ui.eatright.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.jetsynthesys.rightlife.R

class TabContentAdapter(private val onItemClick: (String, Int, Boolean) -> Unit) :
    RecyclerView.Adapter<TabContentAdapter.ViewHolder>() {

    private var items: List<String> = emptyList()
    private var selectedPosition: Int = -1

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.tabText)
        val iconClose : ImageView = itemView.findViewById(R.id.iconClose)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recipe_search_custom_tab_content, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position < items.size) {
            val item = items[position]
            holder.textView.text = item.substringBefore("_")
            // Set the selected state based on the selectedPosition
            if (position == selectedPosition){
                holder.iconClose.visibility = View.VISIBLE
                holder.textView.isSelected = (position == selectedPosition)
            }else{
                holder.textView.isSelected = false
                holder.iconClose.visibility = View.GONE
            }

            holder.itemView.setOnClickListener {
                if (holder.adapterPosition != RecyclerView.NO_POSITION && holder.adapterPosition < itemCount) {
                    if (holder.iconClose.isVisible){
                        val previousPosition = selectedPosition
                        selectedPosition = -1//holder.adapterPosition
                        // Notify changes for the previous and current positions
                        holder.iconClose.visibility = View.GONE
                        notifyItemChanged(-1)
                        notifyItemChanged(-1)
                        onItemClick(item, selectedPosition, true)
                    }else{
                        val previousPosition = selectedPosition
                        selectedPosition = holder.adapterPosition
                        // Notify changes for the previous and current positions
                        notifyItemChanged(previousPosition)
                        notifyItemChanged(selectedPosition)
                        onItemClick(item, selectedPosition, false)
                    }
                }
            }

//            holder.itemView.setOnClickListener {
//                if (holder.adapterPosition != RecyclerView.NO_POSITION && holder.adapterPosition < itemCount) {
//                    val previousPosition = selectedPosition
//                    selectedPosition = -1//holder.adapterPosition
//                    // Notify changes for the previous and current positions
//                    holder.iconClose.visibility = View.GONE
//                    notifyItemChanged(-1)
//                    notifyItemChanged(-1)
//                    onItemClick(item, selectedPosition, true)
//                }
//            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<String>) {
        // Only update the items if they are different to avoid unnecessary updates
        if (items != newItems) {
            items = newItems.toList() // Create a new list to avoid concurrent modification
            selectedPosition = -1 // Reset selection only if the list changes
            notifyDataSetChanged() // Notify full data set change since the list has changed
        }
    }

    fun deselectedUpdateItems(newItems: List<String>, position: Int) {
        // Only update the items if they are different to avoid unnecessary updates
        if (items == newItems) {
            items = newItems.toList() // Create a new list to avoid concurrent modification
            selectedPosition = -1 // Reset selection only if the list changes
            notifyDataSetChanged() // Notify full data set change since the list has changed
        }
    }

    fun setSelectedPosition(position: Int) {
        if (position in 0 until itemCount) {
            val previousPosition = selectedPosition
            selectedPosition = position
            // Notify changes for the previous and current positions
            notifyItemChanged(previousPosition)
            notifyItemChanged(selectedPosition)
        }
    }
}