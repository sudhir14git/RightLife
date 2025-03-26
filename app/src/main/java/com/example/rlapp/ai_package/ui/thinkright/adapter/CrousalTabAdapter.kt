package com.example.rlapp.ai_package.ui.thinkright.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.rlapp.R
import com.example.rlapp.ai_package.model.CardItem

class CrousalTabAdapter(
    private val cardItems: List<CardItem> // List of card data
) : RecyclerView.Adapter<CrousalTabAdapter.CarouselViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarouselViewHolder {
        // Inflate the CardView layout
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_think_right_tab_ai, parent, false)
        return CarouselViewHolder(view)
    }

    override fun onBindViewHolder(holder: CarouselViewHolder, position: Int) {
        // Bind data to the CardView
        val item = cardItems[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return cardItems.size
    }

    class CarouselViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
      //  private val cardTitle: TextView = itemView.findViewById(R.id.functional_strength_heading)
        //private val cardDescription: TextView = itemView.findViewById(R.id.cardDescription)

        fun bind(item: CardItem) {
          //  cardTitle.text = item.title
            //  cardDescription.text = item.description
        }
    }
}