package com.jetsynthesys.rightlife.ai_package.ui.eatright.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ai_package.model.response.IngredientRecipeDetails
import kotlin.math.round

class SnapMealLogsAdapter(val context: Context, private var dataLists: ArrayList<IngredientRecipeDetails>,
                          private var clickPos: Int, private var snapMealData : IngredientRecipeDetails?, private var isClickView : Boolean,
                          val onBreakFastSnapMealDeleteItem: (IngredientRecipeDetails, Int, Boolean) -> Unit,
                          val onBreakFastSnapMealEditItem: (IngredientRecipeDetails, Int, Boolean) -> Unit, val isLogs : Boolean) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_SNAP_MEAL = 0
    }

    override fun getItemViewType(position: Int): Int {
        return when (dataLists[position]) {
            is IngredientRecipeDetails -> TYPE_SNAP_MEAL
            else -> {
                TYPE_SNAP_MEAL
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_SNAP_MEAL -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_lunch_meal_logs_ai, parent, false)
                SnapMealViewHolder(view)
            }
            else -> throw IllegalArgumentException("Unknown view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = dataLists[position]) {
            is IngredientRecipeDetails -> (holder as SnapMealViewHolder).bind(item)
        }
    }

    override fun getItemCount(): Int {
        return dataLists.size
    }

    inner class SnapMealViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(data: IngredientRecipeDetails) {
            val mealTimeTv : TextView = itemView.findViewById(R.id.tv_eat_time)
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
            val imageProtein: ImageView = itemView.findViewById(R.id.imageProtein)
            val proteinValue: TextView = itemView.findViewById(R.id.tvProteinValue)
            val proteinUnit: TextView = itemView.findViewById(R.id.tvProteinUnit)
            val imageCarbs: ImageView = itemView.findViewById(R.id.imageCarbs)
            val carbsValue: TextView = itemView.findViewById(R.id.tvCarbsValue)
            val carbsUnit: TextView = itemView.findViewById(R.id.tvCarbsUnit)
            val imageFats: ImageView = itemView.findViewById(R.id.imageFats)
            val fatsValue: TextView = itemView.findViewById(R.id.tvFatsValue)
            val fatsUnit: TextView = itemView.findViewById(R.id.tvFatsUnit)
            val layoutVegNonveg : LinearLayoutCompat = itemView.findViewById(R.id.layout_veg_nonveg)
            val layoutEatTime : LinearLayoutCompat = itemView.findViewById(R.id.layout_eat_time)
            val servesLayout : LinearLayoutCompat = itemView.findViewById(R.id.servesLayout)

            if (isLogs){
                delete.visibility = View.GONE
                edit.visibility = View.GONE
                layoutEatTime.visibility = View.GONE
                layoutVegNonveg.visibility = View.GONE
                servesLayout.visibility = View.VISIBLE
            }else {
                delete.visibility = View.VISIBLE
                edit.visibility = View.VISIBLE
                layoutEatTime.visibility = View.VISIBLE
                layoutVegNonveg.visibility = View.VISIBLE
                servesLayout.visibility = View.VISIBLE
            }
            val snapData = data
            if (snapData != null){
//                if (data!!.isNotEmpty()){
//                    val mealNames  = data.dish!!.map { it.name }
//                    val name = mealNames.joinToString(", ")
//                    val capitalized = name.replaceFirstChar { it.uppercase() }
//                    mealName.text = capitalized
//                }else{
                val capitalized = data.food_name?.replaceFirstChar { it.uppercase() }
                    mealName.text = capitalized
 //               }
                servesCount.text = "1"
                val mealTime = ""
                mealTimeTv.text = ""//mealTime.toInt().toString()
                calValue.text = round(snapData.calories_kcal!!)?.toInt().toString()
                proteinValue.text = round(snapData.protein_g!!)?.toInt().toString()
                carbsValue.text = round(snapData.carbs_g!!)?.toInt().toString()
                fatsValue.text = round(snapData.fat_g!!)?.toInt().toString()
                var imageUrl : String? = ""
                imageUrl = if (data.photo_url.contains("drive.google.com")) {
                    getDriveImageUrl(data.photo_url)
                }else{
                    data.photo_url
                }
                Glide.with(this.itemView)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_view_meal_place)
                    .error(R.drawable.ic_view_meal_place)
                    .into(mealImage)
            }
//            delete.setOnClickListener {
//                onBreakFastSnapMealDeleteItem(data, bindingAdapterPosition, true)
//            }
//            edit.setOnClickListener {
//                onBreakFastSnapMealEditItem(data, bindingAdapterPosition, true)
//            }
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

    fun addAll(item : ArrayList<IngredientRecipeDetails>?, pos: Int, snapMeal : IngredientRecipeDetails?,
               isClick : Boolean) {
        dataLists.clear()
        if (item != null) {
            dataLists = item
            clickPos = pos
            snapMealData = snapMeal
            isClickView = isClick
        }
        notifyDataSetChanged()
    }
}