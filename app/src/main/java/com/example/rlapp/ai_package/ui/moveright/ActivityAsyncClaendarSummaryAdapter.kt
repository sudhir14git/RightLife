package com.example.rlapp.ai_package.ui.moveright

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.rlapp.R
import com.example.rlapp.ai_package.ui.eatright.model.CalendarSummaryModel

class ActivityAsyncClaendarSummaryAdapter(private val context: Context, private var dataLists: ArrayList<CalendarSummaryModel>,
                                          private var clickPos: Int, private var mealLogListData : CalendarSummaryModel?,
                                          private var isClickView : Boolean, val onMealLogDateItem: (CalendarSummaryModel, Int, Boolean) -> Unit,) :
    RecyclerView.Adapter<ActivityAsyncClaendarSummaryAdapter.ViewHolder>() {

    private var selectedItem = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_summary_ai, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataLists[position]

        holder.txtSummary.text = item.surplusType
        holder.txtValue.text = item.surplusValue

        if (item.surplusType.contentEquals("Deficit")){
            holder.txtSummary.setTextColor(ContextCompat.getColor(context, R.color.week_red))
            holder.layoutValue.setBackgroundResource(R.drawable.red_circle_background)
        }else{
            holder.txtSummary.setTextColor(ContextCompat.getColor(context, R.color.border_green))
            holder.layoutValue.setBackgroundResource(R.drawable.circle_background)
        }

//        holder.txtValue.setBackgroundResource(
//            if (item.value < 0) R.drawable.bg_deficit else R.drawable.bg_surplus
//        )

//        holder.layoutMain.setOnClickListener {
//           // holder.createNewVersionCard.visibility = View.GONE
//            onMealLogDateItem(item, position, true)
//        }
    }

    override fun getItemCount(): Int {
        return dataLists.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val txtSummary: TextView = itemView.findViewById(R.id.txtSummary)
        val txtValue: TextView = itemView.findViewById(R.id.txtValue)
        val layoutValue : LinearLayoutCompat = itemView.findViewById(R.id.layoutValue)
    }

    fun addAll(item : ArrayList<CalendarSummaryModel>?, pos: Int, mealLogItem : CalendarSummaryModel?, isClick : Boolean) {
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
