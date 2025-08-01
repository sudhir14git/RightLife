package com.jetsynthesys.rightlife.ai_package.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ai_package.model.RoutineWorkoutDisplayModel
import com.jetsynthesys.rightlife.ai_package.ui.moveright.RemoveWorkoutBottomSheet

class RoutineWorkoutListAdapter(
    private val context: Context,
    private var dataList: ArrayList<RoutineWorkoutDisplayModel>,
    private val onItemClick: (RoutineWorkoutDisplayModel, Int) -> Unit,
    private val onItemRemove: (Int) -> Unit // New callback for removing item from parent fragment
) : RecyclerView.Adapter<RoutineWorkoutListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_your_workouts_ai, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataList[position]

        // Bind data to views
        holder.mealTitle.text = item.name
        var imageUrl = ""
        if (item.icon.startsWith("https")){
            imageUrl = item.icon
        }else{
            imageUrl = "https://jetsynthesisqa-us-east-1.s3-accelerate.amazonaws.com/" + item.icon
        }
        Glide.with(context)
            .load(imageUrl) // <-- your image URL string
            .into(holder.workoutIcon)
        holder.servesCount.text = item.duration
        holder.calValue.text = item.caloriesBurned
        holder.subtractionValue.text = item.intensity

        // Hide unused views
        holder.mealName.visibility = View.GONE
        holder.delete.visibility = View.GONE
        holder.circlePlus.visibility = View.VISIBLE

        // Set up edit button click listener
        holder.edit.setOnClickListener {
            val bottomSheet = RemoveWorkoutBottomSheet.newInstance(position)
            // Set callback to remove item from list
            bottomSheet.setOnRemoveSuccessListener {
                dataList.removeAt(position)
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, dataList.size)
                onItemRemove(position) // Notify parent fragment to remove from workoutList
            }
            bottomSheet.show((context as AppCompatActivity).supportFragmentManager, "RemoveWorkoutBottomSheet")
        }

        // Set up item click listener
        holder.itemView.setOnClickListener {
            onItemClick(item, position)
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mealTitle: TextView = itemView.findViewById(R.id.tv_meal_title)
        val workoutIcon: ImageView = itemView.findViewById(R.id.main_heading_icon)
        val delete: ImageView = itemView.findViewById(R.id.image_delete)
        val edit: ImageView = itemView.findViewById(R.id.image_edit)
        val circlePlus: ImageView = itemView.findViewById(R.id.image_circle_plus)
        val mealName: TextView = itemView.findViewById(R.id.tv_meal_name)
        val serve: ImageView = itemView.findViewById(R.id.image_serve)
        val serves: TextView = itemView.findViewById(R.id.tv_serves)
        val servesCount: TextView = itemView.findViewById(R.id.tv_serves_count)
        val cal: ImageView = itemView.findViewById(R.id.image_cal)
        val calValue: TextView = itemView.findViewById(R.id.tv_cal_value)
        val calUnit: TextView = itemView.findViewById(R.id.tv_cal_unit)
        val subtraction: ImageView = itemView.findViewById(R.id.image_subtraction)
        val subtractionValue: TextView = itemView.findViewById(R.id.tv_subtraction_value)
        val subtractionUnit: TextView = itemView.findViewById(R.id.tv_subtraction_unit)
        val baguette: ImageView = itemView.findViewById(R.id.image_baguette)
        val baguetteValue: TextView = itemView.findViewById(R.id.tv_baguette_value)
        val baguetteUnit: TextView = itemView.findViewById(R.id.tv_baguette_unit)
        val dewpoint: ImageView = itemView.findViewById(R.id.image_dewpoint)
        val dewpointValue: TextView = itemView.findViewById(R.id.tv_dewpoint_value)
        val dewpointUnit: TextView = itemView.findViewById(R.id.tv_dewpoint_unit)
    }

    fun setData(newData: ArrayList<RoutineWorkoutDisplayModel>) {
        dataList.clear()
        dataList.addAll(newData)
        notifyDataSetChanged()
    }
}