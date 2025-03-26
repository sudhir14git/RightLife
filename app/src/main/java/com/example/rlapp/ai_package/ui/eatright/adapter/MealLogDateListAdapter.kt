package com.example.rlapp.ai_package.ui.eatright.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.rlapp.R
import com.example.rlapp.ai_package.model.DailyRecipe

class MealLogDateListAdapter(private val context: Context, private var dataLists: ArrayList<DailyRecipe>,
                             private var clickPos: Int, private var mealLogListData : DailyRecipe?,
                             private var isClickView : Boolean, val onMealLogDateItem: (DailyRecipe, Int, Boolean) -> Unit,) :
    RecyclerView.Adapter<MealLogDateListAdapter.ViewHolder>() {

    private var selectedItem = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_meal_log_date_layout_ai, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataLists[position]

        val day = item.currentDay.first()
        holder.mealDay.text = day.toString()
        val date = item.date.takeLast(2)
        holder.mealDate.text = date
        if (item.status == true) {
            holder.circleStatus.setImageResource(R.drawable.circle_check)
            if (mealLogListData != null){
                if (clickPos == position && mealLogListData == item && isClickView == true){
                    holder.layoutMain.setBackgroundResource(R.drawable.dark_green_meal_bg)
                    holder.mealDay.setTextColor(ContextCompat.getColor(context,R.color.white))
                    holder.mealDate.setTextColor(ContextCompat.getColor(context,R.color.white))
                }else{
                    holder.layoutMain.setBackgroundResource(R.drawable.white_meal_date_bg)
                    holder.mealDay.setTextColor(ContextCompat.getColor(context,R.color.black_no_meals))
                    holder.mealDate.setTextColor(ContextCompat.getColor(context,R.color.black_no_meals))
                }
            }
        }else{
            holder.mealDay.setTextColor(ContextCompat.getColor(context,R.color.black_no_meals))
            holder.mealDate.setTextColor(ContextCompat.getColor(context,R.color.black_no_meals))
            holder.circleStatus.setImageResource(R.drawable.circle_uncheck)
            if (mealLogListData != null){
                if (clickPos == position && mealLogListData == item && isClickView == true){
                    holder.layoutMain.setBackgroundResource(R.drawable.dark_green_meal_bg)
                    holder.mealDay.setTextColor(ContextCompat.getColor(context,R.color.white))
                    holder.mealDate.setTextColor(ContextCompat.getColor(context,R.color.white))
                }else{
                    holder.layoutMain.setBackgroundResource(R.drawable.white_meal_date_bg)
                    holder.mealDay.setTextColor(ContextCompat.getColor(context,R.color.black_no_meals))
                    holder.mealDate.setTextColor(ContextCompat.getColor(context,R.color.black_no_meals))
                }
            }
        }

        holder.layoutMain.setOnClickListener {
            onMealLogDateItem(item, position, true)
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

    fun addAll(item : ArrayList<DailyRecipe>?, pos: Int, mealLogItem : DailyRecipe?, isClick : Boolean) {
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