package com.jetsynthesys.rightlife.ai_package.ui.eatright.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ai_package.model.response.OtherRecipe
import com.jetsynthesys.rightlife.ai_package.ui.eatright.model.RecipeSuggestion

class OtherRecipeEatLandingAdapter(private val context: Context, private var dataLists: ArrayList<OtherRecipe>,
                                   private var clickPos: Int, private var mealLogListData : OtherRecipe?,
                                   private var isClickView : Boolean, val onOtherRecipeItem: (OtherRecipe, Int, Boolean) -> Unit,) :
    RecyclerView.Adapter<OtherRecipeEatLandingAdapter.ViewHolder>() {

    private var selectedItem = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_other_reciepie_eat_landing_ai, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataLists[position]

        holder.mealTitle.text = item.meal_name
        // holder.mealName.text = item.mealName
       // holder.iamgeFood.text = item.serve
        val imageUrl = getDriveImageUrl(item.image)
        Glide.with(context)
            .load(imageUrl)
            .placeholder(R.drawable.ic_breakfast)
            .error(R.drawable.ic_breakfast)
            .into(holder.iamgeFood)
        holder.calValue.text = item.calories.toInt().toString()
        holder.subtractionValue.text = item.protein.toInt().toString()
        holder.baguetteValue.text = item.carbs.toInt().toString()
        holder.dewpointValue.text = item.fats.toInt().toString()
     //   if (item.isAddDish == true) {
//            holder.mealDay.setTextColor(ContextCompat.getColor(context,R.color.black_no_meals))
//            holder.mealDate.setTextColor(ContextCompat.getColor(context,R.color.black_no_meals))
   //         holder.circlePlus.setImageResource(R.drawable.circle_check)
//            if (mealLogListData != null){
//                if (clickPos == position && mealLogListData == item && isClickView == true){
//                    holder.layoutMain.setBackgroundResource(R.drawable.green_meal_date_bg)
//                }else{
//                    holder.layoutMain.setBackgroundResource(R.drawable.white_meal_date_bg)
//                }
//            }
  //      }else{
//            holder.mealDay.setTextColor(ContextCompat.getColor(context,R.color.black_no_meals))
//            holder.mealDate.setTextColor(ContextCompat.getColor(context,R.color.black_no_meals))
   //         holder.circlePlus.setImageResource(R.drawable.ic_plus_circle)
//            if (mealLogListData != null){
//                if (clickPos == position && mealLogListData == item && isClickView == true){
//                    holder.layoutMain.setBackgroundResource(R.drawable.green_meal_date_bg)
//                }else{
//                    holder.layoutMain.setBackgroundResource(R.drawable.white_meal_date_bg)
//                }
//            }
   //     }

//        holder.circlePlus.setOnClickListener {
//            onOtherReciepeDateItem(item, position, true)
//        }

        holder.layoutMain.setOnClickListener {
            onOtherRecipeItem(item, position, true)
        }
    }

    override fun getItemCount(): Int {
        return dataLists.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val layoutMain : ConstraintLayout = itemView.findViewById(R.id.layout_main)
        val iamgeFood : ImageView = itemView.findViewById(R.id.iamgeFood)
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
    }

    fun addAll(item : ArrayList<OtherRecipe>?, pos: Int, mealLogItem : OtherRecipe?, isClick : Boolean) {
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