package com.jetsynthesys.rightlife.ai_package.ui.eatright.adapter.tab

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ai_package.model.response.MyRecipe

class MyRecipeAdapter(private val context: Context, private var dataLists: ArrayList<MyRecipe>,
                      private var clickPos: Int, private var mealLogListData : MyRecipe?,
                      private var isClickView : Boolean, val onDeleteRecipeItem: (MyRecipe, Int, Boolean) -> Unit,
                      val onEditRecipeItem: (MyRecipe, Int, Boolean) -> Unit,
                      val onLogRecipeItem: (MyRecipe, Int, Boolean) -> Unit) :
    RecyclerView.Adapter<MyRecipeAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_my_meal_ai, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataLists[position]

        val data = item.ingredients
        if (data.isNotEmpty()){
            val ingredientsNames  = data!!.map { it.ingredient_name }
            val name = ingredientsNames.joinToString(", ")
            val capitalized = name.replaceFirstChar { it.uppercase() }
            holder.mealName.text = capitalized
        }else{
            holder.mealName.text = item.recipe
        }
        holder.mealTitle.text = item.recipe
        holder.servesCount.text = item.servings.toString()
        if (item.servings != null && item.servings > 0){
            val servingsCount = item.servings
            holder.calValue.text = item.calories_kcal.times(servingsCount).toInt().toString()
            holder.subtractionValue.text = item.protein_g.times(servingsCount).toInt().toString()
            holder.baguetteValue.text = item.carbs_g.times(servingsCount).toInt().toString()
            holder.dewpointValue.text = item.fat_g.times(servingsCount).toInt().toString()
        }else{
            holder.calValue.text = item.calories_kcal.toInt().toString()
            holder.subtractionValue.text = item.protein_g.toInt().toString()
            holder.baguetteValue.text = item.carbs_g.toInt().toString()
            holder.dewpointValue.text = item.fat_g.toInt().toString()
        }

        if (item.isRecipeLog) {
           // if (clickPos == position && mealLogListData == item && isClickView == true){
                holder.circlePlus.setImageResource(R.drawable.circle_check)
           // }
        }else{
           // if (clickPos == position && mealLogListData == item && isClickView == true){
                holder.circlePlus.setImageResource(R.drawable.ic_plus_circle)
          //  }
        }

        holder.delete.setOnClickListener {
            onDeleteRecipeItem(item, position, true)
        }

        holder.circlePlus.setOnClickListener {
            if (item.isRecipeLog){
                item.isRecipeLog = false
            }else{
                item.isRecipeLog = true
            }
            onLogRecipeItem(item, position, true)
        }

        holder.edit.setOnClickListener {
            onEditRecipeItem(item, position, true)
        }
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
    }

    fun addAll(item : ArrayList<MyRecipe>?, pos: Int, mealLogItem : MyRecipe?, isClick : Boolean) {
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