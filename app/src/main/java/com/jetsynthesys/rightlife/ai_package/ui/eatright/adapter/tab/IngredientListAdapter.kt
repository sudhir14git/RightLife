package com.jetsynthesys.rightlife.ai_package.ui.eatright.adapter.tab

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ai_package.model.MealDetails
import com.jetsynthesys.rightlife.ai_package.model.MealLists
import com.jetsynthesys.rightlife.ai_package.model.response.IngredientDetail
import com.jetsynthesys.rightlife.ai_package.model.response.SearchResultItem
import com.jetsynthesys.rightlife.ai_package.model.response.SnapRecipeData

class IngredientListAdapter(private val context: Context, private var dataLists: ArrayList<IngredientDetail>,
                            private var clickPos: Int, private var mealLogListData : IngredientDetail?,
                            private var isClickView : Boolean, val onIngredientClickItem: (IngredientDetail, Int, Boolean) -> Unit,
                            val onIngredientDeleteItem: (IngredientDetail, Int, Boolean) -> Unit,
                            val onIngredientEditItem: (IngredientDetail, Int, Boolean) -> Unit) :
    RecyclerView.Adapter<IngredientListAdapter.ViewHolder>() {

    private var selectedItem = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ingredient_ai, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataLists[position]

      //  holder.mealTitle.text = item.mealType
        val capitalized = item.ingredient_name.toString().replaceFirstChar { it.uppercase() }
        holder.mealName.text = capitalized
        if (item.quantity.toInt() > 0){
            holder.servesCount.text = item.quantity.toInt().toString()
        }

        if (item.unit.isNotEmpty()){
            holder.serves.text = item.unit
        }
//        if (item.cooking_time_in_seconds != null){
//            val mealTime = item.cooking_time_in_seconds.toString()
//            holder.mealTime.text = mealTime
//        }
        holder.calValue.text = item.calories?.toInt().toString()
        holder.subtractionValue.text = item.carbs?.toInt().toString()
        holder.baguetteValue.text = item.protein?.toInt().toString()
        holder.dewpointValue.text = item.fat?.toInt().toString()
        val imageUrl = getDriveImageUrl(item.photo_url!!)
        Glide.with(context)
            .load(imageUrl)
            .placeholder(R.drawable.ic_view_meal_place)
            .error(R.drawable.ic_view_meal_place)
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

        holder.delete.setOnClickListener {
            onIngredientDeleteItem(item, position, true)
        }

        holder.edit.setOnClickListener {
            onIngredientEditItem(item, position, true)
        }
    }

    override fun getItemCount(): Int {
        return dataLists.size
    }

     inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

         val mealTime: TextView = itemView.findViewById(R.id.tv_eat_time)
         val delete: ImageView = itemView.findViewById(R.id.image_delete)
         val edit: ImageView = itemView.findViewById(R.id.image_edit)
         val imageUpDown : ImageView = itemView.findViewById(R.id.imageUpDown)
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

    fun addAll(item : ArrayList<IngredientDetail>?, pos: Int, mealLogItem : IngredientDetail?, isClick : Boolean) {
        dataLists.clear()
        if (item != null) {
            dataLists = item
            clickPos = pos
            mealLogListData = mealLogItem
            isClickView = isClick
        }
        notifyDataSetChanged()
    }

    fun getDriveImageUrl(originalUrl: String): String? {
        val regex = Regex("(?<=/d/)(.*?)(?=/|$)")
        val matchResult = regex.find(originalUrl)
        val fileId = matchResult?.value
        return if (!fileId.isNullOrEmpty()) {
            "https://drive.google.com/uc?export=view&id=$fileId"
        } else {
            null
        }
    }
}