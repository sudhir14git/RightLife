package com.example.rlapp.ai_package.ui.eatright.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.rlapp.R
import com.example.rlapp.ai_package.model.MealList
import com.example.rlapp.ai_package.ui.eatright.model.BreakfastMealModel

class YourBreakfastMealLogsAdapter(private val context: Context, private var dataLists: ArrayList<MealList>,
                                   private var clickPos: Int, private var mealLogListData : MealList?,
                                   private var isClickView : Boolean, val onMealLogDateItem: (BreakfastMealModel, Int, Boolean) -> Unit,) :
    RecyclerView.Adapter<YourBreakfastMealLogsAdapter.ViewHolder>() {

    private var selectedItem = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_breakfast_meal_logs_ai, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataLists[position]

      //  holder.mealTitle.text = item.mealType
        holder.mealName.text = item.name
        holder.servesCount.text = item.numOfServings.toString()
        val mealTime = item.cookingTime.split(" ")[0]
        holder.mealTime.text = mealTime
        holder.calValue.text = item.calories.toInt().toString()
        holder.subtractionValue.text = item.carbs.toInt().toString()
        holder.baguetteValue.text = item.protein.toInt().toString()
        holder.dewpointValue.text = item.fats.toInt().toString()
        Glide.with(context)
            .load(item.image)
            .placeholder(R.drawable.ic_breakfast)
            .error(R.drawable.ic_breakfast)
            .into(holder.mealImage)
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

         val mealTime: TextView = itemView.findViewById(R.id.tv_eat_time)
//         val delete: ImageView = itemView.findViewById(R.id.image_delete)
//         val edit: ImageView = itemView.findViewById(R.id.image_edit)
//         val circlePlus : ImageView = itemView.findViewById(R.id.image_circle_plus)
         val mealImage : ImageView = itemView.findViewById(R.id.image_meal)
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

    fun addAll(item : ArrayList<MealList>?, pos: Int, mealLogItem : MealList?, isClick : Boolean) {
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