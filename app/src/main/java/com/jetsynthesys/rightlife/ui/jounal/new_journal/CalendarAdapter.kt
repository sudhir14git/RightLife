package com.jetsynthesys.rightlife.ui.jounal.new_journal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.jetsynthesys.rightlife.R

class CalendarAdapter(
    private var daysList: List<CalendarDay>,
    private val onItemClick: (CalendarDay) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_day, parent, false)

        // Dynamically adjust item width (for 7 items fitting the screen)
        val screenWidth = parent.resources.displayMetrics.widthPixels
        val itemWidth = screenWidth / 9
        view.layoutParams.width = itemWidth

        return CalendarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        val day = daysList[position]
        holder.bind(day)

        holder.itemView.setOnClickListener {
            // Clear previous selection
            onItemClick(day)
        }
    }

    override fun getItemCount(): Int = daysList.size

    fun updateData(newList: List<CalendarDay>) {
        daysList = newList
        notifyDataSetChanged()
    }

    class CalendarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDay: TextView = itemView.findViewById(R.id.tvDay)
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private val ivCheckMark: ImageView = itemView.findViewById(R.id.ivCheckMark)
        private val cardView: CardView = itemView.findViewById(R.id.dayContainer)

        fun bind(day: CalendarDay) {
            tvDay.text = day.day
            tvDate.text = day.date.toString()

            val context = itemView.context

            // Highlight selected day
            cardView.setCardBackgroundColor(
                ContextCompat.getColor(
                    context,
                    if (day.isSelected) R.color.btn_color_journal else R.color.white
                )
            )
            if (day.isSelected) {
                tvDay.setTextColor(ContextCompat.getColor(context, R.color.color_think_right))
                tvDate.setTextColor(ContextCompat.getColor(context, R.color.color_think_right))
            } else {
                tvDay.setTextColor(ContextCompat.getColor(context, R.color.black_plain))
                tvDate.setTextColor(ContextCompat.getColor(context, R.color.black_plain))

            }

            ivCheckMark.visibility = if (day.isSelected) View.VISIBLE else View.INVISIBLE

            //ivCheckMark.visibility = if (day.isChecked) View.VISIBLE else View.INVISIBLE
        }
    }
}