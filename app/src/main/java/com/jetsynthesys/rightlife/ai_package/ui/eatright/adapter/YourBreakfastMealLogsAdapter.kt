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
import com.jetsynthesys.rightlife.ai_package.model.response.MergedLogsMealItem
import com.jetsynthesys.rightlife.ai_package.model.response.RegularRecipeEntry
import com.jetsynthesys.rightlife.ai_package.model.response.SnapMeal
import kotlin.math.round

class YourBreakfastMealLogsAdapter(val context: Context, private var dataLists: ArrayList<MergedLogsMealItem>,
                                   private var clickPos: Int, private var regularRecipeEntry : RegularRecipeEntry?,
                                   private var snapMealData : SnapMeal?, private var isClickView : Boolean,
                                   val onBreakFastRegularRecipeDeleteItem: (RegularRecipeEntry, Int, Boolean) -> Unit,
                                   val onBreakFastRegularRecipeEditItem: (RegularRecipeEntry, Int, Boolean) -> Unit,
                                   val onBreakFastSnapMealDeleteItem: (SnapMeal, Int, Boolean) -> Unit,
                                   val onBreakFastSnapMealEditItem: (SnapMeal, Int, Boolean) -> Unit, val isLanding : Boolean,
                                   val isCalender : Boolean) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var selectedItem = -1

    companion object {
        private const val TYPE_REGULAR_RECIPE = 0
        private const val TYPE_SNAP_MEAL = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (dataLists[position]) {
            is MergedLogsMealItem.RegularRecipeList -> TYPE_REGULAR_RECIPE
            is MergedLogsMealItem.SnapMealList -> TYPE_SNAP_MEAL
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_REGULAR_RECIPE -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_breakfast_meal_logs_ai, parent, false)
                RegularRecipeViewHolder(view)
            }
            TYPE_SNAP_MEAL -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_breakfast_meal_logs_ai, parent, false)
                SnapMealViewHolder(view)
            }
            else -> throw IllegalArgumentException("Unknown view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = dataLists[position]) {
            is MergedLogsMealItem.RegularRecipeList -> (holder as RegularRecipeViewHolder).bind(item.entry)
            is MergedLogsMealItem.SnapMealList -> (holder as SnapMealViewHolder).bind(item.meal)
        }
    }

    override fun getItemCount(): Int {
        return dataLists.size
    }

    inner class RegularRecipeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(data : RegularRecipeEntry) {
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

            if (isLanding){
                delete.visibility = View.GONE
                edit.visibility = View.GONE
                layoutEatTime.visibility = View.GONE
                layoutVegNonveg.visibility = View.GONE
                if (isCalender){
                    servesLayout.visibility = View.VISIBLE
                }else{
                    servesLayout.visibility = View.GONE
                }
            }else {
                delete.visibility = View.VISIBLE
                edit.visibility = View.VISIBLE
                layoutEatTime.visibility = View.VISIBLE
                layoutVegNonveg.visibility = View.VISIBLE
                servesLayout.visibility = View.VISIBLE
            }

            val mealNames =  data.recipe.recipe.takeIf { r -> !r.isNullOrBlank() } ?:  data.recipe.food_name
            mealName.text = mealNames
            servesCount.text = data.recipe.servings.toString()
            val mealTime = data.recipe.active_cooking_time_min
            mealTimeTv.text = mealTime.toInt().toString()
            calValue.text = round(data.recipe.calories_kcal!!).toInt().toString()
            proteinValue.text = round(data.recipe.protein_g!!).toInt().toString()
            carbsValue.text = round(data.recipe.carbs_g!!).toInt().toString()
            fatsValue.text = round(data.recipe.fat_g!!).toInt().toString()
            val imageUrl = getDriveImageUrl(data.recipe.photo_url)
            Glide.with(this.itemView)
                .load(imageUrl)
                .placeholder(R.drawable.ic_view_meal_place)
                .error(R.drawable.ic_view_meal_place)
                .into(mealImage)

            delete.setOnClickListener {
                onBreakFastRegularRecipeDeleteItem(data, bindingAdapterPosition, true)
            }

            edit.setOnClickListener {
                onBreakFastRegularRecipeEditItem(data, bindingAdapterPosition, true)
            }
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

    inner class SnapMealViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(data: SnapMeal) {
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

            if (isLanding){
                delete.visibility = View.GONE
                edit.visibility = View.GONE
                layoutEatTime.visibility = View.GONE
                layoutVegNonveg.visibility = View.GONE
                if (isCalender){
                    servesLayout.visibility = View.VISIBLE
                }else{
                    servesLayout.visibility = View.GONE
                }
            }else {
                delete.visibility = View.VISIBLE
                edit.visibility = View.VISIBLE
                layoutEatTime.visibility = View.VISIBLE
                layoutVegNonveg.visibility = View.VISIBLE
                servesLayout.visibility = View.VISIBLE
            }

            val snapData = data.meal_nutrition_summary
            if (snapData != null){
//                if (data.dish!!.isNotEmpty()){
//                    val mealNames  = data.dish!!.map { it.name }
//                    val name = mealNames.joinToString(", ")
//                    val capitalized = name.replaceFirstChar { it.uppercase() }
//                    mealName.text = capitalized
//                }else{
                    mealName.text = data.meal_name
 //               }
                servesCount.text = "1"
                val mealTime = ""
                mealTimeTv.text = ""//mealTime.toInt().toString()
                calValue.text = round(snapData.calories_kcal)?.toInt().toString()
                proteinValue.text = round(snapData.protein_g)?.toInt().toString()
                carbsValue.text = round(snapData.carbs_g)?.toInt().toString()
                fatsValue.text = round(snapData.fat_g)?.toInt().toString()
                val imageUrl = data.image_url//getDriveImageUrl(data.photo_url)
                Glide.with(this.itemView)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_view_meal_place)
                    .error(R.drawable.ic_view_meal_place)
                    .into(mealImage)
            }

            delete.setOnClickListener {
                onBreakFastSnapMealDeleteItem(data, bindingAdapterPosition, true)
            }

            edit.setOnClickListener {
                onBreakFastSnapMealEditItem(data, bindingAdapterPosition, true)
            }
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

    fun updateList(newList : ArrayList<MergedLogsMealItem>?, pos: Int, mealLogItem : RegularRecipeEntry?, snapMeal : SnapMeal?,
               isClick : Boolean) {
        dataLists.clear()
        if (newList != null) {
            dataLists.addAll(newList)
            clickPos = pos
            regularRecipeEntry = mealLogItem
            snapMealData = snapMeal
            isClickView = isClick
            notifyDataSetChanged()
        }
    }
}