package com.example.rlapp.ai_package.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.rlapp.R
import com.example.rlapp.ai_package.model.WorkoutList

class WorkoutAdapter(private var context :Context, private var workoutList: List<WorkoutList>, private val onItemClick: (WorkoutList) -> Unit) :
    RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder>() {

    inner class WorkoutViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val workoutTitle: TextView = view.findViewById(R.id.textTitle)
        private val workoutImage: ImageView = view.findViewById(R.id.iconUrl)
        private val forwardIcon: ImageView = view.findViewById(R.id.imageRightArrow)

        fun bind(item: WorkoutList) {
            workoutTitle.text = item.title
            val imageBaseUrl = "https://d1sacaybzizpm5.cloudfront.net/" + item.iconUrl
            Glide.with(context)
                .load(imageBaseUrl)
                .placeholder(R.drawable.athelete_search)
                .error(R.drawable.athelete_search)
                .into(workoutImage)
          //  forwardIcon.setImageResource(item.icon2)

            // Handle item click
            itemView.setOnClickListener { onItemClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_layout_ai, parent, false)
        return WorkoutViewHolder(view)
    }

    override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int) {
        holder.bind(workoutList[position])
    }

    override fun getItemCount(): Int = workoutList.size

    fun updateList(newList: List<WorkoutList>) {
        workoutList = newList
        notifyDataSetChanged()
    }
}
