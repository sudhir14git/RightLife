package com.jetsynthesys.rightlife.ai_package.ui.thinkright.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ai_package.model.WorkoutRoutineItem
import com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment.tab.frequentlylogged.LoggedBottomSheet
import com.jetsynthesys.rightlife.ai_package.ui.moveright.DeleteRoutineBottomSheet
import com.jetsynthesys.rightlife.ai_package.ui.moveright.MyRoutineFragment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MyRoutineMainListAdapter(
    private val context: Context,
    private var dataLists: ArrayList<WorkoutRoutineItem>,
    private var clickPos: Int,
    private val fragment: MyRoutineFragment,
    private var workoutItem: WorkoutRoutineItem?,
    private var isClickView: Boolean,
    private val onWorkoutItemClick: (WorkoutRoutineItem, Int, Boolean) -> Unit
) : RecyclerView.Adapter<MyRoutineMainListAdapter.ViewHolder>() {

    private var selectedItem = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_routine_list_main_ai, parent, false)
        return ViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val item = dataLists[position]

        holder.mealTitle.text = item.routineName
        holder.mealName.text = item.activityName
        holder.servesCount.text = item.duration
        holder.calValue.text = "${item.caloriesBurned} kcal"
        holder.subtractionValue.text = item.intensity
        holder.baguetteValue.text = ""
        holder.dewpointValue.text = ""

        // Set CardView visibility based on selectedItem
        holder.editDeleteLayout.visibility = if (selectedItem == position) View.VISIBLE else View.GONE

        holder.edit.setOnClickListener {
            // Implement edit functionality
        }
        holder.addToWorkout.setOnClickListener {
            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            // Call the logRoutineToServer function directly using the fragment reference
            fragment.logRoutineToServer(
                userId = item.userId ?: "67f6698fa213d14e22a47c2a",
                date = currentDate,
                routineId = item.routineId
            )

            // Optionally, you can still show the bottom sheet if needed
            val bottomSheet = LoggedBottomSheet()
            bottomSheet.show((context as AppCompatActivity).supportFragmentManager, "EditWorkoutBottomSheet")
        }
        holder.deleteLayout.setOnClickListener {
            val bottomSheet = DeleteRoutineBottomSheet.newInstance(
                calorieId = item.routineId,
                userId = item.userId // Replace with dynamic userId if available
            )
            bottomSheet.setOnDeleteSuccessListener {
                dataLists.removeAt(position)
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
        val edit: ImageView = itemView.findViewById(R.id.image_edit)
        val editDeleteLayout: CardView = itemView.findViewById(R.id.btn_edit_delete)
        val addToWorkout: LinearLayoutCompat = itemView.findViewById(R.id.layout_btn_log)
        val circlePlus: ImageView = itemView.findViewById(R.id.image_circle_plus)
        val threedots: ImageView = itemView.findViewById(R.id.image_circle_plus)
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