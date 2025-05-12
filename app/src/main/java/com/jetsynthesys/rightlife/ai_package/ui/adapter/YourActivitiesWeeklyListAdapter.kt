package com.jetsynthesys.rightlife.ai_package.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ai_package.model.WorkoutWeeklyDayModel
import com.jetsynthesys.rightlife.ai_package.model.YourActivityLogMeal

class YourActivitiesWeeklyListAdapter(private val context: Context, private var dataLists: ArrayList<WorkoutWeeklyDayModel>,
                                      private var clickPos: Int, private var mealLogListData : WorkoutWeeklyDayModel?,
                                      private var isClickView : Boolean, val onWorkoutLogDateItem: (WorkoutWeeklyDayModel, Int, Boolean) -> Unit,) :
    RecyclerView.Adapter<YourActivitiesWeeklyListAdapter.ViewHolder>() {

    private var selectedItem = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_your_activities_date_layout_ai, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataLists[position]

        val day = item.dayName
        holder.mealDay.text = day.toString()
        val date = item.dayNumber
        holder.mealDate.text = date
        if (item.is_available == true) {
            holder.mealDay.setTextColor(ContextCompat.getColor(context, R.color.black_no_meals))
            holder.mealDate.setTextColor(ContextCompat.getColor(context, R.color.black_no_meals))
            holder.circleStatus.setImageResource(R.drawable.circle_check)
            if (mealLogListData != null){
                if (clickPos == position && mealLogListData == item && isClickView == true){
                    holder.layoutMain.setBackgroundResource(R.drawable.red_meal_date_bg)
                }else{
                    holder.layoutMain.setBackgroundResource(R.drawable.white_meal_date_bg)
                }
            }
        }else{
            holder.mealDay.setTextColor(ContextCompat.getColor(context, R.color.black_no_meals))
            holder.mealDate.setTextColor(ContextCompat.getColor(context, R.color.black_no_meals))
            holder.circleStatus.setImageResource(R.drawable.circle_uncheck)
            if (mealLogListData != null){
                if (clickPos == position && mealLogListData == item && isClickView == true){
                    holder.layoutMain.setBackgroundResource(R.drawable.red_meal_date_bg)
                }else{
                    holder.layoutMain.setBackgroundResource(R.drawable.white_meal_date_bg)
                }
            }
        }

        holder.layoutMain.setOnClickListener {
            onWorkoutLogDateItem(item, position, true)
        }
    }

    override fun getItemCount(): Int {
        return dataLists.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val mealDay: TextView = itemView.findViewById(R.id.tv_day)
        val mealDate: TextView = itemView.findViewById(R.id.tv_date)
        val circleStatus: ImageView = itemView.findViewById(R.id.image_circle)
        val layoutMain : LinearLayoutCompat = itemView.findViewById(R.id.layout_main)
    }

    fun addAll(item : ArrayList<WorkoutWeeklyDayModel>?, pos: Int, mealLogItem : WorkoutWeeklyDayModel?, isClick : Boolean) {
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