package com.jetsynthesys.rightlife.ai_package.ui.thinkright.fragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jetsynthesys.rightlife.R

class StatCardAdapter(private val items: List<MindfullChartRenderer.StatCard>) :
    RecyclerView.Adapter<StatCardAdapter.StatCardViewHolder>() {

    inner class StatCardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvSubtitle: TextView = itemView.findViewById(R.id.tvSubtitle)
        val tvCount: TextView = itemView.findViewById(R.id.tvCount)
        val tvFooter: TextView = itemView.findViewById(R.id.tvFooter)
        val imageView: ImageView = itemView.findViewById(R.id.midfulness_image_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatCardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_stat_card, parent, false)
        return StatCardViewHolder(view)
    }

    override fun onBindViewHolder(holder: StatCardViewHolder, position: Int) {
        val item = items[position]
        holder.tvTitle.text = item.title
        holder.tvSubtitle.text = item.subtitle
        holder.tvCount.text = item.count.toString()
        holder.tvFooter.text = item.footer
        holder.imageView.setImageResource(item.imageResId)
    }

    override fun getItemCount(): Int = items.size
}
