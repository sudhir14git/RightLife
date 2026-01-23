package com.jetsynthesys.rightlife.ai_package.ui.eatright.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ai_package.model.response.IngredientRecipeDetails

class SnapMealScanResultAdapter(private val context: Context, private var dataLists: ArrayList<IngredientRecipeDetails>,
                                private var clickPos: Int, private var mealLogListData : IngredientRecipeDetails?,
                                private var isClickView : Boolean, val onMenuEditItem: (IngredientRecipeDetails, Int, Boolean) -> Unit,
                                val onMenuDeleteItem: (IngredientRecipeDetails, Int, Boolean) -> Unit) :
    RecyclerView.Adapter<SnapMealScanResultAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_meal_scan_results_ai, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataLists[position]

        if (item.selected_serving != null){
            if (item.selected_serving.value != null && item.selected_serving.type != null){
                holder.mealQuantityTv.text = item.selected_serving.value?.toInt().toString() + " "  + item.selected_serving.type
            }else{
                holder.mealQuantityTv.text = "1 Serving"
            }
        }else{
            holder.mealQuantityTv.text = "1 Serving"
        }

        val capitalized = item.food_name.toString().replaceFirstChar { it.uppercase() }
        holder.mealName.text = capitalized
//        Glide.with(context)
//            .load(item.url)
//            .placeholder(R.drawable.ic_breakfast)
//            .error(R.drawable.ic_breakfast)
//            .into(holder.imageMeal)
        // holder.mealName.text = item.mealName
        if (item != null){
         //   holder.servesCount.text = "1"
            var value : Double = 0.0
            if (item.quantity != null){
                if (item.quantity > 0.0){
                    value = item.quantity
                }else{
                    value = 1.0
                }
            }else{
                value = 1.0
            }
            holder.calValue.text = String.format("%.1f", item.calories_kcal)
            holder.proteinValue.text = String.format("%.1f", item.protein_g)
            holder.cabsValue.text = String.format("%.1f", item.carbs_g)
            holder.fatsValue.text = String.format("%.1f", item.fat_g)
        }else{
          //  holder.servesCount.text = "NA"
            holder.calValue.text = "NA"
            holder.proteinValue.text = "NA"
            holder.cabsValue.text = "NA"
            holder.fatsValue.text = "NA"
        }

        holder.editDeleteMenu.setOnClickListener { view ->
            val popupMenu = PopupMenu(view.context, holder.editDeleteMenu)
            popupMenu.menuInflater.inflate(R.menu.sanp_meal_popup_menu, popupMenu.menu)
            // 🔽 Enable icons using reflection
            try {
                val fields = popupMenu.javaClass.declaredFields
                for (field in fields) {
                    if (field.name == "mPopup") {
                        field.isAccessible = true
                        val menuPopupHelper = field.get(popupMenu)
                        val classPopupHelper = Class.forName(menuPopupHelper.javaClass.name)
                        val setForceIcons =
                            classPopupHelper.getMethod("setForceShowIcon", Boolean::class.java)
                        setForceIcons.invoke(menuPopupHelper, true)
                        break
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_edit -> {
                    onMenuEditItem(item, position, true)
                    true
                }
                R.id.action_delete -> {
                    onMenuDeleteItem(item, position, true)
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

//        holder.editDeleteMenu.setOnClickListener {
//            val popup = PopupMenu(context, holder.editDeleteMenu)
//            popup.inflate(R.menu.sanp_meal_popup_menu)
//            popup.setOnMenuItemClickListener {
//                when (it.itemId) {
//                    R.id.action_edit -> {
//                        onFrequentlyLoggedItem(item.selected_portion_nutrition, position, true)
//                        true
//                    }
//                    R.id.action_delete -> {
//                        onFrequentlyLoggedItem(item.selected_portion_nutrition, position, true)
//                        true
//                    }
//                    else -> false
//                }
//            }
//            popup.show()
//        }

       // if (item.isAddDish == true) {
//            holder.mealDay.setTextColor(ContextCompat.getColor(context,R.color.black_no_meals))
//            holder.mealDate.setTextColor(ContextCompat.getColor(context,R.color.black_no_meals))
          //  holder.circlePlus.setImageResource(R.drawable.edit_green_icon)
//            if (mealLogListData != null){
//                if (clickPos == position && mealLogListData == item && isClickView == true){
//                    holder.layoutMain.setBackgroundResource(R.drawable.green_meal_date_bg)
//                }else{
//                    holder.layoutMain.setBackgroundResource(R.drawable.white_meal_date_bg)
//                }
//            }
      //  }else{
//            holder.mealDay.setTextColor(ContextCompat.getColor(context,R.color.black_no_meals))
//            holder.mealDate.setTextColor(ContextCompat.getColor(context,R.color.black_no_meals))
        //    holder.circlePlus.setImageResource(R.drawable.edit_green_icon)
//            if (mealLogListData != null){
//                if (clickPos == position && mealLogListData == item && isClickView == true){
//                    holder.layoutMain.setBackgroundResource(R.drawable.green_meal_date_bg)
//                }else{
//                    holder.layoutMain.setBackgroundResource(R.drawable.white_meal_date_bg)
//                }
//            }
   //     }

//        holder.editDeleteMenu.setOnClickListener {
//            onFrequentlyLoggedItem(item.selected_portion_nutrition, position, true)
//        }

//        holder.layoutMain.setOnClickListener {
//            onMealLogDateItem(item, position, true)
//        }
    }

    override fun getItemCount(): Int {
        return dataLists.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val imageMeal : ImageView = itemView.findViewById(R.id.image_meal)
        val mealQuantityTv: TextView = itemView.findViewById(R.id.mealQuantityTv)
    //    val delete: ImageView = itemView.findViewById(R.id.image_delete)
     //   val edit: ImageView = itemView.findViewById(R.id.image_edit)
        val editDeleteMenu : ImageView = itemView.findViewById(R.id.editDeleteMenu)
        val mealName: TextView = itemView.findViewById(R.id.tv_meal_name)
     //   val serve: ImageView = itemView.findViewById(R.id.image_serve)
 //       val serves: TextView = itemView.findViewById(R.id.tv_serves)
 //       val servesCount: TextView = itemView.findViewById(R.id.tv_serves_count)
     //   val cal: ImageView = itemView.findViewById(R.id.image_cal)
        val calValue: TextView = itemView.findViewById(R.id.tv_cal_value)
        val calUnit: TextView = itemView.findViewById(R.id.tv_cal_unit)
     //   val subtraction: ImageView = itemView.findViewById(R.id.image_subtraction)
        val proteinValue: TextView = itemView.findViewById(R.id.tv_protein_value)
        val subtractionUnit: TextView = itemView.findViewById(R.id.tv_subtraction_unit)
      //  val baguette: ImageView = itemView.findViewById(R.id.image_baguette)
        val cabsValue: TextView = itemView.findViewById(R.id.tv_cabs_value)
        val baguetteUnit: TextView = itemView.findViewById(R.id.tv_baguette_unit)
     //   val dewpoint: ImageView = itemView.findViewById(R.id.image_dewpoint)
        val fatsValue: TextView = itemView.findViewById(R.id.tv_fats_value)
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
}