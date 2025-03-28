package com.example.rlapp.ai_package.ui.eatright.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.rlapp.R
import com.example.rlapp.ai_package.ui.eatright.model.CalendarDateModel

class CalendarAdapter(private val context: Context, private var dataLists: ArrayList<CalendarDateModel>,
                      private var clickPos: Int, private var mealLogListData : CalendarDateModel?,
                      private var isClickView : Boolean, val onMealLogCalenderItem: (CalendarDateModel, Int, Boolean) -> Unit,) :
    RecyclerView.Adapter<CalendarAdapter.ViewHolder>() {

    private var selectedItem = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_calendar_day_ai, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataLists[position]

        holder.txtDate.text = item.date.toString()
        holder.txtMonth.text = item.month
        if (item.month.contentEquals(item.currentMonth)){
            val day = item.currentDate.split(" ")[1]
            if (day.contentEquals(item.date.toString())){
                holder.viewDate.visibility = View.VISIBLE
            }else{
                holder.viewDate.visibility = View.INVISIBLE
            }
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context,R.color.white))
        }else{
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context,R.color.meal_log_background))
        }

        if (item.date == 1){
            if (item.month.contentEquals("Jan")){
                holder.txtMonth.visibility = View.VISIBLE
            }else if(item.month.contentEquals("Feb")){
                holder.txtMonth.visibility = View.VISIBLE
            }else{
                holder.txtMonth.visibility = View.VISIBLE
            }
        }else{
            holder.txtMonth.visibility = View.INVISIBLE
        }

        if (item.isSelected){
            holder.imgCheck.setImageResource(R.drawable.ic_check_circle)


            if (mealLogListData != null){
                if (clickPos == position && mealLogListData == item && isClickView == true){
                    holder.imgCheck.setImageResource(R.drawable.ic_check_circle)
                }else{
                    holder.imgCheck.setImageResource(R.drawable.ic_check_circle)
                }
            }
        }else{
            //holder.imgCheck.setImageResource(R.drawable.ic_uncheck_circle)
            if (mealLogListData != null){
                if (clickPos == position && mealLogListData == item && isClickView == true){
                    holder.imgCheck.setImageResource(R.drawable.ic_check_circle)
                }else{
                    holder.imgCheck.setImageResource(R.drawable.ic_uncheck_circle)
                }
            }
        }

        holder.layoutCalendar.setOnClickListener {
            onMealLogCalenderItem(item, position, true)
        }
    }

    override fun getItemCount(): Int {
        return dataLists.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val txtMonth: TextView = itemView.findViewById(R.id.txtMonth)
        val txtDate: TextView = itemView.findViewById(R.id.txtDate)
        val viewDate: View = itemView.findViewById(R.id.viewDate)
        val imgCheck: ImageView = itemView.findViewById(R.id.imgCheck)
        val layoutCalendar : LinearLayoutCompat = itemView.findViewById(R.id.layoutCalendar)
        val layoutMain : LinearLayoutCompat = itemView.findViewById(R.id.layoutMain)
    }

    fun addAll(item : ArrayList<CalendarDateModel>?, pos: Int, mealLogItem : CalendarDateModel?, isClick : Boolean) {
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
