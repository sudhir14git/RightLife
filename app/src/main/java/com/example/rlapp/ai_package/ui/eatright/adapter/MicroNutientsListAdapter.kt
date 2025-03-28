package com.example.rlapp.ai_package.ui.eatright.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.rlapp.R
import com.example.rlapp.ai_package.ui.eatright.model.MicroNutrientsModel

class MicroNutientsListAdapter(private val context: Context, private var dataLists: ArrayList<MicroNutrientsModel>,
                               private var clickPos: Int, private var mealLogListData : MicroNutrientsModel?,
                               private var isClickView : Boolean, val onMicroNutrientsItem: (MicroNutrientsModel, Int, Boolean) -> Unit,) :
    RecyclerView.Adapter<MicroNutientsListAdapter.ViewHolder>() {

    private var selectedItem = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_macro_nutrientst_ai, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataLists[position]

        holder.calValue.text = item.nutrientsValue
        holder.unit.text = item.nutrientsUnit
        holder.energyType.text = item.nutrientsEnergy
        holder.icCal.visibility = View.GONE
//        if (item.status == true) {
//            holder.circleStatus.setImageResource(R.drawable.circle_check)
//            if (mealLogListData != null){
//                if (clickPos == position && mealLogListData == item && isClickView == true){
//                    holder.layoutMain.setBackgroundResource(R.drawable.dark_green_meal_bg)
//                    holder.mealDay.setTextColor(ContextCompat.getColor(context,R.color.white))
//                    holder.mealDate.setTextColor(ContextCompat.getColor(context,R.color.white))
//                }else{
//                    holder.layoutMain.setBackgroundResource(R.drawable.white_meal_date_bg)
//                    holder.mealDay.setTextColor(ContextCompat.getColor(context,R.color.black_no_meals))
//                    holder.mealDate.setTextColor(ContextCompat.getColor(context,R.color.black_no_meals))
//                }
//            }
//        }else{
//            holder.mealDay.setTextColor(ContextCompat.getColor(context,R.color.black_no_meals))
//            holder.mealDate.setTextColor(ContextCompat.getColor(context,R.color.black_no_meals))
//            holder.circleStatus.setImageResource(R.drawable.circle_uncheck)
//            if (mealLogListData != null){
//                if (clickPos == position && mealLogListData == item && isClickView == true){
//                    holder.layoutMain.setBackgroundResource(R.drawable.dark_green_meal_bg)
//                    holder.mealDay.setTextColor(ContextCompat.getColor(context,R.color.white))
//                    holder.mealDate.setTextColor(ContextCompat.getColor(context,R.color.white))
//                }else{
//                    holder.layoutMain.setBackgroundResource(R.drawable.white_meal_date_bg)
//                    holder.mealDay.setTextColor(ContextCompat.getColor(context,R.color.black_no_meals))
//                    holder.mealDate.setTextColor(ContextCompat.getColor(context,R.color.black_no_meals))
//                }
//            }
//        }

//        holder.layoutMain.setOnClickListener {
//           // holder.createNewVersionCard.visibility = View.GONE
//            onMealLogDateItem(item, position, true)
//        }
    }

    override fun getItemCount(): Int {
        return dataLists.size
    }

     inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

         val calValue: TextView = itemView.findViewById(R.id.tv_cal_value)
         val unit: TextView = itemView.findViewById(R.id.tv_unit)
         val energyType: TextView = itemView.findViewById(R.id.tv_energy_type)
         val icCal : ImageView = itemView.findViewById(R.id.image_dewpoint)
     }

    fun addAll(item : ArrayList<MicroNutrientsModel>?, pos: Int, mealLogItem : MicroNutrientsModel?, isClick : Boolean) {
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