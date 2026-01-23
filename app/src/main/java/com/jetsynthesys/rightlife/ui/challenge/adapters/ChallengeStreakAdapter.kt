package com.jetsynthesys.rightlife.ui.challenge.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.jetsynthesys.rightlife.R

class ChallengeStreakAdapter : RecyclerView.Adapter<ChallengeStreakAdapter.VH>() {

    data class Item(
        val label: String,
        val date: String,
        val status: String // COMPLETED / MISSED
    )

    private val items = mutableListOf<Item>()

    fun submitList(newItems: List<Item>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_streak_journey_row, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val card = itemView.findViewById<CardView>(R.id.cardRoot)
        private val ivStatus = itemView.findViewById<ImageView>(R.id.ivStatus)
        private val tvLabel = itemView.findViewById<TextView>(R.id.tvLabel)
        private val tvDate = itemView.findViewById<TextView>(R.id.tvDate)
        private val ivFlameSmall = itemView.findViewById<ImageView>(R.id.ivFlameSmall)

        fun bind(item: Item) {
            tvLabel.text = item.label
            tvDate.text = item.date

            val isCompleted = item.status.equals("COMPLETED", ignoreCase = true)

            if (isCompleted) {
                //  Completed state
                ivStatus.setImageResource(R.drawable.ic_checklist_complete)
                ivFlameSmall.visibility = View.VISIBLE
                card.setCardBackgroundColor(
                    itemView.context.getColor(R.color.streak_completed_bg)
                )
            } else {
                //  Missed state
                ivStatus.setImageResource(R.drawable.ic_checklist_tick_bg)
                ivFlameSmall.visibility = View.GONE
                card.setCardBackgroundColor(
                    itemView.context.getColor(android.R.color.white)
                )
            }
        }
    }
}
