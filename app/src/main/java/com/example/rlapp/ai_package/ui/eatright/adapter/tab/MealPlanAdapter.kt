package com.example.rlapp.ai_package.ui.eatright.adapter.tab

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.rlapp.R
import com.example.rlapp.ai_package.ui.eatright.model.MealPlanModel

class MealPlanAdapter(private val context: Context, private var dataLists: ArrayList<MealPlanModel>,
                      private var clickPos: Int, private var mealLogListData : MealPlanModel?,
                      private var isClickView : Boolean, val onMealLogDateItem: (MealPlanModel, Int, Boolean) -> Unit,) :
    RecyclerView.Adapter<MealPlanAdapter.ViewHolder>() {

    private var selectedItem = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_meal_plan_ai, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataLists[position]

        holder.mealTitle.text = item.mealType
        holder.mealName.text = item.mealName
        holder.servesCount.text = item.serve
        holder.calValue.text = item.cal
        holder.subtractionValue.text = item.subtraction
        holder.baguetteValue.text = item.baguette
        holder.dewpointValue.text = item.dewpoint
        if (item.mealType.contentEquals("Breakfast")){
            holder.mealTypeIc.setImageResource(R.drawable.ic_breakfast)
        }else if (item.mealType.contentEquals("Lunch")){
            holder.mealTypeIc.setImageResource(R.drawable.ic_lunch)
        }else{
            holder.mealTypeIc.setImageResource(R.drawable.ic_dinner)
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
         val edit: ImageView = itemView.findViewById(R.id.image_edit)
         val circlePlus : ImageView = itemView.findViewById(R.id.image_circle_plus)
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
         val mealTypeIc : ImageView = itemView.findViewById(R.id.image_meal_type)
     }

    fun addAll(item : ArrayList<MealPlanModel>?, pos: Int, mealLogItem : MealPlanModel?, isClick : Boolean) {
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