package com.example.rlapp.ai_package.ui.moveright

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
import com.example.rlapp.R
import com.example.rlapp.ai_package.ui.eatright.fragment.tab.frequentlylogged.LoggedBottomSheet
import com.example.rlapp.ai_package.ui.eatright.model.MyMealModel

class MyRoutineListAdapter(private val context: Context, private var dataLists: ArrayList<MyMealModel>,
                           private var clickPos: Int, private var mealLogListData : MyMealModel?,
                           private var isClickView : Boolean, val onMealLogDateItem: (MyMealModel, Int, Boolean) -> Unit,) :
    RecyclerView.Adapter<MyRoutineListAdapter.ViewHolder>() {

    private var selectedItem = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_my_routine_list_ai, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val item = dataLists[position]

        holder.mealTitle.text = item.mealType
        holder.mealName.text = item.mealName
        holder.servesCount.text = item.serve
        holder.calValue.text = item.cal
        holder.subtractionValue.text = item.subtraction
        holder.baguetteValue.text = item.baguette
        holder.dewpointValue.text = item.dewpoint
        holder.edit.setOnClickListener {

        }
        holder.addToWorkout.setOnClickListener {
            val bottomSheet = LoggedBottomSheet()
            bottomSheet.show((context as AppCompatActivity).supportFragmentManager, "EditWorkoutBottomSheet")
        }
        holder.editDeleteLayout.visibility = if (selectedItem == position) View.VISIBLE else View.GONE
        holder.deleteLayout.setOnClickListener {
            val bottomSheet = DeleteWorkoutBottomSheet()
            bottomSheet.show((context as AppCompatActivity).supportFragmentManager, "EditWorkoutBottomSheet")
            holder.editDeleteLayout.visibility = View.GONE
        }

        // Handle three-dot click
        holder.threedots.setOnClickListener {
            // If the same item is clicked again, toggle visibility
            if (selectedItem == position) {
                selectedItem = -1 // Hide the menu
            } else {
                // Show the menu for the clicked item and hide the previous one
                val previousSelectedItem = selectedItem
                selectedItem = position

                // Notify the adapter to update the previous and current items
                if (previousSelectedItem != -1) {
                    notifyItemChanged(previousSelectedItem)
                }
                notifyItemChanged(position)
            }
        }
//        if (item.status == true) {
//            holder.mealDay.setTextColor(ContextCompat.getColor(context,R.color.black_no_meals))
//            holder.mealDate.setTextColor(ContextCompat.getColor(context,R.color.black_no_meals))
//            holder.circleStatus.setImageResource(R.drawable.circle_check)
//            if (mealLogListData != null){
//                if (clickPos == position && mealLogListData == item && isClickView == true){
//                    holder.layoutMain.setBackgroundResource(R.drawable.green_meal_date_bg)
//                }else{
//                    holder.layoutMain.setBackgroundResource(R.drawable.white_meal_date_bg)
//                }
//            }
//        }else{
//            holder.mealDay.setTextColor(ContextCompat.getColor(context,R.color.black_no_meals))
//            holder.mealDate.setTextColor(ContextCompat.getColor(context,R.color.black_no_meals))
//            holder.circleStatus.setImageResource(R.drawable.circle_uncheck)
//            if (mealLogListData != null){
//                if (clickPos == position && mealLogListData == item && isClickView == true){
//                    holder.layoutMain.setBackgroundResource(R.drawable.green_meal_date_bg)
//                }else{
//                    holder.layoutMain.setBackgroundResource(R.drawable.white_meal_date_bg)
//                }
//            }
        //     }

//        holder.layoutMain.setOnClickListener {
//           // holder.createNewVersionCard.visibility = View.GONE
//            onMealLogDateItem(item, position, true)
//        }
    }

    override fun getItemCount(): Int {
        return dataLists.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val mealTitle: TextView = itemView.findViewById(R.id.tv_meal_title)
        val delete: ImageView = itemView.findViewById(R.id.image_delete)
        val deleteLayout: LinearLayoutCompat = itemView.findViewById(R.id.layout_delete)
        val edit: ImageView = itemView.findViewById(R.id.image_edit)
        val editDeleteLayout: CardView = itemView.findViewById(R.id.btn_edit_delete)
        val addToWorkout: LinearLayoutCompat = itemView.findViewById(R.id.layout_btn_log)
        val circlePlus : ImageView = itemView.findViewById(R.id.image_circle_plus)
        val threedots : ImageView = itemView.findViewById(R.id.image_circle_plus)
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

    fun addAll(item : ArrayList<MyMealModel>?, pos: Int, mealLogItem : MyMealModel?, isClick : Boolean) {
        dataLists.clear()
        if (item != null) {
            dataLists = item
            clickPos = pos
            mealLogListData = mealLogItem
            isClickView = isClick
        }
        notifyDataSetChanged()
    }
}