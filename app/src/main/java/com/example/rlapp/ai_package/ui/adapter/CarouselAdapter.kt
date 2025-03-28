package com.example.rlapp.ai_package.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.rlapp.R
import com.example.rlapp.ai_package.model.CardItem
import com.example.rlapp.ai_package.ui.moveright.graphs.LineGraphViewWorkout

class CarouselAdapter(
    private val cardItems: List<CardItem>,
    private val onGraphClick: (CardItem, Int) -> Unit
) : RecyclerView.Adapter<CarouselAdapter.CarouselViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarouselViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_card_view_ai, parent, false)
        return CarouselViewHolder(view, onGraphClick)
    }

    override fun onBindViewHolder(holder: CarouselViewHolder, position: Int) {
        val item = cardItems[position]
        holder.bind(item, position)
    }

    override fun getItemCount(): Int {
        return cardItems.size
    }

    class CarouselViewHolder(
        itemView: View,
        private val onGraphClick: (CardItem, Int) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val cardTitle: TextView = itemView.findViewById(R.id.functional_strength_heading)
        private val lineGraph: LineGraphViewWorkout = itemView.findViewById(R.id.line_graph_workout)

        fun bind(item: CardItem, position: Int) {
            cardTitle.text = item.title
            lineGraph.setOnClickListener {
                onGraphClick(item, position)
            }
        }
    }
}