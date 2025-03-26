package com.example.rlapp.ai_package.ui.steps

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.rlapp.R
import com.example.rlapp.ai_package.model.GridItem
import com.example.rlapp.ai_package.ui.moveright.graphs.BarGraphView

class StepsGridAdapter(private val items: List<GridItem>) : RecyclerView.Adapter<StepsGridAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.steps_item_image)
        val textView: TextView = itemView.findViewById(R.id.steps_item_text)
      //  val additionalTextView: TextView = itemView.findViewById(R.id.unit_rating) // For the third parameter
     //   val fourthTextView: TextView = itemView.findViewById(R.id.rating) // For the fourth parameter
      //  val lineGraphView: LineGraphView = itemView.findViewById(R.id.line_graph)
        val barGraphView: BarGraphView = itemView.findViewById(R.id.stepsBarGraphView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.steps_grid_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.textView.text = item.name
        holder.imageView.setImageResource(item.imageRes)
     //   holder.additionalTextView.text = item.additionalInfo // Set the third parameter
    //    holder.fourthTextView.text = item.fourthParameter // Set the fourth parameter

        // Show Bar Graph if the item is "Burn", else show Line Graph
    //    if (item.name == "Burn") {
            holder.barGraphView.visibility = View.VISIBLE
       //     holder.lineGraphView.visibility = View.GONE
      //  } else {
        //    holder.lineGraphView.visibility = View.VISIBLE
       //     holder.barGraphView.visibility = View.GONE
     //   }
    }

    override fun getItemCount(): Int = items.size
}