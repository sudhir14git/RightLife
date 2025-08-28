package com.jetsynthesys.rightlife.ai_package.ui.eatright.adapter.tab

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ai_package.model.response.FrequentRecipe
import com.jetsynthesys.rightlife.ai_package.ui.eatright.model.MyMealModel

class FrequentlyLoggedListAdapter(private val context: Context, private var dataLists: ArrayList<FrequentRecipe>,
                                  private var clickPos: Int, private var mealLogListData : FrequentRecipe?,
                                  private var isClickView : Boolean, val onFrequentlyLoggedItem: (FrequentRecipe, Int, Boolean) -> Unit,) :
    RecyclerView.Adapter<FrequentlyLoggedListAdapter.ViewHolder>() {

    private var selectedItem = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_frequently_looged_ai, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataLists[position]

        holder.mealNameTv.text = item.recipe_name
        holder.servesCount.text = item.servings.toString()
        holder.calValue.text = item.calories.toInt().toString()
        holder.subtractionValue.text = item.protein.toInt().toString()
        holder.baguetteValue.text = item.carbs.toInt().toString()
        holder.dewpointValue.text = item.fat.toInt().toString()
 //       if (isClickView == true) {
//            holder.mealDay.setTextColor(ContextCompat.getColor(context,R.color.black_no_meals))
//            holder.mealDate.setTextColor(ContextCompat.getColor(context,R.color.black_no_meals))
//            holder.circlePlus.setImageResource(R.drawable.circle_check)
        if (item.isFrequentLog) {
           // if (clickPos == position && mealLogListData == item && isClickView == true){
                holder.circlePlus.setImageResource(R.drawable.circle_check)
           // }
        }else{
           // if (clickPos == position && mealLogListData == item && isClickView == true){
                holder.circlePlus.setImageResource(R.drawable.ic_plus_circle)
          //  }
        }
       // }else{
//            holder.mealDay.setTextColor(ContextCompat.getColor(context,R.color.black_no_meals))
//            holder.mealDate.setTextColor(ContextCompat.getColor(context,R.color.black_no_meals))
     //       holder.circlePlus.setImageResource(R.drawable.ic_plus_circle)
//            if (mealLogListData != null){
//                if (clickPos == position && mealLogListData == item && isClickView == true){
//                    holder.layoutMain.setBackgroundResource(R.drawable.green_meal_date_bg)
//                }else{
//                    holder.layoutMain.setBackgroundResource(R.drawable.white_meal_date_bg)
//                }
//            }
  //      }

        holder.circlePlus.setOnClickListener {
            if (item.isFrequentLog){
                item.isFrequentLog = false
            }else{
                item.isFrequentLog = true
            }
            onFrequentlyLoggedItem(item, position, true)
        }

//        holder.layoutMain.setOnClickListener {
//            onMealLogDateItem(item, position, true)
//        }
    }

    override fun getItemCount(): Int {
        return dataLists.size
    }

     inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

         val mealNameTv: TextView = itemView.findViewById(R.id.mealNameTv)
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
     }

    fun addAll(item : ArrayList<FrequentRecipe>?, pos: Int, mealLogItem : FrequentRecipe?, isClick : Boolean) {
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