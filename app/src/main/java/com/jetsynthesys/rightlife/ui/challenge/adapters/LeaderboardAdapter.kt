package com.jetsynthesys.rightlife.ui.challenge.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ui.challenge.LeaderboardUiStyler


class LeaderboardAdapter : RecyclerView.Adapter<LeaderboardAdapter.VH>() {

    data class Item(val rank: Int, val name: String, val score: Int)

    private val items = mutableListOf<Item>()

    fun submitList(newItems: List<Item>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_leaderboard_row, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val card = itemView.findViewById<CardView>(R.id.cardRoot)
        private val rankCircle = itemView.findViewById<FrameLayout>(R.id.flRank)
        private val tvRank = itemView.findViewById<TextView>(R.id.tvRank)
        private val tvName = itemView.findViewById<TextView>(R.id.tvName)
        private val tvScore = itemView.findViewById<TextView>(R.id.tvScore)

        fun bind(item: Item) {
            tvRank.text = item.rank.toString()
            tvName.text = item.name
            tvScore.text = item.score.toString()
            if (item.rank <= 3) {
                tvRank.visibility = View.GONE
            } else {
                tvRank.visibility = View.VISIBLE
                tvRank.text = item.rank.toString()
            }
            LeaderboardUiStyler.apply(item.rank, card, rankCircle)
        }
    }
}
