package com.jetsynthesys.rightlife.ai_package.ui.eatright.adapter.tab.createmeal

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ai_package.model.response.IngredientRecipeDetails


class DishListAdapter(private val context: Context, private var dataLists: ArrayList<IngredientRecipeDetails>,
                      private var clickPos: Int, private var mealLogListData : IngredientRecipeDetails?,
                      private var isClickView : Boolean, val onMealLogClickItem: (IngredientRecipeDetails, Int, Boolean) -> Unit,
                      val onMealLogDeleteItem: (IngredientRecipeDetails, Int, Boolean) -> Unit,
                      val onMealLogEditItem: (IngredientRecipeDetails, Int, Boolean) -> Unit) :
    RecyclerView.Adapter<DishListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_meal_logs_ai, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataLists[position]

      //  holder.mealTitle.text = item.mealType
        val capitalized = item.recipe.toString().replaceFirstChar { it.uppercase() }
        holder.mealName.text = capitalized
        holder.servesCount.text = item.selected_serving?.value?.toInt().toString() + item.selected_serving?.type
        if (item.active_cooking_time_min != null){
            val mealTime = item.active_cooking_time_min.toString()
            holder.mealTime.text = mealTime
        }
        var value : Double = 0.0
        if (item.quantity != null){
            value = 1.0
        }else{
            value = 1.0
        }
        holder.calValue.text = item.calories_kcal?.times(value)?.toInt().toString()
        holder.subtractionValue.text = item.protein_g?.times(value)?.toInt().toString()
        holder.baguetteValue.text = item.carbs_g?.times(value)?.toInt().toString()
        holder.dewpointValue.text = item.fat_g?.times(value)?.toInt().toString()
        var imageUrl : String? = ""
        imageUrl = if (item.photo_url.contains("drive.google.com")) {
            getDriveImageUrl(item.photo_url)
        }else{
            item.photo_url
        }
        Glide.with(this.context)
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
            onMealLogDeleteItem(item, position, true)
        }

        holder.edit.setOnClickListener {
            onMealLogEditItem(item, position, true)
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

    fun addAll(item : ArrayList<IngredientRecipeDetails>?, pos: Int, mealLogItem : IngredientRecipeDetails?, isClick : Boolean) {
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