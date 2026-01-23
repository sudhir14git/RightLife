package com.jetsynthesys.rightlife.ai_package.ui.thinkright.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ai_package.model.ActivityModel
import com.jetsynthesys.rightlife.ai_package.model.WorkoutRoutineItem
import com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment.tab.frequentlylogged.LoggedBottomSheet
import com.jetsynthesys.rightlife.ai_package.ui.moveright.DeleteRoutineBottomSheet
import com.jetsynthesys.rightlife.ai_package.ui.moveright.DeleteWorkoutBottomSheet
import com.jetsynthesys.rightlife.ui.utility.AnalyticsEvent
import com.jetsynthesys.rightlife.ui.utility.AnalyticsLogger
import com.jetsynthesys.rightlife.ui.utility.AnalyticsParam
import kotlin.math.roundToInt

class MyRoutineMainListAdapter(
    private val context: Context,
    private var dataLists: ArrayList<WorkoutRoutineItem>,
    private var clickPos: Int,
    private var workoutItem: WorkoutRoutineItem?,
    private var isClickView: Boolean,
    private val onCirclePlusClick: (WorkoutRoutineItem, Int) -> Unit,
    private val onAddToLogButtonClick: (WorkoutRoutineItem) -> Unit,
    private val onWorkoutItemClick: (WorkoutRoutineItem, Int, Boolean) -> Unit
) : RecyclerView.Adapter<MyRoutineMainListAdapter.ViewHolder>() {

    private var selectedItem = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_routine_list_main_ai, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val item = dataLists[position]

        holder.mealTitle.text = item.routineName
        holder.mealName.text = item.activityName
        val durationStr = item.duration.replace(" min", "").trim() // Remove "min" if present
        val durationInMinutes = durationStr.toDoubleOrNull()?.roundToInt() ?: 0 // Handle decimal like "1890.6"
        val hours = durationInMinutes / 60
        val minutes = durationInMinutes % 60
        holder.min_serves.text = minutes.toString()
       // holder.min_serves.text = minutes.toString()
        holder.serves.text = hours.toString()
        val durationStr1 = item.caloriesBurned
        val durationInInt = durationStr1.toDouble().roundToInt()
        holder.calValue.text = "${durationInInt}"
        holder.subtractionValue.text = item.intensity
        holder.baguetteValue.text = ""
        holder.dewpointValue.text = ""
        // Set CardView visibility based on selectedItem
            if (selectedItem == position) {
                holder.editDeleteLayout.visibility = View.VISIBLE
            }else {
                holder.editDeleteLayout.visibility = View.GONE
            }
        holder.edit.setOnClickListener {
            // Implement edit functionality
        }
        holder.addToWorkout.setOnClickListener {
            context?.let { it1 ->
                AnalyticsLogger.logEvent(
                    it1, AnalyticsEvent.MR_Routine_AddToLog
                )
            }
            onAddToLogButtonClick(item)
        }
        holder.layout_edit.setOnClickListener {
            onCirclePlusClick(item, position)
        }
        holder.deleteLayout.setOnClickListener {
            val bottomSheet = DeleteRoutineBottomSheet.newInstance(
                calorieId = item.routineId,
                userId = item.userId // Replace with dynamic userId if available
            )
            bottomSheet.setOnDeleteSuccessListener {
                dataLists.removeAt(position)
                selectedItem = -1
                notifyDataSetChanged()
            }
            bottomSheet.show((context as AppCompatActivity).supportFragmentManager, "EditWorkoutBottomSheet")
        }

        holder.threedots.setOnClickListener {
            val previousSelectedItem = selectedItem
            if (selectedItem == position) {
                // If the same item is clicked, hide the CardView
                selectedItem = -1
            } else {
                // Show the CardView for the new item
                selectedItem = position
            }
            // Notify changes for the previous and current items
            if (previousSelectedItem != -1 && previousSelectedItem != position) {
                notifyItemChanged(previousSelectedItem)
            }
            notifyItemChanged(position)
        }
    }

    override fun getItemCount(): Int = dataLists.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mealTitle: TextView = itemView.findViewById(R.id.tv_meal_title)
        val delete: ImageView = itemView.findViewById(R.id.image_delete)
        val deleteLayout: LinearLayoutCompat = itemView.findViewById(R.id.layout_delete)
        val layout_edit: LinearLayoutCompat = itemView.findViewById(R.id.layout_edit)
        val edit: ImageView = itemView.findViewById(R.id.image_edit)
        val editDeleteLayout: CardView = itemView.findViewById(R.id.btn_edit_delete)
        val addToWorkout: LinearLayoutCompat = itemView.findViewById(R.id.layout_btn_log)
       // val circlePlus: ImageView = itemView.findViewById(R.id.image_circle_plus)
        val threedots: ImageView = itemView.findViewById(R.id.image_circle_plus)
        val mealName: TextView = itemView.findViewById(R.id.tv_meal_name)
        val serve: ImageView = itemView.findViewById(R.id.image_serve)
        val serves: TextView = itemView.findViewById(R.id.tv_serves)
        val min_serves: TextView = itemView.findViewById(R.id.min_serves)
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

    fun addAll(items: ArrayList<WorkoutRoutineItem>?, pos: Int, workoutItem: WorkoutRoutineItem?, isClick: Boolean) {
        dataLists.clear()
        if (items != null) {
            dataLists = items
            clickPos = pos
            this.workoutItem = workoutItem
            isClickView = isClick
        }
        selectedItem = -1 // Reset selected item when new data is added
        notifyDataSetChanged()
    }
}